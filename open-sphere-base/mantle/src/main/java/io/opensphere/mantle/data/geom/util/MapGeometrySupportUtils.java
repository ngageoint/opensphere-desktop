package io.opensphere.mantle.data.geom.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.opensphere.core.geometry.EllipseGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.MapCircleGeometrySupport;
import io.opensphere.mantle.data.geom.MapEllipseGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;

/**
 * Utility methods to support map geometries.
 */
public final class MapGeometrySupportUtils
{
    /**
     * Generate a circle around the center of the {@link MapGeometrySupport}
     * with the radius measured by the arc length.
     *
     * @param mgs the {@link MapGeometrySupport} whose center is the center of
     *            the circle.
     * @param arcRadiusMeters the arc length radius in meters.
     * @param vertexCount The number of unique vertices.
     * @param projection The projection to use.
     * @return the points for point radius circle from
     *         {@link MapGeometrySupport} center
     */
    public static List<LatLonAlt> generateArcLengthCircle(MapGeometrySupport mgs, double arcRadiusMeters, int vertexCount,
            Projection projection)
    {
        Utilities.checkNull(mgs, "mgs");
        List<LatLonAlt> llaList = new ArrayList<>(vertexCount);
        GeographicBoundingBox gbb = mgs.getBoundingBox(projection);
        if (gbb != null)
        {
            LatLonAlt center = gbb.getCenter().getLatLonAlt();
            double angleDegrees = 0.0;
            for (int i = 0; i < vertexCount; i++)
            {
                LatLonAlt circlePoint = GeographicBody3D.greatCircleEndPosition(center, Math.toRadians(angleDegrees),
                        WGS84EarthConstants.RADIUS_MEAN_M, arcRadiusMeters);
                llaList.add(circlePoint);
                angleDegrees += 360.0 / vertexCount;
            }
        }
        return llaList;
    }

    /**
     * Gets the bounding box for a {@link MapCircleGeometrySupport}.
     *
     * @param megs the MapCircleGeometrySupport
     * @param projection The map projection to use
     * @return the {@link GeographicBoundingBox}
     */
    public static GeographicBoundingBox getBoundingBox(MapCircleGeometrySupport megs, Projection projection)
    {
        Utilities.checkNull(megs, "megs");
        EllipseGeometry.ProjectedBuilder ellipseBuilder = new EllipseGeometry.ProjectedBuilder();
        ellipseBuilder.setProjection(projection);
        ellipseBuilder.setCenter(new GeographicPosition(LatLonAlt.createFromDegreesMeters(megs.getLocation().getLatD(),
                megs.getLocation().getLonD(), megs.getLocation().getAltM(), megs.getLocation().getAltitudeReference())));
        ellipseBuilder.setSemiMajorAxis(megs.getRadius());
        ellipseBuilder.setSemiMinorAxis(megs.getRadius());
        EllipseGeometry eg = new EllipseGeometry(ellipseBuilder, new DefaultPolygonRenderProperties(0, true, false), null);
        return findBounds(eg.getVertices());
    }

    /**
     * Gets the bounding box for a {@link MapEllipseGeometrySupport}.
     *
     * @param megs the MapEllipseGeometrySupport
     * @param projection The map projection.
     * @return the {@link GeographicBoundingBox}
     */
    public static GeographicBoundingBox getBoundingBox(MapEllipseGeometrySupport megs, Projection projection)
    {
        Utilities.checkNull(megs, "megs");
        EllipseGeometry.ProjectedBuilder ellipseBuilder = new EllipseGeometry.ProjectedBuilder();
        ellipseBuilder.setProjection(projection);
        ellipseBuilder.setCenter(new GeographicPosition(LatLonAlt.createFromDegreesMeters(megs.getLocation().getLatD(),
                megs.getLocation().getLonD(), megs.getLocation().getAltM(), megs.getLocation().getAltitudeReference())));
        ellipseBuilder.setAngle(90. - megs.getOrientation());
        ellipseBuilder.setSemiMajorAxis(megs.getSemiMajorAxis());
        ellipseBuilder.setSemiMinorAxis(megs.getSemiMinorAxis());
        EllipseGeometry eg = new EllipseGeometry(ellipseBuilder, new DefaultPolygonRenderProperties(0, true, false), null);
        return findBounds(eg.getVertices());
    }

    /**
     * Gets the bounding box for the {@link MapLineOfBearingGeometrySupport}.
     *
     * @param lob the {@link MapLineOfBearingGeometrySupport}.
     * @return the bounding box
     */
    public static GeographicBoundingBox getBoundingBox(MapLineOfBearingGeometrySupport lob)
    {
        Utilities.checkNull(lob, "lob");
        LatLonAlt endPosition = getLineOfBearingEndPoint(lob);
        double minLat = endPosition.getLatD() < lob.getLocation().getLatD() ? endPosition.getLatD() : lob.getLocation().getLatD();
        double maxLat = endPosition.getLatD() > lob.getLocation().getLatD() ? endPosition.getLatD() : lob.getLocation().getLatD();
        double minLon = endPosition.getLonD() < lob.getLocation().getLonD() ? endPosition.getLonD() : lob.getLocation().getLonD();
        double maxLon = endPosition.getLonD() < lob.getLocation().getLonD() ? endPosition.getLonD() : lob.getLocation().getLonD();
        LatLonAlt ll = LatLonAlt.createFromDegrees(minLat, minLon);
        LatLonAlt ur = LatLonAlt.createFromDegrees(maxLat, maxLon);
        GeographicBoundingBox bb = new GeographicBoundingBox(ll, ur);
        return bb;
    }

