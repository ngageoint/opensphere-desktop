package io.opensphere.csvcommon.detect;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import io.opensphere.core.util.collections.New;

/**
 * Holds some values with associated confidences.
 *
 * @param <T> The type of the values.
 */
@NotThreadSafe
public class ValuesWithConfidence<T>
{
    /** The value. */
    private final List<ValueWithConfidence<? extends T>> myValues = New.list();

    /** Comparator that orders values by confidence. */
    private static final Comparator<ValueWithConfidence<?>> COMPARATOR = (o1, o2) -> Float.compare(o1.getConfidence(),
            o2.getConfidence());

    /**
     * Constructor.
     */
    public ValuesWithConfidence()
    {
    }

    /**
     * Constructor that takes a single value and confidence pair.
     *
     * @param value the value
     * @param confidence the confidence (0.0 to 1.0)
     */
    public ValuesWithConfidence(T value, float confidence)
    {
        this(new ValueWithConfidence<T>(value, confidence));
    }

    /**
     * Constructor that takes a single value and confidence pair.
     *
     * @param value the value/confidence pair
     */
    public ValuesWithConfidence(ValueWithConfidence<? extends T> value)
    {
        myValues.add(value);
    }

    /**
     * Pseudo copy constructor.
     *
     * @param other The other values.
     */
    public ValuesWithConfidence(ValuesWithConfidence<? extends T> other)
    {
        for (ValueWithConfidence<? extends T> pair : other.getValues())
        {
            myValues.add(pair);
        }
    }

    /**
     * Add a value/confidence pair to my collection.
     *
     * @param value The value/confidence pair.
     */
    public void add(ValueWithConfidence<? extends T> value)
    {
        myValues.add(value);
        Collections.sort(myValues, COMPARATOR);
    }

    /**
     * Get the confidence of the value with the highest confidence.
     *
     * @return The confidence.
     */
    public float getBestConfidence()
    {
        return myValues.isEmpty() ? 0f : myValues.get(0).getConfidence();
    }

    /**
     * Get the value with the highest confidence.
     *
     * @return The value.
     */
    public T getBestValue()
    {
        return myValues.isEmpty() ? null : myValues.get(0).getValue();
    }

    /**
     * Get a copy of my values, with the highest confidence values first.
     *
     * @return The values with confidence.
     */
    public List<ValueWithConfidence<? extends T>> getValues()
    {
        return New.unmodifiableList(myValues);
    }

    /**
     * Remove a value/confidence pair from my collection.
     *
     * @param value The value/confidence pair.
     * @return {@code true} if the value was removed.
     */
    public boolean remove(Object value)
    {
        return myValues.remove(value);
    }
}
