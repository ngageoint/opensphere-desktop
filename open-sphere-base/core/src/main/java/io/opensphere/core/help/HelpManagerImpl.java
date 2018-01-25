package io.opensphere.core.help;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.JHelp;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sun.java.help.search.Indexer;

import io.opensphere.core.HelpManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.help.data.PluginHelpInfo;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** This is the main class that manages the help system. */
public class HelpManagerImpl implements HelpManager
{
    /** The name of the mappings file. */
    public static final String MAPPINGS_FILE_NAME = "helpMap.xml";

    /** The name of the master help file. */
    public static final String MASTER_FILE_NAME = "helpMaster.xml";

    /** The name of the search configuration file. */
    public static final String SEARCH_CONFIG = "search.cfg";

    /** The name of the search directory. */
    public static final String SEARCH_DIR = "searchDB";

    /** The name of the search logging file. */
    public static final String SEARCH_LOG = "searchLog.txt";

    /** The name of the table of contents file. */
    public static final String TOC_FILE_NAME = "helpToc.xml";

    /** The name of the HTML directory. */
    public static final String CONTENT_NAME = "content";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(HelpManagerImpl.class);

    /** The About window. */
    private AboutDialog myAboutDialog;

    /** The root help directory where all accessory files will be kept. */
    private final File myHelpFileDirectory;

    /** The Help window. */
    private HelpFrame myHelpFrame;

    /** The index file helper. */
    private final IndexFileHelper myIndexHelper;

    /** The main java help. */
    private JHelp myMainHelp;

    /** The mappings file helper. */
    private final MapFileHelper myMapHelper;

    /** The table of contents file helper. */
    private final TOCFileHelper myTOCHelper;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public HelpManagerImpl(Toolbox toolbox)
    {
        myToolbox = toolbox;

        final StringBuilder helpDir = new StringBuilder(
                StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties()));
        helpDir.append(File.separator);
        helpDir.append("help");
        myHelpFileDirectory = new File(helpDir.toString());
        myTOCHelper = new TOCFileHelper(helpDir.toString());
        myMapHelper = new MapFileHelper(helpDir.toString());
        myIndexHelper = new IndexFileHelper(helpDir.toString());

