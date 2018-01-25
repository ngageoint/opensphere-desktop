package io.opensphere.lidar.elevation;

import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.terrain.util.ElevationImageReader;
import io.opensphere.core.terrain.util.ElevationImageReaderException;
import io.opensphere.lidar.util.GeotiffFileReader;

/** Elevation provider for file based LiDAR data. */
public class LidarElevationReader implements ElevationImageReader
{
    /** The bounds over which this reader provides data. */
    private GeographicBoundingBox myBoundingBox;

    /** The CRS which matches the values returned by this reader. */
    private String myCRS;

    /**
     * The key used to determine the participant in the order manager with which
     * this reader is associated.
     */
    private String myElevationOrderId;

    /**
     * The value given when no data is available. This is not an indication of a
     * failure to read the value, for this value to be returned, it must be
     * present in the image.
     */
    private double myMissingDataValue;

    /**
     * Constructor.
     *
     * @param bounds The bounds over which this reader provides data.
     * @param missingDataValue The value given when no data is available.
     * @param crs The CRS which matches the values returned by this reader.
     * @param orderId key used to determine the participant in the order manager
     *            with which this reader is associated.
     */
    public LidarElevationReader(GeographicBoundingBox bounds, double missingDataValue, String crs, String orderId)
    {
        myBoundingBox = bounds;
        myMissingDataValue = missingDataValue;
        myCRS = crs;
        myElevationOrderId = orderId;
    }

    @Override
    public GeographicBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    @Override
    public String getCRS()
    {
        return myCRS;
    }

    @Override
    public String getElevationOrderId()
    {
        return myElevationOrderId;
    }

    @Override
    public double getMissingDataValue()
    {
        return myMissingDataValue;
//        return myReader.getDataSetSampler().getNoDataValue();
    }

    @Override
    public String getImageFormat()
    {
        return "geotiff";
    }

    @Override
    public void init(GeographicBoundingBox bounds, double missingDataValue, String crs, String orderId)
    {
        myBoundingBox = bounds;
        myMissingDataValue = missingDataValue;
        myCRS = crs;
        myElevationOrderId = orderId;
    }

    @Override
    public double readElevation(GeographicPosition position, Image image, GeographicBoundingBox bounds, boolean approximate)
        throws ElevationImageReaderException
    {
        double elevation = getMissingDataValue();
        GeotiffFileReader reader = LidarImageCache.getInstance().getReader(myElevationOrderId, image, bounds);
        if (reader != null)
        {
            LatLonAlt lla = position.getLatLonAlt();
            elevation = reader.getDataSetSampler().sampleGeo(lla.getLatD(), lla.getLonD(), approximate);
        }

        return elevation;
    }
}
