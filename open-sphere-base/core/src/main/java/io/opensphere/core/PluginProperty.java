package io.opensphere.core;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB-generated class for a plugin property in pluginLoader.xml.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "myKey", "myValue" })
public class PluginProperty
{
    /** Property key. */
    @XmlElement(name = "key", required = true)
    private String myKey;

    /** Property value. */
    @XmlElement(name = "value", required = true)
    private String myValue;

    /**
     * Gets the value of the key property.
     *
     * @return possible object is {@link String }
     */
    public String getKey()
    {
        return myKey;
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue()
    {
        return myValue;
    }

    /**
     * Sets the value of the key property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setKey(String value)
    {
        myKey = value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setValue(String value)
    {
        myValue = value;
    }
}