    /**
     * Gets the bounding box for the path geometry support.
     *
     * @param pathGs the path {@link MapPathGeometrySupport}
     * @return the bounding box
     */
    public static GeographicBoundingBox getBoundingBox(MapPathGeometrySupport pathGs)
    {
        Utilities.checkNull(pathGs, "pathGs");
        List<LatLonAlt> llaList = pathGs.getLocations();
        List<GeographicPosition> lgp = new ArrayList<>(llaList.size());
        for (LatLonAlt lla : llaList)
        {
            lgp.add(new GeographicPosition(lla));
        }
        return findBounds(lgp);
    }

    /**
     * Gets the line of bearing (lob) end point.
     *
     * @param lob the {@link MapLineOfBearingGeometrySupport}
     * @return the lob end point {@link LatLonAlt}
     */
    public static LatLonAlt getLineOfBearingEndPoint(MapLineOfBearingGeometrySupport lob)
    {
        Utilities.checkNull(lob, "lob");
        return GeographicBody3D.greatCircleEndPosition(lob.getLocation(), Math.toRadians(lob.getOrientation()),
                WGS84EarthConstants.RADIUS_MEAN_M, lob.getLength());
    }

    /**
     * Get merged child bounds. Returns null if no children.
     *
     * @param mgs the mgs
     * @param projection The map projection.
     * @return the merged child bounds
     */
    public static GeographicBoundingBox getMergedChildBounds(MapGeometrySupport mgs, Projection projection)
    {
        return getMergedChildBounds(mgs, projection, null);
    }

    /**
     * Get merged child bounds. Returns null if no children.
     *
     * @param mgs the mgs
     * @param projection The map projection.
     * @param refLevel The reference level to use for the altitude.
     * @return the merged child bounds
     */
    public static GeographicBoundingBox getMergedChildBounds(MapGeometrySupport mgs, Projection projection,
            Altitude.ReferenceLevel refLevel)
    {
        GeographicBoundingBox gbb = null;
        if (mgs.hasChildren())
        {
            GeographicBoundingBox gbbChild = null;
            for (MapGeometrySupport child : mgs.getChildren())
            {
                gbbChild = child.getBoundingBox(projection);
                if (gbb == null)
                {
                    gbb = gbbChild;
                }
                else
                {
                    gbb = refLevel != null ? GeographicBoundingBox.merge(gbb, gbbChild, refLevel)
                            : GeographicBoundingBox.merge(gbb, gbbChild);
                }
            }
        }
        return gbb;
    }

    /**
     * Find bounds.
     *
     * @param vertices the vertices
     * @return the geographic bounding box
     */
    private static GeographicBoundingBox findBounds(List<? extends Position> vertices)
    {
        if (vertices == null || vertices.isEmpty())
        {
            return null;
        }

        // Find the min/max xy ( lat/lon )
        Vector3d vec = vertices.get(0).asVector3d();
        double minY = vec.getY();
        double maxY = vec.getY();
        double minX = vec.getX();
        double maxX = vec.getX();
        if (vertices.size() > 1)
        {
            for (int i = 1; i < vertices.size(); i++)
            {
                vec = vertices.get(i).asVector3d();
                minX = Math.min(minX, vec.getX());
                maxX = Math.max(maxX, vec.getX());
                minY = Math.min(minY, vec.getY());
                maxY = Math.max(maxY, vec.getY());
            }
        }

        // Determine the reference level. Use the reference level of the
        // vertices if they are all the same, otherwise use terrain.
        Set<ReferenceLevel> referenceLevels = New.set();
        for (Position vertex : vertices)
        {
            if (vertex instanceof GeographicPosition)
            {
                referenceLevels.add(((GeographicPosition)vertex).getAlt().getReferenceLevel());
            }
        }
        ReferenceLevel referenceLevel = referenceLevels.size() == 1 ? referenceLevels.iterator().next()
                : Altitude.ReferenceLevel.TERRAIN;

        LatLonAlt llaLR = LatLonAlt.createFromDegreesMeters(minY, minX, 0., referenceLevel);
        LatLonAlt llaUR = LatLonAlt.createFromDegreesMeters(maxY, maxX, 0., referenceLevel);
        return new GeographicBoundingBox(llaLR, llaUR);
    }

    /** Disallow instantiation. */
    private MapGeometrySupportUtils()
    {
    }
}
