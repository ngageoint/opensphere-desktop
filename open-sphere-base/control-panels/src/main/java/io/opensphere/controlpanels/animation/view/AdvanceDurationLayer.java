package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.AbstractTimelineLayer;
import io.opensphere.controlpanels.timeline.ContextLabel;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.awt.AWTUtilities;

/**
 * Layer to see and adjust the advance duration.
 */
class AdvanceDurationLayer extends AbstractTimelineLayer
{
    /** The ghost active window color. */
    private static final Color GHOST_WINDOW_COLOR = new Color(255, 255, 255, 16);

    /** The active time span. */
    private final ObservableValue<TimeSpan> myActiveTimeSpan;

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The line area polygon. */
    private final Rectangle myLineArea = new Rectangle();

    /** The handle bounding rectangle. */
    private final Rectangle myHandleRect = new Rectangle();

    /** The instant when dragging started. */
    private TimeInstant myDragStartValue;

    /**
     * Constructor.
     *
     * @param activeTimeSpan the active time span
     * @param animationModel the animation model
     */
    public AdvanceDurationLayer(ObservableValue<TimeSpan> activeTimeSpan, AnimationModel animationModel)
    {
        super();
        myActiveTimeSpan = activeTimeSpan;
        myAnimationModel = animationModel;
    }

    @Override
    public boolean canDrag(Point p)
    {
        return !myAnimationModel.getPlayState().isPlaying() && p != null && (myHandleRect.contains(p) || myLineArea.contains(p));
    }

    @Override
    public Object drag(Object dragObject, Point from, Point to, boolean beginning, Duration dragTime)
    {
        if (beginning)
        {
            myDragStartValue = myActiveTimeSpan.get().getEndInstant().plus(myAnimationModel.getAdvanceDuration());
        }

        TimeInstant newValue = getUIModel().snap(myDragStartValue.plus(dragTime));
        Duration newAdvanceDuration = newValue.minus(myActiveTimeSpan.get().getEndInstant());
        if (newAdvanceDuration.signum() > 0)
        {
            myAnimationModel.setAdvanceDuration(newAdvanceDuration);
        }

        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        return incoming;
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

        if (!myAnimationModel.getPlayState().isPlaying()
                && (getUIModel().getDraggingObject() == null || getUIModel().getDraggingObject() == this))
        {
            TimeInstant startTime = myActiveTimeSpan.get().getEndInstant();
            TimeInstant endTime = startTime.plus(myAnimationModel.getAdvanceDuration());
            if (getUIModel().getUISpan().get().overlaps(endTime))
            {
                int startX = getUIModel().timeToX(startTime);
                int endX = getUIModel().timeToX(endTime);
                int nearMinX = getUIModel().timeToX(myActiveTimeSpan.get().getStart());
                int nearMaxX = MathUtil.addSafe(endX, 30);
                Rectangle nearRect = new Rectangle(nearMinX, getUIModel().getTimelinePanelBounds().y,
                        MathUtil.subtractSafe(nearMaxX, nearMinX), getUIModel().getTimelinePanelBounds().height);
                Point cursorPosition = getUIModel().getCursorPosition();
                if (cursorPosition != null && nearRect.contains(cursorPosition))
                {
                    paint(g2d, startX, endX);
                }
            }
        }
    }

    /**
     * Paints for real.
     *
     * @param g2d the graphics context
     * @param startX where to start painting
     * @param endX where to end painting
     */
    private void paint(Graphics2D g2d, int startX, int endX)
    {
        // Paint the box where the active window will be
        if (Utilities.sameInstance(this, getUIModel().getDraggingObject()))
        {
            TimeSpan newActiveTime = myActiveTimeSpan.get().plus(myAnimationModel.getAdvanceDuration());
            int activeStartX = getUIModel().timeToX(newActiveTime.getStart());
            int activeEndX = getUIModel().timeToX(newActiveTime.getEnd());
            Rectangle rect = new Rectangle(activeStartX, getUIModel().getTimelinePanelBounds().y,
                    MathUtil.subtractSafe(activeEndX, activeStartX), getUIModel().getTimelinePanelBounds().height);
            g2d.setColor(GHOST_WINDOW_COLOR);
            g2d.fill(rect);
        }

        boolean hasFocus = isDraggingOrCanDrag();
        g2d.setColor(hasFocus ? AnimationConstants.ACTIVE_HANDLE_HOVER_COLOR : AnimationConstants.ACTIVE_HANDLE_COLOR);

        // Draw vertical line
        drawLine(g2d, endX);
        int maxY = AWTUtilities.getMaxY(getUIModel().getTimelinePanelBounds()) - 1;
        myLineArea.setBounds(MathUtil.subtractSafe(endX, 1), getUIModel().getTimelinePanelBounds().y, 4, maxY);

        int halfY = getUIModel().getTimelinePanelBounds().y + (getUIModel().getTimelinePanelBounds().height >> 1);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the handle
        myHandleRect.setBounds(endX, halfY - 11, 6, 22);
        paintRightHandle(g2d, myHandleRect);
        paintRightHandle(g2d, new Rectangle(endX, halfY - 8, 4, 16));

        g2d.setColor(AnimationConstants.ACTIVE_HANDLE_COLOR);

        // Draw the arrow head
        if (MathUtil.subtractSafe(endX, startX) > 7)
        {
            g2d.drawLine(endX, halfY, endX - 7, halfY + 7);
            g2d.drawLine(endX, halfY, endX - 7, halfY - 7);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

        // Draw horizontal line
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new DashedStroke());
        g2d.drawLine(startX, halfY, endX, halfY);
        g2d.setStroke(oldStroke);

        if (hasFocus)
        {
            // Add duration label
            String label = "Advance: " + myAnimationModel.getAdvanceDuration().toPrettyString();
            int labelX = AWTUtilities.getTextXLocation(label, endX, 0, SwingConstants.RIGHT, g2d) + myHandleRect.width + 2;
            int labelY = getUIModel().getTimelinePanelBounds().y + (getUIModel().getTimelinePanelBounds().height >> 1) - 3;
            getTemporaryLayers().add(new ContextLabel(label, labelX, labelY));

            // Set the cursor
            getUIModel().getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        }
    }

    /**
     * Paints a right handle.
     *
     * @param g2d the graphics context
     * @param r the bounding rectangle
     */
    private void paintRightHandle(Graphics2D g2d, Rectangle r)
    {
        int y = r.y + (r.height >> 1);
        int width = r.width - 2;
        int arcLength = width << 1;
        int halfLineHeight = (r.height >> 1) - width;
        g2d.drawArc(r.x - width, y - width - halfLineHeight, arcLength, arcLength, 0, 90);
        g2d.drawLine(r.x + width, y + halfLineHeight, r.x + width, y - halfLineHeight);
        g2d.drawArc(r.x - width, y - width + halfLineHeight, arcLength, arcLength, 0, -90);
    }
}
