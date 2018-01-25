package io.opensphere.imagery.gdal;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/**
 * Mutable to pass around min/max lat long. Sector is not mutable. Only for
 * degree measurements.
 */
public class MiniSector
{
    /** The Constant FIVE_THOUSAND. */
    private static final double FIVE_THOUSAND = 5000.0;

    /** The Max lat. */
    private double myMaxLat = -1 * FIVE_THOUSAND;

    /** The Max lon. */
    private double myMaxLon = -1 * FIVE_THOUSAND;

    /** The Min lat. */
    private double myMinLat = FIVE_THOUSAND;

    /** The Min lon. */
    private double myMinLon = FIVE_THOUSAND;

    /**
     * Instantiates a new mini sector.
     */
    public MiniSector()
    {
    }

    /**
     * Accepts a sector and uses its min and max lats and lons in degrees only.
     *
     * @param gbb the gbb
     */
    MiniSector(GeographicBoundingBox gbb)
    {
        myMinLat = gbb.getLowerLeft().getLatLonAlt().getLatD();
        myMinLon = gbb.getLowerLeft().getLatLonAlt().getLonD();
        myMaxLat = gbb.getUpperRight().getLatLonAlt().getLatD();
        myMaxLon = gbb.getUpperRight().getLatLonAlt().getLonD();
    }

    /**
     * Get the max latitude.
     *
     * @return the max lat
     */
    public double getMaxLat()
    {
        return myMaxLat;
    }

    /**
     * Getter for max longitude.
     *
     * @return the max lon
     */
    public double getMaxLon()
    {
        return myMaxLon;
    }

    /**
     * Return min latitude.
     *
     * @return the min lat
     */
    public double getMinLat()
    {
        return myMinLat;
    }

    /**
     * Getter for min longitude.
     *
     * @return the min lon
     */
    public double getMinLon()
    {
        return myMinLon;
    }

    /**
     * Setter for max latitude.
     *
     * @param maxLat the new max lat
     */
    public void setMaxLat(double maxLat)
    {
        myMaxLat = maxLat;
    }

    /**
     * Setter for max longitude.
     *
     * @param maxLon the new max lon
     */
    public void setMaxLon(double maxLon)
    {
        myMaxLon = maxLon;
    }

    /**
     * Setter for min latitude.
     *
     * @param minLat the new min lat
     */
    public void setMinLat(double minLat)
    {
        myMinLat = minLat;
    }

    /**
     * Setter for min longitude.
     *
     * @param minLon the new min lon
     */
    public void setMinLon(double minLon)
    {
        myMinLon = minLon;
    }

    /**
     * Takes current min and max lats and lons and returns the Nasa World Wind
     * Sector.
     *
     * @return the geographic bounding box
     */
    public GeographicBoundingBox toSector()
    {
        LatLonAlt aMinLatLon = LatLonAlt.createFromDegrees(myMinLat, myMinLon);
        LatLonAlt aMaxLatLon = LatLonAlt.createFromDegrees(myMaxLat, myMaxLon);
        return new GeographicBoundingBox(new GeographicPosition(aMinLatLon), new GeographicPosition(aMaxLatLon));
    }

    /**
     * Writes the mins and maxes to a String for printing.
     *
     * @return the string
     */
    @Override
    public String toString()
    {
        return "MiniSector [minLat=" + myMinLat + ", maxLat=" + myMaxLat + ", minLon=" + myMinLon + ", maxLon=" + myMaxLon + "]";
    }
}
