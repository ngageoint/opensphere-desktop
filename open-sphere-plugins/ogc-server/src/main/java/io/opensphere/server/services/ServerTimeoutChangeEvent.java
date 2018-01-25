package io.opensphere.server.services;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;

/**
 * The Class ServerTimeoutChangeEvent.
 */
public class ServerTimeoutChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The Server title. */
    private final String myServerTitle;

    /** The Connect timeout ms. */
    private final int myConnectTimeoutMS;

    /** The Read timeout ms. */
    private final int myReadTimeoutMS;

    /** The OGC server source session unique id. */
    private final int myOGCServerSourceSessionUniqueId;

    /** The Is default change. */
    private final boolean myIsDefaultChange;

    /** The Source. */
    private final Object mySource;

    /**
     * Instantiates a new server timeout change event.
     *
     * @param serverTitle the server title
     * @param ogcServerSourceSessionUniqueId the ogc server source session
     *            unique id
     * @param connectTimeoutMS the connect timeout ms
     * @param readTimeoutMS the read timeout ms
     * @param isDefaultChange the is default change
     * @param source the source
     */
    public ServerTimeoutChangeEvent(String serverTitle, int ogcServerSourceSessionUniqueId, int connectTimeoutMS,
            int readTimeoutMS, boolean isDefaultChange, Object source)
    {
        myServerTitle = serverTitle;
        myOGCServerSourceSessionUniqueId = ogcServerSourceSessionUniqueId;
        myReadTimeoutMS = readTimeoutMS;
        myConnectTimeoutMS = connectTimeoutMS;
        myIsDefaultChange = isDefaultChange;
        mySource = source;
    }

    /**
     * Gets the connect timeout ms.
     *
     * @return the connect timeout ms
     */
    public final int getConnectTimeoutMS()
    {
        return myConnectTimeoutMS;
    }

    @Override
    public String getDescription()
    {
        return ServerTimeoutChangeEvent.class.getName();
    }

    /**
     * Gets the oGC server source session unique id.
     *
     * @return the oGC server source session unique id
     */
    public final int getOGCServerSourceSessionUniqueId()
    {
        return myOGCServerSourceSessionUniqueId;
    }

    /**
     * Gets the read timeout ms.
     *
     * @return the read timeout ms
     */
    public final int getReadTimeoutMS()
    {
        return myReadTimeoutMS;
    }

    /**
     * Gets the server title.
     *
     * @return the server title
     */
    public final String getServerTitle()
    {
        return myServerTitle;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Checks if is default change.
     *
     * @return true, if is default change
     */
    public boolean isDefaultChange()
    {
        return myIsDefaultChange;
    }
}
