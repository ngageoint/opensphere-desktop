package io.opensphere.wms;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.ImageManager.RequestObserver;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.BoundedTimeConstraint;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.constraint.ViewerPositionConstraint;
import io.opensphere.core.geometry.renderproperties.ParentTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.terrain.util.BilSrtmImageReader;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.lidar.elevation.LidarElevationReader;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;
import io.opensphere.server.customization.DefaultCustomization;
import io.opensphere.wms.WMSActiveTimeMonitor.TimeChangeListener;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.layer.TileImageKey;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayer;
import io.opensphere.wms.layer.WMSLayerEvent;
import io.opensphere.wms.util.WMSQueryTracker;

/**
 * Transforms WMS models into {@link Geometry}s.
 */
@SuppressWarnings("PMD.GodClass")
public class WMSTransformer extends DefaultTransformer implements TimeChangeListener
{
    /** The core event manager. */
    private final EventManager myEventManager;

    /** Pool of references to ImageManagers. */
    private final SharedObjectPool<ImageManager> myImageMgrPool = new SharedObjectPool<>();

    /** My layer event listener. */
    private final EventListener<WMSLayerEvent> myLayerEventListener = new EventListener<WMSLayerEvent>()
    {
        @Override
        public void notify(WMSLayerEvent event)
        {
            WMSLayer layer = myTypeInfoMap.get(event.getInfo());
            if (layer != null)
            {
                switch (event.getEventAction())
                {
                    case ACTIVATE: // Intentional fall-through to RESET case.
                    case RESET:
                    {
                        reloadSingleLayer(layer);
                        break;
                    }
                    case DEACTIVATE:
                    {
                        removeLayers(layer);
                        break;
                    }
                    default:
                    {
                        throw new UnexpectedEnumException(event.getEventAction());
                    }
                }
            }
        }
    };

    /** The data registry listener. */
    private final DataRegistryListener<WMSLayer> myListener = new DataRegistryListenerAdapter<WMSLayer>()
    {
        @Override
        public void allValuesRemoved(Object source)
        {
            removeAllLayers();
        }

        @Override
        public boolean isIdArrayNeeded()
        {
            return false;
        }

        @Override
        public boolean isWantingRemovedObjects()
        {
            return true;
        }

        @Override
        public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends WMSLayer> newValues,
                Object source)
        {
            Set<? extends TimeSpan> currentSequence = myTimeMonitor.getCurrentSequence();

            // Check if the sequence contains durations that can't be handled.
            // If so, they layers will be saved off in the type info map, but
            // the geometries won't be published until the sequence changes to
            // something that can be handled. This can happen because
            // WMSGetCapabilitiesEnvoy activates the layers and changes the data
            // load durations at the same time but they happen on different
            // threads so there's a race condition for when this info is
            // received by this class.
            boolean badSequence = currentSequence != null && currentSequence.stream()
                    .anyMatch(timeSpan -> DefaultCustomization.getLargestIntegerUnitType(timeSpan.getDuration()).intValue() != 1);

            for (WMSLayer layer : newValues)
            {
                myMapLock.writeLock().lock();
                try
                {
                    myTypeInfoMap.put(layer.getTypeInfo(), layer);
                }
                finally
                {
                    myMapLock.writeLock().unlock();
                }

                if (layer.getTypeInfo().isVisible() && (layer.isTimeless() || !badSequence))
                {
                    List<? extends AbstractTileGeometry<?>> geoms = transformLayer(layer, currentSequence);
                    synchronized (myLoadedGeometriesMap)
                    {
                        CollectionUtilities.multiMapAddAll(myLoadedGeometriesMap, layer, geoms, false);
                    }
                    publishGeometries(geoms, Collections.<Geometry>emptyList());
                }
            }
        }

