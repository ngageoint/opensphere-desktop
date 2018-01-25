package io.opensphere.core.geometry;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.model.GeographicPosition;

/**
 * A class that contains information pertinent to a geometry that will display
 * for a grid coordinate.
 */
public class GridCoordinateInfo
{
    /** The adjusted height for the geometry. */
    private double myAdjustedHeight;

    /**
     * The center location where the geometry will be placed to represent the
     * grid.
     */
    private GeographicPosition myCenterLocation;

    /** The associated positions. */
    private final List<GeographicPosition> myPositions;

    /**
     * Default constructor.
     */
    public GridCoordinateInfo()
    {
        myPositions = new ArrayList<>();
    }

    /**
     * Standard getter.
     *
     * @return The calculated adjusted height.
     */
    public double getAdjustedHeight()
    {
        return myAdjustedHeight;
    }

    /**
     * Get the centroid from the collection of positions associated with this
     * grid. If this has not been found yet, calculate it.
     *
     * @return The center position.
     */
    public GeographicPosition getCenterLocation()
    {
        if (myCenterLocation == null && !myPositions.isEmpty())
        {
            if (myPositions.size() == 1)
            {
                myCenterLocation = myPositions.get(0);
            }
            else
            {
                myCenterLocation = GeographicPosition.findCentroid(myPositions);
            }
        }
        return myCenterLocation;
    }

    /**
     * Standard getter.
     *
     * @return The list of positions.
     */
    public List<GeographicPosition> getPositions()
    {
        return myPositions;
    }

    /**
     * Standard setter.
     *
     * @param adjustedHeight The new adjusted height.
     */
    public void setAdjustedHeight(double adjustedHeight)
    {
        myAdjustedHeight = adjustedHeight;
    }
}
