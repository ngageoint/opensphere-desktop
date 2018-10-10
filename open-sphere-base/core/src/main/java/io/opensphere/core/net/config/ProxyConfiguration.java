package io.opensphere.core.net.config;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

/** The base class for proxy configurations. */
@XmlTransient // Prevents the mapping of a JavaBean property/type to XML
// representation
@XmlSeeAlso({ NoProxyConfiguration.class, SystemProxyConfiguration.class, UrlProxyConfiguration.class,
    ManualProxyConfiguration.class })
public abstract class ProxyConfiguration
{
    /* intentionally blank */
}
