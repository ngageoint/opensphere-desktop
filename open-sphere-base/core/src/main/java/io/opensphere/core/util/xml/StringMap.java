package io.opensphere.core.util.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.JAXBWrapper;

/**
 * JAXB class for a string to string map. This class is not thread-safe.
 */
@XmlRootElement(name = "StringMap")
@XmlAccessorType(XmlAccessType.NONE)
public class StringMap implements JAXBWrapper<JAXBableStringMap>
{
    /**
     * The map entries.
     */
    @XmlElement(name = "JAXBEntry")
    private final List<JAXBEntry> myEntries = new ArrayList<>();

    /**
     * Constructor.
     */
    public StringMap()
    {
    }

    /**
     * Constructor.
     *
     * @param map The map.
     */
    public StringMap(Map<String, String> map)
    {
        setMap(map);
    }

    /**
     * Get the map.
     *
     * @return The strings.
     */
    public Map<String, String> getMap()
    {
        Map<String, String> map = new LinkedHashMap<>(myEntries.size());
        for (JAXBEntry xmlEntry : myEntries)
        {
            map.put(xmlEntry.getKey(), xmlEntry.getValue());
        }
        return map;
    }

    @Override
    public JAXBableStringMap getWrappedObject()
    {
        return new JAXBableStringMap(getMap());
    }

    /**
     * Set the map.
     *
     * @param map The map.
     */
    public final void setMap(Map<String, String> map)
    {
        myEntries.clear();
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            JAXBEntry xmlEntry = new JAXBEntry();
            xmlEntry.setKey(entry.getKey());
            xmlEntry.setValue(entry.getValue());
            myEntries.add(xmlEntry);
        }
    }

    @Override
    public String toString()
    {
        return myEntries.toString();
    }

    /**
     * JAXB class for a map entry.
     */
    @XmlAccessorType(XmlAccessType.NONE)
    private static class JAXBEntry
    {
        /** The key. */
        @XmlElement(name = "key")
        private String myKey;

        /** The value. */
        @XmlElement(name = "value")
        private String myValue;

        /**
         * Constructor.
         */
        public JAXBEntry()
        {
        }

        /**
         * Accessor for the key.
         *
         * @return The key.
         */
        public final String getKey()
        {
            return myKey;
        }

        /**
         * Accessor for the value.
         *
         * @return The value.
         */
        public final String getValue()
        {
            return myValue;
        }

        /**
         * Mutator for the key.
         *
         * @param key The key to set.
         */
        public void setKey(String key)
        {
            myKey = key;
        }

        /**
         * Mutator for the value.
         *
         * @param value The value to set.
         */
        public void setValue(String value)
        {
            myValue = value;
        }
    }
}
