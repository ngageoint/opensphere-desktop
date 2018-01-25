package io.opensphere.mantle.data.element.mdfilter.impl;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

import io.opensphere.core.util.DateTimeUtilities;

/** Evaluator for the BETWEEN condition. */
public class BetweenEvaluator extends AbstractEvaluator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(BetweenEvaluator.class);

    /** The minimum value. */
    private final double myMin;

    /** The maximum value. */
    private final double myMax;

    /**
     * Constructor.
     *
     * @param value The value to be evaluated.
     */
    public BetweenEvaluator(Object value)
    {
        super(value);

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        String[] tokens = value.toString().split(" - ");
        try
        {
            min = Double.parseDouble(tokens[0]);
            max = Double.parseDouble(tokens[1]);
        }
        catch (NumberFormatException e)
        {
            try
            {
                min = DateTimeUtilities.parseISO8601Date(tokens[0]).getTime();
                max = DateTimeUtilities.parseISO8601Date(tokens[1]).getTime();
            }
            catch (ParseException e1)
            {
                LOGGER.error(e1);
            }
        }
        myMin = min;
        myMax = max;
    }

    @Override
    public boolean evaluate(Object dataValue)
    {
        Double valueToEvaluate = ComparisonEvaluator.getAsDouble(dataValue);
        if (valueToEvaluate != null)
        {
            double doubleValue = valueToEvaluate.doubleValue();
            return doubleValue >= myMin && doubleValue < myMax;
        }
        Date dateToEvaluate = getAsDate(dataValue);
        if (dateToEvaluate != null)
        {
            double doubleValue = dateToEvaluate.getTime();
            return doubleValue >= myMin && doubleValue < myMax;
        }
        return false;
    }
}
