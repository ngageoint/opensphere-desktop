package io.opensphere.core.terrain;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.DefaultSphere;
import io.opensphere.core.math.Line2d;
import io.opensphere.core.math.LineSegment2d;
import io.opensphere.core.math.Plane;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicBoundingCircle;
import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.terrain.util.AbsoluteElevationProvider;
import io.opensphere.core.terrain.util.ElevationManager;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/**
 * A Triangle which is on the surface of a globe whose corners may be elevation
 * adjusted.
 */
@SuppressWarnings("PMD.GodClass")
public class TerrainTriangle implements Cloneable, Tessera<GeographicPosition>
{
    /** Latitude beyond which a point is considered to be at the pole. */
    private static final double AT_NORTH_POLE = 89.9;

    /** Latitude beyond which a point is considered to be at the pole. */
    private static final double AT_SOUTH_POLE = -89.9;

    /** Lager reference. */
    private static final Logger LOGGER = Logger.getLogger(TerrainTriangle.class);

    /**
     * Adjacent to A means that the shared side of the triangles is opposite
     * myVertexA.
     */
    private TerrainTriangle myAdjacentA;

    /**
     * Adjacent to B means that the shared side of the triangles is opposite
     * myVertexB.
     */
    private TerrainTriangle myAdjacentB;

    /**
     * Adjacent to C means that the shared side of the triangles is opposite
     * myVertexC.
     */
    private TerrainTriangle myAdjacentC;

    /**
     * Provider of elevation for this triangle. This may be null when there is
     * no provider or when the triangle overlaps multiple providers.
     */
    private AbsoluteElevationProvider myElevationProvider;

    /** The geographic polygon defined by my vertices. */
    private final GeographicConvexPolygon myGeographicPolygon;

    /** the globe. */
    private TriangleGlobeModel myGlobe;

    /** My current index, "-1" indicates that I am not currently indexed. */
    private int myIndex = -1;

    /** The JTS polygon which is this triangle. */
    private Polygon myJTSPolygon;

    /** left and right in this context assume that myVertexC is pointing up. */
    private TerrainTriangle myLeftChild;

    /** Sphere which bounds me. */
    private Sphere myModelBoundingSphere;

    /** The triangle above me in the tree. */
    private TerrainTriangle myParent;

    /** When true, this triangle can no longer be modified. */
    private boolean myPetrified;

    /** The plane defined by the three vertices of the triangle. */
    private Plane myPlane;

    /** left and right in this context assume that myVertexC is pointing up. */
    private TerrainTriangle myRightChild;

    /**
     * Container for data that is only used when splitting and merging. This is
     * separated out from the rest of the class to reduce the size of petrified
     * triangles.
     */
    private TerrainTriangleSplitMergeHelper mySplitMergeHelper;

    /**
     * The angles should be defined so that A -&gt; B -&gt; C gives a counter
     * clockwise rotation and A and B are the same length.
     */
    private TerrainVertex myVertexA;

    /**
     * The angles should be defined so that A -&gt; B -&gt; C gives a counter
     * clockwise rotation and A and B are the same length.
     */
    private TerrainVertex myVertexB;

    /**
     * The angles should be defined so that A -&gt; B -&gt; C gives a counter
     * clockwise rotation and A and B are the same length.
     */
    private TerrainVertex myVertexC;

    /**
     * Constructor.
     *
     * @param parent parent
     * @param locA the location of the a vertex
     * @param locB the location of the b vertex
     * @param locC the location of the c vertex
     */
    public TerrainTriangle(TerrainTriangle parent, GeographicPosition locA, GeographicPosition locB, GeographicPosition locC)
    {
        this(parent, locA, locB, locC, null, null, null);
        resetTerrain();
    }

    /**
     * Constructor.
     *
     * @param parent parent
     * @param locA the location of the a vertex
     * @param locB the location of the b vertex
     * @param locC the location of the c vertex
     * @param pointA the model position of the a vertex
     * @param pointB the model position of the b vertex
     * @param pointC the model position of the c vertex
     */
    public TerrainTriangle(TerrainTriangle parent, GeographicPosition locA, GeographicPosition locB, GeographicPosition locC,
            Vector3d pointA, Vector3d pointB, Vector3d pointC)
    {
        this(parent, new TerrainVertex(locA, pointA), new TerrainVertex(locB, pointB), new TerrainVertex(locC, pointC));
    }

    /**
     * Constructor.
     *
     * @param parent parent
     * @param vertA terrain vertex a
     * @param vertB terrain vertex b
     * @param vertC terrain vertex c
     */
    public TerrainTriangle(TerrainTriangle parent, TerrainVertex vertA, TerrainVertex vertB, TerrainVertex vertC)
    {
        myVertexA = vertA;
        myVertexB = vertB;
        myVertexC = vertC;
        List<? extends GeographicPosition> vertices = Arrays.asList(myVertexA.getCoordinates(), myVertexB.getCoordinates(),
                myVertexC.getCoordinates());
        myGeographicPolygon = new GeographicConvexPolygon(vertices);

        myParent = parent;
        mySplitMergeHelper = new TerrainTriangleSplitMergeHelper(this);
        myGlobe = myParent == null ? null : myParent.getGlobe();
    }

    /**
     * Check to see whether I should be merged.
     *
     * @param bounds The region over which merging should occur.
     */
    public void checkMerge(GeographicBoundingBox bounds)
    {
        if (myPetrified || myAdjacentC != null && myAdjacentC.isPetrified())
        {
            return;
        }
        mySplitMergeHelper.checkMerge(bounds);
    }

    /**
     * Check to see if this triangle or any children should be merged because of
     * variance and perform the merge if necessary.
     *
     * @param bounds The region over which merging should occur.
     */
    public void checkMergeForVariance(GeographicBoundingBox bounds)
    {
        if (myPetrified || myAdjacentC != null && myAdjacentC.isPetrified())
        {
            return;
        }
        mySplitMergeHelper.checkMergeForVariance(bounds);
    }

    /**
     * Find all of the triangles or children whose provider matches the given
     * one and petrify them.
     *
     * @param provider The provider for which to petrify.
     * @param bounds The region over which petrification should occur.
     */
    public void checkPetrify(AbsoluteElevationProvider provider, GeographicBoundingBox bounds)
    {
        if (myPetrified)
        {
            return;
        }

        if (Utilities.sameInstance(myElevationProvider, provider)
                && (bounds == null || myGeographicPolygon.getBoundingBox().overlaps(bounds, 0.)))
        {
            petrify();
        }
        else
        {
            if (myLeftChild != null)
            {
                myLeftChild.checkPetrify(provider, bounds);
                myRightChild.checkPetrify(provider, bounds);
            }
        }
    }

