package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import io.opensphere.core.geometry.GeoScreenPolylineGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.Vector3f;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelData;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;

/**
 * Processor for {@link GeoScreenPolylineGeometry}s. This class determines the
 * model coordinates of input geometries and putting them in the cache for use
 * by the renderer. For model coordinate calculations within this processor
 * always use the origin for the model center. Because all of the geometries
 * will eventually be rendered in screen coordinates, there will never be a need
 * to use the model center to increase accuracy.
 *
 * @param <E> The type of geometry handled by this processor.
 */
public class GeoScreenPolylineProcessor<E extends GeoScreenPolylineGeometry> extends PolylineProcessor<E>
{
    /**
     * Constructor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public GeoScreenPolylineProcessor(ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        super(GeoScreenPolylineGeometry.class, builder, renderer);
    }

    @Override
    public void generateDryRunGeometries()
    {
        GeoScreenPolylineGeometry.Builder builder = new GeoScreenPolylineGeometry.Builder();
        PolylineRenderProperties props = new DefaultPolylineRenderProperties(0, true, true);
        props.setColor(Color.BLUE);

        List<ScreenPosition> verts = New.list();
        for (int index = 0; index < 10; ++index)
        {
            verts.add(new ScreenPosition(index, index));
        }
        builder.setVertices(verts);
        builder.setAttachment(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.)));
        Collection<GeoScreenPolylineGeometry> geoms = New.collection();
        builder.setLineSmoothing(false);
        geoms.add(new GeoScreenPolylineGeometry(builder, props, null));
        builder.setLineSmoothing(true);
        props.setWidth(2);
        props.setStipple(StippleModelConfig.DASH_DASH_DOT);
        geoms.add(new GeoScreenPolylineGeometry(builder, props, null));
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
            getCache().clearCacheAssociations(GeoScreenPolylineAttachmentCoordinates.class);

            // Clear must come first because super's implementation may trigger
            // drawing to occur.
            getCache().clearCacheAssociations(WindowCoordinates.class);

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
    protected void cacheData(E geo, ModelData data)
    {
        WindowCoordinates mc = (WindowCoordinates)data;
        getCache().putCacheAssociation(geo, mc, WindowCoordinates.class, mc.getSizeBytes(), 0L);
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociations(geoms, WindowCoordinates.class, GeoScreenPolylineAttachmentCoordinates.class);
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
        GeoScreenPolylineAttachmentCoordinates position = getCache().getCacheAssociation(geom,
                GeoScreenPolylineAttachmentCoordinates.class);
        if (position == null)
        {
            Vector3d model = getPositionConverter().convertPositionToModel(geom.getAttachment(), projection, Vector3d.ORIGIN);
            position = new GeoScreenPolylineAttachmentCoordinates(model);
            getCache().putCacheAssociation(geom, position, GeoScreenPolylineAttachmentCoordinates.class, Vector3d.SIZE_BYTES, 0L);
        }
        return position;
    }

    @Override
    protected PolylineModelData getCachedData(E geo, AbstractRenderer.ModelData override)
    {
        return getCache().getCacheAssociation(geo, GeoScreenPolylineProcessor.WindowCoordinates.class);
    }

    @Override
    protected void handleViewChanged(Viewer view, ViewChangeSupport.ViewChangeType type)
    {
        determineOnscreen();
        getRenderer().handleViewChanged(view, type);
        getCache().clearCacheAssociations(GeoScreenPolylineProcessor.WindowCoordinates.class,
                GeoScreenPolylineAttachmentCoordinates.class);
        setOnscreenDirty();
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
        List<? extends ScreenPosition> vertices = geo.getVertices();

        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        List<Vector3d> modelList = New.list(vertices.size());
        Vector3d windowOffset = getPositionConverter().convertModelToWindow(getAttachmentModelCoordinates(geo, projection),
                Vector3d.ORIGIN);
        for (ScreenPosition position : vertices)
        {
            modelList.add(new Vector3d(windowOffset.getX() + position.getX(), windowOffset.getY() - position.getY(), 0.));
        }

        return new GeoScreenPolylineProcessor.WindowCoordinates(modelList);
    }

    /**
     * Subclass to make caching faster.
     */
    public static class WindowCoordinates extends PolylineModelData
    {
        /**
         * Constructor.
         *
         * @param modelPositions The model positions which represent the line.
         */
        public WindowCoordinates(List<Vector3d> modelPositions)
        {
            super(modelPositions);
        }

        /**
         * Constructor.
         *
         * @param modelPositions The model positions which represent the line.
         */
        public WindowCoordinates(Vector3f[] modelPositions)
        {
            super(modelPositions);
        }
    }

    /**
     * Subclass to make caching faster.
     */
    protected static class GeoScreenPolylineAttachmentCoordinates extends Vector3d
    {
        /**
         * Constructor.
         *
         * @param model Model coordinates of the attachment point.
         */
        public GeoScreenPolylineAttachmentCoordinates(Vector3d model)
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
}
