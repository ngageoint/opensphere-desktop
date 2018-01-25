package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Calendar;

import io.opensphere.controlpanels.timeline.AbstractTimelineLayer;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * A timeline layer that shows the data loading boundaries.
 */
class DataLoadBoundaryLayer extends AbstractTimelineLayer
{
    /** The selected data load duration. */
    private final ObservableValue<Duration> mySelectedDataLoadDuration;

    /**
     * Constructor.
     *
     * @param selectedDataLoadDuration The selected data load duration
     */
    public DataLoadBoundaryLayer(ObservableValue<Duration> selectedDataLoadDuration)
    {
        super();
        mySelectedDataLoadDuration = selectedDataLoadDuration;
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        Duration dataLoadDuration = mySelectedDataLoadDuration.get();
        if (dataLoadDuration != null)
        {
            final double minPixels = 6.;
            if (dataLoadDuration
                    .compareTo(new Milliseconds(minPixels * getUIModel().getMillisPerPixel().get().doubleValue())) > 0)
            {
                g2d.setColor(Color.GRAY.brighter());

                int minY = getUIModel().getTimelinePanelBounds().y + 1;
                int maxY = AWTUtilities.getMaxY(getUIModel().getTimelinePanelBounds()) - 1;

                TimeSpan uiSpan = getUIModel().getUISpan().get();
                Calendar startDate = TimelineUtilities.roundDown(uiSpan.getStartDate(), dataLoadDuration);
                long time;
                for (Calendar cal = startDate; (time = cal.getTimeInMillis()) <= uiSpan.getEnd(); dataLoadDuration.addTo(cal))
                {
                    int x = getUIModel().timeToX(time);
                    g2d.drawLine(x, minY, x, maxY);
                }
            }
        }
    }
}
