package io.opensphere.wms;

import java.util.List;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import io.opensphere.wms.config.v1.WMSBoundingBoxConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerGetMapConfig;
import io.opensphere.wms.layer.TileImageKey;
import io.opensphere.wms.layer.WMSDataTypeInfo;
import io.opensphere.wms.layer.WMSLayer;

/**
 * A WMS Layer that is loaded from the file system.
 */
public class FileWMSLayer extends WMSLayer
{
    /** The image provider. */
    private final ImageProvider<LevelRowCol> myLRCImageProvider;

    /**
     * Construct a layer.
     *
     * @param builder The builder.
     */
    public FileWMSLayer(Builder builder)
    {
        super(builder);

        if (builder.getLRCImageProvider() == null)
        {
            throw new IllegalArgumentException("lrcImageProvider cannot be null");
        }
        myLRCImageProvider = builder.getLRCImageProvider();
        setImageProvider(new ImageProvider<TileImageKey>()
        {
            @Override
            public Image getImage(TileImageKey key)
            {
                GeographicBoundingBox gbb = key.getBoundingBox();
                Vector3d dim = gbb.getDimensions();
                Vector2d gridSize = new Vector2d(dim.getX(), dim.getY());
                LevelRowCol coords = getGridCoordinates(gbb.getLowerLeft().getLatLonAlt(), gridSize);
                Image result = myLRCImageProvider.getImage(coords);
                return result;
            }
        });
    }

    /**
     * Get the level/row/column coordinates for some given lat/lon/alt
     * coordinates.
     *
     * @param coord The input coordinates.
     * @param gridSize The grid size.
     * @return The level/row/column of the matching grid square.
     */
    protected LevelRowCol getGridCoordinates(LatLonAlt coord, Vector2d gridSize)
    {
        int level = 0;
        List<Vector2d> layerCellDimensions = getLayerCellDimensions();
        while (level < layerCellDimensions.size() - 1
                && layerCellDimensions.get(level).getX() - MathUtil.DBL_EPSILON > gridSize.getX())
        {
            level++;
        }
        Vector2d dim = layerCellDimensions.get(level);
        GeographicBoundingBox boundingBox = getBoundingBox();
        LatLonAlt lowerLeft = boundingBox.getLowerLeft().getLatLonAlt();
        double minLat = lowerLeft.getLatD();
        double minLon = lowerLeft.getLonD();

        int row = (int)((coord.getLatD() - minLat) / dim.getY());
        int col = (int)((coord.getLonD() - minLon) / dim.getX());
        return new LevelRowCol(level, row, col);
    }

    /**
     * Accessor for the image provider.
     *
     * @return The image provider.
     */
    protected ImageProvider<LevelRowCol> getLRCImageProvider()
    {
        return myLRCImageProvider;
    }

    /** Builder class to aid in construction. */
    public static class Builder extends WMSLayer.Builder
    {
        /** The image provider. */
        private ImageProvider<LevelRowCol> myLRCImageProvider;

        /**
         * Constructor.
         *
         * @param conf Configuration for the layer.
         */
        public Builder(WMSDataTypeInfo conf)
        {
            super(conf);
            WMSLayerConfig layerConfig = conf.getWmsConfig().getLayerConfig();
            WMSLayerGetMapConfig getMapConfig = layerConfig.getGetMapConfig();
            if (getMapConfig.getSRS() == null)
            {
                getMapConfig.setSRS("");
            }
            if (getMapConfig.getImageFormat() == null)
            {
                getMapConfig.setImageFormat(layerConfig.getCacheImageFormat());
            }
            if (layerConfig.getBoundingBoxConfig() == null)
            {
                WMSBoundingBoxConfig bboxConf = new WMSBoundingBoxConfig();
                bboxConf.setGeographicBoundingBox(GeographicBoundingBox.WHOLE_GLOBE);
                layerConfig.setBoundingBoxConfig(bboxConf);
            }
        }

        /**
         * Accessor for the image provider.
         *
         * @return The image provider.
         */
        public ImageProvider<LevelRowCol> getLRCImageProvider()
        {
            return myLRCImageProvider;
        }

        /**
         * Mutator for the image provider.
         *
         * @param imageProvider The imageProvider to set.
         */
        public void setLRCImageProvider(ImageProvider<LevelRowCol> imageProvider)
        {
            myLRCImageProvider = imageProvider;
        }
    }
}
