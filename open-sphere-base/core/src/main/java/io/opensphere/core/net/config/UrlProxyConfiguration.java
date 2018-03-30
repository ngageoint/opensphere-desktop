package io.opensphere.core.net.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.ToStringHelper;

/** Configuration class for URL-based proxies. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UrlProxyConfiguration extends ProxyConfiguration
{
    /** The URL used for proxy configuration. */
    @XmlElement(name = "url")
    private String myProxyUrl;

    /**
     * Gets the value of the proxyUrl ({@link #myProxyUrl}) field.
     *
     * @return the value stored in the {@link #myProxyUrl} field.
     */
    public String getProxyUrl()
    {
        return myProxyUrl;
    }

    /**
     * Sets the value of the proxyUrl ({@link #myProxyUrl}) field.
     *
     * @param proxyUrl the value to store in the {@link #myProxyUrl} field.
     */
    public void setProxyUrl(String proxyUrl)
    {
        myProxyUrl = proxyUrl;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return new ToStringHelper(this).addIfNotNull("Url", myProxyUrl).toString();
    }
}
