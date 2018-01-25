package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import gnu.trove.map.custom_hash.TObjectByteCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.RenderableGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.constraint.ConstraintsChangedEvent;
import io.opensphere.core.geometry.constraint.ConstraintsChangedListener;
import io.opensphere.core.geometry.constraint.MutableConstraints;
import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangeListener;
import io.opensphere.core.geometry.renderproperties.RenderPropertyChangedEvent;
import io.opensphere.core.pipeline.renderer.AbstractRenderer.ProjectionReadyListener;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;

/** Helper for event listening for AbstractProcessor. */
@SuppressWarnings("PMD.GodClass")
abstract class AbstractProcessorListenerHelper
{
    /**
     * Hash strategy for render properties so listeners are added to each
     * instance once.
     */
    private static final HashingStrategy<RenderProperties> RENDER_PROPERTY_HASH_STRATEGY = new HashingStrategy<RenderProperties>()
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        @Override
        public int computeHashCode(RenderProperties object)
        {
            return System.identityHashCode(object);
        }

        @Override
        public boolean equals(RenderProperties o1, RenderProperties o2)
        {
            return Utilities.sameInstance(o1, o2);
        }
    };

    /** Executor for processing changes to constraints. */
    private final ProcrastinatingExecutor myConstraintsChangedExecutor;

    /** Listener for changes to constraints. */
    private final ConstraintsChangedListener myConstraintsListener = new ConstraintsChangedListener()
    {
        @Override
        public void constraintsChanged(final ConstraintsChangedEvent evt)
        {
            myConstraintsChangedExecutor.execute(() -> handleConstraintsChanged(evt));
        }
    };

    /** The display interval change listener. */
    private volatile ActiveTimeSpanChangeListener myDisplayIntervalChangeListener;

    /** Listener for when the renderer becomes ready to render a projection. */
    private final ProjectionReadyListener myProjectionReadyListener = projection -> handleProjectionReady(projection);

    /** Executor for processing changes to render properties. */
    private final ProcrastinatingExecutor myRenderPropertyChangedExecutor;

    /** Listener for changes to render properties. */
    private final RenderPropertyChangeListener myRenderPropertyListener = new RenderPropertyChangeListener()
    {
        @Override
        public void propertyChanged(final RenderPropertyChangedEvent evt)
        {
            myRenderPropertyChangedExecutor.execute(() -> handlePropertyChanged(evt));
        }
    };

    /** Reference to the time manager. */
    private final TimeManager myTimeManager;

    /**
     * Constructor. This class should only be created from AbstractProcessor.
     *
     * @param timeManager The processor's time manager.
     * @param scheduledExecutorService The processor's scheduled executor
     *            service.
     * @param renderer The renderer associated with the processor this helper
     *            helps.
     */
    public AbstractProcessorListenerHelper(TimeManager timeManager, ScheduledExecutorService scheduledExecutorService,
            GeometryRenderer<?> renderer)
    {
        myConstraintsChangedExecutor = new ProcrastinatingExecutor(scheduledExecutorService, 50);
        myRenderPropertyChangedExecutor = new ProcrastinatingExecutor(scheduledExecutorService, 100);
        myTimeManager = timeManager;
        renderer.addProjectionReadyListener(myProjectionReadyListener);
    }

    /** Handle any required cleanup. */
    public void close()
    {
        removeDisplayIntervalChangeListener();
    }

    /**
     * Get if checking time constraints is needed, based on if any of the
     * current geometries are time constrained.
     *
     * @return {@code true} if checking time constraints is needed.
     */
    public boolean isCheckingTimeConstraintsNeeded()
    {
        return myDisplayIntervalChangeListener != null;
    }

    /**
     * Handle adding and removing listeners for some geometries.
     *
     * @param adds Geometries which have been added to my processor.
     * @param removes Geometries which have been removed from my processor.
     */
    public void updateListeners(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        Set<MutableConstraints> newCons;
        TObjectByteCustomHashMap<RenderProperties> newProps;
        if (adds.isEmpty())
        {
            newCons = Collections.emptySet();
            newProps = null;
        }
        else
        {
            newCons = new HashSet<>();
            newProps = new TObjectByteCustomHashMap<>(RENDER_PROPERTY_HASH_STRATEGY);
            getNewConstraintsAndProperties(adds, newCons, newProps);
        }

        Set<MutableConstraints> remCons;
        TObjectByteCustomHashMap<RenderProperties> remProps;
        if (removes.isEmpty())
        {
            remCons = Collections.emptySet();
            remProps = null;
        }
        else
        {
            remCons = new HashSet<>();
            remProps = new TObjectByteCustomHashMap<>(RENDER_PROPERTY_HASH_STRATEGY);
            getRemovedConstraintsAndProperties(removes, remCons, remProps);
        }

        removeConstraintsAndPropertyListeners(remCons,
                remProps == null ? Collections.<RenderProperties>emptySet() : remProps.keySet());

        addConstraintsAndPropertiesListeners(newCons,
                newProps == null ? Collections.<RenderProperties>emptySet() : newProps.keySet());

        if (myDisplayIntervalChangeListener == null && containsTimeConstrainedGeometry(adds))
        {
            registerDisplayIntervalChangeListener();
        }
    }

    /**
     * Get all the geometries in the processor.
     *
     * @return The geometries.
     */
    protected abstract Collection<? extends Geometry> getAllObjects();

    /**
     * Get a string description of the processor.
     *
     * @return The description.
     */
    protected abstract String getProcessorDescription();

    /**
     * Get the time manager.
     *
     * @return The time manager.
     */
    protected TimeManager getTimeManager()
    {
        return myTimeManager;
    }

    /**
     * Handle constraint changes to geometries which are already in the
     * processor.
     *
     * @param evt The event associated with the changed constraints.
     */
    protected abstract void handleConstraintsChanged(ConstraintsChangedEvent evt);

    /**
     * Handle when the renderer has reported that it is ready to render a
     * projection.
     *
     * @param projection The projection which is now ready.
     */
    protected abstract void handleProjectionReady(Projection projection);

    /**
     * Handle property changes to geometries which are already in the processor.
     *
     * @param evt The event associated with the changed properties.
     */
    protected abstract void handlePropertyChanged(RenderPropertyChangedEvent evt);

    /**
     * Handle when the active time spans are changed.
     */
    protected abstract void handleTimeSpansChanged();

    /**
     * Get if the processor is closed.
     *
     * @return If the processor is closed, {@code true}.
     */
    protected abstract boolean isClosed();

    /**
     * Register the display interval change listener with the time manager.
     */
    protected void registerDisplayIntervalChangeListener()
    {
        myDisplayIntervalChangeListener = active -> handleTimeSpansChanged();
        getTimeManager().addActiveTimeSpanChangeListener(myDisplayIntervalChangeListener);
    }

    /**
     * Unsubscribe the display interval change listener.
     */
    protected void removeDisplayIntervalChangeListener()
    {
        if (myDisplayIntervalChangeListener != null)
        {
            getTimeManager().removeActiveTimeSpanChangeListener(myDisplayIntervalChangeListener);
            myDisplayIntervalChangeListener = null;
        }
    }

    /**
     * Add listeners for some constraints and properties.
     *
     * @param constraintsColl The new constraints.
     * @param propertiesColl The new properties.
     */
    private void addConstraintsAndPropertiesListeners(Collection<? extends MutableConstraints> constraintsColl,
            Collection<? extends RenderProperties> propertiesColl)
    {
        if (!constraintsColl.isEmpty())
        {
            for (MutableConstraints cons : constraintsColl)
            {
                cons.addListener(myConstraintsListener);
            }
        }

        if (!propertiesColl.isEmpty())
        {
            for (RenderProperties props : propertiesColl)
            {
                props.addListener(myRenderPropertyListener);
            }
        }
    }

    /**
     * Determine if a collection contains a geometry with a time constraint.
     *
     * @param geometries The collection.
     * @return If there is a time-constrained geometry, {@code true}.
     */
    private boolean containsTimeConstrainedGeometry(Collection<?> geometries)
    {
        boolean needDisplayIntervalChangeListener = false;
        for (Object add : geometries)
        {
            if (add instanceof ConstrainableGeometry)
            {
                Constraints constraints = ((ConstrainableGeometry)add).getConstraints();
                if (constraints != null && constraints.getTimeConstraint() != null)
                {
                    needDisplayIntervalChangeListener = true;
                    break;
                }
            }
        }
        return needDisplayIntervalChangeListener;
    }

    /**
     * Get the sets of constraints and render properties which are used by the
     * add geometries.
     *
     * @param adds The geometries which have been added to my processor.
     * @param newCons Constraints used by the added geometries.
     * @param newProps Render properties used by the added geometries.
     */
    private void getNewConstraintsAndProperties(Collection<? extends Geometry> adds, Set<MutableConstraints> newCons,
            TObjectByteCustomHashMap<RenderProperties> newProps)
    {
        for (Geometry geom : adds)
        {
            if (geom instanceof ConstrainableGeometry)
            {
                Constraints constraints = ((ConstrainableGeometry)geom).getConstraints();
                if (constraints instanceof MutableConstraints)
                {
                    newCons.add((MutableConstraints)constraints);
                }
            }
            if (geom instanceof RenderableGeometry)
            {
                for (RenderProperties renderProperties : ((RenderableGeometry)geom).getRenderProperties()
                        .getThisPlusDescendants())
                {
                    newProps.putIfAbsent(renderProperties, (byte)0);
                }
            }
        }
    }

    /**
     * Get the sets of constraints and render properties which are used by the
     * removed geometries.
     *
     * @param removes The geometries which have been removed from my processor.
     * @param remCons Constraints used by the removed geometries.
     * @param remProps Render properties used by the removed geometries.
     */
    private void getRemovedConstraintsAndProperties(Collection<? extends Geometry> removes, Set<MutableConstraints> remCons,
            TObjectByteCustomHashMap<RenderProperties> remProps)
    {
        for (Geometry geom : removes)
        {
            if (geom instanceof ConstrainableGeometry)
            {
                Constraints constraints = ((ConstrainableGeometry)geom).getConstraints();
                if (constraints instanceof MutableConstraints)
                {
                    remCons.add((MutableConstraints)constraints);
                }
            }
            if (geom instanceof RenderableGeometry)
            {
                for (RenderProperties renderProperties : ((RenderableGeometry)geom).getRenderProperties()
                        .getThisPlusDescendants())
                {
                    remProps.putIfAbsent(renderProperties, (byte)0);
                }
            }
        }

        // if there is a geometry in the processor which uses the
        // constraints or properties, don't remove it.
        if (!remCons.isEmpty())
        {
            removeActiveConstraints(remCons);
        }
        if (!remProps.isEmpty())
        {
            removeActiveRenderProperties(remProps);
        }
    }

    /**
     * Remove constraints associated with the current processor geometries from
     * the input collection.
     *
     * @param cons The constraints to be removed from.
     */
    private void removeActiveConstraints(Set<? extends MutableConstraints> cons)
    {
        for (Geometry geom : getAllObjects())
        {
            if (geom instanceof ConstrainableGeometry)
            {
                Constraints constraints = ((ConstrainableGeometry)geom).getConstraints();
                if (constraints != null)
                {
                    cons.remove(constraints);
                }
            }
        }
    }

    /**
     * Remove render properties associated with the current processor geometries
     * from the input collection.
     *
     * @param props The render properties to be removed from.
     */
    private void removeActiveRenderProperties(TObjectByteCustomHashMap<? extends RenderProperties> props)
    {
        for (Geometry geom : getAllObjects())
        {
            if (geom instanceof RenderableGeometry)
            {
                for (RenderProperties renderProperties : ((RenderableGeometry)geom).getRenderProperties()
                        .getThisPlusDescendants())
                {
                    props.remove(renderProperties);
                }
            }
        }
    }

    /**
     * Remove listeners for some constraints and properties.
     *
     * @param constraintsColl The constraints.
     * @param propertiesColl The properties.
     */
    private void removeConstraintsAndPropertyListeners(Collection<? extends MutableConstraints> constraintsColl,
            Collection<? extends RenderProperties> propertiesColl)
    {
        if (!constraintsColl.isEmpty())
        {
            for (MutableConstraints cons : constraintsColl)
            {
                cons.removeListener(myConstraintsListener);
            }
        }

        if (!propertiesColl.isEmpty())
        {
            for (RenderProperties props : propertiesColl)
            {
                props.removeListener(myRenderPropertyListener);
            }
        }
    }
}
