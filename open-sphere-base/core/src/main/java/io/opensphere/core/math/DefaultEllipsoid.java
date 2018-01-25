package io.opensphere.core.math;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Ellipsoid shape.
 */
public class DefaultEllipsoid extends AbstractShape3D implements Ellipsoid
{
    /** The radius of the sphere which bounds this ellipsoid. */
    private final double myBoundingRadius;

    /** The position which is at the center of the ellipsoid. */
    private final Vector3d myCenter;

    /**
     * A matrix which transforms normal vectors in model coordinates to
     * equivalent normal vectors in ellipsoid coordinates.
     */
    private transient volatile Matrix3d myInverseNormalTransform;

    /**
     * The matrix which transforms points on the ellipsoid to points on the unit
     * sphere.
     */
    private transient volatile Matrix3d myInverseTransform;

    /**
     * The matrix which transforms points on the unit sphere to points on the
     * ellipsoid.
     */
    private final Matrix3d myTransform;

    /**
     * Construct me as a unit sphere.
     */
    public DefaultEllipsoid()
    {
        this(Vector3d.ORIGIN, Vector3d.UNIT_X, Vector3d.UNIT_Y, Vector3d.UNIT_Z);
    }

    /**
     * Construct me.
     *
     * @param radius radius
     * @param radius2 radius
     * @param radius3 radius
     */
    public DefaultEllipsoid(double radius, double radius2, double radius3)
    {
        this(Vector3d.ORIGIN, new Vector3d(radius, 0., 0.), new Vector3d(0., radius2, 0.), new Vector3d(0., 0., radius3));
    }

    /**
     * Construct me.
     *
     * @param center The center of the ellipsoid.
     * @param radius radius
     * @param radius2 radius
     * @param radius3 radius
     */
    public DefaultEllipsoid(Vector3d center, double radius, double radius2, double radius3)
    {
        this(center, new Vector3d(radius, 0., 0.), new Vector3d(0., radius2, 0.), new Vector3d(0., 0., radius3));
    }

    /**
     * Constructor.
     *
     * @param center The center of the ellipsoid.
     * @param xAxis The x axis in the ellipsoid's local coordinate system.
     * @param yAxis The x axis in the ellipsoid's local coordinate system.
     * @param zAxis The x axis in the ellipsoid's local coordinate system.
     */
    public DefaultEllipsoid(Vector3d center, Vector3d xAxis, Vector3d yAxis, Vector3d zAxis)
    {
        myTransform = new Matrix3d();
        myTransform.fromAxes(xAxis, yAxis, zAxis);
        myCenter = center;
        myBoundingRadius = Math.max(zAxis.getLength(), Math.max(xAxis.getLength(), yAxis.getLength()));
    }

    @Override
    public Vector3d directionToLocal(Vector3d direction)
    {
        return getInverseTransform().mult(direction);
    }

    @Override
    public Vector3d directionToModel(Vector3d direction)
    {
        return myTransform.mult(direction);
    }

    @Override
    public double getBoundingRadius()
    {
        return myBoundingRadius;
    }

    @Override
    public Vector3d getCenter()
    {
        return myCenter;
    }

    @Override
    public double getHeading(Vector3d location, Vector3d direction)
    {
        Vector3d tLocation = getInverseTransform().mult(location);
        Vector3d tDirection = getInverseTransform().mult(direction).getNormalized();

        Vector3d normalLoc = tLocation.getNormalized();
        Vector3d normalZ = getZAxis().getNormalized();
        if (MathUtil.isZero(Math.abs(normalLoc.dot(tDirection)) - 1.) || MathUtil.isZero(Math.abs(normalLoc.dot(normalZ)) - 1.))
        {
            return Double.NaN;
        }

        Vector3d normalOne = normalLoc.cross(tDirection).getNormalized();
        double sign = Math.signum(normalOne.cross(normalZ).dot(tDirection));
        double angle = MathUtil.HALF_PI - normalOne.getAngleDifferenceUnit(normalZ) * sign;

        return angle < 0. ? angle + MathUtil.TWO_PI : angle;
    }

    @Override
    public Vector3d getIntersection(Ray3d ray)
    {
        Vector3d dir = getInverseTransform().mult(ray.getDirection().subtract(myCenter));
        Vector3d pos = getInverseTransform().mult(ray.getPosition().subtract(myCenter));

        Vector3d unitSphInter = new DefaultSphere(Vector3d.ORIGIN, 1d).getIntersection(new Ray3d(pos, dir));
        if (unitSphInter != null)
        {
            return myTransform.mult(unitSphInter).add(myCenter);
        }
        return null;
    }

    @Override
    public long getSizeBytes()
    {
        int size = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + 4 * Constants.REFERENCE_SIZE_BYTES,
                Constants.MEMORY_BLOCK_SIZE_BYTES) + Matrix3d.SIZE_BYTES + Vector3d.SIZE_BYTES;
        if (myInverseTransform != null)
        {
            size += Matrix3d.SIZE_BYTES;
        }
        if (myInverseNormalTransform != null)
        {
            size += Matrix3d.SIZE_BYTES;
        }
        return size;
    }

    @Override
    public Vector3d getTangent(Vector3d normal)
    {
        return localToModel(normalToLocal(normal).getNormalized());
    }

    @Override
    public Vector3d getXAxis()
    {
        return myTransform.getColumn(0);
    }

    @Override
    public Vector3d getYAxis()
    {
        return myTransform.getColumn(1);
    }

    @Override
    public Vector3d getZAxis()
    {
        return myTransform.getColumn(2);
    }

    @Override
    public boolean isSphere()
    {
        return getXAxis().getLengthSquared() == getYAxis().getLengthSquared()
                && getXAxis().getLengthSquared() == getZAxis().getLengthSquared()
                && getXAxis().cross(getYAxis()).equals(getZAxis());
    }

    @Override
    public Vector3d localToModel(Vector3d position)
    {
        return myTransform.mult(position).add(myCenter);
    }

    @Override
    public Vector3d modelToLocal(Vector3d position)
    {
        // Translate to the center first, then apply the inverse transform
        return getInverseTransform().mult(position.subtract(myCenter));
    }

    @Override
    public Vector3d normalToLocal(Vector3d normal)
    {
        return getInverseNormalTransform().mult(normal);
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append(getClass().getSimpleName()).append("[center").append(getCenter()).append(" X")
                .append(getXAxis()).append(" Y").append(getYAxis()).append(" Z").append(getZAxis()).toString();
    }

    /**
     * Get the inverse transform.
     *
     * @return The inverse transform.
     */
    protected Matrix3d getInverseTransform()
    {
        if (myInverseTransform == null)
        {
            myInverseTransform = myTransform.invert();
        }
        return myInverseTransform;
    }

    /**
     * Get a transformation matrix that, when multiplied by a normal vector in
     * model coordinates, will give an equivalent normal vector in the
     * ellipsoid's local coordinate system.
     *
     * @return The inverse normal transform.
     */
    private Matrix3d getInverseNormalTransform()
    {
        if (myInverseNormalTransform == null)
        {
            myInverseNormalTransform = myTransform.transpose();
        }
        return myInverseNormalTransform;
    }
}
