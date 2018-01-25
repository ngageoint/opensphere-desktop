package io.opensphere.core.export;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;

/**
 * Export utilities.
 */
public final class ExportUtilities
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ExportUtilities.class);

    /**
     * Exports objects using the given exporter.
     *
     * @param parentComponent The parent component for the message dialog.
     * @param prefsRegistry The preferences registry, used to remember the last
     *            browsed directory.
     * @param exporter The exporter.
     */
    public static void export(Component parentComponent, PreferencesRegistry prefsRegistry, Exporter exporter)
    {
        assert EventQueue.isDispatchThread();

        // Get the file from the user
        final File file = getFileFromUser(parentComponent, prefsRegistry, exporter);

        if (file != null)
        {
            // Do the export
            new SwingWorker<Void, Void>()
            {
                @Override
                protected Void doInBackground() throws IOException, ExportException
                {
                    exporter.export(file);
                    return null;
                }

                @Override
                protected void done()
                {
                    try
                    {
                        get();
                    }
                    catch (InterruptedException e)
                    {
                        LOGGER.error(e);
                    }
                    catch (ExecutionException e)
                    {
                        LOGGER.error(e, e);
                        StringBuilder message = new StringBuilder("Error occurred during export");
                        if (e.getCause() instanceof ExportException || e.getCause() instanceof IOException)
                        {
                            message.append(": ").append(e.getCause().getMessage());
                        }
                        else
                        {
                            message.append('.');
                        }
                        JOptionPane.showMessageDialog(parentComponent, message.toString(), "Export Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    /**
     * Gets a file from the user.
     *
     * @param parentComponent The parent component for the message dialog.
     * @param prefsRegistry The preferences registry, used to remember the last
     *            browsed directory.
     * @param exporter The exporter to use.
     * @return The file, or {@code null} if the operation was cancelled.
     */
    public static File getFileFromUser(final Component parentComponent, final PreferencesRegistry prefsRegistry,
            Exporter exporter)
    {
        assert EventQueue.isDispatchThread();

        File file = null;

        MnemonicFileChooser chooser = new MnemonicFileChooser(prefsRegistry, "ImportExport");
        String[] extensions = exporter.getMimeType().getFileExtensions();
        FileNameExtensionFilter extFilter = exporter.getMimeType().getFileFilter();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(extFilter);
        chooser.setFileFilter(extFilter);
        chooser.setAccessory(exporter.getFileChooserAccessory());

        int value = chooser.showSaveDialog(parentComponent);
        if (value == JFileChooser.APPROVE_OPTION)
        {
            Collection<? extends File> exportFiles = exporter.getExportFiles(chooser.getSelectedFile());

            // Check with the user if they are willing to overwrite all
            // necessary files
            boolean acceptAllOverwrites = true;
            for (File exportFile : exportFiles)
            {
                if (exportFile.exists())
                {
                    int choice = JOptionPane.showConfirmDialog(parentComponent, "Overwrite file \"" + exportFile + "\"?",
                            "Export to " + extensions[0], JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.NO_OPTION)
                    {
                        acceptAllOverwrites = false;
                        break;
                    }
                }
            }

            if (acceptAllOverwrites && !exportFiles.isEmpty())
            {
                file = chooser.getSelectedFile();
            }
        }

        return file;
    }

    /**
     * Private constructor.
     */
    private ExportUtilities()
    {
    }
}
