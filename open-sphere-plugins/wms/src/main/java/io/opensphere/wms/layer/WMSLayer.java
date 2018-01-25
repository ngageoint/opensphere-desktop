package io.opensphere.wms.layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.api.AbstractModel;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Utilities;
import io.opensphere.wms.config.v1.WMSLayerConfig;

/**
 * Model for a WMS layer.
 */
@SuppressWarnings("PMD.GodClass")
public class WMSLayer extends AbstractModel implements ImageProvider<TileImageKey>
{
    /** Property descriptor used for the data registry. */
    public static final PropertyDescriptor<WMSLayer> PROPERTY_DESCRIPTOR = new PropertyDescriptor<WMSLayer>("value",
            WMSLayer.class);

    /** Property descriptor for keys used in the data registry. */
    public static final PropertyDescriptor<String> KEY_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("key", String.class);

    /** Height of the world in degrees. */
    private static final double WORLD_HEIGHT = 180.;

    /** Width of the world in degrees. */
    private static final double WORLD_WIDTH = 360.;

    /** The DataTypeInfo for the layer. */
    private final WMSDataTypeInfo myDataTypeInfo;

    /** The image provider. */
    private ImageProvider<TileImageKey> myImageProvider;

    /** The cell dimensions for each level of detail, starting with level 0. */
    private final List<Vector2d> myLayerCellDimensions;

    /** The maximum display size. */
    private final int myMaximumDisplaySize;

    /** The minimum display size. */
    private final int myMinimumDisplaySize;

    /** The valid duration. */
    private final Duration myValidDuration;

    /**
     * Create a list of cell dimensions, one for each level of detail, each
     * one-quarter the size of the previous level.
     *
     * @param numLevels The number of levels.
     * @param levelZeroSizeD The size of level zero in degrees.
     * @return The list of cell dimensions.
     */
    public static List<Vector2d> createQuadSplitLayerCellDimensions(int numLevels, double levelZeroSizeD)
    {
        List<Vector2d> layerCellDimensions = new ArrayList<>(numLevels);
        double cellSize = levelZeroSizeD;
        for (int i = 0; i < numLevels; i++)
        {
            layerCellDimensions.add(new Vector2d(cellSize, cellSize));
            cellSize *= .5;
        }
        return layerCellDimensions;
    }

    /**
     * Get an accessor to go with the {@link #KEY_PROPERTY_DESCRIPTOR} that
     * takes a {@link WMSLayer}.
     *
     * @return The accessor.
     */
    public static PropertyAccessor<WMSLayer, String> getKeyAccessor()
    {
        return new SerializableAccessor<WMSLayer, String>(KEY_PROPERTY_DESCRIPTOR)
        {
            @Override
            public String access(WMSLayer input)
            {
                return input.getTypeInfo().getTypeKey();
            }
        };
    }

    /**
     * Construct the layer.
     *
     * @param builder The builder.
     */
    public WMSLayer(Builder builder)
    {
        checkConfig(builder);
        myDataTypeInfo = builder.getDataTypeInfo();
        ArrayList<Vector2d> dimensions = new ArrayList<>(builder.getLayerCellDimensions());
        // Comparator that orders coordinates such that smaller ones come first.
        Collections.sort(dimensions, Vector2d.LENGTH_COMPARATOR);
        myLayerCellDimensions = Collections.unmodifiableList(dimensions);
        myImageProvider = builder.getImageProvider();
        myMinimumDisplaySize = builder.getMinimumDisplaySize();
        myMaximumDisplaySize = builder.getMaximumDisplaySize();
        myValidDuration = builder.getValidDuration();
    }

