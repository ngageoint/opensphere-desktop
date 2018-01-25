package io.opensphere.core.util.lang;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;

import org.apache.log4j.Logger;

/**
 * Utilities for Jar files.
 */
public final class JarUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(JarUtilities.class);

    /**
     * List files available to a class loader.
     *
     * @param classLoader The class loader.
     * @param path The path pointing to the top level of the search.
     * @param depth The maximum depth below the path to search. A negative
     *            number indicates no limit.
     * @return A collection of {@link URL}s.
     */
    public static Collection<? extends URL> listFiles(ClassLoader classLoader, String path, int depth)
    {
        Collection<URL> results = new ArrayList<>();
        Enumeration<URL> resources;
        try
        {
            resources = classLoader.getResources(path);
            while (resources.hasMoreElements())
            {
                URL url = resources.nextElement();
                File file = new File(url.getFile());
                if (file.isFile() || depth == 0)
                {
                    results.add(url);
                }
                else if (file.isDirectory())
                {
                    getFilesInDirectory(path, file, depth - 1, results);
                }
                else
                {
                    getFilesInJar(path, url, depth, results);
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to load resources: " + e, e);
        }
        return results;
    }

    /**
     * Find the classes in a directory that match a path.
     *
     * @param path The path.
     * @param dir The directory.
     * @param depth The maximum depth below the path to search. A negative
     *            number indicates no limit.
     * @param results The output collection.
     */
    private static void getFilesInDirectory(String path, File dir, int depth, Collection<? super URL> results)
    {
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isFile())
                {
                    try
                    {
                        results.add(file.toURI().toURL());
                    }
                    catch (MalformedURLException e)
                    {
                        LOGGER.warn(e, e);
                    }
                }
                else if (depth != 0 && file.isDirectory())
                {
                    getFilesInDirectory(path, file, depth - 1, results);
                }
            }
        }
    }

    /**
     * Find the files in a jar that match a path.
     *
     * @param path The path.
     * @param url The URL of the jar file.
     * @param depth The maximum depth below the path to search. A negative
     *            number indicates no limit.
     * @param results The output collection.
     */
    private static void getFilesInJar(String path, URL url, int depth, Collection<? super URL> results)
    {
        String jarURI = url.toExternalForm();
        int bang = jarURI.indexOf('!');
        if (bang > 0)
        {
            jarURI = jarURI.substring(0, bang);
        }
        try
        {
            URLConnection conn = url.openConnection();
            if (conn instanceof JarURLConnection)
            {
                JarURLConnection jarConn = (JarURLConnection)conn;
                Enumeration<JarEntry> entries = jarConn.getJarFile().entries();
                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!name.endsWith("/") && name.startsWith(path))
                    {
                        String substring = name.substring(path.length());
                        if (substring.length() > 0)
                        {
                            substring = substring.substring(1);
                        }
                        String[] split = substring.split("/");
                        if (split.length <= depth)
                        {
                            results.add(new URL(jarURI + "!/" + name));
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to read jar file [" + jarURI + "]: " + e, e);
        }
    }

    /** Disallow instantiation. */
    private JarUtilities()
    {
    }
}
