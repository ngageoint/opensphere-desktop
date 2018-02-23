package io.opensphere.core.geometry;

import java.util.List;

import io.opensphere.core.geometry.EllipseGeometry.ProjectedBuilder;
import io.opensphere.core.math.DefaultEllipsoid;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;

/** Utilities for building ellipses. */
public final class EllipseGeometryUtilities
{
    /**
     * Create the vertices in the projection. For ellipses whose centers have a
     * non-zero altitude, the ellipse will enlarge as the altitude increases.
     *
     * @param builder The geometry builder.
     * @return The projected vertices.
     */
    public static List<? extends GeographicPosition> createProjectedVertices(ProjectedBuilder builder)
    {
        return createProjectedVertices(builder, 180);
    }

    /**
     * Create the vertices in the projection. For ellipses whose centers have a
     * non-zero altitude, the ellipse will enlarge as the altitude increases.
     *
     * @param builder The geometry builder.
     * @param arcAmountDeg The amount of the arc to draw on either side from the angle (in degrees).
     * @return The projected vertices.
     */
    public static List<? extends GeographicPosition> createProjectedVertices(ProjectedBuilder builder, double arcAmountDeg)
    {
        GeographicPosition center = builder.getCenter();
        double angle = builder.getAngle() * MathUtil.DEG_TO_RAD;
        int vertexCount = builder.getVertexCount();
        double semiMajorAxis = builder.getSemiMajorAxis();
        double semiMinorAxis = builder.getSemiMinorAxis();
        Projection projection = builder.getProjection();

        // Create an ellipsoid to use for projecting the points into model
        // coordinates.
        Ellipsoid ellipsoid = createEllipsoid(projection, center, angle, semiMajorAxis, semiMinorAxis);

        double angleStep = MathUtil.TWO_PI / vertexCount;

        List<GeographicPosition> vertices = New.list();
        final double halfPi = Math.PI / 2;
        double arcAmount = Math.toRadians(arcAmountDeg);
        double endTheta = halfPi + arcAmount;
        for (double theta = halfPi - arcAmount; theta <= endTheta; theta += angleStep)
        {
            double x = Math.cos(theta);
            double y = Math.sin(theta);
            Vector3d modelPt = ellipsoid.localToModel(new Vector3d(x, y, 0.));
            GeographicPosition geoWithAlt = projection.convertToPosition(modelPt, ReferenceLevel.TERRAIN);
            // Remove the altitude from the position so that the ellipsoid
            // renders flat on the model. This contracts the radius slightly. If
            // we want to project it flat, the model position should be moved in
            // the opposite direction of the ellipsoid's z-axis.
            LatLonAlt location = LatLonAlt.createFromDegreesMeters(geoWithAlt.getLatLonAlt().getLatD(),
                    geoWithAlt.getLatLonAlt().getLonD(), center.getAlt().getMeters(), center.getAlt().getReferenceLevel());
            GeographicPosition geo = new GeographicPosition(location);
            vertices.add(geo);
        }
        return vertices;
    }

    /**
     * Create an ellipsoid whose z-axis is perpendicular to the surface of the
     * model and whose y-axis is rotated clockwise from north by the given
     * orientation. This method does not respect the center's altitude, but
     * instead creates an ellipsoid whose center is on the surface of the model.
     *
     * @param projection The projection in which the ellipse will be calculated.
     * @param center The location of the center of the ellipsoid.
     * @param orientation The orientation clockwise from north in radians.
     * @param semiMajorM The length of the semi-major axis in meters.
     * @param semiMinorM The length of the semi-minor axis in meters.
     * @return the ellipsoid.
     */
    public static Ellipsoid createEllipsoid(Projection projection, GeographicPosition center, double orientation,
            double semiMajorM, double semiMinorM)
    {
        double lat = center.getLatLonAlt().getLatD();
        double lon = center.getLatLonAlt().getLonD();

        // Get the vector which is perpendicular to the model's surface.
        LatLonAlt onSurface = LatLonAlt.createFromDegrees(lat, lon, ReferenceLevel.ELLIPSOID);
        final double anyAlt = 100.;
        LatLonAlt elevated = LatLonAlt.createFromDegreesMeters(lat, lon, anyAlt, ReferenceLevel.ELLIPSOID);

        Vector3d onSurfaceModel = projection.convertToModel(new GeographicPosition(onSurface), Vector3d.ORIGIN);
        Vector3d elevatedModel = projection.convertToModel(new GeographicPosition(elevated), Vector3d.ORIGIN);

        Vector3d zAxis = elevatedModel.subtract(onSurfaceModel).getNormalized();
        Vector3d yAxis;
        // If this point is at one of the poles, then the orientation is
        // arbitrary.
        if (MathUtil.isZero(onSurfaceModel.getX()) && MathUtil.isZero(onSurfaceModel.getY()))
        {
            yAxis = new Vector3d(1., 0., 0.);
        }
        else
        {
            yAxis = new Vector3d(0., 0., WGS84EarthConstants.RADIUS_POLAR_M).subtract(onSurfaceModel).getNormalized();
        }
        Vector3d xAxis = yAxis.cross(zAxis).getNormalized();
        // square up the y axis
        yAxis = zAxis.cross(xAxis).getNormalized();

        // re-align the axes so that they are rotated clockwise from north by
        // the given orientation angle.
        double oriAngle = -orientation;
        yAxis = yAxis.rotate(zAxis, oriAngle).multiply(semiMajorM);
        xAxis = xAxis.rotate(zAxis, oriAngle).multiply(semiMinorM);

        return new DefaultEllipsoid(onSurfaceModel, xAxis, yAxis, zAxis);
    }

    /** Disallow instantiation. */
    private EllipseGeometryUtilities()
    {
    }
}