    /**
     * Generate a grid of bounding boxes at the specified level of detail.
     *
     * @param level The level of detail.
     * @return A collection of bounding boxes.
     */
    public Collection<GeographicBoundingBox> generateFixedGrid(int level)
    {
        Vector2d cellDimensions = myLayerCellDimensions.get(level);

        final double minLat;
        final double minLon;
        final double maxLat;
        final double maxLon;
        GeographicBoundingBox bbox = myDataTypeInfo.getWmsConfig().getLayerConfig().getBoundingBoxConfig()
                .getGeographicBoundingBox();
        if (bbox == null || bbox.getDeltaLonD() > WORLD_WIDTH || bbox.getDeltaLatD() > WORLD_HEIGHT)
        {
            minLat = -WORLD_HEIGHT * 0.5;
            minLon = -WORLD_WIDTH * 0.5;
            maxLat = WORLD_HEIGHT * 0.5;
            maxLon = WORLD_WIDTH * 0.5;
        }
        else
        {
            minLat = bbox.getLowerLeft().getLatLonAlt().getLatD();
            minLon = bbox.getLowerLeft().getLatLonAlt().getLonD();
            maxLat = bbox.getUpperRight().getLatLonAlt().getLatD();
            maxLon = bbox.getUpperRight().getLatLonAlt().getLonD();
        }

        double lat1 = Math.floor((minLat + WORLD_HEIGHT * .5) / cellDimensions.getY()) * cellDimensions.getY()
                - WORLD_HEIGHT * .5;
        double lastLat = Math.ceil((maxLat + WORLD_HEIGHT * .5) / cellDimensions.getY()) * cellDimensions.getY()
                - WORLD_HEIGHT * .5;

        double baseLon = Math.floor((minLon + WORLD_WIDTH * .5) / cellDimensions.getX()) * cellDimensions.getX()
                - WORLD_WIDTH * .5;
        double lastLon = Math.ceil((maxLon + WORLD_WIDTH * .5) / cellDimensions.getX()) * cellDimensions.getX()
                - WORLD_WIDTH * .5;

        // Make sure we haven't exceed the map bounds
        lat1 = Math.max(lat1, -WORLD_HEIGHT * 0.5);
        baseLon = Math.max(baseLon, -WORLD_WIDTH * 0.5);

        int cols = (int)Math.round((lastLon - baseLon) / cellDimensions.getX());
        int rows = (int)Math.round((lastLat - lat1) / cellDimensions.getY());

        List<GeographicBoundingBox> results = new ArrayList<>(rows * cols);

        for (int row = 0; row < rows; row++)
        {
            double lat2 = lat1 + cellDimensions.getY();
            double lon1 = baseLon;
            for (int col = 0; col < cols; col++)
            {
                double lon2 = lon1 + cellDimensions.getX();
                LatLonAlt lowerLeft = LatLonAlt.createFromDegrees(lat1, lon1);
                LatLonAlt upperRight = LatLonAlt.createFromDegrees(lat2, lon2);
                results.add(new GeographicBoundingBox(lowerLeft, upperRight));
                lon1 = lon2;
            }
            lat1 = lat2;
        }

        return results;
    }

    /**
     * Generate a grid of bounding boxes at the specified level of detail.
     *
     * @param level The level of detail.
     * @return A collection of bounding boxes.
     */
    public Collection<GeographicBoundingBox> generateGrid(int level)
    {
        Vector2d cellDimensions = myLayerCellDimensions.get(level);

        double height;
        double width;
        final double minLat;
        final double minLon;
        final double maxLat;
        final double maxLon;
        GeographicBoundingBox bbox = myDataTypeInfo.getWmsConfig().getLayerConfig().getBoundingBoxConfig()
                .getGeographicBoundingBox();
        if (bbox == null || bbox.getDeltaLonD() > WORLD_WIDTH || bbox.getDeltaLatD() > WORLD_HEIGHT)
        {
            height = WORLD_HEIGHT;
            width = WORLD_WIDTH;
            minLat = -height * 0.5;
            minLon = -width * 0.5;
            maxLat = height * 0.5;
            maxLon = width * 0.5;
        }
        else
        {
            height = bbox.getDeltaLatD();
            width = bbox.getDeltaLonD();
            minLat = bbox.getLowerLeft().getLatLonAlt().getLatD();
            minLon = bbox.getLowerLeft().getLatLonAlt().getLonD();
            maxLat = bbox.getUpperRight().getLatLonAlt().getLatD();
            maxLon = bbox.getUpperRight().getLatLonAlt().getLonD();
        }

        int rows = (int)Math.ceil(height / cellDimensions.getY());
        int cols = (int)Math.ceil(width / cellDimensions.getX());
        List<GeographicBoundingBox> results = new ArrayList<>(rows * cols);
        double lat1 = minLat;
        for (int row = 0; row < rows;)
        {
            double lat2 = minLat + cellDimensions.getY() * ++row;
            if (lat2 > maxLat)
            {
                lat2 = maxLat;
            }
            double lon1 = minLon;
            for (int col = 0; col < cols;)
            {
                double lon2 = minLon + cellDimensions.getX() * ++col;
                if (lon2 > maxLon)
                {
                    lon2 = maxLon;
                }
                LatLonAlt lowerLeft = LatLonAlt.createFromDegrees(lat1, lon1);
                LatLonAlt upperRight = LatLonAlt.createFromDegrees(lat2, lon2);
                results.add(new GeographicBoundingBox(lowerLeft, upperRight));
                lon1 = lon2;
            }
            lat1 = lat2;
        }

        return results;
    }

