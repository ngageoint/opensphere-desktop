package io.opensphere.wfs.layer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * Class that represents a collection of layers and the columns associated with
 * that layer.
 */
@XmlRootElement(name = "LayerColumns")
@XmlAccessorType(XmlAccessType.FIELD)
public class WFSLayerColumns implements Serializable
{
    /** Auto-generated Serial Version UID. */
    private static final long serialVersionUID = 7229266273620947508L;

    /** Key for the preferences. */
    public static final String PREFERENCE_KEY = "LayerColumns";

    /** The collection of layer configurations. */
    @XmlElement(name = "Layer")
    private Set<WFSLayerConfig> myLayers;

    /**
     * Default constructor.
     */
    public WFSLayerColumns()
    {
        myLayers = new HashSet<>();
    }

    /**
     * Copy CTOR.
     *
     * @param toCopy the to copy
     */
    public WFSLayerColumns(WFSLayerColumns toCopy)
    {
        myLayers = new HashSet<>();
        for (WFSLayerConfig config : toCopy.getLayers())
        {
            WFSLayerConfig newConfig = new WFSLayerConfig();
            newConfig.setLayerKey(config.getLayerKey());
            Set<String> newCols = New.set();
            for (String col : config.getDeselectedColumns())
            {
                newCols.add(col);
            }
            newConfig.setDeselectedColumns(newCols);
            myLayers.add(newConfig);
        }
    }

    /**
     * Add a new layer column configuration to our list. If an existing
     * configuration exists (has the same server and layer name) it will be
     * replaced by the new one.
     *
     * @param newLayer The new layer column configuration.
     * @return True if the new layer column info was added, false otherwise.
     */
    public boolean add(WFSLayerConfig newLayer)
    {
        WFSLayerConfig existing = findLayer(newLayer.getLayerKey());
        if (existing != null)
        {
            myLayers.remove(existing);
        }

        return myLayers.add(newLayer);
    }

    /**
     * Find the layer column configuration for the given server and layer name.
     *
     * @param layerKey the layer key
     * @return The layer column configuration for the given server and layer
     *         name if found, otherwise returns null.
     */
    public WFSLayerConfig findLayer(String layerKey)
    {
        for (WFSLayerConfig layer : myLayers)
        {
            if (layer.getLayerKey().equals(layerKey))
            {
                return layer;
            }
        }
        return null;
    }

    /**
     * Accessor for the layer column configurations.
     *
     * @return A set of the layer column configurations.
     */
    public Set<WFSLayerConfig> getLayers()
    {
        return myLayers;
    }

    /**
     * Removes a config from the list.
     *
     * @param key the key
     */
    public void remove(String key)
    {
        WFSLayerConfig existing = findLayer(key);
        if (existing != null)
        {
            myLayers.remove(existing);
        }
    }

    /**
     * Mutator for the layer column configurations.
     *
     * @param layers The new set of layer column configurations.
     */
    public void setLayers(Set<WFSLayerConfig> layers)
    {
        myLayers = layers;
    }
}
