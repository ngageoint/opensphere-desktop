package io.opensphere.core.appl.versions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * This class holds utilities for making meaningful comparisons with file
 * descriptors.
 */
public final class DescriptorUtils
{
    /** Logger reference. */
    private static final Logger LOG = Logger.getLogger(DescriptorUtils.class);

    /**
     * Disallows instantiation.
     */
    private DescriptorUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Generates the md5 checksum value for a file.
     *
     * @param file the file
     * @return the md5 checksum string
     */
    public static String createChecksum(File file)
    {
        String md5Checksum = null;
        try (InputStream fileInputStream = new FileInputStream(file))
        {
            md5Checksum = DigestUtils.md5Hex(IOUtils.toByteArray(fileInputStream));
        }
        catch (IOException e)
        {
            LOG.error("Null checksum created for " + file.getName(), e);
        }
        return md5Checksum;
    }

    /**
     * If there exists a file out of a list which matches the file descriptor.
     *
     * @param fileDescriptor the file descriptor which describes a desired file
     * @param files the list of files
     * @return if the list of files contains a name/checksum match for the given
     *         file descriptor
     */
    public static boolean hasMatchForFile(FileDescriptor fileDescriptor, Collection<File> files)
    {
        return getMatchForFile(fileDescriptor, files) == null ? false : true;
    }

    /**
     * The file out of a list which matches the file descriptor.
     *
     * @param fileDescriptor the file descriptor which describes a desired file
     * @param files the list of files
     * @return the file which matches the file descriptor or null if no files
     *         match
     */
    public static File getMatchForFile(FileDescriptor fileDescriptor, Collection<File> files)
    {
        for (File file : files)
        {
            if (fileDescriptor.getFileName().equals(file.getName()))
            {
                if (fileDescriptor.getChecksum().equals(createChecksum(file)))
                {
                    return file;
                }
            }
        }
        return null;
    }
}
