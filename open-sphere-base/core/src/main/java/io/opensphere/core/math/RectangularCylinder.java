package io.opensphere.core.math;

import java.util.Collection;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A rectangular cylinder in three dimensions backed with double precision
 * values.
 */
public class RectangularCylinder implements Comparable<RectangularCylinder>
{
    /** Corner defined by the largest x, y and z values. */
    private final Vector3d myGreatestCorner;

    /**
     * The matrix which transforms points in model coordinates into points in
     * this rectangular cylinder's local space.
     */
    private transient volatile Matrix3d myInverseTransform;

    /** Corner defined by the smallest x, y and z values. */
    private final Vector3d myLeastCorner;

    /**
     * The matrix which transforms points in this rectangular cylinder's local
     * space into points in model coordinates..
     */
    private final Matrix3d myTransform;

    /**
     * Constructor.
     *
     * @param locations The locations which should be bounded by this
     *            rectangular cylinder. The axes will be the cardinal
     *            directions.
     */
    public RectangularCylinder(Collection<? extends Vector3d> locations)
    {
        myTransform = null;
        myInverseTransform = null;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        // Find the minimum and maximum values for the locations transformed
        // into local coordinates.
        for (Vector3d loc : locations)
        {
            if (!Double.isNaN(loc.getX()))
            {
                minX = Math.min(minX, loc.getX());
                maxX = Math.max(maxX, loc.getX());
            }
            if (!Double.isNaN(loc.getY()))
            {
                minY = Math.min(minY, loc.getY());
                maxY = Math.max(maxY, loc.getY());
            }
            if (!Double.isNaN(loc.getZ()))
            {
                minZ = Math.min(minZ, loc.getZ());
                maxZ = Math.max(maxZ, loc.getZ());
            }
        }

        myLeastCorner = new Vector3d(minX, minY, minZ);
        myGreatestCorner = new Vector3d(maxX, maxY, maxZ);
    }

    /**
     * Constructor.
     *
     * @param locations The locations which should be bounded by this
     *            rectangular cylinder. The axes will be the cardinal
     *            directions.
     * @param xAxis The x-axis orientation for this rectangular cylinder.
     * @param yAxis The y-axis orientation for this rectangular cylinder.
     * @param zAxis The z-axis orientation for this rectangular cylinder.
     */
    public RectangularCylinder(Collection<? extends Vector3d> locations, Vector3d xAxis, Vector3d yAxis, Vector3d zAxis)
    {
        myTransform = new Matrix3d();
        myTransform.fromAxes(xAxis, yAxis, zAxis);
        myInverseTransform = myTransform.invert();

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;

        // Find the minimum and maximum values for the locations transformed
        // into local coordinates.
        for (Vector3d loc : locations)
        {
            Vector3d transformedLoc = myInverseTransform.mult(loc);
            if (!Double.isNaN(transformedLoc.getX()))
            {
                minX = Math.min(minX, transformedLoc.getX());
                maxX = Math.max(maxX, transformedLoc.getX());
            }
            if (!Double.isNaN(transformedLoc.getY()))
            {
                minY = Math.min(minY, transformedLoc.getY());
                maxY = Math.max(maxY, transformedLoc.getY());
            }
            if (!Double.isNaN(transformedLoc.getZ()))
            {
                minZ = Math.min(minZ, transformedLoc.getZ());
                maxZ = Math.max(maxZ, transformedLoc.getZ());
            }
        }

        myLeastCorner = new Vector3d(minX, minY, minZ);
        myGreatestCorner = new Vector3d(maxX, maxY, maxZ);
    }

    /**
     * Constructor.
     *
     * @param leastCorner The corner which is at the minimum x, y and z values.
     * @param greatestCorner The corner which is at the maximum x, y and z
     *            values.
     */
    public RectangularCylinder(Vector3d leastCorner, Vector3d greatestCorner)
    {
        myLeastCorner = Utilities.checkNull(leastCorner, "leastCorner");
        myGreatestCorner = Utilities.checkNull(greatestCorner, "greatestCorner");
        myTransform = null;
        myInverseTransform = null;
    }

    @Override
    public int compareTo(RectangularCylinder other)
    {
        int result = myLeastCorner.compareTo(other.myLeastCorner);

        if (result == 0)
        {
            result = myGreatestCorner.compareTo(other.myGreatestCorner);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        RectangularCylinder other = (RectangularCylinder)obj;
        return myLeastCorner.equals(other.myLeastCorner) && myGreatestCorner.equals(other.myGreatestCorner);
    }

    /**
     * Get the center point of the rectangular cylinder.
     *
     * @return The center point of the rectangular cylinder.
     */
    public Vector3d getCenter()
    {
        if (myTransform != null)
        {
            return myTransform.mult(myLeastCorner.interpolate(myGreatestCorner, .5));
        }
        return myLeastCorner.interpolate(myGreatestCorner, .5);
    }

    /**
     * Get the greatestCorner.
     *
     * @return the greatestCorner
     */
    public Vector3d getGreatestCorner()
    {
        return myGreatestCorner;
    }

    /**
     * Get the leastCorner.
     *
     * @return the leastCorner
     */
    public Vector3d getLeastCorner()
    {
        return myLeastCorner;
    }

    /**
     * Get the span of the cylinder in the direction of the axis.
     *
     * @param axis The axis of the span.
     * @return The span of the cylinder in the direction of the axis.
     */
    public double getSpan(Vector3d axis)
    {
        Vector3d center = myLeastCorner.interpolate(myGreatestCorner, .5);

        Vector3d transformedAxis;
        if (myInverseTransform != null)
        {
            transformedAxis = myInverseTransform.mult(axis);
        }
        else
        {
            transformedAxis = axis;
        }

        // Since it is symmetrical, use just the bottom side and double it.
        Collection<Vector3d> bottomCorners = New.collection();
        bottomCorners.add(myLeastCorner);
        bottomCorners.add(new Vector3d(myGreatestCorner.getX(), myLeastCorner.getY(), myLeastCorner.getZ()));
        bottomCorners.add(new Vector3d(myGreatestCorner.getX(), myGreatestCorner.getY(), myLeastCorner.getZ()));
        bottomCorners.add(new Vector3d(myLeastCorner.getX(), myGreatestCorner.getY(), myLeastCorner.getZ()));

        double halfDistance = 0;
        for (Vector3d corner : bottomCorners)
        {
            halfDistance = Math.max(halfDistance, Math.abs(corner.subtract(center).dot(transformedAxis)));
        }

        return halfDistance * 2.;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myGreatestCorner.hashCode();
        result = prime * result + myLeastCorner.hashCode();
        return result;
    }

    /**
     * Get the inverse transform.
     *
     * @return The inverse transform.
     */
    protected Matrix3d getInverseTransform()
    {
        return myInverseTransform;
    }
}
