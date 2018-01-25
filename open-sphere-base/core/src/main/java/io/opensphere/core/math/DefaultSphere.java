package io.opensphere.core.math;

import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;

/**
 * Sphere shape.
 */
public class DefaultSphere extends AbstractShape3D implements Sphere
{
    /** How many bytes used internally by one of these objects. */
    public static final int SIZE_BYTES = MathUtil.roundUpTo(
            Constants.OBJECT_SIZE_BYTES + Constants.DOUBLE_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES,
            Constants.MEMORY_BLOCK_SIZE_BYTES) + Vector3d.SIZE_BYTES;

    /** The center. */
    private final Vector3d myCenter;

    /** The radius. */
    private final double myRadius;

    /**
     * Get the minimum sphere which bounds the points.
     *
     * @param ptA first point
     * @param ptB second point
     * @param ptC third point
     * @return bounding sphere
     */
    public static DefaultSphere genMinimumBoundingSphere(Vector3d ptA, Vector3d ptB, Vector3d ptC)
    {
        if (ptA == null || ptB == null || ptC == null)
        {
            return null;
        }
        Vector3d ab = ptB.subtract(ptA);
        Vector3d ac = ptC.subtract(ptA);
        double abLenSq = ab.getLengthSquared();
        double dotABAC = ab.dot(ac);
        double acLenSq = ac.getLengthSquared();
        double d = 2.0 * (abLenSq * acLenSq - dotABAC * dotABAC);
        Vector3d referencePt = ptA;
        final Vector3d center;
        if (MathUtil.isZero(d))
        {
            // a, b, and c lie on a line.
            Vector3d bc = ptC.subtract(ptB);
            double bcLenSq = bc.getLengthSquared();
            double maxLenSq = Math.max(abLenSq, Math.max(bcLenSq, acLenSq));
            if (abLenSq == maxLenSq)
            {
                center = ab.add(ab.multiply(Math.sqrt(abLenSq) * 0.5));
            }
            else if (bcLenSq == maxLenSq)
            {
                center = bc.add(bc.multiply(Math.sqrt(bcLenSq) * 0.5));
            }
            else
            {
                center = ac.add(ac.multiply(Math.sqrt(acLenSq) * 0.5));
            }
        }
        else
        {
            double s = (abLenSq * acLenSq - acLenSq * dotABAC) / d;
            double t = (acLenSq * abLenSq - abLenSq * dotABAC) / d;
            // s controls height over AC, t over AB, (1 - s - t) over BC
            if (s <= 0.0)
            {
                // 0.5 * (a + c)
                center = ptA.add(ptC).multiply(0.5);
            }
            else if (t <= 0.0)
            {
                // 0.5 * (a + b)
                center = ptA.add(ptB).multiply(0.5);
            }
            else if (s + t >= 1.0)
            {
                // 0.5 * (b + c)
                center = ptB.add(ptC).multiply(0.5);
                referencePt = ptB;
            }
            else
            {
                // a + s*(b - a) + t*(c - a);
                center = ptA.add(ptB.subtract(ptA).multiply(s)).add(ptC.subtract(ptA).multiply(t));
            }
        }

        double radius = center.subtract(referencePt).getLength();

        // TODO SED - We should check to make sure that this is working by
        // making sure that the points are within the sphere.

        return new DefaultSphere(center, radius);
    }

    /**
     * Construct me.
     *
     * @param center center
     * @param radius radius
     */
    public DefaultSphere(Vector3d center, double radius)
    {
        myCenter = center;
        if (radius <= 0.)
        {
            throw new IllegalArgumentException("Radius cannot be negative.");
        }
        myRadius = radius;
    }

    @Override
    public boolean contains(Vector3d position)
    {
        return myCenter.distance(position) <= myRadius;
    }

    @Override
    public Vector3d directionToLocal(Vector3d direction)
    {
        return direction;
    }

    @Override
    public Vector3d directionToModel(Vector3d direction)
    {
        return direction;
    }

    @Override
    public double getBoundingRadius()
    {
        return getRadius();
    }

    @Override
    public Vector3d getCenter()
    {
        return myCenter;
    }

    @Override
    public double getHeading(Vector3d location, Vector3d direction)
    {
        Vector3d normalLoc = location.getNormalized();
        if (MathUtil.isZero(Math.abs(normalLoc.dot(direction)) - 1.)
                || MathUtil.isZero(location.getX()) && MathUtil.isZero(location.getY()))
        {
            return Double.NaN;
        }

        Vector3d normalOne = normalLoc.cross(direction).getNormalized();
        double sign = Math.signum(normalOne.cross(Vector3d.UNIT_Z).dot(direction));
        double angle = MathUtil.HALF_PI - normalOne.getAngleDifferenceUnit(Vector3d.UNIT_Z) * sign;

        return angle < 0. ? angle + MathUtil.TWO_PI : angle;
    }

    @Override
    public Vector3d getIntersection(Ray3d ray)
    {
        Vector3d translatedPosition = ray.getPosition().subtract(myCenter);
        double b = translatedPosition.dot(ray.getDirection());
        double c = translatedPosition.dot(translatedPosition) - Math.pow(myRadius, 2);
        double d = Math.pow(b, 2) - c;
        if (d > 0)
        {
            // should the length be divided by 2?
            double length = -b - Math.sqrt(d);
            return ray.getDirection().multiply(length).add(translatedPosition);
        }

        return null;
    }

    @Override
    public double getRadius()
    {
        return myRadius;
    }

    @Override
    public long getSizeBytes()
    {
        return SIZE_BYTES;
    }

    @Override
    public Vector3d getTangent(Vector3d normal)
    {
        return getCenter().add(normal.getNormalized().multiply(getRadius()));
    }

    @Override
    public Vector3d getXAxis()
    {
        return Vector3d.UNIT_X;
    }

    @Override
    public Vector3d getYAxis()
    {
        return Vector3d.UNIT_Y;
    }

    @Override
    public Vector3d getZAxis()
    {
        return Vector3d.UNIT_Z;
    }

    @Override
    public boolean isSphere()
    {
        return true;
    }

    @Override
    public Vector3d localToModel(Vector3d position)
    {
        return position.add(getCenter());
    }

    @Override
    public Vector3d modelToLocal(Vector3d position)
    {
        return position.subtract(getCenter());
    }

    @Override
    public Vector3d normalToLocal(Vector3d normal)
    {
        return normal;
    }

    @Override
    public boolean overlaps(Sphere otherSphere)
    {
        return myCenter.distance(otherSphere.getCenter()) < myRadius + otherSphere.getRadius();
    }
}
