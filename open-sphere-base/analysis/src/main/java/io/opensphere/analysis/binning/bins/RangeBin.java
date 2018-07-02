package io.opensphere.analysis.binning.bins;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import net.jcip.annotations.NotThreadSafe;

import org.apache.log4j.Logger;

import io.opensphere.analysis.util.DataTypeUtilities;

/**
 * A bin that accepts values in a given range.
 *
 * @param <T> the type of the data in the bin
 */
@NotThreadSafe
public class RangeBin<T> extends AbstractBin<T> implements Comparable<RangeBin<T>>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RangeBin.class);

    /** The minimum value (inclusive). */
    private final double myMin;

    /** The maximum value (exclusive). */
    private final double myMax;

    /** The value representing the bin. */
    private final Object myValue;

    /** The function to convert data => value. */
    private final Function<T, ?> myDataToValue;

    /** The function to convert value => double. */
    private final ToDoubleFunction<Object> myValueToDouble;

    /**
     * Constructor.
     *
     * @param min The minimum value (inclusive)
     * @param max The maximum value (exclusive)
     * @param value The value representing the bin
     * @param dataToValue The function to convert data => value
     * @param valueToDouble The function to convert value => double
     */
    public RangeBin(double min, double max, Object value, Function<T, ?> dataToValue, ToDoubleFunction<Object> valueToDouble)
    {
        super();
        myMin = min;
        myMax = max;
        myValue = value;
        myDataToValue = dataToValue;
        myValueToDouble = valueToDouble;
    }

    @Override
    public boolean accepts(T data)
    {
        boolean accepts = false;
        try
        {
            Object value = myDataToValue.apply(data);
            if (value != null && myValue != null)
            {
                double doubleValue = myValueToDouble.applyAsDouble(value);
                accepts = doubleValue >= myMin && doubleValue < myMax;
            }
            else if (value == null && myValue == null)
            {
                accepts = true;
            }
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error(e, e);
            accepts = false;
        }
        return accepts;
    }

    @Override
    public Object getValueObject()
    {
        return myValue;
    }

    /**
     * Gets the min.
     *
     * @return the min
     */
    public double getMin()
    {
        return myMin;
    }

    /**
     * Gets the max.
     *
     * @return the max
     */
    public double getMax()
    {
        return myMax;
    }

    @Override
    public String toString()
    {
        return DataTypeUtilities.getLabel(myValue);
    }

    @Override
    public int compareTo(RangeBin<T> o)
    {
        int result;
        if (myValue == null)
        {
            result = 1;
        }
        else if (o.myValue == null)
        {
            result = -1;
        }
        else
        {
            result = Double.compare(myMin, o.myMin);
        }
        return result;
    }
}
