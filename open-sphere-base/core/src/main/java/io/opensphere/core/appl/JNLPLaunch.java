package io.opensphere.core.appl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import io.opensphere.core.util.SystemPropertyLoader;
import io.opensphere.core.util.filesystem.FileUtilities;

/**
 * Launcher for the application that unpacks a JRE and runs Web Start.
 */
public final class JNLPLaunch
{
    /** Logger reference. */
    private static final Logger LOGGER;

    static
    {
        SystemPropertyLoader.validateUserHome();
        SystemPropertyLoader.loadSystemProperties();
        LOGGER = Logger.getLogger(JNLPLaunch.class);
    }

    /**
     * Unpack the JRE and launch Web Start.
     *
     * @param args The program arguments.
     * @throws IOException If the JRE zip cannot be read.
     */
    public static void main(String[] args) throws IOException
    {
        final String codebase = getCodebase();
        final String options = System.getProperty("jnlp.opensphere.options", "");

        String javawsPath;
        final String jreName = System.getProperty("jnlp.opensphere.jre");
        if (jreName == null)
        {
            LOGGER.error("jnlp.opensphere.jre property is not defined");
            javawsPath = null;
        }
        else
        {
            LOGGER.info("jnlp.opensphere.jre is " + jreName);

            final String jrePath = jreName.replace(".zip", "") + "_" + System.getProperty("os.arch");
            File dir = new File(System.getProperty("java.io.tmpdir"), jrePath);
            int index = 0;
            while (dir.exists())
            {
                javawsPath = dir.getPath() + File.separator + "bin/javaws";
                if (launchApplication(javawsPath, codebase, options))
                {
                    System.exit(0);
                }
                else
                {
                    LOGGER.warn("Failed to launch javaws at path " + javawsPath);
                    if (FileUtilities.deleteDirRecursive(dir))
                    {
                        break;
                    }
                    else
                    {
                        if (index == 0)
                        {
                            dir = new File(dir.getPath() + Integer.toString(++index));
                        }
                        else
                        {
                            dir = new File(dir.getPath().replace(Integer.toString(index), Integer.toString(++index)));
                        }
                        LOGGER.warn("Failed to delete directory " + dir + "; trying " + dir);
                    }
                }
            }

            if (!dir.mkdirs())
            {
                LOGGER.error("Failed to create directory: " + dir);
                javawsPath = null;
            }
            else
            {
                javawsPath = getJWS(jreName, dir);
            }
        }

        if (javawsPath == null)
        {
            LOGGER.info("Attempting to locate installed javaws.");
            // Try to find the installed javaws.
            final File javabin = new File(System.getProperty("java.home") + File.separator + "bin");
            String javaws = null;
            final String[] files = javabin.list();
            if (files != null)
            {
                for (final String file : files)
                {
                    if (file.startsWith("javaws"))
                    {
                        javaws = file;
                        break;
                    }
                }
            }
            File file;
            if (javaws == null || !(file = new File(javabin, javaws)).exists())
            {
                LOGGER.fatal("Could not locate installed javaws.");
                System.exit(1);
                return;
            }
            javawsPath = file.getPath();
        }

        launchApplication(javawsPath, codebase, options);
        System.exit(0);
    }

    /**
     * Unzip the JRE.
     *
     * @param stream The JRE input stream.
     * @param dir The target directory.
     * @return The path to the unzipped javaws executable.
     */
    static String unzipJRE(InputStream stream, File dir)
    {
        final List<String> files = FileUtilities.explodeZip(stream, dir, 0);
        final Pattern pat = Pattern.compile(".*javaws[.\\w]*");
        String javawsPath = null;
        for (final String file : files)
        {
            if (!new File(file).setExecutable(true))
            {
                LOGGER.warn("Failed to set file executable: " + file);
            }
            if (javawsPath == null && pat.matcher(file).matches())
            {
                javawsPath = file;
            }
        }

        return javawsPath;
    }

    /**
     * Get the codebase from the system property.
     *
     * @return The codebase.
     */
    private static String getCodebase()
    {
        final String codebase = System.getProperty("jnlp.opensphere.codebase.jnlp");
        if (codebase == null)
        {
            LOGGER.fatal("No jnlp.opensphere.codebase.jnlp property found.");
            System.exit(1);
        }
        return codebase;
    }

    /**
     * Get a URL for the JRE using the given resource name and class loader.
     *
     * @param jrePath The resource name for the JRE.
     * @param classLoader The class loader.
     * @return The URL, or {@code null} if one was not found.
     */
    private static URL getJreUrl(String jrePath, ClassLoader classLoader)
    {
        URL jreUrl = null;
        try
        {
            for (final URL url : Collections.list(classLoader.getResources(jrePath)))
            {
                if ("jar".equals(url.getProtocol()))
                {
                    jreUrl = url;
                    break;
                }
            }
        }
        catch (final IOException e)
        {
            LOGGER.error("Error listing resources for jnlp.opensphere.jre resource: " + e, e);
        }
        return jreUrl;
    }

    /**
     * Get the path to the embedded Java Web Start.
     *
     * @param jreName The name of the JRE resource on the classpath.
     * @param targetDir The directory on the file system where the JRE should be
     *            exploded.
     *
     * @return The path, or {@code null} if javaws was not found.
     */
    private static String getJWS(String jreName, File targetDir)
    {
        final ClassLoader classLoader = JNLPLaunch.class.getClassLoader();
        LOGGER.info("classloader is " + classLoader);
        for (ClassLoader parent = classLoader.getParent(); parent != null; parent = parent.getParent())
        {
            LOGGER.info("parent is " + parent);
        }
        final URL jreUrl = getJreUrl(jreName, classLoader);
        if (jreUrl == null)
        {
            LOGGER.error("JRE could not be found in resource path " + jreName);
            return null;
        }

        LOGGER.info("Loading JRE from resource path " + jreUrl);

        InputStream stream;
        try
        {
            stream = jreUrl.openStream();
        }
        catch (final IOException e)
        {
            LOGGER.error("Could not open stream for JRE URL [" + jreUrl + ": " + e, e);
            return null;
        }
        final String javawsPath = unzipJRE(stream, targetDir);
        if (javawsPath == null)
        {
            LOGGER.error("No javaws executable found in " + jreUrl);
        }
        return javawsPath;
    }

    /**
     * Launch the main process.
     *
     * @param javawsPath The path to javaws.
     * @param codebase The URL for the codebase.
     * @param options The options.
     * @return {@code true} if the process was launched successfully.
     */
    private static boolean launchApplication(String javawsPath, String codebase, String options)
    {
        final ProcessBuilder builder = new ProcessBuilder(javawsPath, "-J-Duser.home=" + System.getProperty("user.home"), options,
                codebase);
        LOGGER.info("Launching: " + builder.command());
        try
        {
            builder.start();
            return true;
        }
        catch (final RuntimeException e)
        {
            LOGGER.fatal("Failed to start process: " + e, e);
            return false;
        }
        catch (final Error e)
        {
            LOGGER.fatal("Failed to start process: " + e, e);
            return false;
        }
        catch (final IOException e)
        {
            LOGGER.fatal("Failed to start process: " + e, e);
            return false;
        }
    }

    /** Disallow instantiation. */
    private JNLPLaunch()
    {
    }
}
