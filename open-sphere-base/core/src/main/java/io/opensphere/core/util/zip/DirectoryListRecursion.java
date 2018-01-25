package io.opensphere.core.util.zip;

import java.io.File;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * This class generates and returns an Array of file paths and names. It
 * recurses through the directory tree starting at the location provided by the
 * caller. The paths of the files can be absolute or relative, depending on the
 * method that is called. A time may be supplied to each method call to
 * specifying a time that the files {@link File#lastModified} time must be
 * greater than.
 */
public final class DirectoryListRecursion
{
    /**
     * Recursively searches the specified directory and creates a list of all
     * the files in that and sub-directories.
     *
     * @param dir the directory from which to start the recursive search for
     *            files
     * @return a list of files with absolute paths
     */
    public static List<String> getAbsoluteFiles(File dir)
    {
        List<String> allFiles = New.list();
        getFiles(allFiles, dir);
        return allFiles;
    }

    /**
     * Gets all the files recursively from a directory and with a filter that
     * gets files where the last modified is after the input time.
     *
     * @param dir the directory from which to start the recursive search for
     *            files
     * @param minimumLastModTime the time for which all returned files' <code>
     *            {@link File#lastModified()}</code> is greater than.
     * @return a list of files with absolute paths and
     *         {@link File#lastModified()} greater than <code>time</code>
     */
    public static List<String> getAbsoluteFiles(File dir, long minimumLastModTime)
    {
        List<String> allFiles = New.list();
        getFiles(allFiles, dir, minimumLastModTime);
        return allFiles;
    }

    /**
     * Recursively searches the provided directory and returns a list of all
     * files.
     *
     * @param dir the directory from which to start the recursive search for
     *            files
     * @return a list of file names with paths relative to <code>dir</code>
     */
    public static List<String> getRelativeFiles(File dir)
    {
        List<String> allFiles = New.list();
        getFiles(allFiles, dir);
        int rootLen = dir.toString().length();
        for (int index = 0; index < allFiles.size(); index++)
        {
            allFiles.set(index, allFiles.get(index).substring(rootLen + 1));
        }
        return allFiles;
    }

    /**
     * Recursively searches the provided directory for files that occur after
     * the minLastModTime.
     *
     * @param dir the directory from which to start the recursive search for
     *            files
     * @param minLastModTime the time for which all returned files' <code>
     *            {@link File#lastModified()}</code> is greater than.
     * @return a list of files with paths relative to <code>dir</code> and
     *         {@link File#lastModified()} greater than <code>time</code>
     */
    public static List<String> getRelativeFiles(File dir, long minLastModTime)
    {
        List<String> allFiles = New.list();
        getFiles(allFiles, dir, minLastModTime);
        int rootLen = dir.toString().length();
        for (int index = 0; index < allFiles.size(); index++)
        {
            allFiles.set(index, allFiles.get(index).substring(rootLen + 1));
        }
        return allFiles;
    }

    /**
     * <b>getFiles:</b> Recursive method to retrieve all files in the specified
     * directory and all of its sub-directories.
     *
     * @param fileList the file list to which files are appended.
     * @param baseDir the starting directory
     */
    private static void getFiles(Collection<String> fileList, File baseDir)
    {
        File[] items = baseDir.listFiles();
        if (items != null)
        {
            for (File f : items)
            {
                if (f.isDirectory() && !f.toString().startsWith("."))
                {
                    getFiles(fileList, f);
                }
                else
                {
                    fileList.add(f.toString());
                }
            }
        }
    }

    /**
     * <b>getFiles:</b> Recursive method to retrieve all files in the specified
     * directory and all of its sub-directories with a last modified time
     * greater that he specified time.
     *
     * @param fileList the file list to which files are appended.
     * @param baseDir the starting directory
     * @param minLastModTime the specified time that all returned files should
     *            have been created after.
     */
    private static void getFiles(Collection<String> fileList, File baseDir, long minLastModTime)
    {
        File[] items = baseDir.listFiles();
        if (items != null)
        {
            for (File f : items)
            {
                if (f.isDirectory() && !f.toString().startsWith("."))
                {
                    getFiles(fileList, f, minLastModTime);
                }
                else
                {
                    if (f.lastModified() > minLastModTime)
                    {
                        fileList.add(f.toString());
                    }
                }
            }
        }
    }

    /**
     * Don't allow instantiation.
     */
    private DirectoryListRecursion()
    {
    }
}
