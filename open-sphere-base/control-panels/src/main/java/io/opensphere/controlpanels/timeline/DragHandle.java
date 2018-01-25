package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

import javax.swing.SwingConstants;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * A drag handle in the timeline.
 */
public class DragHandle extends AbstractTimelineLayer
{
    /** The triangle size. */
    public static final int TRIANGLE_SIZE = 11;

    /** The drag handle color. */
    private final Color myColor;

    /** Constraint on where the time can be dragged. */
    private final Function<? super TimeInstant, ? extends TimeInstant> myConstraint;

    /** The context label layer (made private as an optimization). */
    private final ContextLabel myContextLabel = new ContextLabel();

    /** A date used for painting (made private as an optimization). */
    private final Date myDate = new Date();

    /** The current drag location (without snapping). */
    private TimeInstant myDragShadow;

    /** The time when dragging started. */
    private TimeInstant myDragStartValue;

    /** The flag polygon. */
    private Polygon myFlagPolygon;

    /** Whether to force showing the context label. */
    private boolean myForceShowContextLabel;

    /** The label format. */
    private final SimpleDateFormat myFormat = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);

    /** The drag handle hover color. */
    private final Color myHoverColor;

    /** Whether to paint the flag above the line. */
    private boolean myIsAboveLine = true;

    /** Whether the flag is visible. */
    private boolean myIsFlagVisible = true;

    /** The line area polygon. */
    private final Rectangle myLineArea = new Rectangle();

    /** The name of the layer. */
    private final String myName;

    /** Which side the handle is on. */
    private final int mySide;

    /** The snap function. */
    private final SnapFunction mySnapFunction;

    /** The time. */
    private final ObservableValue<TimeInstant> myTime;

    /**
     * Constructor.
     *
     * @param side Which side the handle is on
     * @param time the time
     * @param name the name of the layer
     * @param constraint constraint on where the time can be dragged
     * @param snapFunction the snap function
     * @param color the color
     * @param hoverColor the hover color
     */
    public DragHandle(int side, ObservableValue<TimeInstant> time, String name,
            Function<? super TimeInstant, ? extends TimeInstant> constraint, SnapFunction snapFunction, Color color,
            Color hoverColor)
    {
        mySide = side;
        myTime = time;
        myName = name;
        myConstraint = constraint;
        mySnapFunction = snapFunction;
        myColor = color;
        myHoverColor = hoverColor;
    }

    @Override
    public boolean canDrag(Point p)
    {
        return myIsFlagVisible && p != null && (myFlagPolygon != null && myFlagPolygon.contains(p) || myLineArea.contains(p));
    }

    @Override
    public Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime)
    {
        if (beginning)
        {
            myDragStartValue = myTime.get();
        }

        myDragShadow = myDragStartValue.plus(dragTime);

        TimeInstant newTime = mySnapFunction.getSnapDestination(myDragStartValue.plus(dragTime), RoundingMode.HALF_UP);
        myTime.set(myConstraint.apply(newTime));
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        if (canDrag(event.getPoint()))
        {
            return StringUtilities.concat("The ", myName, mySide == SwingConstants.LEFT ? " span start" : " span end");
        }
        else
        {
            return incoming;
        }
    }

    @Override
    public boolean hasDragObject(Object dragObject)
    {
        return Utilities.sameInstance(this, dragObject);
    }

    /**
     * Gets the isFlagVisible.
     *
     * @return the isFlagVisible
     */
    public boolean isFlagVisible()
    {
        return myIsFlagVisible;
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);

        long time = myTime.get().getEpochMillis();
        int x = getUIModel().timeToX(time);
        boolean hasFocus = isDraggingOrCanDrag();

        boolean isHovering = getUIModel().getDraggingObject() == null && canDrag(getUIModel().getLastMousePoint());
        g2d.setColor(isHovering ? myHoverColor : myColor);

        // Draw the line
        drawLine(g2d, x);
        int maxY = AWTUtilities.getMaxY(getUIModel().getTimelinePanelBounds()) - 1;
        myLineArea.setBounds(MathUtil.subtractSafe(x, 1), getUIModel().getTimelinePanelBounds().y, 4, maxY);

        // Draw the flag
        if (myIsFlagVisible)
        {
            updatePolygon(x);
            g2d.fill(myFlagPolygon);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.draw(myFlagPolygon);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }

        // Draw the context label
        if (hasFocus || myForceShowContextLabel && myIsFlagVisible)
        {
            myDate.setTime(time);
            String text = myFormat.format(myDate);
            int labelX = AWTUtilities.getTextXLocation(text, x, DragHandle.TRIANGLE_SIZE + 4, mySide, g2d);
            int labelY = AWTUtilities.getMaxY(getUIModel().getTopDragPanelBounds()) - 2;
            myContextLabel.update(text, labelX, labelY);
            getTemporaryLayers().add(myContextLabel);
        }

        // Set the cursor
        if (hasFocus)
        {
            getUIModel().getComponent().setCursor(
                    Cursor.getPredefinedCursor(mySide == SwingConstants.LEFT ? Cursor.W_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR));
        }

        // Draw the drag shadow
        if (Utilities.sameInstance(this, getUIModel().getDraggingObject()) && myDragShadow != null)
        {
            int shadowX = getUIModel().timeToX(myDragShadow.getEpochMillis());
            if (Math.abs(MathUtil.subtractSafe(x, shadowX)) > 5)
            {
                g2d.setColor(ColorUtilities.opacitizeColor(myColor, 100));
                drawLine(g2d, shadowX);

                Stroke oldStroke = g2d.getStroke();
                int halfY = getUIModel().getTimelinePanelBounds().y + (getUIModel().getTimelinePanelBounds().height >> 1);
                g2d.setStroke(new DashedStroke());
                g2d.drawLine(x, halfY, shadowX, halfY);
                g2d.setStroke(oldStroke);

                if (myIsFlagVisible)
                {
                    updatePolygon(shadowX);
                    g2d.fill(myFlagPolygon);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.draw(myFlagPolygon);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
                }
            }
        }
    }

    /**
     * Sets the isAboveLine.
     *
     * @param isAboveLine the isAboveLine
     */
    public void setAboveLine(boolean isAboveLine)
    {
        myIsAboveLine = isAboveLine;
    }

    /**
     * Sets the isFlagVisible.
     *
     * @param isFlagVisible the isFlagVisible
     */
    public void setFlagVisible(boolean isFlagVisible)
    {
        myIsFlagVisible = isFlagVisible;
    }

    /**
     * Sets whether to force showing the context label.
     *
     * @param forceShowContextLabel whether to force showing the context label
     */
    public void setForceShowContextLabel(boolean forceShowContextLabel)
    {
        myForceShowContextLabel = forceShowContextLabel;
    }

    /**
     * Updates the flag polygon.
     *
     * @param x the x value
     */
    private void updatePolygon(int x)
    {
        int y3 = AWTUtilities.getMaxY(getUIModel().getTopDragPanelBounds());
        y3 += myIsAboveLine ? -1 : TRIANGLE_SIZE;
        int y2 = y3 - (TRIANGLE_SIZE >> 1);
        int y1 = y3 - TRIANGLE_SIZE + 1;
        if (mySide == SwingConstants.RIGHT)
        {
            int x2 = MathUtil.addSafe(x, TRIANGLE_SIZE);
            myFlagPolygon = new Polygon(new int[] { x, x2, x2, x }, new int[] { y1, y1, y2, y3 }, 4);
        }
        else
        {
            int x2 = MathUtil.subtractSafe(x, TRIANGLE_SIZE);
            myFlagPolygon = new Polygon(new int[] { x2, x, x, x2 }, new int[] { y1, y1, y3, y2 }, 4);
        }
    }
}
