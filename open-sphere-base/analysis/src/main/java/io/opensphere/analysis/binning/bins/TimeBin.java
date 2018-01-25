package io.opensphere.analysis.binning.bins;

import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.concurrent.NotThreadSafe;

import io.opensphere.analysis.binning.criteria.TimeBinType;
import io.opensphere.core.util.Utilities;

/**
 * A bin that accepts values in a given range.
 *
 * @param <T> the type of the data in the bin
 */
@NotThreadSafe
public class TimeBin<T> extends AbstractBin<T>
{
    /** The bin type. */
    private final TimeBinType myBinType;

    /** The value representing the bin. */
    private final Date myDate;

    /** The function to convert data => value. */
    private final Function<T, Date> myDataToValue;

    /** The bin value object. */
    private final Object myValueObject;

    /**
     * Constructor.
     *
     * @param binType The bin type
     * @param value The value representing the bin
     * @param dataToValue The function to convert data => value
     */
    public TimeBin(TimeBinType binType, Date value, Function<T, Date> dataToValue)
    {
        super();
        myBinType = binType;
        myDate = Utilities.clone(value);
        myDataToValue = dataToValue;
        myValueObject = getValue(value, binType);
    }

    @Override
    public boolean accepts(T data)
    {
        Object dataValue = getValue(myDataToValue.apply(data), myBinType);
        return Objects.equals(myValueObject, dataValue);
    }

    @Override
    public Object getValueObject()
    {
        return myValueObject;
    }

    @Override
    public String toString()
    {
        return myDate != null ? myBinType.getLabel(myDate) : "NONE";
    }

    /**
     * Gets the binType.
     *
     * @return the binType
     */
    public TimeBinType getBinType()
    {
        return myBinType;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate()
    {
        return myDate;
    }

    /**
     * Gets the value object for the given date.
     *
     * @param date the date
     * @param binType The bin type
     * @return the value object
     */
    public static Object getValue(Date date, TimeBinType binType)
    {
        Object binValue = null;
        if (date != null)
        {
            if (binType == TimeBinType.UNIQUE)
            {
                binValue = date;
            }
            else if (binType.isPeriod())
            {
                binValue = Integer.valueOf(binType.getPeriodicValue(date));
            }
            else
            {
                binValue = Long.valueOf(binType.getMin(date));
            }
        }
        return binValue;
    }
}
