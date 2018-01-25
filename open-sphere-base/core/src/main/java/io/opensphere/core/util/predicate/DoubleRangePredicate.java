package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

/**
 * A predicate that accepts doubles with an inclusive range.
 */
public class DoubleRangePredicate implements Predicate<Double>
{
    /** The minimum value. */
    private final double myMin;

    /** The maximum value. */
    private final double myMax;

    /**
     * Constructor.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     */
    public DoubleRangePredicate(double min, double max)
    {
        myMin = min;
        myMax = max;
    }

    @Override
    public boolean test(Double input)
    {
        if (input == null)
        {
            return false;
        }
        double val = input.doubleValue();
        return myMin <= val && val <= myMax;
    }
}
