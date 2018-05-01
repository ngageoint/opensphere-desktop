package io.opensphere.wps;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerToolboxUtils;
import io.opensphere.server.toolbox.ServerValidatorRegistry;
import io.opensphere.wps.envoy.WpsDescribeProcessTypeEnvoy;
import io.opensphere.wps.envoy.WpsEnvoySuite;
import io.opensphere.wps.envoy.WpsGetCapabilitiesEnvoy;
import io.opensphere.wps.response.WPSGmlResponseHandler;
import io.opensphere.wps.response.WpsToolbox;

/**
 * The plugin adapter used to enable the WPS processes.
 */
public class WpsPlugin extends PluginAdapter
{
    /**
     * The toolbox used to interact with the rest of the application.
     */
    private Toolbox myToolbox;

    /** The Mantle controller. */
    private WpsMantleController myMantleController;

    /**
     * The event listener with which the plugin listens for WPS server events.
     */
    private EventListener<ServerConfigEvent> myWpsServerEventListener;

    /** Envoys that request WFS features from the server. */
    private final Collection<WpsEnvoySuite> myEnvoys = New.list();

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#initialize(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    public void initialize(PluginLoaderData pData, Toolbox pToolbox)
    {
        myToolbox = pToolbox;

        WpsToolbox wpsToolbox = new WpsToolbox();
        wpsToolbox.setGmlResponseHandlerCreator(WPSGmlResponseHandler::new);
        pToolbox.getPluginToolboxRegistry().registerPluginToolbox(wpsToolbox);

        Preferences preferences = pToolbox.getPreferencesRegistry().getPreferences(WpsPlugin.class);
        myMantleController = new WpsMantleController(pToolbox, preferences);
        registerValidator(pToolbox);
        createEventListeners(myToolbox.getEventManager());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#close()
     */
    @Override
    public void close()
    {
        myMantleController.close();
    }

    /**
     * Create the event listeners for this class.
     *
     * @param eventManager The Event Manager to subscribe with to receive Events
     */
    private void createEventListeners(EventManager eventManager)
    {
        if (eventManager != null)
        {
            myWpsServerEventListener = new EventListener<ServerConfigEvent>()
            {
                @Override
                public void notify(ServerConfigEvent event)
                {
                    String serverId = event.getServer() == null ? null
                            : event.getServer().getServerId(OGCServerSource.WPS_SERVICE);
                    if (StringUtils.isNotEmpty(serverId))
                    {
                        switch (event.getEventAction())
                        {
                            case ACTIVATE:
                                addServer(serverId, event.getServer());
                                break;
                            case DEACTIVATE:
                                removeServer(serverId);
                                break;
                            default:
                                break;
                        }
                    }
                }
            };
            eventManager.subscribe(ServerConfigEvent.class, myWpsServerEventListener);
        }
    }

    /**
     * Removes the existing server from the set of known WPS servers.
     *
     * @param pServerId the identifier of the server to remove.
     */
    protected void removeServer(String pServerId)
    {
        WpsEnvoySuite envoySuite = findEnvoySuite(pServerId);
        if (envoySuite != null)
        {
            myToolbox.getEnvoyRegistry().removeObjectsForSource(this,
                    Collections.singleton(envoySuite.getGetCapabilitiesEnvoy()));
            myToolbox.getEnvoyRegistry().removeObjectsForSource(this,
                    Collections.singleton(envoySuite.getDescribeProcessTypeEnvoy()));

            myEnvoys.remove(envoySuite);
        }
        myMantleController.serverRemoved(pServerId);
    }

    /**
     * Adds a new WPS Server based upon the supplied parameters. When the server
     * is added, a request is made for its capabilities.
     *
     * @param pServerId the unique ID of the server to add.
     * @param pServer the connection parameters of the server to add.
     */
    protected void addServer(String pServerId, ServerConnectionParams pServer)
    {
        myMantleController.addServerDetails(pServerId, pServer);
        WpsEnvoySuite envoySuite = findEnvoySuite(pServerId);
        if (envoySuite == null)
        {
            WpsDescribeProcessTypeEnvoy describeProcessEnvoy = new WpsDescribeProcessTypeEnvoy(myToolbox, pServer);

            WpsGetCapabilitiesEnvoy getCapabilitiesEnvoy = new WpsGetCapabilitiesEnvoy(myToolbox, pServer,
                    myMantleController.getDataGroup(pServerId, pServer.getServerTitle()));

            envoySuite = new WpsEnvoySuite(pServerId, getCapabilitiesEnvoy, describeProcessEnvoy);
            myEnvoys.add(envoySuite);
            myToolbox.getEnvoyRegistry().addObjectsForSource(this, New.list(getCapabilitiesEnvoy, describeProcessEnvoy));
        }
    }

    /**
     * Find an existing WPS envoy suite in the internal cache, based on the
     * supplied server identifier.
     *
     * @param pServerId The server identifier used to match an available envoy.
     * @return The local envoy whose URL matches the server identifier or null
     *         if none is found.
     */
    protected WpsEnvoySuite findEnvoySuite(String pServerId)
    {
        for (WpsEnvoySuite envoySuite : myEnvoys)
        {
            if (StringUtils.equals(pServerId, envoySuite.getServerId()))
            {
                return envoySuite;
            }
        }

        return null;
    }

    /**
     * Register a validator with the Server plugin to allow WMS validation.
     *
     * @param pToolbox the toolbox from which application configuration is read.
     */
    protected void registerValidator(Toolbox pToolbox)
    {
        ServerValidatorRegistry validatorRegistry = ServerToolboxUtils.getServerValidatorRegistry(pToolbox);
        if (validatorRegistry != null)
        {
            validatorRegistry.register(new WpsServerValidator(pToolbox));
        }
    }
}
