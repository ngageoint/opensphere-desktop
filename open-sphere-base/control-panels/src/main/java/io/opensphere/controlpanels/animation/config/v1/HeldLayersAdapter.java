package io.opensphere.controlpanels.animation.config.v1;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.controlpanels.animation.config.v1.HeldLayersAdapter.Entries;
import io.opensphere.core.model.time.ISO8601TimeSpanAdapter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/** Held layers XML adapter. */
public class HeldLayersAdapter extends XmlAdapter<Entries, Map<TimeSpan, Collection<String>>>
{
    @Override
    public Map<TimeSpan, Collection<String>> unmarshal(Entries entries)
    {
        Map<TimeSpan, Collection<String>> map = New.map();
        for (Entry entry : entries.getEntries())
        {
            map.put(entry.getKey(), entry.getValues());
        }
        return map;
    }

    @Override
    public Entries marshal(Map<TimeSpan, Collection<String>> map)
    {
        Entries entries = new Entries();
        for (Map.Entry<TimeSpan, Collection<String>> entry : map.entrySet())
        {
            entries.getEntries().add(new Entry(entry.getKey(), entry.getValue()));
        }
        return entries;
    }

    /** The list of entries in the map. */
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Entries
    {
        /** The entries. */
        @XmlElement(name = "entry")
        private Collection<Entry> myEntries = New.list();

        /**
         * Gets the entries.
         *
         * @return the entries
         */
        public Collection<Entry> getEntries()
        {
            return myEntries;
        }

        /**
         * Sets the entries.
         *
         * @param entries the entries
         */
        public void setEntries(Collection<Entry> entries)
        {
            myEntries = entries;
        }
    }

    /** An entry in the map. */
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Entry
    {
        /** The key. */
        @XmlAttribute(name = "key")
        @XmlJavaTypeAdapter(ISO8601TimeSpanAdapter.class)
        private TimeSpan myKey;

        /** The values. */
        @XmlElement(name = "value")
        private Collection<String> myValues;

        /** Constructor. */
        public Entry()
        {
        }

        /**
         * Constructor.
         *
         * @param key The key
         * @param values The values
         */
        public Entry(TimeSpan key, Collection<String> values)
        {
            myKey = key;
            myValues = values;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public TimeSpan getKey()
        {
            return myKey;
        }

        /**
         * Sets the key.
         *
         * @param key the key
         */
        public void setKey(TimeSpan key)
        {
            myKey = key;
        }

        /**
         * Gets the values.
         *
         * @return the values
         */
        public Collection<String> getValues()
        {
            return myValues;
        }

        /**
         * Sets the values.
         *
         * @param values the values
         */
        public void setValues(Collection<String> values)
        {
            myValues = values;
        }
    }
}
