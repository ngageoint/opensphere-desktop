package io.opensphere.core.image;

/**
 * An object that can be used to keep track of metrics related to reading and
 * processing images.
 */
public class ImageMetrics
{
    /** The number of nanoseconds it took to decompress an image. */
    private long myDecodeTimeNanoseconds = -1L;

    /** The number of nanoseconds it took to encode an image. */
    private long myEncodeTimeNanoseconds = -1L;

    /**
     * Get the number of nanoseconds it took to decode an image.
     *
     * @return The nanoseconds.
     */
    public long getDecodeTimeNanoseconds()
    {
        return myDecodeTimeNanoseconds;
    }

    /**
     * Get the number of nanoseconds it took to encode an image.
     *
     * @return The nanoseconds.
     */
    public long getEncodeTimeNanoseconds()
    {
        return myEncodeTimeNanoseconds;
    }

    /**
     * Set the number of nanoseconds it took to decode an image.
     *
     * @param ns The nanoseconds.
     */
    public void setDecodeTimeNanoseconds(long ns)
    {
        myDecodeTimeNanoseconds = ns;
    }

    /**
     * Set the number of nanoseconds it took to encode an image.
     *
     * @param ns The nanoseconds.
     */
    public void setEncodeTimeNanoseconds(long ns)
    {
        myEncodeTimeNanoseconds = ns;
    }
}
