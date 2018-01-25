package io.opensphere.core.pipeline.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import io.opensphere.core.geometry.AbstractGeometryGroup;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.GeometryRegistryImpl;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ModelData;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ThreadedStateMachine;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateChangeHandler;
import io.opensphere.core.util.concurrent.ThreadedStateMachine.StateController;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;

/**
 * Process AbstractGeometryGroup and cause them to be rendered as a single unit.
 *
 * @param <E> The GeometryGroupGeomery sub-type.
 */
public abstract class GeometryGroupProcessor<E extends AbstractGeometryGroup> extends AbstractProcessor<E>
{
    /** Data retriever for geometry registries. */
    private final ExecutorService myDataRetriever;

    /** Organizers and distributors of geometries to processors. */
    private final Map<E, ModelGeometryDistributor> myDistributors = Collections
            .synchronizedMap(New.<E, ModelGeometryDistributor>weakMap());

    /** The builder for more geometry processors. */
    private final ProcessorBuilder myProcessorBuilder;

    /**
     * Keep a map of listeners so that they can be removed when the group is
     * removed.
     */
    private final Map<E, RegistryChangeListener> myRegListeners = new HashMap<>();

    /**
     * Construct the processor.
     *
     * @param geometryType Class type for the geometry.
     * @param builder The builder for the processor.
     * @param renderer The renderer for the geometries handled by this
     *            processor.
     */
    public GeometryGroupProcessor(Class<?> geometryType, ProcessorBuilder builder, GeometryRenderer<E> renderer)
    {
        super(geometryType, builder, renderer);

        myDataRetriever = builder.getDataRetriever();
        myProcessorBuilder = builder.clone();

        StateChangeHandler<E> processingHandler = new StateChangeHandler<E>()
        {
            @Override
            public void handleStateChanged(List<? extends E> objects, ThreadedStateMachine.State newState,
                    StateController<E> controller)
            {
                if (GroupState.class.isInstance(newState))
                {
                    switch (GroupState.class.cast(newState))
                    {
                        case PROCESSING_STARTED:
                            processProcessingStarted(objects, controller);
                            break;
                        case AWAITING_SUB_GEOMETRIES:
                            processAwaitingSubGeometries(objects, controller);
                            break;
                        case RENDER_SUB_GEOMETRIES:
                            processRenderSubGeometries(objects, controller);
                            break;
                        default:
                            throw new UnexpectedEnumException(GroupState.class.cast(newState));
                    }
                }
            }
        };
        EnumSet<GroupState> eventThreadStates = EnumSet.of(GroupState.AWAITING_SUB_GEOMETRIES, GroupState.PROCESSING_STARTED);
        getStateMachine().registerStateChangeHandler(eventThreadStates, processingHandler, builder.getExecutorService(),
                builder.getLoadSensitiveExecutor(), 2);

        EnumSet<GroupState> glThreadStates = EnumSet.of(GroupState.RENDER_SUB_GEOMETRIES);
        getStateMachine().registerStateChangeHandler(glThreadStates, processingHandler, builder.getGLExecutor(), null, 0);
    }

    /**
     * Create a repaint listener for a geometry group geometry.
     *
     * @param group group for which to create a repaint listener.
     * @return newly create repaint listener.
     */
    public abstract RepaintListener createRepaintListener(E group);

    @Override
    public void handleProjectionChanged(ProjectionChangedEvent evt)
    {
        // Do nothing. If the sub-processors are affected by the projection
        // change, they will also receive the event and act on their own.
    }

    @Override
    protected void cacheData(E geo, ModelData data)
    {
        if (data instanceof ModelGeometryDistributor)
        {
            ModelGeometryDistributor distrib = (ModelGeometryDistributor)data;
            myDistributors.put(geo, distrib);
        }
    }

    @Override
    protected void clearCachedData(Collection<? extends Geometry> geoms)
    {
        // Do not clear the distributors for geometry groups since each group
        // must have a distributor for the sub-geometries. Since there is no
        // mechanism for the distributor to be restored, the only case where the
        // distributor should be removed is when we are certain that the group
        // is being removed. This will happen in recieveObjects().
    }

