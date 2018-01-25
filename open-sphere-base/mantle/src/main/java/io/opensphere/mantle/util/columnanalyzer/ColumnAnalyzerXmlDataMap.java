package io.opensphere.mantle.util.columnanalyzer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class ColumnAnalyzerXmlDataMap.
 */
@XmlRootElement(name = "ColumnAnalyzerXmlDataMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnAnalyzerXmlDataMap
{
    /** List of entries in the map. */
    @XmlElement(name = "Column")
    private final List<ColumnAnalyzerXmlDataMapEntry> myMapEntries = new ArrayList<>();

    /**
     * Adds a layer to the internal list.
     *
     * @param entry the entry
     */
    public void addLayer(ColumnAnalyzerXmlDataMapEntry entry)
    {
        if (entry != null)
        {
            myMapEntries.add(entry);
        }
    }

    /**
     * Gets the layers.
     *
     * @return the layers
     */
    public List<ColumnAnalyzerXmlDataMapEntry> getDataList()
    {
        return myMapEntries;
    }
}
