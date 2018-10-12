package io.opensphere.core.projection;

import io.opensphere.core.math.DefaultSphere;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * A sphere model.
 */
public class SphereBody extends GeographicBody3D
{
    /**
     * Get a unit vector which is normal to the surface of the model at the
     * location.
     *
     * @param inPos The position at which to get the normal.
     * @return The unit normal vector.
     */
    public static Vector3d getDefaultNormalAtPosition(GeographicPosition inPos)
    {
        LatLonAlt lla = inPos.getLatLonAlt();
        double φ = Math.toRadians(lla.getLatD());
        double λ = Math.toRadians(lla.getLonD());

        double cosλ = Math.cos(λ);
        double sinλ = Math.sin(λ);
        double cosφ = Math.cos(φ);
        double sinφ = Math.sin(φ);

        // This should already be a unit vector
        return new Vector3d(cosφ * cosλ, cosφ * sinλ, sinφ);
    }

    /**
     * Construct the SphereBody.
     *
     * @param radius radius of the sphere.
     */
    public SphereBody(double radius)
    {
        super("SphereBody", new DefaultSphere(new Vector3d(0, 0, 0), radius));
    }

    @Override
    public Vector3d convertToModel(GeographicPosition inPos, Vector3d modelCenter)
    {
        double φ = Math.toRadians(inPos.getLatLonAlt().getLatD());
        double λ = Math.toRadians(inPos.getLatLonAlt().getLonD());

        double cosφ = Math.cos(φ);
        double sinφ = Math.sin(φ);
        double sinλ = Math.sin(λ);
        double cosλ = Math.cos(λ);

        double radius = getShape().getRadius();

        double x = radius * cosφ * cosλ;
        double y = radius * cosφ * sinλ;
        double z = radius * sinφ;

        if (inPos.getLatLonAlt().getAltM() > 0)
        {
            if (inPos.getLatLonAlt().getAltitudeReference() == ReferenceLevel.ELLIPSOID)
            {
                double scalar = 1.0 + inPos.getLatLonAlt().getAltM() / radius;
                return new Vector3d(x * scalar, y * scalar, z * scalar);
            }
            else if (inPos.getLatLonAlt().getAltitudeReference() == ReferenceLevel.ORIGIN)
            {
                return new Vector3d(x, y, z).getNormalized().multiply(inPos.getLatLonAlt().getAltM());
            }
            else
            {
                throw new UnexpectedEnumException(inPos.getLatLonAlt().getAltitudeReference());
            }
        }
        return new Vector3d(x - modelCenter.getX(), y - modelCenter.getY(), z - modelCenter.getZ());
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        double a = inPos.getX() * inPos.getX() + inPos.getY() * inPos.getY();
        double u = Math.sqrt(a + inPos.getZ() * inPos.getZ());

        double elevation;
        if (altReference == ReferenceLevel.ELLIPSOID)
        {
            elevation = u - getShape().getRadius();
        }
        else if (altReference == ReferenceLevel.ORIGIN)
        {
            elevation = u;
        }
        else
        {
            throw new UnexpectedEnumException(altReference);
        }

        double lat = Math.atan(inPos.getZ() / Math.sqrt(a));
        double lon = Math.atan2(inPos.getY(), inPos.getX());

        return new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(Math.toDegrees(lat), Math.toDegrees(lon), elevation, altReference));
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return getDefaultNormalAtPosition(inPos);
    }

    @Override
    public Sphere getShape()
    {
        return (Sphere)super.getShape();
    }
}
