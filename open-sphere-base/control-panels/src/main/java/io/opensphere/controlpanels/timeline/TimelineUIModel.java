package io.opensphere.controlpanels.timeline;

import java.awt.Point;
import java.awt.Rectangle;
import java.math.RoundingMode;
import java.util.Objects;

import javax.swing.JComponent;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueService;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.awt.AWTUtilities;

/**
 * Contains information about the timeline area that can be used by painters.
 */
@SuppressWarnings("PMD.GodClass")
public class TimelineUIModel extends ObservableValueService
{
    /** Maximum UI span (~1000 years). */
    private static final long MAX_ZOOM_LEVEL = 31536000000000L;

    /** Minimum UI span. */
    private static final long MIN_ZOOM_LEVEL = 1000L;

    /** The zoom factor. */
    private static final float ZOOM_FACTOR = 1.2f;

    /** The component this represents. */
    private JComponent myComponent;

    /** The cursor position. */
    private Point myCursorPosition;

    /** The time under the cursor. */
    private final StrongObservableValue<TimeInstant> myCursorTime = new StrongObservableValue<>();

    /** The object being dragged. */
    private Object myDraggingObject;

    /** The first mouse location. */
    private Point myFirstMousePoint;

    /** The label "panel" bounds. */
    private final Rectangle myLabelPanelBounds = new Rectangle();

    /** The last mouse location. */
    private Point myLastMousePoint;

    /** Whether to lock the selection box. */
    private boolean myLockSelection;

    /** The milliseconds per pixel. */
    private final ObservableValue<Double> myMillisPerPixel = new StrongObservableValue<>();

    /** The pixels per millisecond. */
    private double myPixelsPerMilli;

    /** A temporary message to be displayed to the user. */
    private final ObservableValue<String> myTemporaryMessage = new StrongObservableValue<>();

    /** The timeline "panel" bounds. */
    private final ObservableValue<Rectangle> myTimelinePanelBounds = new StrongObservableValue<>();

    /** The top drag "panel" bounds. */
    private final Rectangle myTopDragPanelBounds = new Rectangle();

    /** A read-only view to the end of {@link #myUISpan}. */
    private final ObservableValue<TimeInstant> myUIEndDate = new StrongObservableValue<>();

    /** The time span of the UI. */
    private final ObservableValue<TimeSpan> myUISpan = new StrongObservableValue<TimeSpan>()
    {
        @Override
        public boolean set(TimeSpan input, boolean forceFire)
        {
            TimeSpan constraint = myUISpanConstraint.get();
            return super.set(constraint == null ? input : constraint.clamp(input), forceFire);
        }
    };

    /** If the UI span is locked so it doesn't go outside the loop span. */
    private final ObservableValue<TimeSpan> myUISpanConstraint = new StrongObservableValue<>();

    /** A read-only view to the start of {@link #myUISpan}. */
    private final ObservableValue<TimeInstant> myUIStartDate = new StrongObservableValue<>();

    /**
     * Constructor.
     */
    public TimelineUIModel()
    {
        bindModel(myUISpan, new ChangeListener<TimeSpan>()
        {
            @Override
            public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
            {
                TimeSpan span = myUISpan.get();
                myUIStartDate.set(span == null ? null : span.getStartInstant());
                myUIEndDate.set(span == null ? null : span.getEndInstant());

                calculateRatios();
                setCursorPosition(myCursorPosition);
            }
        });

        bindModel(myUISpanConstraint, new ChangeListener<TimeSpan>()
        {
            @Override
            public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
            {
                getUISpan().set(getUISpan().get());
            }
        });
    }

    /**
     * Calculates the ratios of coordinates and time space.
     */
    public void calculateRatios()
    {
        if (myComponent != null)
        {
            int pixels = myComponent.getWidth();
            long millis = myUISpan.get().getDurationMs();
            myPixelsPerMilli = (double)pixels / millis;
            myMillisPerPixel.set(Double.valueOf((double)millis / pixels));
        }
    }

    /**
     * Gets the component.
     *
     * @return the component
     */
    public JComponent getComponent()
    {
        return myComponent;
    }

    /**
     * Gets the cursor position.
     *
     * @return the cursor position
     */
    public Point getCursorPosition()
    {
        return myCursorPosition;
    }

    /**
     * Gets the time under the cursor.
     *
     * @return the time under the cursor
     */
    public ObservableValue<TimeInstant> getCursorTime()
    {
        return myCursorTime;
    }