    /**
     * Get the configuration for this layer.
     *
     * @return the configuration.
     */
    public WMSLayerConfig getConfiguration()
    {
        return myDataTypeInfo.getWmsConfig().getLayerConfig().clone();
    }

    @Override
    public Image getImage(TileImageKey key)
    {
        if (myImageProvider == null)
        {
            throw new IllegalStateException("getImage called before image provider has been set.");
        }
        return myImageProvider.getImage(key);
    }

    /**
     * Get the image provider for this layer.
     *
     * @return The image provider.
     */
    public ImageProvider<TileImageKey> getImageProvider()
    {
        return myImageProvider;
    }

    /**
     * Get the approximate maximum number of pixels this geometry should occupy
     * before it is split. This value is ignored if the geometry is not
     * divisible.
     *
     * @return The maximumDisplaySize.
     */
    public int getMaximumDisplaySize()
    {
        return myMaximumDisplaySize;
    }

    /**
     * Get the approximate minimum number of pixels this geometry should occupy
     * before it is joined. This value is ignored if the geometry has no parent.
     *
     * @return The minimumDisplaySize.
     */
    public int getMinimumDisplaySize()
    {
        return myMinimumDisplaySize;
    }

    /**
     * Get the size of the grid for the highest level of detail.
     *
     * @return The size of the smallest grid square.
     */
    public Vector2d getMinimumGridSize()
    {
        return myLayerCellDimensions.get(myLayerCellDimensions.size() - 1);
    }

    /**
     * Get the time span that this layer is valid.
     *
     * @return The time span for the layer.
     */
    public TimeSpan getTimeSpan()
    {
        // TODO: Get the timespan from the capabilities document.
        return myDataTypeInfo.getWmsConfig().getLayerConfig().getTimeExtent();
    }

    /**
     * Get the title of the layer.
     *
     * @return The title of the layer.
     */
    public String getTitle()
    {
        return myDataTypeInfo.getDisplayName();
    }

    /**
     * Gets the type info.
     *
     * @return the type info
     */
    public WMSDataTypeInfo getTypeInfo()
    {
        return myDataTypeInfo;
    }

    /**
     * Get the amount of time that this layer is valid.
     *
     * @return The duration.
     */
    public Duration getValidDuration()
    {
        return myValidDuration;
    }

