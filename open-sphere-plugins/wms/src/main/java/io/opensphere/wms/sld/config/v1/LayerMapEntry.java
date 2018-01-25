package io.opensphere.wms.sld.config.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.opengis.sld._100.StyledLayerDescriptor;

/**
 * Single entry in the map for a given layerKey.
 */
@XmlRootElement(name = "LayerMapEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerMapEntry
{
    /** The layer key. */
    @XmlAttribute(name = "key")
    private String myLayerKey;

    /** The list of SLDs for the layer. */
    @XmlElement(name = "StyledLayerDescriptor")
    private List<StyledLayerDescriptor> mySlds = new ArrayList<>();

    /**
     * Gets the layer key.
     *
     * @return the layer key
     */
    public String getLayerKey()
    {
        return myLayerKey;
    }

    /**
     * Gets the list of SLDs.
     *
     * @return the list of SLDs
     */
    public List<StyledLayerDescriptor> getSlds()
    {
        return mySlds;
    }

    /**
     * Sets the layer key.
     *
     * @param key the new layer key
     */
    public void setLayerKey(String key)
    {
        myLayerKey = key;
    }

    /**
     * Sets the list of SLDs.
     *
     * @param slds the new list of SLDs
     */
    public void setSlds(List<StyledLayerDescriptor> slds)
    {
        mySlds = slds;
    }
}
