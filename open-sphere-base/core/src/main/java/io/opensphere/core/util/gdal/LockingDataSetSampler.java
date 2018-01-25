package io.opensphere.core.util.gdal;

import org.apache.log4j.Logger;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osrConstants;

import io.opensphere.core.math.Matrix2d;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.lang.Pair;

/**
 * A helper for synchronizing access to the {@link Dataset} which backs this
 * sampler.
 */
public class LockingDataSetSampler
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LockingDataSetSampler.class);

    /** The GDAL data set read from the file. */
    private final Dataset myDataset;

    /**
     * The matrix which transforms from the data set's native reference system
     * into pixel coordinates (without translation).
     */
    private final Matrix2d myInverseRotateAndScale;

    /**
     * The transform which converts from WGS84 into the data set's native
     * reference system.
     */
    private final CoordinateTransformation myInverseTransformation;

    /** When this value is read, it should be treated as missing data. */
    private final float myNoDataValue;

    /**
     * The matrix which transforms from pixel coordinates into the data set's
     * native reference system (without translation).
     */
    private final Matrix2d myRotateAndScale;

    /**
     * The transform which converts from the data set's native reference system
     * into WGS84.
     */
    private final CoordinateTransformation myTransformation;

    /**
     * Indicates if we should use the inverse transform or not.
     */
    private final boolean myUseInverseTransform;

    /**
     * The x coordinate offset for converting between the data set's native
     * reference system and pixel coordinates.
     */
    private final double myXOffset;

    /**
     * The x scale to use when transforming longitude into pixel coordinates.
     */
    private double myXScale;

    /**
     * The y coordinate offset for converting between the data set's native
     * reference system and pixel coordinates.
     */
    private final double myYOffset;

    /**
     * The y scale to use when transforming latitude into pixel coordinates.
     */
    private double myYScale;

    /**
     * Constructor.
     *
     * @param dataset the {@link Dataset} which backs this sampler.
     * @param isNorthOriented False if the inverse transform should be used,
     *            True if we should assume the image is north oriented.
     */
    public LockingDataSetSampler(Dataset dataset, boolean isNorthOriented)
    {
        final float defaultNoData = -10000f;
        myDataset = dataset;
        myUseInverseTransform = !isNorthOriented;
        if (myDataset == null)
        {
            LOGGER.error("The GDAL environment is not available");
            myRotateAndScale = null;
            myInverseRotateAndScale = null;
            myXOffset = 0.;
            myYOffset = 0.;
            myNoDataValue = defaultNoData;
            myTransformation = null;
            myInverseTransformation = null;
        }
        else
        {
            // Get the transform for converting pixel coordinates into
            // geo-referenced coordinates.
            double[] geoTransform = new double[6];
            myDataset.GetGeoTransform(geoTransform);
            myXScale = geoTransform[1];
            myYScale = geoTransform[5];
            myRotateAndScale = new Matrix2d(geoTransform[1], geoTransform[2], geoTransform[4], geoTransform[5]);
            myInverseRotateAndScale = myRotateAndScale.invert();
            myXOffset = geoTransform[0];
            myYOffset = geoTransform[3];

            String projection = myDataset.GetProjectionRef();
            SpatialReference spatialRef = new SpatialReference(projection);
            SpatialReference wgs84 = new SpatialReference(osrConstants.SRS_WKT_WGS84);
            myTransformation = new CoordinateTransformation(spatialRef, wgs84);
            myInverseTransformation = new CoordinateTransformation(wgs84, spatialRef);

            Band band = myDataset.GetRasterBand(1);
            Double[] val = new Double[1];
            band.GetNoDataValue(val);
            if (val[0] != null)
            {
                myNoDataValue = (float)val[0].doubleValue();
            }
            else
            {
                myNoDataValue = defaultNoData;
            }
        }
    }

    /** Perform any required cleanup. */
    public synchronized void close()
    {
        myDataset.delete();
    }

    /**
     * Get the geographic position for the associated image position.
     *
     * @param x Image x coordinate.
     * @param y Image y coordinate.
     * @return The geographic position.
     */
    public LatLonAlt getGeoPosition(int x, int y)
    {
        Vector2d result = myRotateAndScale.mult(new Vector2d(x, y));
        double[] transPoint = new double[3];
        myTransformation.TransformPoint(transPoint, result.getX() + myXOffset, result.getY() + myYOffset, 0);

        return LatLonAlt.createFromDegrees(transPoint[1], transPoint[0], ReferenceLevel.ELLIPSOID);
    }

    /**
     * Get the image height.
     *
     * @return The image height.
     */
    public synchronized int getImageHeight()
    {
        return myDataset.getRasterYSize();
    }

    /**
     * Get the image width.
     *
     * @return The image width.
     */
    public synchronized int getImageWidth()
    {
        return myDataset.getRasterXSize();
    }

    /**
     * Get the noDataValue.
     *
     * @return the noDataValue
     */
    public double getNoDataValue()
    {
        return myNoDataValue;
    }

    /**
     * Get the pixel coordinates for the geographic location.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @return The pixel coordinates for the geographic location.
     */
    public Vector2i getPixelCoordinates(double lat, double lon)
    {
        if (myUseInverseTransform)
        {
            double[] invTransPoint = new double[3];
            myInverseTransformation.TransformPoint(invTransPoint, lon, lat, 0);
            Vector2d invResult = myInverseRotateAndScale.mult(new Vector2d(invTransPoint[0], invTransPoint[1]));
            return new Vector2i((int)(invResult.getX() - myXOffset), (int)(invResult.getY() + myYOffset));
        }
        else
        {
            return new Vector2i((int)((lon - myXOffset) / myXScale), (int)((lat - myYOffset) / myYScale));
        }
    }

    /**
     * Get the elevation at the geographic location.
     *
     * @param lat The latitude of the location.
     * @param lon The longitude of the location.
     * @param approximate When true, return the an approximate value when the
     *            actual value is missing.
     * @return The elevation at the geographic location.
     */
    public float sampleGeo(double lat, double lon, boolean approximate)
    {
        Vector2i pixelCoords = getPixelCoordinates(lat, lon);
        float sample = samplePixel(pixelCoords.getX(), pixelCoords.getY());
        int step = 1;
        while (MathUtil.isZero(sample - myNoDataValue) && approximate && step < 5)
        {
            Pair<Float, Integer> sumAndValues = addToSum(samplePixel(pixelCoords.getX() - step, pixelCoords.getY() - step), null);
            sumAndValues = addToSum(samplePixel(pixelCoords.getX() - step, pixelCoords.getY()), sumAndValues);
            sumAndValues = addToSum(samplePixel(pixelCoords.getX() - step, pixelCoords.getY() + step), sumAndValues);
            sumAndValues = addToSum(samplePixel(pixelCoords.getX() + step, pixelCoords.getY() - step), sumAndValues);
            sumAndValues = addToSum(samplePixel(pixelCoords.getX() + step, pixelCoords.getY()), sumAndValues);
            sumAndValues = addToSum(samplePixel(pixelCoords.getX() + step, pixelCoords.getY() + step), sumAndValues);
            sumAndValues = addToSum(samplePixel(pixelCoords.getX(), pixelCoords.getY() - step), sumAndValues);
            sumAndValues = addToSum(samplePixel(pixelCoords.getX(), pixelCoords.getY() + step), sumAndValues);

            if (sumAndValues != null)
            {
                sample = sumAndValues.getFirstObject().floatValue() / sumAndValues.getSecondObject().intValue();
            }
            ++step;
        }
        return MathUtil.isZero(sample - myNoDataValue) ? 0 : sample;
    }

    /**
     * Get the elevation at the pixel coordinates. If the pixel coordinates are
     * out of bounds, the No Data Value is returned.
     *
     * @param x The x pixel coordinate.
     * @param y The y pixel coordinate.
     * @return The elevation at the pixel coordinates.
     */
    public synchronized float samplePixel(int x, int y)
    {
        if (x < getImageWidth() && y < getImageHeight() && x >= 0 && y >= 0)
        {
            Band band = myDataset.GetRasterBand(1);
            float[] array = new float[1];
            band.ReadRaster(x, y, 1, 1, array);
            return array[0];
        }

        return myNoDataValue;
    }

    /**
     * If the value is not the No Data Value, add the value to the sum and
     * increment the number of values.
     *
     * @param value The value to add to the sum.
     * @param sumValues The current sum and the number of values which comprise
     *            the sum.
     * @return The new sum and the number of values which comprise the sum.
     */
    private Pair<Float, Integer> addToSum(float value, Pair<Float, Integer> sumValues)
    {
        if (!MathUtil.isZero(value - myNoDataValue))
        {
            if (sumValues == null)
            {
                return new Pair<Float, Integer>(Float.valueOf(value), Integer.valueOf(1));
            }
            else
            {
                return new Pair<Float, Integer>(Float.valueOf(sumValues.getFirstObject().floatValue() + value),
                        Integer.valueOf(sumValues.getSecondObject().intValue() + 1));
            }
        }
        return sumValues;
    }
}
