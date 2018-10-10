package io.opensphere.core.terrain.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.StreamingImage;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.MathUtil;

/** Reader for extracting SRTM elevation data from an image. */
public class BilSrtmImageReader implements ElevationImageReader
{
    /** The number of bytes per terrain pixel. */
    private static final int BYTES_PER_PIXEL = 2;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(BilSrtmImageReader.class);

    /** Used to ensure that the BIL size warning is only issued once. */
    private static Object ourBoundsWarningMonitor = new Object();

    /**
     * The warning for attempting to read outside of the image size should only
     * be issued once. When this is true, we have already issued the warning.
     */
    private static volatile boolean ourOutOfBoundsWarningIssued;

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
     * If this constructor is called, init() call must precede before use.
     */
    public BilSrtmImageReader()
    {
    }

    /**
     * Constructor.
     *
     * @param bounds The bounds over which this reader provides data.
     * @param missingDataValue The value given when no data is available.
     * @param crs The CRS which matches the values returned by this reader.
     * @param orderId key used to determine the participant in the order manager
     *            with which this reader is associated.
     */
    public BilSrtmImageReader(GeographicBoundingBox bounds, double missingDataValue, String crs, String orderId)
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
    public String getImageFormat()
    {
        return "bil";
    }

    @Override
    public double getMissingDataValue()
    {
        return myMissingDataValue;
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
        InputStream stream = validate(position, image, bounds);
        int closestX;
        int closestY;
        try
        {
            int width = image.getWidth();
            int height = image.getHeight();

            Vector3d offset = bounds.getOffsetPercent(position);

            double xPixelPosition = (width - 1) * offset.getX();
            double yPixelPosition = (height - 1) * (1. - offset.getY());
            closestX = (int)Math.round(xPixelPosition);
            closestY = (int)Math.round(yPixelPosition);

            boolean onXPixel = MathUtil.isZero(closestX - xPixelPosition);
            boolean onYPixel = MathUtil.isZero(closestY - yPixelPosition);

            if (onXPixel)
            {
                if (onYPixel)
                {
                    int startRead = (closestY * width + closestX) * BYTES_PER_PIXEL;
                    elevation = readShortAt(startRead, stream);
                }
                else
                {
                    // Sample two values in the y direction on the x pixel
                    int trunkY = (int)yPixelPosition;
                    int startRead = (trunkY * width + closestX) * BYTES_PER_PIXEL;
                    double sample1 = readShortAt(startRead, stream);
                    double sample2 = readShortAt(width * BYTES_PER_PIXEL - BYTES_PER_PIXEL, stream);
                    elevation = interpolate(yPixelPosition - (int)yPixelPosition, sample1, sample2);
                }
            }
            else if (onYPixel)
            {
                // Sample two values in the x direction on the y pixel
                int trunkX = (int)xPixelPosition;
                int startRead = (closestY * width + trunkX) * BYTES_PER_PIXEL;
                double sample1 = readShortAt(startRead, stream);
                double sample2 = readShortAt(0, stream);
                elevation = interpolate(xPixelPosition - (int)xPixelPosition, sample1, sample2);
            }
            else
            {
                // Sample the 4 nearest values
                int trunkX = (int)xPixelPosition;
                int trunkY = (int)yPixelPosition;

                int startRead = (trunkY * width + trunkX) * BYTES_PER_PIXEL;
                double sample1 = readShortAt(startRead, stream);
                double sample2 = readShortAt(0, stream);
                double sample3 = readShortAt(width * BYTES_PER_PIXEL - 2 * BYTES_PER_PIXEL, stream);
                double sample4 = readShortAt(0, stream);
                double lerpXPct = xPixelPosition - (int)xPixelPosition;
                double lerpYpct = yPixelPosition - (int)yPixelPosition;

                elevation = interpolate(lerpXPct, lerpYpct, sample1, sample2, sample3, sample4);
            }
        }
        finally
        {
            closeStream(stream);
        }

        if (elevation == getMissingDataValue() && approximate)
        {
            elevation = approximate(image, closestX, closestY);
        }

        return elevation;
    }

