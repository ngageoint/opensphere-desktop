package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import io.opensphere.core.geometry.GeoScreenPolygonGeometry;
import io.opensphere.core.geometry.GeoScreenPolylineGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelData;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.Projection.SimpleProjectedTesseraVertex;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * Processor for {@link GeoScreenPolygonGeometry}s. This class determines the
 * model coordinates of input geometries and putting them in the cache for use
 * by the renderer. For model coordinate calculations within this processor
 * always use the origin for the model center. Because all of the geometries
 * will eventually be rendered in screen coordinates, there will never be a need
 * to use the model center to increase accuracy.
 *
 * @param <E> The type of geometry handled by this processor.
 */
public class GeoScreenPolygonProcessor<E extends GeoScreenPolygonGeometry> extends PolygonProcessor<E>
{
    /**
     * Constructor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public GeoScreenPolygonProcessor(ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        super(GeoScreenPolylineGeometry.class, builder, renderer);
    }

    @Override
    public void generateDryRunGeometries()
    {
        GeoScreenPolygonGeometry.Builder builder = new GeoScreenPolygonGeometry.Builder();
        PolygonRenderProperties props = new DefaultPolygonRenderProperties(0, true, true);
        props.setColor(Color.BLUE);

        List<ScreenPosition> verts = New.list();
        for (int index = 0; index < 10; ++index)
        {
            verts.add(new ScreenPosition(index, index));
        }
        builder.setVertices(verts);
        builder.setAttachment(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.)));
        Collection<PolygonGeometry> geoms = New.collection();
        builder.setLineSmoothing(false);
        geoms.add(new GeoScreenPolygonGeometry(builder, props, null));
        builder.setLineSmoothing(true);
        props.setWidth(2);
        props.setStipple(StippleModelConfig.DASH_DASH_DOT);
        geoms.add(new GeoScreenPolygonGeometry(builder, props, null));
        receiveObjects(this, geoms, Collections.<GeoScreenPolylineGeometry>emptySet());
    }

    @Override
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        Lock writeLock = getProjectionChangeLock().writeLock();
        writeLock.lock();
        try
        {
            // Always clear the attachment coordinates, since it will not
            // interfere with what's currently being drawn.
            getCache().clearCacheAssociations(GeoScreenPolygonAttachmentCoordinates.class);

            super.handleProjectionChanged(evt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public boolean sensitiveToProjectionChanges()
    {
        return true;
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociations(geoms, GeoScreenPolygonAttachmentCoordinates.class);
        super.clearCachedData(geoms);
    }

    @Override
    protected Collection<PolylineModelData> createLineDataForNonGeo(E geom, Projection projection, Vector3d modelOffset)
    {
        Collection<PolylineModelData> lineData = New.collection();

        List<Vector3d> modelPositions = New.list(geom.getVertices().size());
        for (ScreenPosition position : geom.getVertices())
        {
            modelPositions.add(new Vector3d(modelOffset.getX() + position.getX(), modelOffset.getY() - position.getY(), 0.));
        }
        lineData.add(new PolylineModelData(modelPositions));

        for (List<? extends Position> hole : geom.getHoles())
        {
            modelPositions = New.list(hole.size());
            for (Position position : hole)
            {
                if (position instanceof ScreenPosition)
                {
                    ScreenPosition screen = (ScreenPosition)position;
                    modelPositions.add(new Vector3d(modelOffset.getX() + screen.getX(), modelOffset.getY() - screen.getY(), 0.));
                }
            }
            lineData.add(new PolylineModelData(modelPositions));
        }
        return lineData;
    }

    /**
     * Get the attachment point in model coordinates.
     *
     * @param geom The geometry.
     * @param projectionSnapshot If non-null this snapshot will be used when
     *            creating model data.
     * @return The model coordinates.
     */
    protected Vector3d getAttachmentModelCoordinates(E geom, Projection projectionSnapshot)
    {
        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        GeoScreenPolygonAttachmentCoordinates position = getCache().getCacheAssociation(geom,
                GeoScreenPolygonAttachmentCoordinates.class);
        if (position == null)
        {
            Vector3d model = getPositionConverter().convertPositionToModel(geom.getAttachment(), projection, Vector3d.ORIGIN);
            position = new GeoScreenPolygonAttachmentCoordinates(model);
            getCache().putCacheAssociation(geom, position, GeoScreenPolygonAttachmentCoordinates.class, Vector3d.SIZE_BYTES, 0L);
        }
        return position;
    }

    @Override
    protected void handleViewChanged(Viewer view, ViewChangeSupport.ViewChangeType type)
    {
        determineOnscreen();
        getRenderer().handleViewChanged(view, type);
        getCache().clearCacheAssociations(PolygonModelData.class, GeoScreenPolygonAttachmentCoordinates.class);
        setOnscreenDirty();
    }

    @Override
    protected boolean isCheckingTimeConstraintsNeeded()
    {
        return true;
    }

    @Override
    protected boolean isOnScreen(E geom, boolean useTime)
    {
        if (!super.isOnScreen(geom, useTime))
        {
            return false;
        }

        Vector3d position = getAttachmentModelCoordinates(geom, null);
        return getViewer().isInView(position, 0f) && !isObscured(position);
    }

    @Override
    protected ModelData processGeometry(E geo, Projection projectionSnapshot, AbstractRenderer.ModelData override,
            TimeBudget timeBudget)
    {
        if (override != null)
        {
            return override;
        }

        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        if (projection == null)
        {
            return null;
        }

        Vector3d windowOffset = getPositionConverter().convertModelToWindow(getAttachmentModelCoordinates(geo, projection),
                Vector3d.ORIGIN);
        return processScreen(geo, projection, windowOffset, new GeoScreenVertexGenerator(windowOffset));
    }

    /**
     * Subclass to make caching faster.
     */
    protected static class GeoScreenPolygonAttachmentCoordinates extends Vector3d
    {
        /**
         * Constructor.
         *
         * @param model Model coordinates of the attachment point.
         */
        public GeoScreenPolygonAttachmentCoordinates(Vector3d model)
        {
            super(model);
        }

        @Override
        public boolean equals(Object obj)
        {
            return Utilities.sameInstance(this, obj);
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(this);
        }
    }

    /** A simple generator for creating screen vertices. */
    protected static class GeoScreenVertexGenerator extends ScreenVertexGenerator
    {
        /**
         * Constructor.
         *
         * @param modelCenter The origin of the model coordinate space for the
         *            results.
         */
        public GeoScreenVertexGenerator(Vector3d modelCenter)
        {
            super(null, modelCenter);
        }

        @Override
        public SimpleProjectedTesseraVertex<ScreenPosition> generateVertex(Vector3d location)
        {
            ScreenPosition position = new ScreenPosition(location.getX(), location.getY());
            Vector3d model = new Vector3d(location.getX() + getModelCenter().getX(), getModelCenter().getY() - location.getY(),
                    0.);
            return new SimpleProjectedTesseraVertex<ScreenPosition>(position, model);
        }
    }
}
