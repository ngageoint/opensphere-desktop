package io.opensphere.core.mgrs;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;

/** Class to hold generic grid information. */
public class GenericGrid
{
    /** The bounding box of this geometry. */
    private GeographicBoundingBox myBoundingBox;

    /** The center position. */
    private GeographicPosition myCenterPosition;

    /** The north east corner location. */
    private GeographicPosition myNEPos;

    /** The north west corner location. */
    private GeographicPosition myNWPos;

    /** The south east corner location. */
    private GeographicPosition mySEPos;

    /** The south west easting value. */
    private double mySWEasting;

    /** The south west northing value. */
    private double mySWNorthing;

    /** The south west corner location. */
    private GeographicPosition mySWPos;

    /**
     * Default constructor.
     */
    public GenericGrid()
    {
    }

    /**
     * Standard getter.
     *
     * @return The bounding box of this grid.
     */
    public GeographicBoundingBox getBoundingBox()
    {
        return myBoundingBox;
    }

    /**
     * Standard getter.
     *
     * @return The center geographic position of grid.
     */
    public GeographicPosition getCenterPosition()
    {
        return myCenterPosition;
    }

    /**
     * Standard getter.
     *
     * @return The north east geographic position.
     */
    public GeographicPosition getNEPos()
    {
        return myNEPos;
    }

    /**
     * Standard getter.
     *
     * @return The north west geographic position.
     */
    public GeographicPosition getNWPos()
    {
        return myNWPos;
    }

    /**
     * Standard getter.
     *
     * @return The south east geographic position.
     */
    public GeographicPosition getSEPos()
    {
        return mySEPos;
    }

    /**
     * Standard getter.
     *
     * @return The south west easting value.
     */
    public double getSWEasting()
    {
        return mySWEasting;
    }

    /**
     * Standard getter.
     *
     * @return The south west northing value.
     */
    public double getSWNorthing()
    {
        return mySWNorthing;
    }

    /**
     * Standard getter.
     *
     * @return The south west geographic position.
     */
    public GeographicPosition getSWPos()
    {
        return mySWPos;
    }

    /**
     * Standard setter.
     *
     * @param boundingBox The new bounding box.
     */
    public void setBoundingBox(GeographicBoundingBox boundingBox)
    {
        this.myBoundingBox = boundingBox;
    }

    /**
     * Standard setter.
     *
     * @param centerPosition The center geographic position of grid.
     */
    public void setCenterPosition(GeographicPosition centerPosition)
    {
        this.myCenterPosition = centerPosition;
    }

    /**
     * Standard setter.
     *
     * @param nePos The north east geographic position.
     */
    public void setNEPos(GeographicPosition nePos)
    {
        this.myNEPos = nePos;
    }

    /**
     * Standard setter.
     *
     * @param nwPos The north west geographic position.
     */
    public void setNWPos(GeographicPosition nwPos)
    {
        this.myNWPos = nwPos;
    }

    /**
     * Standard setter.
     *
     * @param sePos The south east geographic position.
     */
    public void setSEPos(GeographicPosition sePos)
    {
        this.mySEPos = sePos;
    }

    /**
     * Standard setter.
     *
     * @param swEasting The south west easting value.
     */
    public void setSWEasting(double swEasting)
    {
        this.mySWEasting = swEasting;
    }

    /**
     * Standard setter.
     *
     * @param swNorthing The south west northing value.
     */
    public void setSWNorthing(double swNorthing)
    {
        this.mySWNorthing = swNorthing;
    }

    /**
     * Standard setter.
     *
     * @param swPos The south west geographic position.
     */
    public void setSWPos(GeographicPosition swPos)
    {
        this.mySWPos = swPos;
    }
}
