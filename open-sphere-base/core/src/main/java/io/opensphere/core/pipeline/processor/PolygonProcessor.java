package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.MultiPolygonGeometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.MultiTimeConstraint;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.SimpleModelTesseraVertex;
import io.opensphere.core.model.SimpleTesseraBlockBuilder;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.model.TesseraList.TesseraBlock;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelData;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicProjectedTesseraVertex;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.Projection.SimpleProjectedTesseraVertex;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ThreadedStateMachine;
import io.opensphere.core.util.jts.PolygonTriangulationUtil;
import io.opensphere.core.util.jts.PolygonTriangulationUtil.AbstractSimpleVertexGenerator;
import io.opensphere.core.util.jts.PolygonTriangulationUtil.VertexGenerator;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.viewer.impl.PositionConverter;

/**
 * Processor for {@link PolygonGeometry}s. This class determines the model
 * coordinates of input geometries and putting them in the cache for use by the
 * renderer.
 *
 * @param <E> The type of geometry handled by this processor.
 */
@SuppressWarnings("PMD.GodClass")
public class PolygonProcessor<E extends PolygonGeometry> extends AbstractProcessor<E>
{
    /** The logger used to capture output from this class. */
    private static final Logger LOG = Logger.getLogger(PolygonProcessor.class);

    /**
     * Construct a polyline processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public PolygonProcessor(ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        super(PolygonGeometry.class, builder, renderer, 0);
    }

    /**
     * Constructor for use by derived polyline processors.
     *
     * @param geometryType The concrete type of geometry handled by this
     *            processor.
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    protected PolygonProcessor(Class<?> geometryType, ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        super(geometryType, builder, renderer);
    }

    @Override
    public void generateDryRunGeometries()
    {
        PolygonGeometry.Builder<ScreenPosition> builder = new PolygonGeometry.Builder<>();
        ColorRenderProperties fillColor = new DefaultColorRenderProperties(0, true, true, true);
        fillColor.setColor(Color.GREEN);
        PolygonRenderProperties props = new DefaultPolygonRenderProperties(0, true, true, fillColor);
        props.setColor(Color.BLUE);

        List<ScreenPosition> verts = New.list();
        for (int index = 0; index < 10; ++index)
        {
            verts.add(new ScreenPosition(index, index));
        }
        builder.setVertices(verts);
        Collection<PolygonGeometry> geoms = New.collection();
        builder.setLineSmoothing(false);
        geoms.add(new PolygonGeometry(builder, props, null));
        builder.setLineSmoothing(true);
        props.setWidth(2);
        props.setStipple(StippleModelConfig.DASH_DASH_DOT);
        geoms.add(new PolygonGeometry(builder, props, null));
        receiveObjects(this, geoms, Collections.<PolygonGeometry>emptySet());
    }

    @Override
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        if (!sensitiveToProjectionChanges())
        {
            return;
        }

        Lock writeLock = getProjectionChangeLock().writeLock();
        writeLock.lock();
        try
        {
            // Clear must come first because super's implementation may trigger
            // drawing to occur.
            if (evt.isFullClear())
            {
                getCache().clearCacheAssociations(PolygonModelData.class, Ellipsoid.class);
            }
            else
            {
                Collection<Geometry> overlapping = null;
                for (Geometry geom : getGeometries())
                {
                    for (GeographicBoundingBox bounds : evt.getBounds())
                    {
                        if (geom.overlaps(bounds, 0.))
                        {
                            overlapping = CollectionUtilities.lazyAdd(geom, overlapping);
                            break;
                        }
                    }
                }
                if (overlapping != null)
                {
                    getCache().clearCacheAssociations(overlapping, PolygonModelData.class, Ellipsoid.class);
                }
            }

            super.handleProjectionChanged(evt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    protected void cacheData(E geo, AbstractRenderer.ModelData data)
    {
        PolygonModelData mc = (PolygonModelData)data;
        getCache().putCacheAssociation(geo, mc, PolygonModelData.class, mc.getSizeBytes(), 0L);
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociations(geoms, PolygonModelData.class);
    }

    /**
     * Create the line data for the outer boundary and holes of the polygon for
     * non-geographic types.
     *
     * @param geom The geometry for which the line data is desired.
     * @param projection The projection currently in use for this processor.
     * @param modelOffset When necessary, offset the converted positions by
     *            adding this position.
     * @return The newly created line data.
     */
    protected Collection<PolylineModelData> createLineDataForNonGeo(E geom, Projection projection, Vector3d modelOffset)
    {
        Collection<PolylineModelData> lineData = New.collection();
        List<Vector3d> modelPositions = getPositionConverter().convertPositionsToModel(geom.getVertices(), projection,
                Vector3d.ORIGIN);
        lineData.add(new PolylineModelData(modelPositions, getConstraintTimeSpans(modelPositions.size(), geom)));

        for (List<? extends Position> hole : geom.getHoles())
        {
            modelPositions = getPositionConverter().convertPositionsToModel(hole, projection, Vector3d.ORIGIN);
            lineData.add(new PolylineModelData(modelPositions, getConstraintTimeSpans(modelPositions.size(), geom)));
        }
        return lineData;
    }

