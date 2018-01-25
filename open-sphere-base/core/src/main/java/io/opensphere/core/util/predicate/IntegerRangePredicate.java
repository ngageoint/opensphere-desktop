package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

/**
 * A predicate that accepts integer with an inclusive range.
 */
public class IntegerRangePredicate implements Predicate<Integer>
{
    /** The minimum value. */
    private final int myMin;

    /** The maximum value. */
    private final int myMax;

    /**
     * Constructor.
     *
     * @param min The minimum value.
     * @param max The maximum value.
     */
    public IntegerRangePredicate(int min, int max)
    {
        myMin = min;
        myMax = max;
    }

    @Override
    public boolean test(Integer input)
    {
        if (input == null)
        {
            return false;
        }
        int intVal = input.intValue();
        return myMin <= intVal && intVal <= myMax;
    }
}
