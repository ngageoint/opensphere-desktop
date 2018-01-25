package io.opensphere.wms.state.activate.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.Plugin;
import io.opensphere.core.api.Envoy;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.registry.GenericRegistry;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wms.envoy.WMSEnvoy;
import io.opensphere.wms.envoy.WMSLayerKey;
import io.opensphere.wms.layer.WMSDataType;
import io.opensphere.wms.state.model.WMSEnvoyAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Associates WMS envoy's to their respective layer states.
 *
 */
public class EnvoyCoupler
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(EnvoyCoupler.class);

    /**
     * The wms plugin used to help get the wms envoys.
     */
    private final Plugin myWmsPlugin;

    /**
     * The system envoy registry.
     */
    private final GenericRegistry<Envoy> myEnvoyRegistry;

    /**
     * The DataGroupController used to get existing layers/data types to clone
     * for a state.
     */
    private final DataGroupController myDataController;

    /**
     * Constructs a new envoy coupler.
     *
     * @param wmsPlugin The wms plugin used to help get the wms envoys.
     * @param envoyRegistry The envoy registry.
     * @param dataController Used to get data types to clone for saved states.
     */
    public EnvoyCoupler(Plugin wmsPlugin, GenericRegistry<Envoy> envoyRegistry, DataGroupController dataController)
    {
        myWmsPlugin = wmsPlugin;
        myEnvoyRegistry = envoyRegistry;
        myDataController = dataController;
    }

    /**
     * Retrieves and couples the envoys that are related to the states.
     *
     * @param states The states to retrieve envoys for.
     * @return The list of couples envoys and states.
     */
    public List<WMSEnvoyAndState> retrieveRelatedEnvoys(Collection<? extends WMSLayerState> states)
    {
        List<WMSEnvoyAndState> envoyAndStates = New.list();

        Map<String, WMSEnvoy> serversAndEnvoys = mapServersAndEnvoys();
        Set<String> retrievedTypes = New.set();
        Set<String> notLoadedServers = New.set();

        for (WMSLayerState state : states)
        {
            WMSEnvoy envoy = serversAndEnvoys.get(state.getUrl());

            if (envoy != null)
            {
                String[] layerNames = state.getParameters().getLayerName().split(",");
                for (String layerName : layerNames)
                {
                    String key = WMSLayerKey.createKey(
                            envoy.getServerConnectionConfig().getServerId(OGCServerSource.WMS_GETMAP_SERVICE), layerName);

                    if (!retrievedTypes.contains(key))
                    {
                        DataTypeInfo existingType = myDataController.findMemberById(key);

                        if (existingType instanceof WMSDataType)
                        {
                            retrievedTypes.add(key);
                            WMSDataType existingWmsType = (WMSDataType)existingType;
                            WMSEnvoyAndState envoyAndState = new WMSEnvoyAndState(envoy, existingWmsType, state, layerName);
                            envoyAndStates.add(envoyAndState);
                        }
                        else
                        {
                            String message = "Unable to restore WMS layer " + state.getId() + " layer does not exist on server "
                                    + state.getUrl();
                            LOGGER.warn(message);
                            Notify.warn(message);
                        }
                    }
                }
            }
            else
            {
                notLoadedServers.add(state.getUrl());
            }
        }

        for (String notLoadedServer : notLoadedServers)
        {
            LOGGER.warn("Server not loaded unable to load any WMS layers for " + notLoadedServer);
        }

        return envoyAndStates;
    }

    /**
     * Maps the server urls to the get capabilites envoys.
     *
     * @return The map of server url's to their respective get capabilities
     *         envoy.
     */
    private Map<String, WMSEnvoy> mapServersAndEnvoys()
    {
        Map<String, WMSEnvoy> serversAndEnvoys = New.map();

        Collection<Envoy> envoys = myEnvoyRegistry.getObjectsForSource(myWmsPlugin);
        for (Envoy envoy : envoys)
        {
            if (envoy instanceof WMSEnvoy)
            {
                WMSEnvoy wmsEnvoy = (WMSEnvoy)envoy;
                ServerConnectionParams serverConfig = ((WMSEnvoy)envoy).getServerConnectionConfig();
                serversAndEnvoys.put(serverConfig.getWmsUrl(), wmsEnvoy);
            }
        }

        return serversAndEnvoys;
    }
}
