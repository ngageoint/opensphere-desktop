package io.opensphere.wps.envoy;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.event.Event.State;
import io.opensphere.core.event.EventManager;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wfs.consumer.FeatureConsumerManager;
import io.opensphere.wps.util.WpsServerConnectionHelper;

/**
 * An abstract base implementation of an envoy, designed to be used to perform a single WPS request type against a single server.
 *
 * @param <RESPONSE_TYPE> the WPS type returned by the envoy.
 */
@SuppressWarnings("PMD.GenericsNaming")
public abstract class AbstractWpsEnvoy<RESPONSE_TYPE> extends AbstractEnvoy
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(AbstractWpsEnvoy.class);

    /**
     * The server configuration used to interact with the remote system.
     */
    @GuardedBy("this")
    private final ServerConnectionParams myServer;

    /**
     * The request type for which this envoy is configured.
     */
    private final WpsRequestType myRequestType;

    /**
     * Manager used to retrieve feature consumers.
     */
    private final FeatureConsumerManager myConsumerManager;

    /**
     * Creates a new envoy through which a WPS request is executed for a single server.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     * @param pServer the configuration of the server to which the query will be made.
     * @param pRequestType The request type for which this envoy is configured.
     */
    public AbstractWpsEnvoy(Toolbox pToolbox, ServerConnectionParams pServer, WpsRequestType pRequestType)
    {
        super(pToolbox);

        myServer = pServer;
        myRequestType = pRequestType;
        myConsumerManager = new FeatureConsumerManager(MantleToolboxUtils.getMantleToolbox(pToolbox), pToolbox.getTimeManager());
    }

    /**
     * Gets the value of the {@link #myServer} field.
     *
     * @return the value stored in the {@link #myServer} field.
     */
    protected ServerConnectionParams getServer()
    {
        return myServer;
    }

    /**
     * Gets the value of the {@link #myRequestType} field.
     *
     * @return the value stored in the {@link #myRequestType} field.
     */
    public WpsRequestType getRequestType()
    {
        return myRequestType;
    }

    /**
     * Fire state event to the event manager to indicate actions occurred.
     *
     * @param pServer the WPS Server configuration for which the event is being fired.
     * @param pValid the valid flag
     * @param pErrorText String detailing any errors that occurred.
     * @param pEventManager the event manager used to send the new state event
     * @param pDataGroup The data group to which the data belongs.
     */
    protected void fireStateEvent(ServerConnectionParams pServer, boolean pValid, String pErrorText, EventManager pEventManager,
            DataGroupInfo pDataGroup)
    {
        LOG.debug("Firing state event for WPS server.");

        if (pEventManager != null)
        {
            OGCServiceStateEvent stateEvent = new OGCServiceStateEvent(pServer.getWpsUrl(), pServer.getServerTitle(),
                    OGCServerSource.WPS_SERVICE, pDataGroup, pValid);
            if (pErrorText != null)
            {
                stateEvent.setError(pErrorText);
                // Note: EventManager.setEventState publishes the event
                pEventManager.setEventState(stateEvent, State.FAILED);
                LOG.warn(pErrorText);
            }
            else
            {
                // Note: EventManager.setEventState publishes the event
                pEventManager.setEventState(stateEvent, State.COMPLETED);
            }
        }
    }

    /**
     * An optional event propagation method in which a state event is published to notify listeners of new data sources.
     *
     * @param pErrorMessage an optional error message to publish if problems were encountered.
     */
    protected void fireStateEvent(String pErrorMessage)
    {
        /* intentionally blank */
    }

    /**
     * Performs a WPS request, for the server defined in the {@link #getServer()} field. This is an optional operation, and will
     * throw an unsupported operation exception if not implemented.
     *
     * @param pParameters the set of parameters with which to execute the query (not all queries require parameters, as such, this
     *            parameter is optional).
     * @return a WPS response object returned from the remote server.
     * @throws QueryException if the remote server cannot be queried.
     * @throws UnsupportedOperationException this is an optional operation, and will throw an unsupported operation exception if
     *             not implemented.
     */
    protected RESPONSE_TYPE executeQuery(Map<String, String> pParameters) throws QueryException
    {
        return executeQuery(
                new WpsServerConnectionHelper(WpsUrlHelper.buildGetCapabilitiesUrl(getServer().getWpsUrl()), getToolbox()),
                pParameters);
    }

    /**
     * Performs a WPS request, for the server defined in the {@link #getServer()} field.
     *
     * @param pParameters the set of parameters with which to execute the query (not all queries require parameters, as such, this
     *            parameter is optional).
     * @return an InputStream returned from the remote server.
     * @throws QueryException if the remote server cannot be queried.
     */
    protected InputStream executeStreamQuery(Map<String, String> pParameters) throws QueryException
    {
        return executeStreamQuery(
                new WpsServerConnectionHelper(WpsUrlHelper.buildGetCapabilitiesUrl(getServer().getWpsUrl()), getToolbox()),
                pParameters);
    }

    /**
     * Performs a WPS request, for the server defined in the {@link #getServer()} field. This is an optional operation, and will
     * throw an unsupported operation exception if not implemented.
     *
     * @param pHelper The connection helper with which to make the request.
     * @param pParameters the set of parameters with which to execute the query (not all queries require parameters, as such, this
     *            parameter is optional).
     * @return a WPS response object returned from the remote server.
     * @throws QueryException if the remote server cannot be queried.
     * @throws UnsupportedOperationException this is an optional operation, and will throw an unsupported operation exception if
     *             not implemented.
     */
    protected abstract RESPONSE_TYPE executeQuery(WpsServerConnectionHelper pHelper, Map<String, String> pParameters)
        throws QueryException;

    /**
     * Performs a WPS request, for the server defined in the {@link #getServer()} field.
     *
     * @param pHelper The connection helper with which to make the request.
     * @param pParameters the set of parameters with which to execute the query (not all queries require parameters, as such, this
     *            parameter is optional).
     * @return an InputStream returned from the remote server.
     * @throws QueryException if the remote server cannot be queried.
     */
    protected abstract InputStream executeStreamQuery(WpsServerConnectionHelper pHelper, Map<String, String> pParameters)
        throws QueryException;

    /**
     * Gets the unique identifier assigned to the server.
     *
     * @return The unique identifier assigned to the server.
     */
    public String getServerId()
    {
        return this.getServer().getServerId(OGCServerSource.WPS_SERVICE);
    }

    /**
     * Gets the title assigned to the server.
     *
     * @return The title assigned to the server.
     */
    public String getServerName()
    {
        return this.getServer().getServerTitle();
    }

    /**
     * Creates a parameter map unique to the implemented request type.
     *
     * @return a Map in which the configured parameters are defined (may be null if none are needed).
     */
    protected abstract Map<String, String> getParameterMap();

    /**
     * Gets the value of the {@link #myConsumerManager} field.
     *
     * @return the value stored in the {@link #myConsumerManager} field.
     */
    public FeatureConsumerManager getConsumerManager()
    {
        return myConsumerManager;
    }
}