    /**
     * Gets the draggingObject.
     *
     * @return the draggingObject
     */
    public Object getDraggingObject()
    {
        return myDraggingObject;
    }

    /**
     * Gets the drag selection time span (when the user draws using shift/drag)
     * using the default snap function.
     *
     * @return the drag selection time span
     */
    public TimeSpan getDragSelectionSpan()
    {
        ResolutionBasedSnapFunction func = new ResolutionBasedSnapFunction(myMillisPerPixel);
        return getDragSelectionSpan(func, func);
    }

    /**
     * Gets the drag selection time span (when the user draws using shift/drag).
     *
     * @param leftSnapFunction the snap function for the start of the span
     * @param rightSnapFunction the snap function for the end of the span
     * @return the drag selection time span
     */
    public TimeSpan getDragSelectionSpan(SnapFunction leftSnapFunction, SnapFunction rightSnapFunction)
    {
        TimeInstant first = xToTime(myFirstMousePoint.x);
        TimeInstant last = xToTime(myLastMousePoint.x);
        TimeInstant left;
        TimeInstant right;
        if (first.compareTo(last) < 0)
        {
            left = first;
            right = last;
        }
        else
        {
            left = last;
            right = first;
        }
        left = leftSnapFunction.getSnapDestination(left, RoundingMode.FLOOR);
        right = rightSnapFunction.getSnapDestination(right, RoundingMode.CEILING);
        return TimeSpan.get(left, right);
    }

    /**
     * Gets the firstMousePoint.
     *
     * @return the firstMousePoint
     */
    public Point getFirstMousePoint()
    {
        return myFirstMousePoint;
    }

    /**
     * Gets the labelPanelBounds.
     *
     * @return the labelPanelBounds
     */
    public Rectangle getLabelPanelBounds()
    {
        return myLabelPanelBounds;
    }

    /**
     * Gets the lastMousePoint.
     *
     * @return the lastMousePoint
     */
    public Point getLastMousePoint()
    {
        return myLastMousePoint;
    }

    /**
     * Gets the millisPerPixel.
     *
     * @return the millisPerPixel
     */
    public ObservableValue<Double> getMillisPerPixel()
    {
        return myMillisPerPixel;
    }

    /**
     * Gets the pixelsPerMilli.
     *
     * @return the pixelsPerMilli
     */
    public double getPixelsPerMilli()
    {
        return myPixelsPerMilli;
    }

    /**
     * Get the temporary message.
     *
     * @return The message.
     */
    public ObservableValue<String> getTemporaryMessage()
    {
        return myTemporaryMessage;
    }

    /**
     * Gets the timeline panel bounds.
     *
     * @return the timeline panel bounds
     */
    public Rectangle getTimelinePanelBounds()
    {
        return myTimelinePanelBounds.get();
    }

    /**
     * Gets the timeline panel bounds observable value.
     *
     * @return the timeline panel bounds observable value
     */
    public ObservableValue<Rectangle> timelinePanelBoundsProperty()
    {
        return myTimelinePanelBounds;
    }

    /**
     * Gets the top drag panel bounds.
     *
     * @return the top drag panel bounds
     */
    public Rectangle getTopDragPanelBounds()
    {
        return myTopDragPanelBounds;
    }

    /**
     * Gets the UI span.
     *
     * @return the UI span
     */
    public ObservableValue<TimeSpan> getUISpan()
    {
        return myUISpan;
    }

    /**
     * Get the constraint on the UI span.
     *
     * @return The UI span constraint.
     */
    public ObservableValue<TimeSpan> getUISpanConstraint()
    {
        return myUISpanConstraint;
    }

    /**
     * Repaints the component.
     */
    public void repaint()
    {
        myComponent.repaint();
    }

    /**
     * Sets the component.
     *
     * @param component the component
     */
    public void setComponent(JComponent component)
    {
        myComponent = component;
    }

    /**
     * Set the current cursor position.
     *
     * @param point the cursor position
     * @return {@code true} if the position was changed
     */
    public boolean setCursorPosition(Point point)
    {
        myCursorPosition = point;
        TimeInstant time;
        return myCursorTime.set(point == null || !myUISpan.get().overlaps(time = snap(xToTime(point.x))) ? null : time);
    }

    /**
     * Sets the draggingObject.
     *
     * @param draggingObject the draggingObject
     * @return whether the value changed
     */
    public boolean setDraggingObject(Object draggingObject)
    {
        if (Utilities.notSameInstance(myDraggingObject, draggingObject))
        {
            myDraggingObject = draggingObject;
            return true;
        }
        return false;
    }

