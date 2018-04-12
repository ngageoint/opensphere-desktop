package io.opensphere.mantle.util.compiler;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;

/**
 * The Class DynamicClassFileManager.
 */
public class DynamicClassFileManager extends ForwardingJavaFileManager<JavaFileManager>
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(DynamicClassFileManager.class);

    /** The loader. */
    private ByteArrayClassLoader myLoader;

    /**
     * Instantiates a new dynamic class file manager.
     *
     * @param mgr the mgr
     */
    protected DynamicClassFileManager(StandardJavaFileManager mgr)
    {
        super(mgr);
        try
        {
            myLoader = AccessController.doPrivileged(new PrivilegedAction<ByteArrayClassLoader>()
            {
                @Override
                public ByteArrayClassLoader run()
                {
                    return new ByteArrayClassLoader();
                }
            });
        }
        catch (RuntimeException ex)
        {
            LOGGER.error(ex);
        }
    }

    @Override
    public ClassLoader getClassLoader(Location location)
    {
        return myLoader;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) throws IOException
    {
        ByteArrayJavaFileObject co = new ByteArrayJavaFileObject(name, kind);
        myLoader.put(name, co);
        return co;
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file)
    {
        if (file instanceof URIJavaFileObject)
        {
            return ((URIJavaFileObject)file).getBinaryName();
        }
        else
        {
            return super.inferBinaryName(location, file);
        }
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
        throws IOException
    {
        Iterable<JavaFileObject> result = null;

        try
        {
            result = super.list(location, packageName, kinds, recurse);
        }
        catch (IOException e)
        {
            result = continueList(location, packageName, kinds, recurse);
        }

        if (result == null || !result.iterator().hasNext())
        {
            result = continueList(location, packageName, kinds, recurse);
        }

        return result;
    }

    /**
     * Extracted code from list for multi-use cases.
     * 
     * @param location
     * @param packageName
     * @param kinds
     * @param recurse
     * @return
     * @throws IOException
     */
    private Iterable<JavaFileObject> continueList(Location location, String packageName, Set<Kind> kinds, boolean recurse)
        throws IOException
    {
        Collection<JavaFileObject> results = New.collection();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
                .getResources(packageName.replaceAll("\\.", "/"));
        while (resources.hasMoreElements())
        {
            URL url = resources.nextElement();
            File file = new File(url.getFile());
            if (file.isDirectory())
            {
                getFilesInDirectory(packageName, file, results);
            }
            else
            {
                getFilesInJar(packageName, url, results);
            }
        }
        return results;
    }

    /**
     * Find the classes in a directory that match a package name.
     *
     * @param packageName The package name.
     * @param dir The directory.
     * @param results The output collection.
     */
    private void getFilesInDirectory(String packageName, File dir, Collection<? super JavaFileObject> results)
    {
        File[] files = dir.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isFile() && file.getName().endsWith(".class"))
                {
                    String binaryName = packageName + "." + file.getName();
                    binaryName = binaryName.replaceAll(".class$", "");
                    results.add(new URIJavaFileObject(binaryName, file.toURI()));
                }
            }
        }
    }

    /**
     * Find the classes in a jar that match a package name.
     *
     * @param packageName The package name.
     * @param url The URL of the jar file.
     * @param results The output collection.
     */
    private void getFilesInJar(String packageName, URL url, Collection<? super JavaFileObject> results)
    {
        String jarURI = url.toExternalForm().split("!")[0];
        try
        {
            JarURLConnection conn = (JarURLConnection)url.openConnection();
            String rootEntryName = conn.getEntryName();
            int rootEnd = rootEntryName.length() + 1;
            Enumeration<JarEntry> entries = conn.getJarFile().entries();
            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(rootEntryName) && name.indexOf('/', rootEnd) == -1 && name.endsWith(".class"))
                {
                    URI uri = URI.create(jarURI + "!/" + name);
                    String binaryName = name.replaceAll("/", ".").replaceAll(".class$", "");
                    results.add(new URIJavaFileObject(binaryName, uri));
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to open " + url + " as a jar file", e);
        }
    }
}
