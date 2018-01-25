package io.opensphere.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * Utility class that loads system properties from a file on the classpath.
 */
public final class SystemPropertyLoader
{
    /**
     * Load the standard system properties files.
     */
    public static void loadSystemProperties()
    {
        // Clear the timezone set by Java to allow it to be set from one of the
        // files.
        System.clearProperty("user.timezone");

        loadSystemProperties("override.system.properties", false);
        loadSystemProperties("build.system.properties", false);
        loadSystemProperties("system.properties", true);
    }

    /**
     * Load properties from a file on the classpath. Do not replace properties
     * that have already been set.
     *
     * @param resourceName The name of the classpath resource.
     * @param warn Indicates if a warning should be reported if the resource is
     *            not found.
     */
    public static void loadSystemProperties(String resourceName, boolean warn)
    {
        try
        {
            final Enumeration<URL> urls = SystemPropertyLoader.class.getClassLoader().getResources(resourceName);
            if (urls.hasMoreElements())
            {
                do
                {
                    final InputStream is = urls.nextElement().openStream();
                    try
                    {
                        final Properties props = new Properties();
                        props.load(is);
                        final Enumeration<?> keys = props.propertyNames();
                        while (keys.hasMoreElements())
                        {
                            final String key = (String)keys.nextElement();
                            if (System.getProperty(key) == null)
                            {
                                System.setProperty(key, props.getProperty(key));
                            }
                        }
                    }
                    catch (final IOException e)
                    {
                        Logger.getLogger(SystemPropertyLoader.class).error("Failed to load " + resourceName + ": " + e, e);
                    }
                    finally
                    {
                        try
                        {
                            is.close();
                        }
                        catch (final IOException e)
                        {
                            Logger.getLogger(SystemPropertyLoader.class)
                                    .trace("Failed to close stream after loading system properties " + e, e);
                        }
                    }
                }
                while (urls.hasMoreElements());
            }
            else if (warn)
            {
                Logger.getLogger(SystemPropertyLoader.class)
                        .warn("System properties resource could not be loaded: " + resourceName);
            }
        }
        catch (final IOException e)
        {
            Logger.getLogger(SystemPropertyLoader.class).error("Failed to get resources for " + resourceName + ": " + e, e);
        }
    }

    /** Make sure the user.home system property is set to something usable. */
    public static void validateUserHome()
    {
        File userHomeFile = new File(System.getProperty("user.home", ""));
        if (testDirectoryWritable(userHomeFile))
        {
            return;
        }

        final File userdirFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "opensphere.userdir.txt");
        try
        {
            final FileInputStream fis = new FileInputStream(userdirFile);
            try
            {
                final byte[] arr = new byte[1024];
                final int len = fis.read(arr);
                userHomeFile = new File(new String(arr, 0, len, Charset.defaultCharset()));
            }
            finally
            {
                fis.close();
            }
        }
        catch (final IOException e)
        {
            Logger.getLogger(SystemPropertyLoader.class).info(e, e);
        }
        while (!testDirectoryWritable(userHomeFile))
        {
            JOptionPane.showMessageDialog(null,
                    "The local storage directory \"" + userHomeFile + "\" is not writable. Please choose a writable directory.",
                    "Writable directory required", JOptionPane.WARNING_MESSAGE);
            final JFileChooser chooser = new JFileChooser(userHomeFile);
            chooser.setDialogTitle("User Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                userHomeFile = chooser.getSelectedFile();
            }
            else
            {
                final int option = JOptionPane.showConfirmDialog(null,
                        "If you do not choose a directory, the application will exit. Are you sure?", "User directory required",
                        JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION)
                {
                    System.exit(1);
                }
            }
        }

        System.setProperty("user.home", userHomeFile.getPath());

        try
        {
            final FileOutputStream fos = new FileOutputStream(userdirFile);
            try
            {
                fos.write(userHomeFile.getPath().getBytes(Charset.defaultCharset()));
            }
            finally
            {
                fos.close();
            }
        }
        catch (final IOException e)
        {
            Logger.getLogger(SystemPropertyLoader.class).error(e, e);
        }
    }

    /**
     * Test that directories can be created in the given directory.
     *
     * @param dir The directory.
     * @return {@code true} if directories can be created.
     */
    private static boolean testDirectoryWritable(File dir)
    {
        final File subdir = new File(new File(dir, "opensphere"), "vortex");
        final boolean existed = subdir.exists();
        if ((existed || subdir.mkdirs()) && subdir.canWrite())
        {
            try
            {
                boolean tempCreated = File.createTempFile("opensphere", null, subdir).delete();
                if (!existed)
                {
                    tempCreated = subdir.delete() && subdir.getParentFile().delete() || tempCreated;
                }
                return tempCreated;
            }
            catch (final IOException e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /** Disallow instantiation. */
    private SystemPropertyLoader()
    {
    }
}
