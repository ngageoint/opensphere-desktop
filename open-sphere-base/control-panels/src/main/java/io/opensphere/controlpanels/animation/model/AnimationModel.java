package io.opensphere.controlpanels.animation.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.controlpanels.timeline.chart.ChartType;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.WeakObservableValue;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;

/**
 * The animation model.
 */
public class AnimationModel
{
    /** The duration of the active window (active span). */
    private final ObservableValue<Duration> myActiveSpanDuration = new WeakObservableValue<>();

    /** The advance amount between frames (step size). */
    private final ObservableValue<Duration> myAdvanceDuration = new WeakObservableValue<>();

    /** The durations that data can be loaded. */
    private final ObservableValue<Set<? extends Duration>> myAvailableDataLoadDurations = new WeakObservableValue<>();

    /** The chart type. */
    private final ObservableValue<ChartType> myChartType = new WeakObservableValue<>();

    /** The fade amount. */
    private final IntegerModel myFade = new IntegerModel(0, 100);

    /** The fade amount the user has set. */
    private final IntegerModel myFadeUser = new IntegerModel(0, 100);

    /** The frames per second. */
    private final ObservableValue<Float> myFPS = new WeakObservableValue<>();

    /** The held intervals. */
    private final ObservableList<TimeSpan> myHeldIntervals = new ObservableList<>();

    /** Map of held intervals to layers. */
    private final Map<TimeSpan, Collection<String>> myHeldIntervalToLayersMap = Collections.synchronizedMap(New.map());

    /** The view that was last shown. */
    private final ObservableValue<ViewPreference> myLastShownView = new StrongObservableValue<>();

    /** If the timeline is currently in "live" mode. */
    private final BooleanModel myLiveMode = new BooleanModel();

    /** The data load spans. */
    private final ObservableList<TimeSpan> myLoadIntervals = new ObservableList<>();

    /** The loop span (the minimum and maximum of the animation). */
    private final ObservableValue<TimeSpan> myLoopSpan = new StrongObservableValue<>();

    /** Indicates if the active span is prevented from pushing the loop span. */
    private final BooleanModel myLoopSpanLocked = new BooleanModel();

    /** The play state. */
    private final ObservableValue<PlayState> myPlayState = new WeakObservableValue<>();

    /** Whether to remember times between sessions. */
    private final BooleanModel myRememberTimes = new BooleanModel();

    /** The selected data load duration. */
    private final ObservableValue<Duration> mySelectedDataLoadDuration = new WeakObservableValue<>();

    /** The skipped intervals. */
    private final ObservableList<TimeSpan> mySkippedIntervals = new ObservableList<>();

    /** If the active span snaps to the data boundaries. */
    private final BooleanModel mySnapToDataBoundaries = new BooleanModel();

    /** If the UI span is locked so it doesn't go outside the loop span. */
    private final BooleanModel myUISpanLock = new BooleanModel();

    /** The view preference. */
    private final ChoiceModel<ViewPreference> myViewPreference = new ChoiceModel<>(ViewPreference.values());

    /** The last action the user performed. */
    private final ObservableValue<Action> myLastAction = new StrongObservableValue<>();

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     * 
     * @param toolbox The toolbox through which application state is accessed.
     */
    public AnimationModel(Toolbox toolbox)
    {
        myToolbox = toolbox;
        mySnapToDataBoundaries.set(Boolean.TRUE);

        myLiveMode.setNameAndDescription("LIVE", "Automatically keep time now within the active span.");
        myLiveMode.set(Boolean.FALSE);
        myLoopSpanLocked.set(Boolean.FALSE);

        myUISpanLock.setNameAndDescription("Restrict timeline view to loop span", "Keep the timeline view within the loop span.");
        myUISpanLock.set(Boolean.FALSE);

        myFadeUser.addListener(this::userChangedFade);

        mySkippedIntervals.addChangeListener(new ListDataListener<TimeSpan>()
        {
            @Override
            public void elementsRemoved(ListDataEvent<TimeSpan> e)
            {
                Quantify.collectMetric("mist3d.timeline.remove-skip-interval");
            }

            @Override
            public void elementsChanged(ListDataEvent<TimeSpan> e)
            {
                Quantify.collectMetric("mist3d.timeline.change-skip-interval");
            }

            @Override
            public void elementsAdded(ListDataEvent<TimeSpan> e)
            {
                Quantify.collectMetric("mist3d.timeline.add-skip-interval");
            }
        });
    }

    /**
     * Gets the duration of the active window (active span).
     *
     * @return the duration of the active window (active span)
     */
    public ObservableValue<Duration> getActiveSpanDuration()
    {
        return myActiveSpanDuration;
    }

    /**
     * Gets the advance duration property.
     *
     * @return the advance duration property.
     */
    public ObservableValue<Duration> advanceDurationProperty()
    {
        return myAdvanceDuration;
    }

    /**
     * Gets the advance duration between frames (step size).
     *
     * @return the advance amount between frames
     */
    public Duration getAdvanceDuration()
    {
        return myAdvanceDuration.get();
    }

    /**
     * Sets the advance duration between frames (step size).
     *
     * @param advanceDuration the advance duration
     */
    public void setAdvanceDuration(Duration advanceDuration)
    {
        myAdvanceDuration.set(advanceDuration);
    }

