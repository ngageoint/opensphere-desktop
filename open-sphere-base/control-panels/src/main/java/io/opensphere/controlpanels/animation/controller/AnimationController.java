package io.opensphere.controlpanels.animation.controller;

import java.awt.EventQueue;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.ControlPanelToolbox;
import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.animation.model.ViewPreference;
import io.opensphere.controlpanels.animation.view.ActiveSpanEndSnapFunction;
import io.opensphere.controlpanels.animation.view.LoopSpanEndSnapFunction;
import io.opensphere.controlpanels.event.AnimationChangeExtentRequestEvent;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.RequestedDataDurationsChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.appl.DefaultFade;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.AbstractObservableValue;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.input.DontShowDialog;
import io.opensphere.core.util.swing.input.model.PropertyChangeListenerHandle;

/**
 * The master of ceremonies for all things animation in control panels.
 */
@SuppressWarnings("PMD.GodClass")
public class AnimationController extends EventListenerService
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(AnimationController.class);

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** Controller for the animation plan. */
    private final AnimationPlanController myAnimationPlanController;

    /** The model for the active time. */
    private final TimeManagerWrapper myTimeModel;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The UI controller. */
    private final AnimationUIController myUIController;

    /** The timeline UI model. */
    private final TimelineUIModel myUIModel;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public AnimationController(Toolbox toolbox)
    {
        super(toolbox.getEventManager());
        myToolbox = toolbox;

        AnimationConfigManager animationConfigManager = addService(new AnimationConfigManager(toolbox.getPreferencesRegistry()));
        myAnimationModel = animationConfigManager.getAnimationModel();

        myUIModel = addService(new TimelineUIModel());
        toolbox.getPluginToolboxRegistry().getPluginToolbox(ControlPanelToolbox.class).setUISpan(myUIModel.getUISpan());

        myAnimationPlanController = addService(
                new AnimationPlanController(toolbox.getAnimationManager(), myAnimationModel, toolbox.getTimeManager()));

        myTimeModel = addService(
                new TimeManagerWrapper(toolbox.getTimeManager(), myAnimationPlanController, myAnimationModel, myUIModel));

        addService(new LoadTimeAdapter(toolbox.getTimeManager(), myAnimationModel));

        myUIController = addService(new AnimationUIController(toolbox, myAnimationModel, myTimeModel, myUIModel));

        addService(new LiveModeController(myAnimationModel, myTimeModel, myUIModel));

        addService(new GeometryNotificationController(toolbox));

        bindAnimationModelListeners();

        bindAuxListeners();
    }

    @Override
    public void close()
    {
        assert EventQueue.isDispatchThread();
        super.close();
    }

    /**
     * Fits the UI span around the loop span.
     */
    public void fitUIToLoopSpan()
    {
        myUIController.fitUIToLoopSpan();
    }

    /**
     * Gets the live mode.
     *
     * @return True if live mode.
     */
    public Boolean isLive()
    {
        return myAnimationModel.getLiveMode().get();
    }

    @Override
    public void open()
    {
        assert EventQueue.isDispatchThread();
        initializeStateBeforeListeners();
        super.open();
        initializeStateAfterListeners();
    }

    /**
     * Sets the live mode.
     *
     * @param live True if live mode.
     */
    public void setLive(final boolean live)
    {
        EventQueueUtilities.runOnEDT(() -> myAnimationModel.getLiveMode().set(Boolean.valueOf(live)));
    }

    /**
     * Shows the given view.
     *
     * @param view the view
     */
    public void showView(ViewPreference view)
    {
        myUIController.showView(view);
    }

    /**
     * Binds animation models to their listeners.
     */
    private void bindAnimationModelListeners()
    {
        bindModel(myAnimationModel.playStateProperty(), (obs, old, newValue) -> handlePlayStateChange());
        bindModel(myAnimationModel.getActiveSpanDuration(), (obs, old, newValue) -> handleActiveSpanDurationChange());
        bindModel(myAnimationModel.getLoopSpan(), (obs, old, newValue) -> handleLoopSpanChange());
        bindModel(myAnimationModel.getFPS(), (obs, old, newValue) -> setChangeRate());
        bindModel(myAnimationModel.getFade(), (obs, old, newValue) -> handleFadeChange());
        bindModel(myAnimationModel.getSelectedDataLoadDuration(), (obs, old, newValue) -> handleSelectedDataLoadDurationChange());
        bindModel(myAnimationModel.advanceDurationProperty(), (obs, old, newValue) -> handleAdvanceDurationChange());
        bindModel(myAnimationModel.getSnapToDataBoundaries(), (obs, old, newValue) -> handleSnapToDataBoundariesChange(old, newValue));
        addService(new PropertyChangeListenerHandle(myAnimationModel.getSnapToDataBoundaries(), event -> updateSnapToNameAndDescription()));

        bindModel(myAnimationModel.getHeldIntervals(), new ListDataListener<TimeSpan>()
        {
            @Override
            public void elementsAdded(ListDataEvent<TimeSpan> e)
            {
                updateSecondaryTimeSpans();
            }

            @Override
            public void elementsChanged(ListDataEvent<TimeSpan> e)
            {
                updateSecondaryTimeSpans();
            }

            @Override
            public void elementsRemoved(ListDataEvent<TimeSpan> e)
            {
                updateSecondaryTimeSpans();
            }
        });

        bindModel(myAnimationModel.getUISpanLock(), (obs, old, newValue) -> setUISpanConstraint());
    }

    /**
     * Binds listeners which are not for animation model changes.
     */
    private void bindAuxListeners()
    {
        bindEvent(AnimationChangeExtentRequestEvent.class, this::handleAnimationChangeExtentRequestEvent);

        addService(new Service()
        {
            /** Listener for changes to the requested data durations. */
            private final RequestedDataDurationsChangeListener myListener = durations -> EventQueueUtilities
                    .invokeLater(() -> myAnimationModel.getDataLoadDurations().set(durations));

            @Override
            public void close()
            {
                myToolbox.getTimeManager().removeRequestedDataDurationsChangeListener(myListener);
            }

            @Override
            public void open()
            {
                myToolbox.getTimeManager().addRequestedDataDurationsChangeListener(myListener);
            }
        });
    }

    /**
     * Creates an animation plan, synchronizing the animation model if
     * necessary.
     */
    private void createPlan()
    {
        assert EventQueue.isDispatchThread();
        if (myAnimationModel.getSnapToDataBoundaries().get().booleanValue()
                && myAnimationModel.getSelectedDataLoadDuration().get() != null)
        {
            snapToDataBoundary();
        }
        myAnimationPlanController.createPlan();
    }

    /**
     * Handles a change in the active span duration.
     */
    private void handleActiveSpanDurationChange()
    {
        /* If snap is on, don't change the advance here because snap might get
         * shut off because the data duration hasn't been changed yet. */
        if (!myAnimationModel.getSnapToDataBoundaries().get().booleanValue())
        {
            myAnimationModel.setAdvanceDuration(myAnimationModel.getActiveSpanDuration().get());
            setFade();
        }
    }

    /**
     * Handles a change in the advance duration.
     */
    private void handleAdvanceDurationChange()
    {
        /* Turn off snap if the advance duration has been changed so it doesn't
         * match the data load duration. */
        if (myAnimationModel.getSnapToDataBoundaries().get().booleanValue()
                && !myAnimationModel.getAdvanceDuration().equals(myAnimationModel.getSelectedDataLoadDuration().get()))
        {
            myAnimationModel.getSnapToDataBoundaries().set(Boolean.FALSE);
        }
        myAnimationPlanController.createPlanAsync();
    }

    /**
     * Handles a AnimationChangeExtentRequestEvent.
     *
     * @param event the event
     */
    private void handleAnimationChangeExtentRequestEvent(final AnimationChangeExtentRequestEvent event)
    {
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                LoopSpanEndSnapFunction func = new LoopSpanEndSnapFunction(myAnimationModel, null);
                TimeInstant loopStart = func.getSnapDestination(event.getExtent().getStartInstant(), RoundingMode.FLOOR);
                TimeInstant loopEnd = func.getSnapDestination(event.getExtent().getEndInstant(), RoundingMode.CEILING);
                myAnimationModel.getLoopSpan().set(TimeSpan.get(loopStart, loopEnd));
                myUIController.fitUIToLoopSpan();
            }
        });
    }

    /** Handle the fade model changed. */
    private void handleFadeChange()
    {
        if (myAnimationModel.getFade().get().intValue() > 0)
        {
            myAnimationModel.getSnapToDataBoundaries().set(Boolean.FALSE);
        }
        EventQueueUtilities.invokeLater(this::setFade);
    }

    /** Update the loop span to match changes to the animation model. */
    private void handleLoopSpanChange()
    {
        assert EventQueue.isDispatchThread();

        if (myAnimationModel.getUISpanLock().get().booleanValue())
        {
            myAnimationModel.getUISpanLock().set(Boolean.TRUE, true);
        }

        TimeSpan activeSpan = myToolbox.getTimeManager().getPrimaryActiveTimeSpans().getExtent();
        TimeSpan loopSpan = myAnimationModel.getLoopSpan().get();

        // Keep the active time within the loop span
        if (!loopSpan.contains(activeSpan))
        {
            TimeSpan newActiveSpan;
            if (activeSpan.getDurationMs() <= loopSpan.getDurationMs())
            {
                if (loopSpan.getStart() > activeSpan.getStart())
                {
                    newActiveSpan = TimeSpan.get(loopSpan.getStart(), activeSpan.getDuration());
                }
                else
                {
                    newActiveSpan = TimeSpan.get(activeSpan.getDuration(), loopSpan.getEnd());
                }
            }
            else
            {
                newActiveSpan = loopSpan;
            }

            myTimeModel.set(newActiveSpan);
        }

        myAnimationPlanController.createPlanAsync();
    }

    /**
     * Handles the play state being changed.
     */
    private void handlePlayStateChange()
    {
        assert EventQueue.isDispatchThread();
        PlayState playState = myAnimationModel.getPlayState();
        AnimationManager animationManager = myToolbox.getAnimationManager();
        try
        {
            switch (playState)
            {
                case FORWARD:
                    myAnimationPlanController.conformLoopSpanToActiveSpan(myTimeModel.get(), AnimationState.Direction.FORWARD);
                    animationManager.play(myAnimationPlanController.getPlan(myTimeModel.get()), AnimationState.Direction.FORWARD);
                    break;
                case BACKWARD:
                    myAnimationPlanController.conformLoopSpanToActiveSpan(myTimeModel.get(), AnimationState.Direction.BACKWARD);
                    animationManager.play(myAnimationPlanController.getPlan(myTimeModel.get()),
                            AnimationState.Direction.BACKWARD);
                    break;
                case STOP:
                    animationManager.pause(myAnimationPlanController.getPlan(myTimeModel.get()));
                    break;
                case STEP_FIRST:
                    animationManager.stepFirst(myAnimationPlanController.getPlan(myTimeModel.get()), false);
                    break;
                case STEP_LAST:
                    animationManager.stepLast(myAnimationPlanController.getPlan(myTimeModel.get()), false);
                    break;
                case STEP_FORWARD:
                    myAnimationPlanController.conformLoopSpanToActiveSpan(myTimeModel.get(), AnimationState.Direction.FORWARD);
                    animationManager.stepForward(myAnimationPlanController.getPlan(myTimeModel.get()), false);
                    break;
                case STEP_BACKWARD:
                    myAnimationPlanController.conformLoopSpanToActiveSpan(myTimeModel.get(), AnimationState.Direction.BACKWARD);
                    animationManager.stepBackward(myAnimationPlanController.getPlan(myTimeModel.get()), false);
                    break;
                default:
                    throw new UnexpectedEnumException(playState);
            }
        }
        catch (AnimationPlanModificationException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Handle selected data load duration changed.
     */
    private void handleSelectedDataLoadDurationChange()
    {
        boolean enabled = myAnimationModel.getSelectedDataLoadDuration().get() != null;
        myAnimationModel.getSnapToDataBoundaries().setEnabled(enabled);
        if (enabled && myAnimationModel.getSnapToDataBoundaries().get().booleanValue())
        {
            /* This may be caused by a change to the active time, and it's not
             * allowed to change the active time within the time listener, so do
             * it later. */
            EventQueueUtilities.invokeLater(() -> myAnimationModel.getSnapToDataBoundaries().set(Boolean.TRUE, true));
        }
    }

    /**
     * Handles a change in snap to data boundaries.
     *
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void handleSnapToDataBoundariesChange(Boolean oldValue, Boolean newValue)
    {
        updateSnapToNameAndDescription();
        if (newValue.booleanValue())
        {
            snapToDataBoundary();
        }
        else if (oldValue.booleanValue())
        {
            myUIModel.getTemporaryMessage().set("Snapping disabled");
            EventQueueUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    myAnimationModel.getFade().set(myAnimationModel.getFadeUser().get());
                    DontShowDialog.showMessageDialog(myToolbox.getPreferencesRegistry(),
                            myToolbox.getUIRegistry().getMainFrameProvider().get(),
                            "Active time will no longer snap to data boundaries. Displayed data may not match the active time.",
                            "Snapping Disabled", true);
                }
            });
        }
    }

    /**
     * Initializes the state after listeners are added.
     */
    private void initializeStateAfterListeners()
    {
        myAnimationModel.getDataLoadDurations().set(myToolbox.getTimeManager().getRequestedDataDurations());
        ((AbstractObservableValue<Duration>)myAnimationModel.getSelectedDataLoadDuration()).fireChangeEvent();
    }

    /**
     * Initializes the state before listeners are added. This manipulates mostly
     * the time and animation managers, but also the animation model.
     */
    private void initializeStateBeforeListeners()
    {
        createPlan();
        setChangeRate();
        setFade();
        updateSecondaryTimeSpans();
    }

    /**
     * Sets the change rate in the animation manager from the animation model.
     */
    private void setChangeRate()
    {
        try
        {
            myToolbox.getAnimationManager().setChangeRate(myToolbox.getAnimationManager().getCurrentPlan(),
                    new Milliseconds(Math.round(1000 / myAnimationModel.getFPS().get().floatValue())));
        }
        catch (AnimationPlanModificationException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * Sets the fade in the time manager from the animation model.
     */
    private void setFade()
    {
        Duration activeDuration = myAnimationModel.getActiveSpanDuration().get();
        Duration fadeOut = activeDuration.multiply((double)myAnimationModel.getFade().get().intValue() / 100);
        myToolbox.getTimeManager().setFade(new DefaultFade(Seconds.ZERO, fadeOut));
    }

    /**
     * Updates the secondary time spans in the time manager from the animation
     * model.
     */
    private void updateSecondaryTimeSpans()
    {
        // Invert the animation model data so it can be put into time manager
        Map<Object, Collection<TimeSpan>> layerSpansMap = New.map();
        for (TimeSpan span : myAnimationModel.getHeldIntervals())
        {
            Collection<String> layers = myAnimationModel.getHeldIntervalToLayersMap().get(span);
            if (layers == null)
            {
                layerSpansMap.computeIfAbsent(TimeManager.WILDCARD_CONSTRAINT_KEY, k -> New.list()).add(span);
            }
            else
            {
                for (String layer : layers)
                {
                    layerSpansMap.computeIfAbsent(layer, k -> New.list()).add(span);
                }
            }
        }

        // Update the time manager model with the current state
        for (Map.Entry<Object, Collection<TimeSpan>> entry : layerSpansMap.entrySet())
        {
            Object layer = entry.getKey();
            Collection<TimeSpan> layerSpans = entry.getValue();
            myToolbox.getTimeManager().setSecondaryActiveTimeSpans(layer, layerSpans);
        }

        // Remove layers as needed
        Set<Object> existingLayers = myToolbox.getTimeManager().getSecondaryActiveTimeSpans().keySet();
        Collection<Object> layersToRemove = CollectionUtilities.difference(existingLayers, layerSpansMap.keySet());
        for (Object layer : layersToRemove)
        {
            myToolbox.getTimeManager().setSecondaryActiveTimeSpans(layer, Collections.emptyList());
        }
    }

    /**
     * Set the constraint on the UI span based on the current loop span.
     */
    private void setUISpanConstraint()
    {
        TimeSpan constraint;
        if (myAnimationModel.getUISpanLock().get().booleanValue())
        {
            TimeSpan loopSpan = myAnimationModel.getLoopSpan().get();
            Duration buffer = new Milliseconds(myUIModel.getMillisPerPixel().get().intValue() * 10);
            constraint = TimeSpan.get(loopSpan.getStartInstant().minus(buffer), loopSpan.getEndInstant().plus(buffer));
        }
        else
        {
            constraint = null;
        }
        myUIModel.getUISpanConstraint().set(constraint);
    }

    /**
     * Snap the loop span according to the current settings in the animation
     * model.
     */
    private void snapLoopSpan()
    {
        assert EventQueue.isDispatchThread();
        TimeSpan loop = myAnimationModel.getLoopSpan().get();

        LoopSpanEndSnapFunction func = new LoopSpanEndSnapFunction(myAnimationModel, null);
        TimeInstant loopStart = func.getSnapDestination(loop.getStartInstant(), RoundingMode.FLOOR);
        TimeInstant loopEnd = func.getSnapDestination(loop.getEndInstant(), RoundingMode.CEILING);

        myAnimationModel.getLoopSpan().set(TimeSpan.get(loopStart, loopEnd));
    }

    /**
     * Synchronize the animation model so that the active duration and the
     * advance duration match the data load duration.
     */
    private void snapToDataBoundary()
    {
        assert EventQueue.isDispatchThread();
        snapLoopSpan();

        myToolbox.getTimeManager().setFade(new DefaultFade(Seconds.ZERO, Seconds.ZERO));

        Duration dur = myAnimationModel.getSelectedDataLoadDuration().get();

        myAnimationModel.setAdvanceDuration(dur);

        TimeSpan activeSpan = myTimeModel.get();

        TimeSpan snapped = TimeSpan.get(dur, new ActiveSpanEndSnapFunction(myAnimationModel, null)
                .getSnapDestination(activeSpan.getEndInstant(), RoundingMode.CEILING));
        myTimeModel.set(snapped);
    }

    /** Update the name and description of the snap-to button. */
    private void updateSnapToNameAndDescription()
    {
        Quantify.collectMetric("mist3d.timeline.buttons.snap-to-data-boundaries");
        myAnimationModel.getSnapToDataBoundaries().setNameAndDescription("snap",
                myAnimationModel.getSnapToDataBoundaries().isEnabled()
                        ? myAnimationModel.getSnapToDataBoundaries().get().booleanValue() ? "Stop snapping to data boundaries"
                                : "Snap to data boundaries"
                        : "<html><b>Disabled</b><p> because there are no data boundaries</html>");
    }
}
