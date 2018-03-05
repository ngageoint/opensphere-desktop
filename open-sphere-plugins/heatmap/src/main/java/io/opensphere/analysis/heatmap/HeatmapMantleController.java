package io.opensphere.analysis.heatmap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;

import io.opensphere.analysis.util.MutableInteger;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.DataRemovalEvent;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.crust.AbstractMantleController;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.cache.CacheDataTypeQuery;
import io.opensphere.mantle.data.cache.CacheEntryView;
import io.opensphere.mantle.data.cache.QueryAccessConstraint;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultMapTileVisualizationInfo;
import io.opensphere.mantle.data.tile.InterpolatedTileVisualizationSupport;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Heat map mantle controller. */
public class HeatmapMantleController extends AbstractMantleController
{
    /** Layer to count map. */
    @GuardedBy("myLayerCountMap")
    private final Map<String, MutableInteger> myLayerCountMap = New.map();

    /** The group activation listener. */
    private volatile ActivationListener myActivationListener;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public HeatmapMantleController(Toolbox toolbox)
    {
        super(toolbox, "Heatmap");
        bindEvent(DataRemovalEvent.class, e -> deactivateAllGroups());
    }

    @Override
    public void close()
    {
        // Deactivate groups to perform clean up of the preferences
        deactivateAllGroups();
        super.close();
    }

    /**
     * Adds a heat map layer.
     *
     * @param layerName the layer name
     * @return the heat map data type
     */
    public DataTypeInfo addLayer(String layerName)
    {
        int count;
        synchronized (myLayerCountMap)
        {
            MutableInteger counter = myLayerCountMap.computeIfAbsent(layerName, k -> new MutableInteger());
            counter.increment();
            count = counter.get();
        }

        StringBuilder keyBuilder = new StringBuilder(layerName.replace(' ', '_')).append("!!heatmap");
        StringBuilder layerBuilder = new StringBuilder(layerName);
        if (count > 1)
        {
            keyBuilder.append(count);
            layerBuilder.append(' ').append(count);
        }
        String typeKey = keyBuilder.toString();
        String layerNameWithCount = layerBuilder.toString();

        DataGroupInfo group = add1stLevelLayer(layerNameWithCount, layerNameWithCount, typeKey,
                DefaultOrderCategory.IMAGE_DATA_CATEGORY, null, this::removeLayer);
        group.activationProperty().setActive(true);
        DefaultDataTypeInfo dataType = (DefaultDataTypeInfo)group.getMembers(false).iterator().next();

        TileRenderProperties properties = new DefaultTileRenderProperties(0, true, false);
        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(getToolbox());
        float opacity = mantleToolbox.getDataTypeInfoPreferenceAssistant().getOpacityPreference(typeKey,
                ColorUtilities.COLOR_COMPONENT_MAX_VALUE) / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE;
        properties.setOpacity(opacity);

        DataGroupController dataGroupController = mantleToolbox.getDataGroupController();
        dataType.registerInUse(dataGroupController, false);

        VisualizationStyleRegistry registry = mantleToolbox.getVisualizationStyleRegistry();
        HeatmapVisualizationStyle style = (HeatmapVisualizationStyle)registry.getStyle(InterpolatedTileVisualizationSupport.class,
                typeKey, true);

        if (style == null)
        {
            style = new HeatmapVisualizationStyle(getToolbox(), typeKey);
            style.initialize();
            registry.setStyle(InterpolatedTileVisualizationSupport.class, typeKey, style, this);
        }

        DefaultMapTileVisualizationInfo visualizationInfo = new DefaultMapTileVisualizationInfo(
                MapVisualizationType.INTERPOLATED_IMAGE_TILES, properties, true);
        dataType.setMapVisualizationInfo(visualizationInfo);

        return dataType;
    }

    /**
     * Gets the layer name for the given base name.
     *
     * @param baseName the base name
     * @return the layer name
     */
    public String getLayerName(String baseName)
    {
        return baseName + " Heatmap";
    }

    /**
     * Sets the activation listener.
     *
     * @param activationListener the activation listener
     */
    public void setActivationListener(ActivationListener activationListener)
    {
        myActivationListener = activationListener;
    }

    /**
     * Queries for geometries.
     *
     * @param dataTypes the optional data types
     * @param queryGeom the optional query box
     * @return the results
     */
    public List<GeometryInfo> getGeometries(Collection<? extends DataTypeInfo> dataTypes, Geometry queryGeom)
    {
        Set<String> featureTypeKeys = getTypeKeys(dataTypes);
        GeographicBoundingBox queryBbox = getBbox(queryGeom);
        TimeSpan timeSpan = getToolbox().getTimeManager().getPrimaryActiveTimeSpans().get(0);
        Projection projection = getToolbox().getMapManager().getProjection();

        HeatmapCacheDataTypeQuery query = new HeatmapCacheDataTypeQuery(featureTypeKeys, queryBbox, timeSpan, projection);
        getMantleToolbox().getDataElementCache().query(query);

        return query.getResults();
    }

    /**
     * Gets the geometries for the IDs.
     *
     * @param ids the IDs
     * @param dataTypeKey the data type key
     * @return the geometries
     */
    public List<GeometryInfo> getGeometries(List<Long> ids, String dataTypeKey)
    {
        List<GeometryInfo> results = New.list(ids.size());

        DataElementLookupUtils dataElementLookupUtils = getMantleToolbox().getDataElementLookupUtils();
        List<MapGeometrySupport> geometries = dataElementLookupUtils.getMapGeometrySupport(ids);
        for (int i = 0; i < ids.size(); i++)
        {
            long id = ids.get(i).longValue();
            MapGeometrySupport geometry = geometries.get(i);
            List<Object> metaData = dataElementLookupUtils.getMetaData(id);
            results.add(new GeometryInfo(geometry, dataTypeKey, metaData, geometry.getTimeSpan()));
        }

        return results;
    }

