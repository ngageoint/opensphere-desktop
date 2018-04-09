package io.opensphere.core.animation.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.model.time.ISO8601TimeSpanAdapter;
import io.opensphere.core.model.time.TimeSpan;

/**
 * A model of an animation state, used for export.
 */
@XmlRootElement(name = "animation")
@XmlAccessorType(XmlAccessType.NONE)
public class ExportAnimationState
{
    /**
     * The behavior when the current interval reaches the end of the loop
     * interval.
     */
    @XmlElement(name = "loopBehavior")
    private LoopBehavior myLoopBehavior;

    /** The loop interval. */
    @XmlJavaTypeAdapter(value = ISO8601TimeSpanAdapter.class)
    @XmlElement(name = "loop")
    private TimeSpan myLoopInterval;

    /** The time between frame updates. */
    @XmlElement(name = "millisPerFrame")
    private int myMillisPerFrame;

    /** Indicates if the animation is currently playing. */
    @XmlElement(name = "playState")
    private PlayState myPlayState;

    /**
     * Get the behavior when the animation reaches the end of the loop.
     *
     * @return The loop behavior.
     */
    public LoopBehavior getLoopBehavior()
    {
        return myLoopBehavior;
    }

    /**
     * Get the interval that defines where the animation loop begins and ends.
     *
     * @return The animation loop interval.
     */
    public TimeSpan getLoopInterval()
    {
        return myLoopInterval;
    }

    /**
     * Get the time between frame updates.
     *
     * @return The milliseconds per frame.
     */
    public int getMillisPerFrame()
    {
        return myMillisPerFrame;
    }

    /**
     * Get if the animation is playing.
     *
     * @return The play state.
     */
    public PlayState getPlayState()
    {
        return myPlayState;
    }

    /**
     * Set the behavior when the animation reaches the end of the loop.
     *
     * @param loopBehavior The loop behavior.
     */
    public void setLoopBehavior(LoopBehavior loopBehavior)
    {
        myLoopBehavior = loopBehavior;
    }

    /**
     * Set the interval that defines where the animation loop begins and ends.
     *
     * @param loopInterval The animation loop interval.
     */
    public void setLoopInterval(TimeSpan loopInterval)
    {
        myLoopInterval = loopInterval;
    }

    /**
     * Set the time between frame updates.
     *
     * @param millisPerFrame The milliseconds per frame.
     */
    public void setMillisPerFrame(int millisPerFrame)
    {
        myMillisPerFrame = millisPerFrame;
    }

    /**
     * Set if the animation is playing.
     *
     * @param playState The play state.
     */
    public void setPlayState(PlayState playState)
    {
        myPlayState = playState;
    }

    /**
     * Description of how the animation will behave at the end of the loop.
     */
    @XmlEnum
    public enum LoopBehavior
    {
        /**
         * Advance the current interval so that the end of the current interval
         * goes beyond the end of the loop interval, until the start of the
         * current interval equals the end of the loop interval, then snap the
         * current interval such that the start of the current interval is equal
         * to the start of the loop interval.
         */
        @XmlEnumValue("taperEndSnapStart")
        TAPER_END_SNAP_START,

        /**
         * Advance the current interval so that the end of the current interval
         * goes beyond the end of the loop interval, until the start of the
         * current interval equals the end of the loop interval, then set the
         * current interval end to the start of the loop interval and advance
         * from there.
         */
        @XmlEnumValue("taperEndTaperStart")
        TAPER_END_TAPER_START,

        ;
    }

    /**
     * Description of if and how the animation is playing.
     */
    @XmlEnum
    public enum PlayState
    {
        /** Animation is playing forward. */
        @XmlEnumValue("Forward")
        FORWARD,

        /** Animation is playing in reverse. */
        @XmlEnumValue("Reverse")
        REVERSE,

        /** Animation is stopped. */
        @XmlEnumValue("Stop")
        STOP,

        ;
    }
}
