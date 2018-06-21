package io.opensphere.analysis.binning.bins;

import java.util.Objects;
import java.util.function.Function;

import net.jcip.annotations.NotThreadSafe;

import io.opensphere.analysis.util.DataTypeUtilities;

/**
 * A bin that accepts unique values.
 *
 * @param <T> the type of the data in the bin
 */
@NotThreadSafe
public class UniqueValueBin<T> extends AbstractBin<T>
{
    /** The value in the bin. */
    private final Object myValue;

    /** The function to get the value for comparison. */
    private final Function<T, ?> myValueFunction;

    /**
     * Constructor.
     *
     * @param value The value in the bin
     * @param valueFunction The function to get the value for comparison
     */
    public UniqueValueBin(Object value, Function<T, ?> valueFunction)
    {
        super();
        myValue = value;
        myValueFunction = valueFunction;
    }

    @Override
    public boolean accepts(T data)
    {
        Object value = myValueFunction.apply(data);
        return Objects.equals(myValue, value);
    }

    @Override
    public Object getValueObject()
    {
        return myValue;
    }

    @Override
    public String toString()
    {
        return DataTypeUtilities.getLabel(myValue);
    }
}
