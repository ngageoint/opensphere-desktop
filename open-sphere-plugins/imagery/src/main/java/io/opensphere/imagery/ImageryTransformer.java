package io.opensphere.imagery;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.data.DataRegistryListener;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.ImageProvidingGeometry;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.constraint.ViewerPositionConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultLabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.SharedObjectPool;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;

/**
 * Transforms Imagery Tile models into {@link Geometry}s.
 */
@SuppressWarnings("PMD.GodClass")
public class ImageryTransformer extends DefaultTransformer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageryTransformer.class);

    /** Lock for my category map. */
    private final ReentrantReadWriteLock myCatMapLock = new ReentrantReadWriteLock();

    /** The Core control action manager. */
    private final ContextActionManager myControlActionManager;

    /** The DTI change listener. */
    private final EventListener<DataTypeInfoColorChangeEvent> myDTIChangeListener = new EventListener<DataTypeInfoColorChangeEvent>()
    {
        @Override
        public void notify(DataTypeInfoColorChangeEvent event)
        {
            if (event.isOpacityChangeOnly() && event.getDataTypeInfo() instanceof ImageryDataTypeInfo)
            {
                TileRenderProperties trp = event.getDataTypeInfo().getMapVisualizationInfo().getTileRenderProperties();
                Color c = event.getColor();
                if (trp != null && c != null)
                {
                    Color hc = trp.getHighlightColor();
                    trp.setOpacity(c.getComponents(null)[3]);
                    trp.setHighlightColor(ColorUtilities.opacitizeColor(hc, c.getAlpha()));
                }
            }
        }
    };

    /** My event manager. */
    private final EventManager myEventManager;

    /** Pool of references to ImageManagers. */
    private final SharedObjectPool<ImageManager> myImageMgrPool = new SharedObjectPool<>();

    /** A map of layers to labels. */
    private final Map<ImageryLayer, LabelGeometry> myLabelMap = Collections
            .synchronizedMap(New.<ImageryLayer, LabelGeometry>map());

    /** My layer category map. */
    private final Map<ImageryLayer, DataModelCategory> myLayerCategoryMap = New.map();

    /** My layer event listener. */
    private final EventListener<ImageryLayerEvent> myLayerEventListener = new EventListener<ImageryLayerEvent>()
    {
        @Override
        public void notify(ImageryLayerEvent event)
        {
            ImageryLayer layer = myTypeInfoMap.get(event.getInfo());
            if (layer != null)
            {
                DataModelCategory cat = myLayerCategoryMap.get(layer);
                if (cat != null)
                {
                    switch (event.getEventAction())
                    {
                        case ACTIVATE:
                        {
                            reloadSingleLayer(cat, layer);
                            break;
                        }
                        case DEACTIVATE:
                        {
                            removeLayers(cat);
                            break;
                        }
                        case RESET:
                        {
                            reloadSingleLayer(cat, layer);
                            break;
                        }
                        default:
                        {
                            throw new UnexpectedEnumException(event.getEventAction());
                        }
                    }
                }
            }
        }
    };

    /** The data registry listener. */
    private final DataRegistryListener<ImageryLayer> myListener = new DataRegistryListenerAdapter<ImageryLayer>()
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
        public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends ImageryLayer> newValues,
                Object source)
        {
            addLayers(dataModelCategory, newValues);
            loadTimelessLayers(dataModelCategory);
        }

        @Override
        public void valuesRemoved(DataModelCategory dataModelCategory, long[] ids, Object source)
        {
            WriteLock lock = myCatMapLock.writeLock();
            lock.lock();
            try
            {
                Set<Entry<ImageryLayer, DataModelCategory>> entrySet = myLayerCategoryMap.entrySet();
                for (Iterator<Entry<ImageryLayer, DataModelCategory>> iter = entrySet.iterator(); iter.hasNext();)
                {
                    Entry<ImageryLayer, DataModelCategory> entry = iter.next();
                    if (entry.getValue().equals(dataModelCategory))
                    {
                        iter.remove();
                    }
                }
            }
            finally
            {
                lock.unlock();
            }

            removeLayers(dataModelCategory);
        }
    };

    /** A map of categories to published geometries. */
    private final Map<DataModelCategory, List<Geometry>> myLoadedGeometriesMap = New.map();

    /** The menu provider for my tiles. */
    private final ContextMenuProvider<GeometryContextKey> myMenuProvider = new ContextMenuProvider<GeometryContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, GeometryContextKey key)
        {
            Geometry geometry = key.getGeometry();
            if (geometry instanceof TileGeometry)
            {
                final TileGeometry tile = (TileGeometry)geometry;
                if (tile.getImageManager().getImageProvider() instanceof ImageryLayer)
                {
                    List<JMenuItem> menuItems = New.list();

                    JMenuItem menuItem = new JMenuItem("Clear Image Cache");
                    menuItem.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            ((ImageryLayer)tile.getImageManager().getImageProvider()).getTypeInfo().getImageryFileSource()
                                    .cleanCache(getDataRegistry());
                            tile.getImageManager().clearImages();
                            tile.requestImageData();
                        }
                    });
                    menuItems.add(menuItem);

                    final LabelGeometry label = myLabelMap.get(tile.getImageManager().getImageProvider());
                    if (label != null)
                    {
                        if (label.getRenderProperties().isHidden())
                        {
                            menuItem = new JMenuItem("Show label");
                            menuItem.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent e)
                                {
                                    label.getRenderProperties().setHidden(false);
                                }
                            });
                        }
                        else
                        {
                            menuItem = new JMenuItem("Hide label");
                            menuItem.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent e)
                                {
                                    label.getRenderProperties().setHidden(true);
                                }
                            });
                        }
                        menuItems.add(menuItem);
                    }

                    return menuItems;
                }
            }
            return null;
        }

        @Override
        public int getPriority()
        {
            return 11600;
        }
    };

    /** Map of Layers to the render properties for the layer's tiles. */
    private final ImageryPropertiesManager myPropertiesMgr;

    /** Map used to lookup ImageryLayer given a DataTypeInfo. */
    private final Map<ImageryDataTypeInfo, ImageryLayer> myTypeInfoMap = New.map();

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public ImageryTransformer(Toolbox toolbox)
    {
        super(toolbox.getDataRegistry());
        myEventManager = toolbox.getEventManager();
        myPropertiesMgr = new ImageryPropertiesManager(toolbox.getEventManager());
        myControlActionManager = toolbox.getUIRegistry().getContextActionManager();
        myControlActionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                GeometryContextKey.class, myMenuProvider);
    }

    @Override
    public void close()
    {
        myPropertiesMgr.close();
        getDataRegistry().removeChangeListener(myListener);
        myEventManager.unsubscribe(ImageryLayerEvent.class, myLayerEventListener);
        myEventManager.unsubscribe(DataTypeInfoColorChangeEvent.class, myDTIChangeListener);
        myControlActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                GeometryContextKey.class, myMenuProvider);
        super.close();
    }

    @Override
    public void open()
    {
        super.open();
        myEventManager.subscribe(ImageryLayerEvent.class, myLayerEventListener);
        getDataRegistry().addChangeListener(myListener,
                new DataModelCategory(Nulls.STRING, ImageryLayer.class.getName(), Nulls.STRING),
                ImageryLayer.PROPERTY_DESCRIPTOR);
        myEventManager.subscribe(DataTypeInfoColorChangeEvent.class, myDTIChangeListener);
        myPropertiesMgr.open();
    }

    /**
     * Add layers into the list of available layers.
     *
     * @param dataModelCategory The data model category.
     * @param layers The WMS layer models.
     */
    protected void addLayers(DataModelCategory dataModelCategory, Iterable<? extends ImageryLayer> layers)
    {
        for (ImageryLayer layer : layers)
        {
            myCatMapLock.writeLock().lock();
            try
            {
                myLayerCategoryMap.put(layer, dataModelCategory);
            }
            finally
            {
                myCatMapLock.writeLock().unlock();
            }
            myTypeInfoMap.put(layer.getTypeInfo(), layer);
        }
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
            for (TimeSpan activeSpan : activeSpans)
            {
                if (layerSpan.overlaps(activeSpan))
                {
                    timeDivisions.add(activeSpan);
                }
            }
        }
        return timeDivisions;
    }

    /**
     * Transform timeless layers with the specified category into tile
     * geometries.
     *
     * @param dataModelCategory The data model category.
     */
    protected void loadTimelessLayers(DataModelCategory dataModelCategory)
    {
        Collection<Geometry> addGeometries = New.list();
        List<ImageryLayer> activeTimelessLayers = New.list();

        myCatMapLock.readLock().lock();
        try
        {
            for (Entry<ImageryLayer, DataModelCategory> entry : myLayerCategoryMap.entrySet())
            {
                ImageryLayer layer = entry.getKey();
                DataModelCategory cat = entry.getValue();
                if (cat.equals(dataModelCategory) && layer.getTypeInfo().isVisible())
                {
                    activeTimelessLayers.add(layer);
                }
            }
        }
        finally
        {
            myCatMapLock.readLock().unlock();
        }

        if (!activeTimelessLayers.isEmpty())
        {
            for (ImageryLayer layer : activeTimelessLayers)
            {
                Collection<? extends Geometry> geoms = transformLayer(layer, Collections.singleton(TimeSpan.TIMELESS));
                synchronized (myLoadedGeometriesMap)
                {
                    CollectionUtilities.multiMapAddAll(myLoadedGeometriesMap, dataModelCategory, geoms, false);
                }
                LOGGER.info("Publishing Imagery Tile Geometries: Layer[" + layer.getLayerId() + "] Category[" + dataModelCategory
                        + "] Adds[" + geoms.size() + "]");
                addGeometries.addAll(geoms);
            }
        }
        publishGeometries(addGeometries, Collections.<Geometry>emptyList());
    }

    /**
     * Reload all the Geometries for a single layer.
     *
     * @param category the Data Model Category for the layer
     * @param layer the layer to reload
     */
    protected void reloadSingleLayer(DataModelCategory category, ImageryLayer layer)
    {
        Collection<Geometry> adds = New.list();
        List<Geometry> removes = New.list();

        myLabelMap.remove(layer);
        synchronized (myLoadedGeometriesMap)
        {
            List<Geometry> geomList = myLoadedGeometriesMap.remove(category);
            if (geomList != null && !geomList.isEmpty())
            {
                removes.addAll(geomList);
            }
        }

        if (layer.isDisplayable())
        {
            Iterable<TimeSpan> timeList = Collections.singleton(TimeSpan.TIMELESS);
            Collection<? extends Geometry> geoms = transformLayer(layer, timeList);
            synchronized (myLoadedGeometriesMap)
            {
                CollectionUtilities.multiMapAddAll(myLoadedGeometriesMap, category, geoms, false);
            }
            adds.addAll(geoms);
        }

        LOGGER.info("Publishing Imagery Tile Geometries: Layer[" + layer.getLayerId() + "] Category[" + category + "] Adds["
                + adds.size() + "] Removes[" + removes.size() + "]");
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
            Collection<List<Geometry>> values = myLoadedGeometriesMap.values();
            for (List<Geometry> list : values)
            {
                removes.addAll(list);
            }
            myLoadedGeometriesMap.clear();
        }
        myLabelMap.clear();
        myCatMapLock.writeLock().lock();
        try
        {
            myLayerCategoryMap.clear();
        }
        finally
        {
            myCatMapLock.writeLock().unlock();
        }
        myTypeInfoMap.clear();
        LOGGER.info("De-Publishing All Imagery Tile Geometries.");
        publishGeometries(Collections.<Geometry>emptyList(), removes);
    }

    /**
     * Remove the layers associated with a particular category.
     *
     * @param dataModelCategory The data model category.
     */
    protected void removeLayers(DataModelCategory dataModelCategory)
    {
        List<Geometry> removes;
        synchronized (myLoadedGeometriesMap)
        {
            removes = myLoadedGeometriesMap.remove(dataModelCategory);
        }
        synchronized (myLabelMap)
        {
            Set<ImageryLayer> keySet = myLabelMap.keySet();
            for (Iterator<ImageryLayer> iter = keySet.iterator(); iter.hasNext();)
            {
                ImageryLayer imageryLayer = iter.next();
                if (imageryLayer.getTitle().equals(dataModelCategory.getCategory()))
                {
                    LOGGER.info("De-Publishing Imagery Tile Geometries: Layer[" + imageryLayer.getLayerId() + "] Category["
                            + imageryLayer + "]");
                    iter.remove();
                }
            }
        }
        if (removes != null)
        {
            // Remove the image managers from the pool so that new images will
            // be downloaded.
            for (Geometry remove : removes)
            {
                if (remove instanceof ImageProvidingGeometry)
                {
                    myImageMgrPool.remove(((ImageProvidingGeometry<?>)remove).getImageManager());
                }
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
    protected Collection<? extends Geometry> transformLayer(ImageryLayer layer, Iterable<? extends TimeSpan> sequence)
    {
        TileRenderProperties props = null;

        TileGeometry.Builder<GeographicPosition> tileBuilder = new TileGeometry.Builder<GeographicPosition>();
        props = layer.getTypeInfo().getMapVisualizationInfo().getTileRenderProperties();
        tileBuilder.setDataModelId(layer.getLayerId());
        ViewerPositionConstraint viewConstraint = null;

        Divider divider = new Divider(layer.getTypeInfo().getTypeKey(), layer.getMinimumGridSize());
        tileBuilder.setDivider(divider);
        tileBuilder.setMinimumDisplaySize(384);
        tileBuilder.setMaximumDisplaySize(768);

        DataGroupInfo dgi = layer.getTypeInfo().getParent();
        Object constraintKey = dgi == null ? null : dgi.getId();
        Collection<Geometry> geomsForModel = New.collection();
        Collection<AbstractTileGeometry<?>> tileGeometries = New.collection();
        Constraints constraints = createConstraints(viewConstraint, constraintKey);

        int maxTileGeneration = 0;
        for (GeographicBoundingBox bbox : layer.generateGrid(0))
        {
            tileBuilder.setBounds(bbox);
            tileBuilder.setImageManager(getImageManager(new ImageryImageKey(bbox), layer));
            AbstractTileGeometry<?> geom = new ImageryTileGeometry(tileBuilder, props, constraints);
            int maxGen = divider.determineMaxGeneration(geom);
            if (maxGen > maxTileGeneration)
            {
                maxTileGeneration = maxGen;
            }
            tileGeometries.add(geom);
            geomsForModel.add(geom);
        }
        updateTileLevelController(layer, tileGeometries, divider, maxTileGeneration);

        LabelGeometry.Builder<GeographicPosition> labelBuilder = new LabelGeometry.Builder<GeographicPosition>();
        labelBuilder.setPosition(layer.getBoundingBox().getCenter());
        labelBuilder.setText(layer.getTitle());
        labelBuilder.setHorizontalAlignment(.5f);
        labelBuilder.setVerticalAlignment(.5f);
        labelBuilder.setOutlined(true);
        LabelRenderProperties labelRenderProps = new DefaultLabelRenderProperties(props.getZOrder(), true, false);
        LabelGeometry label = new LabelGeometry(labelBuilder, labelRenderProps, null);
        myLabelMap.put(layer, label);
        geomsForModel.add(label);
        return geomsForModel;
    }

    /**
     * Create the constraints based on the viewer constraint and time division.
     *
     * @param viewConstraint Constraint for viewer position.
     * @param constraintKey Key for the constraints.
     * @return The newly created constraints.
     */
    private Constraints createConstraints(ViewerPositionConstraint viewConstraint, Object constraintKey)
    {
        return new Constraints((TimeConstraint)null, viewConstraint);
    }

    /**
     * Gets an image manager for the specified {@link ImageryImageKey}. If a
     * weak reference to an {@link ImageManager} exists for the given key,
     * return that, otherwise create a new one and return it.
     *
     * @param key the unique image key for a geographic area and time
     * @param layer the server layer for the key
     * @return the appropriate image manager
     */
    private ImageManager getImageManager(ImageryImageKey key, ImageryLayer layer)
    {
        return myImageMgrPool.get(new ImageManager(key, layer));
    }

    /**
     * Update tile level controller.
     *
     * @param layer the layer
     * @param tileGeometries the tile geometries
     * @param divider the divider
     * @param maxTileGeneration the max tile generation
     */
    private void updateTileLevelController(ImageryLayer layer, Collection<AbstractTileGeometry<?>> tileGeometries,
            Divider divider, int maxTileGeneration)
    {
        if (layer.getTypeInfo().getMapVisualizationInfo().getTileLevelController() instanceof DefaultTileLevelController)
        {
            DefaultTileLevelController tileLevelController = (DefaultTileLevelController)layer.getTypeInfo()
                    .getMapVisualizationInfo().getTileLevelController();
            tileLevelController.clearDividers();
            tileLevelController.clearTileGeometries();
            tileLevelController.setMaxGeneration(maxTileGeneration);
            tileLevelController.addDivider(divider);
            tileLevelController.addTileGeometries(tileGeometries);
        }
    }

    /** The tile divider. */
    private static final class Divider extends AbstractDivider<GeographicPosition>
    {
        /** Minimum grid size produced by this divider. */
        private final Vector2d myMinimumGridSize;

        /**
         * Construct the divider.
         *
         * @param dividerKey the divider unique key
         * @param minimumGridSize The smallest grid size the divider will
         *            produce (longitude degrees, latitude degrees).
         */
        public Divider(String dividerKey, Vector2d minimumGridSize)
        {
            super(dividerKey);
            myMinimumGridSize = minimumGridSize;
        }

        /**
         * Determine max generation for the divider.
         *
         * @param topLevelTile the top level tile
         * @return the int
         */
        public int determineMaxGeneration(AbstractTileGeometry<?> topLevelTile)
        {
            int genCounter = 1;
            GeographicBoundingBox bbox = (GeographicBoundingBox)topLevelTile.getBounds();
            double deltaLon = bbox.getDeltaLonD() / 2.0;
            double deltaLat = bbox.getDeltaLatD() / 2.0;
            boolean divisible = deltaLon / 2 > myMinimumGridSize.getX() && deltaLat / 2 > myMinimumGridSize.getY();
            while (divisible)
            {
                genCounter++;
                deltaLon = deltaLon / 2.0;
                deltaLat = deltaLat / 2.0;
                divisible = deltaLon / 2.0 > myMinimumGridSize.getX() && deltaLat / 2.0 > myMinimumGridSize.getY();
            }
            return genCounter + 1;
        }

        @Override
        public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
        {
            Collection<AbstractTileGeometry<?>> result = New.list(4);
            GeographicBoundingBox bbox = (GeographicBoundingBox)parent.getBounds();
            boolean divisible = bbox.getDeltaLonD() / 2 > myMinimumGridSize.getX()
                    && bbox.getDeltaLatD() / 2 > myMinimumGridSize.getY();

            for (GeographicBoundingBox aBox : bbox.quadSplit())
            {
                result.add(parent.createSubTile(aBox, new ImageryImageKey(aBox), divisible ? this : null));
            }

            return result;
        }
    }
}
