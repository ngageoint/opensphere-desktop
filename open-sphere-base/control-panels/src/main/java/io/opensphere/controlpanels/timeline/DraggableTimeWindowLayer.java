package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Utilities;

/**
 * A draggable time window layer.
 */
public class DraggableTimeWindowLayer extends TimeWindowLayer
{
    /**
     * Optional constraint on where the time span can be dragged.
     */
    @Nullable
    private final Function<TimeInstant, TimeInstant> myConstraint;

    /**
     * Keep track of whether we could drag in order to suppress the tool tip
     * after a while.
     */
    private boolean myCouldDrag;

    /** The current drag location (without snapping). */
    private TimeSpan myDragShadow;

    /** The time span when dragging started. */
    private TimeSpan myDragStartValue;

    /** The snap function. */
    private final SnapFunction mySnapFunction;

    /**
     * The duration of the time span, which could be a month versus what is
     * returned by timeSpan.getDuration.
     */
    private final Supplier<Duration> myTimeSpanDuration;

    /** The number of times the tool tip has been displayed. */
    private int myToolTipDisplayCount;

    /** The tool tip text. */
    private String myToolTipText;

    /**
     * Constructor.
     *
     * @param timeSpan the time span
     * @param outlineColor the outline color
     * @param bgColor the background color
     * @param constraint optional constraint on where the time span can be
     *            dragged
     * @param timeSpanDuration the duration of the time span, which could be a
     *            month versus what is returned by timeSpan.getDuration
     * @param snapFunction the snap function
     * @param deleter the layer deleter, or null if it's not deletable
     */
    public DraggableTimeWindowLayer(ObservableValue<TimeSpan> timeSpan, Color outlineColor, Color bgColor,
            @Nullable Function<TimeInstant, TimeInstant> constraint, Supplier<Duration> timeSpanDuration,
            SnapFunction snapFunction, Consumer<? super TimeSpan> deleter)
    {
        super(timeSpan, outlineColor, bgColor, deleter);
        myTimeSpanDuration = timeSpanDuration;
        myConstraint = constraint;
        mySnapFunction = snapFunction;
    }

    @Override
    public boolean canDrag(Point p)
    {
        return p != null && squeeze(getRectangle(), 2).contains(p)
                && !getTimeSpan().get().contains(getUIModel().getUISpan().get());
    }

    @Override
    public Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime)
    {
        if (beginning)
        {
            myDragStartValue = getTimeSpan().get();
        }
        myDragShadow = myDragStartValue.plus(dragTime);
        TimeInstant end = mySnapFunction.getSnapDestination(myDragStartValue.getEndInstant().plus(dragTime),
                RoundingMode.HALF_UP);
        TimeSpan newValue = TimeSpan.get(myTimeSpanDuration.get(), end);
        if (myConstraint != null)
        {
            if (dragTime.signum() > 0)
            {
                long delta = newValue.getEnd() - myConstraint.apply(newValue.getEndInstant()).getEpochMillis();
                if (delta > 0)
                {
                    newValue = newValue.minus(new Milliseconds(delta));
                }
            }
            else
            {
                long delta = myConstraint.apply(newValue.getStartInstant()).getEpochMillis() - newValue.getStart();
                if (delta > 0)
                {
                    newValue = newValue.plus(new Milliseconds(delta));
                }
            }
        }
        getTimeSpan().set(newValue);
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        boolean canDrag = canDrag(event.getPoint());

        // Stop showing the tool tip after a while
        if (!myCouldDrag && canDrag)
        {
            if (myToolTipDisplayCount < 3)
            {
                myToolTipDisplayCount++;
            }
            else
            {
                myToolTipText = null;
            }
        }
        myCouldDrag = canDrag;

        return canDrag && myToolTipText != null ? myToolTipText : super.getToolTipText(event, incoming);
    }

    @Override
    public boolean hasDragObject(Object dragObject)
    {
        return Utilities.sameInstance(this, dragObject);
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);

        if (getTimeSpan().get().overlaps(getUIModel().getUISpan().get()) && getUIModel().getDraggingObject() == null
                && canDrag(getUIModel().getLastMousePoint()))
        {
            getUIModel().getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        if (Utilities.sameInstance(this, getUIModel().getDraggingObject()) && myDragShadow != null)
        {
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new DashedStroke());

            int startX = getUIModel().timeToXClamped(myDragShadow.getStart());
            int endX = getUIModel().timeToXClamped(myDragShadow.getEnd());
            Rectangle rect = new Rectangle(startX, getUIModel().getTimelinePanelBounds().y, endX - startX,
                    getUIModel().getTimelinePanelBounds().height);
            g2d.setColor(new Color(127, 127, 255, 200));
            g2d.draw(rect);
            g2d.setStroke(oldStroke);
        }
    }

    /**
     * Sets the toolTipText.
     *
     * @param toolTipText the toolTipText
     */
    public void setToolTipText(String toolTipText)
    {
        myToolTipText = toolTipText;
    }

    /**
     * Squeezes a rectangle horizontally.
     *
     * @param r the rectangle
     * @param amount the squeeze amount
     * @return the squeezed rectangle
     */
    private static Rectangle squeeze(Rectangle r, int amount)
    {
        return new Rectangle(r.x + amount, r.y, r.width - amount * 2, r.height);
    }
}
