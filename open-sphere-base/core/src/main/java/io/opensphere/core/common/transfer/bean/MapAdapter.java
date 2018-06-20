package io.opensphere.core.common.transfer.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/** XML adapter for maps. */
public class MapAdapter extends XmlAdapter<MapType, Map<String, String>>
{
    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public MapType marshal(Map<String, String> pHashMap)
    {
        MapType output = new MapType();
        for (Entry<String, String> entry : pHashMap.entrySet())
        {
            MapEntryType xmlEntry = new MapEntryType();
            xmlEntry.key = entry.getKey();
            xmlEntry.value = entry.getValue();
            output.getEntries().add(xmlEntry);
        }
        return output;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
     */
    @Override
    public Map<String, String> unmarshal(MapType pXmlMap)
    {
        Map<String, String> output = new HashMap<>();
        for (MapEntryType entry : pXmlMap.getEntries())
        {
            output.put(entry.key, entry.value);
        }
        return output;
    }
}
