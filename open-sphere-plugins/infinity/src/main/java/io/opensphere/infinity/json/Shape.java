package io.opensphere.infinity.json;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.model.GeographicBoundingBox;

/** Elasticsearch shape JSON bean. */
public class Shape
{
    /** The type. */
    private String myType;

    /** The coordinates. */
    private double[][] myCoordinates;

    /**
     * Constructor.
     */
    public Shape()
    {
    }

    /**
     * Constructor.
     *
     * @param type the type
     * @param geometry the geometry
     */
    public Shape(String type, Geometry geometry)
    {
        myType = type;
        GeographicBoundingBox queryBbox = BoundingBox.getMinimumBoundingBoxLLA(geometry.getCoordinates());
        myCoordinates = new double[][] { { queryBbox.getMinLonD(), queryBbox.getMaxLatD() },
            { BoundingBox.adjustLon(queryBbox.getMaxLonD()), queryBbox.getMinLatD() } };
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType()
    {
        return myType;
    }

    /**
     * Sets the type.
     *
     * @param type the type
     */
    public void setType(String type)
    {
        myType = type;
    }

    /**
     * Gets the coordinates.
     *
     * @return the coordinates
     */
    public double[][] getCoordinates()
    {
        return myCoordinates;
    }

    /**
     * Sets the coordinates.
     *
     * @param coordinates the coordinates
     */
    public void setCoordinates(double[][] coordinates)
    {
        myCoordinates = coordinates;
    }
}
