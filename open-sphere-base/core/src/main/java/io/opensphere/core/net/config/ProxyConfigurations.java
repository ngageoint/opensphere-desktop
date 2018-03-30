package io.opensphere.core.net.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.util.lang.ToStringHelper;

/** A collection of the set of possible proxy configurations. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProxyConfigurations
{
    /** The selected configuration type. */
    @XmlElement(name = "selected", required = true, defaultValue = "system")
    private ConfigurationType mySelectedConfigurationType;

    /** The no-proxy configuration, may be null. */
    @XmlElement(name = "no-proxy")
    private NoProxyConfiguration myNoProxyConfiguration = new NoProxyConfiguration();

    /** The system-proxy configuration, may be null. */
    @XmlElement(name = "system-proxy")
    private SystemProxyConfiguration mySystemProxyConfiguration = new SystemProxyConfiguration();

    /** The url-proxy configuration, may be null. */
    @XmlElement(name = "url-proxy")
    private UrlProxyConfiguration myUrlProxyConfiguration = new UrlProxyConfiguration();

    /** The manual proxy configuration, may be null. */
    @XmlElement(name = "manual-proxy")
    private ManualProxyConfiguration myManualProxyConfiguration = new ManualProxyConfiguration();

    /**
     * Gets the value of the selectedConfiguration
     * ({@link #mySelectedConfigurationType}) field.
     *
     * @return the value stored in the {@link #mySelectedConfigurationType}
     *         field.
     */
    public ConfigurationType getSelectedConfigurationType()
    {
        return mySelectedConfigurationType;
    }

    /**
     * Sets the value of the selectedConfiguration
     * ({@link #mySelectedConfigurationType}) field.
     *
     * @param selectedConfigurationType the value to store in the
     *            {@link #mySelectedConfigurationType} field.
     */
    public void setSelectedConfigurationType(ConfigurationType selectedConfigurationType)
    {
        mySelectedConfigurationType = selectedConfigurationType;
    }

    /**
     * Utility method to retrieve the configuration instance corresponding with
     * the {@link #mySelectedConfigurationType}.
     *
     * @return the configuration instance corresponding with the
     *         {@link #mySelectedConfigurationType}.
     */
    @XmlTransient
    public ProxyConfiguration getSelectedConfiguration()
    {
        switch (mySelectedConfigurationType)
        {
            case NONE:
                return myNoProxyConfiguration;
            case SYSTEM:
                return mySystemProxyConfiguration;
            case URL:
                return myUrlProxyConfiguration;
            case MANUAL:
                return myManualProxyConfiguration;
            default:
                throw new UnsupportedOperationException(
                        "Unrecognized proxy configuration type: " + mySelectedConfigurationType.name());
        }
    }

    /**
     * Gets the value of the noProxyConfiguration
     * ({@link #myNoProxyConfiguration}) field.
     *
     * @return the value stored in the {@link #myNoProxyConfiguration} field.
     */
    public NoProxyConfiguration getNoProxyConfiguration()
    {
        if (myNoProxyConfiguration == null)
        {
            myNoProxyConfiguration = new NoProxyConfiguration();
        }
        return myNoProxyConfiguration;
    }

    /**
     * Gets the value of the systemProxyConfiguration
     * ({@link #mySystemProxyConfiguration}) field.
     *
     * @return the value stored in the {@link #mySystemProxyConfiguration}
     *         field.
     */
    public SystemProxyConfiguration getSystemProxyConfiguration()
    {
        if (mySystemProxyConfiguration == null)
        {
            mySystemProxyConfiguration = new SystemProxyConfiguration();
        }
        return mySystemProxyConfiguration;
    }

    /**
     * Gets the value of the urlProxyConfiguration
     * ({@link #myUrlProxyConfiguration}) field.
     *
     * @return the value stored in the {@link #myUrlProxyConfiguration} field.
     */
    public UrlProxyConfiguration getUrlProxyConfiguration()
    {
        if (myUrlProxyConfiguration == null)
        {
            myUrlProxyConfiguration = new UrlProxyConfiguration();
        }
        return myUrlProxyConfiguration;
    }

    /**
     * Gets the value of the manualProxyConfiguration
     * ({@link #myManualProxyConfiguration}) field.
     *
     * @return the value stored in the {@link #myManualProxyConfiguration}
     *         field.
     */
    public ManualProxyConfiguration getManualProxyConfiguration()
    {
        if (myManualProxyConfiguration == null)
        {
            myManualProxyConfiguration = new ManualProxyConfiguration();
        }
        return myManualProxyConfiguration;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return new ToStringHelper(this).add("SelectedProxy", mySelectedConfigurationType)
                .add(mySystemProxyConfiguration.toString()).add(myUrlProxyConfiguration.toString())
                .add(myManualProxyConfiguration.toString()).toStringPreferenceDump();
    }
}
