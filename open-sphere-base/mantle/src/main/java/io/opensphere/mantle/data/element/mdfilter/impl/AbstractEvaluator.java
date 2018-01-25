package io.opensphere.mantle.data.element.mdfilter.impl;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.Utilities;

/** The Class AbstractEvaluator. */
public abstract class AbstractEvaluator implements Evaluator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractEvaluator.class);

    /** The value to compare against the data. */
    private final Object myValue;

    /** The value as double. */
    private Double myValueAsDouble;

    /** The value as string. */
    private final String myValueAsString;

    /**
     * Instantiates a new abstract evaluator.
     *
     * @param value the value to compare against the data
     */
    public AbstractEvaluator(Object value)
    {
        Utilities.checkNull(value, "value");
        myValue = value;
        myValueAsString = value.toString();

        try
        {
            myValueAsDouble = Double.valueOf(myValueAsString);
        }
        catch (NumberFormatException e)
        {
            myValueAsDouble = null;
        }
    }

    /**
     * Gets the value as a date using the specified format.
     *
     * @return the value as a date
     */
    public Date getAsDate()
    {
        try
        {
            return DateTimeUtilities.parseISO8601Date(getValueAsString());
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    /**
     * Gets the value to compare against the data.
     *
     * @return the value to compare against the data
     */
    public Object getValue()
    {
        return myValue;
    }

    /**
     * Gets the value as double.
     *
     * @return the value as double
     */
    public Double getValueAsDouble()
    {
        return myValueAsDouble;
    }

    /**
     * Gets the value as string.
     *
     * @return the value as string
     */
    public String getValueAsString()
    {
        return myValueAsString;
    }

    /**
     * Converts the value to a Date, if possible.
     *
     * @param value the value
     * @return the equivalent Date, or null
     */
    public static Date getAsDate(Object value)
    {
        Date date = null;
        if (value instanceof Date)
        {
            date = (Date)value;
        }
        else if (value instanceof TimeSpan && !((TimeSpan)value).isUnboundedStart())
        {
            date = ((TimeSpan)value).getStartDate();
        }
        else if (value instanceof String)
        {
            try
            {
                date = DateTimeUtilities.parseISO8601Date((String)value);
            }
            catch (ParseException e)
            {
                LOGGER.trace(e);
            }
        }
        return date;
    }
}
