package io.opensphere.mantle.util.columnanalyzer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class DataTypeAnalysisXmlDataMap.
 */
@XmlRootElement(name = "DataTypeAnalysisXmlDataMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataTypeAnalysisXmlDataMap
{
    /** List of entries in the map. */
    @XmlElement(name = "Type")
    private final List<DataTypeAnalaysMapXmMapEntry> myMapEntries = new ArrayList<>();

    /**
     * Adds a layer to the internal list.
     *
     * @param entry the entry
     */
    public void addLayer(DataTypeAnalaysMapXmMapEntry entry)
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
    public List<DataTypeAnalaysMapXmMapEntry> getDataList()
    {
        return myMapEntries;
    }
}
