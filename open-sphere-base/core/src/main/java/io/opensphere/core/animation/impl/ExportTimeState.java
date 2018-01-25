package io.opensphere.core.animation.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.model.time.ISO8601TimeSpanAdapter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.ISO8601DurationAdapter;
import io.opensphere.core.util.collections.New;

/**
 * A model of a time state, used for export.
 */
@XmlRootElement(name = "time")
@XmlAccessorType(XmlAccessType.NONE)
public class ExportTimeState
{
    /** The amount to advance the current interval per step. */
    @XmlJavaTypeAdapter(value = ISO8601DurationAdapter.class)
    @XmlElement(name = "advance")
    private Duration myAdvanceDuration;

    /** The current time span. */
    @XmlJavaTypeAdapter(value = ISO8601TimeSpanAdapter.class)
    @XmlElement(name = "current")
    private TimeSpan myCurrent;

    /** The sequence of intervals in the animation. */
    @XmlElement(name = "sequence")
    private final Sequence mySequence = new Sequence();

    /**
     * Get the duration that the current time interval is advanced at each step.
     *
     * @return The advance duration.
     */
    public Duration getAdvanceDuration()
    {
        return myAdvanceDuration;
    }

    /**
     * Get the current time span.
     *
     * @return The current time.
     */
    public TimeSpan getCurrent()
    {
        return myCurrent;
    }

    /**
     * Get the sequence of intervals available to the animation.
     *
     * @return The sequence.
     */
    public List<? extends TimeSpan> getSequence()
    {
        return mySequence.getIntervals();
    }

    /**
     * Set the duration that the current time interval is advanced at each step.
     *
     * @param advanceDuration The advance duration.
     */
    public void setAdvanceDuration(Duration advanceDuration)
    {
        myAdvanceDuration = advanceDuration;
    }

    /**
     * Set the current time span.
     *
     * @param current The current time.
     */
    public void setCurrent(TimeSpan current)
    {
        myCurrent = current;
    }

    /**
     * Set the sequence of intervals available to the animation.
     *
     * @param sequence The sequence.
     */
    public void setSequence(List<? extends TimeSpan> sequence)
    {
        mySequence.setIntervals(New.unmodifiableList(sequence));
    }

    /** Nested class to encapsulate the sequence. */
    @XmlAccessorType(XmlAccessType.NONE)
    private static class Sequence
    {
        /** The sequence of intervals in the animation. */
        @XmlJavaTypeAdapter(value = ISO8601TimeSpanAdapter.class)
        @XmlElement(name = "interval")
        private List<? extends TimeSpan> myIntervals;

        /**
         * Get the intervals.
         *
         * @return The intervals.
         */
        public List<? extends TimeSpan> getIntervals()
        {
            return myIntervals;
        }

        /**
         * Set the intervals.
         *
         * @param intervals The intervals.
         */
        public void setIntervals(List<? extends TimeSpan> intervals)
        {
            myIntervals = intervals;
        }
    }
}
