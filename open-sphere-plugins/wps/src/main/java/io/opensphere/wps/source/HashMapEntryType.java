package io.opensphere.wps.source;

import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * Defining a type to avoid JAXB bug which causes maps marshalled incorrectly.
 *
 * @see <a href= "https://jaxb.dev.java.net/guide/Mapping_your_favorite_class.html">https
 *      ://jaxb.dev.java.net/guide/Mapping_your_favorite_class.html</a>
 */
@XmlType(name = "param")
@XmlAccessorType(XmlAccessType.FIELD)
public class HashMapEntryType
{
    /** My key. */
    @XmlElement(name = "key", required = true)
    private String myKey;

    /** My value. */
    @XmlElement(name = "value", required = true)
    private String myValue;

    /**
     * Instantiates a new hash map entry type that can marshalled/unmarshalled via JAXB.
     */
    public HashMapEntryType()
    {
    }

    /**
     * Instantiates a new hash map entry type.
     *
     * @param entry the entry to build from
     */
    public HashMapEntryType(Map.Entry<String, String> entry)
    {
        myKey = entry.getKey();
        myValue = entry.getValue();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        HashMapEntryType other = (HashMapEntryType)obj;
        //@formatter:off
        return Objects.equals(myKey, other.myKey)
                && Objects.equals(myValue, other.myValue);
        //@formatter:on
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey()
    {
        return myKey;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue()
    {
        return myValue;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myKey);
        result = prime * result + HashCodeHelper.getHashCode(myValue);
        return result;
    }

    /**
     * Sets the key.
     *
     * @param key the new key
     */
    public void setKey(String key)
    {
        myKey = key;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(String value)
    {
        myValue = value;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return myKey + "='" + myValue + "'";
    }
}
