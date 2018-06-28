package io.opensphere.core.quantify.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple metric used to track the number of times some key has occurred.
 */
public class Metric
{
    /** The key to track. */
    private final String myName;

    /** The number of times that the key occurred. */
    private AtomicInteger myValue;

    /**
     * Creates a new metric bound to the supplied key, with a zero instance
     * count.
     *
     * @param name the key for which metrics will be tracked.
     */
    public Metric(String name)
    {
        myName = name;
        myValue = new AtomicInteger(0);
    }

    /**
     * Gets the value of the name ({@link #myName}) field.
     *
     * @return the value stored in the {@link #myName} field.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the value of the value ({@link #myValue}) field.
     *
     * @return the value stored in the {@link #myValue} field.
     */
    public int getValue()
    {
        return myValue.get();
    }

    /**
     * Increments the value of the count ({@link #myValue}) field.
     *
     * @return the updated value stored in the {@link #myValue} field.
     */
    public int increment()
    {
        return myValue.incrementAndGet();
    }
}
