package io.opensphere.core.projection;

import io.opensphere.core.math.DefaultSphere;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.viewer.Viewer;

/**
 * Base class for all rectangular projections. A rectangular projection has
 * latitude lines that are parallel to each other, and longitude lines that are
 * perpendicular to the latitude lines.
 */
public abstract class AbstractRectangularProjection extends AbstractGeographicProjection
{
    @Override
    public Ellipsoid getBoundingEllipsoid(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter, boolean forceGenerate)
    {
        return getBoundingSphere(bbox, modelCenter, forceGenerate);
    }

    @Override
    public Sphere getBoundingSphere(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter, boolean forceGenerate)
    {
        GeographicBoundingBox geoBox = (GeographicBoundingBox)bbox;
        Vector3d[] vectors = new Vector3d[2];
        vectors[0] = convertToModel(geoBox.getLowerLeft(), modelCenter);
        vectors[1] = convertToModel(geoBox.getUpperRight(), modelCenter);

        double x0 = vectors[0].getX();
        double x1 = vectors[1].getX();
        double y0 = vectors[0].getY();
        double y1 = vectors[1].getY();
        double z0 = vectors[0].getZ();
        double z1 = vectors[1].getZ();
        double x = (x0 + x1) / 2f;
        double y = (y0 + y1) / 2f;
        double z = (z0 + z1) / 2f;
        double radius = Math.max(MathUtil.DBL_EPSILON, Math.max(x1 - x, Math.max(y1 - y, z1 - z)));
        return new DefaultSphere(new Vector3d(x, y, z), radius);
    }

    @Override
    public ElevationManager getElevationManager()
    {
        // 2-D projections do not support elevation.
        return null;
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
