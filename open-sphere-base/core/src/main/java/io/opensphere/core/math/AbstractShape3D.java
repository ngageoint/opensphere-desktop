package io.opensphere.core.math;

import io.opensphere.core.util.MathUtil;

/**
 * Abstract base class for 3-D shapes.
 */
public abstract class AbstractShape3D extends AbstractShape implements Shape3D
{
    @Override
    public double getMapOrientationHeading(Vector3d direction, Vector3d orientation)
    {
        Vector3d zProjectedOntoHeadingPlane = direction.cross(getZAxis()).cross(direction);
        double length = zProjectedOntoHeadingPlane.getLength();
        if (MathUtil.isZero(length))
        {
            return Double.NaN;
        }
        zProjectedOntoHeadingPlane = zProjectedOntoHeadingPlane.divide(length);
        double angle = Math.signum(orientation.cross(direction).dot(zProjectedOntoHeadingPlane))
                * orientation.getAngleDifferenceUnit(zProjectedOntoHeadingPlane.getNormalized());
        return angle < 0. ? angle + MathUtil.TWO_PI : angle;
    }
}
