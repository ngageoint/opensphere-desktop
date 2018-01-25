package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

import javax.swing.SwingConstants;

import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;

/**
 * A time window layer.
 */
public class TimeWindowLayer extends AbstractTimeSpanLayer
{
    /** Normal time format. */
    private static final SimpleDateFormat ourFormatNormal = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);

    /** Time format with milliseconds. */
    private static final SimpleDateFormat ourFormatMillis = new SimpleDateFormat(DateTimeFormats.DATE_TIME_MILLIS_FORMAT);

    /** The window background color. */
    private final Color myBGColor;

    /** The window outline color. */
    private final Color myOutlineColor;

    /** The window rectangle. */
    private final Rectangle myRectangle = new Rectangle();

    /** The time listener. */
    private final ChangeListener<TimeSpan> myTimeListener;

    /** The layer deleter. */
    private final Consumer<? super TimeSpan> myDeleter;

    /** The delete icon. */
    private final Image myDeleteIcon = IconUtil.getColorizedIcon(IconType.CLOSE, Color.RED).getImage();

    /** The delete icon rectangle. */
    private final Rectangle myDeleteRectangle = new Rectangle();

    /**
     * Constructor.
     *
     * @param timeSpan the time span
     * @param color the window color
     */
    public TimeWindowLayer(ObservableValue<TimeSpan> timeSpan, Color color)
    {
        this(timeSpan, color, ColorUtilities.opacitizeColor(color, 64), null);
    }

    /**
     * Constructor.
     *
     * @param timeSpan the time span
     * @param outlineColor the outline color
     * @param bgColor the background color
     * @param deleter the layer deleter, or null if it's not deletable
     */
    public TimeWindowLayer(ObservableValue<TimeSpan> timeSpan, Color outlineColor, Color bgColor,
            Consumer<? super TimeSpan> deleter)
    {
        super(timeSpan);
        myOutlineColor = outlineColor;
        myBGColor = bgColor;
        myDeleter = deleter;

        myTimeListener = new ChangeListener<TimeSpan>()
        {
            @Override
            public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
            {
                getUIModel().repaint();
            }
        };
        timeSpan.addListener(myTimeListener);
    }

    /**
     * Gets the background color.
     *
     * @return the background color
     */
    public Color getBGColor()
    {
        return myBGColor;
    }

    /**
     * Gets the rectangle.
     *
     * @return the rectangle
     */
    public Rectangle getRectangle()
    {
        return myRectangle;
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);
        if (getTimeSpan().get().overlaps(getUIModel().getUISpan().get()))
        {
            int startX = getUIModel().timeToXClamped(getTimeSpan().get().getStart());
            int endX = getUIModel().timeToXClamped(getTimeSpan().get().getEnd());
            myRectangle.setBounds(startX, getUIModel().getTimelinePanelBounds().y, endX - startX,
                    getUIModel().getTimelinePanelBounds().height);
            if (myBGColor != null)
            {
                g2d.setColor(myBGColor);
                g2d.fill(myRectangle);
            }
            g2d.setColor(myOutlineColor);
            g2d.draw(myRectangle);

            if (myDeleter != null)
            {
                int iconSize = 12;
                int iconX = (startX + endX - iconSize) / 2;
                int iconY = getUIModel().getTopDragPanelBounds().y;
                g2d.drawImage(myDeleteIcon, iconX, iconY, iconSize, iconSize, null);
                myDeleteRectangle.setBounds(iconX, iconY, iconSize, iconSize);
            }

            addLabels(g2d);
        }
        else
        {
            myRectangle.setBounds(0, 0, 0, 0);
        }
    }

    @Override
    public void mouseEvent(MouseEvent e)
    {
        if (myDeleter != null && e.getButton() == MouseEvent.BUTTON1 && myDeleteRectangle.contains(e.getX(), e.getY()))
        {
            myDeleter.accept(getTimeSpan().get());
        }
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        return myDeleteRectangle.contains(event.getX(), event.getY()) ? "Remove this span" : incoming;
    }

    /**
     * Adds context labels.
     *
     * @param g2d the graphics context
     */
    private void addLabels(Graphics2D g2d)
    {
        if (isStartLabelVisible())
        {
            Date date = getTimeSpan().get().getStartDate();
            String text = format(date);
            int x = getUIModel().timeToX(date.getTime());
            x = AWTUtilities.getTextXLocation(text, x, 0, SwingConstants.LEFT, g2d);
            int y = AWTUtilities.getMaxY(getUIModel().getTopDragPanelBounds()) - 2;
            getTemporaryLayers().add(new ContextLabel(text, x, y));
        }
        if (isEndLabelVisible())
        {
            Date date = getTimeSpan().get().getEndDate();
            String text = format(date);
            int x = getUIModel().timeToX(date.getTime());
            x = AWTUtilities.getTextXLocation(text, x, 0, SwingConstants.RIGHT, g2d);
            int y = AWTUtilities.getMaxY(getUIModel().getTopDragPanelBounds()) - 2;
            getTemporaryLayers().add(new ContextLabel(text, x, y));
        }
        if (isDurationLabelVisible())
        {
            String text = getTimeSpan().get().getDuration().toPrettyString();
            int side = getDirection() == Direction.FORWARD
                    && getUIModel().getUISpan().get().overlaps(getTimeSpan().get().getEndInstant()) ? SwingConstants.RIGHT
                            : getUIModel().getUISpan().get().overlaps(getTimeSpan().get().getStartInstant()) ? SwingConstants.LEFT
                                    : SwingConstants.RIGHT;
            int x = getUIModel()
                    .timeToX(side == SwingConstants.RIGHT ? getTimeSpan().get().getEnd() : getTimeSpan().get().getStart());
            x = AWTUtilities.getTextXLocation(text, x, 4, side, g2d);
            int y = getUIModel().getTimelinePanelBounds().y + (getUIModel().getTimelinePanelBounds().height >> 1) - 3;
            getTemporaryLayers().add(new ContextLabel(text, x, y));
        }
    }

    /**
     * Formats the given date dynamically based on the precision of the Date.
     *
     * @param date the date
     * @return the formatted string
     */
    private String format(Date date)
    {
        int modulus = ResolutionBasedSnapFunction.getModulus(getUIModel().getMillisPerPixel().get().doubleValue());
        SimpleDateFormat formatter = modulus < Constants.MILLI_PER_UNIT ? ourFormatMillis : ourFormatNormal;
        return formatter.format(date);
    }
}
