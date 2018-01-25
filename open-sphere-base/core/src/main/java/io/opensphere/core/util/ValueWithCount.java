package io.opensphere.core.util;

/**
 * Holds a value with associated count.
 *
 * @param <T> The type of the value.
 */
public class ValueWithCount<T>
{
    /** The value. */
    private T myValue;

    /** The count. */
    private int myCount;

    /**
     * Constructor.
     */
    public ValueWithCount()
    {
    }

    /**
     * Constructor.
     *
     * @param value the value
     * @param count the count
     */
    public ValueWithCount(T value, int count)
    {
        myValue = value;
        myCount = count;
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
     * Gets the value.
     *
     * @return the value
     */
    public T getValue()
    {
        return myValue;
    }

    /**
     * Sets the count.
     *
     * @param count the count
     */
    public void setCount(int count)
    {
        myCount = count;
    }

    /**
     * Sets the value.
     *
     * @param value the value
     */
    public void setValue(T value)
    {
        myValue = value;
    }

    @Override
    public String toString()
    {
        return "ValueWithCount [value=" + myValue + ", count=" + myCount + "]";
    }
}
