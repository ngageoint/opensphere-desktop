package io.opensphere.controlpanels.about;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.core.util.zip.Zip;
import io.opensphere.core.util.zip.ZipInputAdapter;

/**
 * Utility class for the About panel. Contains extracted filesystem methods.
 */
public class AboutUtil
{
    /** The `Success` state message. */
    static final String SUCCESS = "SUCCESS";

    /** The `Failure` state message. */
    static final String FAILURE = "FAILURE";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AboutUtil.class);

    /** The application Toolbox. */
    private final transient Toolbox myToolbox;

    /** The parent frame. */
    private final Component myParent;

    /** The path to find DB files. */
    private final String myDbPath;

    /** The path to find log files. */
    private final String myLogPath;

    /** The application runtime path. */
    private final String myRunPath;

    /**
     * Instantiates a new AboutUtil
     *
     * @param toolbox
     * @param parent
     * @param systemPropertiesMap
     */
    AboutUtil(Toolbox toolbox, Component parent, Map<String, String> systemPropertiesMap)
    {
        myToolbox = toolbox;
        myParent = parent;

        myDbPath = systemPropertiesMap.get("opensphere.db.path");
        myLogPath = systemPropertiesMap.get("log.path");
        myRunPath = systemPropertiesMap.get("opensphere.path.runtime");
    }

    /**
     * Initiates the `Save As` dialog and returns the chosen file.
     *
     * @return the file to save
     * @see io.opensphere.core.util.filesystem.MnemonicFileChooser
     */
    File initSaveAs()
    {
        File saveFile = null;
        MnemonicFileChooser fileChooser = new MnemonicFileChooser(myToolbox.getPreferencesRegistry(), null);

        int result = fileChooser.showSaveDialog(myParent, Collections.singleton(".zip"));
        if (result == JFileChooser.APPROVE_OPTION)
        {
            saveFile = fileChooser.getSelectedFile();
        }

        return saveFile;
    }

    /**
     * Creates ZIP archive with given filename.
     *
     * @param saveFile the file to save
     * @param includeDb whether or not to include the db files
     * @param toNotify the event listener to notify when complete
     * @see io.opensphere.core.util.zip.Zip
     */
    void performZipAction(File saveFile, boolean includeDb, ActionListener toNotify)
    {
        ThreadUtilities.runBackground(new Runnable()
        {
            @SuppressWarnings("boxing")
            @Override
            public void run()
            {
                List<ZipInputAdapter> inputAdapters = Zip.createAdaptersForDirectory("", new File(myLogPath), null);
                Zip.createAdaptersForDirectory("", Paths.get(myRunPath, "prefs").toFile(), inputAdapters, ZipEntry.STORED);
                if (includeDb)
                {
                    Zip.createAdaptersForDirectory("", new File(myDbPath), inputAdapters, ZipEntry.STORED);
                }

                Optional<Integer> filesize = inputAdapters.parallelStream().map(f -> (int)f.getSize()).reduce((a, b) -> a + b);

                ProgressMonitor progressMon = new ProgressMonitor(myParent, "Writing Debug Export File: " + saveFile.getName(),
                        "Writing", 0, filesize.orElse(0));
                progressMon.setMillisToPopup(0);

                try (TaskActivity ta = new TaskActivity())
                {
                    ta.setLabelValue("Exporting log files...");
                    ta.setActive(true);
                    myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

                    Zip.zipfiles(saveFile, inputAdapters, progressMon, true);

                    toNotify.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, SUCCESS));
                }
                catch (IOException e)
                {
                    if (!saveFile.delete() && LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Failed to delete file: " + saveFile.getAbsolutePath(), e);
                    }
                    JOptionPane.showMessageDialog(myParent, "Error encountered while saving export file", "File Save Error",
                            JOptionPane.ERROR_MESSAGE);

                    toNotify.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, FAILURE));
                }

                progressMon.close();
            }
        });
    }
}
