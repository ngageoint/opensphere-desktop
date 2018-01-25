package io.opensphere.installer.panels.jre;

import java.io.File;
import java.io.IOException;

/**
 * A set of defined conditions to use with IzPack &lt;condition&gt; XML
 * constructs, to determine if a given directory exists and is writable.
 */
public class DirectoryWritableCondition
{
    /**
     * A constant field used to indicate to an IzPack &lt;condition&gt; that the
     * Linux /opt directory exists as a directory, and is writable.
     */
    public static final boolean IS_OPT_WRITABLE = isDirWritable(File.separatorChar + "opt");

    /**
     * A constant field used to indicate to an IzPack &lt;condition&gt; that the
     * windows 'Program Files' directory exists as a directory, and is writable.
     */
    public static final boolean IS_PROGRAM_FILES_WRITABLE = isDirWritable(
            "C:" + File.separatorChar + "program files" + File.separatorChar);

    /**
     * Tests to determine if the supplied path exists as a directory, and is
     * writable by the executing user.
     *
     * @param path the path to test.
     * @return true if all conditions are met (path exists as a directory, and
     *         is writable by the current user).
     */
    private static boolean isDirWritable(String path)
    {
        File directory = new File(path);

        boolean isDirectory = directory.isDirectory();
        boolean canWrite = directory.canWrite();

        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().contains("windows"))
        {
            File tempDirectory;
            File tempFile;

            try
            {
                tempDirectory = new File(path, "tempWriteTest" + System.currentTimeMillis());
                tempDirectory.mkdirs();

                tempFile = new File(tempDirectory.getAbsolutePath(), "tempWriteTest.tmp");
                tempFile.createNewFile();

                isDirectory = true;

                if (!tempFile.delete())
                {
                    tempFile.deleteOnExit();
                }
                if (!tempDirectory.delete())
                {
                    tempDirectory.deleteOnExit();
                }

                canWrite = true;
            }
            catch (IOException e)
            {
                isDirectory = true;
                canWrite = false;
            }
        }

        if (isDirectory && canWrite)
        {
            System.out.println("'" + path + "' is a writable directory.");
        }
        else
        {
            System.out.println("'" + path + "' is not a directory, or is not writable.");
        }

        return isDirectory && canWrite;
    }
}
