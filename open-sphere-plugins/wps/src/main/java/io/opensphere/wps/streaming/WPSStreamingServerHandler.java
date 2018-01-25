package io.opensphere.wps.streaming;

import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.server.ModifiableServerProvider;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.server.StreamingServer;
import io.opensphere.core.util.collections.New;
import net.opengis.wps._100.ProcessBriefType;
import net.opengis.wps._100.ProcessOfferings;
import net.opengis.wps._100.WPSCapabilitiesType;

/**
 * Manages the creation and destruction of a {@link WPSStreamingServer} based on if the server has streaming capabilities.
 */
public class WPSStreamingServerHandler
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(WPSStreamingServerHandler.class);

    /**
     * The system server provider registry.
     */
    private final ServerProviderRegistry myProviderRegistry;

    /**
     * The streaming server or null if the server does not have streaming capability.
     */
    private WPSStreamingServer myServer;

    /**
     * Constructs a new streaming server handler.
     *
     * @param capabilities The wps capabilities of the server.
     * @param providerRegistry The server provider registry to add the streaming server to.
     * @param factory Used to build the necessary components used to stream NRT data.
     */
    public WPSStreamingServerHandler(WPSCapabilitiesType capabilities, ServerProviderRegistry providerRegistry,
            ComponentsFactory factory)
    {
        myProviderRegistry = providerRegistry;
        createStreamingServer(capabilities, factory);
    }

    /**
     * Removes the streaming server from the registry.
     */
    public void close()
    {
        if (myServer != null)
        {
            ServerProvider<StreamingServer> provider = myProviderRegistry.getProvider(StreamingServer.class);

            if (provider instanceof ModifiableServerProvider)
            {
                ((ModifiableServerProvider<StreamingServer>)provider).removeServer(myServer);
            }
        }
    }

    /**
     * Creates the streaming server if the server has streaming capabilities.
     *
     * @param capabilities The wps capabilities of the server.
     * @param factory Used to build the necessary components used to stream NRT data.
     */
    private void createStreamingServer(WPSCapabilitiesType capabilities, ComponentsFactory factory)
    {
        ProcessOfferings offerings = capabilities.getProcessOfferings();

        if (offerings != null)
        {
            Set<String> processNames = New.set();
            for (ProcessBriefType briefType : offerings.getProcess())
            {
                processNames.add(briefType.getIdentifier().getValue());
            }

            boolean serverHasStreaming = processNames.contains(StreamingConstants.SUBSCRIBE_PROCESS)
                    && processNames.contains(StreamingConstants.UNSUBSCRIBE_PROCESS);

            if (serverHasStreaming)
            {
                myServer = new WPSStreamingServer(factory);
                ServerProvider<StreamingServer> provider = myProviderRegistry.getProvider(StreamingServer.class);

                if (provider instanceof ModifiableServerProvider)
                {
                    ((ModifiableServerProvider<StreamingServer>)provider).addServer(myServer);
                }
                else
                {
                    LOGGER.warn("Could not find a streaming server provider.");
                }
            }
        }
    }
}
