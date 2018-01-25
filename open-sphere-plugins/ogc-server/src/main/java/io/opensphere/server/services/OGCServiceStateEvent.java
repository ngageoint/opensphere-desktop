package io.opensphere.server.services;

import java.util.Collection;

import io.opensphere.core.event.AbstractMultiStateEvent;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * An event on a server.
 */
public class OGCServiceStateEvent extends AbstractMultiStateEvent
{
    /** The layer configuration from this server. */
    private final DataGroupInfo myDataGroup;

    /** In case of an error, provide a string detailing the problem. */
    private String myErrorString;

    /**
     * Boolean indicating whether the Server was able to successfully retrieve
     * and confidently parse the GetCapabilities document.
     */
    private final boolean myIsValid;

    /** A list of layers not included in myDataGroup. */
    private final Collection<? extends AbstractServerDataTypeInfo> myLayerList;

    /**
     * The base URL that this server communicates with. This should be unique
     * for each Envoy.
     */
    private final String myServerId;

    /** The OGC service for the plugin that sent this State Event. */
    private final String myService;

    /** The title (Human-readable name) provided by the service. */
    private final String myTitle;

    /**
     * Construct me.
     *
     * @param serverId The unique ID for this service.
     * @param title the human-readable name provided by the service.
     * @param service the service that created this Event.
     * @param isValidated flag indicating whether the server validated
     *            successfully.
     */
    public OGCServiceStateEvent(String serverId, String title, String service, boolean isValidated)
    {
        this(serverId, title, service, null, null, isValidated);
    }

    /**
     * Construct me.
     *
     * @param serverId The unique ID for this service.
     * @param title the human-readable name provided by the service.
     * @param service the service that created this Event.
     * @param layers layers that haven't been assigned to a dataGroup
     * @param isValidated flag indicating whether the server validated
     *            successfully.
     */
    public OGCServiceStateEvent(String serverId, String title, String service,
            Collection<? extends AbstractServerDataTypeInfo> layers, boolean isValidated)
    {
        this(serverId, title, service, null, layers, isValidated);
    }

    /**
     * Construct me.
     *
     * @param serverId The unique ID for this service.
     * @param title the human-readable name provided by the service.
     * @param service the service that created this Event.
     * @param dataGroup the Server's layer configuration.
     * @param isValidated flag indicating whether the server validated
     *            successfully.
     */
    public OGCServiceStateEvent(String serverId, String title, String service, DataGroupInfo dataGroup, boolean isValidated)
    {
        this(serverId, title, service, dataGroup, null, isValidated);
    }

    /**
     * Construct me.
     *
     * @param serverId The unique ID for this service.
     * @param title the human-readable name provided by the service.
     * @param service the service that created this Event.
     * @param dataGroup the Server's layer configuration.
     * @param layers additional layers that don't show up in the dataGroup
     * @param isValidated flag indicating whether the server validated
     *            successfully.
     */
    private OGCServiceStateEvent(String serverId, String title, String service, DataGroupInfo dataGroup,
            Collection<? extends AbstractServerDataTypeInfo> layers, boolean isValidated)
    {
        myDataGroup = dataGroup;
        myIsValid = isValidated;
        myLayerList = layers;
        myServerId = serverId;
        myService = service;
        myTitle = title;
    }

    @Override
    public String getDescription()
    {
        return "An event indicating that a server has been added, activated, or deactivated.";
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public String getError()
    {
        return myErrorString;
    }

    /**
     * Gets the list of layers that are not included in the layer tree. This
     * could also be used for services whose layers are not hierarchical.
     *
     * @return the layer list
     */
    public Collection<? extends AbstractServerDataTypeInfo> getLayerList()
    {
        return myLayerList;
    }

    /**
     * Get the layers from the Server in the form of a DataGroupInfo.
     *
     * @return the server layers
     */
    public DataGroupInfo getLayerTree()
    {
        return myDataGroup;
    }

    /**
     * Gets the server ID. This is used to uniquely identify the source server
     * and is usually the server URL (but not always).
     *
     * @return the server url
     */
    public String getServerId()
    {
        return myServerId;
    }

    /**
     * Gets the title (Human-readable name) provided by the service.
     *
     * @return the service's server title
     */
    public String getServerTitle()
    {
        return myTitle;
    }

    /**
     * Gets the OGC service that sent this State Event.
     *
     * @return the service
     */
    public String getService()
    {
        return myService;
    }

    /**
     * Get the validation flag.
     *
     * @return true, if the server validated successfully.
     */
    public boolean isValid()
    {
        return myIsValid;
    }

    /**
     * Sets the error.
     *
     * @param errorString the new error
     */
    public void setError(String errorString)
    {
        myErrorString = errorString;
    }
}
