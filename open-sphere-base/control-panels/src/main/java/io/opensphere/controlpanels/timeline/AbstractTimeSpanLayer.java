package io.opensphere.controlpanels.timeline;

import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableValue;

/**
 * An abstract layer who's model is a time span.
 */
public abstract class AbstractTimeSpanLayer extends AbstractTimelineLayer
{
    /** The direction of the active window. */
    private Direction myDirection = Direction.FORWARD;

    /** Whether the duration label is visible. */
    private boolean myDurationLabelVisible;

    /** Whether the end label is visible. */
    private boolean myEndLabelVisible;

    /** Whether the start label is visible. */
    private boolean myStartLabelVisible;

    /** The time span. */
    private final ObservableValue<TimeSpan> myTimeSpan;

    /**
     * Constructor.
     *
     * @param timeSpan the time span
     */
    public AbstractTimeSpanLayer(ObservableValue<TimeSpan> timeSpan)
    {
        super();
        myTimeSpan = timeSpan;
    }

    /**
     * Gets the direction the span is moving.
     *
     * @return the direction
     */
    public Direction getDirection()
    {
        return myDirection;
    }

    /**
     * Gets the time span.
     *
     * @return the time span
     */
    public ObservableValue<TimeSpan> getTimeSpan()
    {
        return myTimeSpan;
    }

    /**
     * Gets the visibility of the duration label.
     *
     * @return whether the label is visible
     */
    public boolean isDurationLabelVisible()
    {
        return myDurationLabelVisible;
    }

    /**
     * Gets the visibility of the end label.
     *
     * @return whether the label is visible
     */
    public boolean isEndLabelVisible()
    {
        return myEndLabelVisible;
    }

    /**
     * Gets the visibility of the start label.
     *
     * @return whether the label is visible
     */
    public boolean isStartLabelVisible()
    {
        return myStartLabelVisible;
    }

    /**
     * Set the direction the span is moving.
     *
     * @param direction The direction.
     */
    public void setDirection(Direction direction)
    {
        myDirection = direction;
    }

    /**
     * Sets the visibility of the duration label.
     *
     * @param isVisible whether the label is visible
     */
    public void setDurationLabelVisible(boolean isVisible)
    {
        myDurationLabelVisible = isVisible;
    }

    /**
     * Sets the visibility of the end label.
     *
     * @param isVisible whether the label is visible
     */
    public void setEndLabelVisible(boolean isVisible)
    {
        myEndLabelVisible = isVisible;
    }

    /**
     * Sets the visibility of the start label.
     *
     * @param isVisible whether the label is visible
     */
    public void setStartLabelVisible(boolean isVisible)
    {
        myStartLabelVisible = isVisible;
    }
}