    /**
     * Approximate a value based on the values nearest the point in which we are
     * interested.
     *
     * @param image The image which contains the elevation values.
     * @param closestX The x coordinate of the image position which most closely
     *            matches the geographic position for which the elevation is
     *            desired.
     * @param closestY The y coordinate of the image position which most closely
     *            matches the geographic position for which the elevation is
     *            desired.
     * @return an approximation of the elevation.
     * @throws ElevationImageReaderException If values could not be read from
     *             the image.
     */
    private double approximate(Image image, int closestX, int closestY) throws ElevationImageReaderException
    {
        int width = image.getWidth();
        int height = image.getHeight();

        InputStream stream = ((StreamingImage<?>)image).getInputStream();
        try
        {
            if (stream == null)
            {
                throw new ElevationImageReaderException("Elevation sampling failed. Stream unavailable for streaming image.");
            }
            // sample a 5x5 block of values. Even if we are on a corner, this
            // should give us a minimum of 9 new samples.
            double[][] samples = new double[5][5];

            // the first sample is at (closestX - 2, closestY - 2)
            int streamPosition = 0;
            for (int i = -2; i < 2; ++i)
            {
                int sampleX = closestX + i;
                for (int j = -2; j < 2; ++j)
                {
                    int sampleY = closestY + j;
                    if (sampleY >= 0 && sampleY < width && sampleX >= 0 && sampleX < height)
                    {
                        int skip = (sampleY * width + sampleX) * BYTES_PER_PIXEL - streamPosition;
                        samples[i + 2][j + 2] = readShortAt(skip, stream);
                        // The new stream position is moved forward by the
                        // bytes skipped plus the bytes read.
                        streamPosition += skip + BYTES_PER_PIXEL;
                    }
                    else
                    {
                        samples[i + 2][j + 2] = getMissingDataValue();
                    }
                }
            }

            double dist1avg = getDist1Average(samples);
            return dist1avg == getMissingDataValue() ? getDist2Average(samples) : dist1avg;
        }
        finally
        {
            closeStream(stream);
        }
    }

