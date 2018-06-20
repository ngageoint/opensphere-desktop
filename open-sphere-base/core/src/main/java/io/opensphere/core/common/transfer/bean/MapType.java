package io.opensphere.core.common.transfer.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/** XML map type. */
@XmlAccessorType(XmlAccessType.NONE)
public class MapType
{
    /**
     * The set of entries associated with the map type.
     */
    @XmlElement(name = "entry")
    List<MapEntryType> entries = new ArrayList<>();

    /**
     * Retrieves the value of the entries field.
     *
     * @return the entries
     */
    public List<MapEntryType> getEntries()
    {
        return entries;
    }

    /**
     * Sets the value of the entries field.
     *
     * @param pEntries the entries to store.
     */
    public void setEntries(List<MapEntryType> pEntries)
    {
        entries = pEntries;
    }
}