    /**
     * Check to see whether this triangle and any children should be split and
     * perform the splits as necessary.
     *
     * @param view the current viewer.
     * @param bounds The region over which splitting should occur.
     */
    public void checkSplit(Viewer view, GeographicBoundingBox bounds)
    {
        if (myPetrified || myAdjacentC != null && myAdjacentC.isPetrified())
        {
            return;
        }
        mySplitMergeHelper.checkSplit(view, bounds);
    }

    /** Clear the indices for me and my vertices. */
    public void clearIndices()
    {
        myIndex = -1;
        myVertexA.setIndex(-1);
        myVertexB.setIndex(-1);
        myVertexC.setIndex(-1);
        if (myLeftChild != null)
        {
            myLeftChild.clearIndices();
            myRightChild.clearIndices();
        }
    }

    @Override
    public TerrainTriangle clone()
    {
        try
        {
            return (TerrainTriangle)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }

    /**
     * Determine whether the location is within me.
     *
     * @param loc location
     * @return true if I contain the location
     */
    public boolean contains(GeographicPosition loc)
    {
        return myGeographicPolygon.contains(loc, 0.);
    }

    /**
     * Determine whether the location is within me.
     *
     * @param position location
     * @return true if I contain the location
     */
    public boolean containsModelPosition(Vector3d position)
    {
        Vector3d triA = myVertexA.getModelCoordinates();
        Vector3d triB = myVertexB.getModelCoordinates();
        Vector3d triC = myVertexC.getModelCoordinates();

        /* This calculation is very sensitive to floating point error when test
         * location is close to one of the vertices. When the vector between one
         * vertex and the test location is very short, the accuracy of the cross
         * product degrades quickly. In addition, when the cross product length
         * is close to zero, the dot product with the triangle's normal will
         * also be close to zero since we are not normalizing the vectors. */
        if (position.equals(triA, MathUtil.DBL_LARGE_EPSILON) || position.equals(triB, MathUtil.DBL_LARGE_EPSILON)
                || position.equals(triC, MathUtil.DBL_LARGE_EPSILON))
        {
            return true;
        }

        Vector3d normal = getPlane().getNormal();

        Vector3d ab = triB.subtract(triA);
        Vector3d bLoc = position.subtract(triB);
        Vector3d abLoc = ab.cross(bLoc);
        if (abLoc.dot(normal) < -MathUtil.DBL_EPSILON)
        {
            return false;
        }

        Vector3d ca = triA.subtract(triC);
        Vector3d aLoc = position.subtract(triA);
        Vector3d caLoc = ca.cross(aLoc);
        if (caLoc.dot(normal) < -MathUtil.DBL_EPSILON)
        {
            return false;
        }

        Vector3d bc = triC.subtract(triB);
        Vector3d cLoc = position.subtract(triC);
        Vector3d bcLoc = bc.cross(cLoc);
        return bcLoc.dot(normal) >= -MathUtil.DBL_EPSILON;
    }

    /**
     * Generate the indices for me and my vertices.
     *
     * @param vertices The current list of vertices for all of the terrain.
     * @param triangles The current list of triangles for all of the terrain
     *            which have been indexed.
     */
    public void generateIndices(List<TerrainVertex> vertices, List<TerrainTriangle> triangles)
    {
        if (myIndex == -1)
        {
            myIndex = triangles.size();
            triangles.add(this);
        }

        // When setting the indices for the vertices get the collection size to
        // use as the index first before adding the vertex
        if (myVertexA.getIndex() == -1)
        {
            myVertexA.setIndex(vertices.size());
            vertices.add(myVertexA);
        }
        if (myVertexB.getIndex() == -1)
        {
            myVertexB.setIndex(vertices.size());
            vertices.add(myVertexB);
        }
        if (myVertexC.getIndex() == -1)
        {
            myVertexC.setIndex(vertices.size());
            vertices.add(myVertexC);
        }

        if (myLeftChild != null)
        {
            myLeftChild.generateIndices(vertices, triangles);
            myRightChild.generateIndices(vertices, triangles);
        }
    }

    /**
     * The triangle adjacent to me opposite my vertex a.
     *
     * @return The triangle adjacent to me opposite my vertex a.
     */
    public TerrainTriangle getAdjacentA()
    {
        return myAdjacentA;
    }

    /**
     * The triangle adjacent to me opposite my vertex b.
     *
     * @return The triangle adjacent to me opposite my vertex b.
     */
    public TerrainTriangle getAdjacentB()
    {
        return myAdjacentB;
    }

    /**
     * The triangle adjacent to me opposite my vertex c.
     *
     * @return The triangle adjacent to me opposite my vertex c.
     */
    public TerrainTriangle getAdjacentC()
    {
        return myAdjacentC;
    }

    /**
     * Find the triangle which contains the given point. This is guaranteed to
     * return a triangle. If the given location is not within this triangle the
     * closest leaf triangle will be returned.
     *
     * @param loc Location which must have normalized latitude and longitude.
     * @param allowDegenerate When false, if that containing triangle is
     *            degenerate in model coordinates, a nearby triangle will be
     *            given.
     * @return the leaf triangle which contains the location.
     */
    public TerrainTriangle getContainingTriangle(GeographicPosition loc, boolean allowDegenerate)
    {
        if (!allowDegenerate && myLeftChild == null && isDegenerateOnGlobe())
        {
            if (myAdjacentC != null)
            {
                return myAdjacentC;
            }
            return myAdjacentA;
        }

        if (myLeftChild == null)
        {
            return this;
        }

        TerrainTriangle tri;
        Vector2d locAsVec = loc.getLatLonAlt().asVec2d();
        // It is enough to use the square of the distance since we do not need
        // to know the actual distance, just which is larger.
        double leftDist = myLeftChild.getGeographicPolygon().getBoundingCircle().getCenter().getLatLonAlt().asVec2d()
                .distanceSquared(locAsVec);
        double rightDist = myRightChild.getGeographicPolygon().getBoundingCircle().getCenter().getLatLonAlt().asVec2d()
                .distanceSquared(locAsVec);
        if (leftDist < rightDist)
        {
            tri = myLeftChild.getContainingTriangle(loc, allowDegenerate);
        }
        else
        {
            tri = myRightChild.getContainingTriangle(loc, allowDegenerate);
        }

        return tri;
    }

    /**
     * Get the model position which is based on the celestial body, but adjusted
     * in altitude by the amount given by the elevation provider.
     *
     * @param pos The geographic position whose model position is desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The elevation adjusted model position.
     */
    public final Vector3d getElevationAdjustedModelPosition(GeographicPosition pos, Vector3d modelCenter)
    {
        if (myElevationProvider != null)
        {
            LatLonAlt lla = pos.getLatLonAlt();
            double elevationM = lla.getAltM() + myElevationProvider.getElevationM(pos, true);
            GeographicPosition posToConvert = new GeographicPosition(LatLonAlt.createFromDegreesMeters(lla.getLatD(),
                    lla.getLonD(), elevationM, Altitude.ReferenceLevel.ELLIPSOID));
            return getGlobe().getCelestialBody().convertToModel(posToConvert, modelCenter);
        }
        return getGlobe().getElevationAdjustedModelPosition(pos, modelCenter);
    }

    /**
     * Get the elevationProvider.
     *
     * @return the elevationProvider
     */
    public AbsoluteElevationProvider getElevationProvider()
    {
        return myElevationProvider;
    }

    /**
     * Get all of the points at the lowest level where the line segment
     * intersects this triangle or its children. If necessary, handle the case
     * where the line would intersect triangles by wrapping through +180/-180.
     *
     * @param intersections Collection of points to which to add the
     *            intersections.
     * @param ptA first point of the line of intersection in geographic
     *            coordinates.
     * @param ptB second point of the line of intersection in geographic
     *            coordinates.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     */
    public void getFlatIntersections(List<TerrainVertex> intersections, Vector2d ptA, Vector2d ptB, Vector3d modelCenter)
    {
        if (Math.abs(ptA.getX() - ptB.getX()) > 180)
        {
            if (ptA.getX() < ptB.getX())
            {
                Vector2d adjustedB = new Vector2d(ptB.getX() - 360, ptB.getY());
                getStrictFlatIntersections(intersections, ptA, adjustedB, modelCenter);

                Vector2d adjustedA = new Vector2d(ptA.getX() + 360, ptA.getY());
                getStrictFlatIntersections(intersections, adjustedA, ptB, modelCenter);
            }
            else
            {
                Vector2d adjustedB = new Vector2d(ptB.getX() + 360, ptB.getY());
                getStrictFlatIntersections(intersections, ptA, adjustedB, modelCenter);

                Vector2d adjustedA = new Vector2d(ptA.getX() - 360, ptA.getY());
                getStrictFlatIntersections(intersections, adjustedA, ptB, modelCenter);
            }
        }
        else
        {
            getStrictFlatIntersections(intersections, ptA, ptB, modelCenter);
        }
    }

    /**
     * Given two points which define a line segment, determine where the line
     * segment intersects the given line.
     *
     * @param line The line with which to intersect.
     * @param ptA first point
     * @param ptB second point
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return intersection of the line and the plane. If the line segment is
     *         collinear, return both end points.
     */
    public List<TerrainVertex> getFlatSegmentIntersection(Line2d line, TerrainVertex ptA, TerrainVertex ptB, Vector3d modelCenter)
    {
        List<? extends Vector2d> coords = line.getSegmentIntersection(ptA.getGeographicPositionAsVector(),
                ptB.getGeographicPositionAsVector());
        List<TerrainVertex> intersections = New.list(coords.size());
        Vector2d vecA = ptA.getGeographicPositionAsVector();
        Vector2d vecB = ptB.getGeographicPositionAsVector();
        double segLength = vecA.distance(vecB);
        for (Vector2d latLon : coords)
        {
            double pct = vecA.distance(latLon) / segLength;
            Vector3d interLoc = ptA.getModelCoordinates().interpolate(ptB.getModelCoordinates(), pct);
            Vector3d loc = interLoc.subtract(modelCenter);
            GeographicPosition pos = new GeographicPosition(LatLonAlt.createFromVec2d(latLon));
            TerrainVertex intersectVertex = new TerrainVertex(pos, loc);
            intersections.add(intersectVertex);
        }
        return intersections;
    }

    /**
     * Get the geographicPolygon.
     *
     * @return the geographicPolygon
     */
    public GeographicConvexPolygon getGeographicPolygon()
    {
        return myGeographicPolygon;
    }

    /**
     * Get the index.
     *
     * @return the index
     */
    public int getIndex()
    {
        return myIndex;
    }

    /**
     * Get all of the points at the lowest level where the plane intersects this
     * triangle or its children and where the points are in the bounding box.
     *
     * @param intersections Collection of points to which to add the
     *            intersections.
     * @param plane Plane of intersection in model coordinates.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     */
    public void getIntersections(List<TerrainVertex> intersections, Plane plane, Vector3d modelCenter)
    {
        double distance = plane.getDistance(getModelBoundingSphere().getCenter());
        if (distance > getModelBoundingSphere().getRadius())
        {
            return;
        }

        if (myLeftChild != null)
        {
            myLeftChild.getIntersections(intersections, plane, modelCenter);
            myRightChild.getIntersections(intersections, plane, modelCenter);
        }
        else
        {
            if (isDegenerateOnGlobe())
            {
                return;
            }
            intersections.addAll(getSegmentIntersection(plane, myVertexA, myVertexB, modelCenter));
            intersections.addAll(getSegmentIntersection(plane, myVertexB, myVertexC, modelCenter));
            intersections.addAll(getSegmentIntersection(plane, myVertexC, myVertexA, modelCenter));
        }
    }

    /**
     * Get the jTSPolygon.
     *
     * @return the jTSPolygon
     */
    public Polygon getJTSPolygon()
    {
        if (myJTSPolygon == null)
        {
            myJTSPolygon = JTSUtilities.createJTSPolygon(New.list(myGeographicPolygon.getVertices()), null);
        }
        return myJTSPolygon;
    }

    /**
     * The child on my left side.
     *
     * @return The child on my left side.
     */
    public TerrainTriangle getLeftChild()
    {
        return myLeftChild;
    }

    /**
     * Get the distance from the view plane to my bounding sphere in model
     * coordinates.
     *
     * @param view The viewer from which the distance is desired.
     * @return The distance from the view plane to my bounding sphere in the
     *         model's native units.
     */
    public double getMinDistance(Viewer view)
    {
        // TODO This is broken.
        LOGGER.error("TerrainTriange.getMinDistance() is broken and should not be used until fixed.");
        double minDist = Double.MAX_VALUE;

        if (isInView(view))
        {
            if (myLeftChild != null)
            {
                minDist = Math.min(minDist, myLeftChild.getMinDistance(view));
                if (minDist > 0)
                {
                    minDist = Math.min(minDist, myRightChild.getMinDistance(view));
                }
            }
            else
            {
                if (!isDegenerateOnGlobe())
                {
                    ViewerPosition3D viewPos = (ViewerPosition3D)view.getPosition();
                    Plane viewPlane = new Plane(viewPos.getLocation(), viewPos.getDir());
                    minDist = viewPlane.getDistance(getModelBoundingSphere().getCenter()) - getModelBoundingSphere().getRadius();
                    if (minDist < 0)
                    {
                        return 0;
                    }
                }
            }
        }
        return minDist;
    }

    /**
     * Get the modelBoundingSphere.
     *
     * @return the modelBoundingSphere
     */
    public Sphere getModelBoundingSphere()
    {
        if (myModelBoundingSphere == null)
        {
            setModelBoundingSphere();
        }
        return myModelBoundingSphere;
    }

    /**
     * Get the model coordinates for the lat/lon of the give location. This
     * method assumes that that given location is interior to this triangle.
     *
     * @param loc location
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return model position
     */
    public Vector3d getModelCoordinates(GeographicPosition loc, Vector3d modelCenter)
    {
        Vector2d locVec = loc.getLatLonAlt().asVec2d();
        Vector2d vertA = myVertexA.getGeographicPositionAsVector();
        Vector2d vertB = myVertexB.getGeographicPositionAsVector();
        Vector2d vertC = myVertexC.getGeographicPositionAsVector();

        // For local coordinates, use vertex C as the origin. C to A and C to B
        // are perpendicular and are the same length.
        Vector2d xAxis = vertA.subtract(vertC);
        Vector2d yAxis = vertB.subtract(vertC);
        Vector2d cToLoc = locVec.subtract(vertC);

        double axisLength = xAxis.getLength();
        Vector2d cToLocScaled = cToLoc.divide(axisLength);
        // Unitizing the axes in this way avoids calculating the length
        // additional times.
        double xPct = cToLocScaled.dot(xAxis.divide(axisLength));
        double yPct = cToLocScaled.dot(yAxis.divide(axisLength));

        Vector3d xAxisModel = myVertexA.getModelCoordinates().subtract(myVertexC.getModelCoordinates());
        Vector3d yAxisModel = myVertexB.getModelCoordinates().subtract(myVertexC.getModelCoordinates());

        // Now what we have the axes in model coordinates and we know how far to
        // move along each axis, we can get the components of the vector from in
        // model coordinates.
        Vector3d xComponent = xAxisModel.multiply(xPct);
        Vector3d yComponent = yAxisModel.multiply(yPct);

        // Translate from local coordinates to model coordinates.
        Vector3d position = myVertexC.getModelCoordinates().add(xComponent).add(yComponent);

        if (!MathUtil.isZero(loc.getLatLonAlt().getAltM()))
        {
            // Adjust for the altitude using the vector which is normal to the
            // surface of the ellipsoid.
            Vector3d longAligned = Vector3d.UNIT_X.rotate(Vector3d.UNIT_Z, loc.getLatLonAlt().getLonD() * MathUtil.DEG_TO_RAD);
            Vector3d cross = longAligned.cross(Vector3d.UNIT_Z);
            Vector3d normal = longAligned.rotate(cross, loc.getLatLonAlt().getLatD() * MathUtil.DEG_TO_RAD);
            position = position.add(normal.multiply(loc.getLatLonAlt().getAltM()));
        }

        return position.subtract(modelCenter);
    }

    /**
     * Get the triangles whose geographic bounding circle overlaps the given
     * circle. The triangle itself may not overlap the circle.
     *
     * @param bounds The bounding geographic circle with coordinates (lat, lon).
     * @param petrified The triangles returned should match this flag.
     * @return Triangles which overlap the polygon.
     */
    public synchronized List<TerrainTriangle> getOverlappingTriangles(GeographicBoundingCircle bounds, boolean petrified)
    {
        List<TerrainTriangle> overlap = New.list();
        if (myPetrified && !petrified)
        {
            // Don't bother checking the children because all children will be
            // petrified as well.
            return overlap;
        }

        if (myGeographicPolygon.getBoundingCircle().overlaps(bounds))
        {
            if (myLeftChild != null)
            {
                overlap.addAll(myLeftChild.getOverlappingTriangles(bounds, petrified));
                overlap.addAll(myRightChild.getOverlappingTriangles(bounds, petrified));
            }
            else if (myPetrified == petrified)
            {
                overlap.add(this);
            }
        }
        return overlap;
    }

    /**
     * Get the triangles which overlap the polygon.
     *
     * @param polygon The vertices of the convex polygon (counter-clockwise)
     *            which bound the region to tessellate.
     * @param petrified The triangles returned should match this flag.
     * @return Triangles which overlap the polygon.
     */
    public synchronized List<TerrainTriangle> getOverlappingTriangles(GeographicConvexPolygon polygon, boolean petrified)
    {
        if (polygon.isDegenerate())
        {
            LOGGER.error("Attempted to get tesserae for a degenerate polygon");
            return null;
        }

        GeographicBoundingCircle polygonBoundingCircle = polygon.getBoundingCircle();
        List<TerrainTriangle> searchTriangles = getOverlappingTriangles(polygonBoundingCircle, petrified);

        List<TerrainTriangle> overlap = New.list();
        for (TerrainTriangle tri : searchTriangles)
        {
            /* Using a positive epsilon causes inclusion of triangles which
             * touch the polygon. This is necessary for the cases where the
             * triangle shares lines and vertices with the polygon. Consider the
             * case of the triangle defined by (0, 0), (6, 0), (3, 3) and the
             * square defined by (3, 0), (6, 0), (6, 3), (3, 3). These overlap
             * but neither strictly contains vertices from the other and there
             * are no strict line crossings. */
            if (polygon.overlaps(tri.getGeographicPolygon(), MathUtil.DBL_EPSILON))
            {
                // all of the search triangles will already have the correct
                // petrification, so there is no need to check again.
                overlap.add(tri);
            }
        }

        return overlap;
    }

    /**
     * Get the triangles (this triangle or its children) which overlap the given
     * polygon.
     *
     * @param polygon The polygon for which overlapping triangles are desired.
     * @param fullyContained Triangles which are full contained within the
     *            polygon.
     * @param partiallyContained Triangles which overlap but are not fully
     *            contained within the polygon.
     */
    public void getOverlappingTriangles(Polygon polygon, Collection<TerrainTriangle> fullyContained,
            Collection<TerrainTriangle> partiallyContained)
    {
        if (getJTSPolygon().intersects(polygon))
        {
            if (myLeftChild != null)
            {
                myLeftChild.getOverlappingTriangles(polygon, fullyContained, partiallyContained);
                myRightChild.getOverlappingTriangles(polygon, fullyContained, partiallyContained);
            }
            else
            {
                if (polygon.covers(getJTSPolygon()))
                {
                    fullyContained.add(this);
                }
                else
                {
                    partiallyContained.add(this);
                }
            }
        }
    }

    /**
     * Get the parent triangle.
     *
     * @return the parent to get.
     */
    public TerrainTriangle getParent()
    {
        return myParent;
    }

    /**
     * Get the plane that is defined by my vertices.
     *
     * @return the plane defined by me.
     */
    public Plane getPlane()
    {
        if (myPlane == null)
        {
            Vector3d normal = null;
            if (isDegenerateOnGlobe())
            {
                normal = myVertexA.getModelCoordinates().getNormalized();
            }
            else
            {
                Vector3d ab = myVertexB.getModelCoordinates().subtract(myVertexA.getModelCoordinates()).getNormalized();
                Vector3d bc = myVertexC.getModelCoordinates().subtract(myVertexB.getModelCoordinates()).getNormalized();
                normal = ab.cross(bc);
            }
            myPlane = new Plane(myVertexC.getModelCoordinates(), normal);
        }
        return myPlane;
    }

    /**
     * If I have split, get my right side child.
     *
     * @return the right side chile.
     */
    public TerrainTriangle getRightChild()
    {
        return myRightChild;
    }

    /**
     * Given two points which define a line segment, determine where the line
     * segment intersects the plane.
     *
     * @param plane The plane with which to intersect.
     * @param ptA first point
     * @param ptB second point
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return intersection of the line and the plane. If the line segment is on
     *         the plane, return both end points.
     */
    public List<TerrainVertex> getSegmentIntersection(Plane plane, TerrainVertex ptA, TerrainVertex ptB, Vector3d modelCenter)
    {
        List<Vector3d> coords = plane.getSegmentIntersection(ptA.getModelCoordinates(), ptB.getModelCoordinates());
        List<TerrainVertex> intersections = New.list(coords.size());
        for (Vector3d modelCoord : coords)
        {
            TerrainVertex intersectVertex;
            intersectVertex = new TerrainVertex(null, modelCoord.subtract(modelCenter));
            intersections.add(intersectVertex);
        }
        return intersections;
    }

    /**
     * Get the splitMergeHelper.
     *
     * @return the splitMergeHelper
     */
    public TerrainTriangleSplitMergeHelper getSplitMergeHelper()
    {
        return mySplitMergeHelper;
    }

    /**
     * Get all of the points at the lowest level where the line intersects this
     * triangle or its children. Do NOT, handle the case where the line would
     * intersect triangles by wrapping through +180/-180.
     *
     * @param intersections Collection of points to which to add the
     *            intersections.
     * @param ptA first point of the line of intersection in geographic
     *            coordinates.
     * @param ptB second point of the line of intersection in geographic
     *            coordinates.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     */
    public void getStrictFlatIntersections(List<TerrainVertex> intersections, Vector2d ptA, Vector2d ptB, Vector3d modelCenter)
    {
        if (!hasFlatIntersection(ptA, ptB) && !contains(new GeographicPosition(LatLonAlt.createFromVec2d(ptA))))
        {
            return;
        }

        if (myLeftChild != null)
        {
            myLeftChild.getStrictFlatIntersections(intersections, ptA, ptB, modelCenter);
            myRightChild.getStrictFlatIntersections(intersections, ptA, ptB, modelCenter);
        }
        else
        {
            if (isDegenerateOnGlobe())
            {
                return;
            }
            Vector2d normalPerp = ptB.subtract(ptA).getNormalized();
            Vector2d normal = new Vector2d(normalPerp.getY(), -normalPerp.getX());
            Line2d line = new Line2d(ptA, normal);
            intersections.addAll(getFlatSegmentIntersection(line, myVertexA, myVertexB, modelCenter));
            intersections.addAll(getFlatSegmentIntersection(line, myVertexB, myVertexC, modelCenter));
            intersections.addAll(getFlatSegmentIntersection(line, myVertexC, myVertexA, modelCenter));
        }
    }

    /**
     * Get the leaf level intersections the ray with the terrain.
     *
     * @param ray The ray to intersect with the terrain.
     * @param view The current standard viewer.
     * @return The model coordinates where the ray intersects the terrain.
     */
    public synchronized List<Vector3d> getTerrainIntersection(Ray3d ray, Viewer view)
    {
        List<Vector3d> intersections = New.list();
        if (view == null || isInView(view))
        {
            if (myLeftChild != null)
            {
                if (ray.getIntersection(getModelBoundingSphere()) != null)
                {
                    intersections.addAll(myLeftChild.getTerrainIntersection(ray, view));
                    intersections.addAll(myRightChild.getTerrainIntersection(ray, view));
                }
            }
            else
            {
                if (getPlane().isInFront(ray.getPosition(), 0.))
                {
                    Plane triPlane = getPlane();
                    Vector3d planeIntersect = triPlane.getIntersection(ray);
                    if (planeIntersect != null && containsModelPosition(planeIntersect))
                    {
                        intersections.add(planeIntersect);
                    }
                }
            }
        }

        return intersections;
    }

    /**
     * Add my tesserae to the appropriate builder.
     *
     * @param triBuilder Builder for triangle tessera.
     * @param quadBuilder Builder for quad tessera.
     * @param polygon The vertices of the convex polygon (counter-clockwise)
     *            which bound the region to tessellate.
     * @param petrified The tesserae loaded into the builders should match the
     *            this flag.
     */
    public void getTesserae(SimpleTesseraBlockBuilder<TerrainVertex> triBuilder,
            SimpleTesseraBlockBuilder<TerrainVertex> quadBuilder, GeographicConvexPolygon polygon, boolean petrified)
    {
        if (polygon.isDegenerate())
        {
            LOGGER.error("Attempted to get tesserae for a degenerate polygon");
            return;
        }

        List<TerrainTriangle> triangles = getOverlappingTriangles(polygon, petrified);

        // Any triangles still left in the searchTriangles are in front of all
        // of the sides of the polygon and therefore overlap it.
        for (TerrainTriangle tess : triangles)
        {
            // TODO Currently the sets geometries added by addQuadTessera and
            // addTriangleTessera() overlap and cause rendering artifacts.
            //            if (tess.touchesPole())
            //            {
            //                tess.addQuadTessera(quadBuilder);
            //            }
            //            else
            //            {
            //                tess.addTriangleTessera(triBuilder);
            //            }
            if (!tess.isDegenerateOnGlobe())
            {
                tess.addTriangleTessera(triBuilder);
            }
        }
    }

    @Override
    public List<? extends TesseraVertex<GeographicPosition>> getTesseraVertices()
    {
        return Arrays.asList(myVertexA, myVertexB, myVertexC);
    }

    /**
     * My first vertex.
     *
     * @return My first vertex.
     */
    public TerrainVertex getVertexA()
    {
        return myVertexA;
    }

    /**
     * My second vertex.
     *
     * @return My second vertex.
     */
    public TerrainVertex getVertexB()
    {
        return myVertexB;
    }

    /**
     * My third vertex.
     *
     * @return My third vertex.
     */
    public TerrainVertex getVertexC()
    {
        return myVertexC;
    }

    /**
     * Get all of my vertices as an array.
     *
     * @return array of my vertices.
     */
    public final TerrainVertex[] getVertices()
    {
        TerrainVertex[] verts = new TerrainVertex[3];
        verts[0] = myVertexA;
        verts[1] = myVertexB;
        verts[2] = myVertexC;
        return verts;
    }

    /**
     * Check to see if any of my line segments intersect the line.
     *
     * @param ptA first point of the line of intersection in geographic
     *            coordinates.
     * @param ptB second point of the line of intersection in geographic
     *            coordinates.
     * @return true when one or more line segments intersect the line.
     */
    public boolean hasFlatIntersection(Vector2d ptA, Vector2d ptB)
    {
        if (isDegenerateOnGlobe())
        {
            return true;
        }

        Vector2d vertA = myVertexA.getGeographicPositionAsVector();
        Vector2d vertB = myVertexB.getGeographicPositionAsVector();
        Vector2d vertC = myVertexC.getGeographicPositionAsVector();

        return LineSegment2d.segmentsIntersect(ptA, ptB, vertA, vertB) || LineSegment2d.segmentsIntersect(ptA, ptB, vertB, vertC)
                || LineSegment2d.segmentsIntersect(ptA, ptB, vertC, vertA);
    }

    /**
     * Check to see if any of my line segments intersect the plane.
     *
     * @param plane Plane in model coordinates.
     * @return true when one or more line segments intersect the plane.
     */
    public boolean hasIntersection(Plane plane)
    {
        // TODO this isn't currently being used, but returning true for
        // degenerate triangles seems wrong.
        // What this really needs to do is check to see if the curved that the
        // triangle represents intersects the plane.
        if (isDegenerateOnGlobe())
        {
            return true;
        }
        return plane.hasIntersection(myVertexA.getModelCoordinates(), myVertexB.getModelCoordinates())
                || plane.hasIntersection(myVertexB.getModelCoordinates(), myVertexC.getModelCoordinates())
                || plane.hasIntersection(myVertexC.getModelCoordinates(), myVertexA.getModelCoordinates());
    }

    /**
     * Determine whether this triangle is degenerate when converted to model
     * coordinates.
     *
     * @return true if this triangle is degenerate when converted to model
     *         coordinates.
     */
    public boolean isDegenerateOnGlobe()
    {
        double latA = myVertexA.getCoordinates().getLatLonAlt().getLatD();
        double latB = myVertexB.getCoordinates().getLatLonAlt().getLatD();
        double latC = myVertexC.getCoordinates().getLatLonAlt().getLatD();
        if (latA > AT_NORTH_POLE)
        {
            return latB > AT_NORTH_POLE || latC > AT_NORTH_POLE;
        }
        else if (latB > AT_NORTH_POLE)
        {
            return latC > AT_NORTH_POLE;
        }
        else if (latA < AT_SOUTH_POLE)
        {
            return latB < AT_SOUTH_POLE || latC < AT_SOUTH_POLE;
        }
        else
        {
            return latB < AT_SOUTH_POLE && latC < AT_SOUTH_POLE;
        }
    }

    /**
     * Determine whether I overlap the box. This method uses the bounding circle
     * for the triangle for performance reasons, so it may return true when the
     * triangle is close to the box, but not overlapping.
     *
     * @param box bounding box
     * @return true if I overlap the box
     */
    public boolean isInBox(GeographicBoundingBox box)
    {
        GeographicPosition position = myGeographicPolygon.getBoundingCircle().getCenter();
        return box.contains(position, myGeographicPolygon.getBoundingCircle().getRadiusD());
    }

    /**
     * Determine whether I am in view.
     *
     * @param view the viewer
     * @return true if I am in view
     */
    public boolean isInView(Viewer view)
    {
        return view.isInView(getModelBoundingSphere().getCenter(), getModelBoundingSphere().getRadius());
    }

    /**
     * Determine whether the triangle is a neighbor of mine.
     *
     * @param tri triangle
     * @return true if the triangle is a neighbor of mine
     */
    public boolean isNeighbor(TerrainTriangle tri)
    {
        return Utilities.sameInstance(myAdjacentA, tri) || Utilities.sameInstance(myAdjacentB, tri)
                || Utilities.sameInstance(myAdjacentC, tri);
    }

    /**
     * Get the petrified.
     *
     * @return the petrified
     */
    public boolean isPetrified()
    {
        return myPetrified;
    }

    /**
     * Reset the elevation for me or my children which overlap the given region.
     *
     * @param region The region over which to reset terrain.
     * @param providerChanged true when the provider has been added or removed
     *            for the region.
     */
    public void modifyElevation(GeographicPolygon region, boolean providerChanged)
    {
        if (isPetrified())
        {
            return;
        }

        boolean overlap = false;
        for (TerrainVertex vertex : getVertices())
        {
            if (region.contains(vertex.getCoordinates(), 0.))
            {
                vertex.setElevationCurrent(false);
                overlap = true;
            }
        }

        if (!overlap)
        {
            for (GeographicPosition vertex : region.getVertices())
            {
                if (myGeographicPolygon.contains(vertex, 0.))
                {
                    overlap = true;
                    break;
                }
            }
        }

        if (overlap)
        {
            if (providerChanged)
            {
                resetElevationProvider();
            }

            if (myLeftChild != null)
            {
                myLeftChild.modifyElevation(region, providerChanged);
                myRightChild.modifyElevation(region, providerChanged);
            }
            else
            {
                resetTerrain();
            }

            // Since the vertices are shared by other triangles, we may need to
            // reset the bounding sphere even if we didn't change any vertices
            // ourselves.
            setModelBoundingSphere();
        }
    }

    /**
     * Replace an adjacent triangle with an new one.
     *
     * @param oldTri old adjacent
     * @param newTri new adjacent
     */
    public void replaceAdjacent(TerrainTriangle oldTri, TerrainTriangle newTri)
    {
        if (Utilities.sameInstance(myAdjacentA, oldTri))
        {
            myAdjacentA = newTri;
        }
        else if (Utilities.sameInstance(myAdjacentB, oldTri))
        {
            myAdjacentB = newTri;
        }
        else if (Utilities.sameInstance(myAdjacentC, oldTri))
        {
            myAdjacentC = newTri;
        }
    }

    /** Determine the resolution hint for this triangle and any children. */
    public void resetElevationParameters()
    {
        if (myPetrified)
        {
            return;
        }

        mySplitMergeHelper.resetElevationParameters();

        if (myLeftChild != null)
        {
            myLeftChild.resetElevationParameters();
            myRightChild.resetElevationParameters();
        }
    }

    /**
     * Reset the size of this triangle in the view based on the given viewer.
     *
     * @param view the current viewer.
     */
    public void resetViewSize(Viewer view)
    {
        if (view == null || myPetrified)
        {
            return;
        }

        mySplitMergeHelper.resetViewSize(view);

        if (myLeftChild != null)
        {
            myLeftChild.resetViewSize(view);
            myRightChild.resetViewSize(view);
        }
    }

    /**
     * Set the triangle adjacent to me across from my vertex a.
     *
     * @param adjacentA the adjacent triange to set.
     */
    public void setAdjacentA(TerrainTriangle adjacentA)
    {
        myAdjacentA = adjacentA;
    }

    /**
     * Set the triangle adjacent to me across from my vertex b.
     *
     * @param adjacentB the adjacent triange to set.
     */
    public void setAdjacentB(TerrainTriangle adjacentB)
    {
        myAdjacentB = adjacentB;
    }

    /**
     * Set the triangle adjacent to me across from my vertex c.
     *
     * @param adjacentC the adjacent triange to set.
     */
    public void setAdjacentC(TerrainTriangle adjacentC)
    {
        myAdjacentC = adjacentC;
    }

    /**
     * Set the adjacent triangles for my children.
     */
    public void setChildAdjacents()
    {
        if (myAdjacentA != null)
        {
            myAdjacentA.replaceAdjacent(this, myRightChild);
        }

        if (myAdjacentB != null)
        {
            myAdjacentB.replaceAdjacent(this, myLeftChild);
        }

        myLeftChild.setAdjacentB(myRightChild);
        myRightChild.setAdjacentA(myLeftChild);

        myLeftChild.setAdjacentC(myAdjacentB);
        myRightChild.setAdjacentC(myAdjacentA);

        if (myAdjacentC != null)
        {
            // Right child's adjacentB is myAdjacentC's left child's adjacentA
            myRightChild.setAdjacentB(myAdjacentC.getLeftChild());

            // Left child's adjacentA is myAdjacentC's right child's adjacentB
            myLeftChild.setAdjacentA(myAdjacentC.getRightChild());
        }
    }

    /**
     * Set the globe.
     *
     * @param globe globe to set.
     */
    public void setGlobe(TriangleGlobeModel globe)
    {
        myGlobe = globe;
        resetTerrain();
    }

    /**
     * Set the index.
     *
     * @param index the index to set
     */
    public void setIndex(int index)
    {
        myIndex = index;
    }

    /**
     * Set the child on my left side.
     *
     * @param leftChild the triangle to set.
     */
    public void setLeftChild(TerrainTriangle leftChild)
    {
        myLeftChild = leftChild;
    }

    /** Recalculate my bounding sphere in model coordinates. */
    public final void setModelBoundingSphere()
    {
        myModelBoundingSphere = DefaultSphere.genMinimumBoundingSphere(myVertexA.getModelCoordinates(),
                myVertexB.getModelCoordinates(), myVertexC.getModelCoordinates());
    }

    /**
     * Set the triangle who is my parent.
     *
     * @param parent the triangle to set.
     */
    public void setParent(TerrainTriangle parent)
    {
        myParent = parent;
    }

    /**
     * Set the triangle who is my right child.
     *
     * @param rightChild the triangle to set.
     */
    public void setRightChild(TerrainTriangle rightChild)
    {
        myRightChild = rightChild;
    }

    /**
     * Set the first vertex.
     *
     * @param vertexA the vertex to set.
     */
    public void setVertexA(TerrainVertex vertexA)
    {
        myVertexA = vertexA;
    }

    /**
     * Set the second vertex.
     *
     * @param vertexB the vertex to set.
     */
    public void setVertexB(TerrainVertex vertexB)
    {
        myVertexB = vertexB;
    }

    /**
     * Set the third vertex.
     *
     * @param vertexC the vertex to set.
     */
    public void setVertexC(TerrainVertex vertexC)
    {
        myVertexC = vertexC;
    }

    @Override
    public String toString()
    {
        return "[" + myVertexA.getCoordinates().getLatLonAlt() + ", " + myVertexB.getCoordinates().getLatLonAlt() + ", "
                + myVertexC.getCoordinates().getLatLonAlt() + "]";
    }

    /**
     * Determine whether this triangle touches one of the poles.
     *
     * @return true when this triangle touches one of the poles.
     */
    public boolean touchesPole()
    {
        return Math.abs(myVertexA.getCoordinates().getLatLonAlt().getLatD()) > AT_NORTH_POLE
                || Math.abs(myVertexB.getCoordinates().getLatLonAlt().getLatD()) > AT_NORTH_POLE
                || Math.abs(myVertexC.getCoordinates().getLatLonAlt().getLatD()) > AT_NORTH_POLE;
    }

    /**
     * Add the quad which contains this triangle to the builder.
     *
     * @param quadBuilder The builder to which to add.
     */
    protected void addQuadTessera(SimpleTesseraBlockBuilder<TerrainVertex> quadBuilder)
    {
        // if I am not a zero area triangle, then skip me. The area I cover will
        // be added when myAdjacentC is added.
        if (isDegenerateOnGlobe())
        {
            if (myAdjacentC == null)
            {
                myParent.addQuadTessera(quadBuilder);
            }
            else
            {
                if (Math.abs(myVertexA.getCoordinates().getLatLonAlt().getLatD()) > AT_NORTH_POLE)
                {
                    TerrainVertex[] quad = { myAdjacentC.getVertexC(), myVertexB, myVertexC, myVertexA };
                    subdivide(quadBuilder, quad);
                }
                else
                {
                    TerrainVertex[] quad = { myVertexA, myAdjacentC.getVertexC(), myVertexB, myVertexC };
                    subdivide(quadBuilder, quad);
                }
            }
        }
    }

    /**
     * Add this triangle to the tesserae.
     *
     * @param triBuilder The builder to which to add the tessera.
     */
    protected void addTriangleTessera(SimpleTesseraBlockBuilder<TerrainVertex> triBuilder)
    {
        if (!isDegenerateOnGlobe())
        {
            triBuilder.add(getVertices());
        }
    }

    /**
     * Create the children for this triangle using the given vertex.
     *
     * @param splitVert The vertex which divides my hypotenuse.
     */
    protected void createChildren(TerrainVertex splitVert)
    {
        myLeftChild = new TerrainTriangle(this, myVertexC, myVertexA, splitVert);
        myRightChild = new TerrainTriangle(this, myVertexB, myVertexC, splitVert);
    }

    /**
     * The globe of which I am a part.
     *
     * @return The globe of which I am a part.
     */
    protected final TriangleGlobeModel getGlobe()
    {
        return myGlobe;
    }

    /**
     * For my neighbors and myself set adjacent triangles as necessary to
     * replace the children I am about to remove.
     */
    protected void resetAdjacentsForMerge()
    {
        myAdjacentA = myRightChild.getAdjacentC();
        myAdjacentB = myLeftChild.getAdjacentC();

        if (myAdjacentA != null)
        {
            myAdjacentA.replaceAdjacent(myRightChild, this);
            if (myAdjacentA.getParent() != null)
            {
                myAdjacentA.getParent().replaceAdjacent(myRightChild, this);
            }
        }

        if (myAdjacentB != null)
        {
            myAdjacentB.replaceAdjacent(myLeftChild, this);
            if (myAdjacentB.getParent() != null)
            {
                myAdjacentB.getParent().replaceAdjacent(myLeftChild, this);
            }
        }
    }

    /**
     * If a single provider provides elevation for all of my vertices, find it.
     * Otherwise, set the provider to <code>null</code>.
     */
    protected void resetElevationProvider()
    {
        if (myPetrified)
        {
            return;
        }

        // TODO Is there an efficient way to know whether my parent's provider
        // is also my provider?
        ElevationManager manager = getGlobe().getCelestialBody().getElevationManager();
        AbsoluteElevationProvider prov = manager.getProviderForPosition(myVertexA.getCoordinates());
        if (!Utilities.sameInstance(prov, manager.getProviderForPosition(myVertexB.getCoordinates()))
                || !Utilities.sameInstance(prov, manager.getProviderForPosition(myVertexC.getCoordinates())))
        {
            prov = null;
        }

        if (!Utilities.sameInstance(myElevationProvider, prov) || prov == null)
        {
            resetElevationParameters();
        }

        myElevationProvider = prov;
    }

    /**
     * Obtain the model coordinates for my vertices based on the latest terrain.
     */
    protected final void resetTerrain()
    {
        if (getGlobe() == null)
        {
            // The globe has not been set yet. No need to worry about this, as
            // it should be set later.
            return;
        }

        for (TerrainVertex vertex : getVertices())
        {
            if (!vertex.isElevationCurrent())
            {
                vertex.setModelCoordinates(getElevationAdjustedModelPosition(vertex.getCoordinates(), Vector3d.ORIGIN));
                vertex.setElevationCurrent(true);
            }
        }

        setModelBoundingSphere();
        myPlane = null;
    }

    /**
     * Build a quad which occupies the given block over the given percentages.
     *
     * @param builder Builder for the tessera blocks.
     * @param modelPositions Model positions for the quad which is being
     *            divided.
     * @param geoPositions Geographic positions for the quad which is being
     *            divided.
     * @param startPct The starting percentage for the division to occupy.
     * @param endPct The ending percentage for the division to occupy.
     */
    private void buildQuadForDivision(SimpleTesseraBlockBuilder<TerrainVertex> builder, Vector3d[] modelPositions,
            GeographicPosition[] geoPositions, double startPct, double endPct)
    {
        GeographicPosition coord;
        Vector3d model;

        TerrainVertex[] quad = new TerrainVertex[4];
        coord = geoPositions[0].interpolate(geoPositions[1], startPct);
        model = modelPositions[0].interpolate(modelPositions[1], startPct);
        quad[0] = new TerrainVertex(coord, model);

        coord = geoPositions[2].interpolate(geoPositions[3], startPct);
        model = modelPositions[2].interpolate(modelPositions[3], startPct);
        quad[1] = new TerrainVertex(coord, model);

        coord = geoPositions[2].interpolate(geoPositions[3], endPct);
        model = modelPositions[2].interpolate(modelPositions[3], endPct);
        quad[2] = new TerrainVertex(coord, model);

        coord = geoPositions[0].interpolate(geoPositions[1], endPct);
        model = modelPositions[0].interpolate(modelPositions[1], endPct);
        quad[3] = new TerrainVertex(coord, model);

        builder.add(quad);
    }

    /** Petrify this triangle and clean up any unnecessary fields. */
    private void petrify()
    {
        if (!myPetrified)
        {
            myPetrified = true;
            mySplitMergeHelper = null;

            if (myLeftChild != null)
            {
                myLeftChild.petrify();
                myRightChild.petrify();
            }
        }
    }

    /**
     * Divide the quad into pieces to make rendering at the poles look nicer.
     * When a quad is collapsed at one end, GL renders it like a triangle and
     * the texture is not correctly scaled.
     *
     * @param builder Builder for the tessera block.
     * @param quad the corners of the quad.
     */
    private void subdivide(SimpleTesseraBlockBuilder<TerrainVertex> builder, TerrainVertex[] quad)
    {
        GeographicPosition coord0 = quad[0].getCoordinates().interpolate(quad[1].getCoordinates(), 0);
        Vector3d model0 = quad[0].getModelCoordinates().interpolate(quad[1].getModelCoordinates(), 0);
        GeographicPosition coord1 = quad[3].getCoordinates().interpolate(quad[2].getCoordinates(), 0);
        Vector3d model1 = quad[3].getModelCoordinates().interpolate(quad[2].getModelCoordinates(), 0);
        GeographicPosition coord2 = quad[0].getCoordinates().interpolate(quad[1].getCoordinates(), 1);
        Vector3d model2 = quad[0].getModelCoordinates().interpolate(quad[1].getModelCoordinates(), 1);
        GeographicPosition coord3 = quad[3].getCoordinates().interpolate(quad[2].getCoordinates(), 1);
        Vector3d model3 = quad[3].getModelCoordinates().interpolate(quad[2].getModelCoordinates(), 1);

        Vector3d[] modelPositions = { model0, model1, model2, model3 };
        GeographicPosition[] geoPositions = { coord0, coord1, coord2, coord3 };

        double previous = 0.;
        for (int i = 1; i < 4; ++i)
        {
            double mid = 1. - Math.pow(.5, i);
            buildQuadForDivision(builder, modelPositions, geoPositions, previous, mid);
            previous = mid;
        }
        buildQuadForDivision(builder, modelPositions, geoPositions, previous, 1.);
    }
}
