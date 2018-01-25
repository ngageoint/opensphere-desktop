package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;

import javax.swing.SwingConstants;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;

/**
 * A line with a time label on the timeline.
 */
public class TimeProbe extends AbstractTimelineLayer
{
    /** The line color. */
    private final Color myColor;

    /** The context label layer (made private as an optimization). */
    private final ContextLabel myContextLabel = new ContextLabel();

    /** The time. */
    private final ObservableValue<TimeInstant> myTime;

    /**
     * Constructor.
     *
     * @param time The time.
     * @param color The color.
     */
    public TimeProbe(ObservableValue<TimeInstant> time, Color color)
    {
        myTime = time;
        myColor = color;
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);
        if (myTime.get() != null && getUIModel().getDraggingObject() == null
                && getUIModel().getTimelinePanelBounds().contains(getUIModel().getLastMousePoint()))
        {
            int x = getUIModel().timeToX(myTime.get());

            // Draw the line
            g2d.setColor(myColor);
            drawLine(g2d, x);

            // Set the context label
            String text = formatTime();
            int labelX = AWTUtilities.getTextXLocation(text, x, 0, SwingConstants.CENTER, g2d);
            int labelY = AWTUtilities.getMaxY(getUIModel().getLabelPanelBounds()) - 2;
            myContextLabel.update(text, labelX, labelY);
            getTemporaryLayers().add(myContextLabel);
        }
    }

    /**
     * Format the time for the tool tip based on the precision of the date
     * value.
     *
     * @return The formatted time.
     */
    private String formatTime()
    {
        String fmt;
        int modulus = ResolutionBasedSnapFunction.getModulus(getUIModel().getMillisPerPixel().get().doubleValue());
        if (modulus < Constants.MILLI_PER_UNIT)
        {
            fmt = "HH:mm:ss.SSS";
        }
        else if (modulus < Constants.MILLIS_PER_MINUTE)
        {
            fmt = "HH:mm:ss";
        }
        else if (modulus < Constants.MILLIS_PER_HOUR)
        {
            fmt = "HH:mm";
        }
        else if (modulus < Constants.MILLIS_PER_DAY)
        {
            fmt = "EEE dd MMM HH:mm";
        }
        else
        {
            fmt = "EEE dd MMM";
        }

        return new SimpleDateFormat(fmt).format(myTime.get().toDate());
    }
}
