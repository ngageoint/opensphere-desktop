package io.opensphere.core.net.config;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.ToStringHelper;

/** Configuration class for Manual proxies with exclusion lists. */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ManualProxyConfiguration extends ProxyConfiguration
{
    /** The host to which to connect for a proxy configuration. */
    @XmlElement(name = "host")
    private String myHost;

    /** The port to which to connect for a proxy configuration. */
    @XmlElement(name = "port")
    private int myPort;

    /** The exclusion patterns applied to the proxy configuration. */
    @XmlElement(name = "exclusions")
    private Set<String> myExclusionPatterns;

    /**
     * Gets the value of the exclusionPatterns ({@link #myExclusionPatterns})
     * field.
     *
     * @return the value stored in the {@link #myExclusionPatterns} field.
     */
    public Set<String> getExclusionPatterns()
    {
        if (myExclusionPatterns == null)
        {
            myExclusionPatterns = new HashSet<>();
        }
        return myExclusionPatterns;
    }

    /**
     * Gets the value of the host ({@link #myHost}) field.
     *
     * @return the value stored in the {@link #myHost} field.
     */
    public String getHost()
    {
        return myHost;
    }

    /**
     * Sets the value of the host ({@link #myHost}) field.
     *
     * @param host the value to store in the {@link #myHost} field.
     */
    public void setHost(String host)
    {
        myHost = host;
    }

    /**
     * Gets the value of the port ({@link #myPort}) field.
     *
     * @return the value stored in the {@link #myPort} field.
     */
    public int getPort()
    {
        return myPort;
    }

    /**
     * Sets the value of the port ({@link #myPort}) field.
     *
     * @param port the value to store in the {@link #myPort} field.
     */
    public void setPort(int port)
    {
        myPort = port;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return new ToStringHelper(this).add("Host", myHost).add("Port", myPort).addIfNotNull("Exclusions", myExclusionPatterns)
                .toString();
    }
}
