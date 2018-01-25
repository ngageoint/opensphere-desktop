package io.opensphere.controlpanels.animation.config.v1;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.animation.model.ViewPreference;
import io.opensphere.controlpanels.timeline.chart.ChartType;
import io.opensphere.core.model.time.ISO8601TimeSpanAdapter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.units.duration.JAXBDuration;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * The animation configuration.
 */
@XmlRootElement(name = "AnimationConfig")
@XmlAccessorType(XmlAccessType.NONE)
public class AnimationConfig
{
    /** zero. */
    private static final Duration ZERO_DUR = Duration.create(ChronoUnit.SECONDS, 0);

    /** alternative to zero. */
    private static final Duration DEFAULT_DUR = Duration.create(ChronoUnit.MILLIS, 10);

    /** The advance duration. */
    @XmlElement(name = "advance")
    private JAXBDuration myAdvanceDuration;

    /** The chart type. */
    @XmlElement(name = "chartType")
    private ChartType myChartType;

    /** The fade amount. */
    @XmlElement(name = "fade")
    private int myFade;

    /** The fade amount. */
    @XmlElement(name = "fadeUser")
    private int myFadeUser;

    /** The frames per second. */
    @XmlElement(name = "fps")
    private float myFPS;

    /** The held intervals. */
    @XmlElement(name = "heldInterval")
    @XmlJavaTypeAdapter(ISO8601TimeSpanAdapter.class)
    private List<TimeSpan> myHeldIntervals = New.list();

    /** Map of held intervals to layers. */
    @XmlElement(name = "heldIntervalLayerMap")
    @XmlJavaTypeAdapter(HeldLayersAdapter.class)
    private Map<TimeSpan, Collection<String>> myHeldIntervalToLayersMap = New.map();

    /** The view that was last shown. */
    @XmlElement(name = "lastShownView")
    private ViewPreference myLastShownView;

    /** The load intervals. */
    @XmlElement(name = "loadInterval")
    @XmlJavaTypeAdapter(ISO8601TimeSpanAdapter.class)
    private List<TimeSpan> myLoadIntervals = New.list();

    /** The loop span. */
    @XmlElement(name = "loopSpan")
    @XmlJavaTypeAdapter(ISO8601TimeSpanAdapter.class)
    private TimeSpan myLoopSpan;

    /** Indicates if the loop span is locked. */
    @XmlElement(name = "loopSpanLocked")
    private boolean myLoopSpanLocked = true;

    /** Whether to remember times between sessions. */
    @XmlElement(name = "rememberTimes")
    private boolean myRememberTimes;

    /** The skipped intervals. */
    @XmlElement(name = "skippedInterval")
    @XmlJavaTypeAdapter(ISO8601TimeSpanAdapter.class)
    private List<TimeSpan> mySkippedIntervals = New.list();

    /** The snap-to. */
    @XmlElement(name = "snapToDataBoundaries")
    private boolean mySnapToDataBoundaries = true;

    /** The time duration. */
    @XmlElement(name = "timeDuration")
    private JAXBDuration myTimeDuration;

    /** The view preference. */
    @XmlElement(name = "viewPreference")
    private ViewPreference myViewPreference;

    /**
     * Gets the time span from the end of today going back the given duration.
     *
     * @param loopDuration the loop duration
     * @param activeDuration the active duration
     * @return the time span
     */
    private static TimeSpan getRecentSpan(Duration loopDuration, Duration activeDuration)
    {
        Duration roundingDuration = activeDuration.isLessThan(Days.ONE)
                ? new DurationUnitsProvider().getLargestIntegerUnitType(activeDuration) : Days.ONE;
        Calendar endTime = TimelineUtilities.roundUp(new Date(), roundingDuration);
        return TimeSpan.get(loopDuration, endTime.getTimeInMillis());
    }

    /**
     * Constructor for JAXB.
     */
    public AnimationConfig()
    {
    }

