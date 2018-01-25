package io.opensphere.mantle.util;

import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * The Class InputStreamMonitorTaskActivity.
 */
public class InputStreamMonitorTaskActivity extends TaskActivity implements InputStreamMonitor
{
    /** The Determinate label prefix. */
    private final String myDeterminateLabelPrefix;

    /** The Indeterminate label. */
    private final String myIndeterminateLabel;

    /** The Is indeterminate. */
    private boolean myIsIndterminate;

    /** The Percent complete. */
    private double myPercentComplete;

    /**
     * Instantiates a new input stream monitor task activity.
     *
     * @param label the label
     */
    public InputStreamMonitorTaskActivity(String label)
    {
        this(label, label);
    }

    /**
     * Instantiates a new input stream monitor task activity.
     *
     * @param determinateLabelPrefix the determinate label prefix
     * @param indeterminateLabel the indeterminate label
     */
    public InputStreamMonitorTaskActivity(String determinateLabelPrefix, String indeterminateLabel)
    {
        super();
        myDeterminateLabelPrefix = determinateLabelPrefix;
        myIndeterminateLabel = indeterminateLabel;
        setActive(true);
        setLabelValue(indeterminateLabel);
    }

    @Override
    public void inputStreamClosed()
    {
        setComplete(true);
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public void monitorUpdate(final int totalRead, final int totalToRead)
    {
        EventQueueUtilities.runOnEDT(() -> monitorImpl(totalRead, totalToRead));
    }

    /**
     * Calculates the percent complete, and updates the label.
     *
     * @param totalRead the total number of items read to this point.
     * @param totalToRead the total number of items that need to be read in the batch.
     */
    private void monitorImpl(final int totalRead, final int totalToRead)
    {
        if (totalToRead <= 0)
        {
            myIsIndterminate = true;
        }
        else
        {
            myPercentComplete = (double)totalRead / (double)totalToRead * 100;
        }
        updateLabelValue();
    }

    /**
     * Update label value.
     */
    private void updateLabelValue()
    {
        if (myIsIndterminate)
        {
            setLabelValue(myIndeterminateLabel);
        }
        else
        {
            String perComplete = String.format("%-10.1f", myPercentComplete).trim();
            setLabelValue(myDeterminateLabelPrefix + " " + perComplete + "%");
        }
    }
}
