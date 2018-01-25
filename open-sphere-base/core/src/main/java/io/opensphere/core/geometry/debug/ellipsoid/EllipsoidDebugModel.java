package io.opensphere.core.geometry.debug.ellipsoid;

import java.awt.Color;
import java.io.Serializable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Model used by the debug UI that allows the user to draw ellipsoids.
 */
public class EllipsoidDebugModel implements Serializable
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Altitude of ellipsoid from terrain.
     */
    private final DoubleProperty myAltitude = new SimpleDoubleProperty();

    /**
     * Semi major axis.
     */
    private final DoubleProperty myAxisA = new SimpleDoubleProperty();

    /**
     * Horizontal semi minor axis.
     */
    private final DoubleProperty myAxisB = new SimpleDoubleProperty();

    /**
     * Vertical semi minor axis.
     */
    private final DoubleProperty myAxisC = new SimpleDoubleProperty();

    /**
     * Color of ellipsoid.
     */
    private Color myColor;

    /**
     * Orientation of the ellipsoid.
     */
    private final DoubleProperty myHeading = new SimpleDoubleProperty();

    /**
     * Latitude location of ellipsoid.
     */
    private final DoubleProperty myLatitude = new SimpleDoubleProperty();

    /**
     * Longitude location of ellipsoid.
     */
    private final DoubleProperty myLongitude = new SimpleDoubleProperty();

    /**
     * The opacity of the ellipsoid.
     */
    private final IntegerProperty myOpacity = new SimpleIntegerProperty(255);

    /**
     * Pitch of the ellipsoid.
     */
    private final DoubleProperty myPitch = new SimpleDoubleProperty();

    /**
     * The number of polygons to use when rendering the ellipsoid.
     */
    private final IntegerProperty myQuality = new SimpleIntegerProperty(10);

    /**
     * Indicates if the previously created ellipsoid should be removed from the
     * globe.
     */
    private final BooleanProperty myRemovePrevious = new SimpleBooleanProperty(true);

    /**
     * Roll angle of the ellipsoid.
     */
    private final DoubleProperty myRoll = new SimpleDoubleProperty();

    /**
     * Indicates if we should render the ellipsoid with lighting or no lighting.
     */
    private final BooleanProperty myUseLighting = new SimpleBooleanProperty(true);

    /**
     * Gets the altitude of ellipsoid from terrain.
     *
     * @return the altitude.
     */
    public DoubleProperty getAltitude()
    {
        return myAltitude;
    }

    /**
     * Gets Semi major axis.
     *
     * @return the Semi major axis.
     */
    public DoubleProperty getAxisA()
    {
        return myAxisA;
    }

    /**
     * Gets Horizontal semi minor axis.
     *
     * @return Horizontal semi minor axis.
     */
    public DoubleProperty getAxisB()
    {
        return myAxisB;
    }

    /**
     * Gets vertical semi minor axis.
     *
     * @return Vertical semi minor axis.
     */
    public DoubleProperty getAxisC()
    {
        return myAxisC;
    }

    /**
     * Gets the color of ellipsoid.
     *
     * @return the color.
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Gets the orientation of the ellipsoid.
     *
     * @return the heading.
     */
    public DoubleProperty getHeading()
    {
        return myHeading;
    }

    /**
     * Gets Latitude location of ellipsoid.
     *
     * @return the latitude.
     */
    public DoubleProperty getLatitude()
    {
        return myLatitude;
    }

    /**
     * Gets longitude location of ellipsoid.
     *
     * @return the longitude.
     */
    public DoubleProperty getLongitude()
    {
        return myLongitude;
    }

    /**
     * Gets the opacity of the geometry.
     *
     * @return The opacity of the geometry.
     */
    public IntegerProperty getOpacity()
    {
        return myOpacity;
    }

    /**
     * Gets the pitch angle of the ellipsoid.
     *
     * @return the pitch The pitch angle.
     */
    public DoubleProperty getPitch()
    {
        return myPitch;
    }

    /**
     * Gets the quality of the ellipsoid to render.
     *
     * @return The quality of the ellipsoid.
     */
    public IntegerProperty getQuality()
    {
        return myQuality;
    }

    /**
     * Gets the roll angle of the ellipsoid.
     *
     * @return the roll The roll angle.
     */
    public DoubleProperty getRoll()
    {
        return myRoll;
    }

    /**
     * Indicates if the previously created ellipsoid should be removed from the
     * globe.
     *
     * @return True if the previous ellipsoid should be removed, false
     *         otherwise.
     */
    public BooleanProperty isRemovePrevious()
    {
        return myRemovePrevious;
    }

    /**
     * Indicates if we should render the ellipsoid with lighting or no lighting.
     *
     * @return True if the ellipsoid will be rendered with light, false if
     */
    public BooleanProperty isUseLighting()
    {
        return myUseLighting;
    }

    /**
     * Sets the color.
     *
     * @param color the color to set.
     */
    public void setColor(Color color)
    {
        myColor = color;
    }
}
