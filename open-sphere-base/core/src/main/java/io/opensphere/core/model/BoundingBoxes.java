package io.opensphere.core.model;

import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.util.collections.CollectionUtilities;

/**
 * Utilities for bounding boxes.
 */
public final class BoundingBoxes
{
    /**
     * Determines whether a BoundingBox contains another BoundingBox. Note: Is
     * always false if either or both aBB or bBB is null.
     *
     * @param <T> the generic that extends {@link Position}
     * @param aBB the first {@link BoundingBox}
     * @param bBB the second {@link BoundingBox} to see if it is contained in
     *            the first
     * @return <code>true</code> if the bounding boxes intersect, otherwise
     *         <code>false</code>.
     */
    public static <T extends Position> boolean contains(BoundingBox<T> aBB, BoundingBox<T> bBB)
    {
        if (aBB == null || bBB == null)
        {
            return false;
        }

        Polygon polyA = toJTSPolygon(aBB);
        Polygon polyB = toJTSPolygon(bBB);
        return polyA.contains(polyB);
    }

    /**
     * Get the smallest bounding box which contains all of the positions.
     *
     * @param <T> The position type.
     * @param positions The positions which must be contained in the box.
     * @return The smallest bounding box which contains all of the positions.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Position> BoundingBox<T> getMinimumBoundingBox(Collection<? extends T> positions)
    {
        if (positions.isEmpty())
        {
            return null;
        }
        T item = CollectionUtilities.getItem(positions, 0);
        if (item instanceof GeographicPosition)
        {
            return (BoundingBox<T>)GeographicBoundingBox
                    .getMinimumBoundingBox((Collection<? extends GeographicPosition>)positions);
        }
        else if (item instanceof ScreenPosition)
        {
            return (BoundingBox<T>)ScreenBoundingBox.getMinimumBoundingBox((Collection<? extends ScreenPosition>)positions);
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a {@link Geometry} that represents the envelope (bounding
     * rectangle) of the intersection of two bounding boxes. If there is no
     * intersection it will return null.
     *
     * If one of the two bounding boxes is null it will return the envelope of
     * the non-null box. If both boxes are null it will return null.
     *
     * See {@link Geometry}.intersection to see how the result is delivered.
     *
     * @param <T> the generic type that extends {@link Position}
     * @param aBB the {@link BoundingBox} to intersect with second bounding box.
     * @param bBB the {@link BoundingBox} to intersect with the first bounding
     *            box.
     * @return the envelope (rectangle) of the intersection of the two bounding
     *         boxes, or null if no intersection.
     */
    public static <T extends Position> Geometry intersectionEnvelope(BoundingBox<T> aBB, BoundingBox<T> bBB)
    {
        Geometry result;
        Polygon aPoly = aBB == null ? null : BoundingBoxes.toJTSPolygon(aBB);
        Polygon bPoly = bBB == null ? null : BoundingBoxes.toJTSPolygon(bBB);
        if (aPoly == null)
        {
            result = bPoly == null ? null : bPoly.getEnvelope();
        }
        else if (bPoly == null)
        {
            result = aPoly.getEnvelope();
        }
        else
        {
            if (aPoly.intersects(bPoly))
            {
                Geometry gc = aPoly.intersection(bPoly);
                if (gc != null && gc.getCoordinates().length > 0)
                {
                    if (gc.getCoordinates().length == 1)
                    {
                        // Handle the case where there is only a single point in
                        // common
                        // by building a polygon on that point.
                        GeometryFactory gf = new GeometryFactory();
                        Coordinate[] cds = new Coordinate[5];
                        for (int i = 0; i < cds.length; i++)
                        {
                            cds[i] = gc.getCoordinates()[0];
                        }
                        result = gf.createPolygon(gf.createLinearRing(cds), null);
                    }
                    else if (gc.getCoordinates().length == 2)
                    {
                        // Handle the case where there is a shared edge and we
                        // get
                        // back a line string with two points that define the
                        // intersection.
                        // Construct a polygon using the two points we have.
                        GeometryFactory gf = new GeometryFactory();
                        Coordinate[] cds = new Coordinate[5];
                        cds[0] = gc.getCoordinates()[0];
                        cds[1] = gc.getCoordinates()[1];
                        cds[2] = gc.getCoordinates()[1];
                        cds[3] = gc.getCoordinates()[0];
                        cds[4] = gc.getCoordinates()[0];
                        result = gf.createPolygon(gf.createLinearRing(cds), null);
                    }
                    else if (gc.getCoordinates().length == 5)
                    {
                        result = gc.getEnvelope();
                    }
                    else
                    {
                        result = null;
                    }
                }
                else
                {
                    result = null;
                }
            }
            else
            {
                result = null;
            }
        }
        return result;
    }

