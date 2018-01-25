package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Months;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.units.duration.Years;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * The timeline layer with tick marks and time labels.
 */
public class GridTimelineLayer extends AbstractTimelineLayer
{
    /** The bold font. */
    private static final Font BOLD_FONT = new Font(Font.DIALOG, Font.BOLD, 12);

    /** The normal font. */
    private static final Font PLAIN_FONT = new Font(Font.DIALOG, Font.PLAIN, 10);

    /** The maximum label width. */
    private int myMaxLabelWidth;

    /** The tick infos. */
    private final List<TickInfo> myTickInfos;

    /**
     * Constructor.
     */
    public GridTimelineLayer()
    {
        myTickInfos = New.list(27);
        myTickInfos.add(new TickInfo(new Milliseconds(1)));
        myTickInfos.add(new TickInfo(new Milliseconds(10)));
        myTickInfos.add(new TickInfo(new Milliseconds(100)));
        myTickInfos.add(new TickInfo(new Seconds(1)));
        myTickInfos.add(new TickInfo(new Seconds(5)));
        myTickInfos.add(new TickInfo(new Seconds(15)));
        myTickInfos.add(new TickInfo(new Seconds(30)));
        myTickInfos.add(new TickInfo(new Minutes(1)));
        myTickInfos.add(new TickInfo(new Minutes(5)));
        myTickInfos.add(new TickInfo(new Minutes(15)));
        myTickInfos.add(new TickInfo(new Minutes(30)));
        myTickInfos.add(new TickInfo(new Hours(1)));
        myTickInfos.add(new TickInfo(new Hours(3)));
        myTickInfos.add(new TickInfo(new Hours(6)));
        myTickInfos.add(new TickInfo(new Days(1)));
        myTickInfos.add(new TickInfo(new Weeks(1)));
        myTickInfos.add(new TickInfo(new Months(1)));
        myTickInfos.add(new TickInfo(new Months(3)));
        myTickInfos.add(new TickInfo(new Years(1)));
        myTickInfos.add(new TickInfo(new Years(5)));
        myTickInfos.add(new TickInfo(new Years(10)));
        myTickInfos.add(new TickInfo(new Years(20)));
        myTickInfos.add(new TickInfo(new Years(50)));
        myTickInfos.add(new TickInfo(new Years(100)));
        myTickInfos.add(new TickInfo(new Years(200)));
        myTickInfos.add(new TickInfo(new Years(500)));
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        Rectangle bounds = getUIModel().getTimelinePanelBounds();
        if (bounds != null && event.getPoint().y > AWTUtilities.getMaxY(bounds))
        {
            TimeInstant time = getUIModel().xToTime(event.getPoint().x);
            return new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss").format(time.toDate());
        }
        else
        {
            return incoming;
        }
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);
        g2d.setColor(getUIModel().getComponent().getForeground());
        paintLines(g2d);
        paintTicksAndLabels(g2d);
    }

    /**
     * Get the max label width using the font from the graphics object.
     *
     * @param g The graphics.
     * @return The max label width.
     */
    private int getMaxLabelWidth(Graphics2D g)
    {
        Graphics2D g2d = (Graphics2D)g.create();
        try
        {
            g2d.setFont(BOLD_FONT);

            Calendar cal = Calendar.getInstance();
            cal.clear();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM ");
            int maxMonthWidth = 0;
            for (int month = cal.getMinimum(Calendar.MONTH); month <= cal.getMaximum(Calendar.MONTH); ++month)
            {
                cal.set(Calendar.MONTH, month);
                String str = sdf.format(cal.getTime());
                int textWidth = (int)AWTUtilities.getTextWidth(str, g2d);
                if (textWidth > maxMonthWidth)
                {
                    maxMonthWidth = textWidth;
                }
            }

            sdf = new SimpleDateFormat("dd");
            int maxDayWidth = 0;
            for (int day = cal.getMinimum(Calendar.DAY_OF_MONTH); day <= cal.getMaximum(Calendar.DAY_OF_MONTH); ++day)
            {
                cal.set(Calendar.DAY_OF_MONTH, day);
                String str = sdf.format(cal.getTime());
                int textWidth = (int)AWTUtilities.getTextWidth(str, g2d);
                if (textWidth > maxDayWidth)
                {
                    maxDayWidth = textWidth;
                }
            }

            return maxMonthWidth + maxDayWidth;
        }
        finally
        {
            g2d.dispose();
        }
    }

    /**
     * Paints the top/bottom lines.
     *
     * @param g2d the graphics
     */
    private void paintLines(Graphics2D g2d)
    {
        Rectangle rect = getUIModel().getTimelinePanelBounds();
        int minX = rect.x;
        int minY = rect.y;
        int maxX = rect.x + rect.width;
        int maxY = rect.y + rect.height;
        g2d.drawLine(minX, minY, maxX, minY);
        g2d.drawLine(minX, maxY, maxX, maxY);
    }

    /**
     * Paints all the ticks and labels.
     *
     * @param g2d the graphics
     */
    private void paintTicksAndLabels(Graphics2D g2d)
    {
        if (myMaxLabelWidth == 0)
        {
            myMaxLabelWidth = getMaxLabelWidth(g2d);
        }

        // Find the tick infos to paint
        TickInfo primaryTick = null;
        TickInfo secondaryTick = null;
        for (TickInfo tickInfo : myTickInfos)
        {
            double intervalPixels = tickInfo.getMillisPerInterval() * getUIModel().getPixelsPerMilli();
            if (intervalPixels > myMaxLabelWidth)
            {
                primaryTick = tickInfo;
                break;
            }
            if (intervalPixels > 10)
            {
                secondaryTick = tickInfo;
            }
        }

        Collection<Rectangle> paintedRegions = New.collection();

        // If the primary tick is weeks, put months in there too.
        if (primaryTick != null && primaryTick.getIntervalDuration() instanceof Weeks)
        {
            paintTicksAndLabels(g2d, new TickInfo(Months.ONE), true, true, paintedRegions);
        }
        // Paint
        if (primaryTick != null)
        {
            paintTicksAndLabels(g2d, primaryTick, true, false, paintedRegions);
        }
        if (secondaryTick != null)
        {
            paintTicksAndLabels(g2d, secondaryTick, false, false, paintedRegions);
        }
    }

    /**
     * Paints a set of tick marks and optionally labels.
     *
     * @param g2d the graphics
     * @param tickInfo the tick info
     * @param paintLabel whether to paint the label
     * @param bold force bold
     * @param paintedRegions i/o collection of painted regions
     */
    private void paintTicksAndLabels(Graphics2D g2d, TickInfo tickInfo, boolean paintLabel, boolean bold,
            Collection<Rectangle> paintedRegions)
    {
        if (paintLabel)
        {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        Rectangle timelinePanelBounds = getUIModel().getTimelinePanelBounds();
        int tickMaxY = AWTUtilities.getMaxY(timelinePanelBounds) - 1;
        int tickMinY = tickMaxY - (timelinePanelBounds.height >> (paintLabel ? 2 : 3));
        int labelY = AWTUtilities.getMaxY(getUIModel().getLabelPanelBounds()) - 2;

        Duration tickDuration = tickInfo.getIntervalDuration();
        TimeSpan uiSpan = getUIModel().getUISpan().get();
        for (Calendar cal = TimelineUtilities.roundDown(uiSpan.getStartDate(), tickDuration); cal.getTimeInMillis() <= uiSpan
                .getEnd(); tickDuration.addTo(cal))
        {
            long time = cal.getTimeInMillis();

            int x = getUIModel().timeToX(time);
            Duration dur = tickInfo.getLargestIntervalDuration(time);

            // Major tick for days/weeks/months/years.
            boolean isMajorTick = dur instanceof Days || dur instanceof Weeks || dur instanceof Months || dur instanceof Years;

            // Bold if this is a week/month/year boundary and bigger than the
            // tick duration.
            boolean isBold = bold || (dur instanceof Months || dur instanceof Weeks || dur instanceof Years)
                    && Duration.create(dur.getClass(), 1).compareTo(tickDuration) > 0;

            // Paint tick
            if (x >= 0)
            {
                g2d.drawLine(x, tickMinY, x, tickMaxY);
            }

            // Paint label
            if (paintLabel)
            {
                String label = tickInfo.format(time, dur);
                g2d.setFont(isBold ? BOLD_FONT : PLAIN_FONT);
                final Rectangle bounds = g2d.getFontMetrics().getStringBounds(label, g2d).getBounds();
                bounds.x = MathUtil.subtractSafe(x, (int)(bounds.getWidth() / 2));
                bounds.y = labelY;

                if (!paintedRegions.stream().anyMatch(rect -> rect.intersects(bounds)))
                {
                    paintedRegions.add(bounds);
                    Color color = g2d.getColor();
                    if (!isMajorTick)
                    {
                        g2d.setColor(color.darker());
                    }
                    g2d.drawString(label, bounds.x, bounds.y);
                    g2d.setColor(color);
                }
            }
        }
        if (paintLabel)
        {
            g2d.setFont(PLAIN_FONT);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
    }
}
