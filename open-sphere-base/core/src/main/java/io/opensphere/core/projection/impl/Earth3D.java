package io.opensphere.core.projection.impl;

import io.opensphere.core.math.DefaultEllipsoid;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.SphereBody;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * A three-dimensional model of Earth using the WGS84 constants. The Earth is an
 * oblate spheroid, with two of its three radii being the same. This is also
 * called a biaxial ellipsoid.
 */
public class Earth3D extends GeographicBody3D
{
    /**
     * Constructor. TODO the constructor should take a set of constants rather
     * than assuming WGS84.
     */
    public Earth3D()
    {
        super("Earth", new DefaultEllipsoid(WGS84EarthConstants.RADIUS_EQUATORIAL_M, WGS84EarthConstants.RADIUS_EQUATORIAL_M,
                WGS84EarthConstants.RADIUS_POLAR_M));
    }

    @Override
    public Vector3d convertToModel(GeographicPosition inPos, Vector3d modelCenter)
    {
        LatLonAlt lla = inPos.getLatLonAlt();
        double φ = Math.toRadians(lla.getLatD());
        double λ = Math.toRadians(lla.getLonD());
        double altM = lla.getAltM();

        double cosλ = Math.cos(λ);
        double sinλ = Math.sin(λ);
        double cosφ = Math.cos(φ);
        double sinφ = Math.sin(φ);

        Vector3d vec;
        if (inPos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.ELLIPSOID)
        {
            double denom = Math.sqrt(cosφ * cosφ + WGS84EarthConstants.ONE_MINUS_FLATTENING_SQ * sinφ * sinφ);
            double semiMajOverDenom = WGS84EarthConstants.SEMI_MAJOR_AXIS_M / denom;
            double r = (semiMajOverDenom + altM) * cosφ;
            double s = (semiMajOverDenom * WGS84EarthConstants.ONE_MINUS_FLATTENING_SQ + altM) * sinφ;
            vec = new Vector3d(r * cosλ - modelCenter.getX(), r * sinλ - modelCenter.getY(), s - modelCenter.getZ());
        }
        else if (inPos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.ORIGIN)
        {
            vec = new Vector3d(altM * cosφ * cosλ - modelCenter.getX(), altM * cosφ * sinλ - modelCenter.getY(),
                    altM * sinφ - modelCenter.getZ());
        }
        else
        {
            throw new UnexpectedEnumException(inPos.getLatLonAlt().getAltitudeReference());
        }

        return vec;
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        // The normal calculation will fail when we are on the z-axis (divide by
        // 0) and is badly behaved near the axis because of floating point
        // error. So, when we are within a meter of the z-axis place the
        // geographic position on the pole.
        if (MathUtil.isZero(inPos.getX(), 1.) && MathUtil.isZero(inPos.getY(), 1.))
        {
            double latDeg = 90.;
            if (inPos.getZ() < 0)
            {
                latDeg = -90.;
            }
            double height;
            if (altReference == Altitude.ReferenceLevel.ORIGIN)
            {
                height = inPos.getLength();
            }
            else if (altReference == Altitude.ReferenceLevel.ELLIPSOID)
            {
                height = inPos.getLength() - WGS84EarthConstants.RADIUS_POLAR_M;
            }
            else
            {
                throw new UnexpectedEnumException(altReference);
            }
            // Any longitude value can be chosen since we are along the line
            // between the poles.
            return new GeographicPosition(
                    LatLonAlt.createFromDegreesMeters(latDeg, 0., height, Altitude.ReferenceLevel.ELLIPSOID));
        }

        double φ = Math.sqrt(inPos.getX() * inPos.getX() + inPos.getY() * inPos.getY());
        double λ = Math.atan2(WGS84EarthConstants.SEMI_MAJOR_AXIS_M * inPos.getZ(), WGS84EarthConstants.SEMI_MINOR_AXIS_M * φ);
        double sinλ = Math.sin(λ);
        double cosλ = Math.cos(λ);
        double lat = Math.atan2(
                inPos.getZ()
                + WGS84EarthConstants.SECOND_ECCENTRICITY_SQ * WGS84EarthConstants.SEMI_MINOR_AXIS_M * sinλ * sinλ * sinλ,
                φ - WGS84EarthConstants.FIRST_ECCENTRICITY_SQ * WGS84EarthConstants.SEMI_MAJOR_AXIS_M * cosλ * cosλ * cosλ);
        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double n = WGS84EarthConstants.SEMI_MAJOR_AXIS_M
                / Math.sqrt(1 - WGS84EarthConstants.FIRST_ECCENTRICITY_SQ * sinLat * sinLat);

        double height;
        if (altReference == Altitude.ReferenceLevel.ORIGIN)
        {
            height = φ / cosLat;
        }
        else if (altReference == Altitude.ReferenceLevel.ELLIPSOID)
        {
            height = φ / cosLat - n;
        }
        else
        {
            throw new UnexpectedEnumException(altReference);
        }

        double lon = Math.atan2(inPos.getY(), inPos.getX());

        return new GeographicPosition(
                LatLonAlt.createFromDegreesMeters(Math.toDegrees(lat), Math.toDegrees(lon), height, altReference));
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return SphereBody.getDefaultNormalAtPosition(inPos);
    }
}
