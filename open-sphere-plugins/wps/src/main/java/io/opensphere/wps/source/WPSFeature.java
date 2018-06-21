package io.opensphere.wps.source;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.opensphere.core.model.LatLonAlt;

/** A WPS feature over a particular region. */
public class WPSFeature
{
    /** The color. */
    private Color myColor;

    /** The down date. */
    private final Date myDownDate;

    /** The type of our geometries. */
    private GEOMTYPE myGeometryType;

    /** Locations for this feature. */
    private final List<LatLonAlt> myLocations;

    /** The name identifier. */
    private String myName;

    /** The map of wps feature properties. */
    private final Map<String, String> myProperties;

    /** The date associated with feature. */
    private final Date myTimeInstant;

    /** The up date. */
    private final Date myUpDate;

    /**
     * Constructor.
     *
     * @param positions The positions associated with this feature.
     */
    public WPSFeature(List<LatLonAlt> positions)
    {
        myLocations = new ArrayList<>(positions);
        myUpDate = new Date();
        myDownDate = new Date();
        myTimeInstant = new Date();
        myProperties = new HashMap<>();
    }

    /**
     * Add the given properties to the existing properties.
     *
     * @param properties The properties to add.
     */
    public void addProperties(Map<String, String> properties)
    {
        myProperties.putAll(properties);
    }

    /**
     * Get the color.
     *
     * @return The color.
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Get the down date.
     *
     * @return The down date.
     */
    public Date getDownDate()
    {
        return new Date(myDownDate.getTime());
    }

    /**
     * Get the type of the geometries.
     *
     * @return The type of the geometries.
     */
    public GEOMTYPE getGeometryType()
    {
        return myGeometryType;
    }

    /**
     * Get the feature locations.
     *
     * @return The locations.
     */
    public List<LatLonAlt> getLocations()
    {
        return myLocations;
    }

    /**
     * Get the name.
     *
     * @return The name identifier.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Get the properties.
     *
     * @return The feature properties.
     */
    public Map<String, String> getProperties()
    {
        return myProperties;
    }

    /**
     * Get the time instant.
     *
     * @return timeInstant The time instant.
     */
    @NonNull
    public Date getTimeInstant()
    {
        return new Date(myTimeInstant.getTime());
    }

    /**
     * Get the up date.
     *
     * @return The up date.
     */
    public Date getUpDate()
    {
        return new Date(myUpDate.getTime());
    }

    /**
     * Set the color.
     *
     * @param color The color.
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Set the down date.
     *
     * @param downDate The down date.
     */
    public void setDownDate(Date downDate)
    {
        myDownDate.setTime(downDate.getTime());
    }

    /**
     * Set the type of the geometries.
     *
     * @param type The geometry type.
     */
    public void setGeometryType(GEOMTYPE type)
    {
        myGeometryType = type;
    }

    /**
     * Set the name.
     *
     * @param name The name identifier.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Set the time instant.
     *
     * @param timeInstant The time instant.
     */
    public void setTimeInstant(Date timeInstant)
    {
        myTimeInstant.setTime(timeInstant.getTime());
    }

    /**
     * Set the up date.
     *
     * @param upDate The up date.
     */
    public void setUpDate(Date upDate)
    {
        myUpDate.setTime(upDate.getTime());
    }

    /** The simple types of feature geometries. */
    public enum GEOMTYPE
    {
        /** A line geometry. */
        LINE,

        /** A point geometry. */
        POINT,

        /** A polygon geometry. */
        POLYGON
    }
}
