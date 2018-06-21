/**
 * DirectoryListRecursion.java Oct 2, 2007
 *
 */
package io.opensphere.core.common.util.zip;

import java.io.File;
import java.util.ArrayList;

/**
 * This class generates and returns an Array of file paths and names. It
 * recurses through the directory tree starting at the location provided by the
 * caller. The paths of the files can be absolute or relative, depending on the
 * method that is called. A time may be supplied to each method call to
 * specifying a time that the files {@link File#lastModified} time must be
 * greater than.
 */
public class DirectoryListRecursion
{
    /** The files. */
    private static ArrayList<String> allFiles;

    /**
     * @param dir the directory from which to start the recursive search for
     *            files
     * @return a {@link String}[] of files with paths relative to
     *         <code>dir</code>
     */
    public static String[] getRelativeFiles(File dir)
    {
        allFiles = new ArrayList<>();
        getFiles(dir);
        String[] a = new String[allFiles.size()];
        allFiles.toArray(a);
        int rootLen = dir.toString().length();
        for (int i = 0; i < a.length; i++)
        {
            // System.out.print("a["+i+"] = "+a[i]+" rel = ");
            a[i] = a[i].substring(rootLen + 1);
            // System.out.println(a[i]);
        }
        return a;
    }

    /**
     * @param dir the directory from which to start the recursive search for
     *            files
     * @param time the time for which all returned files'
     *            <code>{@link File#lastModified()}</code> is greater than.
     * @return a {@link String}[] of files with paths relative to
     *         <code>dir</code> and {@link File#lastModified()} greater than
     *         <code>time</code>
     */
    public static String[] getRelativeFiles(File dir, long time)
    {
        allFiles = new ArrayList<>();
        getFiles(dir, time);
        String[] a = new String[allFiles.size()];
        allFiles.toArray(a);
        int rootLen = dir.toString().length();
        for (int i = 0; i < a.length; i++)
        {
            // System.out.println("a["+i+"] = "+a[i]+" dir = "+dir+"
            // rootlen="+rootLen);
            a[i] = a[i].substring(rootLen + 1);
        }
        return a;
    }

    /**
     * @param dir the directory from which to start the recursive search for
     *            files
     * @return a {@link String}[] of files with absolute paths
     */
    public static String[] getAbsoluteFiles(File dir)
    {
        allFiles = new ArrayList<>();
        getFiles(dir);
        String[] a = new String[allFiles.size()];
        allFiles.toArray(a);
        return a;
    }

    /**
     * @param dir the directory from which to start the recursive search for
     *            files
     * @param time the time for which all returned files'
     *            <code>{@link File#lastModified()}</code> is greater than.
     * @return a {@link String}[] of files with absolute paths and
     *         {@link File#lastModified()} greater than <code>time</code>
     */
    public static String[] getAbsoluteFiles(File dir, long time)
    {
        allFiles = new ArrayList<>();
        getFiles(dir, time);
        String[] a = new String[allFiles.size()];
        allFiles.toArray(a);
        return a;
    }

    /**
     * <b>getFiles:</b> Recursive method to retrieve all files in the specified
     * directory and all of its sub-directories.
     *
     * @param baseDir the starting directory
     */
    private static void getFiles(File baseDir)
    {
        File[] items = baseDir.listFiles();
        for (File f : items)
        {
            if (f.isDirectory() && !f.toString().startsWith("."))
            {
                getFiles(f);
            }
            else
            {
                allFiles.add(f.toString());
            }
        }
    }

    /**
     * <b>getFiles:</b> Recursive method to retrieve all files in the specified
     * directory and all of its sub-directories with a
     * {@link File#lastModified()} greater than the specified time.
     *
     * @param baseDir the starting directory
     * @param time the specfied time that all returned files should have been
     *            created after.
     */
    private static void getFiles(File baseDir, long time)
    {
        File[] items = baseDir.listFiles();
        for (File f : items)
        {
            if (f.isDirectory() && !f.toString().startsWith("."))
            {
                getFiles(f, time);
            }
            else
            {
                if (f.lastModified() > time)
                {
                    allFiles.add(f.toString());
                }
            }
        }
    }

}