    /**
     * Gets the chart type.
     *
     * @return the chart type
     */
    public ObservableValue<ChartType> getChartType()
    {
        return myChartType;
    }

    /**
     * Get the durations that data can be loaded.
     *
     * @return The durations.
     */
    public ObservableValue<Set<? extends Duration>> getDataLoadDurations()
    {
        return myAvailableDataLoadDurations;
    }

    /**
     * Gets the fade.
     *
     * @return the fade
     */
    public IntegerModel getFade()
    {
        return myFade;
    }

    /**
     * Gets the fade the user has set.
     *
     * @return the fade the user has set.
     */
    public IntegerModel getFadeUser()
    {
        return myFadeUser;
    }

    /**
     * Gets the frames per second.
     *
     * @return the frames per second
     */
    public ObservableValue<Float> getFPS()
    {
        return myFPS;
    }

    /**
     * Gets the held intervals.
     *
     * @return the held intervals
     */
    public ObservableList<TimeSpan> getHeldIntervals()
    {
        return myHeldIntervals;
    }

    /**
     * Gets the heldIntervalToLayersMap.
     *
     * @return the heldIntervalToLayersMap
     */
    public Map<TimeSpan, Collection<String>> getHeldIntervalToLayersMap()
    {
        return myHeldIntervalToLayersMap;
    }

    /**
     * Gets the last shown view.
     *
     * @return the last shown view
     */
    public ObservableValue<ViewPreference> getLastShownView()
    {
        return myLastShownView;
    }

    /**
     * Get if the timeline is in "live" mode.
     *
     * @return If the timeline is in "live" mode.
     */
    public BooleanModel getLiveMode()
    {
        return myLiveMode;
    }

    /**
     * Gets the load intervals property.
     *
     * @return the load intervals property
     */
    public ObservableList<TimeSpan> loadIntervalsProperty()
    {
        return myLoadIntervals;
    }

    /**
     * Gets the load intervals.
     *
     * @return the load intervals
     */
    public List<TimeSpan> getLoadIntervals()
    {
        return myLoadIntervals.isEmpty() ? Collections.singletonList(myLoopSpan.get()) : new ArrayList<>(myLoadIntervals);
    }

    /**
     * Gets the loop span (the constraints on the animation).
     *
     * @return the loop span
     */
    public ObservableValue<TimeSpan> getLoopSpan()
    {
        return myLoopSpan;
    }

    /**
     * Gets the loop span lock (determines if the loop span can be changed by
     * the active span).
     *
     * @return the loop span
     */
    public BooleanModel getLoopSpanLocked()
    {
        return myLoopSpanLocked;
    }

    /**
     * Gets the last action property.
     *
     * @return the last action property
     */
    public ObservableValue<Action> lastActionProperty()
    {
        return myLastAction;
    }

    /**
     * Sets the playState.
     *
     * @param playState the playState
     */
    public final void setPlayState(PlayState playState)
    {
        myPlayState.set(playState);
    }

    /**
     * Gets the playState.
     *
     * @return the playState
     */
    public final PlayState getPlayState()
    {
        return myPlayState.get();
    }

    /**
     * Gets the playState property.
     *
     * @return the playState property
     */
    public ObservableValue<PlayState> playStateProperty()
    {
        return myPlayState;
    }

    /**
     * Gets whether to remember times.
     *
     * @return whether to remember times
     */
    public BooleanModel getRememberTimes()
    {
        return myRememberTimes;
    }

    /**
     * Get the selected data load duration.
     *
     * @return The duration.
     */
    public ObservableValue<Duration> getSelectedDataLoadDuration()
    {
        return mySelectedDataLoadDuration;
    }

    /**
     * Gets the skipped intervals.
     *
     * @return the skipped intervals
     */
    public ObservableList<TimeSpan> getSkippedIntervals()
    {
        return mySkippedIntervals;
    }

    /**
     * Get if the active span snaps to data boundaries.
     *
     * @return If the active span snaps to data boundaries.
     */
    public BooleanModel getSnapToDataBoundaries()
    {
        return mySnapToDataBoundaries;
    }

    /**
     * Get the UI span lock.
     *
     * @return The UI span lock.
     */
    public BooleanModel getUISpanLock()
    {
        return myUISpanLock;
    }

    /**
     * Gets the view preference.
     *
     * @return the view preference
     */
    public ChoiceModel<ViewPreference> getViewPreference()
    {
        return myViewPreference;
    }

    /**
     * Determines whether the model is valid.
     *
     * @return whether the model is valid
     */
    public boolean isValid()
    {
        return myFade.getValidationStatus() == ValidationStatus.VALID;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(150);
        builder.append("    Time Duration : ").append(myActiveSpanDuration.get());
        builder.append("\n    Loop Span : ").append(myLoopSpan.get());
        builder.append("\n    Advance Duration : ").append(myAdvanceDuration.get());
        builder.append("\n    FPS : ").append(myFPS.get());
        builder.append("\n    PlayState : ").append(myPlayState.get());
        builder.append("\n    Fade : ").append(myFade.get());
        return builder.toString();
    }

    /**
     * Called when the user changes the fade value and updates the real fade
     * property.
     *
     * @param observable The user changed property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    private void userChangedFade(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
    {
        myFade.set(newValue);
    }
}
