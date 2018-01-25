package io.opensphere.core.projection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.math.DefaultEllipsoid;
import io.opensphere.core.math.DefaultSphere;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.SimpleTessera;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.model.Tessera.TesseraVertex;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.model.TesseraList.TesseraBlock;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntArrayList;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;
import io.opensphere.core.util.jts.PolygonTriangulationUtil;
import io.opensphere.core.util.jts.PolygonTriangulationUtil.AbstractSimpleVertexGenerator;
import io.opensphere.core.util.jts.PolygonTriangulationUtil.VertexGenerator;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.viewer.Viewer;

/**
 * Abstract projection implementation that provides functionality common to all
 * projections.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractGeographicProjection extends AbstractProjection
{
    /** Cache for ellipsoids. */
    private final transient Map<BoundingBox<?>, Ellipsoid> myEllipsoidCache = Collections
            .synchronizedMap(New.<BoundingBox<?>, Ellipsoid>map());

    @Override
    public List<Vector3d> convertLinesToModel(List<? extends GeographicPosition> positions, int limit, LineType type,
            Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        List<Vector3d> positionList = New.list();
        int vertexCount = positions.size();
        Pair<Tessera<GeographicPosition>, ProjectionCursor> tessera = null;
        Vector3d previousModelPosition = null;
        for (int i = 1; i < limit; ++i)
        {
            int lineEndIndex = i % vertexCount;
            GeographicPosition lineEnd = positions.get(lineEndIndex);
            if (tessera == null)
            {
                tessera = convertLineToModel(positions.get(0), lineEnd, type, modelCenter);
            }
            else
            {
                tessera = convertLineToModel(tessera.getSecondObject(), lineEnd, type, modelCenter);
            }
            if (tessera.getFirstObject() != null)
            {
                List<? extends TesseraVertex<GeographicPosition>> modelVertices = tessera.getFirstObject().getTesseraVertices();
                int modelVertexCount = modelVertices.size();
                if (modelVertexCount > 0)
                {
                    if (i == 1)
                    {
                        Vector3d modelPos = ((GeographicTesseraVertex)modelVertices.get(0)).getModelCoordinates();
                        if (previousModelPosition == null || !modelPos.equals(previousModelPosition))
                        {
                            positionList.add(modelPos);
                            previousModelPosition = modelPos;
                        }
                    }
                    for (int j = 1; j < modelVertexCount; j++)
                    {
                        Vector3d modelPos = ((GeographicTesseraVertex)modelVertices.get(j)).getModelCoordinates();
                        if (previousModelPosition == null || !modelPos.equals(previousModelPosition))
                        {
                            positionList.add(modelPos);
                            previousModelPosition = modelPos;
                        }
                    }
                }
            }
        }

        return positionList;
    }

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(GeographicPosition start,
            GeographicPosition end, LineType type, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        List<GeographicTesseraVertex> vertices = New.list();
        vertices.add(new GeographicTesseraVertex(start, convertToModel(start, modelCenter)));
        GeographicTesseraVertex endVertex = new GeographicTesseraVertex(end, convertToModel(end, modelCenter));
        vertices.add(endVertex);

        Tessera<GeographicPosition> tess = new SimpleTessera<>(vertices);
        ProjectionCursor cur = new GeographicProjectionCursor(endVertex);
        return new Pair<>(tess, cur);
    }

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(ProjectionCursor start, GeographicPosition end,
            LineType type, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        List<GeographicProjectedTesseraVertex> vertices = New.list();
        vertices.add(((GeographicProjectionCursor)start).getVertex());
        GeographicTesseraVertex endVertex = new GeographicTesseraVertex(end, convertToModel(end, modelCenter));
        vertices.add(endVertex);

        Tessera<GeographicPosition> tess = new SimpleTessera<>(vertices);
        ProjectionCursor cur = new GeographicProjectionCursor(endVertex);
        return new Pair<>(tess, cur);
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertPolygonToModelMesh(Polygon polygon,
            Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);

        SimpleTesseraBlockBuilder<GeographicTesseraVertex> triBuilder = new SimpleTesseraBlockBuilder<>(3, Vector3d.ORIGIN);

        VertexGenerator<GeographicTesseraVertex> vertexGenerator = new SimpleGeographicVertexGenerator(null, this, modelCenter);
        PolygonTriangulationUtil.triangulatePolygon(triBuilder, polygon, vertexGenerator);

        List<TesseraBlock<GeographicTesseraVertex>> tess = New.list(1);
        if (!triBuilder.getBlockVertices().isEmpty())
        {
            tess.add(new TesseraBlock<>(triBuilder, false));
        }
        return new TesseraList<>(tess);
    }

    @Override
    public Collection<Vector3d> convertPositionsToModel(Collection<? extends GeographicPosition> positions, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        Collection<Vector3d> vertices = New.list(positions.size());

        for (GeographicPosition geoPos : positions)
        {
            vertices.add(convertToModel(geoPos, modelCenter));
        }

        return vertices;
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, GeographicPosition vert4, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        List<GeographicTesseraVertex> vertices = New.list();
        vertices.add(new GeographicTesseraVertex(vert1, convertToModel(vert1, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert2, convertToModel(vert2, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert3, convertToModel(vert3, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert4, convertToModel(vert4, modelCenter)));
        return new TesseraList<GeographicProjectedTesseraVertex>(vertices, 4, false);
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertTriangleToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        List<GeographicTesseraVertex> vertices = New.list();
        vertices.add(new GeographicTesseraVertex(vert1, convertToModel(vert1, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert2, convertToModel(vert2, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert3, convertToModel(vert3, modelCenter)));
        return new TesseraList<GeographicProjectedTesseraVertex>(vertices, 3, false);
    }

    @Override
    public Ellipsoid getBoundingEllipsoid(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter, boolean forceGenerate)
    {
        Ellipsoid ellipsoid = null;
        if (!forceGenerate)
        {
            ellipsoid = getEllipsoidFromCache(bbox);
        }
        if (ellipsoid == null)
        {
            // TODO It would be nice if this could be modified to get the
            // minimum bounding ellipsoid.
            GeographicBoundingBox geoBox = (GeographicBoundingBox)bbox;
            Vector3d[] modelCorners = new Vector3d[4];
            modelCorners[0] = convertToModel(geoBox.getLowerLeft(), modelCenter);
            modelCorners[1] = convertToModel(geoBox.getLowerRight(), modelCenter);
            modelCorners[2] = convertToModel(geoBox.getUpperRight(), modelCenter);
            modelCorners[3] = convertToModel(geoBox.getUpperLeft(), modelCenter);

            double xsum = 0;
            double ysum = 0;
            double zsum = 0;
            for (Vector3d corner : modelCorners)
            {
                xsum += corner.getX();
                ysum += corner.getY();
                zsum += corner.getZ();
            }
            double quarter = 1. / modelCorners.length;
            double x = xsum * quarter;
            double y = ysum * quarter;
            double z = zsum * quarter;

            Vector3d center = new Vector3d(x, y, z);
            double radius = 0;
            for (Vector3d corner : modelCorners)
            {
                radius = Math.max(radius, center.distance(corner));
            }

            Vector3d geoCenterModel = convertToModel(geoBox.getCenter(), modelCenter);

            Vector3d xAxis;
            Vector3d yAxis;
            if (modelCorners[1].equals(modelCorners[2]))
            {
                xAxis = modelCorners[0].subtract(center).getNormalized();
                yAxis = modelCorners[1].subtract(center).getNormalized();
            }
            else
            {
                xAxis = modelCorners[1].subtract(center).getNormalized();
                yAxis = modelCorners[2].subtract(center).getNormalized();
            }
            Vector3d zAxis = xAxis.cross(yAxis).getNormalized();
            // make the y axis perpendicular to both other axes.
            yAxis = zAxis.cross(xAxis);

            xAxis = xAxis.multiply(radius);
            yAxis = yAxis.multiply(radius);
            final double maximumFlattening = .05;
            zAxis = zAxis.multiply(Math.max(radius * maximumFlattening, geoCenterModel.subtract(center).getLength()));

            ellipsoid = new DefaultEllipsoid(center, xAxis, yAxis, zAxis);
            // if this is a force generated ellipsoid, then do not cache it.
            if (!forceGenerate)
            {
                cacheEllipsoid(bbox, ellipsoid);
            }
        }
        return ellipsoid;
    }

    @Override
    public Sphere getBoundingSphere(BoundingBox<GeographicPosition> bbox, Vector3d modelCenter, boolean forceGenerate)
    {
        GeographicBoundingBox geoBox = (GeographicBoundingBox)bbox;
        Vector3d[] modelCorners = new Vector3d[4];
        modelCorners[0] = convertToModel(geoBox.getLowerLeft(), modelCenter);
        modelCorners[1] = convertToModel(geoBox.getUpperRight(), modelCenter);
        modelCorners[2] = convertToModel(geoBox.getLowerRight(), modelCenter);
        modelCorners[3] = convertToModel(geoBox.getUpperLeft(), modelCenter);

        double x0 = Double.MAX_VALUE;
        double x1 = -Double.MAX_VALUE;
        double y0 = Double.MAX_VALUE;
        double y1 = -Double.MAX_VALUE;
        double z0 = Double.MAX_VALUE;
        double z1 = -Double.MAX_VALUE;

        for (Vector3d corner : modelCorners)
        {
            x0 = Math.min(x0, corner.getX());
            x1 = Math.max(x1, corner.getX());
            y0 = Math.min(y0, corner.getY());
            y1 = Math.max(y1, corner.getY());
            z0 = Math.min(z0, corner.getZ());
            z1 = Math.max(z1, corner.getZ());
        }

        double x = (x0 + x1) * .5f;
        double y = (y0 + y1) * .5f;
        double z = (z0 + z1) * .5f;
        Vector3d center = new Vector3d(x, y, z);
        double radius = 0;
        for (Vector3d corner : modelCorners)
        {
            radius = Math.max(radius, center.distance(corner));
        }
        return new DefaultSphere(center, radius);
    }

    @Override
    public double getMinimumTerrainDistance(Viewer view)
    {
        Vector3d viewLoc = view.getPosition().getLocation();
        return getTerrainIntersection(new Ray3d(viewLoc, viewLoc.multiply(-1).getNormalized()), view).subtract(viewLoc)
                .getLength();
    }

    @Override
    public double getModelHeight()
    {
        final double northernmostLatitude = 90.;
        return convertToModel(
                new GeographicPosition(LatLonAlt.createFromDegrees(northernmostLatitude, 0., ReferenceLevel.ELLIPSOID)),
                Vector3d.ORIGIN).getY() * 2.;
    }

    @Override
    public double getModelWidth()
    {
        final double easternmostLongitude = 180.;
        return convertToModel(
                new GeographicPosition(LatLonAlt.createFromDegrees(0., easternmostLongitude, ReferenceLevel.ELLIPSOID)),
                Vector3d.ORIGIN).getX() * 2.;
    }

    @Override
    public void resetProjection(boolean highAccuracy)
    {
        // most projections will not have anything to reset.
    }

    /**
     * Put an ellipsoid in my cache.
     *
     * @param bbox The bounding box.
     * @param ellipsoid The ellipsoid.
     */
    protected void cacheEllipsoid(BoundingBox<GeographicPosition> bbox, Ellipsoid ellipsoid)
    {
        myEllipsoidCache.put(bbox, ellipsoid);
    }

    /**
     * Get an ellipsoid from my cache.
     *
     * @param bbox The bounding box.
     * @return The ellipsoid, or {@code null} if one is not cached.
     */
    protected Ellipsoid getEllipsoidFromCache(BoundingBox<GeographicPosition> bbox)
    {
        return myEllipsoidCache.get(bbox);
    }

    /**
     * Break a geographic quad into a number of <i>tesserae</i>, specifying the
     * number of splits to perform on the quad.
     *
     * @param vert1 A vertex of the quad.
     * @param vert2 A vertex of the quad.
     * @param vert3 A vertex of the quad.
     * @param vert4 A vertex of the quad.
     * @param xSplits The number of X splits.
     * @param ySplits The number of Y splits.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The tesserae.
     */
    protected TesseraList<GeographicProjectedTesseraVertex> quadTessellate(GeographicPosition vert1, GeographicPosition vert2,
            GeographicPosition vert3, GeographicPosition vert4, int xSplits, int ySplits, Vector3d modelCenter)
    {
        int rows = ySplits + 2;
        int cols = xSplits + 2;
        int cells = rows * cols;
        LatLonAlt[] grid = new LatLonAlt[cells];
        int lastColIndex = rows * (cols - 1);
        LatLonAlt llaVert1 = vert1.getLatLonAlt();
        LatLonAlt llaVert2 = vert2.getLatLonAlt();
        LatLonAlt llaVert3 = vert3.getLatLonAlt();
        LatLonAlt llaVert4 = vert4.getLatLonAlt();
        for (int j = 0; j < rows; j++)
        {
            grid[j] = llaVert1.interpolate(llaVert4, (double)j / (rows - 1));
            grid[j + lastColIndex] = llaVert2.interpolate(llaVert3, (double)j / (rows - 1));
        }

        for (int i = 1; i < cols - 1; i++)
        {
            for (int j = 0; j < rows; j++)
            {
                LatLonAlt vert = grid[j].interpolate(grid[j + lastColIndex], (double)i / (cols - 1));
                grid[j + i * rows] = vert;
            }
        }

        List<GeographicTesseraVertex> vertices = New.list(cells);
        for (int ix = 0; ix < cells; ix++)
        {
            GeographicPosition geoPos = new GeographicPosition(grid[ix]);
            Vector3d vec = convertToModel(geoPos, modelCenter);
            vertices.add(new GeographicTesseraVertex(geoPos, vec));
        }

        PetrifyableTIntList indices = new PetrifyableTIntArrayList(cells * 4);
        for (int i = 0; i < cols - 1; i++)
        {
            for (int j = 0; j < rows - 1; j++)
            {
                indices.add(j + i * rows);
                indices.add(j + (i + 1) * rows);
                indices.add(j + (i + 1) * rows + 1);
                indices.add(j + i * rows + 1);
            }
        }
        return new TesseraList<>(vertices, indices, 4, false);
    }

    /**
     * Break a geographic quad into a number of <i>tesserae</i>.
     *
     * @param vert1 A vertex of the quad.
     * @param vert2 A vertex of the quad.
     * @param vert3 A vertex of the quad.
     * @param vert4 A vertex of the quad.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The tesserae.
     */
    protected TesseraList<GeographicProjectedTesseraVertex> quadTessellate(GeographicPosition vert1, GeographicPosition vert2,
            GeographicPosition vert3, GeographicPosition vert4, Vector3d modelCenter)
    {
        List<GeographicTesseraVertex> vertices = New.list(4);
        vertices.add(new GeographicTesseraVertex(vert1, convertToModel(vert1, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert2, convertToModel(vert2, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert3, convertToModel(vert3, modelCenter)));
        vertices.add(new GeographicTesseraVertex(vert4, convertToModel(vert4, modelCenter)));
        PetrifyableTIntList indices = new PetrifyableTIntArrayList(4);
        indices.add(0);
        indices.add(1);
        indices.add(2);
        indices.add(3);
        PetrifyableTIntList result = quadTessellate(vertices, indices, modelCenter);
        return new TesseraList<>(vertices, result, 4, false);
    }

    /**
     * Break a model coordinates quad into a number of <i>tesserae</i>,
     * specifying the number of splits to perform on the quad.
     *
     * @param tessverts The list of vertices. New vertices may be added to this
     *            list.
     * @param vertIndex1 The index into the list of vertices for the first
     *            vertex.
     * @param vertIndex2 The index into the list of vertices for the second
     *            vertex.
     * @param vertIndex3 The index into the list of vertices for the third
     *            vertex.
     * @param vertIndex4 The index into the list of vertices for the fourth
     *            vertex.
     * @param xSplits The number of X splits.
     * @param ySplits The number of Y splits.
     * @return The result indices.
     */
    protected PetrifyableTIntList quadTessellate(List<GeographicTesseraVertex> tessverts, int vertIndex1, int vertIndex2,
            int vertIndex3, int vertIndex4, int xSplits, int ySplits)
    {
        GeographicTesseraVertex vert1 = tessverts.get(vertIndex1);
        GeographicTesseraVertex vert2 = tessverts.get(vertIndex2);
        GeographicTesseraVertex vert3 = tessverts.get(vertIndex3);
        GeographicTesseraVertex vert4 = tessverts.get(vertIndex4);

        Vector3d vec1 = vert1.getModelCoordinates();
        Vector3d vec2 = vert2.getModelCoordinates();
        Vector3d vec3 = vert3.getModelCoordinates();
        Vector3d vec4 = vert4.getModelCoordinates();

        int rows = ySplits + 2;
        int cols = xSplits + 2;
        int cells = rows * cols;
        Vector3d[] grid = new Vector3d[cells];
        int lastColIndex = rows * (cols - 1);

        // Fill in the corners.
        grid[0] = vec1;
        grid[lastColIndex] = vec2;
        grid[cells - 1] = vec3;
        grid[rows - 1] = vec4;

        // Fill in the first and last column for the interior rows.
        for (int j = 1; j < rows - 1; j++)
        {
            grid[j] = vec1.interpolate(vec4, (double)j / (rows - 1));
            grid[j + lastColIndex] = vec2.interpolate(vec3, (double)j / (rows - 1));
        }

        // Fill in the interior columns.
        for (int i = 1; i < cols - 1; i++)
        {
            for (int j = 0; j < rows; j++)
            {
                Vector3d vert = grid[j].interpolate(grid[j + lastColIndex], (double)i / (cols - 1));
                grid[j + i * rows] = vert;
            }
        }

        int[] cellIndices = new int[cells];
        cellIndices[0] = vertIndex1;
        cellIndices[lastColIndex] = vertIndex2;
        cellIndices[cells - 1] = vertIndex3;
        cellIndices[rows - 1] = vertIndex4;
        for (int ix = 1; ix < cells - 1; ix++)
        {
            if (ix != lastColIndex && ix != rows - 1)
            {
                cellIndices[ix] = tessverts.size();
                tessverts.add(new GeographicTesseraVertex(convertToPosition(grid[ix], ReferenceLevel.ELLIPSOID), grid[ix]));
            }
        }

        PetrifyableTIntList indices = new PetrifyableTIntArrayList((rows - 1) * (cols - 1) * 4);
        for (int i = 0; i < cols - 1; i++)
        {
            for (int j = 0; j < rows - 1; j++)
            {
                indices.add(cellIndices[j + i * rows]);
                indices.add(cellIndices[j + (i + 1) * rows]);
                indices.add(cellIndices[j + (i + 1) * rows + 1]);
                indices.add(cellIndices[j + i * rows + 1]);
            }
        }

        return indices;
    }

    /**
     * Break a quad into smaller quads (if needed).
     *
     * @param tessverts The list of vertices.
     * @param inputIndices The indices of the vertices that make up the quad
     *            being tessellated.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return The smaller tesserae.
     */
    protected PetrifyableTIntList quadTessellate(List<GeographicTesseraVertex> tessverts, PetrifyableTIntList inputIndices,
            Vector3d modelCenter)
    {
        int vertIndex1 = inputIndices.get(0);
        int vertIndex2 = inputIndices.get(1);
        int vertIndex3 = inputIndices.get(2);
        int vertIndex4 = inputIndices.get(3);

        LatLonAlt lla1 = tessverts.get(vertIndex1).getCoordinates().getLatLonAlt();
        LatLonAlt lla2 = tessverts.get(vertIndex2).getCoordinates().getLatLonAlt();
        LatLonAlt lla3 = tessverts.get(vertIndex3).getCoordinates().getLatLonAlt();
        LatLonAlt lla4 = tessverts.get(vertIndex4).getCoordinates().getLatLonAlt();

        Vector3d center = convertToModel(
                new GeographicPosition(lla1.interpolate(lla2, .5).interpolate(lla3.interpolate(lla4, .5), .5)), modelCenter);

        Vector3d vert1 = tessverts.get(vertIndex1).getModelCoordinates();
        Vector3d vert3 = tessverts.get(vertIndex3).getModelCoordinates();

        double xRatio = Math.abs((vert3.getX() - center.getX()) / (center.getX() - vert1.getX()));
        double yRatio = Math.abs((vert3.getY() - center.getY()) / (center.getY() - vert1.getY()));

        if (1f / xRatio > xRatio)
        {
            xRatio = 1f / xRatio;
        }
        if (1f / yRatio > yRatio)
        {
            yRatio = 1f / yRatio;
        }

        if (Double.isInfinite(xRatio) || Double.isInfinite(yRatio))
        {
            return inputIndices;
        }

        int xSplits = Math.min(8, Math.round((float)Math.log(xRatio)));
        int ySplits = Math.min(8, Math.round((float)Math.log(yRatio)));

        if (xSplits < 1 && ySplits < 1)
        {
            return inputIndices;
        }

        PetrifyableTIntList result = new PetrifyableTIntArrayList();

        PetrifyableTIntList indices = quadTessellate(tessverts, vertIndex1, vertIndex2, vertIndex3, vertIndex4, xSplits, ySplits);
        for (int i = 0; i < indices.size(); i += 4)
        {
            result.addAll(quadTessellate(tessverts, indices.subList(i, i + 4), modelCenter));
        }
        return result;
    }

    /**
     * A projected tessera vertex whose position is geographic.
     */
    public interface GeographicProjectedTesseraVertex extends ProjectedTesseraVertex<GeographicPosition>
    {
    }

    /** A Projection cursor for geographic projections. */
    public static class GeographicProjectionCursor implements ProjectionCursor
    {
        /** The cursor's current vertex. */
        private final GeographicProjectedTesseraVertex myVertex;

        /**
         * Constructor.
         *
         * @param vertex The cursor's current vertex.
         */
        public GeographicProjectionCursor(GeographicProjectedTesseraVertex vertex)
        {
            myVertex = vertex;
        }

        @Override
        public GeographicProjectedTesseraVertex getVertex()
        {
            return myVertex;
        }
    }

    /**
     * A simple implementation of {code Tessera.TesseraVertex} that comprises a
     * set of geographic coordinates and a set of model coordinates.
     */
    public static class GeographicTesseraVertex extends SimpleProjectedTesseraVertex<GeographicPosition>
            implements GeographicProjectedTesseraVertex
    {
        /**
         * Construct a vertex.
         *
         * @param coord The geographic coordinates.
         * @param model The model coordinates.
         */
        public GeographicTesseraVertex(GeographicPosition coord, Vector3d model)
        {
            super(coord, model);
        }

        @Override
        public GeographicTesseraVertex adjustToModelCenter(Vector3d modelCenter)
        {
            return new GeographicTesseraVertex(getCoordinates(), getModelCoordinates().subtract(modelCenter));
        }
    }

    /** A simple generator for creating geographic vertices. */
    protected static class SimpleGeographicVertexGenerator extends AbstractSimpleVertexGenerator<GeographicTesseraVertex>
    {
        /**
         * The projection used to generate model positions for the projected
         * vertex.
         */
        private final Projection myProjection;

        /**
         * Constructor.
         *
         * @param vertices The existing vertices in the tessellation.
         * @param projection The projection used to generate model positions for
         *            the projected vertex.
         * @param modelCenter The origin of the model coordinate space for the
         *            results.
         */
        public SimpleGeographicVertexGenerator(Map<Vector3d, GeographicTesseraVertex> vertices, Projection projection,
                Vector3d modelCenter)
        {
            super(vertices, modelCenter);
            myProjection = projection;
        }

        @Override
        public GeographicTesseraVertex generateVertex(Vector3d location)
        {
            LatLonAlt lla = LatLonAlt.createFromDegrees(location.getY(), location.getX(), ReferenceLevel.TERRAIN);
            GeographicPosition geo = new GeographicPosition(lla);
            Vector3d model = myProjection.convertToModel(geo, getModelCenter());
            return new GeographicTesseraVertex(geo, model);
        }

        @Override
        public boolean hasCorrectFacing(GeographicTesseraVertex vertexA, GeographicTesseraVertex vertexB,
                GeographicTesseraVertex vertexC)
        {
            // The facing direction can be determined using the cross product,
            // but we only need the part of the calculation which yields the z
            // component.
            LatLonAlt llaA = vertexA.getCoordinates().getLatLonAlt();
            LatLonAlt llaB = vertexB.getCoordinates().getLatLonAlt();
            LatLonAlt llaC = vertexC.getCoordinates().getLatLonAlt();

            double x1 = llaB.getLonD() - llaA.getLonD();
            double x2 = llaC.getLonD() - llaB.getLonD();

            double y1 = llaB.getLatD() - llaA.getLatD();
            double y2 = llaC.getLatD() - llaB.getLatD();

            double zCross = x1 * y2 - y1 * x2;

            return zCross > 0.;
        }
    }
}
