package io.opensphere.core.projection.impl;

import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.projection.AbstractGeographicProjection;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.viewer.Viewer;

/**
 * A Hammer projection.
 */
public class HammerProjection extends AbstractGeographicProjection
{
    @Override
    public TesseraList<GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition lla1, GeographicPosition lla2,
            GeographicPosition lla3, GeographicPosition lla4, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        return quadTessellate(lla1, lla2, lla3, lla4, 3, 3, modelCenter);
    }

    @Override
    public Vector3d convertToModel(GeographicPosition lla, Vector3d modelCenter)
    {
        double latR = Math.toRadians(lla.getLatLonAlt().getLatD());
        double lonRby2 = Math.toRadians(lla.getLatLonAlt().getLonD()) / 2.;
        double cosLat = Math.cos(latR);
        double denom = Math.sqrt(1. + cosLat * Math.cos(lonRby2));
        double x = 2. * cosLat * Math.sin(lonRby2) / denom;
        double y = Math.sin(latR) / denom;

        return new Vector3d(x - modelCenter.getX(), y - modelCenter.getY(), 0f);
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        double x = inPos.getX();
        double y = inPos.getY();
        final double a = .25;
        double z = Math.sqrt(2 - a * x * x - y * y);
        double lat = Math.toDegrees(Math.asin(z * y));
        double lon = Math.toDegrees(2. * Math.atan(z * x / (2. * z * z - 2.)));
        return new GeographicPosition(LatLonAlt.createFromDegreesKm(lat, lon, 0., altReference));
    }

    @Override
    public ElevationManager getElevationManager()
    {
        // 2-D projections do not support elevation.
        return null;
    }

    @Override
    public String getName()
    {
        return "Hammer";
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return new Vector3d(0., 0., 1.);
    }

    @Override
    public Vector3d getSurfaceIntersection(Vector3d pointA, Vector3d pointB)
    {
        return new Vector3d(pointB);
    }

    @Override
    public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
    {
        return new Vector3d(ray.getPosition());
    }
}