    @Override
    protected synchronized void doReceiveObjects(Object source, Collection<? extends E> adds,
            Collection<? extends Geometry> removes)
    {
        for (Geometry geom : removes)
        {
            if (geom instanceof AbstractGeometryGroup)
            {
                AbstractGeometryGroup group = (AbstractGeometryGroup)geom;
                GeometryRegistry reg = group.getGeometryRegistry();
                if (reg != null)
                {
                    RegistryChangeListener listen = myRegListeners.remove(group);
                    reg.removeSubscriber(listen);
                    reg.removeGeometriesForSource(group);
                    ModelGeometryDistributor modelDistrib = myDistributors.remove(group);
                    if (modelDistrib != null)
                    {
                        GeometryDistributor distrib = modelDistrib.getDistributor();
                        distrib.close();
                    }
                }
            }
        }

        super.doReceiveObjects(source, adds, removes);
        setOnscreenDirty();
        determineOnscreen();
    }

    @Override
    protected ModelData getCachedData(E geom, AbstractRenderer.ModelData override)
    {
        return myDistributors.get(geom);
    }

    /**
     * Get the map context which is appropriate for the given group geometry.
     *
     * @param group The group for which the map context is desired.
     * @return The map context.
     */
    protected abstract MapContext<?> getMapContextForGroup(E group);

    @Override
    protected void handleViewChanged(Viewer view, ViewChangeType type)
    {
        // Do nothing. If the sub-processors are affected by the view
        // change, they will also receive the event and act on their own.
    }

    @Override
    protected boolean isOnScreen(E geom, boolean useTime)
    {
        // On-screen is determined by the sub-processors.
        // It is important that we return true here, otherwise the entire group
        // will not be rendered.
        return true;
    }

    @Override
    protected void processGeometries(Collection<? extends E> unprocessed, Collection<? super E> ready,
            StateController<E> controller)
    {
        for (E geo : unprocessed)
        {
            processGeometry(geo, null, null, TimeBudget.ZERO);
        }
    }

    @Override
    protected ModelData processGeometry(E group, Projection projectionShapshot, AbstractRenderer.ModelData override,
            TimeBudget timeBudget)
    {
        if (override != null)
        {
            return override;
        }

        ProcessorBuilder procBuilder = myProcessorBuilder.clone();

        MapContext<?> mapContext = getMapContextForGroup(group);
        procBuilder.setMapContext(mapContext);

        procBuilder.setRepaintListener(createRepaintListener(group));
        GeometryDistributor distrib = new GeometryDistributor(procBuilder);
        cacheData(group, new ModelGeometryDistributor(distrib));

        RegistryChangeListener listen = new RegistryChangeListener(distrib);
        myRegListeners.put(group, listen);

        GeometryRegistryImpl reg = new GeometryRegistryImpl(myDataRetriever);
        reg.addSubscriber(listen);

        group.setGeometryRegistry(reg);

        return null;
    }

    /**
     * Render all of my subGeometries. This must happen on the thread with the
     * active GL context.
     *
     * @param objects Geometries to render.
     * @param controller state controller for changing geometry state.
     */
    protected abstract void processRenderSubGeometries(Collection<? extends E> objects, StateController<E> controller);

    @Override
    protected void processUnprocessed(Collection<? extends E> unprocessed, StateController<E> controller)
    {
        super.processUnprocessed(unprocessed, controller);

        checkReady(unprocessed, controller);
    }

