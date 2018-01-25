package io.opensphere.imagery.transform;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.common.coordinate.math.EarthMath;
import io.opensphere.core.common.coordinate.math.strategy.EarthMathContext;
import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.mantle.util.EncryptionUtilities;

/**
 * An interface to allow for different orders of transform to go on in the
 * background without users of those objects understanding that.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class ImageryTransform implements Cloneable
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ImageryTransform.class);

    /** The Constant MAX_ERROR_START_VALUE. */
    private static final double MAX_ERROR_START_VALUE = -10000.0;

    /** The debug. */
    private static boolean ourDebug;

    /** The GC ps. */
    private List<GroundControlPoint> myGCPs;

    /** The Geo transform geo to pixel. */
    private TransformCoefficients myGeoTransformGeoToPixel;

    /** The Geo transform pixel to geo. */
    private TransformCoefficients myGeoTransformPixelToGeo;

    /** The Max meters error. */
    private double myMaxMetersError;

    /** The M d5 hash. */
    private String myMD5Hash = "noMD5HashYet";

    /** The Order. */
    private int myOrder = 1;

    /** The Order size (matrix width to solve for this). */
    private int myOrderSize = 3;

    /** The RMSE. */
    private double myRMSE;

    /** The RMSE meters. */
    private double myRMSEMeters;

    /**
     * Sort gc ps.
     *
     * @param gcps the gcps
     */
    public static void sortGCPs(List<GroundControlPoint> gcps)
    {
        Collections.sort(gcps, new Comparator<GroundControlPoint>()
        {
            @Override
            public int compare(GroundControlPoint a, GroundControlPoint b)
            {
                if (a == null && b == null)
                {
                    return 0;
                }

                if (a == null && b != null)
                {
                    return -1;
                }

                if (a != null && b == null)
                {
                    return 1;
                }

                if (a != null && b != null)
                {
                    int val = Double.compare(a.getLat(), b.getLat());
                    if (val != 0)
                    {
                        return val;
                    }

                    val = Double.compare(a.getLon(), b.getLon());
                    if (val != 0)
                    {
                        return val;
                    }

                    val = Double.compare(a.getLine(), b.getLine());
                    if (val != 0)
                    {
                        return val;
                    }

                    val = Double.compare(a.getPixel(), b.getPixel());
                    if (val != 0)
                    {
                        return val;
                    }
                }

                return 0;
            }
        });
    }

    @Override
    public ImageryTransform clone()
    {
        try
        {
            return (ImageryTransform)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Detect amount of error.
     *
     * @param gcps the gcps
     */
    public void detectAmountOfError(List<GroundControlPoint> gcps)
    {
        EarthMath vinc = EarthMathContext.getStaticVincenty();

        double totalResidualY = 0.0;
        double totalResidualX = 0.0;
        double maxErrorX = 0.0;
        double maxErrorY = 0.0;

        double maxMeterError = MAX_ERROR_START_VALUE;
        double totalMeterError = 0.0;
        for (GroundControlPoint gcp : gcps)
        {
            LatLonAlt latlon = getLatLonBasedOnTransform(gcp.getPixel(), gcp.getLine());
            double residualY = Math.abs(latlon.getLatD() - gcp.getLat());
            totalResidualY += residualY * residualY;

            double residualX = Math.abs(latlon.getLonD() - gcp.getLon());
            totalResidualX += residualX * residualX;

            if (residualX > maxErrorX)
            {
                maxErrorX = residualX;
            }

            if (residualY > maxErrorY)
            {
                maxErrorY = residualY;
            }

            double errorMeters = vinc.calculateDistanceBetweenPoints(latlon.getLatD(), latlon.getLonD(), gcp.getLat(),
                    gcp.getLon());
            totalMeterError += errorMeters;
            if (errorMeters > maxMeterError)
            {
                maxMeterError = errorMeters;
            }
        }
        double divisor = 1.0 / gcps.size();
        // RMSE = Root Mean Squared Error
        double aRMSEX = Math.sqrt(divisor * totalResidualX);
        double aRMSEY = Math.sqrt(divisor * totalResidualY);

        myRMSE = Math.sqrt(aRMSEX * aRMSEX + aRMSEY * aRMSEY);
        myRMSEMeters = Math.sqrt(divisor * totalMeterError);
        myMaxMetersError = maxMeterError;

        if (ourDebug)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                        "ImageryTransform total errorY: " + totalResidualY + " errorX: " + totalResidualX + " aRMSE: " + myRMSE);
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("ImageryTransform biggest single X error: " + maxErrorX + " biggest single Y error: " + maxErrorY);
            }
        }
    }

    /**
     * Error geo to pixel x.
     *
     * @param gcps the gcps
     * @return the double
     */
    public double errorGeoToPixelX(List<GroundControlPoint> gcps)
    {
        double maxError = MAX_ERROR_START_VALUE;

        for (GroundControlPoint gcp : gcps)
        {
            double xPixel = getXPixelBasedOnTransform(gcp.getLon(), gcp.getLat());

            double error = Math.abs(xPixel - gcp.getPixel());
            if (error > 1000000)
            {
                return Double.MAX_VALUE;
            }
            if (error > maxError)
            {
                maxError = error;
            }
        }
        return maxError;
    }

    /**
     * Error geo to pixel y.
     *
     * @param gcps the gcps
     * @return the double
     */
    public double errorGeoToPixelY(List<GroundControlPoint> gcps)
    {
        double maxError = MAX_ERROR_START_VALUE;

        for (GroundControlPoint gcp : gcps)
        {
            double yPixel = getYPixelBasedOnTransform(gcp.getLon(), gcp.getLat());

            double error = Math.abs(yPixel - gcp.getLine());
            if (error > 1000000)
            {
                return Double.MAX_VALUE;
            }
            if (error > maxError)
            {
                maxError = error;
            }
        }
        return maxError;
    }

    /**
     * Returns single max meter error assuming the lat component is zero...
     *
     * @param gcps the gcps
     * @return the double
     */
    public double errorPixelToGeoX(List<GroundControlPoint> gcps)
    {
        EarthMath vinc = EarthMathContext.getStaticVincenty();

        double maxMeterError = MAX_ERROR_START_VALUE;

        for (GroundControlPoint gcp : gcps)
        {
            double xGeo = getXGeoBasedOnTransform(gcp.getPixel(), gcp.getLine());

            if (xGeo > 180.0 || xGeo < -180.0)
            {
                return Double.MAX_VALUE;
            }

            double errorMeters = vinc.calculateDistanceBetweenPoints(0, xGeo, 0, gcp.getLon());

            if (Double.isNaN(errorMeters))
            {
                return Double.MAX_VALUE;
            }

            if (errorMeters > maxMeterError)
            {
                maxMeterError = errorMeters;
            }
        }
        return maxMeterError;
    }

    /**
     * Returns single max meter error assuming the lon component is zero...
     *
     * @param gcps the gcps
     * @return the double
     */
    public double errorPixelToGeoY(List<GroundControlPoint> gcps)
    {
        EarthMath vinc = EarthMathContext.getStaticVincenty();

        double maxMeterError = MAX_ERROR_START_VALUE;

        for (GroundControlPoint gcp : gcps)
        {
            double yGeo = getYGeoBasedOnTransform(gcp.getPixel(), gcp.getLine());

            if (yGeo > 90.0 || yGeo < -90.0)
            {
                return Double.MAX_VALUE;
            }

            double errorMeters = vinc.calculateDistanceBetweenPoints(yGeo, 0, gcp.getLat(), 0);

            if (Double.isNaN(errorMeters))
            {
                maxMeterError = Double.MAX_VALUE;
            }

            if (errorMeters > maxMeterError)
            {
                maxMeterError = errorMeters;
            }
        }
        return maxMeterError;
    }

    /**
     * Find transform.
     *
     * @param listOfGCPs the list of gc ps
     */
    public abstract void findTransform(List<GroundControlPoint> listOfGCPs);

    /**
     * Gets the aDF transform.
     *
     * @return the aDF transform
     */
    public abstract double[] getADFTransform();

    /**
     * Gets the gC ps.
     *
     * @return the gC ps
     */
    public List<GroundControlPoint> getGCPs()
    {
        return GroundControlPoint.cloneList(myGCPs);
    }

    /**
     * Gets the geo to pixel transform coeff.
     *
     * @return the geo to pixel transform coeff
     */
    public TransformCoefficients getGeoToPixelTransformCoeff()
    {
        return myGeoTransformGeoToPixel;
    }

    /**
     * Gets the lat lon based on transform.
     *
     * @param xPixel the x pixel
     * @param yPixel the y pixel
     * @return the lat lon based on transform
     */
    public abstract LatLonAlt getLatLonBasedOnTransform(double xPixel, double yPixel);

    /**
     * Gets the max meters error.
     *
     * @return the max meters error
     */
    public double getMaxMetersError()
    {
        return myMaxMetersError;
    }

    /**
     * Gets the m d5 hash.
     *
     * @return the m d5 hash
     */
    public String getMD5Hash()
    {
        return myMD5Hash;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public int getOrder()
    {
        return myOrder;
    }

    /**
     * Gets the order size.
     *
     * @return the order size
     */
    public int getOrderSize()
    {
        return myOrderSize;
    }

    /**
     * Gets the pixel to geo transform coeff.
     *
     * @return the pixel to geo transform coeff
     */
    public TransformCoefficients getPixelToGeoTransformCoeff()
    {
        return myGeoTransformPixelToGeo;
    }

    /**
     * Get the Root Mean Squared Error.
     *
     * @return the rMSE
     */
    public double getRMSE()
    {
        return myRMSE;
    }

    /**
     * Gets the rMSE meters.
     *
     * @return the rMSE meters
     */
    public double getRMSEMeters()
    {
        return myRMSEMeters;
    }

    /**
     * Gets the x geo based on transform.
     *
     * @param xPixel the x pixel
     * @param yPixel the y pixel
     * @return the x geo based on transform
     */
    public abstract double getXGeoBasedOnTransform(double xPixel, double yPixel);

    /**
     * Gets the x pixel based on transform.
     *
     * @param xGeo the x geo
     * @param yGeo the y geo
     * @return the x pixel based on transform
     */
    public abstract double getXPixelBasedOnTransform(double xGeo, double yGeo);

    /**
     * Gets the y geo based on transform.
     *
     * @param xPixel the x pixel
     * @param yPixel the y pixel
     * @return the y geo based on transform
     */
    public abstract double getYGeoBasedOnTransform(double xPixel, double yPixel);

    /**
     * Gets the y pixel based on transform.
     *
     * @param xGeo the x geo
     * @param yGeo the y geo
     * @return the y pixel based on transform
     */
    public abstract double getYPixelBasedOnTransform(double xGeo, double yGeo);

    /**
     * Sets the gC ps.
     *
     * @param gcpList the new gC ps
     */
    public void setGCPs(List<GroundControlPoint> gcpList)
    {
        myGCPs = gcpList;
    }

    /**
     * Sets the geo to pixel transform coeff.
     *
     * @param coeff the new geo to pixel transform coeff
     */
    public void setGeoToPixelTransformCoeff(TransformCoefficients coeff)
    {
        myGeoTransformGeoToPixel = coeff;
    }

    /**
     * Sets the max meters error.
     *
     * @param aMaxMetersError the new max meters error
     */
    public void setMaxMetersError(double aMaxMetersError)
    {
        myMaxMetersError = aMaxMetersError;
    }

    /**
     * Sets the m d5 hash.
     *
     * @param hash the new m d5 hash
     */
    public void setMD5Hash(String hash)
    {
        myMD5Hash = hash;
    }

    /**
     * Sets the order.
     *
     * @param anOrder the new order
     */
    public void setOrder(int anOrder)
    {
        myOrder = anOrder;
    }

    /**
     * Sets the order size.
     *
     * @param anOrderSize the new order size
     */
    public void setOrderSize(int anOrderSize)
    {
        myOrderSize = anOrderSize;
    }

    /**
     * Sets the pixel to geo transform coeff.
     *
     * @param coeff the new pixel to geo transform coeff
     */
    public void setPixelToGeoTransformCoeff(TransformCoefficients coeff)
    {
        myGeoTransformPixelToGeo = coeff;
    }

    /**
     * Set the Root Mean Squared Error.
     *
     * @param inRMSE - the amount of error
     */
    public void setRMSE(double inRMSE)
    {
        myRMSE = inRMSE;
    }

    /**
     * Sets the rMSE meters.
     *
     * @param rMSEMeters the new rMSE meters
     */
    public void setRMSEMeters(double rMSEMeters)
    {
        myRMSEMeters = rMSEMeters;
    }

    /**
     * To m d5 hash.
     */
    public void toMD5Hash()
    {
        try
        {
            myMD5Hash = EncryptionUtilities.createMD5Hash(toMD5String());
        }
        catch (NoSuchAlgorithmException e)
        {
            LOGGER.warn(e);
        }
    }

    /**
     * Only the important pieces need to be in the MD5 string. Order and GCPs.
     *
     * @return the string
     */
    public String toMD5String()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Order: ").append(myOrder).append(" GCPs: ");
        for (GroundControlPoint gcp : myGCPs)
        {
            sb.append(gcp.toString());
        }

        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "ImageryTransform [myGeoTransformGeoToPixel=" + myGeoTransformGeoToPixel + ", myGeoTransformPixelToGeo="
                + myGeoTransformPixelToGeo + ", myGCPs=" + myGCPs + ", myRMSE=" + myRMSE + ", myOrder=" + myOrder
                + ", myOrderSize=" + myOrderSize + "]";
    }
}
