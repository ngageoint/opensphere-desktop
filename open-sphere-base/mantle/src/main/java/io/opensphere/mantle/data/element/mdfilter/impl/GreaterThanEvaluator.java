package io.opensphere.mantle.data.element.mdfilter.impl;

/**
 * The Class GreaterThanEvaluator.
 */
public class GreaterThanEvaluator extends ComparisonEvaluator
{
    /** Whether to include equality in the comparison. */
    private final boolean orEqual;

    /**
     * Instantiates a new greater than evaluator.
     *
     * @param value the value
     * @param allowEquality Whether to include equality in the comparison
     */
    public GreaterThanEvaluator(Object value, boolean allowEquality)
    {
        super(value);
        orEqual = allowEquality;
    }

    @Override
    public boolean acceptCompareTo(int compareToValue)
    {
        if (orEqual)
        {
            return compareToValue >= 0;
        }
        return compareToValue > 0;
    }

    @Override
    public boolean compareAsNumber(Number valueToEvaluate)
    {
        if (orEqual)
        {
            return valueToEvaluate.doubleValue() >= getValueAsDouble().doubleValue();
        }
        return valueToEvaluate.doubleValue() > getValueAsDouble().doubleValue();
    }
}
