package io.opensphere.core.util.jts;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationAlgorithm;
import org.poly2tri.triangulation.TriangulationContext;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;
import io.opensphere.core.model.Tessera.TesseraVertex;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;

/** A utility class for creating a triangular tessellation for polygons. */
public final class PolygonTriangulationUtil
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolygonTriangulationUtil.class);

    /**
     * Generate a triangular tessellation which covers the given polygon.
     *
     * @param triBuilder The builder which contains the tessellation. The
     *            builder may already contain tesserae if the given polygon is
     *            part of a larger tessellation.
     * @param polygon The polygon to tessellate.
     * @param vertexGenerator The generator which supplies vertices when new
     *            vertices are required.
     * @param <T> the Type of vertex used in the tessellation.
     */
    @SuppressWarnings("unchecked")
    public static <T extends TesseraVertex<?>> void triangulatePolygon(SimpleTesseraBlockBuilder<T> triBuilder, Polygon polygon,
            VertexGenerator<T> vertexGenerator)
    {
        org.poly2tri.geometry.polygon.Polygon poly2TriPoly = convertToPoly2TriPolygon(polygon);
        if (poly2TriPoly == null || poly2TriPoly.getPoints().size() < 3)
        {
            return;
        }

        // When the polygon is invalid, Poly2Tri can throw StackOverFlowError or
        // NullPointerException.
        try
        {
            TriangulationContext<?> context = Poly2Tri.createContext(TriangulationAlgorithm.DTSweep);
            context.prepareTriangulation(poly2TriPoly);
            Poly2Tri.triangulate(context);
            context.terminateTriangulation();
            context.clear();
        }
        catch (RuntimeException re)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.error("Failed to generate triangle fill for polygon " + polygon, re);
            }
        }
        catch (Error e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.error("Failed to generate triangle fill for polygon " + polygon, e);
            }
        }

        for (DelaunayTriangle tri : poly2TriPoly.getTriangles())
        {
            T vertexA = vertexGenerator.getVertex(tri.points[0].getX(), tri.points[0].getY(), tri.points[0].getZ());
            T vertexB = vertexGenerator.getVertex(tri.points[1].getX(), tri.points[1].getY(), tri.points[1].getZ());
            T vertexC = vertexGenerator.getVertex(tri.points[2].getX(), tri.points[2].getY(), tri.points[2].getZ());

            if (vertexGenerator.hasCorrectFacing(vertexA, vertexB, vertexC))
            {
                triBuilder.add((T[])new TesseraVertex<?>[] { vertexA, vertexB, vertexC });
            }
            else
            {
                triBuilder.add((T[])new TesseraVertex<?>[] { vertexA, vertexC, vertexB });
            }
        }
    }

    /**
     * Convert a JTS polygon to a Poly2Tri polygon.
     *
     * @param jtsPoly The JTS polygon
     * @return The newly created Poly2Tri polygon.
     */
    private static org.poly2tri.geometry.polygon.Polygon convertToPoly2TriPolygon(Polygon jtsPoly)
    {
        int dimension = jtsPoly.getDimension();
        if (dimension < 2 || dimension > 3)
        {
            LOGGER.warn("Could not process polygon with " + dimension + " dimensions");
            return null;
        }

        org.poly2tri.geometry.polygon.Polygon poly2TriPoly = createPolygonFromCoords(jtsPoly.getExteriorRing().getCoordinates(),
                dimension);
        for (int i = 0; i < jtsPoly.getNumInteriorRing(); ++i)
        {
            org.poly2tri.geometry.polygon.Polygon hole = createPolygonFromCoords(jtsPoly.getInteriorRingN(i).getCoordinates(),
                    dimension);
            poly2TriPoly.addHole(hole);
        }

        return poly2TriPoly;
    }

    /**
     * Create a Poly2Tri polygon from the JTS coordinates.
     *
     * @param coords The JTS coordinates which form the polygon.
     * @param dimension The dimensions of the polygon.
     * @return The newly created Poly2Tri polygon.
     */
    @SuppressWarnings("null")
    private static org.poly2tri.geometry.polygon.Polygon createPolygonFromCoords(Coordinate[] coords, int dimension)
    {
        List<PolygonPoint> points = New.list(coords.length);

        Coordinate vertex = null;
        Coordinate lastVertex = null;
        Coordinate twoAgoVertex = null;
        for (int i = 0; i < coords.length; ++i)
        {
            vertex = coords[i];
            /* Do not allow duplicate points. Also, Poly2Tri requires the
             * polygon to NOT be a closed ring, so remove the last point if it
             * closes the polygon. */
            if (lastVertex != null && JTSUtilities.coordinateEquals(vertex, lastVertex, dimension))
            {
                continue;
            }

            // Do not allow degenerate regions.
            if (twoAgoVertex != null && JTSUtilities.coordinateEquals(vertex, twoAgoVertex, dimension))
            {
                points.remove(points.size() - 1);
                continue;
            }

            PolygonPoint point;
            if (dimension == 2)
            {
                point = new PolygonPoint(vertex.x, vertex.y);
            }
            else
            {
                point = new PolygonPoint(vertex.x, vertex.y, vertex.z);
            }
            points.add(point);
            twoAgoVertex = lastVertex;
            lastVertex = vertex;
        }

        removeClosingPoint(points);
        return new org.poly2tri.geometry.polygon.Polygon(points);
    }

    /**
     * Tell whether these points represent the same position.
     *
     * @param pt1 The first point to compare.
     * @param pt2 The second point to compare.
     * @return true when the points are at the same location.
     */
    private static boolean pointsEqual(PolygonPoint pt1, PolygonPoint pt2)
    {
        return MathUtil.isZero(pt1.getX() - pt2.getX()) && MathUtil.isZero(pt1.getY() - pt2.getY())
                && MathUtil.isZero(pt1.getZ() - pt2.getZ());
    }

    /**
     * Remove points from the end of the list which are the same as the
     * beginning point.
     *
     * @param points The points which will be processed.
     */
    private static void removeClosingPoint(List<PolygonPoint> points)
    {
        while (pointsEqual(points.get(0), points.get(points.size() - 1)))
        {
            points.remove(points.size() - 1);
        }
    }

    /** Disallow instantiation. */
    private PolygonTriangulationUtil()
    {
    }

    /**
     * A simple generator for tessera vertices.
     *
     * @param <T> The type of vertex this generator provides.
     */
    public abstract static class AbstractSimpleVertexGenerator<T extends TesseraVertex<? extends Position>>
    implements VertexGenerator<T>
    {
        /** The model center for the generated positions. */
        private final Vector3d myModelCenter;

        /**
         * A map of existing vertices. If a vertex has already be generated for
         * a location, the existing one will be returned rather than generating
         * a new one.
         */
        private Map<Vector3d, T> myVertices;

        /**
         * Constructor.
         *
         * @param vertices The existing vertices in the tessellation.
         * @param modelCenter The origin of the model coordinate space for the
         *            results.
         */
        public AbstractSimpleVertexGenerator(Map<Vector3d, T> vertices, Vector3d modelCenter)
        {
            if (vertices == null)
            {
                myVertices = New.map();
            }
            else
            {
                myVertices = vertices;
            }
            myModelCenter = modelCenter;
        }

        /**
         * Generate a new vertex for the location.
         *
         * @param location The location for which a vertex is required.
         * @return the newly generated vertex.
         */
        public abstract T generateVertex(Vector3d location);

        /**
         * Get the modelCenter.
         *
         * @return the modelCenter
         */
        public Vector3d getModelCenter()
        {
            return myModelCenter;
        }

        @Override
        public T getVertex(double x, double y, double z)
        {
            Vector3d location = new Vector3d(x, y, z);
            T vertex = myVertices.get(location);

            if (vertex == null)
            {
                vertex = generateVertex(location);
                myVertices.put(location, vertex);
            }

            return vertex;
        }
    }

    /**
     * The interface for generating vertices during tessellation, the method of
     * generation will be specific to the the vertex type.
     *
     * @param <T> The type of vertex used during tessellation.
     */
    public interface VertexGenerator<T extends TesseraVertex<?>>
    {
        /**
         * Get the vertex for the location, generating a new vertex if
         * necessary.
         *
         * @param x the x coordinate (or longitude) of the location.
         * @param y the y coordinate (or latitude) of the location.
         * @param z the z coordinate (or altitude) of the location.
         * @return The vertex for the given location
         */
        T getVertex(double x, double y, double z);

        /**
         * Check to see whether the tesserae defined by the three vertices in
         * order has the correct facing.
         *
         * @param vertexA the first vertex.
         * @param vertexB the second vertex.
         * @param vertexC the third vertex.
         * @return true when the facing is correct.
         */
        boolean hasCorrectFacing(T vertexA, T vertexB, T vertexC);
    }
}
