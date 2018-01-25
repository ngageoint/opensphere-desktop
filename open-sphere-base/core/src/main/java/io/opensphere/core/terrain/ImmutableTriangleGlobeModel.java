package io.opensphere.core.terrain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

import io.opensphere.core.math.DefaultSphere;
import io.opensphere.core.math.Plane;
import io.opensphere.core.math.Ray3d;
import io.opensphere.core.math.Sphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicConvexPolygon;
import io.opensphere.core.model.GeographicPolygon;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.SimpleTessera;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.model.TesseraList.TesseraBlock;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.terrain.util.AbsoluteElevationProvider;
import io.opensphere.core.terrain.util.ElevationChangedEvent;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.PolygonTriangulationUtil;
import io.opensphere.core.util.jts.PolygonTriangulationUtil.VertexGenerator;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.Viewer;

/**
 * A 3D immutable model of the globe made from triangles.
 */
@SuppressWarnings("PMD.GodClass")
public class ImmutableTriangleGlobeModel extends TriangleGlobeModel
{
    /**
     * The maximum length of a line segment in either latitude or longitude
     * before its great circle length is checked for splitting.
     */
    private static final double LINE_LENGTH_CHECK_THRESHOLD = 22.5;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImmutableTriangleGlobeModel.class);

    /**
     * The last triangle which was used to estimate terrain position for a
     * geographic location. This is useful because locations lookups tend to be
     * near each other.
     */
    private TerrainTriangle myLastEstimatedVertexTriangle;

    /**
     * Construct this globe.
     *
     * @param model The model of which I will be an immutable copy.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ImmutableTriangleGlobeModel(TriangleGlobeModel model)
    {
        super(model.getMinimumGenerations(), model.getMaximumGenerations(), model.getCelestialBody());

        for (Entry<AbsoluteElevationProvider, Map<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>>> blockEntry : model
                .getPetrifiedTerrainBlocks().entrySet())
        {
            Map<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>> tessMap = New.map();
            getPetrifiedTerrainBlocks().put(blockEntry.getKey(), tessMap);
            for (Entry<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>> tessEntry : blockEntry
                    .getValue().entrySet())
            {
                tessMap.put(tessEntry.getKey(), tessEntry.getValue());
            }
        }

        // Copy the list of locked tessera blocks
        List<TerrainTriangle> modelTriangles = New.list();
        List<TerrainVertex> modelVertices = New.list();
        model.getNorthBottom().clearIndices();
        model.getSouthBottom().clearIndices();
        model.getNorthBottom().generateIndices(modelVertices, modelTriangles);
        model.getSouthBottom().generateIndices(modelVertices, modelTriangles);

        List<TerrainVertex> clonedVertices = New.list(modelVertices.size());
        for (TerrainVertex vert : modelVertices)
        {
            clonedVertices.add(vert.snapshot());
        }

        List<TerrainTriangle> trianglesCopy = cloneTriangles(modelTriangles, clonedVertices);

        setAdjacentsAndChildren(modelTriangles, trianglesCopy);

        // attach the triangles to the globe.
        TerrainTriangle north = trianglesCopy.get(model.getNorthBottom().getIndex());
        TerrainTriangle south = trianglesCopy.get(model.getSouthBottom().getIndex());
        super.setNorthBottom(north);
        super.setSouthBottom(south);
    }

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(GeographicPosition start,
            GeographicPosition end, LineType type, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        LatLonAlt startLLA = start.getLatLonAlt();
        LatLonAlt endLLA = end.getLatLonAlt();
        if (startLLA.getAltitudeReference() != endLLA.getAltitudeReference())
        {
            throw new IllegalArgumentException("Incompatible altitudes references: " + startLLA.getAltitudeReference() + " and "
                    + endLLA.getAltitudeReference());
        }

        if (startLLA.getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
        {
            Pair<List<TerrainVertex>, TerrainModelCursor> verts = getLineModel(start, end, type, modelCenter);
            return new Pair<Tessera<GeographicPosition>, Projection.ProjectionCursor>(
                    new SimpleTessera<GeographicPosition>(verts.getFirstObject()), verts.getSecondObject());
        }
        else
        {
            return getCelestialBody().convertLineToModel(start, end, type, modelCenter);
        }
    }

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(ProjectionCursor start, GeographicPosition end,
            LineType type, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        LatLonAlt startLLA = ((GeographicProjectionCursor)start).getVertex().getCoordinates().getLatLonAlt();
        LatLonAlt endLLA = end.getLatLonAlt();
        if (startLLA.getAltitudeReference() != endLLA.getAltitudeReference())
        {
            throw new IllegalArgumentException("Incompatible altitudes references: " + startLLA.getAltitudeReference() + " and "
                    + endLLA.getAltitudeReference());
        }

        if (startLLA.getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
        {
            Pair<List<TerrainVertex>, TerrainModelCursor> verts = getLineModel((TerrainModelCursor)start, end, type, modelCenter);
            return new Pair<Tessera<GeographicPosition>, Projection.ProjectionCursor>(
                    new SimpleTessera<GeographicPosition>(verts.getFirstObject()), verts.getSecondObject());
        }
        else
        {
            return getCelestialBody().convertLineToModel(start, end, type, modelCenter);
        }
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertPolygonToModelMesh(Polygon polygon,
            Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);

        try
        {
            SimpleTesseraBlockBuilder<GeographicTesseraVertex> triBuilder = new SimpleTesseraBlockBuilder<GeographicTesseraVertex>(
                    3, modelCenter);

            Collection<TerrainTriangle> fullyContained = New.collection();
            Collection<TerrainTriangle> partiallyContained = New.collection();

            getNorthBottom().getOverlappingTriangles(polygon, fullyContained, partiallyContained);
            getSouthBottom().getOverlappingTriangles(polygon, fullyContained, partiallyContained);

            for (TerrainTriangle tri : fullyContained)
            {
                triBuilder.add(tri.getVertices());
            }

            Map<Vector3d, GeographicTesseraVertex> terrainVertices = New.map();
            for (TerrainTriangle contained : fullyContained)
            {
                for (TerrainVertex vert : contained.getVertices())
                {
                    terrainVertices.put(vert.getCoordinates().asVector3d(), vert);
                }
            }

            for (TerrainTriangle partial : partiallyContained)
            {
                Geometry geom = polygon.intersection(partial.getJTSPolygon());
                VertexGenerator<GeographicTesseraVertex> vertexGenerator = new SimpleVertexGenerator(terrainVertices, partial);
                for (int i = 0; i < geom.getNumGeometries(); ++i)
                {
                    try
                    {
                        Geometry subGeom = geom.getGeometryN(i);
                        if (subGeom instanceof Polygon)
                        {
                            PolygonTriangulationUtil.triangulatePolygon(triBuilder, (Polygon)subGeom, vertexGenerator);
                        }
                    }
                    catch (RuntimeException e)
                    {
                        LOGGER.error("Poly2Tri could not triangulate the polygon." + e, e);
                    }
                }
            }

            List<TesseraBlock<GeographicTesseraVertex>> tess = New.list(1);
            if (!triBuilder.getBlockVertices().isEmpty())
            {
                tess.add(new TesseraBlock<GeographicTesseraVertex>(triBuilder, false));
            }
            return new TesseraList<GeographicTesseraVertex>(tess);
        }
        catch (TopologyException e)
        {
            // This is likely caused by the polygon overlapping itself.
            LOGGER.error("JTS polygon intersection failed." + e);
            return null;
        }
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, GeographicPosition vert4, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        Altitude.ReferenceLevel altVert1 = vert1.getLatLonAlt().getAltitudeReference();
        Altitude.ReferenceLevel altVert2 = vert2.getLatLonAlt().getAltitudeReference();
        Altitude.ReferenceLevel altVert3 = vert3.getLatLonAlt().getAltitudeReference();
        Altitude.ReferenceLevel altVert4 = vert4.getLatLonAlt().getAltitudeReference();
        if (!Utilities.sameInstance(altVert1, altVert2) || !Utilities.sameInstance(altVert2, altVert3)
                || !Utilities.sameInstance(altVert3, altVert4))
        {
            throw new IllegalArgumentException("Incompatible altitude reference.");
        }
        if (altVert1 == Altitude.ReferenceLevel.TERRAIN)
        {
            TesseraList<? extends GeographicProjectedTesseraVertex> model = null;

            List<GeographicPosition> polygon = New.list(4);
            polygon.add(vert1);
            polygon.add(vert2);
            polygon.add(vert3);
            polygon.add(vert4);
            model = getTesserae(new GeographicConvexPolygon(polygon), modelCenter);

            return model;
        }
        else
        {
            return super.convertQuadToModel(vert1, vert2, vert3, vert4, modelCenter);
        }
    }

    @Override
    @SuppressWarnings("PMD.MissingBreakInSwitch")
    public Vector3d convertToModel(GeographicPosition inPos, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        switch (inPos.getLatLonAlt().getAltitudeReference())
        {
            case ORIGIN:
            case ELLIPSOID:
                return getCelestialBodyModelPosition(inPos, modelCenter);
            case TERRAIN:
                return getTerrainModelPosition(inPos, modelCenter);
            default:
                throw new UnexpectedEnumException(inPos.getLatLonAlt().getAltitudeReference());
        }
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        switch (altReference)
        {
            case ELLIPSOID:
                return getCelestialBody().convertToPosition(inPos, ReferenceLevel.ELLIPSOID);
            case ORIGIN:
                return getCelestialBody().convertToPosition(inPos, ReferenceLevel.ORIGIN);
            case TERRAIN:
                // TODO why do we set the alt to 0. It seems like we should
                // preserve this.
                // correct the position to be 0 above the terrain and set the
                // reverence level to terrain.
                GeographicPosition geo = getCelestialBody().convertToPosition(inPos, ReferenceLevel.ELLIPSOID);
                return new GeographicPosition(LatLonAlt.createFromDegrees(geo.getLatLonAlt().getLatD(),
                        geo.getLatLonAlt().getLonD(), ReferenceLevel.TERRAIN));
            default:
                throw new UnexpectedEnumException(altReference);
        }
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertTriangleToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        Altitude.ReferenceLevel altVert1 = vert1.getLatLonAlt().getAltitudeReference();
        Altitude.ReferenceLevel altVert2 = vert2.getLatLonAlt().getAltitudeReference();
        Altitude.ReferenceLevel altVert3 = vert3.getLatLonAlt().getAltitudeReference();
        if (!Utilities.sameInstance(altVert1, altVert2) || !Utilities.sameInstance(altVert2, altVert3))
        {
            throw new IllegalArgumentException("Incompatible altitude reference.");
        }
        if (altVert1 == Altitude.ReferenceLevel.TERRAIN)
        {
            List<GeographicPosition> polygon = New.list(3);
            polygon.add(vert1);
            polygon.add(vert2);
            polygon.add(vert3);
            return getTesserae(new GeographicConvexPolygon(polygon), modelCenter);
        }
        else
        {
            return super.convertTriangleToModel(vert1, vert2, vert3, modelCenter);
        }
    }

    @Override
    public TerrainTriangle getContainingTriangle(GeographicPosition inPos, TerrainTriangle quickCheck)
    {
        TerrainTriangle tri = null;
        if (quickCheck != null && quickCheck.contains(inPos))
        {
            tri = quickCheck;
        }

        if (tri == null)
        {
            if (getSouthBottom().contains(inPos))
            {
                tri = getSouthBottom().getContainingTriangle(inPos, false);
            }
            else
            {
                tri = getNorthBottom().getContainingTriangle(inPos, false);
            }
        }

        return tri;
    }

    @Override
    public double getElevationOnTerrainM(GeographicPosition position)
    {
        GeographicPosition flatPos = new GeographicPosition(
                LatLonAlt.createFromDegrees(position.getLatLonAlt().getLatD(), position.getLatLonAlt().getLonD()));
        Vector3d terrain = getTerrainModelPosition(flatPos, null);
        Vector3d model = getCelestialBody().convertToModel(flatPos, Vector3d.ORIGIN);
        return terrain.subtract(model).getLength();
    }

    @Override
    public double getMinimumInviewDistance(Viewer view)
    {
        // TODO this gets the distance from the viewer to the terrain, which may
        // be farther than the closest point.
        Vector3d viewLoc = view.getPosition().getLocation();
        Ray3d ray = new Ray3d(viewLoc, viewLoc.multiply(-1.).getNormalized());
        Vector3d terrain = getCelestialBody().getTerrainIntersection(ray, view);
        return terrain.subtract(viewLoc).getLength();

        // TODO This is broken. Get minimum distance relies on the triangles
        // being in view and sometimes returns bad results.
//        double northMin = getNorthBottom().getMinDistance(view);
//        double southMin = getSouthBottom().getMinDistance(view);
//        return Math.min(northMin, southMin);
    }

    @Override
    public ImmutableTriangleGlobeModel getModelSnapshot()
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Requesting snapshot from the snapshot");
        }
        return this;
    }

    @Override
    public String getName()
    {
        return "Immutable Triangle Globe Model";
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        if (inPos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.ORIGIN
                || inPos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.ELLIPSOID)
        {
            return getCelestialBody().getNormalAtPosition(inPos);
        }
        else if (inPos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
        {
            TerrainTriangle containTri = getContainingTriangle(inPos, myLastEstimatedVertexTriangle);
            return containTri.getPlane().getNormal();
        }
        else
        {
            throw new UnexpectedEnumException(inPos.getLatLonAlt().getAltitudeReference());
        }
    }

    @Override
    public Vector3d getTerrainIntersection(Ray3d ray, Viewer view)
    {
        List<Vector3d> intersections = New.list();
        intersections.addAll(getNorthBottom().getTerrainIntersection(ray, view));
        intersections.addAll(getSouthBottom().getTerrainIntersection(ray, view));

        Vector3d closest = null;
        double shortest = Double.MAX_VALUE;
        for (Vector3d vec : intersections)
        {
            double dist = ray.getPosition().distance(vec);
            if (dist < shortest)
            {
                closest = vec;
                shortest = dist;
            }
        }
        return closest;
    }

    @Override
    public Vector3d getTerrainModelPosition(GeographicPosition pos, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        if (pos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.ORIGIN
                || pos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.ELLIPSOID)
        {
            return getCelestialBody().convertToModel(pos, modelCenter);
        }
        else if (pos.getLatLonAlt().getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
        {
            TerrainTriangle containTri = getContainingTriangle(pos, myLastEstimatedVertexTriangle);
            myLastEstimatedVertexTriangle = containTri;
            return containTri.getModelCoordinates(pos, modelCenter);
        }
        else
        {
            throw new UnexpectedEnumException(pos.getLatLonAlt().getAltitudeReference());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> getTesserae(GeographicConvexPolygon polygon,
            Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, MODEL_CENTER_PARAMETER_NAME);
        SimpleTesseraBlockBuilder<TerrainVertex> triBuilder = new SimpleTesseraBlockBuilder<>(3, modelCenter);
        SimpleTesseraBlockBuilder<TerrainVertex> quadBuilder = new SimpleTesseraBlockBuilder<>(4, modelCenter);

        // When getting the tesserae, exclude any petrified regions.
        getNorthBottom().getTesserae(triBuilder, quadBuilder, polygon, false);
        getSouthBottom().getTesserae(triBuilder, quadBuilder, polygon, false);

        List<TesseraBlock<TerrainVertex>> tess = New.list(2);
        if (!triBuilder.getBlockVertices().isEmpty())
        {
            tess.add(new TesseraBlock<TerrainVertex>(triBuilder, false));
        }
        if (!quadBuilder.getBlockVertices().isEmpty())
        {
            tess.add(new TesseraBlock<TerrainVertex>(quadBuilder, false));
        }

        // Check to see if this overlaps any petrified blocks.
        // @formatter:off
        for (Entry<AbsoluteElevationProvider, Map<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>>> providerEntry
                : getPetrifiedTerrainBlocks().entrySet())
        {
        // @formatter:on
            for (Entry<GeographicPolygon, TesseraList<? extends GeographicProjectedTesseraVertex>> tesseraeEntry : providerEntry
                    .getValue().entrySet())
            {
                GeographicPolygon lockRegion = tesseraeEntry.getKey();
                if (polygon.overlaps(lockRegion, 0.))
                {
                    TesseraList<? extends GeographicProjectedTesseraVertex> lockTess = tesseraeEntry.getValue();
                    for (TesseraBlock<?> val : lockTess.getTesseraBlocks())
                    {
                        tess.add((TesseraBlock<TerrainVertex>)val);
                    }
                }
            }
        }
        return new TesseraList<TerrainVertex>(tess);
    }

    @Override
    public Collection<GeographicBoundingBox> handleElevationChange(ElevationChangedEvent event)
    {
        throw new UnsupportedOperationException("Cannot change elevation for an immutable globe");
    }

    @Override
    public Collection<GeographicBoundingBox> updateModelForView(Viewer view)
    {
        throw new UnsupportedOperationException("Cannot update model on immutable globe");
    }

    @Override
    protected void setNorthBottom(TerrainTriangle northBottom)
    {
        LOGGER.error("Attempt to set north triangle of immutable globe");
    }

    @Override
    protected void setSouthBottom(TerrainTriangle southBottom)
    {
        LOGGER.error("Attempt to set south triangle of immutable globe");
    }

    /**
     * When converting a line to conform to the terrain, it is sometimes
     * necessary to split the line. This splits the line in half and conforms
     * each half to the terrain, then combines the resulting vertices into a
     * single collection.
     *
     * @param start start of line
     * @param end end of line
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return line segments which follow the model and the cursor for the last
     *         vertex along the line.
     */
    private Pair<List<TerrainVertex>, TerrainModelCursor> bisectLine(TerrainModelCursor start, TerrainModelCursor end,
            LineType type, Vector3d modelCenter)
    {
        TerrainVertex startVert = (TerrainVertex)start.getVertex();
        TerrainVertex endVert = (TerrainVertex)end.getVertex();
        LatLonAlt startLLA = startVert.getCoordinates().getLatLonAlt();
        LatLonAlt endLLA = endVert.getCoordinates().getLatLonAlt();

        GeographicPosition midGeo;
        if (type == LineType.GREAT_CIRCLE)
        {
            GeographicPosition middleNoAlt = new GeographicPosition(
                    GeographicBody3D.greatCircleInterpolate(startLLA, endLLA, .5));
            double altM = (startLLA.getAltM() + endLLA.getAltM()) * 0.5;
            LatLonAlt middleWithAlt = LatLonAlt.createFromDegreesMeters(middleNoAlt.getLatLonAlt().getLatD(),
                    middleNoAlt.getLatLonAlt().getLonD(), altM, startLLA.getAltitudeReference());
            midGeo = new GeographicPosition(middleWithAlt);
        }
        else
        {
            midGeo = new GeographicPosition(startLLA.interpolate(endLLA, .5));
        }

        List<TerrainVertex> vertices = New.list();
        Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor> midTess = getLineModel(start, midGeo, type,
                modelCenter);
        vertices.addAll(midTess.getFirstObject());

        Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor> endTess = getLineModel(
                midTess.getSecondObject(), end, type, modelCenter);
        vertices.addAll(endTess.getFirstObject());

        return new Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor>(vertices, end);
    }

    /**
     * Clone the triangles from the original globe model.
     *
     * @param modelTriangles The original triangles from the original globe
     *            model.
     * @param clonedVertices The vertices cloned from the original globe model.
     * @return The collection of cloned triangles.
     */
    private List<TerrainTriangle> cloneTriangles(List<TerrainTriangle> modelTriangles, List<TerrainVertex> clonedVertices)
    {
        List<TerrainTriangle> trianglesCopy = New.list(modelTriangles.size());
        for (TerrainTriangle tri : modelTriangles)
        {
            if (tri.isPetrified())
            {
                trianglesCopy.add(tri);
            }
            else
            {
                TerrainTriangle snap = tri.clone();
                snap.setVertexA(clonedVertices.get(tri.getVertexA().getIndex()));
                snap.setVertexB(clonedVertices.get(tri.getVertexB().getIndex()));
                snap.setVertexC(clonedVertices.get(tri.getVertexC().getIndex()));
                snap.setGlobe(this);
                trianglesCopy.add(snap);
            }
        }
        return trianglesCopy;
    }

    /**
     * Generated a tessellated line which conforms to the terrain. This will
     * assume the line is drawn from least longitude to greatest longitude.
     *
     * @param start start of line
     * @param end end of line
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return line segments which follow the model and the cursor
     */
    private Pair<List<TerrainVertex>, TerrainModelCursor> getLineModel(GeographicPosition start, GeographicPosition end,
            LineType type, Vector3d modelCenter)
    {
        TerrainTriangle startTri = getContainingTriangle(start, null);
        Vector3d startVertex = startTri.getModelCoordinates(start, modelCenter);
        TerrainVertex startVert = new TerrainVertex(start, startVertex);
        TerrainModelCursor startCursor = new TerrainModelCursor(startVert, startTri);

        TerrainTriangle endTri = getContainingTriangle(end, startCursor.getContainingTriangle());
        Vector3d endVertex = endTri.getModelCoordinates(end, modelCenter);
        TerrainVertex endVert = new TerrainVertex(end, endVertex);
        TerrainModelCursor endCursor = new TerrainModelCursor(endVert, endTri);

        if (type == LineType.STRAIGHT_LINE_IGNORE_TERRAIN)
        {
            // Use the line model since it creates an ordered set.
            TerrainLineModel model = new TerrainLineModel(getCelestialBody(), startVert, endVert, modelCenter);
            model.add(startVert, false);
            model.add(endVert, false);
            return new Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor>(model.getVertices(), endCursor);
        }

        return getLineModel(startCursor, endCursor, type, modelCenter);
    }

    /**
     * Generated a tessellated line which conforms to the terrain. This will
     * assume the line is drawn from least longitude to greatest longitude.
     *
     * @param start start of line
     * @param end end of line
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return line segments which follow the model and the cursor for the last
     *         vertex along the line.
     */
    private Pair<List<TerrainVertex>, TerrainModelCursor> getLineModel(TerrainModelCursor start, GeographicPosition end,
            LineType type, Vector3d modelCenter)
    {
        TerrainTriangle endTri = getContainingTriangle(end, start.getContainingTriangle());
        Vector3d endVertex = endTri.getModelCoordinates(end, modelCenter);
        TerrainVertex endVert = new TerrainVertex(end, endVertex);
        TerrainModelCursor endCursor = new TerrainModelCursor(endVert, endTri);

        return getLineModel(start, endCursor, type, modelCenter);
    }

    /**
     * Generated a tessellated line which conforms to the terrain. This will
     * assume the line is drawn from least longitude to greatest longitude.
     *
     * @param start start of line
     * @param end end of line
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return line segments which follow the model and the cursor for the last
     *         vertex along the line.
     */
    private Pair<List<TerrainVertex>, TerrainModelCursor> getLineModel(TerrainModelCursor start, TerrainModelCursor end,
            LineType type, Vector3d modelCenter)
    {
        TerrainVertex startVert = (TerrainVertex)start.getVertex();
        TerrainVertex endVert = (TerrainVertex)end.getVertex();

        LatLonAlt startLLA = startVert.getCoordinates().getLatLonAlt();
        LatLonAlt endLLA = endVert.getCoordinates().getLatLonAlt();

        if (LatLonAlt.crossesAntimeridian(startLLA, endLLA))
        {
            return splitLineOnAntimeridian(start, end, type, modelCenter);
        }

        // For lines longer than 45 degrees, splitting it into multiple
        // pieces helps to insure that extraneous points are not included in
        // the line. This also increases performance because more terrain
        // triangles are excluded from the search more quickly.
        if ((Math.abs(startLLA.getLatD() - endLLA.getLatD()) > LINE_LENGTH_CHECK_THRESHOLD
                || LatLonAlt.longitudeDifference(startLLA.getLonD(), endLLA.getLonD()) > LINE_LENGTH_CHECK_THRESHOLD)
                && GeographicBody3D.greatCircleDistanceR(startLLA, endLLA) > MathUtil.HALF_PI)
        {
            return bisectLine(start, end, type, modelCenter);
        }

        TerrainLineModel model = new TerrainLineModel(getCelestialBody(), startVert, endVert, modelCenter);
        model.add(startVert, false);
        model.add(endVert, false);

        TerrainTriangle startTri = start.getContainingTriangle();
        TerrainTriangle endTri = end.getContainingTriangle();
        Vector3d startVertex = startVert.getModelCoordinates();
        Vector3d endVertex = endVert.getModelCoordinates();

        // If the start and end positions are in the same triangle or less
        // than 0.1 meters apart, just return those two points.
        final double tenth = 0.1;
        if (Utilities.sameInstance(startTri, endTri)
                || MathUtil.isZero(startVertex.subtract(endVertex).getLengthSquared(), tenth))
        {
            return new Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor>(model.getVertices(), end);
        }

        // Get the ellipse plane based on the model coordinates for the
        // ellipsoidal model.
        if (type == LineType.GREAT_CIRCLE)
        {
            // The ellipse plane should not be adjusted for the model offset
            // since the intersections will be calculated in world coordinates.
            Vector3d elStart = getCelestialBody().convertToModel(new GeographicPosition(
                    LatLonAlt.createFromDegrees(startLLA.getLatD(), startLLA.getLonD(), Altitude.ReferenceLevel.ELLIPSOID)),
                    Vector3d.ORIGIN);
            Vector3d normStart = elStart.getNormalized();
            Vector3d elEnd = getCelestialBody().convertToModel(
                    new GeographicPosition(
                            LatLonAlt.createFromDegrees(endLLA.getLatD(), endLLA.getLonD(), Altitude.ReferenceLevel.ELLIPSOID)),
                    Vector3d.ORIGIN);
            Vector3d normEnd = elEnd.getNormalized();
            Plane ellipsePlane = new Plane(Vector3d.ORIGIN, normEnd.cross(normStart));

            List<TerrainVertex> intersections = New.list();
            getNorthBottom().getIntersections(intersections, ellipsePlane, modelCenter);
            getSouthBottom().getIntersections(intersections, ellipsePlane, modelCenter);
            model.addAll(intersections, true);
        }
        else
        {
            List<TerrainVertex> intersections = New.list();

            getNorthBottom().getFlatIntersections(intersections, startLLA.asVec2d(), endLLA.asVec2d(), modelCenter);
            getSouthBottom().getFlatIntersections(intersections, startLLA.asVec2d(), endLLA.asVec2d(), modelCenter);
            model.addAll(intersections, true);
        }

        return new Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor>(model.getVertices(), end);
    }

    /**
     * Set the associated adjacent triangles and child triangles in the copied
     * triangles to match the original triangles.
     *
     * @param modelTriangles the original triangles from the globe model.
     * @param trianglesCopy The copied triangles.
     */
    private void setAdjacentsAndChildren(List<TerrainTriangle> modelTriangles, List<TerrainTriangle> trianglesCopy)
    {
        for (int i = 0; i < modelTriangles.size(); ++i)
        {
            TerrainTriangle tri = modelTriangles.get(i);
            TerrainTriangle triCopy = trianglesCopy.get(i);

            if (tri.getParent() != null)
            {
                triCopy.setParent(trianglesCopy.get(tri.getParent().getIndex()));
            }

            if (tri.getLeftChild() != null)
            {
                triCopy.setLeftChild(trianglesCopy.get(tri.getLeftChild().getIndex()));
                triCopy.setRightChild(trianglesCopy.get(tri.getRightChild().getIndex()));
            }

            if (tri.getAdjacentA() != null)
            {
                triCopy.setAdjacentA(trianglesCopy.get(tri.getAdjacentA().getIndex()));
            }
            if (tri.getAdjacentB() != null)
            {
                triCopy.setAdjacentB(trianglesCopy.get(tri.getAdjacentB().getIndex()));
            }
            if (tri.getAdjacentC() != null)
            {
                triCopy.setAdjacentC(trianglesCopy.get(tri.getAdjacentC().getIndex()));
            }
        }
    }

    /**
     * When converting a line to conform to the terrain, it is sometimes
     * necessary to split the line. This splits the line at the antimeridian and
     * conforms each section to the terrain, then combines the resulting
     * vertices into a single collection.
     *
     * @param start start of line
     * @param end end of line
     * @param type The type of line desired.
     * @param modelCenter The origin of the model coordinate space for the
     *            results.
     * @return line segments which follow the model and the cursor for the last
     *         vertex along the line.
     */
    private Pair<List<TerrainVertex>, TerrainModelCursor> splitLineOnAntimeridian(TerrainModelCursor start,
            TerrainModelCursor end, LineType type, Vector3d modelCenter)
    {
        TerrainVertex startVert = (TerrainVertex)start.getVertex();
        TerrainVertex endVert = (TerrainVertex)end.getVertex();
        LatLonAlt startLLA = startVert.getCoordinates().getLatLonAlt();
        LatLonAlt endLLA = endVert.getCoordinates().getLatLonAlt();

        double splitLat;
        double splitAlt;
        if (type == LineType.GREAT_CIRCLE)
        {
            GeographicPosition ellipsoidReferencedStart = startVert.getCoordinates().convertReference(ReferenceLevel.ELLIPSOID);
            GeographicPosition ellipsoidReferencedEnd = endVert.getCoordinates().convertReference(ReferenceLevel.ELLIPSOID);
            Vector3d startModel = getCelestialBodyModelPosition(ellipsoidReferencedStart, Vector3d.ORIGIN);
            Vector3d endModel = getCelestialBodyModelPosition(ellipsoidReferencedEnd, Vector3d.ORIGIN);

            Sphere containingSphere = new DefaultSphere(startModel.interpolate(endModel, 0.5),
                    startModel.distance(endModel) * 0.5);

            Plane lineCoplane = new Plane(Vector3d.ORIGIN, startModel, endModel);
            Plane antiMeridianCoplane = new Plane(Vector3d.ORIGIN, Vector3d.UNIT_Y);
            Ray3d intersect = lineCoplane.getIntersection(antiMeridianCoplane);

            Vector3d globePosition = getCelestialBody().getShape().getIntersection(intersect);
            Vector3d splitPosition = null;
            if (containingSphere.contains(globePosition))
            {
                splitPosition = globePosition;
            }
            else
            {
                Vector3d reflection = globePosition.multiply(-1.);
                if (containingSphere.contains(reflection))
                {
                    splitPosition = reflection;
                }
                else
                {
                    LOGGER.error("Could not split line on antimeridian.");
                    return null;
                }
            }

            LatLonAlt splitNoAlt = getCelestialBody().convertToPosition(splitPosition, ReferenceLevel.ELLIPSOID).getLatLonAlt();
            splitLat = splitNoAlt.getLatD();
            double fullArc = GeographicBody3D.greatCircleDistanceR(startLLA, endLLA);
            double meridianArc = GeographicBody3D.greatCircleDistanceR(startLLA, splitNoAlt);
            double pct = meridianArc / fullArc;
            splitAlt = (startLLA.getAltM() + endLLA.getAltM()) * pct;
        }
        else
        {
            double meridianDelta = LatLonAlt.longitudeDifference(startLLA.getLonD(), 180.);
            double lineDelta = LatLonAlt.longitudeDifference(startLLA.getLonD(), endLLA.getLonD());
            double pct = meridianDelta / lineDelta;
            LatLonAlt split = startLLA.interpolate(endLLA, pct);
            splitLat = split.getLatD();
            splitAlt = split.getAltM();
        }

        List<TerrainVertex> vertices = New.list();
        // Make sure that the longitude on the same side of the antimeridian as
        // the start.
        double lon = startLLA.getLonD() < 0 ? -180. : 180.;
        LatLonAlt splitWithAltFirst = LatLonAlt.createFromDegreesMeters(splitLat, lon, splitAlt, startLLA.getAltitudeReference());
        GeographicPosition splitEnd = new GeographicPosition(splitWithAltFirst);

        Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor> startToSplitTess = getLineModel(start, splitEnd,
                type, modelCenter);
        vertices.addAll(startToSplitTess.getFirstObject());

        // Make sure that the longitude on the same side of the antimeridian as
        // the end.
        LatLonAlt splitWithAltSecond = LatLonAlt.createFromDegreesMeters(splitLat, -lon, splitAlt,
                startLLA.getAltitudeReference());
        GeographicPosition secondGeo = new GeographicPosition(splitWithAltSecond);
        Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor> splitToEndTess = getLineModel(secondGeo,
                endVert.getCoordinates(), type, modelCenter);
        vertices.addAll(splitToEndTess.getFirstObject());

        return new Pair<List<TerrainVertex>, ImmutableTriangleGlobeModel.TerrainModelCursor>(vertices, end);
    }

    /**
     * A projection cursor for the terrain projection. In addition to providing
     * the current vertex, this cursor provides the containing triangle for the
     * vertex.
     */
    public static class TerrainModelCursor extends GeographicProjectionCursor
    {
        /** Containing triangle for the this cursor's vertex. */
        private final TerrainTriangle myContainingTriangle;

        /**
         * Constructor.
         *
         * @param vertex The cursor's current vertex.
         * @param containingTriangle Containing triangle for the this cursor's
         *            vertex.
         */
        public TerrainModelCursor(GeographicTesseraVertex vertex, TerrainTriangle containingTriangle)
        {
            super(vertex);
            myContainingTriangle = containingTriangle;
        }

        /**
         * Get the containingTriangle.
         *
         * @return the containingTriangle
         */
        public TerrainTriangle getContainingTriangle()
        {
            return myContainingTriangle;
        }
    }

    /**
     * A simple generator for creating geographic vertices based on the terrain
     * triangle in which they reside.
     */
    protected static class SimpleVertexGenerator extends SimpleGeographicVertexGenerator
    {
        /** The triangle in which the vertex resides. */
        private final TerrainTriangle myOwningTriangle;

        /**
         * Constructor.
         *
         * @param vertices The existing vertices in the tessellation.
         * @param owningTriangle The triangle in which the vertex resides.
         */
        public SimpleVertexGenerator(Map<Vector3d, GeographicTesseraVertex> vertices, TerrainTriangle owningTriangle)
        {
            super(vertices, null, null);
            myOwningTriangle = owningTriangle;
        }

        @Override
        public GeographicTesseraVertex generateVertex(Vector3d location)
        {
            LatLonAlt lla = LatLonAlt.createFromDegreesMeters(location.getY(), location.getX(), location.getZ(),
                    ReferenceLevel.TERRAIN);
            GeographicPosition geo = new GeographicPosition(lla);
            Vector3d model = myOwningTriangle.getModelCoordinates(geo, Vector3d.ORIGIN);
            return new TerrainVertex(geo, model);
        }
    }
}