    /**
     * Checks if is timeless.
     *
     * @return true, if is timeless
     */
    public boolean isTimeless()
    {
        return myDataTypeInfo.getWmsConfig().getLayerConfig().getTimeExtent() == TimeSpan.TIMELESS;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getTitle().length() + 19);
        sb.append(WMSLayer.class.getSimpleName()).append(" [myTitle=").append(getTitle()).append(']');
        return sb.toString();
    }

    /**
     * Check to make sure that the configuration has everything set.
     *
     * @param builder The builder for the layer.
     */
    protected final void checkConfig(Builder builder)
    {
        if (builder.myDataTypeInfo.getWmsConfig() == null)
        {
            throw new IllegalArgumentException("layer configuration cannot be null");
        }
        if (builder.getLayerCellDimensions() == null)
        {
            throw new IllegalArgumentException("layerCellDimensions cannot be null");
        }
        WMSLayerConfig conf = builder.myDataTypeInfo.getWmsConfig().getLayerConfig();

        if (conf.getLayerTitle() == null)
        {
            throw new IllegalArgumentException("title cannot be null");
        }
        if (conf.getGetMapConfig().getImageFormat() == null)
        {
            throw new IllegalArgumentException("imageFormat cannot be null");
        }
        if (conf.getGetMapConfig().getSRS() == null)
        {
            throw new IllegalArgumentException("srs cannot be null");
        }
        if (conf.getBoundingBoxConfig() != null && conf.getBoundingBoxConfig().getGeographicBoundingBox() == null)
        {
            throw new IllegalArgumentException("bbox cannot be null");
        }
    }

    /**
     * Get the overall bounding box for this layer.
     *
     * @return The bounding box.
     */
    protected GeographicBoundingBox getBoundingBox()
    {
        return myDataTypeInfo.getWmsConfig().getLayerConfig().getBoundingBoxConfig().getGeographicBoundingBox();
    }

    /**
     * Get the list containing the cell dimensions of each level of detail,
     * starting with level 0.
     *
     * @return The cell dimensions.
     */
    protected List<Vector2d> getLayerCellDimensions()
    {
        return myLayerCellDimensions;
    }

    /**
     * Set the image provider for the layer.
     *
     * @param imageProvider The image provider.
     */
    protected final void setImageProvider(ImageProvider<TileImageKey> imageProvider)
    {
        myImageProvider = imageProvider;
    }

    /** Builder class to aid in construction of WMSLayer. */
    public static class Builder
    {
        /** DataTypeInfo for this layer. */
        private final WMSDataTypeInfo myDataTypeInfo;

        /** The image provider. */
        private ImageProvider<TileImageKey> myImageProvider;

        /**
         * The cell dimensions for each level of detail, starting with level 0.
         */
        private List<Vector2d> myLayerCellDimensions;

        /** The maximum display size. */
        private int myMaximumDisplaySize = Integer.MAX_VALUE;

        /** The minimum display size. */
        private int myMinimumDisplaySize;

        /** The duration that the layer is valid. */
        private Duration myValidDuration = new Milliseconds(Long.MAX_VALUE);

        /**
         * Construct me.
         *
         * @param info The layer's DataTypeInfo.
         */
        public Builder(WMSDataTypeInfo info)
        {
            myDataTypeInfo = info;
        }

        /**
         * Accessor for the DataTypeInfo.
         *
         * @return The DataTypeInfo.
         */
        public WMSDataTypeInfo getDataTypeInfo()
        {
            return myDataTypeInfo;
        }

        /**
         * Accessor for the imageProvider.
         *
         * @return The imageProvider.
         */
        public ImageProvider<TileImageKey> getImageProvider()
        {
            return myImageProvider;
        }

        /**
         * Accessor for the layerCellDimensions.
         *
         * @return The layerCellDimensions.
         */
        public List<Vector2d> getLayerCellDimensions()
        {
            return myLayerCellDimensions;
        }

        /**
         * Get the approximate maximum number of pixels this geometry should
         * occupy before it is split. This value is ignored if the geometry is
         * not divisible.
         *
         * @return The maximumDisplaySize.
         */
        public int getMaximumDisplaySize()
        {
            return myMaximumDisplaySize;
        }

        /**
         * Get the approximate minimum number of pixels this geometry should
         * occupy before it is joined. This value is ignored if the geometry has
         * no parent.
         *
         * @return The minimumDisplaySize.
         */
        public int getMinimumDisplaySize()
        {
            return myMinimumDisplaySize;
        }

        /**
         * Get the valid duration.
         *
         * @return The valid duration.
         */
        public Duration getValidDuration()
        {
            return myValidDuration;
        }

        /**
         * Mutator for the image provider.
         *
         * @param imageProvider The imageProvider to set.
         */
        public void setImageProvider(ImageProvider<TileImageKey> imageProvider)
        {
            myImageProvider = imageProvider;
        }

        /**
         * Mutator for the layerCellDimensions.
         *
         * @param layerCellDimensions The layerCellDimensions to set.
         */
        public void setLayerCellDimensions(List<Vector2d> layerCellDimensions)
        {
            myLayerCellDimensions = layerCellDimensions;
        }

        /**
         * Set the approximate maximum number of pixels this geometry should
         * occupy before it is split. This value is ignored if the geometry is
         * not divisible.
         *
         * @param maximumDisplaySize The maximumDisplaySize to set.
         */
        public void setMaximumDisplaySize(int maximumDisplaySize)
        {
            myMaximumDisplaySize = maximumDisplaySize;
        }

        /**
         * Set the approximate minimum number of pixels this geometry should
         * occupy before it is joined. This value is ignored if the geometry has
         * no parent.
         *
         * @param minimumDisplaySize The minimumDisplaySize to set.
         */
        public void setMinimumDisplaySize(int minimumDisplaySize)
        {
            myMinimumDisplaySize = minimumDisplaySize;
        }

        /**
         * Set the valid duration.
         *
         * @param validDuration The valid duration to set.
         */
        public void setValidDuration(Duration validDuration)
        {
            myValidDuration = Utilities.checkNull(validDuration, "validDuration");
        }
    }
}