    /**
     * Constructor.
     *
     * @param animationModel the animation model
     */
    public AnimationConfig(AnimationModel animationModel)
    {
        myTimeDuration = new JAXBDuration(animationModel.getActiveSpanDuration().get());
        myLoopSpan = animationModel.getLoopSpan().get();
        myLoopSpanLocked = animationModel.getLoopSpanLocked().get().booleanValue();
        myAdvanceDuration = new JAXBDuration(animationModel.getAdvanceDuration());
        myFPS = animationModel.getFPS().get().floatValue();
        myRememberTimes = animationModel.getRememberTimes().get().booleanValue();
        myFade = animationModel.getFade().get().intValue();
        myFadeUser = animationModel.getFadeUser().get().intValue();
        myChartType = animationModel.getChartType().get();
        myHeldIntervals = animationModel.getHeldIntervals();
        myHeldIntervalToLayersMap = animationModel.getHeldIntervalToLayersMap();
        mySkippedIntervals = animationModel.getSkippedIntervals();
        mySnapToDataBoundaries = animationModel.getSnapToDataBoundaries().get().booleanValue();
        myViewPreference = animationModel.getViewPreference().get();
        myLastShownView = animationModel.getLastShownView().get();
        myLoadIntervals = animationModel.loadIntervalsProperty();
    }

    /**
     * Gets the animation model.
     *
     * @return the animation model
     */
    public AnimationModel getAnimationModel()
    {
        AnimationModel model = new AnimationModel();
        model.getActiveSpanDuration().set(myTimeDuration == null || myTimeDuration.getWrappedObject().signum() == 0 ? Days.ONE
                : myTimeDuration.getWrappedObject());

        setAdvanceDuration(model);
        model.getFPS().set(Float.valueOf(myFPS == 0 ? 1 : myFPS));
        model.setPlayState(PlayState.STOP);
        model.getRememberTimes().set(Boolean.valueOf(myRememberTimes));

        EventQueueUtilities.runOnEDTAndWait(() ->
        {
            model.getFadeUser().set(Integer.valueOf(myFadeUser));
        });
        model.getFade().set(Integer.valueOf(myFade));

        model.getChartType().set(myChartType == null ? ChartType.LINE_OVERLAPPING : myChartType);
        model.getHeldIntervals().addAll(myHeldIntervals);
        for (Map.Entry<TimeSpan, Collection<String>> entry : myHeldIntervalToLayersMap.entrySet())
        {
            model.getHeldIntervalToLayersMap().put(entry.getKey(), New.list(entry.getValue()));
        }
        model.getSkippedIntervals().addAll(mySkippedIntervals);
        model.getSnapToDataBoundaries().set(Boolean.valueOf(mySnapToDataBoundaries));
        model.getViewPreference().set(myViewPreference == null ? ViewPreference.LAST_SHOWN : myViewPreference);
        model.getLastShownView().set(myLastShownView == null ? ViewPreference.TIME_BROWSER : myLastShownView);
        model.getLoopSpanLocked().set(Boolean.valueOf(myLoopSpanLocked));

        if (model.getRememberTimes().get().booleanValue())
        {
            model.loadIntervalsProperty().addAll(myLoadIntervals);
        }

        setLoopSpan(model);

        return model;
    }

    /**
     * Sets the advance duration.
     *
     * @param model The model to set the advance duration.
     */
    private void setAdvanceDuration(AnimationModel model)
    {
        // in case the advance duration was recorded as zero (or negative) in
        // the XML, supply a positive default
        Duration advDur = null;
        if (myAdvanceDuration != null)
        {
            advDur = myAdvanceDuration.getWrappedObject();
        }
        else
        {
            advDur = model.getActiveSpanDuration().get();
        }
        if (advDur.compareTo(ZERO_DUR) <= 0)
        {
            advDur = DEFAULT_DUR;
        }
        model.setAdvanceDuration(advDur);
    }

    /**
     * Set the loop span on the animation model.
     *
     * @param model The animation model.
     */
    private void setLoopSpan(AnimationModel model)
    {
        if (model.getRememberTimes().get().booleanValue() && myLoopSpan != null)
        {
            model.getLoopSpan().set(myLoopSpan);
        }
        else
        {
            Duration loopDuration = myLoopSpan != null ? myLoopSpan.getDuration() : new Days(7);
            model.getLoopSpan().set(getRecentSpan(loopDuration, model.getActiveSpanDuration().get()));
        }
    }
}
