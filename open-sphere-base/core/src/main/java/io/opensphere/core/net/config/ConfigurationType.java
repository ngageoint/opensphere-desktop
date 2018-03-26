package io.opensphere.core.net.config;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/** An enumeration over the set of available proxy types. */
@XmlType
@XmlEnum
public enum ConfigurationType
{
    /** The enum type for no proxy. */
    @XmlEnumValue("none")
    NONE,

    /** The enum type for system proxy. */
    @XmlEnumValue("system")
    SYSTEM,

    /** The enum type for a URL proxy. */
    @XmlEnumValue("url")
    URL,

    /** The enum type for manual proxy. */
    @XmlEnumValue("manual")
    MANUAL;
}