    /**
     * Determines whether a BoundingBox <i>intersects</i> another BoundingBox.
     * <p>
     * Intersection is defined like
     * {@link com.vividsolutions.jts.geom.Geometry#intersects(Geometry)}, and as
     * such, includes the case where two geometries touch but do not overlap.
     * <p>
     * Intersection is always false if either or both aBB or bBB is null.
     *
     * @param <T> the generic that extends {@link Position}
     * @param aBB the first {@link BoundingBox}
     * @param bBB the second {@link BoundingBox}
     * @return <code>true</code> if the bounding boxes intersect, otherwise
     *         <code>false</code>.
     */
    public static <T extends Position> boolean intersects(BoundingBox<T> aBB, BoundingBox<T> bBB)
    {
        if (aBB == null || bBB == null || !isFinite(aBB) || !isFinite(bBB))
        {
            return false;
        }

        Polygon polyA = toJTSPolygon(aBB);
        Polygon polyB = toJTSPolygon(bBB);
        return polyA.intersects(polyB);
    }

    /**
     * Converts the two dimension x and y coordinates of a BoundingBox of
     * Position to a JTS {@link Polygon}.
     *
     * @param <T> the generic type that extends {@link Position}
     * @param bb the {@link BoundingBox}
     * @return the JTS {@link Polygon} that represents the bounding box.
     */
    public static <T extends Position> Polygon toJTSPolygon(BoundingBox<T> bb)
    {
        GeometryFactory geomFactory = new GeometryFactory();
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(bb.getLowerLeft().asVector3d().getX(), bb.getLowerLeft().asVector3d().getY(),
                bb.getLowerLeft().asVector3d().getZ());
        coords[1] = new Coordinate(bb.getLowerRight().asVector3d().getX(), bb.getLowerRight().asVector3d().getY(),
                bb.getLowerRight().asVector3d().getZ());
        coords[2] = new Coordinate(bb.getUpperRight().asVector3d().getX(), bb.getUpperRight().asVector3d().getY(),
                bb.getUpperRight().asVector3d().getZ());
        coords[3] = new Coordinate(bb.getUpperLeft().asVector3d().getX(), bb.getUpperLeft().asVector3d().getY(),
                bb.getUpperLeft().asVector3d().getZ());
        coords[4] = coords[0];
        return geomFactory.createPolygon(geomFactory.createLinearRing(coords), null);
    }

    /**
     * Returns the envelope (bounding rectangle) for the union of two bounding
     * boxes. If both boxes are null, returns null. If one of the bounding boxes
     * are null returns the envelope of the non-null box.
     *
     * @param <T> the generic type
     * @param aBB the first of the two bounding boxes to union
     * @param bBB the second of the two bounding boxes to union.
     * @return the geometry the resultant envelope (bounding rectangle) of the
     *         union or null.
     */
    public static <T extends Position> Geometry unionEnvelope(BoundingBox<T> aBB, BoundingBox<T> bBB)
    {
        Geometry result;
        Polygon aPoly = aBB == null ? null : BoundingBoxes.toJTSPolygon(aBB);
        Polygon bPoly = bBB == null ? null : BoundingBoxes.toJTSPolygon(bBB);
        if (aPoly == null)
        {
            result = bPoly == null ? null : bPoly.getEnvelope();
        }
        else if (bPoly == null)
        {
            result = aPoly.getEnvelope();
        }
        else
        {
            Geometry gc = aPoly.union(bPoly);
            if (gc != null && gc.getCoordinates().length > 0)
            {
                result = gc.getEnvelope();
            }
            else
            {
                result = null;
            }
        }
        return result;
    }

    /**
     * Returns whether or not all points of the box are finite.
     *
     * @param bbox the bounding box
     * @return whether or not all points of the box are finite
     */
    private static <T extends Position> boolean isFinite(BoundingBox<T> bbox)
    {
        return bbox.getLowerLeft().asVector2d().isFinite() && bbox.getUpperRight().asVector2d().isFinite();
    }

    /** Disallow instantiation. */
    private BoundingBoxes()
    {
    }
}
