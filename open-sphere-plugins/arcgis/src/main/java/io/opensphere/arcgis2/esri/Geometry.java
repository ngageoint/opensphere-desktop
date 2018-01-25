package io.opensphere.arcgis2.esri;

/** A geometry. */
public class Geometry
{
    /** The X. */
    private double myX;

    /** The Y. */
    private double myY;

    /** The rings. */
    private double[][][] myRings;

    /** The paths. */
    private double[][][] myPaths;

    /**
     * Gets the x.
     *
     * @return the x
     */
    public double getX()
    {
        return myX;
    }

    /**
     * Sets the x.
     *
     * @param x the x
     */
    public void setX(double x)
    {
        myX = x;
    }

    /**
     * Gets the y.
     *
     * @return the y
     */
    public double getY()
    {
        return myY;
    }

    /**
     * Sets the y.
     *
     * @param y the y
     */
    public void setY(double y)
    {
        myY = y;
    }

    /**
     * Gets the rings.
     *
     * @return the rings
     */
    public double[][][] getRings()
    {
        return myRings;
    }

    /**
     * Sets the rings.
     *
     * @param rings the rings
     */
    public void setRings(double[][][] rings)
    {
        myRings = rings;
    }

    /**
     * Gets the paths.
     *
     * @return The paths or polylines.
     */
    public double[][][] getPaths()
    {
        return myPaths;
    }

    /**
     * Sets the paths.
     *
     * @param paths Sets the paths or polylines.
     */
    public void setPaths(double[][][] paths)
    {
        myPaths = paths;
    }

    @Override
    public String toString()
    {
        return "Geometry [x=" + myX + ", y=" + myY + "]";
    }
}