    /**
     * Close the stream if possible. Exceptions are caught a logged if closing
     * fails.
     *
     * @param stream The stream to close.
     */
    private void closeStream(InputStream stream)
    {
        try
        {
            if (stream != null)
            {
                stream.close();
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to close the stream. " + e, e);
        }
    }

    /**
     * Get a weighted average of the values midway between opposite points at a
     * step distance of 1 the center. The step distance is measured by closest
     * neighbor horizontally, vertically or diagonally. In this case the used
     * samples must border the center position.
     *
     * @param samples The samples to average. It is expected that this array is
     *            a minimum of 3 x 3 and it is recommended that the width and
     *            height be odd.
     * @return The weighted average.
     */
    private double getDist1Average(double[][] samples)
    {
        int mid = samples[0].length / 2;
        int low = mid - 1;
        int high = mid + 1;

        double diag1 = interpolate(0.5, samples[low][low], samples[high][high]);
        double diag2 = interpolate(0.5, samples[low][high], samples[high][low]);
        double horiz = interpolate(0.5, samples[low][mid], samples[high][mid]);
        double vert = interpolate(0.5, samples[mid][low], samples[mid][high]);

        double diag = interpolate(0.5, diag1, diag2);
        double sqr = interpolate(0.5, horiz, vert);

        return interpolate(0.5, diag, sqr);
    }

    /**
     * Get an average of the values midway between opposite points at a step
     * distance of 2 the center. The step distance is measured by closest
     * neighbor horizontally, vertically or diagonally. In this case the used
     * samples must be neighbors of values which border the center position.
     *
     * @param samples The samples to average. It is expected that this array is
     *            a minimum of 5 x 5 and it is recommended that the width and
     *            height be odd.
     * @return The weighted average.
     */
    private double getDist2Average(double[][] samples)
    {
        int mid = samples[0].length / 2;
        int low = mid - 2;
        int high = mid + 2;

        int validValues = 0;
        double[] avgs = new double[8];

        // get the vertical sides averages.
        for (int i = low; i < high; ++i)
        {
            double val = interpolate(0.5, samples[low][i], samples[high][high - i]);
            if (val != getMissingDataValue())
            {
                avgs[validValues] = val;
                ++validValues;
            }
        }

        // get the remaining horizontal sides averages.
        for (int i = low + 1; i < high - 1; ++i)
        {
            double val = interpolate(0.5, samples[i][low], samples[high - i][high]);
            if (val != getMissingDataValue())
            {
                avgs[validValues] = val;
                ++validValues;
            }
        }

        if (validValues > 0)
        {
            double sum = 0.;
            for (int i = 0; i < validValues; ++i)
            {
                sum += avgs[i];
            }
            return sum / validValues;
        }
        return getMissingDataValue();
    }

    /**
     * Special interpolation which excludes the "missing data" value. If one
     * sample is the "missing data" value, the other value will be returned. If
     * both values are the "missing data" value then the "missing data" value is
     * returned.
     *
     * @param pct The percentage from sample1 to sample2.
     * @param sample1 The starting sample.
     * @param sample2 the ending sample.
     * @return The interpolated value or the "missing data" value if no
     *         interpolation is possible.
     */
    private double interpolate(double pct, double sample1, double sample2)
    {
        if (sample1 == getMissingDataValue())
        {
            return sample2;
        }
        else if (sample2 == getMissingDataValue())
        {
            return sample1;
        }
        else
        {
            return MathUtil.lerp(pct, sample1, sample2);
        }
    }

    /**
     * Special interpolation which excludes the "missing data" value.
     * Interpolation is done in the x direction between sample1 and sample2,
     * then again in the x direction between sample3 and sample4. The resultant
     * values are then interpolated in the y direction.
     *
     * @param xPct The percentage of the sampling in the x direction.
     * @param yPct The percentage of the sampling in the y direction.
     * @param sample1 The first of the four closest samples to the value in
     *            which we are interested.
     * @param sample2 The second of the four closest samples to the value in
     *            which we are interested.
     * @param sample3 The third of the four closest samples to the value in
     *            which we are interested.
     * @param sample4 The fourth of the four closest samples to the value in
     *            which we are interested.
     * @return The interpolated value or the "missing data" value if no
     *         interpolation is possible.
     */
    private double interpolate(double xPct, double yPct, double sample1, double sample2, double sample3, double sample4)
    {
        double set1 = interpolate(xPct, sample1, sample2);
        double set2 = interpolate(xPct, sample3, sample4);
        return interpolate(yPct, set1, set2);
    }

    /**
     * Read a terrain elevation as a short from the input stream.
     *
     * @param skip The number of bytes to skip before reading the elevation
     *            value.
     * @param stream The stream which contains the elevation data.
     * @return The elevation value if one could be read.
     * @throws ElevationImageReaderException If the elevation value could not be
     *             read.
     */
    private double readShortAt(int skip, InputStream stream) throws ElevationImageReaderException
    {
        byte[] bytes = new byte[BYTES_PER_PIXEL];

        try
        {
            long skipped = stream.skip(skip);
            int bytesRead = stream.read(bytes);
            if (skipped < skip || bytesRead != BYTES_PER_PIXEL)
            {
                synchronized (ourBoundsWarningMonitor)
                {
                    if (!ourOutOfBoundsWarningIssued)
                    {
                        ourOutOfBoundsWarningIssued = true;
                        LOGGER.error("Could not read elevation. Image dimensions do not match buffer size.");
                    }
                }
            }
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            buf.order(ByteOrder.BIG_ENDIAN);
            return buf.getShort();
        }
        catch (IOException e)
        {
            throw new ElevationImageReaderException("Failed to read from stream." + e, e);
        }
    }

    /**
     * Validate whether the values given can be used to generate an elevation
     * value.
     *
     * @param position The geographic position for which an elevation is
     *            desired.
     * @param image The image which contains the elevation values.
     * @param bounds The geographic bounds provided by the image.
     * @return The input stream.
     * @throws ElevationImageReaderException If the elevation cannot be read
     *             from the image.
     */
    private InputStream validate(GeographicPosition position, Image image, GeographicBoundingBox bounds)
            throws ElevationImageReaderException
    {
        if (!bounds.contains(position, 0.))
        {
            throw new ElevationImageReaderException("Position " + position + " not within tile bounds " + bounds);
        }

        if (!(image instanceof StreamingImage))
        {
            throw new ElevationImageReaderException("Image cannot provide a stream.");
        }

        InputStream stream = ((StreamingImage<?>)image).getInputStream();
        if (stream == null)
        {
            throw new ElevationImageReaderException("Elevation sampling failed. Stream unavailable for streaming image.");
        }

        return stream;
    }
}
