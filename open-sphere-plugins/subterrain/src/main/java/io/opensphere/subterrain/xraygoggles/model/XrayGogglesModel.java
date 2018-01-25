package io.opensphere.subterrain.xraygoggles.model;

import java.util.Observable;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * The model used by the xray goggles components.
 */
public class XrayGogglesModel extends Observable implements Cloneable
{
    /**
     * The geographic position property.
     */
    public static final String GEO_POSITION = "geoPosition";

    /**
     * The screen position property.
     */
    public static final String SCREEN_POSITION = "screenPosition";

    /**
     * The xray goggles label.
     */
    public static final String XRAY_GOGGLES = "Xray Goggles";

    /**
     * The geographic position where the center of the xray window intersects
     * with the globe.
     */
    private GeographicPosition myCenterGeo;

    /**
     * The lower left screen position of the xray window.
     */
    private ScreenPosition myLowerLeft;

    /**
     * The geographic position where the lower left corner of the xray window
     * intersects with the globe.
     */
    private GeographicPosition myLowerLeftGeo;

    /**
     * The lower right screen position of the xray window.
     */
    private ScreenPosition myLowerRight;

    /**
     * The geographic position where the lower right corner of the xray window
     * intersects with the globe.
     */
    private GeographicPosition myLowerRightGeo;

    /**
     * The upper left screen position of the xray window.
     */
    private ScreenPosition myUpperLeft;

    /**
     * The geographic position where the upper left corner of the xray window
     * intersects with the globe.
     */
    private GeographicPosition myUpperLeftGeo;

    /**
     * The upper right screen position of the xray window.
     */
    private ScreenPosition myUpperRight;

    /**
     * The geographic position where the upper right corner of the xray window
     * intersects with the globe.
     */
    private GeographicPosition myUpperRightGeo;

    /**
     * The validator.
     */
    private XrayModelValidator myValidator;

    /**
     * The geometry used to display the xray window.
     */
    private Geometry myWindowGeometry;

    @Override
    public XrayGogglesModel clone()
    {
        try
        {
            return (XrayGogglesModel)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Gets the geographic position where the center of the xray window
     * intersects with the globe.
     *
     * @return The geographic center position of xray window.
     */
    public GeographicPosition getCenterGeo()
    {
        return myCenterGeo;
    }

    /**
     * Gets the lower left screen position of the xray window.
     *
     * @return the lowerLeft.
     */
    public ScreenPosition getLowerLeft()
    {
        return myLowerLeft;
    }

    /**
     * Gets the geographic position where the lower left corner of the xray
     * window intersects with the globe.
     *
     * @return the lowerLeftGeo
     */
    public GeographicPosition getLowerLeftGeo()
    {
        return myLowerLeftGeo;
    }

    /**
     * Gets the lower right screen position of the xray window.
     *
     * @return the lowerRight.
     */
    public ScreenPosition getLowerRight()
    {
        return myLowerRight;
    }

    /**
     * Gets the geographic position where the lower right corner of the xray
     * window intersects with the globe.
     *
     * @return the lowerRightGeo
     */
    public GeographicPosition getLowerRightGeo()
    {
        return myLowerRightGeo;
    }

    /**
     * Gets the upper left screen position of the xray window.
     *
     * @return the upperLeft.
     */
    public ScreenPosition getUpperLeft()
    {
        return myUpperLeft;
    }

    /**
     * Gets the geographic position where the upper left corner of the xray
     * window intersects with the globe.
     *
     * @return the upperLeftGeo.
     */
    public GeographicPosition getUpperLeftGeo()
    {
        return myUpperLeftGeo;
    }

    /**
     * Gets the upper right screen position of the xray window.
     *
     * @return the upperRight.
     */
    public ScreenPosition getUpperRight()
    {
        return myUpperRight;
    }

    /**
     * Gets the geographic position where the upper right corner of the xray
     * window intersects with the globe.
     *
     * @return the upperRightGeo
     */
    public GeographicPosition getUpperRightGeo()
    {
        return myUpperRightGeo;
    }

    /**
     * Gets the geometry used to display the xray window.
     *
     * @return the windowGeometry
     */
    public Geometry getWindowGeometry()
    {
        return myWindowGeometry;
    }

    /**
     * Sets the geographic positions where the four corners of the xray window
     * intersect the globe.
     *
     * @param upperLeft The geographic position where the upper left corner of
     *            the xray window intersects with the globe.
     * @param upperRight The geographic position where the upper right corner of
     *            the xray window intersects with the globe.
     * @param lowerLeft The geographic position where the lower left corner of
     *            the xray window intersects with the globe.
     * @param lowerRight The geographic position where the lower right corner of
     *            the xray window intersects with the globe.
     * @param center The geographic position where the center of the xray window
     *            intersects with the globe.
     */
    public void setGeoPosition(GeographicPosition upperLeft, GeographicPosition upperRight, GeographicPosition lowerLeft,
            GeographicPosition lowerRight, GeographicPosition center)
    {
        myUpperLeftGeo = upperLeft;
        myUpperRightGeo = upperRight;
        myLowerLeftGeo = lowerLeft;
        myLowerRightGeo = lowerRight;
        myCenterGeo = center;
        setChanged();
        notifyObservers(GEO_POSITION);
    }

    /**
     * Sets a new screen position for the xray window.
     *
     * @param upperLeft The upper left screen position of the xray window.
     * @param upperRight The upper right screen position of the xray window.
     * @param lowerLeft The lower left screen position of the xray window.
     * @param lowerRight The lower right screen position of the xray window.
     */
    public void setScreenPosition(ScreenPosition upperLeft, ScreenPosition upperRight, ScreenPosition lowerLeft,
            ScreenPosition lowerRight)
    {
        if (myValidator == null || myValidator.isValid(upperLeft, upperRight, lowerLeft, lowerRight))
        {
            myUpperLeft = upperLeft;
            myUpperRight = upperRight;
            myLowerLeft = lowerLeft;
            myLowerRight = lowerRight;
            setChanged();
            notifyObservers(SCREEN_POSITION);
        }
    }

    /**
     * Sets the validator.
     *
     * @param validator The validator or null if don't need one.
     */
    public void setValidator(XrayModelValidator validator)
    {
        myValidator = validator;
    }

    /**
     * Sets the geometry used to display the xray window.
     *
     * @param windowGeometry the windowGeometry to set
     */
    public void setWindowGeometry(Geometry windowGeometry)
    {
        myWindowGeometry = windowGeometry;
    }
}
