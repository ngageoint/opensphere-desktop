package io.opensphere.wfs.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.server.services.AbstractServerDataTypeInfo;

/**
 * WFS Configuration which applies to the server.
 */
public class WFSServerConfig implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The unique ID used to identify this server.
     */
    private String myWfsServerId;

    /**
     * Layers folders provided by this server.
     */
    private List<? extends AbstractServerDataTypeInfo> myLayers;

    /**
     * This title is for display purposes. Provided in the GetCapabilities
     * document.
     */
    private String myServerTitle;

    /**
     * The current state of the server.
     */
    private WFSServerState myServerState;

    @Override
    public WFSServerConfig clone() throws CloneNotSupportedException
    {
        WFSServerConfig clone = (WFSServerConfig)super.clone();
        if (myLayers != null && !myLayers.isEmpty())
        {
            clone.setLayers(myLayers);
        }
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
        WFSServerConfig other = (WFSServerConfig)obj;
        return Objects.equals(myWfsServerId, other.myWfsServerId);
    }

    /**
     * Get the layer List.
     *
     * @return the layer list
     */
    public List<? extends AbstractServerDataTypeInfo> getLayers()
    {
        if (myLayers != null)
        {
            return Collections.unmodifiableList(myLayers);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    /**
     * Get the unique WFS server ID.
     *
     * @return the unique WFS server ID
     */
    public String getServerId()
    {
        return myWfsServerId;
    }

    /**
     * Accessor for the server state.
     *
     * @return The server's state.
     */
    public WFSServerState getServerState()
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myWfsServerId);
        return result;
    }

    /**
     * Set the layers.
     *
     * @param layers the layers to set
     */
    public void setLayers(Collection<? extends AbstractServerDataTypeInfo> layers)
    {
        myLayers = CollectionUtilities.getList(layers);
    }

    /**
     * Set the unique WFS Server ID.
     *
     * @param serverId the unique WFS Server ID to set
     */
    public void setServerId(String serverId)
    {
        myWfsServerId = serverId;
    }

    /**
     * Mutator for the server state.
     *
     * @param state The state of this server
     */
    public void setServerState(WFSServerState state)
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

    @Override
    public String toString()
    {
        return myServerTitle;
    }

    /** The current state of the server. */
    public enum WFSServerState
    {
        /** Unknown server state. */
        UNKNOWN,

        /** Server is ACTIVE. */
        ACTIVE,

        /** Server is INACTIVE but available. */
        INACTIVE,

        /** There was an error activating or initializing the server. */
        ERROR,
    }
}
