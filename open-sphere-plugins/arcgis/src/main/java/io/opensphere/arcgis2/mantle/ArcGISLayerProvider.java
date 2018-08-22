package io.opensphere.arcgis2.mantle;

import java.awt.Color;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.arcgis2.envoy.ArcGISDescribeLayerEnvoy;
import io.opensphere.arcgis2.esri.EsriFullLayer;
import io.opensphere.arcgis2.model.ArcGISDataGroupInfo;
import io.opensphere.arcgis2.model.ArcGISLayer;
import io.opensphere.arcgis2.util.Constants;
import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultBasicVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapFeatureVisualizationInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.xyztile.model.Projection;
import io.opensphere.xyztile.model.XYZServerInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * Creates and adds to the system the necessary {@link DataGroupInfo}s and
 * {@link DataTypeInfo}s so the user can see the different layers an ArcGIS
 * server provides.
 */
public class ArcGISLayerProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISLayerProvider.class);

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /** The layer info provider. */
    private final ArcGISLayerInfoProvider myLayerInfoProvider;

    /** The request executor. */
    private final ExecutorService myExecutor = ThreadUtilities
            .newTerminatingFixedThreadPool(new NamedThreadFactory("ArcGISLayerProvider"), 4);

    /**
     * Constructs a new layer provider.
     *
     * @param toolbox The system toolbox.
     */
    public ArcGISLayerProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myLayerInfoProvider = new ArcGISLayerInfoProvider(toolbox);
    }

    /**
     * Adds the layers to the system for the specified server.
     *
     * @param serverName The user provided name for the server.
     * @param sourceURL The server's url.
     * @param mantleController The object that will add the server group to
     *            mantle.
     * @return The server group.
     * @throws QueryException If something went wrong getting the layer
     *             information from the server.
     */
    public DataGroupInfo serverAdded(String serverName, String sourceURL, MantleController mantleController) throws QueryException
    {
        DefaultDataGroupInfo serverGroup = null;
        DataModelCategory dataModelCategory = new DataModelCategory(Nulls.STRING, ArcGISLayer.class.getName(), sourceURL);
        SimpleQuery<ArcGISLayer> query = new SimpleQuery<>(dataModelCategory, ArcGISLayer.LAYER_PROPERTY);
        QueryTracker tracker = myToolbox.getDataRegistry().performQuery(query);
        if (tracker.getQueryStatus() == QueryTracker.QueryStatus.SUCCESS)
        {
            serverGroup = new ArcGISDataGroupInfo(myToolbox, serverName, sourceURL, true);

            Map<String, DefaultDataGroupInfo> groupMap = New.map();

            // Run the query again to get the layers in the right order.
            query.clearResults();
            long[] ids = tracker.getIds();
            myToolbox.getDataRegistry().performLocalQuery(ids, query);
            List<ArcGISLayer> results = query.getResults();
            List<Pair<ArcGISLayer, DataGroupInfo>> layersAndParents = New.list();
            for (int index = 0; index < results.size(); index++)
            {
                ArcGISLayer layer = results.get(index);
                DataGroupInfo layerGroup = createDataGroups(sourceURL, serverGroup, groupMap, layer, ids[index]);
                layersAndParents.add(new Pair<>(layer, layerGroup));
            }

            Map<String, List<Pair<ArcGISLayer, DataGroupInfo>>> serviceToLayerMap = CollectionUtilities
                    .partition(layersAndParents, v -> getService(v.getFirstObject().getURL()));
            List<Pair<ArcGISLayer, DataGroupInfo>> mapLayers = serviceToLayerMap.getOrDefault("MapServer",
                    Collections.emptyList());
            List<Pair<ArcGISLayer, DataGroupInfo>> featureLayers = serviceToLayerMap.getOrDefault("FeatureServer",
                    Collections.emptyList());

            // Feature data types must be created before adding the data groups
            // to the controller.
            createFeatureTypes(mapLayers);
            createFeatureTypes(featureLayers);

            mantleController.addServerGroup(serverName, sourceURL, serverGroup);

            // Tile data types must be added after the datagroups are already
            // added to the system, so that the xyz tile plugin can find the
            // datagroups.
            if (CollectionUtilities.hasContent(mapLayers))
            {
                createXYZTileTypes(mapLayers, serverName, sourceURL);
            }
        }
        else if (tracker.getQueryStatus() == QueryTracker.QueryStatus.CANCELLED)
        {
            LOGGER.info("The query for " + serverName + " was canceled");
        }
        else
        {
            throw new QueryException(tracker.getException());
        }

        return serverGroup;
    }

    /**
     * Removes the layers from the system.
     *
     * @param serverUrl The url of the server that was removed.
     */
    public void serverRemoved(String serverUrl)
    {
        DataModelCategory layerCategory = XYZTileUtils.newLayersCategory(serverUrl, Constants.PROVIDER);
        myToolbox.getDataRegistry().removeModels(layerCategory, false);
    }

    /**
     * Creates a data groups and its hierarchy (if necessary) and adds them to
     * the server group and the provided group map.
     *
     * @param sourceURL The source url.
     * @param serverGroup The server group.
     * @param groupMap The group map.
     * @param layer The layer.
     * @param id The data registry id.
     *
     * @return The id of the {@link DataGroupInfo} representing the passed in
     *         layer.
     */
    private DataGroupInfo createDataGroups(String sourceURL, DefaultDataGroupInfo serverGroup,
            Map<String, DefaultDataGroupInfo> groupMap, ArcGISLayer layer, long id)
    {
        // Create the parents.
        DefaultDataGroupInfo parent = serverGroup;
        String groupId = sourceURL;
        for (String name : layer.getPath())
        {
            groupId = UrlUtilities.concatUrlFragments(groupId, StringUtils.endsWith(groupId, name) ? "" : name);

            DefaultDataGroupInfo group = groupMap.get(name);
            if (group == null)
            {
                group = new DefaultDataGroupInfo(false, myToolbox, Constants.PROVIDER, groupId, name);
                groupMap.put(name, group);
                parent.addChild(group, this);
            }
            parent = group;
        }

        // Create and add the layer group if necessary
        DataGroupInfo layerGroup = parent.getChildren().stream().filter(g -> g.getDisplayName().equals(layer.getLayerName()))
                .findAny().orElse(null);
        if (layerGroup == null)
        {
            groupId = UrlUtilities.concatUrlFragments(groupId, layer.getId());
            layerGroup = new ArcGISDataGroupInfo(myToolbox, layer, groupId, id);
            parent.addChild(layerGroup, this);
        }

        return layerGroup;
    }

    /**
     * Creates the xyz tile layers.
     *
     * @param layersAndParents The ArcGIS layers and parents.
     * @param serverName The user designated name of the server.
     * @param serverUrl The server's base url.
     */
    private void createXYZTileTypes(Collection<Pair<ArcGISLayer, DataGroupInfo>> layersAndParents, String serverName,
            String serverUrl)
    {
        List<XYZTileLayerInfo> xyzLayers = New.list();
        XYZServerInfo serverInfo = new XYZServerInfo(serverName, serverUrl);
        for (Pair<ArcGISLayer, DataGroupInfo> layerAndParent : layersAndParents)
        {
            ArcGISLayer layer = layerAndParent.getFirstObject();
            DataGroupInfo layerGroup = layerAndParent.getSecondObject();

            int topLevelTiles = 1;
            Projection projection = Projection.EPSG_3857;
            int minLevel = 5;

            if (layer.getSpatialReference() == 4326 || !layer.isSingleFusedMapCache())
            {
                minLevel = 2;
                topLevelTiles = 2;
                projection = Projection.EPSG_4326;
            }

            XYZTileLayerInfo xyzLayer = new XYZTileLayerInfo(layer.getFullURL(), layer.getLayerName(), projection, topLevelTiles,
                    false, minLevel, serverInfo);
            xyzLayer.setDescription(layer.getDescription());
            xyzLayer.setMaxLevels(layer.getMaxLevels());
            xyzLayer.setParentId(layerGroup.getId());
            xyzLayers.add(xyzLayer);
        }

        DataModelCategory layerCategory = XYZTileUtils.newLayersCategory(serverInfo.getServerUrl(), Constants.PROVIDER);
        SimpleSessionOnlyCacheDeposit<XYZTileLayerInfo> deposit = new SimpleSessionOnlyCacheDeposit<>(layerCategory,
                XYZTileUtils.LAYERS_DESCRIPTOR, xyzLayers);
        myToolbox.getDataRegistry().addModels(deposit);
    }

    /**
     * Creates the feature layers.
     *
     * @param layersAndParents The ArcGIS layers and parents.
     */
    private void createFeatureTypes(Collection<Pair<ArcGISLayer, DataGroupInfo>> layersAndParents)
    {
        List<FeatureCreator> tasks = layersAndParents.stream().filter(lp -> !lp.getFirstObject().isSingleFusedMapCache())
                .map(lp -> new FeatureCreator(lp.getFirstObject(), lp.getSecondObject())).collect(Collectors.toList());
        try
        {
            myExecutor.invokeAll(tasks);
        }
        catch (InterruptedException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Gets the service from the URL.
     *
     * @param url the URL
     * @return the service
     */
    private static String getService(URL url)
    {
        String s = url.toString();
        return s.substring(s.lastIndexOf('/') + 1);
    }

    /**
     * Creates and adds a feature layer.
     */
    private class FeatureCreator implements Callable<Void>
    {
        /** The layer object. */
        private final ArcGISLayer myLayer;

        /** The group to add the layer to. */
        private final DataGroupInfo myLayerGroup;

        /**
         * Constructor.
         *
         * @param layer the layer object
         * @param layerGroup the group to add the layer to
         */
        public FeatureCreator(ArcGISLayer layer, DataGroupInfo layerGroup)
        {
            myLayer = layer;
            myLayerGroup = layerGroup;
        }

        @Override
        public Void call()
        {
            try
            {
                EsriFullLayer fullLayer = ArcGISDescribeLayerEnvoy.query(myToolbox.getDataRegistry(), myLayer.getFullURL());
                if (fullLayer.getCapabilities().contains("Query"))
                {
                    DefaultDataTypeInfo dataType = createFeatureType(myLayer);
                    myLayerInfoProvider.updateDataType(dataType, fullLayer);
                    myLayerGroup.addMember(dataType, this);
                }
            }
            catch (QueryException e)
            {
                LOGGER.error(e.getMessage());
            }
            return null;
        }

        /**
         * Creates the feature layer.
         *
         * @param layer The ArcGIS layer.
         * @return the data type
         */
        private DefaultDataTypeInfo createFeatureType(ArcGISLayer layer)
        {
            String layerUrl = layer.getFullURL();

            // Make the type key different than the tile layer's type key
            String typeKey = layerUrl.replace("MapServer", "FeatureServer");

            DefaultDataTypeInfo dataType = new DefaultDataTypeInfo(myToolbox, Constants.PROVIDER, typeKey, Constants.PROVIDER,
                    layer.getLayerName(), true);
//            dataType.setTimeExtents(new DefaultTimeExtents(), this);
            dataType.setUrl(layerUrl);
            dataType.setDescription(layer.getDescription());
            dataType.setBasicVisualizationInfo(new DefaultBasicVisualizationInfo(LoadsTo.STATIC,
                    DefaultBasicVisualizationInfo.LOADS_TO_STATIC_ONLY, Color.PINK, true));
            dataType.setMapVisualizationInfo(new DefaultMapFeatureVisualizationInfo(MapVisualizationType.POINT_ELEMENTS));
            dataType.setOrderKey(new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_FEATURE_LAYER_FAMILY,
                    DefaultOrderCategory.FEATURE_CATEGORY, dataType.getTypeKey()));
            dataType.setQueryable(true);

            DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
            metaData.setSpecialKeyDetector(MantleToolboxUtils.getMantleToolbox(myToolbox).getColumnTypeDetector());
            dataType.setMetaDataInfo(metaData);

            OrderManager manager = myToolbox.getOrderManagerRegistry().getOrderManager(dataType.getOrderKey());
            int zorder = manager.activateParticipant(dataType.getOrderKey());
            dataType.getMapVisualizationInfo().setZOrder(zorder, null);

            return dataType;
        }
    }
}