        if (!myHelpFileDirectory.exists() && !myHelpFileDirectory.mkdir())
        {
            LOGGER.error("Unable to create help directory.");
            return;
        }

        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                // Clean up help files when app is closed.
                myToolbox.getUIRegistry().getMainFrameProvider().get().addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosing(WindowEvent e)
                    {
                        // remove table of contents XML file.
                        myTOCHelper.removeFile();
                        // remove mappings XML file.
                        myMapHelper.removeFile();
                        // remove index XML file.
                        myIndexHelper.removeFile();
                        // remove master XML file.
                        final File masterFile = new File(myHelpFileDirectory.getAbsolutePath() + File.separator + MASTER_FILE_NAME);
                        if (!masterFile.delete())
                        {
                            LOGGER.warn("Unable to remove the help system master file.");
                        }
                        // remove actual HTML help files.
                        removeAllHelpFiles();
                    }
                });
            }
        });

        // Copy the master file to the help directory
        final File masterHelpFile = new File(HelpManagerImpl.class.getResource("/help/" + MASTER_FILE_NAME).getFile());
        final File destination = new File(myHelpFileDirectory.getAbsolutePath() + File.separator + MASTER_FILE_NAME);

        try
        {
            FileUtilities.copyfile(masterHelpFile, destination);
        }
        catch (final IOException e)
        {
            LOGGER.error("Unable to copy master help file: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized boolean addHelpFiles(String name, File directory)
    {
        if (StringUtils.isBlank(name) || directory == null || !directory.isDirectory())
        {
            LOGGER.error("Specified directory is not valid.  Unable to copy help files.");
            return false;
        }

        final StringBuilder strBuilder = new StringBuilder(myHelpFileDirectory.getAbsolutePath());
        strBuilder.append(File.separator).append(CONTENT_NAME);
        strBuilder.append(File.separator).append(name);
        final File destination = new File(strBuilder.toString());

        return FileUtilities.copyDirectory(directory, destination, true);
    }

    /**
     * Add the given plugin help information to the help system.
     *
     * @param helpInfo The plugin specific help information.
     * @return True if successful, false otherwise.
     */
    @Override
    public boolean addHelpInfo(PluginHelpInfo helpInfo)
    {
        LOGGER.warn(" Adding Help info from " + helpInfo.getDisplayName());

        // Add entries to the help menu (it should be initialized now) the first
        // time
        // this is called.
        addHelpMenuOptions();

        boolean result = true;
        // Modify table of contents
        result &= myTOCHelper.addItem(helpInfo.getName(), helpInfo.getTOCInfo());

        // Modify mappings file
        result &= myMapHelper.addItem(helpInfo.getName(), helpInfo.getMappingInfo());

        // Modify indexing file
        result &= myIndexHelper.addItem(helpInfo.getName(), helpInfo.getIndexInfo());

        rebuildHelp();

        return result;
    }

    /**
     * Read in the search configuration file and initialize the search
     * functionality. This should be called last after configuration files have
     * been built.
     */
    public void buildSearch()
    {
        final StringBuffer strBuf = new StringBuffer(myHelpFileDirectory.getAbsolutePath());
        strBuf.append(File.separator);
        strBuf.append(SEARCH_DIR);
        @SuppressWarnings("PMD.PrematureDeclaration")
        final
        String dbAbsPath = strBuf.toString();
        strBuf.append(File.separator);
        strBuf.append(SEARCH_CONFIG);

        // Create the search configuration file from our mappings information.
        try
        {
            LOGGER.error("Help system search config file = " + strBuf.toString());
            final File configFile = new File(strBuf.toString());
            if (!configFile.exists())
            {
                LOGGER.error("Search config file doesn't exist");
            }
            HelpUtilities.createDBConfig(configFile, myHelpFileDirectory + File.separator + CONTENT_NAME,
                    myMapHelper.getHelpMappings());
        }
        catch (final IOException e)
        {
            LOGGER.error("Unable to create search configuration file: " + e.getMessage(), e);
            return;
        }

        final StringBuffer logPath = new StringBuffer(dbAbsPath);
        logPath.append(File.separator);
        logPath.append(SEARCH_LOG);

        final String[] args = { "-c", strBuf.toString(), "-logfile", logPath.toString(), "-db", dbAbsPath, };
        final Indexer indexer = new Indexer();
        try
        {
            indexer.compile(args);
        }
        catch (final Exception e)
        {
            LOGGER.warn("Unable to index help files.  Help search functionality will not be available. " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeHelpInfo(String name)
    {
        if (StringUtils.isBlank(name))
        {
            LOGGER.error("Unable to remove help information.");
            return false;
        }

        boolean result = true;
        result &= myTOCHelper.removeItem(name);
        result &= myMapHelper.removeItem(name);
        result &= myIndexHelper.removeItem(name);
        result &= removeHelpFiles(name);

        rebuildHelp();
        return result;
    }

    /**
     * Add the "About" and "Contents" menu options (if they haven't already been
     * added).
     */
    private void addHelpMenuOptions()
    {
        final JMenu mainMenu = myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR,
                MenuBarRegistry.HELP_MENU);

        // Check to make sure menu items haven't already been added.
        if (mainMenu.getItemCount() == 0)
        {
            final JMenuItem aboutItem = new JMenuItem("About");
            // /aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
            // InputEvent.ALT_DOWN_MASK));
            aboutItem.addActionListener(e -> displayAbout());

            final JMenuItem contentsItem = new JMenuItem("Contents");
            // /contentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
            // InputEvent.ALT_DOWN_MASK));
            contentsItem.addActionListener(e -> displayHelp());

            mainMenu.add(aboutItem);
            mainMenu.add(contentsItem);
            mainMenu.revalidate();
            mainMenu.repaint();
        }
    }

    /**
     * Display the "About" window.
     */
    private void displayAbout()
    {
        // Bring up "about" dialog window
        if (myAboutDialog == null)
        {
            myAboutDialog = new AboutDialog();
            myAboutDialog.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    myAboutDialog = null;
                }
            });
        }
        else
        {
            myAboutDialog.setVisible(true);
        }
    }

    /**
     * Display the "Help" window.
     */
    private void displayHelp()
    {
        // Bring up help contents window
        if (myHelpFrame == null)
        {
            rebuildHelp();

            myHelpFrame = new HelpFrame(getMainHelp());
            myHelpFrame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    myHelpFrame = null;
                }
            });
        }
        else
        {
            myHelpFrame.setVisible(true);
        }
    }

    /**
     * Create and return the help system set.
     *
     * @return The help system set.
     */
    private HelpSet getHelpSet()
    {
        try
        {
            final StringBuffer masterFileName = new StringBuffer(myHelpFileDirectory.getAbsolutePath());
            masterFileName.append(File.separator).append(MASTER_FILE_NAME);
            final File masterFile = new File(masterFileName.toString());
            if (!masterFile.exists())
            {
                LOGGER.warn("Unable to open master help file: " + masterFileName.toString());
                return null;
            }

            final URL url = masterFile.toURI().toURL();
            return new HelpSet(null, url);
        }
        catch (final HelpSetException hse)
        {
            LOGGER.warn("Unable to create HelpSet: " + hse.getMessage(), hse);
            return null;
        }
        catch (final MalformedURLException mue)
        {
            LOGGER.warn("Unable to create HelpSet: " + mue.getMessage(), mue);
            return null;
        }
    }

    /**
     * Accessor for main java help.
     *
     * @return The main java help.
     */
    private synchronized JHelp getMainHelp()
    {
        synchronized (myMainHelp)
        {
            return myMainHelp;
        }
    }

    /**
     * Rebuild the help. This will re-read the XML files used for configuration
     * and re-initialize.
     */
    private synchronized void rebuildHelp()
    {
        final HelpSet helpSet = getHelpSet();
        if (helpSet == null)
        {
            LOGGER.error("Unable to create Help set.  Help system will not be available.");
            return;
        }
        myMainHelp = new JHelp(helpSet);
    }

    /**
     * Remove all help files from the help system.
     *
     * @return True if successful, false otherwise.
     */
    private boolean removeAllHelpFiles()
    {
        return removeHelpFiles("");
    }

    /**
     * Remove help files from the help system for the associated name. If The
     * name is not given, the entire help directory of HTML files are removed.
     *
     * @param name The name associated with the help files to remove.
     * @return True if successful, false otherwise.
     */
    private synchronized boolean removeHelpFiles(String name)
    {
        final StringBuffer strBuf = new StringBuffer(myHelpFileDirectory.getAbsolutePath());
        strBuf.append(File.separator);
        strBuf.append("content");
        if (!StringUtils.isBlank(name))
        {
            strBuf.append(File.separator);
            strBuf.append(name);
        }

        final File deleteDir = new File(strBuf.toString());
        if (!deleteDir.isDirectory())
        {
            LOGGER.error("Specified directory " + name + " is not valid.  Unable to delete help files.");
            return false;
        }

        return FileUtilities.deleteDirRecursive(deleteDir);
    }
}
