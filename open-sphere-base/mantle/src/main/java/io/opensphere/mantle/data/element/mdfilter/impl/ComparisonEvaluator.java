package io.opensphere.mantle.data.element.mdfilter.impl;

import java.util.Date;

/** The Class ComparisonEvaluator. */
public abstract class ComparisonEvaluator extends AbstractEvaluator
{
    /**
     * Gets the as double, or null if can't be converted. Null is interpreted as
     * 0.0.
     *
     * @param value the value
     * @return the as double
     */
    public static Double getAsDouble(Object value)
    {
        if (value == null)
        {
            return Double.valueOf(0.0);
        }
        if (value instanceof Number)
        {
            return Double.valueOf(((Number)value).doubleValue());
        }
        try
        {
            return Double.valueOf(Double.parseDouble(value.toString()));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /**
     * Instantiates a new comparison evaluator.
     *
     * @param value the value
     */
    public ComparisonEvaluator(Object value)
    {
        super(value);
    }

    /**
     * Determines if the 'compare to' value evaluates to true for this
     * evaluator.
     *
     * @param compareToValue the compare to value
     * @return true, if successful
     */
    public abstract boolean acceptCompareTo(int compareToValue);

    /**
     * Compare as number.
     *
     * @param valueToEvaluate the value to evaluate
     * @return true, if successful
     */
    public abstract boolean compareAsNumber(Number valueToEvaluate);

    @Override
    public boolean evaluate(Object rhs)
    {
        Double dValToEvaluate = getAsDouble(rhs);
        if (dValToEvaluate != null && getValueAsDouble() != null)
        {
            return compareAsNumber(dValToEvaluate);
        }
        Date dateToEvaluate = getAsDate(rhs);
        if (dateToEvaluate != null)
        {
            Date aDate = getAsDate();
            if (aDate != null)
            {
                return acceptCompareTo(dateToEvaluate.compareTo(aDate));
            }
        }
        String lhsString = getValueAsString();
        if (lhsString != null)
        {
            String strVal = rhs == null ? "" : rhs.toString();
            return acceptCompareTo(strVal.compareTo(lhsString));
        }
        return true;
    }
}
