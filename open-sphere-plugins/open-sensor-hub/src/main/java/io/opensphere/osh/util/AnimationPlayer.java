package io.opensphere.osh.util;

import java.util.Collections;
import java.util.Date;

import org.apache.log4j.Logger;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.impl.AnimationPlanFactory;
import io.opensphere.core.animation.impl.DefaultContinuousAnimationPlan;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.time.TimelineUtilities;
import io.opensphere.osh.model.OSHDataTypeInfo;

/** Makes it easier to play an animation. */
public class AnimationPlayer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AnimationPlayer.class);

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     */
    public AnimationPlayer(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Sets up and plays the animation for historical video.
     *
     * @param dataType the data type
     */
    public void playHistoricalVideo(OSHDataTypeInfo dataType)
    {
        Duration activeDuration = myToolbox.getTimeManager().getPrimaryActiveTimeSpans().get(0).getDuration();
        if (activeDuration.isGreaterThan(Minutes.ONE))
        {
            activeDuration = Minutes.ONE;
        }

        TimeSpan loopSpan = dataType.getTimeExtents().getExtent();
        long loopStart = TimelineUtilities.roundDown(loopSpan.getStartInstant().minus(activeDuration).toDate(), Seconds.ONE)
                .getTimeInMillis();
        long loopEnd = TimelineUtilities.roundUp(loopSpan.getEndDate(), Seconds.ONE).getTimeInMillis();
        loopSpan = TimeSpan.get(loopStart, loopEnd);

        Duration rate = new Milliseconds(dataType.isVideo() ? 50 : 500);

        play(loopSpan, activeDuration, rate);
        dataType.setPlan(myToolbox.getAnimationManager().getCurrentPlan());
    }

    /**
     * Sets up and plays the animation for streaming features.
     *
     * @param dataType the data type
     */
    public void playStreamingFeatures(OSHDataTypeInfo dataType)
    {
        Duration activeDuration = Hours.ONE;

        Duration rate = Minutes.ONE;

        TimeInstant loopStart = TimeInstant.get(TimelineUtilities.roundDown(new Date(), rate)).minus(activeDuration.divide(2));
        TimeSpan loopSpan = TimeSpan.get(loopStart, Days.ONE);

        play(loopSpan, activeDuration, rate);
        dataType.setPlan(myToolbox.getAnimationManager().getCurrentPlan());
    }

    /**
     * Sets up and plays the animation.
     *
     * @param loopSpan the loop span
     * @param activeDuration the active duration
     * @param rate the rate
     */
    public void play(TimeSpan loopSpan, Duration activeDuration, Duration rate)
    {
        AnimationManager animationManager = myToolbox.getAnimationManager();

        animationManager.abandonPlan();

        // In order for the plan to possibly match the current plan, we need to
        // round the sequence span to the nearest days
        long sequenceStart = TimelineUtilities.roundDown(loopSpan.getStartDate(), Days.ONE).getTimeInMillis();
        long sequenceEnd = TimelineUtilities.roundUp(loopSpan.getEndDate(), Days.ONE).getTimeInMillis();
        TimeSpan sequenceSpan = TimeSpan.get(sequenceStart, sequenceEnd);

        DefaultContinuousAnimationPlan plan = new AnimationPlanFactory().createDefaultContinuousAnimationPlan(loopSpan,
                sequenceSpan, activeDuration, rate, Collections.emptyList());
        if (!plan.equals(animationManager.getCurrentPlan()))
        {
            animationManager.setPlan(plan, plan.getInitialState(), Direction.FORWARD, rate);

            // This is unfortunate, but sometimes the plan can get stopped, so
            // wait here to restart it if that happens
            ThreadUtilities.sleep(100);

            // Sometimes the rate doesn't get set in the play call above, so set
            // it again :(
            try
            {
                animationManager.setChangeRate(animationManager.getCurrentPlan(), rate);
            }
            catch (AnimationPlanModificationException e)
            {
                LOGGER.error(e, e);
            }
        }

        if (!animationManager.isPlaying())
        {
            try
            {
                animationManager.play(animationManager.getCurrentPlan(), Direction.FORWARD);
            }
            catch (AnimationPlanModificationException e)
            {
                LOGGER.error(e, e);
            }
        }
    }

    /**
     * Stops the animation if it was started by the given data type.
     *
     * @param dataType the data type
     */
    public void stop(OSHDataTypeInfo dataType)
    {
        AnimationPlan currentPlan = myToolbox.getAnimationManager().getCurrentPlan();
        if (Utilities.sameInstance(dataType.getPlan(), currentPlan))
        {
            try
            {
                myToolbox.getAnimationManager().pause(currentPlan);
            }
            catch (AnimationPlanModificationException e)
            {
                LOGGER.error(e, e);
            }
        }
    }
}
