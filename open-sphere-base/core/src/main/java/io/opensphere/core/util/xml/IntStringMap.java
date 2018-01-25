package io.opensphere.core.util.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import io.opensphere.core.util.JAXBWrapper;

/**
 * JAXB class for a int to string map. This class is not thread-safe.
 */
@XmlRootElement(name = "StringMap")
@XmlAccessorType(XmlAccessType.NONE)
public class IntStringMap implements JAXBWrapper<JAXBableIntStringMap>
{
    /**
     * The map entries.
     */
    @XmlElement(name = "IntStringPair")
    private final List<IntStringPair> myEntries = new ArrayList<>();

    /**
     * Constructor.
     */
    public IntStringMap()
    {
    }

    /**
     * Constructor.
     *
     * @param map The map.
     */
    public IntStringMap(TIntObjectMap<String> map)
    {
        setMap(map);
    }

    /**
     * Get the map.
     *
     * @return The strings.
     */
    public TIntObjectMap<String> getMap()
    {
        TIntObjectMap<String> map = new TIntObjectHashMap<>(myEntries.size());
        for (IntStringPair xmlEntry : myEntries)
        {
            map.put(xmlEntry.getKey(), xmlEntry.getValue());
        }
        return map;
    }

    @Override
    public JAXBableIntStringMap getWrappedObject()
    {
        return new JAXBableIntStringMap(getMap());
    }

    /**
     * Set the map.
     *
     * @param map The map.
     */
    public final void setMap(TIntObjectMap<String> map)
    {
        myEntries.clear();
        map.forEachEntry(new TIntObjectProcedure<String>()
        {
            @Override
            public boolean execute(int key, String value)
            {
                IntStringPair xmlEntry = new IntStringPair();
                xmlEntry.setKey(key);
                xmlEntry.setValue(value);
                myEntries.add(xmlEntry);
                return true;
            }
        });
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
    private static class IntStringPair
    {
        /** The key. */
        @XmlElement(name = "key")
        private int myKey;

        /** The value. */
        @XmlElement(name = "value")
        private String myValue;

        /**
         * Constructor.
         */
        public IntStringPair()
        {
        }

        /**
         * Accessor for the key.
         *
         * @return The key.
         */
        public final int getKey()
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
        public void setKey(int key)
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