        @Override
        public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends WMSLayer> removedValues,
                Object source)
        {
            myMapLock.writeLock().lock();
            try
            {
                for (WMSLayer removedLayer : removedValues)
                {
                    myTypeInfoMap.remove(removedLayer.getTypeInfo());
                    removeLayers(removedLayer);
                }
            }
            finally
            {
                myMapLock.writeLock().unlock();
            }
        }
    };

    /** A map of categories to published geometries. */
    private final Map<WMSLayer, List<AbstractTileGeometry<?>>> myLoadedGeometriesMap = New.map();

    /** Lock for TypeInfo and LoadedGeometries maps. */
    private final ReentrantReadWriteLock myMapLock = new ReentrantReadWriteLock();

    /** Map of Layers to the render properties for the layer's tiles. */
    private final WMSPropertiesManager myPropertiesMgr;

    /** The Query metrics tracker. */
    private final WMSQueryTracker myQueryMetricsTracker;

    /** Observer for starting and ending image requests. */
    private final RequestObserver myRequestObserver = new RequestObserver()
    {
        @Override
        public void requestComplete()
        {
            myQueryMetricsTracker.queryEnded();
        }

        @Override
        public void requestStarted()
        {
            myQueryMetricsTracker.queryStarted();
        }
    };

    /** A monitor for the core managers that handle time. */
    private final WMSActiveTimeMonitor myTimeMonitor;

    /** Map used to lookup WMSLayers given a DataTypeInfo. */
    private final Map<WMSDataTypeInfo, WMSLayer> myTypeInfoMap = New.map();

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param queryMetricsTracker The query metrics tracker.
     */
    public WMSTransformer(Toolbox toolbox, WMSQueryTracker queryMetricsTracker)
    {
        super(toolbox.getDataRegistry());
        myTimeMonitor = new WMSActiveTimeMonitor(toolbox);
        myEventManager = toolbox.getEventManager();
        myPropertiesMgr = new WMSPropertiesManager(toolbox.getEventManager());
        myQueryMetricsTracker = queryMetricsTracker;
    }

    @Override
    public void close()
    {
        myPropertiesMgr.close();
        getDataRegistry().removeChangeListener(myListener);
        myTimeMonitor.removeListener(this);
        myTimeMonitor.deactivate();
        myEventManager.unsubscribe(WMSLayerEvent.class, myLayerEventListener);
        super.close();
    }

    @Override
    public void open()
    {
        super.open();
        myEventManager.subscribe(WMSLayerEvent.class, myLayerEventListener);
        myTimeMonitor.activate();
        myTimeMonitor.addListener(this);
        getDataRegistry().addChangeListener(myListener,
                new DataModelCategory(Nulls.STRING, WMSLayer.class.getName(), Nulls.STRING), WMSLayer.PROPERTY_DESCRIPTOR);
        myPropertiesMgr.open();
    }

    @Override
    public void timespansChanged(Collection<? extends TimeSpan> addedSpans, Collection<? extends TimeSpan> removedSpans)
    {
        Collection<? extends Geometry> addedGeoms = Collections.emptySet();
        Collection<? extends Geometry> removedGeoms = Collections.emptySet();
        if (removedSpans != null && !removedSpans.isEmpty())
        {
            removedGeoms = extractGeometriesForTime(removedSpans);
        }
        if (addedSpans != null && !addedSpans.isEmpty())
        {
            addedGeoms = buildNewGeometriesForTime(addedSpans);
        }
        publishGeometries(addedGeoms, removedGeoms);
    }

    @Override
    public void activeTimeChanged(TimeSpan active)
    {
        fadeTiles(active);
    }

    /**
     * Fades tiles by the percent overlap with the active span.
     *
     * @param active the active span
     */
    private void fadeTiles(TimeSpan active)
    {
        synchronized (myLoadedGeometriesMap)
        {
            for (Map.Entry<WMSLayer, List<AbstractTileGeometry<?>>> entry : myLoadedGeometriesMap.entrySet())
            {
                WMSLayer layer = entry.getKey();
                if (!layer.isTimeless())
                {
                    List<AbstractTileGeometry<?>> geometries = entry.getValue();

                    Map<TimeConstraint, TileRenderProperties> constraintToPropertyMap = New.map();
                    for (AbstractTileGeometry<?> geometry : geometries)
                    {
                        if (geometry instanceof ConstrainableGeometry
                                && geometry.getRenderProperties() instanceof TileRenderProperties)
                        {
                            TimeConstraint timeConstraint = ((ConstrainableGeometry)geometry).getConstraints()
                                    .getTimeConstraint();
                            TileRenderProperties renderProperties = (TileRenderProperties)geometry.getRenderProperties();
                            if (timeConstraint != null)
                            {
                                constraintToPropertyMap.put(timeConstraint, renderProperties);
                            }
                        }
                    }

                    float layerOpacity = layer.getTypeInfo().getMapVisualizationInfo().getTileRenderProperties().getOpacity()
                            / 255;
                    for (Map.Entry<TimeConstraint, TileRenderProperties> propertyEntry : constraintToPropertyMap.entrySet())
                    {
                        TimeConstraint timeConstraint = propertyEntry.getKey();
                        TileRenderProperties renderProperties = propertyEntry.getValue();

                        float fadeMultiple = 1;
                        if (timeConstraint.check(active))
                        {
                            TimeSpan tileSpan = timeConstraint.getTimeSpan();
                            TimeSpan overlap = active.getIntersection(tileSpan);
                            fadeMultiple = (float)overlap.getDurationMs() / tileSpan.getDurationMs();
                        }

                        float opacity = fadeMultiple * layerOpacity;
                        renderProperties.setOpacity(opacity);
                    }
                }
            }
        }
    }

    /**
     * Create tile geometries for all layers that have timespans that coincide
     * with the specified list of timespans. This does not publish the
     * geometries.
     *
     * @param sequence An iterable collection of timespans
     * @return The created geometries.
     */
    protected Collection<? extends Geometry> buildNewGeometriesForTime(Iterable<? extends TimeSpan> sequence)
    {
        Collection<Geometry> newGeometries = New.list();

        myMapLock.readLock().lock();
        try
        {
            for (WMSLayer layer : myTypeInfoMap.values())
            {
                if (!layer.isTimeless() && layer.getTypeInfo().isVisible())
                {
                    List<? extends AbstractTileGeometry<?>> geoms = transformLayer(layer, sequence);
                    synchronized (myLoadedGeometriesMap)
                    {
                        CollectionUtilities.multiMapAddAll(myLoadedGeometriesMap, layer, geoms, false);
                    }
                    newGeometries.addAll(geoms);
                }
            }
        }
        finally
        {
            myMapLock.readLock().unlock();
        }

        return newGeometries;
    }

    /**
     * Builds a collection of TimeSpans.
     *
     * @param layerSpan The bounding time span for a layer
     * @param activeSpans the currently-active time spans
     * @return the collection of timespans built from the passed-in time spans.
     */
    protected Collection<TimeSpan> buildTimeDivisions(TimeSpan layerSpan, Iterable<? extends TimeSpan> activeSpans)
    {
        Collection<TimeSpan> timeDivisions;
        if (layerSpan.isTimeless())
        {
            timeDivisions = Collections.singleton(layerSpan);
        }
        else
        {
            timeDivisions = New.list();
            if (CollectionUtilities.hasContent(activeSpans))
            {
                for (TimeSpan activeSpan : activeSpans)
                {
                    if (layerSpan.overlaps(activeSpan))
                    {
                        timeDivisions.add(activeSpan);
                    }
                }
            }
        }
        return timeDivisions;
    }

    /**
     * Removes Tile Geometries for all timed layers with the specified TimeSpans
     * from my internal models. This does <b>not</b> remove the geometries from
     * the registry.
     *
     * @param removalSpans the timespans to remove
     * @return The geometries that were extracted.
     */
    protected List<? extends Geometry> extractGeometriesForTime(Collection<? extends TimeSpan> removalSpans)
    {
        Set<WMSLayer> timedLayers = New.set();
        myMapLock.readLock().lock();
        try
        {
            for (WMSLayer layer : myLoadedGeometriesMap.keySet())
            {
                if (!layer.isTimeless())
                {
                    timedLayers.add(layer);
                }
            }
        }
        finally
        {
            myMapLock.readLock().unlock();
        }

        List<Geometry> geomsForTime = New.list();
        for (WMSLayer layer : timedLayers)
        {
            synchronized (myLoadedGeometriesMap)
            {
                List<AbstractTileGeometry<?>> allTileGeoms = myLoadedGeometriesMap.get(layer);
                if (CollectionUtilities.hasContent(allTileGeoms))
                {
                    for (Iterator<AbstractTileGeometry<?>> iter = allTileGeoms.iterator(); iter.hasNext();)
                    {
                        AbstractTileGeometry<?> tile = iter.next();
                        if (tile instanceof ConstrainableGeometry
                                && ((ConstrainableGeometry)tile).getConstraints().getTimeConstraint() != null)
                        {
                            TimeSpan tileSpan = ((ConstrainableGeometry)tile).getConstraints().getTimeConstraint().getTimeSpan();
                            if (removalSpans.contains(tileSpan))
                            {
                                iter.remove();
                                myImageMgrPool.remove(tile.getImageManager());
                                geomsForTime.add(tile);
                            }
                        }
                    }
                }
            }
        }
        return geomsForTime;
    }

    /**
     * Remove all of the loaded Geometries for a single layer and replace them
     * with freshly-built geometries (if the layer is active).
     *
     * @param layer the layer to reload
     */
    protected void reloadSingleLayer(WMSLayer layer)
    {
        Collection<Geometry> adds = New.list();
        List<Geometry> removes = New.list();

        synchronized (myLoadedGeometriesMap)
        {
            List<AbstractTileGeometry<?>> geomList = myLoadedGeometriesMap.remove(layer);
            if (geomList != null && !geomList.isEmpty())
            {
                removes.addAll(geomList);
            }
        }

        if (layer.getTypeInfo().isVisible())
        {
            Iterable<TimeSpan> timeList;
            if (layer.isTimeless())
            {
                timeList = Collections.singleton(TimeSpan.TIMELESS);
            }
            else
            {
                Set<? extends TimeSpan> activeSpans = myTimeMonitor.getCurrentSequence() == null
                        ? Collections.<TimeSpan>emptySet() : myTimeMonitor.getCurrentSequence();
                timeList = buildTimeDivisions(layer.getTimeSpan(), activeSpans);
            }
            List<? extends AbstractTileGeometry<?>> geoms = transformLayer(layer, timeList);
            synchronized (myLoadedGeometriesMap)
            {
                CollectionUtilities.multiMapAddAll(myLoadedGeometriesMap, layer, geoms, false);
            }
            adds.addAll(geoms);
        }

        publishGeometries(adds, removes);
    }

    /**
     * Removes all layers and their tile geometries.
     */
    protected void removeAllLayers()
    {
        List<Geometry> removes = New.list();
        synchronized (myLoadedGeometriesMap)
        {
            Collection<List<AbstractTileGeometry<?>>> values = myLoadedGeometriesMap.values();
            for (List<AbstractTileGeometry<?>> list : values)
            {
                removes.addAll(list);
            }
            myLoadedGeometriesMap.clear();
        }
        myMapLock.writeLock().lock();
        try
        {
            myTypeInfoMap.clear();
        }
        finally
        {
            myMapLock.writeLock().unlock();
        }
        publishGeometries(Collections.<Geometry>emptyList(), removes);
    }

    /**
     * Remove the layers associated with a particular category.
     *
     * @param layer The WMSLayer to remove.
     */
    protected void removeLayers(WMSLayer layer)
    {
        List<AbstractTileGeometry<?>> removes;
        synchronized (myLoadedGeometriesMap)
        {
            removes = myLoadedGeometriesMap.remove(layer);
        }
        if (removes != null)
        {
            // Remove the image managers from the pool so that new images will
            // be downloaded.
            for (AbstractTileGeometry<?> remove : removes)
            {
                myImageMgrPool.remove(remove.getImageManager());
            }
            publishGeometries(Collections.<Geometry>emptyList(), removes);
        }
    }

    /**
     * Create the tiles for a layer.
     *
     * @param layer The WMS layer model.
     * @param sequence collection of time spans that need geometries created
     * @return the list of transformed layers.
     */
    protected List<? extends AbstractTileGeometry<?>> transformLayer(WMSLayer layer, Iterable<? extends TimeSpan> sequence)
    {
        WMSLayerConfig layerConf = layer.getConfiguration();

        TileGeometry.Builder<GeographicPosition> tileBuilder = null;
        TerrainTileGeometry.Builder<GeographicPosition> terrainBuilder = null;
        AbstractTileGeometry.Builder<GeographicPosition> absTileBuilder = null;
        if (WMSLayerConfig.LayerType.SRTM.equals(layerConf.getLayerType()))
        {
            terrainBuilder = new TerrainTileGeometry.Builder<GeographicPosition>();
            BilSrtmImageReader srtmReader = new BilSrtmImageReader(layerConf.getBoundingBoxConfig().getGeographicBoundingBox(),
                    -Short.MIN_VALUE, layerConf.getGetMapConfig().getSRS(), layer.getTypeInfo().getTypeKey());
            terrainBuilder.setElevationReader(srtmReader);
            absTileBuilder = terrainBuilder;
        }
        else if (WMSLayerConfig.LayerType.LIDAR.equals(layerConf.getLayerType()))
        {
            terrainBuilder = new TerrainTileGeometry.Builder<GeographicPosition>();
            LidarElevationReader lidarReader = new LidarElevationReader(
                    layerConf.getBoundingBoxConfig().getGeographicBoundingBox(), -Short.MIN_VALUE,
                    layerConf.getGetMapConfig().getSRS(), layer.getTypeInfo().getTypeKey());
            terrainBuilder.setElevationReader(lidarReader);
            absTileBuilder = terrainBuilder;
        }
        else
        {
            tileBuilder = new TileGeometry.Builder<GeographicPosition>();
            absTileBuilder = tileBuilder;
        }

        ViewerPositionConstraint viewConstraint = createViewConstraint(layerConf);

        Divider divider = createAndConfigureDivider(layer, absTileBuilder);

        DataGroupInfo dgi = layer.getTypeInfo().getParent();
        Object constraintKey = dgi == null ? null : dgi.getId();

        Collection<GeographicBoundingBox> fixedGrid = layer.generateFixedGrid(0);

        Collection<TimeSpan> timeDivisions = buildTimeDivisions(layer.getTimeSpan(), sequence);

        TileRenderProperties layerRenderProperties = layer.getTypeInfo().getMapVisualizationInfo().getTileRenderProperties();
        List<TileRenderProperties> childRenderProperties = New.list(timeDivisions.size());

        List<AbstractTileGeometry<?>> geomsForModel = New.list(timeDivisions.size() * fixedGrid.size());
        for (TimeSpan timeDivision : timeDivisions)
        {
            // Use a render property per time division in order to be able to fade tiles that partially overlap the active span
            TileRenderProperties props = layerRenderProperties.clone();
            childRenderProperties.add(props);

            Constraints constraints = createConstraints(viewConstraint, timeDivision, sequence, constraintKey);

            for (GeographicBoundingBox bbox : fixedGrid)
            {
                absTileBuilder.setBounds(bbox);
                absTileBuilder.setImageManager(getImageManager(new TileImageKey(bbox, timeDivision), layer));
                boolean isTerrain = layerConf.getLayerType().getMapVisualizationType() == MapVisualizationType.TERRAIN_TILE;
                AbstractTileGeometry<?> geom = isTerrain
                        ? new TerrainTileGeometry(terrainBuilder, props, layer.getTypeInfo().getTypeKey())
                        : new TileGeometry(tileBuilder, props, constraints, layer.getTypeInfo().getTypeKey());
                geomsForModel.add(geom);
            }
        }

        if (layerRenderProperties instanceof ParentTileRenderProperties)
        {
            ((ParentTileRenderProperties)layerRenderProperties).getChildren().addAll(childRenderProperties);
        }

        int maxTileGeneration = geomsForModel.stream().mapToInt(g -> divider.determineMaxGeneration(g)).max().orElse(0);

        updateTileLevelController(layer, geomsForModel, divider, maxTileGeneration);

        return geomsForModel;
    }

    /**
     * Creates the viewer position constraint for the layer config.
     *
     * @param layerConf the layer config
     * @return the viewer position constraint
     */
    private ViewerPositionConstraint createViewConstraint(WMSLayerConfig layerConf)
    {
        ViewerPositionConstraint viewConstraint = null;
        Double rawMaxAlt = layerConf.getDisplayConfig().getMaxDisplayElevation();
        Double rawMinAlt = layerConf.getDisplayConfig().getMinDisplayElevation();
        if (rawMaxAlt != null || rawMinAlt != null)
        {
            Altitude maxAltitude = rawMaxAlt == null ? null
                    : Altitude.createFromMeters(rawMaxAlt.doubleValue(), Altitude.ReferenceLevel.ELLIPSOID);
            Altitude minAltitude = rawMinAlt == null ? null
                    : Altitude.createFromMeters(rawMinAlt.doubleValue(), Altitude.ReferenceLevel.ELLIPSOID);
            viewConstraint = new ViewerPositionConstraint(minAltitude, maxAltitude);
        }
        return viewConstraint;
    }

    /**
     * Creates the and configure divider.
     *
     * @param layer the layer
     * @param absTileBuilder the abstract tile builder
     * @return the divider
     */
    private Divider createAndConfigureDivider(WMSLayer layer, AbstractTileGeometry.Builder<GeographicPosition> absTileBuilder)
    {
        Divider divider = new Divider(layer.getTypeInfo().getTypeKey(), layer.getMinimumGridSize());
        absTileBuilder.setDivider(divider);
        absTileBuilder.setMinimumDisplaySize(layer.getMinimumDisplaySize());
        absTileBuilder.setMaximumDisplaySize(layer.getMaximumDisplaySize());
        return divider;
    }

    /**
     * Create the constraints based on the viewer constraint and time division.
     *
     * @param viewConstraint Constraint for viewer position.
     * @param timeDivision The time span which defines the time constraint.
     * @param timeDivisions The collection of all time spans that layers are
     *            being created for.
     * @param constraintKey Key for the constraints.
     * @return The newly created constraints.
     */
    private Constraints createConstraints(ViewerPositionConstraint viewConstraint, TimeSpan timeDivision,
            Iterable<? extends TimeSpan> timeDivisions, Object constraintKey)
    {
        Constraints constraints = null;
        if (!timeDivision.isTimeless())
        {
            // Determine if there are any other time divisions that overlap this
            // one but are smaller. If there are, get the largest one of those
            // and use its duration as the minDuration constraint for this time
            // division.
            Duration minDuration;
            TimeSpan largestSmallerOverlap = null;
            for (TimeSpan div : timeDivisions)
            {
                if (timeDivision.contains(div) && div.getDurationMs() < timeDivision.getDurationMs()
                        && (largestSmallerOverlap == null || largestSmallerOverlap.getDurationMs() < div.getDurationMs()))
                {
                    largestSmallerOverlap = div;
                }
            }
            minDuration = largestSmallerOverlap == null ? Seconds.ZERO : largestSmallerOverlap.getDuration();

            BoundedTimeConstraint timeConstraint = BoundedTimeConstraint.getTimeConstraint(constraintKey, timeDivision,
                    minDuration, null);
            constraints = new Constraints(timeConstraint, viewConstraint);
        }
        else if (viewConstraint != null)
        {
            constraints = new Constraints((TimeConstraint)null, viewConstraint);
        }
        return constraints;
    }

    /**
     * Gets an image manager for the specified {@link TileImageKey}. If a weak
     * reference to an {@link ImageManager} exists for the given key, return
     * that, otherwise create a new one and return it.
     *
     * @param key the unique image key for a geographic area and time
     * @param layer the server layer for the key
     * @return the appropriate image manager
     */
    private ImageManager getImageManager(TileImageKey key, WMSLayer layer)
    {
        ImageManager imageManager = new ImageManager(key, layer);
        imageManager.addRequestObserver(myRequestObserver);
        return myImageMgrPool.get(imageManager);
    }

    /**
     * Update tile level controller.
     *
     * @param layer the layer
     * @param geomsForModel the geoms for model
     * @param divider the divider
     * @param maxTileGeneration the max tile generation
     */
    private void updateTileLevelController(WMSLayer layer, List<AbstractTileGeometry<?>> geomsForModel, Divider divider,
            int maxTileGeneration)
    {
        if (layer.getTypeInfo().getMapVisualizationInfo().getTileLevelController() instanceof DefaultTileLevelController)
        {
            DefaultTileLevelController tileLevelController = (DefaultTileLevelController)layer.getTypeInfo()
                    .getMapVisualizationInfo().getTileLevelController();
            tileLevelController.setMaxGeneration(maxTileGeneration);
            tileLevelController.addDivider(divider);
            tileLevelController.addTileGeometries(geomsForModel);
        }
    }

    /** The tile divider. */
    private final class Divider extends AbstractDivider<GeographicPosition>
    {
        /** Minimum grid size produced by this divider. */
        private final Vector2d myMinimumGridSize;

        /**
         * Construct the divider.
         *
         * @param dtiKey the dti key
         * @param minimumGridSize The smallest grid size the divider will
         *            produce (longitude degrees, latitude degrees).
         */
        public Divider(String dtiKey, Vector2d minimumGridSize)
        {
            super(dtiKey);
            myMinimumGridSize = minimumGridSize;
        }

        /**
         * Determine max generation.
         *
         * @param topLevelTile the top level tile
         * @return the int
         */
        public int determineMaxGeneration(AbstractTileGeometry<?> topLevelTile)
        {
            int maxGeneration = 0;
            GeographicBoundingBox bbox = (GeographicBoundingBox)topLevelTile.getBounds();
            double deltaLon = bbox.getDeltaLonD() / 2.0;
            double deltaLat = bbox.getDeltaLatD() / 2.0;
            boolean divisible = deltaLon / 2 > myMinimumGridSize.getX() && deltaLat / 2 > myMinimumGridSize.getY();
            while (divisible)
            {
                maxGeneration++;
                deltaLon = deltaLon / 2.0;
                deltaLat = deltaLat / 2.0;
                divisible = deltaLon / 2.0 > myMinimumGridSize.getX() && deltaLat / 2.0 > myMinimumGridSize.getY();
            }
            return maxGeneration + 1;
        }

        @Override
        public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
        {
            Collection<AbstractTileGeometry<?>> result = New.list(4);
            GeographicBoundingBox bbox = (GeographicBoundingBox)parent.getBounds();
            boolean divisible = bbox.getDeltaLonD() / 2 > myMinimumGridSize.getX()
                    && bbox.getDeltaLatD() / 2 > myMinimumGridSize.getY();

            TimeSpan timeSpan = ((TileImageKey)parent.getImageManager().getImageKey()).getTimeSpan();
            for (GeographicBoundingBox aBox : bbox.quadSplit())
            {
                AbstractTileGeometry<?> subTile = parent.createSubTile(aBox, new TileImageKey(aBox, timeSpan),
                        divisible ? this : null);
                subTile.getImageManager().addRequestObserver(myRequestObserver);
                result.add(subTile);
            }

            return result;
        }
    }
}
