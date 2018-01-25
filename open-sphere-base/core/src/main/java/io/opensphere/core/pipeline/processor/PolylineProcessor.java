package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.constraint.MultiTimeConstraint;
import io.opensphere.core.geometry.constraint.TimeConstraint;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.math.Ellipsoid;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.model.Tessera.TesseraVertex;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.AbstractGeographicProjection.GeographicTesseraVertex;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.Projection.ProjectionCursor;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * Processor for {@link PolylineGeometry}s. This class determines the model
 * coordinates of input geometries and putting them in the cache for use by the
 * renderer.
 *
 * @param <E> The type of geometry handled by this processor.
 */
@SuppressWarnings("PMD.GodClass")
public class PolylineProcessor<E extends PolylineGeometry> extends AbstractProcessor<E>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PolylineProcessor.class);

    /**
     * Construct a polyline processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public PolylineProcessor(ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        super(PolylineGeometry.class, builder, renderer);
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
    protected PolylineProcessor(Class<?> geometryType, ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        super(geometryType, builder, renderer);
    }

    @Override
    public void generateDryRunGeometries()
    {
        PolylineGeometry.Builder<ScreenPosition> builder = new PolylineGeometry.Builder<ScreenPosition>();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(0, true, true);
        props.setColor(Color.BLUE);

        List<ScreenPosition> verts = New.list();
        for (int index = 0; index < 10; ++index)
        {
            verts.add(new ScreenPosition(index, index));
        }
        builder.setVertices(verts);
        Collection<PolylineGeometry> geoms = New.collection();
        builder.setLineSmoothing(false);
        geoms.add(new PolylineGeometry(builder, props, null));
        builder.setLineSmoothing(true);
        props.setWidth(2);
        props.setStipple(StippleModelConfig.DASH_DASH_DOT);
        geoms.add(new PolylineGeometry(builder, props, null));
        receiveObjects(this, geoms, Collections.<PolylineGeometry>emptySet());
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
                getCache().clearCacheAssociations(PolylineModelData.class, Ellipsoid.class);
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
                    getCache().clearCacheAssociations(overlapping, PolylineModelData.class, Ellipsoid.class);
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
    protected void cacheData(PolylineGeometry geo, AbstractRenderer.ModelData data)
    {
        PolylineModelData mc = (PolylineModelData)data;
        getCache().putCacheAssociation(geo, mc, PolylineModelData.class, mc.getSizeBytes(), 0L);
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociation(geoms, PolylineModelData.class);
    }

    @Override
    protected PolylineModelData getCachedData(PolylineGeometry geo, AbstractRenderer.ModelData override)
    {
        return getCache().getCacheAssociation(geo, PolylineModelData.class);
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
        // Do not attempt to process lines which cannot be drawn.
        if (geo.getRenderProperties().getWidth() <= 0f)
        {
            return null;
        }

        List<? extends Position> vertices = geo.getVertices();
        TimeConstraint timeConstraint = geo.getConstraints() == null ? null : geo.getConstraints().getTimeConstraint();
        final List<TimeSpan> timeSpans = timeConstraint == null ? null : New.<TimeSpan>list();

        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        List<Vector3d> modelList;
        if (vertices.get(0) instanceof GeographicPosition)
        {
            modelList = processGeographic(geo, timeSpans, projection);
        }
        else
        {
            modelList = processNonGeographic(geo, timeSpans, projection);
        }

        if (timeSpans != null && timeConstraint != null && !(timeConstraint instanceof MultiTimeConstraint))
        {
            for (int index = 0; index < modelList.size(); ++index)
            {
                timeSpans.add(timeConstraint.getTimeSpan());
            }
        }

        return new PolylineModelData(modelList, timeSpans);
    }

    /**
     * Add time spans for some model vertices that were generated for a segment
     * of the polyline.
     *
     * @param timeConstraint The geometry time constraint.
     * @param geometryVertexIndex The index of the geometry vertex at the end of
     *            the current segment.
     * @param lineSegmentSize The number of tesserae in the segment.
     * @param lineSegmentIndices Which tesserae were added.
     * @param timeSpans The output time spans.
     */
    private void addTimeSpansForMultiTimeConstraint(TimeConstraint timeConstraint, int geometryVertexIndex,
            final int lineSegmentSize, TIntArrayList lineSegmentIndices, final List<TimeSpan> timeSpans)
    {
        List<? extends TimeConstraint> childTimeConstraints = ((MultiTimeConstraint)timeConstraint).getChildren();
        final TimeSpan ts1;
        final TimeSpan ts2;
        if (childTimeConstraints.size() >= geometryVertexIndex)
        {
            TimeConstraint tc1 = childTimeConstraints.get(geometryVertexIndex - 1);
            ts1 = tc1 == null ? TimeSpan.TIMELESS : tc1.getTimeSpan();
            TimeConstraint tc2 = childTimeConstraints.get(geometryVertexIndex);
            ts2 = tc2 == null ? TimeSpan.TIMELESS : tc2.getTimeSpan();
        }
        else
        {
            LOGGER.warn(MultiTimeConstraint.class.getSimpleName() + " has too few child constraints ("
                    + childTimeConstraints.size() + " < " + geometryVertexIndex + ")");
            ts1 = TimeSpan.TIMELESS;
            ts2 = TimeSpan.TIMELESS;
        }
        lineSegmentIndices.forEach(new TIntProcedure()
        {
            @Override
            public boolean execute(int index)
            {
                timeSpans.add(ts1.interpolate(ts2, (double)index / (lineSegmentSize - 1)));
                return true;
            }
        });
    }

    /**
     * Convert the vertices of a geographic geometry into model coordinates.
     *
     * @param geo The geometry.
     * @param timeSpans The output list of time spans, one for each set of model
     *            coordinates, but only if the geometry's time constraint is a
     *            {@link MultiTimeConstraint}.
     * @param projection The projection to be used to convert the coordinates.
     * @return The model coordinates.
     */
    private List<Vector3d> processGeographic(PolylineGeometry geo, final List<TimeSpan> timeSpans, Projection projection)
    {
        List<Vector3d> modelList = New.list();

        TimeConstraint timeConstraint = geo.getConstraints() == null ? null : geo.getConstraints().getTimeConstraint();

        @SuppressWarnings("unchecked")
        List<? extends GeographicPosition> geoVertices = (List<? extends GeographicPosition>)geo.getVertices();

        // TODO This is mostly duplicated code from
        // AbstractGeographicProjection.convertLinesToModel() with added
        // handling for the time constraints. Can these be re-factored?
        Pair<Tessera<GeographicPosition>, ProjectionCursor> tessera = null;
        Vector3d previousModelPosition = null;
        int limit = geoVertices.size() + (geo instanceof PolygonGeometry ? 1 : 0);
        for (int i = 1; i < limit; ++i)
        {
            int lineEndIndex = i % geoVertices.size();
            GeographicPosition lineEnd = geoVertices.get(lineEndIndex);
            if (tessera == null)
            {
                tessera = getPositionConverter().convertLineToModel(geoVertices.get(0), lineEnd, geo.getLineType(), projection,
                        projection.getModelCenter());
            }
            else
            {
                tessera = getPositionConverter().convertLineToModel(tessera.getSecondObject(), lineEnd, geo.getLineType(),
                        projection, projection.getModelCenter());
            }
            if (tessera.getFirstObject() != null)
            {
                List<? extends TesseraVertex<GeographicPosition>> modelVertices = tessera.getFirstObject().getTesseraVertices();
                final int modelVertexCount = modelVertices.size();
                if (modelVertexCount > 0)
                {
                    TIntArrayList indicesAdded = new TIntArrayList(modelVertexCount);
                    for (int index = i == 1 ? 0 : 1; index < modelVertexCount; index++)
                    {
                        Vector3d modelPos = ((GeographicTesseraVertex)modelVertices.get(index)).getModelCoordinates();
                        if (!modelPos.equals(previousModelPosition))
                        {
                            modelList.add(modelPos);
                            previousModelPosition = modelPos;
                            indicesAdded.add(index);
                        }
                    }
                    if (timeSpans != null && timeConstraint instanceof MultiTimeConstraint)
                    {
                        addTimeSpansForMultiTimeConstraint(timeConstraint, lineEndIndex, modelVertexCount, indicesAdded,
                                timeSpans);
                    }
                }
            }
        }
        return modelList;
    }

    /**
     * Convert some non-geographic vertices into model coordinates.
     *
     * @param geo The geometry.
     * @param timeSpans The output list of time spans, one for each set of model
     *            coordinates, but only if the time constraint is a
     *            {@link MultiTimeConstraint}.
     * @param projectionSnapshot The projection to be used to convert the
     *            coordinates.
     * @return The model coordinates.
     */
    private List<Vector3d> processNonGeographic(PolylineGeometry geo, List<TimeSpan> timeSpans, Projection projectionSnapshot)
    {
        List<? extends Position> vertices = geo.getVertices();
        TimeConstraint timeConstraint = geo.getConstraints() == null ? null : geo.getConstraints().getTimeConstraint();
        List<Vector3d> modelList = New.list(vertices.size());
        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        Vector3d modelCenter = projection == null ? Vector3d.ORIGIN : projection.getModelCenter();
        for (int index = 0; index < vertices.size(); ++index)
        {
            Position pos = vertices.get(index);
            modelList.add(getPositionConverter().convertPositionToModel(pos, projection, modelCenter));
            if (timeSpans != null && timeConstraint instanceof MultiTimeConstraint)
            {
                List<? extends TimeConstraint> childTimeConstraints = ((MultiTimeConstraint)timeConstraint).getChildren();
                TimeSpan ts;
                if (childTimeConstraints.size() > index)
                {
                    TimeConstraint tc = childTimeConstraints.get(index);
                    ts = tc == null ? TimeSpan.TIMELESS : tc.getTimeSpan();
                }
                else
                {
                    LOGGER.warn(MultiTimeConstraint.class.getSimpleName() + " has too few child constraints ("
                            + childTimeConstraints.size() + " < " + (index - 1) + ")");
                    ts = TimeSpan.TIMELESS;
                }
                timeSpans.add(ts);
            }
        }
        return modelList;
    }
}