    @Override
    protected void handleGroupActivation(DataGroupActivationProperty activationProperty, ActivationState state,
            PhasedTaskCanceller canceller)
    {
        ActivationListener listener = myActivationListener;
        if (listener != null)
        {
            listener.commit(activationProperty, state, canceller);
        }
    }

    /**
     * Removes the layer.
     *
     * @param group the group
     */
    private void removeLayer(DataGroupInfo group)
    {
        removeLayer(group.getId());
        // Trigger HeatmapController to remove the image
        group.activationProperty().setActive(false);
    }

    /**
     * Deactivates all heatmap groups.
     */
    private void deactivateAllGroups()
    {
        for (DataGroupInfo group : getRootGroup().getChildren())
        {
            group.activationProperty().setActive(false);
        }
    }

    /**
     * Gets the type keys for the data types. If no data types, then all feature
     * types are returned.
     *
     * @param dataTypes the data types
     * @return the type keys
     */
    private Set<String> getTypeKeys(Collection<? extends DataTypeInfo> dataTypes)
    {
        Set<String> typeKeys;
        if (CollectionUtilities.hasContent(dataTypes))
        {
            typeKeys = dataTypes.stream().map(t -> t.getTypeKey()).collect(Collectors.toSet());
        }
        else
        {
            typeKeys = getMantleToolbox().getDataTypeController().getDataTypeInfo().stream()
                    .filter(HeatmapUtilities::isFeatureLayer).map(t -> t.getTypeKey()).collect(Collectors.toSet());
        }
        return typeKeys;
    }

    /**
     * Gets the bounding box for the geometry.
     *
     * @param geom the geometry
     * @return the bounding box
     */
    private GeographicBoundingBox getBbox(Geometry geom)
    {
        GeographicBoundingBox bbox = null;
        if (geom != null)
        {
            Collection<GeographicPosition> queryPositions = CollectionUtilities
                    .filterDowncast(((PolylineGeometry)geom).getVertices(), GeographicPosition.class);
            bbox = GeographicBoundingBox.getMinimumBoundingBox(queryPositions);
        }
        return bbox;
    }

    /** Mantle geometry query that can query by layer, space, and time. */
    private class HeatmapCacheDataTypeQuery extends CacheDataTypeQuery
    {
        /** The query box. */
        private final GeographicBoundingBox myQueryBbox;

        /** The time span. */
        private final TimeSpan myTimeSpan;

        /** The projection. */
        private final Projection myProjection;

        /** The results. */
        private final List<GeometryInfo> myResults = New.list();

        /**
         * Constructor.
         *
         * @param dataTypeKeys the data type keys
         * @param queryBbox the query box
         * @param timeSpan the time span
         * @param projection the projection
         */
        public HeatmapCacheDataTypeQuery(Set<String> dataTypeKeys, GeographicBoundingBox queryBbox, TimeSpan timeSpan,
                Projection projection)
        {
            super(dataTypeKeys, new QueryAccessConstraint(false, true, false, true, true));
            myQueryBbox = queryBbox;
            myTimeSpan = timeSpan;
            myProjection = projection;
        }

        @Override
        public void process(Long id, CacheEntryView entry)
        {
            MapGeometrySupport mantleGeom = entry.getLoadedElementData().getMapGeometrySupport();
            if (mantleGeom != null && entry.getVisState().isVisible() && spatialMatch(myQueryBbox, mantleGeom, myProjection)
                    && temporalMatch(myTimeSpan, mantleGeom, entry))
            {
                myResults.add(new GeometryInfo(mantleGeom, entry.getDataTypeKey(), entry.getLoadedElementData().getMetaData(),
                        entry.getTime()));
            }
        }

        @Override
        public void finalizeQuery()
        {
        }

        /**
         * Gets the results.
         *
         * @return the results
         */
        public List<GeometryInfo> getResults()
        {
            return myResults;
        }

        /**
         * Determines if there's a temporal match.
         *
         * @param timeSpan the time span
         * @param mantleGeom the geometry
         * @param entry the cache entry
         * @return whether there's a temporal match
         */
        private boolean temporalMatch(TimeSpan timeSpan, MapGeometrySupport mantleGeom, CacheEntryView entry)
        {
            return timeSpan == null || timeSpan.overlaps(mantleGeom.getTimeSpan()) || !getLoadsTo(entry).isTimelineEnabled();
        }

        /**
         * Determines if there's a spatial match.
         *
         * @param queryBbox the query box
         * @param mantleGeom the geometry
         * @param projection the projection
         * @return whether there's a spatial match
         */
        private boolean spatialMatch(GeographicBoundingBox queryBbox, MapGeometrySupport mantleGeom, Projection projection)
        {
            return queryBbox == null || queryBbox.intersects(mantleGeom.getBoundingBox(projection));
        }

        /**
         * Gets the loads to for the entry.
         *
         * @param entry the cache entry
         * @return the loads to
         */
        private LoadsTo getLoadsTo(CacheEntryView entry)
        {
            DataTypeInfo dataType = getMantleToolbox().getDataTypeController().getDataTypeInfoForType(entry.getDataTypeKey());
            return dataType != null ? dataType.getBasicVisualizationInfo().getLoadsTo() : LoadsTo.TIMELINE;
        }
    }
}