    @Override
    protected PolygonModelData getCachedData(E geo, AbstractRenderer.ModelData override)
    {
        return getCache().getCacheAssociation(geo, PolygonModelData.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean isOnScreen(E geom, boolean useTime)
    {
        if (!super.isOnScreen(geom, useTime))
        {
            return false;
        }

        GeographicBoundingBox bbox = null;
        if (geom.getVertices().get(0) instanceof GeographicPosition)
        {
            bbox = GeographicBoundingBox.getMinimumBoundingBox((Collection<GeographicPosition>)geom.getVertices());
        }
        return isBoundingEllipsoidOnScreen(geom, useTime, bbox);
    }

    @Override
    protected AbstractRenderer.ModelData processGeometry(E geo, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        if (projection != null && geo.getPositionType().isAssignableFrom(GeographicPosition.class))
        {
            return processGeographic(geo, projection);
        }
        else if (geo.getPositionType().isAssignableFrom(ScreenPosition.class))
        {
            return processScreen(geo, projection, null, new ScreenVertexGenerator(getPositionConverter(), Vector3d.ORIGIN));
        }
        else
        {
            return processModel(geo, projection);
        }
    }

    /**
     * Convert the vertices of a screen coordinate based geometry into model
     * coordinates for the line and/or the the fill mesh as necessary.
     *
     * @param geo The geometry.
     * @param projection The projection to be used to convert the coordinates.
     * @param modelOffset When necessary, offset the converted positions by
     *            adding this position.
     * @param vertexGenerator The generator for vertices which will be used
     *            during triangulation.
     * @return The model coordinates.
     */
    protected PolygonModelData processScreen(E geo, Projection projection, Vector3d modelOffset,
            VertexGenerator<SimpleProjectedTesseraVertex<ScreenPosition>> vertexGenerator)
    {
        boolean drawLine = geo.getRenderProperties().getWidth() > MathUtil.DBL_EPSILON;
        boolean drawFill = geo.getRenderProperties().getFillColorRenderProperties() != null;

        Collection<PolylineModelData> lineData = null;
        PolygonMeshData meshData = null;
        if (drawLine || drawFill)
        {
            if (drawLine)
            {
                lineData = createLineDataForNonGeo(geo, projection, modelOffset);
            }

            if (drawFill)
            {
                // @formatter:off
                SimpleTesseraBlockBuilder<SimpleProjectedTesseraVertex<ScreenPosition>> triBuilder =
                        new SimpleTesseraBlockBuilder<>(3, Vector3d.ORIGIN);
                // @formatter:on

                Polygon jtsPoly = JTSCoreGeometryUtilities.convertToJTSPolygon(geo);
                PolygonTriangulationUtil.triangulatePolygon(triBuilder, jtsPoly, vertexGenerator);

                if (!triBuilder.getBlockVertices().isEmpty())
                {
                    TesseraBlock<SimpleProjectedTesseraVertex<ScreenPosition>> block = new TesseraBlock<>(triBuilder, false);

                    List<? extends SimpleProjectedTesseraVertex<ScreenPosition>> blockVerts = block.getVertices();
                    List<Vector3d> modelCoords = New.list(blockVerts.size());
                    for (SimpleProjectedTesseraVertex<ScreenPosition> vertex : blockVerts)
                    {
                        modelCoords.add(vertex.getModelCoordinates());
                    }

                    meshData = new PolygonMeshData(modelCoords, null, block.getIndices(), null, null,
                            block.getTesseraVertexCount(), false);
                }
            }
        }

        return new PolygonModelData(lineData, meshData);
    }

    @Override
    protected void resetState(Collection<? extends E> objects, ThreadedStateMachine.State toState)
    {
        // Since it can take a long time to process a filled polygon, reset
        // them separately so that they can become visible one at a time.
        for (E geom : objects)
        {
            super.resetState(Collections.singleton(geom), toState);
        }
    }

    /**
     * Get a list of time spans with the given size, with all the same as the
     * time span constraint of the geometry.
     *
     * @param count The number of time spans to be in the list.
     * @param geom The geometry.
     * @return The list of time spans, or {@code null} if there is no time
     *         constraint.
     */
    private List<TimeSpan> getConstraintTimeSpans(int count, PolygonGeometry geom)
    {
        if (geom.getConstraints() != null && geom.getConstraints().getTimeConstraint() != null)
        {
            if (geom.getConstraints().getTimeConstraint() instanceof MultiTimeConstraint)
            {
                throw new UnsupportedOperationException("MultiTimeConstraints are not supported for polygon geometries.");
            }
            TimeSpan timeSpan = geom.getConstraints().getTimeConstraint().getTimeSpan();
            TimeSpan[] result = new TimeSpan[count];
            Arrays.fill(result, timeSpan);
            return Arrays.asList(result);
        }
        return null;
    }

    /**
     * Convert the vertices of a geographic geometry into model coordinates for
     * the line and/or the the fill mesh as necessary.
     *
     * @param geo The geometry.
     * @param projection The projection to be used to convert the coordinates.
     * @return The model coordinates.
     */
    private PolygonModelData processGeographic(E geo, Projection projection)
    {
        boolean drawLine = geo.getRenderProperties().getWidth() > MathUtil.DBL_EPSILON;
        boolean drawFill = geo.getRenderProperties().getFillColorRenderProperties() != null;

        Collection<PolylineModelData> lineData = null;
        List<PolygonMeshData> meshBlocks = New.list(1);
        if (drawLine || drawFill)
        {
            if (drawLine)
            {
                lineData = New.collection();
                if (geo instanceof MultiPolygonGeometry)
                {
                    for (PolygonGeometry child : ((MultiPolygonGeometry)geo).getGeometries())
                    {
                        generateLineData(child, projection, lineData);
                    }
                }
                else
                {
                    generateLineData(geo, projection, lineData);
                }
            }

            if (drawFill)
            {
                GeometryPrecisionReducer reducer = new GeometryPrecisionReducer(new PrecisionModel(100000));
                for (Polygon polygon : getPolygons(reducer.reduce(JTSCoreGeometryUtilities.convertToJTSPolygon(geo))))
                {
                    TesseraList<? extends GeographicProjectedTesseraVertex> mesh = projection.convertPolygonToModelMesh(polygon,
                            projection.getModelCenter());

                    if (mesh != null)
                    {
                        for (TesseraList.TesseraBlock<? extends GeographicProjectedTesseraVertex> block : mesh.getTesseraBlocks())
                        {
                            List<? extends GeographicProjectedTesseraVertex> vertices = block.getVertices();
                            List<Vector3d> modelCoords = New.list(vertices.size());
                            for (GeographicProjectedTesseraVertex vertex : vertices)
                            {
                                modelCoords.add(vertex.getModelCoordinates());
                            }

                            meshBlocks.add(new PolygonMeshData(modelCoords, null, block.getIndices(), null, null,
                                    block.getTesseraVertexCount(), false));
                        }
                    }
                }
            }
        }

        return new PolygonModelData(lineData, meshBlocks.isEmpty() ? null : meshBlocks.get(0));
    }

    protected List<Polygon> getPolygons(com.vividsolutions.jts.geom.Geometry geometry)
    {
        List<Polygon> returnValue;
        if (geometry instanceof MultiPolygon)
        {
            returnValue = New.list();
            int count = ((MultiPolygon)geometry).getNumGeometries();
            for (int i = 0; i < count; i++)
            {
                com.vividsolutions.jts.geom.Geometry geometryN = ((MultiPolygon)geometry).getGeometryN(i);
                returnValue.addAll(getPolygons(geometryN));
            }
        }
        else if (geometry instanceof Polygon)
        {
            returnValue = Collections.singletonList((Polygon)geometry);
        }
        else
        {
            LOG.warn("Unable to convert '" + geometry.getClass().getName() + "' to a list of polygons");
            returnValue = Collections.emptyList();
        }
        return returnValue;
    }

    /**
     * Generates line data for the geometry.
     *
     * @param geo the polygon geometry
     * @param projection the projection
     * @param lineData the line data collection to which to add
     */
    private void generateLineData(PolygonGeometry geo, Projection projection, Collection<? super PolylineModelData> lineData)
    {
        @SuppressWarnings("unchecked")
        List<? extends GeographicPosition> geoVertices = (List<? extends GeographicPosition>)geo.getVertices();

        List<Vector3d> line = getPositionConverter().convertLinesToModel(geoVertices, GeographicPosition.class, geo.getLineType(),
                null, projection.getModelCenter());
        lineData.add(new PolylineModelData(line, getConstraintTimeSpans(line.size(), geo)));

        for (List<? extends Position> hole : geo.getHoles())
        {
            @SuppressWarnings("unchecked")
            List<? extends GeographicPosition> holeVerts = (List<? extends GeographicPosition>)hole;
            line = getPositionConverter().convertLinesToModel(holeVerts, GeographicPosition.class, geo.getLineType(), null,
                    projection.getModelCenter());
            lineData.add(new PolylineModelData(line, getConstraintTimeSpans(line.size(), geo)));
        }
    }

    /**
     * Convert the vertices of a model coordinate based geometry into model
     * coordinates for the line and/or the the fill mesh as necessary.
     *
     * @param geo The geometry.
     * @param projection The projection to be used to convert the coordinates.
     * @return The model coordinates.
     */
    private PolygonModelData processModel(E geo, Projection projection)
    {
        boolean drawLine = geo.getRenderProperties().getWidth() > MathUtil.DBL_EPSILON;
        boolean drawFill = geo.getRenderProperties().getFillColorRenderProperties() != null;

        Collection<PolylineModelData> lineData = null;
        PolygonMeshData meshData = null;
        if (drawLine || drawFill)
        {
            if (drawLine)
            {
                lineData = createLineDataForNonGeo(geo, projection, null);
            }

            if (drawFill)
            {
                SimpleTesseraBlockBuilder<SimpleModelTesseraVertex> triBuilder = new SimpleTesseraBlockBuilder<>(3,
                        Vector3d.ORIGIN);

                Polygon jtsPoly = JTSCoreGeometryUtilities.convertToJTSPolygon(geo);

                // TODO we need a way to properly determine the polygon facing.
                VertexGenerator<SimpleModelTesseraVertex> vertexGenerator = new ModelVertexGenerator(
                        geo.getVertices().get(0).asVector3d(), Vector3d.ORIGIN);
                PolygonTriangulationUtil.triangulatePolygon(triBuilder, jtsPoly, vertexGenerator);

                if (!triBuilder.getBlockVertices().isEmpty())
                {
                    TesseraBlock<SimpleModelTesseraVertex> block = new TesseraBlock<>(triBuilder, false);

                    List<? extends SimpleModelTesseraVertex> blockVerts = block.getVertices();
                    List<Vector3d> modelCoords = New.list(blockVerts.size());
                    for (SimpleModelTesseraVertex vertex : blockVerts)
                    {
                        modelCoords.add(vertex.getCoordinates().asVector3d());
                    }

                    meshData = new PolygonMeshData(modelCoords, null, block.getIndices(), null, null,
                            block.getTesseraVertexCount(), false);
                }
            }
        }

        return new PolygonModelData(lineData, meshData);
    }

    /**
     * The data in model coordinates which is required in order to render a
     * polygon.
     */
    public static class PolygonModelData implements SizeProvider, ModelData
    {
        /** The model data for the bounding lines of the polygon. */
        private final Collection<PolylineModelData> myLineData;

        /**
         * The model data for the mesh which makes up the fill area of the
         * polygon.
         */
        private final PolygonMeshData myMeshData;

        /**
         * Constructor.
         *
         * @param lineData The model data for the bounding lines of the polygon.
         * @param meshData The model data for the mesh which makes up the fill
         *            area of the polygon.
         */
        public PolygonModelData(Collection<PolylineModelData> lineData, PolygonMeshData meshData)
        {
            myLineData = lineData;
            myMeshData = meshData;
        }

        /**
         * Get the lineData.
         *
         * @return the lineData
         */
        public Collection<PolylineModelData> getLineData()
        {
            return myLineData;
        }

        /**
         * Get the meshData.
         *
         * @return the meshData
         */
        public PolygonMeshData getMeshData()
        {
            return myMeshData;
        }

        @Override
        public long getSizeBytes()
        {
            long sizeBytes = MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES * 2,
                    Constants.MEMORY_BLOCK_SIZE_BYTES);
            if (myLineData != null)
            {
                sizeBytes += Utilities.sizeOfArrayListBytes(myLineData.size());
                for (PolylineModelData lineData : myLineData)
                {
                    sizeBytes += lineData.getSizeBytes();
                }
            }
            if (myMeshData != null)
            {
                sizeBytes += myMeshData.getSizeBytes();
            }
            return sizeBytes;
        }
    }

    /** A simple generator for creating model vertices. */
    protected static class ModelVertexGenerator extends AbstractSimpleVertexGenerator<SimpleModelTesseraVertex>
    {
        /** A vector in the direction of the tesserae facing. */
        private final Vector3d myTesseraeFacing;

        /**
         * Constructor.
         *
         * @param tesseraeFacing A vector in the direction of the tesserae
         *            facing.
         * @param modelCenter The origin of the model coordinate space for the
         *            results.
         */
        public ModelVertexGenerator(Vector3d tesseraeFacing, Vector3d modelCenter)
        {
            super(null, modelCenter);
            myTesseraeFacing = tesseraeFacing;
        }

        @Override
        public SimpleModelTesseraVertex generateVertex(Vector3d location)
        {
            ModelPosition position = new ModelPosition(location);
            return new SimpleModelTesseraVertex(position);
        }

        @Override
        public boolean hasCorrectFacing(SimpleModelTesseraVertex vertexA, SimpleModelTesseraVertex vertexB,
                SimpleModelTesseraVertex vertexC)
        {
            Vector3d vecA = vertexA.getCoordinates().asVector3d();
            Vector3d vecB = vertexB.getCoordinates().asVector3d();
            Vector3d vecC = vertexC.getCoordinates().asVector3d();

            Vector3d aToB = vecB.subtract(vecA);
            Vector3d bToC = vecC.subtract(vecB);

            Vector3d cross = aToB.cross(bToC);

            return myTesseraeFacing.dot(cross) > 0.;
        }
    }

    /** A simple generator for creating screen vertices. */
    protected static class ScreenVertexGenerator
            extends AbstractSimpleVertexGenerator<SimpleProjectedTesseraVertex<ScreenPosition>>
    {
        /**
         * The converter used to generate model positions for the projected
         * vertex.
         */
        private final PositionConverter myPositionConverter;

        /**
         * Constructor.
         *
         * @param positionConverter The converter used to generate model
         *            positions for the projected vertex.
         * @param modelCenter The origin of the model coordinate space for the
         *            results.
         */
        public ScreenVertexGenerator(PositionConverter positionConverter, Vector3d modelCenter)
        {
            super(null, modelCenter);
            myPositionConverter = positionConverter;
        }

        @Override
        public SimpleProjectedTesseraVertex<ScreenPosition> generateVertex(Vector3d location)
        {
            ScreenPosition position = new ScreenPosition(location.getX(), location.getY());
            Vector3d model = myPositionConverter.convertPositionToModel(position, getModelCenter());
            return new SimpleProjectedTesseraVertex<>(position, model);
        }

        @Override
        public boolean hasCorrectFacing(SimpleProjectedTesseraVertex<ScreenPosition> vertexA,
                SimpleProjectedTesseraVertex<ScreenPosition> vertexB, SimpleProjectedTesseraVertex<ScreenPosition> vertexC)
        {
            // The facing direction can be determined using the cross product,
            // but we only need the part of the calculation which yields the z
            // component.
            double x1 = vertexB.getCoordinates().getX() - vertexA.getCoordinates().getX();
            double x2 = vertexC.getCoordinates().getX() - vertexB.getCoordinates().getX();

            double y1 = vertexB.getCoordinates().getY() - vertexA.getCoordinates().getY();
            double y2 = vertexC.getCoordinates().getY() - vertexB.getCoordinates().getY();

            double zCross = x1 * y2 - y1 * x2;

            // For screen coordinates the triangles will be left handed because
            // the Y direction is reversed.
            return zCross < 0.;
        }
    }
}
