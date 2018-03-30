package io.opensphere.core.net.config;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.ToStringHelper;

/**
 * Configuration class for system proxy configurations, with exclusion patterns.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SystemProxyConfiguration extends ProxyConfiguration
{
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
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return new ToStringHelper(this).addIfNotNull("Exclusions", myExclusionPatterns).toString();
    }
}
