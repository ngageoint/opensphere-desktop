package io.opensphere.core.projection;

import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.SimpleTesseraVertex;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicProjectedTesseraVertex;
import io.opensphere.core.terrain.util.ElevationChangeListener;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.terrain.util.TerrainElevationProvider;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.Viewer;

/**
 * A projection can convert positions to model coordinates (x/y/z) and back.
 */
public interface Projection extends TerrainElevationProvider, ElevationChangeListener
{
    /**
     * The name of the model center parameter used when displaying argument
     * errors.
     */
    String MODEL_CENTER_PARAMETER_NAME = "modelcenter";

    /**
     * Convert a set of lines to a list of vertices.
     *
     * @param positions the positions
     * @param limit the number of positions to use.
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return vertices in model coordinates.
     */
    List<Vector3d> convertLinesToModel(List<? extends GeographicPosition> positions, int limit, LineType type,
            Vector3d modelCenter);

    /**
     * Convert a line to a {@link Tessera}.
     *
     * @param start The start of the line.
     * @param end The end of the line.
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return A Tessera and a Projection cursor.
     */
    Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(GeographicPosition start, GeographicPosition end,
            LineType type, Vector3d modelCenter);

    /**
     * Convert a line to a {@link Tessera}.
     *
     * @param start The start of the line.
     * @param end The end of the line.
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return A Tessera and a Projection cursor.
     */
    Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(ProjectionCursor start, GeographicPosition end,
            LineType type, Vector3d modelCenter);

    /**
     * Create the tesserae which cover the given polygon.
     *
     * @param polygon The polygon for which the coverage is desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The covering tesserae.
     */
    TesseraList<? extends GeographicProjectedTesseraVertex> convertPolygonToModelMesh(Polygon polygon, Vector3d modelCenter);

    /**
     * Given positions, convert them to model coordinates. Position is
     * preserved.
     *
     * @param positions Positions in projected coordinates.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return Vertices in model coordinates.
     */
    Collection<Vector3d> convertPositionsToModel(Collection<? extends GeographicPosition> positions, Vector3d modelCenter);

