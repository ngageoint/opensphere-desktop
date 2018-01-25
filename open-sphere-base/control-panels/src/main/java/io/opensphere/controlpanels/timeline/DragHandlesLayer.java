package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import javax.swing.SwingConstants;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ChainFunction;
import io.opensphere.core.util.ObservableValue;

/**
 * A time span drag handles timeline layer.
 */
public class DragHandlesLayer extends CompositeLayer
{
    /** The observable time span. */
    private final ObservableTimeSpan myObservableTimeSpan;

    /** Minimum duration allowed. */
    private static final Duration MIN_DUR = Duration.create(ChronoUnit.MILLIS, BigDecimal.TEN);

    /**
     * Create a drag handle.
     *
     * @param side The side of the drag handle. {@link SwingConstants#LEFT} or
     *            {@link SwingConstants#RIGHT}
     * @param observableTimeSpan the observable time span
     * @param name The name of the layer.
     * @param constraint The constraint on where the handle can be dragged.
     * @param snapFunction The snap function.
     * @param color The color for the handle.
     * @param hoverColor The hover color for the handle.
     * @return The handle.
     */
    private static DragHandle createDragHandle(int side, ObservableTimeSpan observableTimeSpan, String name,
            final Function<? super TimeInstant, ? extends TimeInstant> constraint, SnapFunction snapFunction, Color color,
            Color hoverColor)
    {
        Function<? super TimeInstant, ? extends TimeInstant> handleConstraint = new ChainFunction<TimeInstant>(constraint,
                side == SwingConstants.LEFT ? new LeftConstraint(observableTimeSpan.getEnd())
                        : new RightConstraint(observableTimeSpan.getStart()));
        return new DragHandle(side, side == SwingConstants.LEFT ? observableTimeSpan.getStart() : observableTimeSpan.getEnd(),
                name, handleConstraint, snapFunction, color, hoverColor);
    }

    /**
     * Constructor.
     *
     * @param observableTimeSpan the observable time span
     * @param name the name of the layer
     * @param constraint constraint on where the time span can be dragged
     * @param leftSnapFunction the snap function for the left handle
     * @param rightSnapFunction the snap function for the right handle
     * @param color the color
     * @param hoverColor the hover color
     */
    public DragHandlesLayer(ObservableTimeSpan observableTimeSpan, String name,
            Function<? super TimeInstant, ? extends TimeInstant> constraint, SnapFunction leftSnapFunction,
            SnapFunction rightSnapFunction, Color color, Color hoverColor)
    {
        super(createDragHandle(SwingConstants.LEFT, observableTimeSpan, name, constraint, leftSnapFunction, color, hoverColor),
                createDragHandle(SwingConstants.RIGHT, observableTimeSpan, name, constraint, rightSnapFunction, color,
                        hoverColor));
        myObservableTimeSpan = observableTimeSpan;
    }

    /**
     * Sets the isAboveLine.
     *
     * @param isAboveLine the isAboveLine
     */
    public void setAboveLine(boolean isAboveLine)
    {
        for (TimelineLayer layer : getLayers())
        {
            ((DragHandle)layer).setAboveLine(isAboveLine);
        }
    }

    /**
     * Sets whether to force showing the context label.
     *
     * @param forceShowContextLabel whether to force showing the context label
     */
    public void setForceShowContextLabel(boolean forceShowContextLabel)
    {
        for (TimelineLayer layer : getLayers())
        {
            ((DragHandle)layer).setForceShowContextLabel(forceShowContextLabel);
        }
    }

    /**
     * Gets the observable time span.
     *
     * @return the observable time span
     */
    public ObservableTimeSpan getObservableTimeSpan()
    {
        return myObservableTimeSpan;
    }

    /**
     * Sets the isFlagVisible.
     *
     * @param isFlagVisible the isFlagVisible
     */
    protected void setFlagVisible(boolean isFlagVisible)
    {
        for (TimelineLayer layer : getLayers())
        {
            ((DragHandle)layer).setFlagVisible(isFlagVisible);
        }
    }

    /** Test for a time instant being before the end time. */
    private static class LeftConstraint implements Function<TimeInstant, TimeInstant>
    {
        /** The end time. */
        private final ObservableValue<TimeInstant> myEnd;

        /**
         * Constructor.
         *
         * @param end the end time
         */
        public LeftConstraint(ObservableValue<TimeInstant> end)
        {
            myEnd = end;
        }

        @Override
        public TimeInstant apply(TimeInstant t)
        {
            return TimeInstant.min(myEnd.get().minus(MIN_DUR), t);
        }
    }

    /** Test for a time instant being after the start time. */
    private static class RightConstraint implements Function<TimeInstant, TimeInstant>
    {
        /** The start time. */
        private final ObservableValue<TimeInstant> myStart;

        /**
         * Constructor.
         *
         * @param start the end time
         */
        public RightConstraint(ObservableValue<TimeInstant> start)
        {
            myStart = start;
        }

        @Override
        public TimeInstant apply(TimeInstant t)
        {
            return TimeInstant.max(myStart.get().plus(MIN_DUR), t);
        }
    }
}
