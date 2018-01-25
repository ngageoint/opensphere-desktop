package io.opensphere.core.pipeline.processor;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PointSetGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointSizeRenderProperty;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.DelegatingRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.SizeProvider;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;

/**
 * Processor for {@link PointGeometry}s. This class determines the model
 * coordinates of input geometries and putting them in the cache for use by the
 * renderer.
 */
public class PointSetProcessor extends AbstractProcessor<PointSetGeometry>
{
    /**
     * Construct a point processor.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public PointSetProcessor(ProcessorBuilder builder, GeometryRenderer<PointSetGeometry> renderer)
    {
        super(PointSetGeometry.class, builder, renderer, 0);
    }

    @Override
    public void generateDryRunGeometries()
    {
        List<GeographicPosition> positions = New.list();
        List<Color> colors = New.list();

        for (int index = 0; index < 100; ++index)
        {
            positions.add(new GeographicPosition(LatLonAlt.createFromDegrees(index, index, Altitude.ReferenceLevel.ELLIPSOID)));
            colors.add(new Color(index, index, index, index));
        }

        PointSetGeometry.Builder<GeographicPosition> builder = new PointSetGeometry.Builder<GeographicPosition>();
        builder.setPositions(positions);
        builder.setColors(colors);

        PointRenderProperties renderProperties = new DefaultPointRenderProperties(0, true, false, false);

        Collection<PointSetGeometry> geoms = New.collection();

        renderProperties.setSize(2);
        geoms.add(new PointSetGeometry(builder, renderProperties, null));
        renderProperties.setSize(4);
        geoms.add(new PointSetGeometry(builder, renderProperties, null));

        receiveObjects(this, geoms, Collections.<PointSetGeometry>emptySet());
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
            getCache().clearCacheAssociations(PointSetProcessor.ModelCoordinates.class);
            super.handleProjectionChanged(evt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    // TODO for point sets which are geographic, but whose altitude reference
    // is ELLIPSOID, we do not want them to be sensitive to projection changes.
    // In fact this may be problematic in general, some more thought should be
    // put into this.
    @Override
    public boolean sensitiveToProjectionChanges()
    {
        return false;
    }

    @Override
    protected void cacheData(PointSetGeometry geo, AbstractRenderer.ModelData data)
    {
        getCache().putCacheAssociation(geo, (PointSetProcessor.ModelCoordinates)data, PointSetProcessor.ModelCoordinates.class,
                ((PointSetProcessor.ModelCoordinates)data).getSizeBytes(), 0L);
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        getCache().clearCacheAssociations(geoms, PointSetProcessor.ModelCoordinates.class);
    }

    @Override
    protected ModelCoordinates getCachedData(PointSetGeometry geo, AbstractRenderer.ModelData override)
    {
        return getCache().getCacheAssociation(geo, PointSetProcessor.ModelCoordinates.class);
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
            GeometryRenderer<PointSetGeometry> renderer = getRenderer();
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

    @Override
    protected AbstractRenderer.ModelData processGeometry(PointSetGeometry geo, Projection projectionSnapshot,
            AbstractRenderer.ModelData override, TimeBudget timeBudget)
    {
        if (override != null)
        {
            return override;
        }
        Projection projection = projectionSnapshot == null ? getProjectionSnapshot() : projectionSnapshot;
        Vector3d modelCenter = projection == null ? Vector3d.ORIGIN : projection.getModelCenter();
        List<? extends Position> positions = geo.getPositions();
        float[] coords = new float[positions.size() * 3];
        for (int index = 0; index < positions.size(); ++index)
        {
            Vector3d model = getPositionConverter().convertPositionToModel(positions.get(index), projection, modelCenter);
            coords[index * 3] = (float)model.getX();
            coords[index * 3 + 1] = (float)model.getY();
            coords[index * 3 + 2] = (float)model.getZ();
        }

        return new PointSetProcessor.ModelCoordinates(coords);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden to split geometries into separate batches if they have a lot
     * of points in a set.
     */
    @Override
    protected void resetState(Collection<? extends PointSetGeometry> objects,
            io.opensphere.core.util.concurrent.ThreadedStateMachine.State toState)
    {
        int count = 0;
        Collection<PointSetGeometry> batch = New.collection();
        for (PointSetGeometry geom : objects)
        {
            count += geom.getPositions().size();
            batch.add(geom);
            if (count > 10000)
            {
                super.resetState(batch, toState);
                count = 0;
                batch.clear();
            }
        }
        if (!batch.isEmpty())
        {
            super.resetState(batch, toState);
        }
    }

    @Override
    protected boolean sensitiveToViewChanges()
    {
        return false;
    }

    /**
     * Wrapper class to make sure we get the right stuff out of the cache.
     */
    public static class ModelCoordinates implements AbstractRenderer.ModelData, SizeProvider
    {
        /** The interleaved coordinates. */
        private final float[] myCoordinates;

        /**
         * Constructor.
         *
         * @param interleavedCoordinates The interleaved coordinates in XYZ
         *            blocks.
         */
        protected ModelCoordinates(float[] interleavedCoordinates)
        {
            myCoordinates = interleavedCoordinates;
        }

        /**
         * Get the coordinates as a read-only float buffer.
         *
         * @return The interleaved coordinates.
         */
        public FloatBuffer getCoordinates()
        {
            return FloatBuffer.wrap(myCoordinates);
        }

        @Override
        public long getSizeBytes()
        {
            return MathUtil.roundUpTo(Constants.OBJECT_SIZE_BYTES + Constants.REFERENCE_SIZE_BYTES,
                    Constants.MEMORY_BLOCK_SIZE_BYTES)
                    + MathUtil.roundUpTo(Constants.ARRAY_SIZE_BYTES + Constants.FLOAT_SIZE_BYTES * myCoordinates.length,
                            Constants.MEMORY_BLOCK_SIZE_BYTES);
        }
    }
}
