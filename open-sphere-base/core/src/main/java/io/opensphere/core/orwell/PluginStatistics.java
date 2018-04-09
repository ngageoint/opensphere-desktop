package io.opensphere.core.orwell;

import java.util.List;

import io.opensphere.core.PluginProperty;

/**
 * A container in which the statistics describing a single plugin are stored.
 */
public class PluginStatistics
{
    /**
     * The name of the plugin.
     */
    private String myName;

    /**
     * The version of the plugin.
     */
    private String myVersion;

    /**
     * The version of application with which the plugin is compatible.
     */
    private String myCompatibleApplicationVersion;

    /**
     * The author of the plugin.
     */
    private String myAuthor;

    /**
     * The plugin class used as an entry point.
     */
    private String myPluginClass;

    /**
     * The human-readable description of the plugin.
     */
    private String myDescription;

    /**
     * The language in which the plugin is written.
     */
    private String myLanguage;

    /**
     * The human-readable summary of the plugin.
     */
    private String mySummary;

    /**
     * The required dependencies of the plugin.
     */
    private List<String> myRequiredPluginDependencies;

    /**
     * The optional dependencies of the plugin.
     */
    private List<String> myOptionalPluginDependencies;

    /**
     * The properties with which the plugin is initialized.
     */
    private List<PluginProperty> myPluginProperties;

    /**
     * Gets the value of the {@link #myName} field.
     *
     * @return the value stored in the {@link #myName} field.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Sets the value of the {@link #myName} field.
     *
     * @param pName the value to store in the {@link #myName} field.
     */
    public void setName(String pName)
    {
        myName = pName;
    }

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
     * Gets the value of the {@link #myCompatibleApplicationVersion} field.
     *
     * @return the value stored in the {@link #myCompatibleApplicationVersion}
     *         field.
     */
    public String getCompatibleApplicationVersion()
    {
        return myCompatibleApplicationVersion;
    }

    /**
     * Sets the value of the {@link #myCompatibleApplicationVersion} field.
     *
     * @param pCompatibleApplicationVersion the value to store in the
     *            {@link #myCompatibleApplicationVersion} field.
     */
    public void setCompatibleApplicationVersion(String pCompatibleApplicationVersion)
    {
        myCompatibleApplicationVersion = pCompatibleApplicationVersion;
    }

    /**
     * Gets the value of the {@link #myAuthor} field.
     *
     * @return the value stored in the {@link #myAuthor} field.
     */
    public String getAuthor()
    {
        return myAuthor;
    }

    /**
     * Sets the value of the {@link #myAuthor} field.
     *
     * @param pAuthor the value to store in the {@link #myAuthor} field.
     */
    public void setAuthor(String pAuthor)
    {
        myAuthor = pAuthor;
    }

    /**
     * Gets the value of the {@link #myPluginClass} field.
     *
     * @return the value stored in the {@link #myPluginClass} field.
     */
    public String getPluginClass()
    {
        return myPluginClass;
    }

    /**
     * Sets the value of the {@link #myPluginClass} field.
     *
     * @param pPluginClass the value to store in the {@link #myPluginClass}
     *            field.
     */
    public void setPluginClass(String pPluginClass)
    {
        myPluginClass = pPluginClass;
    }

    /**
     * Gets the value of the {@link #myDescription} field.
     *
     * @return the value stored in the {@link #myDescription} field.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Sets the value of the {@link #myDescription} field.
     *
     * @param pDescription the value to store in the {@link #myDescription}
     *            field.
     */
    public void setDescription(String pDescription)
    {
        myDescription = pDescription;
    }

    /**
     * Gets the value of the {@link #myLanguage} field.
     *
     * @return the value stored in the {@link #myLanguage} field.
     */
    public String getLanguage()
    {
        return myLanguage;
    }

    /**
     * Sets the value of the {@link #myLanguage} field.
     *
     * @param pLanguage the value to store in the {@link #myLanguage} field.
     */
    public void setLanguage(String pLanguage)
    {
        myLanguage = pLanguage;
    }

    /**
     * Gets the value of the {@link #mySummary} field.
     *
     * @return the value stored in the {@link #mySummary} field.
     */
    public String getSummary()
    {
        return mySummary;
    }

    /**
     * Sets the value of the {@link #mySummary} field.
     *
     * @param pSummary the value to store in the {@link #mySummary} field.
     */
    public void setSummary(String pSummary)
    {
        mySummary = pSummary;
    }

    /**
     * Gets the value of the {@link #myRequiredPluginDependencies} field.
     *
     * @return the value stored in the {@link #myRequiredPluginDependencies}
     *         field.
     */
    public List<String> getRequiredPluginDependencies()
    {
        return myRequiredPluginDependencies;
    }

    /**
     * Sets the value of the {@link #myRequiredPluginDependencies} field.
     *
     * @param pRequiredPluginDependencies the value to store in the
     *            {@link #myRequiredPluginDependencies} field.
     */
    public void setRequiredPluginDependencies(List<String> pRequiredPluginDependencies)
    {
        myRequiredPluginDependencies = pRequiredPluginDependencies;
    }

    /**
     * Gets the value of the {@link #myOptionalPluginDependencies} field.
     *
     * @return the value stored in the {@link #myOptionalPluginDependencies}
     *         field.
     */
    public List<String> getOptionalPluginDependencies()
    {
        return myOptionalPluginDependencies;
    }

    /**
     * Sets the value of the {@link #myOptionalPluginDependencies} field.
     *
     * @param pOptionalPluginDependencies the value to store in the
     *            {@link #myOptionalPluginDependencies} field.
     */
    public void setOptionalPluginDependencies(List<String> pOptionalPluginDependencies)
    {
        myOptionalPluginDependencies = pOptionalPluginDependencies;
    }

    /**
     * Gets the value of the {@link #myPluginProperties} field.
     *
     * @return the value stored in the {@link #myPluginProperties} field.
     */
    public List<PluginProperty> getPluginProperties()
    {
        return myPluginProperties;
    }

    /**
     * Sets the value of the {@link #myPluginProperties} field.
     *
     * @param pPluginProperties the value to store in the
     *            {@link #myPluginProperties} field.
     */
    public void setPluginProperties(List<PluginProperty> pPluginProperties)
    {
        myPluginProperties = pPluginProperties;
    }
}
