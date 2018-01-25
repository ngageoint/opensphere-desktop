package io.opensphere.core.util.taskactivity;

/**
 * A {@link TaskActivity} that maintains a count.
 */
public class CountingTaskActivity extends TaskActivity
{
    /** The current count. */
    private int myCount;

    /** The label. */
    private final String myLabel;

    /**
     * Constructor.
     *
     * @param prefix The prefix that the count will be appended to, to make up
     *            the task activity label.
     */
    public CountingTaskActivity(String prefix)
    {
        myLabel = prefix;
    }

    /** Decrement the count. */
    public synchronized void decrementCount()
    {
        if (--myCount <= 0)
        {
            setActive(false);
        }
        setLabel();
    }

    /** Increment the count. */
    public synchronized void incrementCount()
    {
        if (++myCount > 0)
        {
            setActive(true);
        }
        setLabel();
    }

    /**
     * Set the current count and update the label.
     *
     * @param count The count.
     */
    public synchronized void setCount(int count)
    {
        myCount = count;
        if (myCount > 0)
        {
            setActive(true);
        }
        setLabel();
    }

    /**
     * Set the current label value.
     */
    private void setLabel()
    {
        setLabelValue(myLabel + myCount);
    }
}
