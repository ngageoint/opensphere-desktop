package io.opensphere.filterbuilder.config;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.preferences.Preferences;

/**
 * Configuration class.
 *
 */
public class FilterBuilderConfigurationManager implements Cloneable
{
    /** The key for the preferences. */
    private static final String PREFERENCE_KEY = "filterbuilder";

    /** The Configuration for the Filter Builder. */
    private FilterBuilderConfiguration myConfig;

    /** The Lock. */
    private final ReentrantLock myLock;

    /** The Props. */
    private final Properties myProps;

    /** The Preferences. */
    private final Preferences myPrefs;

    /** The Default directory. */
    private File myDefaultDir;

    /**
     * The Debug flag. If true the filter builder will be created in debug mode.
     */

    /**
     * Instantiates a new configuration.
     *
     * @param pPrefs the preferences
     * @param pProps the props
     */
    public FilterBuilderConfigurationManager(Preferences pPrefs, Properties pProps)
    {
        myPrefs = pPrefs;
        myProps = pProps;
        myConfig = myPrefs.getJAXBObject(FilterBuilderConfiguration.class, PREFERENCE_KEY, new FilterBuilderConfiguration());
        myLock = new ReentrantLock();
    }

    /**
     * Gets the current file.
     *
     * @return the current file
     */
    public final File getCurrentFile()
    {
        myLock.lock();
        try
        {
            return myConfig.getCurrentFile();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Gets the default directory.
     *
     * @return the default directory
     */
    public File getDefaultDirectory()
    {
        myLock.lock();
        try
        {
            if (myDefaultDir == null)
            {
                final StringBuilder sb = new StringBuilder();

                sb.append(myProps.getProperty(PluginConstants.RUNTIME_DIR_KEY,
                        System.getProperty("user.home") + File.separator + "opensphere"));
                sb.append(File.separator);
                sb.append(myProps.getProperty(PluginConstants.DEFUALT_DIRECTORY_KEY, PluginConstants.DEFAULT_DIRECTORY));

                myDefaultDir = new File(sb.toString());
            }
        }
        finally
        {
            myLock.unlock();
        }

        return myDefaultDir;
    }

    /**
     * Gets the default filter file.
     *
     * @return the default filter file
     */
    public File getDefaultFilterFile()
    {
        myLock.lock();
        File result = null;
        try
        {
            result = new File(getDefaultDirectory(), PluginConstants.DEFAULT_FILE_NAME);
        }
        finally
        {
            myLock.unlock();
        }
        return result;
    }

    /**
     * Gets the last file.
     *
     * @return the myLastFile
     */
    public File getLastFile()
    {
        myLock.lock();
        try
        {
            return myConfig.getLastFile();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Gets the last load directory.
     *
     * @return the myLastLoadDir
     */
    public File getLastLoadDir()
    {
        myLock.lock();
        try
        {
            if (myConfig.getLastLoadDir() == null)
            {
                myConfig.setLastLoadDir(getDefaultDirectory());
            }
            return myConfig.getLastLoadDir();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Gets the last location on screen.
     *
     * @return the last location on screen
     */
    public Point getLastLocationOnScreen()
    {
        myLock.lock();
        try
        {
            if (myConfig.getXLoc() == 0 && myConfig.getYLoc() == 0)
            {
                return null;
            }
            return new Point(myConfig.getXLoc(), myConfig.getYLoc());
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Gets the last open directory.
     *
     * @return the myLastOpenDir
     */
    public final File getLastOpenDir()
    {
        myLock.lock();
        try
        {
            if (myConfig.getLastOpenDir() == null)
            {
                myConfig.setLastOpenDir(getDefaultDirectory());
            }
            return myConfig.getLastOpenDir();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Gets the last save directory.
     *
     * @return the myLastSaveDir
     */
    public File getLastSaveDir()
    {
        myLock.lock();
        try
        {
            if (myConfig.getLastSaveDir() == null)
            {
                myConfig.setLastSaveDir(getDefaultDirectory());
            }
            return myConfig.getLastSaveDir();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Gets the last window size.
     *
     * @return the last window size
     */
    public Dimension getLastWindowSize()
    {
        myLock.lock();
        try
        {
            if (myConfig.getWidth() < 50 || myConfig.getHeight() < 50)
            {
                return PluginConstants.DEFAULT_WINDOW_SIZE;
            }
            return new Dimension(myConfig.getWidth(), myConfig.getHeight());
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Checks if is debug mode.
     *
     * @return true, if debug mode is on.
     */
    public boolean isDebugMode()
    {
        myLock.lock();
        try
        {
            return myConfig.isDebugFlag();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the current file.
     *
     * @param pFile the new current file
     */
    public final void setCurrentFile(File pFile)
    {
        myLock.lock();
        try
        {
            myConfig.setCurrentFile(pFile);
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the last file.
     *
     * @param pLastFile the myLastFile to set
     */
    public final void setLastFile(File pLastFile)
    {
        myLock.lock();
        try
        {
            myConfig.setLastFile(pLastFile);
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the last load directory.
     *
     * @param pLastLoadDir the myLastLoadDir to set
     */
    public final void setLastLoadDir(File pLastLoadDir)
    {
        myLock.lock();
        try
        {
            myConfig.setLastLoadDir(pLastLoadDir);
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the last location on screen.
     *
     * @param pLastLocationOnScreen the new last location on screen
     */
    public void setLastLocationOnScreen(Point pLastLocationOnScreen)
    {
        myLock.lock();
        try
        {
            myConfig.setXLoc(pLastLocationOnScreen.x);
            myConfig.setYLoc(pLastLocationOnScreen.y);
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the last open directory.
     *
     * @param pLastOpenDir the myLastOpenDir to set
     */
    public final void setLastOpenDir(File pLastOpenDir)
    {
        myLock.lock();
        try
        {
            myConfig.setLastOpenDir(pLastOpenDir);
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the last save directory.
     *
     * @param pLastSaveDir the myLastSaveDir to set
     */
    public final void setLastSaveDir(File pLastSaveDir)
    {
        myLock.lock();
        try
        {
            myConfig.setLastSaveDir(pLastSaveDir);
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the last window size.
     *
     * @param pLastWindowSize the new last window size
     */
    public void setLastWindowSize(Dimension pLastWindowSize)
    {
        myLock.lock();
        try
        {
            myConfig.setWidth(pLastWindowSize.width);
            myConfig.setHeight(pLastWindowSize.height);
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Sets the values.
     *
     * @param pCfg the new values
     */
    public final void setValues(FilterBuilderConfiguration pCfg)
    {
        myLock.lock();
        try
        {
            myConfig = pCfg;
            saveConfig();
        }
        finally
        {
            myLock.unlock();
        }
    }

    /**
     * Save config to file on disk.
     */
    private void saveConfig()
    {
        myLock.lock();
        try
        {
            final FilterBuilderConfiguration configCopy = myConfig.clone();
            myPrefs.putJAXBObject(PREFERENCE_KEY, configCopy, false, this);
        }
        finally
        {
            myLock.unlock();
        }
    }
}