    /**
     * Sets the firstMousePoint.
     *
     * @param firstMousePoint the firstMousePoint
     * @return whether the value changed
     */
    public boolean setFirstMousePoint(Point firstMousePoint)
    {
        if (!Objects.equals(myFirstMousePoint, firstMousePoint))
        {
            myFirstMousePoint = firstMousePoint;
            return true;
        }
        return false;
    }

    /**
     * Sets the lastMousePoint.
     *
     * @param lastMousePoint the lastMousePoint
     * @return whether the value changed
     */
    public boolean setLastMousePoint(Point lastMousePoint)
    {
        if (!myLockSelection && !Objects.equals(myLastMousePoint, lastMousePoint))
        {
            myLastMousePoint = lastMousePoint;
            return true;
        }
        return false;
    }

    /**
     * Sets the lockSelection.
     *
     * @param lockSelection the lockSelection
     */
    public void setLockSelection(boolean lockSelection)
    {
        myLockSelection = lockSelection;
    }

    /**
     * Snap the time to a round value based on the current zoom level.
     *
     * @param time The input time.
     * @return The rounded time.
     */
    public TimeInstant snap(TimeInstant time)
    {
        return new ResolutionBasedSnapFunction(myMillisPerPixel).getSnapDestination(time, RoundingMode.HALF_UP);
    }

    /**
     * Converts a time to an x position.
     *
     * @param time the time
     * @return the x position
     */
    public int timeToX(long time)
    {
        return (int)MathUtil.clamp(Math.round((time - myUISpan.get().getStart()) * myPixelsPerMilli), Integer.MIN_VALUE,
                Integer.MAX_VALUE);
    }

    /**
     * Converts a time to an x position.
     *
     * @param time the time
     * @return the x position
     */
    public int timeToX(TimeInstant time)
    {
        return timeToX(time.getEpochMillis());
    }

    /**
     * Converts a time to an x position within the panel bounds.
     *
     * @param time the time
     * @return the x position
     */
    public int timeToXClamped(long time)
    {
        Rectangle timelineBounds = myTimelinePanelBounds.get();
        return MathUtil.clamp(timeToX(time), timelineBounds.x - 1, AWTUtilities.getMaxX(timelineBounds) + 1);
    }

    /**
     * Updates the bounds of internal "panels".
     */
    public void updateBounds()
    {
        int width = myComponent.getWidth();
        int height = myComponent.getHeight();
        myTopDragPanelBounds.setBounds(0, 0, width, 13);
        myLabelPanelBounds.setBounds(0, height - 14, width, 14);
        myTimelinePanelBounds.set(new Rectangle(0, AWTUtilities.getMaxY(myTopDragPanelBounds), width,
                height - myLabelPanelBounds.height - myTopDragPanelBounds.height));
    }

    /**
     * Converts an x position to a time.
     *
     * @param x the x position
     * @return the time
     */
    public TimeInstant xToTime(int x)
    {
        if (myMillisPerPixel.get() == null)
        {
            calculateRatios();
        }
        return TimeInstant.get(myUISpan.get().getStart() + Math.round(x * myMillisPerPixel.get().doubleValue()));
    }

    /**
     * Zooms based on the MouseWheelEvent.
     *
     * @param zoomIn true to zoom in, false to zoom out
     * @param location the relative location on the timeline (0 = far left, 1 =
     *            far right)
     */
    public void zoom(boolean zoomIn, float location)
    {
        long durationMs = myUISpan.get().getDurationMs();

        // Limit the amount they can zoom out
        if (durationMs > MAX_ZOOM_LEVEL && !zoomIn || durationMs < MIN_ZOOM_LEVEL && zoomIn)
        {
            return;
        }

        double zoomDeltaMs = (ZOOM_FACTOR - 1f) * durationMs;
        double leftZoomDeltaMs = zoomDeltaMs * location;
        double rightZoomDeltaMs = zoomDeltaMs * (1f - location);
        if (zoomIn)
        {
            leftZoomDeltaMs /= -ZOOM_FACTOR;
            rightZoomDeltaMs /= -ZOOM_FACTOR;
        }
        myUISpan.set(TimeSpan.get(myUISpan.get().getStart() - Math.round(leftZoomDeltaMs),
                myUISpan.get().getEnd() + Math.round(rightZoomDeltaMs)));
    }
}
