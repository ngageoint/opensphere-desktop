package io.opensphere.wms.sld.config.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class LayerMap.
 */
@XmlRootElement(name = "LayerMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerMap
{
    /** List of entries in the map. */
    @XmlElement(name = "Layer")
    private final List<LayerMapEntry> myMapEntries = new ArrayList<>();

    /**
     * Adds a layer to the internal list.
     *
     * @param entry the entry
     */
    public void addLayer(LayerMapEntry entry)
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
    public List<LayerMapEntry> getLayers()
    {
        return myMapEntries;
    }
}
