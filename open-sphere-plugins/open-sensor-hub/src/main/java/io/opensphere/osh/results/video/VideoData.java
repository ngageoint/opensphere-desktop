package io.opensphere.osh.results.video;

/**
 * Video data.
 */
public class VideoData
{
    /** The time. */
    private long myTime;

    /** The data. */
    private byte[] myData;

    /**
     * Constructor.
     */
    public VideoData()
    {
    }

    /**
     * Constructor.
     *
     * @param time The time
     * @param data The data
     */
    public VideoData(long time, byte[] data)
    {
        myTime = time;
        myData = data;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime()
    {
        return myTime;
    }

    /**
     * Sets the time.
     *
     * @param time the time
     */
    public void setTime(long time)
    {
        myTime = time;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public byte[] getData()
    {
        return myData;
    }

    /**
     * Sets the data.
     *
     * @param data the data
     */
    public void setData(byte[] data)
    {
        myData = data;
    }

    /**
     * Resets the object to a clean state.
     */
    public void reset()
    {
        myTime = 0;
        myData = null;
    }

    /**
     * Sets this object to be equal to the given one.
     *
     * @param data the data
     */
    public void setEqual(VideoData data)
    {
        myTime = data.myTime;
        myData = data.myData;
    }

    /**
     * Returns whether the object has data.
     *
     * @return whether the object has data
     */
    public boolean hasData()
    {
        return myTime != 0 && myData != null;
    }
}
