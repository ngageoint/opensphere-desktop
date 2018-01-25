package io.opensphere.core.util.taskactivity;

import io.opensphere.core.util.concurrent.CommonTimer;

/**
 * A simple example {@link TaskActivity} that displays a fixed message for a set
 * period then completes and goes away.
 */
public class SimpleTimeoutTaskActivity extends TaskActivity
{
    /**
     * A simple TaskActivity that provides a string label and a timeout.
     *
     * @param label - The label to display initially
     * @param longevityMS - the amount of time in milliseconds this TaskActivity
     *            message should be shown.
     */
    public SimpleTimeoutTaskActivity(String label, int longevityMS)
    {
        super();
        setActive(true);
        setLabelValue(label);
        CommonTimer.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                setComplete(true);
            }
        }, longevityMS);
    }
}
