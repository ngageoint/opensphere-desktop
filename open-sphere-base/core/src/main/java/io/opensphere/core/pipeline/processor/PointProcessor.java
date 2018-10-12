package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultBaseAltitudeRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRoundnessRenderProperty;
import io.opensphere.core.geometry.renderproperties.DefaultPointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.Vector3f;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.DelegatingRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Processor for {@link PointGeometry}s. This class determines the model
 * coordinates of input geometries and putting them in the cache for use by the
 * renderer.
 */
public class PointProcessor extends AbstractProcessor<PointGeometry>
{
    /** Local caching for model coordinates of geometries I handle. */
    private final Map<PointGeometry, ModelCoordinates> myModelData = New.map();

    /** Lock to control access to the model data. */
    private final Lock myModelDataLock = new ReentrantLock();

    /**
     * Construct a point processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public PointProcessor(ProcessorBuilder builder, GeometryRenderer<PointGeometry> renderer)
    {
        super(PointGeometry.class, builder, renderer);
    }

    @Override
    public void generateDryRunGeometries()
    {
        PointGeometry.Builder<GeographicPosition> builder = new PointGeometry.Builder<>();
        builder.setPosition(new GeographicPosition(LatLonAlt.createFromDegrees(0., 0.)));

        PointRenderProperties renderProperties = new DefaultPointRenderProperties(
                new DefaultBaseAltitudeRenderProperties(0, true, true, false), new DefaultPointSizeRenderProperty(),
                new DefaultPointRoundnessRenderProperty());

        Collection<PointGeometry> geoms = New.collection();
        geoms.add(new PointGeometry(builder, renderProperties, null));
        renderProperties.getRoundnessRenderProperty().setRound(true);
        renderProperties.setSize(3);
        geoms.add(new PointGeometry(builder, renderProperties, null));

        receiveObjects(this, geoms, Collections.<PointGeometry>emptySet());
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
            myModelDataLock.lock();
            try
            {
                myModelData.clear();
            }
            finally
            {
                myModelDataLock.unlock();
            }
            super.handleProjectionChanged(evt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    protected void cacheData(PointGeometry geo, AbstractRenderer.ModelData data)
    {
        myModelDataLock.lock();
        try
        {
            myModelData.put(geo, (PointProcessor.ModelCoordinates)data);
        }
        finally
        {
            myModelDataLock.unlock();
        }
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        myModelDataLock.lock();
        try
        {
            for (Geometry geom : geoms)
            {
                myModelData.remove(geom);
            }
        }
        finally
        {
            myModelDataLock.unlock();
        }
    }

    @Override
    protected void doReceiveObjects(Object source, Collection<? extends PointGeometry> adds,
            Collection<? extends Geometry> removes)
    {
        clearCachedData(removes);
        super.doReceiveObjects(source, adds, removes);
    }

    @Override
    protected ModelCoordinates getCachedData(PointGeometry geo, AbstractRenderer.ModelData override)
    {
        myModelDataLock.lock();
        try
        {
            return myModelData.get(geo);
        }
        finally
        {
            myModelDataLock.unlock();
        }
    }

    @Override
    protected void handlePropertyChanged(RenderPropertyChangedEvent evt)
    {
        // Because the point size is stored in the display list, the display
        // list must be rebuilt. The regular check for whether the display
        // list needs to be rebuilt has no ability to check for render property
        // changes.
        if (evt.getRenderProperties() instanceof PointSizeRenderProperty)
        {
            GeometryRenderer<PointGeometry> renderer = getRenderer();
            if (getRenderer() instanceof DelegatingRenderer)
            {
                ((DelegatingRenderer<?>)renderer).setDirty();
            }
            getRepaintListener().repaint();
        }
        else
        {
            super.handlePropertyChanged(evt);
        }
    }

    //    @Override
    //    protected boolean isOnScreen(PointGeometry geom, boolean useTime)
    //    {
    //        if (!super.isOnScreen(geom, useTime))
    //        {
    //            return false;
    //        }

    // TODO: This is too slow when there are lots of dots.
    //        Position position = geom.getPosition();
    //        if (position instanceof GeographicPosition)
    //        {
    //            PointProcessor.ModelCoordinates modelData = (PointProcessor.ModelCoordinates)getModelData(geom);
    //            Vector3d model = modelData.getVector();
    //            return getViewer().isInView(model, 0f) && !isObscured(model);
    //        }
    //        else
    //        {
    //            return true;
    //        }
    //        return true;
    //    }

    @Override
    protected AbstractRenderer.ModelData processGeometry(PointGeometry geo, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        if (override != null)
        {
            return override;
        }
        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        Vector3d modelCenter = projection == null ? Vector3d.ORIGIN : projection.getModelCenter();
        Vector3d model = getPositionConverter().convertPositionToModel(geo.getPosition(), projection, modelCenter);
        PointProcessor.ModelCoordinates modelData = new PointProcessor.ModelCoordinates(model);
        return modelData;
    }

    @Override
    protected boolean sensitiveToViewChanges()
    {
        return false;
    }

    /**
     * Wrapper class to make sure we get the right stuff out of the cache.
     */
    public static class ModelCoordinates extends Vector3f implements AbstractRenderer.ModelData, SizeProvider
    {
        /**
         * Constructor.
         *
         * @param vec The vector.
         */
        public ModelCoordinates(Vector3d vec)
        {
            super((float)vec.getX(), (float)vec.getY(), (float)vec.getZ());
        }

        @Override
        public boolean equals(Object obj)
        {
            return Utilities.sameInstance(this, obj);
        }

        @Override
        public long getSizeBytes()
        {
            return Vector3f.SIZE_BYTES;
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(this);
        }
    }
}
