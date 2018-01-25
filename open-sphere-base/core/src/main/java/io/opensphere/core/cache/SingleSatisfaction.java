package io.opensphere.core.cache;

import java.util.Collection;

import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.collections.New;

/**
 * A satisfaction object that contains a single interval property value set.
 */
public class SingleSatisfaction implements Satisfaction
{
    /** The interval property value set. */
    private final IntervalPropertyValueSet myIntervalPropertyValueSet;

    /**
     * Generate a collection of satisfactions from a collection of interval
     * property value sets.
     *
     * @param intervalPropertyValueSets The interval property value sets.
     * @return The satisfactions.
     */
    public static Collection<? extends Satisfaction> generateSatisfactions(
            Collection<? extends IntervalPropertyValueSet> intervalPropertyValueSets)
    {
        Collection<Satisfaction> results = New.collection(intervalPropertyValueSets.size());
        for (final IntervalPropertyValueSet set : intervalPropertyValueSets)
        {
            results.add(new SingleSatisfaction(set));
        }
        return results;
    }

    /**
     * Constructor.
     *
     * @param intervalPropertyValueSet The wrapped interval property value set.
     */
    public SingleSatisfaction(IntervalPropertyValueSet intervalPropertyValueSet)
    {
        myIntervalPropertyValueSet = intervalPropertyValueSet;
    }

    @Override
    public IntervalPropertyValueSet getIntervalPropertyValueSet()
    {
        return myIntervalPropertyValueSet;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(64).append(getClass().getSimpleName()).append('[').append(myIntervalPropertyValueSet).append(']')
                .toString();
    }
}
