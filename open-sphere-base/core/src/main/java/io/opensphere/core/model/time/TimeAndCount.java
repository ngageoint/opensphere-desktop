package io.opensphere.core.model.time;

/**
 * The Class TimeAndCount result bean.
 */
public class TimeAndCount
{
    /** The Count. */
    private final int myCount;

    /** The Time. */
    private final TimeSpan myTime;

    /**
     * Instantiates a new time and count.
     *
     * @param time the time
     * @param count the count
     */
    public TimeAndCount(TimeSpan time, int count)
    {
        super();
        myCount = count;
        myTime = time;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount()
    {
        return myCount;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public TimeSpan getTime()
    {
        return myTime;
    }
}
