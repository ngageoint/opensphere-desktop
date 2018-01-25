package io.opensphere.wms.config.v1;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.wms.config.v1.WMSLayerConfigChangeListener.WMSLayerConfigChangeEvent;
import io.opensphere.wms.sld.WMSUserDefinedSymbolization;

/**
 * WMS Configuration which applies to the server.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WMSServerConfig implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Listeners for layer configuration changes.
     */
    private final transient ChangeSupport<WMSLayerConfigChangeListener> myLayerConfigChangeSupport = new StrongChangeSupport<>();

    /**
     * Layers that have been modified by a user and persisted between sessions.
     */
    @XmlElement(name = "LayerConfigs")
    private List<WMSLayerConfig> myStoredLayers;

    /**
     * Unmodified layers from the server that only exist for the current
     * session.
     */
    @XmlTransient
    private List<WMSLayerConfig> myTransientLayers;

    /**
     * Unique string used to identify this server. Note: This is usually the
     * base URL used to request the WMS capabilities document.
     */
    @XmlElement(name = "ServerIdentifier")
    private String myServerId;

    /**
     * The current state of the server.
     */
    @XmlTransient
    private WMSServerState myServerState;

    /** The user defined symbolization. */
    private transient WMSUserDefinedSymbolization myUserDefinedSymbolization;

    /**
     * This title is for display purposes. Provided in the GetCapabilities
     * document.
     */
    @XmlElement(name = "ServerTitle")
    private String myServerTitle;

    /**
     * Add a layer to the list of default (non-persistent) layers. If there is a
     * persisted configuration for the layer, compare it to the default to keep
     * it from being needlessly persisted.
     *
     * @param layer the layer to add
     */
    public void addDefaultLayer(WMSLayerConfig layer)
    {
        if (layer != null)
        {
            if (myTransientLayers == null)
            {
                myTransientLayers = New.list();
            }
            if (!myTransientLayers.contains(layer))
            {
                myTransientLayers.add(layer);
            }

            WMSLayerConfig storedConfig = getStoredLayer(layer.getLayerName());
            if (storedConfig != null && storedConfig.configurableFieldsEqual(layer))
            {
                myStoredLayers.remove(storedConfig);
            }
        }
    }

    /**
     * Add a listener for layer configuration changes.
     *
     * @param listener The listener to add.
     */
    public void addLayerConfigListener(WMSLayerConfigChangeListener listener)
    {
        myLayerConfigChangeSupport.addListener(listener);
    }

    @Override
    public WMSServerConfig clone() throws CloneNotSupportedException
    {
        WMSServerConfig clone = (WMSServerConfig)super.clone();
        clone.setStoredLayers(New.list(myStoredLayers));
        clone.setTransientLayers(New.list(myTransientLayers));
        return clone;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        WMSServerConfig other = (WMSServerConfig)obj;
        return Objects.equals(myServerId, other.myServerId);
    }

    /**
     * Find The layer with the given name. First, check the list of
     * user-modified layers that have been stored. If not found, check the
     * default (transient) layers.
     *
     * @param layerName the name of the layer to find.
     * @return the layer or <code>null</code> if not found.
     */
    public WMSLayerConfig getLayer(String layerName)
    {
        WMSLayerConfig layer = getStoredLayer(layerName);
        if (layer == null)
        {
            layer = getTransientLayer(layerName);
        }
        return layer;
    }

    /**
     * Get the string that uniquely identifies this server.
     *
     * @return the Server ID string
     */
    public String getServerId()
    {
        return myServerId;
    }

    /**
     * Accessor for the server state.
     *
     * @return The server's state.
     */
    public WMSServerState getServerState()
    {
        return myServerState;
    }

    /**
     * Get the serverTitle.
     *
     * @return the serverTitle
     */
    public String getServerTitle()
    {
        return myServerTitle;
    }

    /**
     * Gets the user defined symbolization.
     *
     * @return the user defined symbolization
     */
    public WMSUserDefinedSymbolization getUserDefinedSymbolization()
    {
        return myUserDefinedSymbolization;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myServerId);
        return result;
    }

    /**
     * Notify interested parties that layer configuration has changed.
     *
     * @param event The configuration change event.
     */
    public void notifyLayerConfigChanged(final WMSLayerConfigChangeEvent event)
    {
        myLayerConfigChangeSupport.notifyListeners(new Callback<WMSLayerConfigChangeListener>()
        {
            @Override
            public void notify(WMSLayerConfigChangeListener listener)
            {
                listener.configurationChanged(event);
            }
        }, null);
    }

    /**
     * Remove a listener for layer configuration changes.
     *
     * @param listener The listener to remove.
     */
    public void removeLayerConfigListener(WMSLayerConfigChangeListener listener)
    {
        myLayerConfigChangeSupport.removeListener(listener);
    }

    /**
     * Set the string that uniquely identifies this server.
     *
     * @param serverId the Server ID string
     */
    public void setServerId(String serverId)
    {
        myServerId = serverId;
    }

    /**
     * Mutator for the server state.
     *
     * @param state The state of this server
     */
    public void setServerState(WMSServerState state)
    {
        myServerState = state;
    }

    /**
     * Set the serverTitle.
     *
     * @param serverTitle the serverTitle to set
     */
    public void setServerTitle(String serverTitle)
    {
        myServerTitle = serverTitle;
    }

    /**
     * Sets the user defined symbolization.
     *
     * @param symbolization the new user defined symbolization
     */
    public void setUserDefinedSymbolization(WMSUserDefinedSymbolization symbolization)
    {
        myUserDefinedSymbolization = symbolization;
    }

    /**
     * Save the configuration for the layer. If the config matches the default
     * configuration for that layer (i.e. if a user has reverted all changes)
     * then the layer will be removed from the set of stored layers.
     *
     * @param layerConfig The new configuration.
     */
    public void storeLayer(WMSLayerConfig layerConfig)
    {
        if (layerConfig != null)
        {
            WMSLayerConfig oldStoredConfig = getStoredLayer(layerConfig.getLayerName());
            if (oldStoredConfig != null)
            {
                myStoredLayers.remove(oldStoredConfig);
            }

            WMSLayerConfig defaultConfig = getTransientLayer(layerConfig.getLayerName());
            if (!layerConfig.configurableFieldsEqual(defaultConfig))
            {
                addStoredLayer(layerConfig);
            }
        }
    }

    @Override
    public String toString()
    {
        return myServerTitle;
    }

    /**
     * Set the list of stored (persisted) layers.
     *
     * @param layers the layer list to set
     */
    protected void setStoredLayers(List<WMSLayerConfig> layers)
    {
        myStoredLayers = layers;
    }

    /**
     * Set the list of non-persisted layers.
     *
     * @param layers the layer list to set
     */
    protected void setTransientLayers(List<WMSLayerConfig> layers)
    {
        myTransientLayers = layers;
    }

    /**
     * Add a layer to the list of saved layers.
     *
     * @param layer the layer to add
     */
    private void addStoredLayer(WMSLayerConfig layer)
    {
        if (myStoredLayers == null)
        {
            myStoredLayers = New.list();
        }
        if (!myStoredLayers.contains(layer))
        {
            myStoredLayers.add(layer);
        }
    }

    /**
     * Find a stored layer with the given name. If not found, return null
     *
     * @param layerName the name of the layer to find.
     * @return the layer or <code>null</code> if not found.
     */
    private WMSLayerConfig getStoredLayer(String layerName)
    {
        if (CollectionUtilities.hasContent(myStoredLayers))
        {
            for (WMSLayerConfig layer : myStoredLayers)
            {
                if (layer.getLayerName().equals(layerName))
                {
                    return layer;
                }
            }
        }
        return null;
    }

    /**
     * Find a transient layer with the given name. If not found, return null
     *
     * @param layerName the name of the layer to find.
     * @return the layer or <code>null</code> if not found.
     */
    private WMSLayerConfig getTransientLayer(String layerName)
    {
        if (CollectionUtilities.hasContent(myTransientLayers))
        {
            for (WMSLayerConfig layer : myTransientLayers)
            {
                if (layer.getLayerName().equals(layerName))
                {
                    return layer;
                }
            }
        }
        return null;
    }

    /** The current state of the server. */
    public enum WMSServerState
    {
        /** Server is ACTIVE. */
        ACTIVE,

        /** There was an error activating or initializing the server. */
        ERROR,

        /** Server is INACTIVE but available. */
        INACTIVE,

        /** Unknown server state. */
        UNKNOWN,
    }
}
