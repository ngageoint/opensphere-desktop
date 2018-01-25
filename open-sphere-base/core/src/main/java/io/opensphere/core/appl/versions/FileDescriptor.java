package io.opensphere.core.appl.versions;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the inner JSON object in an install descriptor JSON file.
 */
@JsonPropertyOrder({ "fileName", "targetPath", "checksum", "executable" })
@JsonInclude(Include.NON_DEFAULT)
public class FileDescriptor
{
    /** The name of the file. */
    private String myFileName;

    /** The file target path. */
    private String myTargetPath;

    /** If the file should be executable. */
    private boolean myExecutable;

    /** The file checksum. */
    private String myChecksum;

    /**
     * Gets the file name.
     *
     * @return the name
     */
    public String getFileName()
    {
        return myFileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the name to set
     */
    public void setFileName(String fileName)
    {
        myFileName = fileName;
    }

    /**
     * Gets the target path.
     *
     * @return the path
     */
    public String getTargetPath()
    {
        return myTargetPath;
    }

    /**
     * Sets if a file should be executable.
     *
     * @param executable if the file should be executable
     */
    public void setExecutable(boolean executable)
    {
        myExecutable = executable;
    }

    /**
     * Gets if the file should be executable.
     *
     * @return if the file should be executable
     */
    public boolean getExecutable()
    {
        return myExecutable;
    }

    /**
     * Sets the target path.
     *
     * @param path the path to set
     */
    public void setTargetPath(String path)
    {
        myTargetPath = path;
    }

    /**
     * Gets the file checksum.
     *
     * @return the checksum
     */
    public String getChecksum()
    {
        return myChecksum;
    }

    /**
     * Sets the file checksum.
     *
     * @param checksum the checksum to set
     */
    public void setChecksum(String checksum)
    {
        myChecksum = checksum;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof FileDescriptor))
        {
            return false;
        }
        FileDescriptor f = (FileDescriptor)o;
        return f.getFileName().equals(myFileName) && f.getTargetPath().equals(myTargetPath) && f.getChecksum().equals(myChecksum);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(myFileName, myTargetPath, myChecksum);
    }
}
