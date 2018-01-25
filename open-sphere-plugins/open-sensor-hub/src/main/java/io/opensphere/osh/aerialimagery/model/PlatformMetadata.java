package io.opensphere.osh.aerialimagery.model;

import java.io.Serializable;
import java.util.Date;

import io.opensphere.core.model.GeographicConvexQuadrilateral;
import io.opensphere.core.model.LatLonAlt;

/**
 * Contains the location of the platform carrying the imagery sensor including
 * the platforms orientation and the camera's orientation. Also contains a
 * calculated footprint of the imagery.
 */
public class PlatformMetadata implements Serializable
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The pitch angle around the axis coming out the sides of the vehicle, in
     * degrees.
     */
    private double myCameraPitchAngle;

    /**
     * The roll angle around the axis coming out the side of the vehicle, in
     * degrees.
     */
    private double myCameraRollAngle;

    /**
     * The yaw angle of the camera around the axis coming from the top of the
     * vehicle, in degrees.
     */
    private double myCameraYawAngle;

    /**
     * The camera footprint on the earth, or null if the camera was not looking
     * at the earth.
     */
    private GeographicConvexQuadrilateral myFootprint;

    /**
     * The location of the vehicle.
     */
    private LatLonAlt myLocation;

    /**
     * The pitch angle around the axis coming out the sides of the vehicle, in
     * degrees.
     */
    private double myPitchAngle;

    /**
     * The roll angle around the axis coming out the side of the vehicle, in
     * degrees.
     */
    private double myRollAngle;

    /**
     * The time the metadata is valid for.
     */
    private Date myTime;

    /**
     * The yaw angle around the axis coming from the top of the vehicle, in
     * degrees.
     */
    private double myYawAngle;

    /**
     * Gets the pitch angle around the axis coming out the sides of the vehicle,
     * in degrees.
     *
     * @return The pitch angle around the axis coming out the sides of the
     *         vehicle, in degrees.
     */
    public double getCameraPitchAngle()
    {
        return myCameraPitchAngle;
    }

    /**
     * Gets the roll angle around the axis coming out the side of the vehicle,
     * in degrees.
     *
     * @return The roll angle around the axis coming out the side of the
     *         vehicle, in degrees.
     */
    public double getCameraRollAngle()
    {
        return myCameraRollAngle;
    }

    /**
     * Gets the yaw angle of the camera around the axis coming from the top of
     * the vehicle, in degrees.
     *
     * @return The yaw angle of the camera around the axis coming from the top
     *         of the vehicle, in degrees.
     */
    public double getCameraYawAngle()
    {
        return myCameraYawAngle;
    }

    /**
     * Gets the camera footprint on the earth, or null if the camera was not
     * looking at the earth.
     *
     * @return The camera footprint on the earth, or null if the camera was not
     *         looking at the earth.
     */
    public GeographicConvexQuadrilateral getFootprint()
    {
        return myFootprint;
    }

    /**
     * Gets the location of the vehicle.
     *
     * @return The location of the vehicle.
     */
    public LatLonAlt getLocation()
    {
        return myLocation;
    }

    /**
     * Gets the pitch angle around the axis coming out the sides of the vehicle,
     * in degrees.
     *
     * @return The pitch angle around the axis coming out the sides of the
     *         vehicle, in degrees.
     */
    public double getPitchAngle()
    {
        return myPitchAngle;
    }

    /**
     * Gets the roll angle around the axis coming out the side of the vehicle,
     * in degrees.
     *
     * @return The roll angle around the axis coming out the side of the
     *         vehicle, in degrees.
     */
    public double getRollAngle()
    {
        return myRollAngle;
    }

    /**
     * Gets the time the metadata is valid for.
     *
     * @return The time the metadata is valid for.
     */
    public Date getTime()
    {
        Date returnTime = null;

        if (myTime != null)
        {
            returnTime = new Date(myTime.getTime());
        }

        return returnTime;
    }

    /**
     * Gets the yaw angle around the axis coming from the top of the vehicle, in
     * degrees.
     *
     * @return The yaw angle around the axis coming from the top of the vehicle,
     *         in degrees.
     */
    public double getYawAngle()
    {
        return myYawAngle;
    }

    /**
     * Sets the pitch angle around the axis coming out the sides of the vehicle,
     * in degrees.
     *
     * @param cameraPitchAngle The pitch angle around the axis coming out the
     *            sides of the vehicle, in degrees.
     */
    public void setCameraPitchAngle(double cameraPitchAngle)
    {
        myCameraPitchAngle = cameraPitchAngle;
    }

    /**
     * Sets the roll angle around the axis coming out the side of the vehicle,
     * in degrees.
     *
     * @param cameraRollAngle The roll angle around the axis coming out the side
     *            of the vehicle, in degrees.
     */
    public void setCameraRollAngle(double cameraRollAngle)
    {
        myCameraRollAngle = cameraRollAngle;
    }

    /**
     * Sets the yaw angle of the camera around the axis coming from the top of
     * the vehicle, in degrees.
     *
     * @param cameraYawAngle The yaw angle of the camera around the axis coming
     *            from the top of the vehicle, in degrees.
     */
    public void setCameraYawAngle(double cameraYawAngle)
    {
        myCameraYawAngle = cameraYawAngle;
    }

    /**
     * Sets the camera footprint on the earth, or null if the camera was not
     * looking at the earth.
     *
     * @param footprint The camera footprint on the earth, or null if the camera
     *            was not looking at the earth.
     */
    public void setFootprint(GeographicConvexQuadrilateral footprint)
    {
        myFootprint = footprint;
    }

    /**
     * Sets the location of the vehicle.
     *
     * @param location The location of the vehicle.
     */
    public void setLocation(LatLonAlt location)
    {
        myLocation = location;
    }

    /**
     * Sets the pitch angle around the axis coming out the sides of the vehicle,
     * in degrees.
     *
     * @param pitchAngle The pitch angle around the axis coming out the sides of
     *            the vehicle, in degrees.
     */
    public void setPitchAngle(double pitchAngle)
    {
        myPitchAngle = pitchAngle;
    }

    /**
     * Sets the roll angle around the axis coming out the side of the vehicle,
     * in degrees.
     *
     * @param rollAngle The roll angle around the axis coming out the side of
     *            the vehicle, in degrees.
     */
    public void setRollAngle(double rollAngle)
    {
        myRollAngle = rollAngle;
    }

    /**
     * Sets the time the metadata is valid for.
     *
     * @param time The time the metadata is valid for.
     */
    public void setTime(Date time)
    {
        myTime = null;
        if (time != null)
        {
            myTime = new Date(time.getTime());
        }
    }

    /**
     * Sets the yaw angle around the axis coming from the top of the vehicle, in
     * degrees.
     *
     * @param yawAngle The yaw angle around the axis coming from the top of the
     *            vehicle, in degrees.
     */
    public void setYawAngle(double yawAngle)
    {
        myYawAngle = yawAngle;
    }

    @Override
    public String toString()
    {
        return "PlatformMetadata [myCameraPitchAngle=" + myCameraPitchAngle + ", myCameraRollAngle=" + myCameraRollAngle
                + ", myCameraYawAngle=" + myCameraYawAngle + ", myFootprint=" + myFootprint + ", myLocation=" + myLocation
                + ", myPitchAngle=" + myPitchAngle + ", myRollAngle=" + myRollAngle + ", myTime=" + myTime + ", myYawAngle="
                + myYawAngle + "]";
    }
}
