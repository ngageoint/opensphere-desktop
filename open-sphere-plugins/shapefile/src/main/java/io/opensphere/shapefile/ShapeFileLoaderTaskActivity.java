package io.opensphere.shapefile;

import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * The Class ShapeFileLoaderTaskActivity.
 */
public class ShapeFileLoaderTaskActivity extends TaskActivity
{
    /**
     * Instantiates a new shape file loader task activity.
     */
    public ShapeFileLoaderTaskActivity()
    {
        super();
    }

    /**
     * Sets the progress.
     *
     * @param value the value
     * @param total the total
     */
    public void setProgress(final int value, final int total)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                StringBuilder sb = new StringBuilder(18);
                sb.append("Loading Shapefile");
                if (total > 0)
                {
                    double percent = (double)value / (double)total * 100;
                    String perComplete = String.format("%-10.1f", Double.valueOf(percent)).trim();
                    sb.append(' ');
                    sb.append(perComplete).append('%');
                }
                setLabelValue(sb.toString());
            }
        });
    }
}
