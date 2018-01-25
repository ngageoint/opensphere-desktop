package io.opensphere.core.appl.versions;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the root JSON object in an install descriptor JSON file.
 */
@JsonPropertyOrder({ "version", "environment", "targetOperatingSystem, files" })
public class InstallDescriptor
{
    /** The version of the files. */
    private String myVersion;

    /** The version of the files. */
    private String myBuild;

    /** The target machine environment. */
    private String myEnvironment;

    /** The target machine operating system. */
    private String myTargetOperatingSystem;

    /** The file descriptors that are a part of the update. */
    private List<FileDescriptor> myFileDescriptors;

    /**
     * Gets the install version.
     *
     * @return the version
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Sets the install version.
     *
     * @param version the version to set
     */
    public void setVersion(String version)
    {
        myVersion = version;
    }

    /**
     * Gets the install environment.
     *
     * @return the environment
     */
    public String getEnvironment()
    {
        return myEnvironment;
    }

    /**
     * Sets the install environment.
     *
     * @param environment the environment to set
     */
    public void setEnvironment(String environment)
    {
        myEnvironment = environment;
    }

    /**
     * Gets the install operating system.
     *
     * @return the operatingSystem
     */
    public String getTargetOperatingSystem()
    {
        return myTargetOperatingSystem;
    }

    /**
     * Sets the install operating system.
     *
     * @param operatingSystem the operatingSystem to set
     */
    public void setTargetOperatingSystem(String operatingSystem)
    {
        myTargetOperatingSystem = operatingSystem;
    }

    /**
     * Gets the value of the {@link #myBuild} field.
     *
     * @return the value stored in the {@link #myBuild} field.
     */
    public String getBuild()
    {
        return myBuild;
    }

    /**
     * Sets the value of the {@link #myBuild} field.
     *
     * @param build the value to store in the {@link #myBuild} field.
     */
    public void setBuild(String build)
    {
        myBuild = build;
    }

    /**
     * Gets the install file descriptors.
     *
     * @return the install file descriptors
     */
    public List<FileDescriptor> getFiles()
    {
        return myFileDescriptors;
    }

    /**
     * Sets the install file descriptors.
     *
     * @param fileDescriptors the file descriptors
     */
    public void setFiles(List<FileDescriptor> fileDescriptors)
    {
        myFileDescriptors = fileDescriptors;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }
        if (!(o instanceof InstallDescriptor))
        {
            return false;
        }
        InstallDescriptor i = (InstallDescriptor)o;
        return i.getVersion().equals(myVersion) && i.getTargetOperatingSystem().equals(myTargetOperatingSystem)
                && i.getEnvironment().equals(myEnvironment) && i.getFiles().equals(myFileDescriptors);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(myVersion, myTargetOperatingSystem, myEnvironment, myFileDescriptors);
    }
}
