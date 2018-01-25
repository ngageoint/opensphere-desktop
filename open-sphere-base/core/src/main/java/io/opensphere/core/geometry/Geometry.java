package io.opensphere.core.geometry;

import java.util.List;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.lang.ActuallyCloneable;

/**
 * A <tt>Geometry</tt> is a model for something drawn on the screen, either
 * using screen coordinates or geographic coordinates. It may be something
 * primitive like a point or a line, or it can be a complex geometry comprising
 * other subordinate geometries.
 */
public interface Geometry extends ActuallyCloneable
{
    /** The Constant ALL_INTERSECTION_TESTS. */
    JTSIntersectionTests ALL_INTERSECTION_TESTS = new JTSIntersectionTests(true, true, true);

    /**
     * Clone this geometry.
     *
     * @return The clone.
     */
    @Override
    Geometry clone();

    /**
     * Create a builder that may be used to create a duplicate of this geometry.
     *
     * @return A builder instance.
     */
    Builder createBuilder();

    /**
     * Get the identifier for the data model from which this geometry
     * originates, or -1 if there is no backing data model.
     *
     * @return The data model id.
     */
    long getDataModelId();

    /**
     * Get the order key for this geometry.
     *
     * @return The order key for this geometry.
     */
    GeometryOrderKey getGeometryOrderKey();

    /**
     * Get the position type for this geometry.
     *
     * @return The position type of this geometry.
     */
    Class<? extends Position> getPositionType();

    /**
     * Get a reference point for the geometry. This could be the center of the
     * geometry or any other point of importance.
     *
     * @return The reference point.
     */
    Position getReferencePoint();

    /**
     * Get the properties for rendering this geometry.
     *
     * @return The rendering properties.
     */
    ZOrderRenderProperties getRenderProperties();

    /**
     * Indicates if this geometry is likely to change frequently (several times
     * a second).
     *
     * @return The rapid update flag.
     */
    boolean isRapidUpdate();

    /**
     * Tests if this geometry is contained by or intersects or overlaps any of a
     * list of polygons. Return false if intersection cannot be computed or if
     * not drawable. The caller can select which of the three tests will tried
     * and combined via OR into a result.
     *
     * @param test the tests to be performed and ORd together. ( if null
     *            performs all tests ).
     * @param polygons the polygons
     * @param geomFactory the geom factory for use to create JTS geometry
     *            elements for comparison.
     *
     * @return true if intersects, false if not or unknown.
     */
    boolean jtsIntersectionTests(JTSIntersectionTests test, List<Polygon> polygons, GeometryFactory geomFactory);

    /**
     * Determine if this geometry overlaps a bounding box.
     *
     * @param <T> The type of the bounding box.
     * @param boundingBox The bounding box.
     * @param tolerance How far this geometry can be from the bounding box and
     *            still be considered overlapping. The units of the tolerance
     *            depend on the type of the bounding box.
     * @return {@code true} if the geometry overlaps the bounding box.
     */
    <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance);

    /** Interface for classes that build geometry instances. */
    public interface Builder
    {
    }

    /**
     * Abstract base class for keys that may be used to determine render order.
     */
    abstract class GeometryOrderKey implements Comparable<GeometryOrderKey>
    {
    }

    /**
     * The Class IntersectionTests.
     */
    class JTSIntersectionTests
    {
        /** The Contains. */
        private final boolean myContains;

        /** The Intersects. */
        private final boolean myIntersects;

        /** The Overlaps. */
        private final boolean myOverlaps;

        /**
         * Instantiates a new intersection tests.
         *
         * @param contains the contains
         * @param overlaps the overlaps
         * @param intersects the intersects
         */
        public JTSIntersectionTests(boolean contains, boolean overlaps, boolean intersects)
        {
            myContains = contains;
            myOverlaps = overlaps;
            myIntersects = intersects;
        }

        /**
         * Checks if is contains.
         *
         * @return true, if is contains
         */
        public final boolean isContains()
        {
            return myContains;
        }

        /**
         * Checks if is intersects.
         *
         * @return true, if is intersects
         */
        public final boolean isIntersects()
        {
            return myIntersects;
        }

        /**
         * Checks if is overlaps.
         *
         * @return true, if is overlaps
         */
        public final boolean isOverlaps()
        {
            return myOverlaps;
        }
    }
}