    /**
     * Check whether the given set of geometries are ready for rendering.
     *
     * @param geoms geometries to check
     * @param controller state controller for changing geometry state.
     */
    private void checkReady(Collection<? extends E> geoms, StateController<E> controller)
    {
        List<E> ready = new ArrayList<>();
        List<E> awaiting = new ArrayList<>();
        GEOMETRY:
        for (E group : geoms)
        {
            ModelGeometryDistributor modDistrib = myDistributors.get(group);
            if (modDistrib != null)
            {
                GeometryDistributor distrib = modDistrib.getDistributor();
                for (RenderableGeometryProcessor<? extends Geometry> processor : distrib.getRenderableGeometryProcessors())
                {
                    if (!processor.allGeometriesReady())
                    {
                        awaiting.add(group);
                        continue GEOMETRY;
                    }
                }
            }

            // All of my sub-geometries are ready, so I am ready.
            ready.add(group);
        }

        if (!ready.isEmpty())
        {
            controller.changeState(ready, GroupState.RENDER_SUB_GEOMETRIES);
        }

        if (!awaiting.isEmpty())
        {
            controller.changeState(awaiting, GroupState.AWAITING_SUB_GEOMETRIES);
        }
    }

    /**
     * Handle geometries which have been moved to the AWAITING_SUB_GEOMETRIES
     * state.
     *
     * @param objects geometries in the AWAITING_SUB_GEOMETRIES state.
     * @param controller state controller.
     */
    private void processAwaitingSubGeometries(Collection<? extends E> objects, StateController<E> controller)
    {
        checkReady(objects, controller);
    }

    /**
     * This state is used for resetting to AWAITING_SUB_GEOMETRIES, so that the
     * geometry can be checked again to see if it is ready.
     *
     * @param objects geometries in the PROCESSING_STARTED state.
     * @param controller state controller.
     */
    private void processProcessingStarted(Collection<? extends E> objects, StateController<E> controller)
    {
        controller.changeState(objects, GroupState.AWAITING_SUB_GEOMETRIES);
    }

    /**
     * Use the model data to keep an association between the distributor and the
     * geometry. This association will be used by the renderer to have the
     * distributor's processors render.
     */
    public static class ModelGeometryDistributor implements AbstractRenderer.ModelData
    {
        /**
         * Each Render to texture geometry has its own distributor with a unique
         * set of renderers.
         */
        private GeometryDistributor myDistributor;

        /**
         * Construct me.
         *
         * @param distrib distributor which goes with the associated
         *            AbstractGeometryGroup.
         */
        public ModelGeometryDistributor(GeometryDistributor distrib)
        {
            myDistributor = distrib;
        }

        /**
         * Get the distributor.
         *
         * @return the distributor
         */
        public GeometryDistributor getDistributor()
        {
            return myDistributor;
        }

        /**
         * Set the distributor.
         *
         * @param distributor the distributor to set
         */
        public void setDistributor(GeometryDistributor distributor)
        {
            myDistributor = distributor;
        }
    }

    /** The states for the render to texture state machine. */
    protected enum GroupState implements ThreadedStateMachine.State
    {
        /**
         * This state is used for resetting to AWAITING_SUB_GEOMETRIES, so that
         * the geometry can be checked again to see if it is ready.
         */
        PROCESSING_STARTED(State.DEFERRED.getStateOrder() + 10),

        /**
         * State for group geometries waiting for their sub-geometries to be
         * ready.
         */
        AWAITING_SUB_GEOMETRIES(PROCESSING_STARTED.getStateOrder() + 10),

        /**
         * State for group geometries which are ready to render their
         * sub-geometries.
         */
        RENDER_SUB_GEOMETRIES(AWAITING_SUB_GEOMETRIES.getStateOrder() + 10);

        /** The order of the state. */
        private final int myStateOrder;

        /**
         * Construct a state.
         *
         * @param order The order of the state.
         */
        GroupState(int order)
        {
            myStateOrder = order;
        }

        @Override
        public int getStateOrder()
        {
            return myStateOrder;
        }
    }

    /**
     * Listen for Changes to the geometry registries created for each geometry
     * group.
     */
    private static class RegistryChangeListener implements GenericSubscriber<Geometry>
    {
        /** The Geometry distributor for the group. */
        private final GeometryDistributor myGeometryDistributor;

        /**
         * Constructor.
         *
         * @param distrib The Geometry distributor for the group.
         */
        public RegistryChangeListener(GeometryDistributor distrib)
        {
            myGeometryDistributor = distrib;
        }

        @Override
        public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
        {
            myGeometryDistributor.updateGeometries(adds, removes);
        }
    }
}
