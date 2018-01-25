package io.opensphere.core.orwell;

import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * A container in which the statistics describing the application are stored.
 */
public class ApplicationStatistics
{
    /**
     * The application version.
     */
    private String myVersion;

    /**
     * The date that the installation was performed.
     */
    private String myInstallationDate;

    /**
     * The location on the file system of the installation.
     */
    private String myInstallationLocation;

    /**
     * The location in which the user's application settings are stored.
     */
    private String myApplicationUserDirectory;

    /**
     * The collection of statistics describing each plugin installed on the system.
     */
    private final List<PluginStatistics> myAvailablePlugins = New.list();

    /**
     * Gets the value of the {@link #myVersion} field.
     *
     * @return the value stored in the {@link #myVersion} field.
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Sets the value of the {@link #myVersion} field.
     *
     * @param pVersion the value to store in the {@link #myVersion} field.
     */
    public void setVersion(String pVersion)
    {
        myVersion = pVersion;
    }

    /**
     * Gets the value of the {@link #myInstallationDate} field.
     *
     * @return the value stored in the {@link #myInstallationDate} field.
     */
    public String getInstallationDate()
    {
        return myInstallationDate;
    }

    /**
     * Sets the value of the {@link #myInstallationDate} field.
     *
     * @param pInstallationDate the value to store in the {@link #myInstallationDate} field.
     */
    public void setInstallationDate(String pInstallationDate)
    {
        myInstallationDate = pInstallationDate;
    }

    /**
     * Gets the value of the {@link #myInstallationLocation} field.
     *
     * @return the value stored in the {@link #myInstallationLocation} field.
     */
    public String getInstallationLocation()
    {
        return myInstallationLocation;
    }

    /**
     * Sets the value of the {@link #myInstallationLocation} field.
     *
     * @param pInstallationLocation the value to store in the {@link #myInstallationLocation} field.
     */
    public void setInstallationLocation(String pInstallationLocation)
    {
        myInstallationLocation = pInstallationLocation;
    }

    /**
     * Gets the value of the {@link #myApplicationUserDirectory} field.
     *
     * @return the value stored in the {@link #myApplicationUserDirectory} field.
     */
    public String getApplicationUserDirectory()
    {
        return myApplicationUserDirectory;
    }

    /**
     * Sets the value of the {@link #myApplicationUserDirectory} field.
     *
     * @param pApplicationUserDirectory the value to store in the {@link #myApplicationUserDirectory} field.
     */
    public void setApplicationUserDirectory(String pApplicationUserDirectory)
    {
        myApplicationUserDirectory = pApplicationUserDirectory;
    }

    /**
     * Gets the value of the {@link #myAvailablePlugins} field.
     *
     * @return the value stored in the {@link #myAvailablePlugins} field.
     */
    public List<PluginStatistics> getAvailablePlugins()
    {
        return myAvailablePlugins;
    }
}