    /**
     * Convert a quad to a {@link TesseraList}.
     *
     * @param lowerLeft A corner of the quad.
     * @param lowerRight A corner of the quad.
     * @param upperRight A corner of the quad.
     * @param upperLeft A corner of the quad.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The TesseraList.
     */
    TesseraList<? extends GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition lowerLeft,
            GeographicPosition lowerRight, GeographicPosition upperRight, GeographicPosition upperLeft, Vector3d modelCenter);

    /**
     * Convert to model coordinates.
     *
     * @param inPos The position in projected coordinates.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The model coordinates.
     */
    Vector3d convertToModel(GeographicPosition inPos, Vector3d modelCenter);

    /**
     * Convert from model coordinates.
     *
     * @param inPos The position in model coordinates.
     * @param altReference The desired altitude reference for the returned
     *            position.
     * @return The unprojected coordinates.
     */
    GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference);

    /**
     * Convert a triangle to a {@link TesseraList}.
     *
     * @param vert1 A corner of the triangle.
     * @param vert2 A corner of the triangle.
     * @param vert3 A corner of the triangle.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The TesseraList.
     */
    TesseraList<? extends GeographicProjectedTesseraVertex> convertTriangleToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, Vector3d modelCenter);

    /**
     * Get the time at which the projection was last used as the current
     * projection.
     *
     * @return the time at which the projection was last used as the current
     *         projection.
     */
    long getActivationTimestamp();

    /**
     * Get a {@code DefaultEllipsoid} in model coordinates that encompasses a
     * bounding box. This is useful for quick visibility calculations.
     *
     * @param bbox The bounding box.
     * @param modelCenter the model center used to offset the ellipsoid
     *            position.
     * @param forceGenerate when true, do not attempt to retrieve from the
     *            cache.
     * @return The ellipsoid.
     */
    Ellipsoid getBoundingEllipsoid(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter, boolean forceGenerate);

    /**
     * Get a {@code Sphere} in model coordinates that encompasses a bounding
     * box. This is useful for quick visibility calculations.
     *
     * @param bbox The bounding box.
     * @param modelCenter the model center used to offset the sphere position.
     * @param forceGenerate when true, do not attempt to retrieve from the
     *            cache.
     * @return The sphere.
     */
    Sphere getBoundingSphere(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter, boolean forceGenerate);

    /**
     * Get the time at which the projection was created.
     *
     * @return the time at which the projection was created.
     */
    long getCreationTimestamp();

    /**
     * Get the manager for elevation providers.
     *
     * @return the elevation manager.
     */
    ElevationManager getElevationManager();

    /**
     * Get the approximate minimum distance from the view plane to the terrain.
     * This value must not be more than the actual distance.
     *
     * @param view The viewer from which the distance is desired.
     * @return The approximate minimum distance from the view plane to the
     *         terrain.
     */
    double getMinimumTerrainDistance(Viewer view);

    /**
     * Get the center for eye coordinates which should be used with this
     * projection.
     *
     * @return the modelCenter
     */
    Vector3d getModelCenter();

    /**
     * Get the height of the projection in model coordinates.
     *
     * @return The height.
     */
    double getModelHeight();

    /**
     * Get the adjustment to world eye coordinates to yield eye coordinates
     * based on the model center.
     *
     * @return the modelViewAdjustment
     */
    Matrix4d getModelViewAdjustment();

    /**
     * Get the width of the projection in model coordinates.
     *
     * @return The width.
     */
    double getModelWidth();

    /**
     * Get the name of this projection.
     *
     * @return The name.
     */
    String getName();

    /**
     * Get a unit vector which is normal to the surface of the model at the
     * location. If the {@link io.opensphere.core.model.Altitude.ReferenceLevel}
     * of the position is
     * {@link io.opensphere.core.model.Altitude.ReferenceLevel#TERRAIN} and the
     * projection supports terrain, the vector returned will be normal to the
     * terrain mesh.
     *
     * @param inPos The position at which to get the normal.
     * @return The unit normal vector.
     */
    Vector3d getNormalAtPosition(GeographicPosition inPos);

    /**
     * Get the current snapshot of the projection.
     *
     * @return The projection snapshot.
     */
    Projection getSnapshot();

    /**
     * Get the intersection with the surface on a line connecting two points.
     *
     * @param pointA The first point in model coordinates.
     * @param pointB The second point in model coordinates.
     * @return The intersection point in model coordinates or <code>null</code>
     *         if no intersection was found.
     */
    Vector3d getSurfaceIntersection(Vector3d pointA, Vector3d pointB);

    /**
     * Get the intersection of ray with the nearest terrain point.
     *
     * @param ray Ray to intersect with the terrain.
     * @param view When this is not <code>null</code>, the intersection will be
     *            optimized by only checking terrain elements which are in view.
     * @return Terrain intersection or null if non-intersecting.
     */
    Vector3d getTerrainIntersection(Ray3d ray, Viewer view);

    /**
     * For projections which have mutable models, adjust the density to match
     * the new setting.
     *
     * @param density The new density setting.
     * @return The region which was modified as a result of the new setting. If
     *         there is no change or if the the changes are deferred to a
     *         backing model, null may be returned.
     */
    Collection<GeographicBoundingBox> handleModelDensityChanged(int density);

    /**
     * Reset the projection and make any necessary adjustments required for the
     * new accuracy setting.
     *
     * @param highAccuracy The new high accuracy setting.
     */
    void resetProjection(boolean highAccuracy);

    /** Set the activation timestamp to the current time. */
    void setActivationTimestamp();

    /**
     * A tessera vertex that provides model coordinates.
     *
     * @param <T> The type of position.
     */
    public interface ProjectedTesseraVertex<T extends Position> extends Tessera.TesseraVertex<T>
    {
        /**
         * Get the model coordinates of the vertex.
         *
         * @return The model coordinates.
         */
        Vector3d getModelCoordinates();
    }

    /**
     * A Cursor for use in successive projection conversions which can be used
     * to as a place holder for where the previous conversion completed. At a
     * minimum, this should provided the current vertex.
     */
    @FunctionalInterface
    public interface ProjectionCursor
    {
        /**
         * Get the vertex.
         *
         * @return the vertex
         */
        GeographicProjectedTesseraVertex getVertex();
    }

    /**
     * A simple implementation of {code Tessera.TesseraVertex} that comprises a
     * set of geographic coordinates and a set of model coordinates.
     *
     * @param <T> The type of position.
     */
    class SimpleProjectedTesseraVertex<T extends Position> extends SimpleTesseraVertex<T> implements ProjectedTesseraVertex<T>
    {
        /** The model coordinates. */
        private Vector3d myModelCoordinates;

        /**
         * Construct a vertex.
         *
         * @param coord The geographic coordinates.
         * @param model The model coordinates.
         */
        public SimpleProjectedTesseraVertex(T coord, Vector3d model)
        {
            super(coord);
            myModelCoordinates = model;
        }

        @Override
        public SimpleTesseraVertex<T> adjustToModelCenter(Vector3d modelCenter)
        {
            return new SimpleProjectedTesseraVertex<T>(getCoordinates(), myModelCoordinates.subtract(modelCenter));
        }

        @Override
        public Vector3d getModelCoordinates()
        {
            return myModelCoordinates;
        }

        /**
         * Set the modelCoordinates.
         *
         * @param modelCoordinates the modelCoordinates to set
         */
        public void setModelCoordinates(Vector3d modelCoordinates)
        {
            myModelCoordinates = modelCoordinates;
        }

        @Override
        public String toString()
        {
            return super.toString() + " " + myModelCoordinates;
        }
    }
}
