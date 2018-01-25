package io.opensphere.analysis.util;

/** A simple mutable integer class. */
public class MutableInteger
{
    /** The value. */
    private int myValue;

    /** Increments the value. */
    public void increment()
    {
        ++myValue;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public int get()
    {
        return myValue;
    }
}
