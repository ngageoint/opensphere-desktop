package io.opensphere.kml.mantle.controller;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.jcip.annotations.ThreadSafe;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.Overlay;
import de.micromata.opengis.kml.v_2_2_0.ScreenOverlay;
import de.micromata.opengis.kml.v_2_2_0.ViewRefreshMode;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.ToolbarManager;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.AbstractTileGeometry.AbstractDivider;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.TileGeometry.Builder;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultZOrderRenderProperties;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.Image.CompressionType;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.ParticipantOrderChangeEvent;
import io.opensphere.core.order.ParticipantOrderChangeEvent.ParticipantChangeType;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.core.timeline.StyledTimelineDatum;
import io.opensphere.core.timeline.TimelineDatum;
import io.opensphere.core.timeline.TimelineRegistry;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.cache.SimpleCache;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLLinkHelper;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Handles overlay geometries.
 */
@SuppressWarnings("PMD.GodClass")
@ThreadSafe
public class OverlayTransformer extends AbstractKMLTransformer
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(OverlayTransformer.class);

    /** The max opacity value. */
    private static final int MAX_OPACITY = 255;

    /**
     * The ratio of the minimum display size to the maximum display size for
     * overlay tiles.
     */
    private static final double MINIMUM_DISPLAY_SIZE_RATIO = Utilities.parseSystemProperty("kml.tileMinDisplaySizeRatio", .4);

    /** The pixel width for lower-level tile images. */
    private static final int SUB_IMAGE_WIDTH_PIXELS = Utilities.parseSystemProperty("kml.subTileWidthPixels", 1024);

    /** The pixel width for top-level tile images. */
    private static final int TOP_IMAGE_WIDTH_PIXELS = Utilities.parseSystemProperty("kml.topTileWidthPixels", 512);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Manager which determines the z-order of the images. */
    private final OrderManager myImageOrderManager;

    /**
     * The Mantle Toolbox used to get Z-Order, availability, and stored
     * preferences for each layer.
     */
    private final MantleToolbox myMantleTb;

    /** The image provider for overlays. */
    private final KMLImageProvider myImageProvider;

    /** A map of data type keys to order management participants. */
    private final Map<String, DefaultOrderParticipantKey> myOrderParticipants = Collections.synchronizedMap(New.map());

    /** Way of storing data types even when they're removed from mantle. */
    private final SimpleCache<String, DataTypeInfo> myDataTypeCache;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public OverlayTransformer(Toolbox toolbox)
    {
        super(toolbox);

        myToolbox = toolbox;
        myMantleTb = toolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
        myImageProvider = new KMLImageProvider(toolbox.getDataRegistry());
        myImageOrderManager = toolbox.getOrderManagerRegistry().getOrderManager(DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY,
                DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY);
        myDataTypeCache = new SimpleCache<>(New.map(), myMantleTb.getDataTypeController()::getDataTypeInfoForType);

        getListenerService().addService(myImageOrderManager.getParticipantChangeListenerService(this::handleOrderChangeEvent));
    }

    @Override
    public void addFeatures(Collection<? extends KMLFeature> features, DataTypeInfo dataType)
    {
        Collection<Geometry> overlayGeoms = New.collection();
        List<KMLFeature> newFeatures = features.stream().filter(f -> f.getFeature() instanceof Overlay && f.getTile() == null)
                .collect(Collectors.toList());
        if (!newFeatures.isEmpty())
        {
            for (KMLFeature feature : newFeatures)
            {
                addOverlay(feature, overlayGeoms);
            }
            updateVisibility(newFeatures);
            CollectionUtilities.multiMapAddAll(getDataTypeKeyToFeatureMap(), dataType.getTypeKey(), newFeatures, false);
            publishGeometries(overlayGeoms, Collections.emptyList());
        }
    }

    @Override
    public void removeFeatures(Collection<? extends KMLFeature> features, String dataTypeKey)
    {
        Collection<KMLFeature> overlayFeatures = features.stream().filter(f -> f.getTile() != null).collect(Collectors.toSet());
        if (!overlayFeatures.isEmpty())
        {
            CollectionUtilities.multiMapRemoveAll(getDataTypeKeyToFeatureMap(), dataTypeKey, overlayFeatures);

            Collection<Geometry> overlayGeoms = overlayFeatures.stream().map(f -> f.getTile()).collect(Collectors.toList());
            publishGeometries(Collections.emptyList(), overlayGeoms);

            for (KMLFeature feature : overlayFeatures)
            {
                setVisibility(feature, false);
                feature.setTile(null);
            }
        }

        DefaultOrderParticipantKey orderKey = myOrderParticipants.remove(dataTypeKey);
        if (orderKey != null)
        {
            myImageOrderManager.deactivateParticipant(orderKey);
        }
    }

    @Override
    public void setOpacity(DataTypeInfo dataTypeInfo, int opacity)
    {
        List<KMLFeature> features = getDataTypeKeyToFeatureMap().get(dataTypeInfo.getTypeKey());
        if (features != null)
        {
            float fractionalOpacity = (float)opacity / MAX_OPACITY;
            for (KMLFeature feature : features)
            {
                feature.getTile().getRenderProperties().setOpacity(fractionalOpacity);
            }
        }
    }

    @Override
    protected void setVisibility(KMLFeature feature, boolean isVisible)
    {
        if (feature.getTile() != null)
        {
            feature.getTile().getRenderProperties().setHidden(!isVisible);
            publishToTimeline(Collections.singletonList(feature.getTile()), isVisible, feature.getDataSource().getDataTypeKey());
        }
    }

    /**
     * Handles a ParticipantOrderChangeEvent.
     *
     * @param event the event
     */
    private void handleOrderChangeEvent(ParticipantOrderChangeEvent event)
    {
        if (event.getChangeType() == ParticipantChangeType.ORDER_CHANGED)
        {
            event.getChangedParticipants().forEachEntry(this::updateOrders);
        }
    }

    /**
     * Create an overlay tile for a feature.
     *
     * @param feature The KML feature.
     * @param adds The collection to add the new tile to.
     * @return Whether a geometry was added
     */
    private boolean addOverlay(KMLFeature feature, Collection<Geometry> adds)
    {
        boolean wasAdded = false;

        KMLImageKey key;

        // Create the tile builder
        TileGeometry.Builder<Position> tileBuilder = null;
        if (feature.getFeature() instanceof GroundOverlay)
        {
            GeographicBoundingBox bbox = feature.getGeoBoundingBox();
            if (bbox == null)
            {
                return false;
            }
            int width = TOP_IMAGE_WIDTH_PIXELS;
            // ensure the image is DDS-compressible
            int height = MathUtil.roundUpTo((int)(bbox.getHeight() * width / bbox.getWidth()), 4);
            key = new GroundOverlayImageKey(feature, bbox, width, height);
            TileGeometry.Builder<Position> builder = new TileGeometry.Builder<>();
            builder.setBounds(bbox);
            GroundOverlay overlay = (GroundOverlay)feature.getFeature();
            String viewFormat = overlay.getIcon().getViewFormat();
            ViewRefreshMode viewRefreshMode = overlay.getIcon().getViewRefreshMode();
            boolean viewFormatHasBbox = viewFormat != null && viewFormat.contains("[bbox");
            if (viewRefreshMode == ViewRefreshMode.ON_STOP && viewFormat == null
                    || viewRefreshMode != null && viewRefreshMode != ViewRefreshMode.NEVER && viewFormatHasBbox)
            {
                builder.setDivider(new Divider(feature.getDataSource().getDataTypeKey()));
                builder.setMinimumDisplaySize((int)(width * MINIMUM_DISPLAY_SIZE_RATIO));
                builder.setMaximumDisplaySize(width);
            }
            tileBuilder = builder;
        }
        else if (feature.getFeature() instanceof ScreenOverlay)
        {
            key = new SimpleKMLImageKey(feature);

            // Set active to true allow the envoy to download the image
            if (feature.getResultingDataSource() != null)
            {
                feature.getResultingDataSource().setActive(true);
            }

            Image image = myImageProvider.getImage(key);
            if (image != null)
            {
                ScreenOverlay overlay = (ScreenOverlay)feature.getFeature();
                Viewer viewer = myToolbox.getMapManager().getStandardViewer();
                ToolbarManager toolbarManager = myToolbox.getUIRegistry().getToolbarComponentRegistry().getToolbarManager();
                TileGeometry.Builder<Position> builder = new TileGeometry.Builder<>();
                builder.setBounds(KMLSpatialTemporalUtils.calculateScreenBoundingBox(overlay, image, viewer, toolbarManager));
                tileBuilder = builder;
            }
            else
            {
                return wasAdded;
            }
        }
        else
        {
            return wasAdded;
        }
        tileBuilder.setImageManager(new ImageManager(key, myImageProvider));
        tileBuilder.setDataModelId(KMLDataElementProvider.ourUniqueTypeCounter.incrementAndGet());

        // Get the data type key
        String dataTypeKey = feature.getDataSource().getDataTypeKey();

        // Create the tile renderer properties and set z-order
        TileRenderProperties props = getTileRenderProperties(feature, dataTypeKey);

        // Create the constraints
        Constraints constraints = new Constraints(KMLSpatialTemporalUtils.getTimeConstraint(feature));

        // Create the tile geometry
        TileGeometry geom = new TileGeometry(tileBuilder, props, constraints);

        feature.setTile(geom);
        wasAdded = adds.add(geom);

        return wasAdded;
    }

    /**
     * Adds/removes the geometries from the timeline registry.
     *
     * @param geoms the geometries
     * @param isAdd true for add, false for remove
     * @param dataTypeKey the data type key
     */
    private void publishToTimeline(Collection<? extends Geometry> geoms, boolean isAdd, String dataTypeKey)
    {
        if (isAdd)
        {
            addToTimeline(geoms, dataTypeKey);
        }
        else
        {
            removeFromTimeline(geoms, dataTypeKey);
        }
    }

    /**
     * Adds the geometries to the timeline registry.
     *
     * @param geoms the geometries
     * @param dataTypeKey the data type key
     */
    private void addToTimeline(Collection<? extends Geometry> geoms, String dataTypeKey)
    {
        Collection<TimelineDatum> data = New.list(geoms.size());
        for (Geometry geom : geoms)
        {
            if (geom instanceof TileGeometry)
            {
                TileGeometry tileGeometry = (TileGeometry)geom;
                if (tileGeometry.getDataModelId() != -1 && tileGeometry.getConstraints().getTimeConstraint() != null)
                {
                    data.add(new StyledTimelineDatum(tileGeometry.getDataModelId(),
                            tileGeometry.getConstraints().getTimeConstraint().getTimeSpan()));
                }
            }
        }

        if (!data.isEmpty())
        {
            DataTypeInfo dataType;
            synchronized (myDataTypeCache)
            {
                dataType = myDataTypeCache.apply(dataTypeKey);
            }
            if (dataType != null)
            {
                TimelineRegistry timelineRegistry = myToolbox.getUIRegistry().getTimelineRegistry();
                timelineRegistry.addData(dataType.getOrderKey(), data);
            }
            else
            {
                LOGGER.error("Failed to add timeline data - no data type found for " + dataTypeKey);
            }
        }
    }

    /**
     * Removes the geometries from the timeline registry.
     *
     * @param geoms the geometries
     * @param dataTypeKey the data type key
     */
    private void removeFromTimeline(Collection<? extends Geometry> geoms, String dataTypeKey)
    {
        Collection<Long> ids = New.list(geoms.size());
        for (Geometry geom : geoms)
        {
            if (geom instanceof TileGeometry)
            {
                TileGeometry tileGeometry = (TileGeometry)geom;
                if (tileGeometry.getDataModelId() != -1)
                {
                    ids.add(Long.valueOf(tileGeometry.getDataModelId()));
                }
            }
        }

        if (!ids.isEmpty())
        {
            DataTypeInfo dataType;
            synchronized (myDataTypeCache)
            {
                dataType = myDataTypeCache.apply(dataTypeKey);
            }
            if (dataType != null)
            {
                TimelineRegistry timelineRegistry = myToolbox.getUIRegistry().getTimelineRegistry();
                timelineRegistry.removeData(dataType.getOrderKey(), ids);
            }
            else
            {
                LOGGER.error("Failed to remove timeline data - no data type found for " + dataTypeKey);
            }
        }
    }

    /**
     * Get the tile render properties for a feature.
     *
     * @param feature The feature.
     * @param dataTypeKey The data type key.
     * @return The render properties.
     */
    private TileRenderProperties getTileRenderProperties(KMLFeature feature, String dataTypeKey)
    {
        TileRenderProperties props;
        if (StringUtils.isNotEmpty(dataTypeKey) && myMantleTb != null)
        {
            // Set z-order
            int zorder = 0;
            int renderOrder = 0;
            if (feature.getFeature() instanceof GroundOverlay)
            {
                DefaultOrderParticipantKey orderKey = new DefaultOrderParticipantKey(
                        DefaultOrderCategory.DEFAULT_IMAGE_LAYER_FAMILY, DefaultOrderCategory.IMAGE_OVERLAY_CATEGORY,
                        dataTypeKey);
                myOrderParticipants.put(dataTypeKey, orderKey);
                zorder = myImageOrderManager.activateParticipant(orderKey);
                renderOrder = ((GroundOverlay)feature.getFeature()).getDrawOrder();
            }

            props = new DefaultTileRenderProperties(zorder, true, false);
            props.setRenderingOrder(renderOrder);

            // Set hidden to true so the tile isn't loaded as soon as it is
            // published.
            props.setHidden(true);

            // Set opacity
            int opacity;
            Color color = KMLSpatialTemporalUtils.convertColor(((Overlay)feature.getFeature()).getColor());
            if (color != null)
            {
                opacity = color.getAlpha();
            }
            else
            {
                opacity = myMantleTb.getDataTypeInfoPreferenceAssistant().getOpacityPreference(dataTypeKey, MAX_OPACITY);
            }
            props.setOpacity((float)opacity / MAX_OPACITY);
        }
        else
        {
            props = new DefaultTileRenderProperties(0, true, false);
        }
        return props;
    }

    /**
     * Updates the tile geometries with the new order.
     *
     * @param participant the participant key
     * @param order the new order
     * @return whether the order was updated
     */
    private boolean updateOrders(OrderParticipantKey participant, int order)
    {
        List<KMLFeature> features = getDataTypeKeyToFeatureMap().get(participant.getId());
        if (features == null)
        {
            return true;
        }

        Collection<TileGeometry> removes = New.collection();
        Collection<TileGeometry> adds = New.collection();
        for (KMLFeature feature : features)
        {
            TileGeometry tile = feature.getTile();
            if (tile != null)
            {
                // get the tile, clone it and replace it.
                removes.add(tile);
                Builder<Position> tileBuilder = tile.createBuilder();
                TileRenderProperties props = tile.getRenderProperties().clone();
                ((DefaultZOrderRenderProperties)props).setZOrder(order);
                TileGeometry newTile = new TileGeometry(tileBuilder, props, tile.getConstraints());
                adds.add(newTile);

                feature.setTile(newTile);
            }
        }

        publishGeometries(adds, removes);
        return true;
    }

    /** Base class for KML image keys. */
    private abstract static class AbstractKMLImageKey implements KMLImageKey
    {
        /** The KMLFeature associated with the overlay. */
        private KMLFeature myKMLFeature;

        /**
         * Constructor.
         *
         * @param feature The associated KMLFeature.
         */
        protected AbstractKMLImageKey(KMLFeature feature)
        {
            if (Utilities.checkNull(feature, "feature").getFeature() instanceof Overlay)
            {
                myKMLFeature = feature;
            }
            else
            {
                throw new IllegalArgumentException("Feature must be an Overlay");
            }
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            return myKMLFeature.equals(((AbstractKMLImageKey)obj).myKMLFeature);
        }

        @Override
        public KMLDataSource getDataSource()
        {
            return getFeature().getDataSource();
        }

        @Override
        public KMLFeature getFeature()
        {
            return myKMLFeature;
        }

        @Override
        public String getURL()
        {
            final String path;
            if (getFeature().getResultingDataSource() == null)
            {
                path = StringUtilities.trim(getOverlay().getIcon().getHref());
            }
            else
            {
                path = getFeature().getResultingDataSource().getActualPath();
            }

            return path;
        }

        @Override
        public boolean hasExtension(String string)
        {
            return getURL().endsWith(string);
        }

        @Override
        public int hashCode()
        {
            return 31 + myKMLFeature.hashCode();
        }

        /**
         * Get the Overlay.
         *
         * @return The Overlay.
         */
        protected Overlay getOverlay()
        {
            return (Overlay)getFeature().getFeature();
        }
    }

    /** Divider for ground overlay tiles. */
    private final class Divider extends AbstractDivider<Position>
    {
        /**
         * Constructor.
         *
         * @param dividerUniqueKey The key for the divider.
         */
        public Divider(String dividerUniqueKey)
        {
            super(dividerUniqueKey);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<AbstractTileGeometry<?>> divide(AbstractTileGeometry<?> parent)
        {
            Collection<AbstractTileGeometry<?>> result = New.list(4);
            GeographicBoundingBox bbox = (GeographicBoundingBox)parent.getBounds();

            GroundOverlayImageKey parentKey = (GroundOverlayImageKey)parent.getImageManager().getImageKey();
            for (GeographicBoundingBox aBox : bbox.quadSplit())
            {
                int width = SUB_IMAGE_WIDTH_PIXELS;
                // ensure the image is DDS-compressible
                int height = MathUtil.roundUpTo((int)(aBox.getHeight() * width / aBox.getWidth()), 4);
                KMLImageKey key = parentKey.derive(aBox, width, height);

                TileGeometry.Builder<Position> builder = (Builder<Position>)parent.createBuilder();
                builder.setParent(parent);
                builder.setBounds(aBox);
                builder.setDivider(this);
                builder.setImageManager(
                        new ImageManager(key, (ImageProvider<Object>)parent.getImageManager().getImageProvider()));

                builder.setMinimumDisplaySize((int)(width * MINIMUM_DISPLAY_SIZE_RATIO));
                builder.setMaximumDisplaySize(width);

                result.add(new TileGeometry(builder, ((TileGeometry)parent).getRenderProperties(),
                        ((TileGeometry)parent).getConstraints()));
            }

            return result;
        }
    }

    /** Image key for ground overlay image providers. */
    private static final class GroundOverlayImageKey extends AbstractKMLImageKey
    {
        /** The bounding box for this image. */
        private final GeographicBoundingBox myBoundingBox;

        /** The height of the image in pixels. */
        private final int myHeight;

        /** The width of the image in pixels. */
        private final int myWidth;

        /**
         * Constructor.
         *
         * @param feature The feature.
         * @param bbox The bounding box for the image.
         * @param width The width of the image in pixels.
         * @param height The height of the image in pixels.
         */
        public GroundOverlayImageKey(KMLFeature feature, GeographicBoundingBox bbox, int width, int height)
        {
            super(feature);
            myBoundingBox = Utilities.checkNull(bbox, "bbox");
            myWidth = width;
            myHeight = height;
        }

        /**
         * Create a new image key like me except with a new bounding box.
         *
         * @param aBox The new bounding box.
         * @param width The width in pixels.
         * @param height The height in pixels.
         * @return The new image key.
         */
        public GroundOverlayImageKey derive(GeographicBoundingBox aBox, int width, int height)
        {
            return new GroundOverlayImageKey(getFeature(), aBox, width, height);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (!super.equals(obj) || getClass() != obj.getClass())
            {
                return false;
            }
            GroundOverlayImageKey other = (GroundOverlayImageKey)obj;
            return myBoundingBox.equals(other.myBoundingBox) && myHeight == other.myHeight && myWidth == other.myWidth;
        }

        @Override
        public String getURL()
        {
            String url = super.getURL();
            ViewRefreshMode viewRefreshMode = getOverlay().getIcon().getViewRefreshMode();
            if (viewRefreshMode != null && viewRefreshMode != ViewRefreshMode.NEVER)
            {
                url = KMLLinkHelper.addURLParameters(getDataSource(), url, myBoundingBox, myWidth, myHeight);
            }
            return url;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + myBoundingBox.hashCode();
            result = prime * result + myHeight;
            result = prime * result + myWidth;
            return result;
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + "[url:" + getURL() + " bbox:" + myBoundingBox + "]";
        }
    }

    /** Interface for keys used in {@link KMLImageProvider}s. */
    private interface KMLImageKey
    {
        /**
         * Get the data source to use to get the image.
         *
         * @return The data source.
         */
        KMLDataSource getDataSource();

        /**
         * Get the KMLFeature.
         *
         * @return The KMLFeature.
         */
        KMLFeature getFeature();

        /**
         * Get the URL for this image key.
         *
         * @return The URL that can be used to retrieve the image.
         */
        String getURL();

        /**
         * Determine if this image key has a particular file extension.
         *
         * @param string The extension.
         * @return {@code true} if the extension was found.
         */
        boolean hasExtension(String string);
    }

    /** Image provider for KML. */
    private static class KMLImageProvider implements ImageProvider<KMLImageKey>
    {
        /** The data registry. */
        private final DataRegistry myDataRegistry;

        /**
         * Constructor.
         *
         * @param dataRegistry The data registry
         */
        public KMLImageProvider(DataRegistry dataRegistry)
        {
            myDataRegistry = dataRegistry;
        }

        @Override
        public Image getImage(KMLImageKey key)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Requesting image for " + key);
            }

            Image returnImage = null;
            try (InputStream inputStream = KMLDataRegistryHelper.queryAndReturn(myDataRegistry, key.getDataSource(),
                    key.getURL()))
            {
                if (inputStream != null)
                {
                    returnImage = Image.read(inputStream);
                }
                else
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(StringUtilities.concat("Image missing from cache: ", key));
                    }
                }
            }
            catch (ClosedByInterruptException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Request for image was interrupted: " + e, e);
                }
            }
            catch (IOException e)
            {
                // Error reading image from cache
                LOGGER.error(StringUtilities.concat("Cached image not readable: ", key, e), e);
            }
            catch (ImageFormatUnknownException e)
            {
                LOGGER.error("Image format unrecognized: " + e, e);
            }

            if (returnImage != null)
            {
                returnImage.setCompressionHint(CompressionType.D3DFMT_A8R8G8B8);
            }
            return returnImage;
        }
    }

    /** A simple image key that just has a static path. */
    private static class SimpleKMLImageKey extends AbstractKMLImageKey
    {
        /**
         * Constructor.
         *
         * @param feature The KMLFeature.
         */
        public SimpleKMLImageKey(KMLFeature feature)
        {
            super(feature);
        }
    }
}
