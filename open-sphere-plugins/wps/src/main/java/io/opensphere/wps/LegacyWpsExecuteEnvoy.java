package io.opensphere.wps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.event.Event.State;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.server.services.OGCServiceStateEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wps.response.WPSProcessResult;
import io.opensphere.wps.streaming.WPSStreamingServerHandler;
import io.opensphere.wps.streaming.factory.ComponentsFactoryImpl;
import io.opensphere.wps.util.ServerException;
import io.opensphere.wps.util.WpsServerConnectionHelper;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * The Class WPSEnvoy.
 */
@SuppressWarnings("PMD.GodClass")
@ThreadSafe
public class LegacyWpsExecuteEnvoy extends AbstractEnvoy
{
    /** Logging reference. */
    private static final Logger LOGGER = Logger.getLogger(LegacyWpsExecuteEnvoy.class);

    /** The active features. */
    @GuardedBy("this")
    private final Map<String, DefaultDataGroupInfo> myActiveFeatures;

    /** The map to keep track of data type info. */
    @GuardedBy("this")
    private final Map<String, DataTypeInfo> myDataTypes;

    /** The WPS group. */
    private final DataGroupInfo myMasterGroup;

    /** My server config. */
    private final ServerConnectionParams myServerConfig;

    /**
     * Handles setting up necessary NRT streaming component if the WPS server is streaming capable.
     */
    @GuardedBy("this")
    private WPSStreamingServerHandler myStreamServerHandler;

    /**
     * The capabilities describing the server to which the connection is made.
     */
    private final WPSCapabilitiesType myCapabilities;

    /**
     * Instantiates a new WPS envoy.
     *
     * @param toolbox The toolbox.
     * @param server The server.
     * @param masterGroup the master group
     * @param pCapabilities The capabilities describing the server to which the connection is made.
     */
    public LegacyWpsExecuteEnvoy(Toolbox toolbox, ServerConnectionParams server, DataGroupInfo masterGroup,
            WPSCapabilitiesType pCapabilities)
    {
        super(toolbox);
        myServerConfig = server;
        myCapabilities = pCapabilities;
        server.getServerTitle();
        server.getServerId(OGCServerSource.WPS_SERVICE);
        myActiveFeatures = New.map();
        myDataTypes = New.map();
        myMasterGroup = masterGroup;
    }

    /**
     * Deactivate the envoy.
     */
    public synchronized void deactivate()
    {
        if (myStreamServerHandler != null)
        {
            myStreamServerHandler.close();
            myStreamServerHandler = null;
        }
    }

    /**
     * Gets the server config.
     *
     * @return the server config
     */
    public synchronized ServerConnectionParams getServerConfig()
    {
        return myServerConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.AbstractEnvoy#open()
     */
    @Override
    public synchronized void open()
    {
        String connError = null;
        try
        {
            ComponentsFactoryImpl factory = new ComponentsFactoryImpl(new URL(myServerConfig.getWpsUrl()),
                    getToolbox().getServerProviderRegistry(), new WPSRequestExecuterImpl(this));
            myStreamServerHandler = new WPSStreamingServerHandler(myCapabilities, getToolbox().getServerProviderRegistry(),
                    factory);
        }
        catch (IOException e)
        {
            connError = "Error connecting to WPS Server: " + myServerConfig.getWpsUrl();
        }
        catch (RuntimeException e)
        {
            LOGGER.error(e, e);
        }

        if (myServerConfig != null)
        {
            fireStateEvent(myServerConfig.getWpsUrl(), myCapabilities != null, connError, getToolbox().getEventManager());
        }
    }

    /**
     * Make an Execute request to the server using the existing connection parameters.
     *
     * @param url The execute url.
     * @param httpResponse The optional http server response.
     * @return The response from the server.
     */
    public synchronized InputStream requestExecuteFromServer(URL url, ResponseValues httpResponse)
    {
        return requestExecuteFromServer(myServerConfig, url, httpResponse);
    }

    /**
     * Unload and remove the WPS result.
     *
     * @param result The WPS result.
     * @return The data type info.
     */
    public synchronized DataTypeInfo unloadSource(WPSProcessResult result)
    {
        DataTypeInfo dti = null;
        if (result != null)
        {
            MantleToolbox mantleTb = getToolbox().getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);

            if (myActiveFeatures.containsKey(result.getName()))
            {
                DataGroupInfo dgi = myActiveFeatures.remove(result.getName());
                dgi.activationProperty().setActive(false);
                myMasterGroup.removeChild(dgi, this);
            }

            String wps = "WPS";
            StringBuilder displayName = new StringBuilder(wps);
            displayName.append('/').append(result.getName());

            dti = myDataTypes.get(displayName.toString());
            if (dti != null)
            {
                mantleTb.getDataTypeController().removeDataType(dti, this);
                myDataTypes.remove(displayName.toString());
            }
        }
        return dti;
    }

    /**
     * Fire state event.
     *
     * @param wpsUrl the WPS URL
     * @param isValid the valid flag
     * @param error String detailing any errors that occurred.
     * @param eventMgr the event manager used to send the new state event
     */
    protected void fireStateEvent(String wpsUrl, boolean isValid, String error, EventManager eventMgr)
    {
        if (eventMgr != null)
        {
            OGCServiceStateEvent stateEvent = new OGCServiceStateEvent(wpsUrl, null, OGCServerSource.WPS_SERVICE, isValid);
            if (error != null)
            {
                stateEvent.setError(error);
                // Note: EventManager.setEventState publishes the event
                eventMgr.setEventState(stateEvent, State.FAILED);
                LOGGER.warn(error);
            }
            else
            {
                // Note: EventManager.setEventState publishes the event
                eventMgr.setEventState(stateEvent, State.COMPLETED);
            }
        }
    }

    /**
     * Make an Execute request to the server.
     *
     * @param serverConf The server connection parameters.
     * @param url The execute url.
     * @param httpResponse The optional http server response.
     * @return The response from the server.
     */
    protected InputStream requestExecuteFromServer(ServerConnectionParams serverConf, URL url, ResponseValues httpResponse)
    {
        InputStream inputStream = null;
        WpsServerConnectionHelper wpsConnection = new WpsServerConnectionHelper(url, getToolbox());

        try
        {
            if (httpResponse != null)
            {
                inputStream = wpsConnection.requestStream(httpResponse);
            }
            else
            {
                inputStream = wpsConnection.requestStream();
            }
        }
        catch (ServerException e)
        {
            LOGGER.error("Error while executing WPS request" + e.getMessage());
        }

        return inputStream;
    }
}
