package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Collections;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.GeometryGroupGeometry;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Processor for GeometryGroupGeomtries. Process the geometries as a group and
 * render them as a single unit.
 */
public class GeometryGroupGeometryProcessor extends GeometryGroupProcessor<GeometryGroupGeometry>
{
    /** Map context for this geometry group. */
    private final MapContext<?> myMapContext;

    /**
     * Construct me.
     *
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public GeometryGroupGeometryProcessor(ProcessorBuilder builder, GeometryRenderer<GeometryGroupGeometry> renderer)
    {
        super(GeometryGroupGeometry.class, builder, renderer);
        myMapContext = builder.getMapContext();
    }

    @Override
    public RepaintListener createRepaintListener(GeometryGroupGeometry group)
    {
        return new GeometryGroupRepaintListener(group);
    }

    @Override
    public void generateDryRunGeometries()
    {
    }

    @Override
    public boolean needsRender(RenderMode mode)
    {
        return mode == RenderMode.DRAW ? !getOnscreenDrawableGeometries().isEmpty() : !getOnscreenPickableGeometries().isEmpty();
    }

    @Override
    public void render(RenderContext rc)
    {
        for (GeometryGroupGeometry group : rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW
                ? getOnscreenDrawableGeometries() : getOnscreenPickableGeometries())
        {
            ModelGeometryDistributor modelData = (ModelGeometryDistributor)getCachedData(group, null);
            if (modelData == null)
            {
                // All processed geometry groups will have a distributor for the
                // sub-geometries. If retrieval failed, then it is in the
                // process of being removed from the processor.
                continue;
            }
            GeometryDistributor distrib = modelData.getDistributor();
            distrib.renderGeometries(rc);
        }
    }

    @Override
    public boolean sensitiveToProjectionChanges()
    {
        // Geometry group doesn't care (sub geometries will handle this).
        return false;
    }

    @Override
    protected MapContext<?> getMapContextForGroup(GeometryGroupGeometry group)
    {
        return myMapContext;
    }

    @Override
    protected void processRenderSubGeometries(Collection<? extends GeometryGroupGeometry> objects,
            StateController<GeometryGroupGeometry> controller)
    {
        controller.changeState(objects, State.READY);
    }

    /**
     * Listener for repaint events from my sub processors. When a repaint is
     * requested, the sub-geometries can be checked to determine if the
     * GeometryGroupGeometry is ready to be rendered.
     */
    private class GeometryGroupRepaintListener implements RepaintListener
    {
        /** The geometry associated with this repaint listener. */
        private final GeometryGroupGeometry myGroup;

        /**
         * Constructor.
         *
         * @param group The geometry associated with this repaint listener.
         */
        public GeometryGroupRepaintListener(GeometryGroupGeometry group)
        {
            myGroup = group;
        }

        @Override
        public void repaint()
        {
            // When my group is not in the current set of geometries handled by
            // the processor, then it has been removed during a re-draw.
            synchronized (getGeometrySet())
            {
                if (getGeometrySet().contains(myGroup))
                {
                    resetState(Collections.<GeometryGroupGeometry>singleton(myGroup), GroupState.PROCESSING_STARTED);
                }
            }
        }
    }
}
