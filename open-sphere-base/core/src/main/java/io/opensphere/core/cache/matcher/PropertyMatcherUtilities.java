package io.opensphere.core.cache.matcher;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Utilities for property matchers.
 */
public final class PropertyMatcherUtilities
{
    /**
     * Build an interval property value set that comprises the simple bounds of
     * some interval matchers.
     *
     * @param intervalMatchers The interval matchers.
     * @return The simple bounds of the matchers.
     */
    public static IntervalPropertyValueSet buildBoundingIntervalSet(List<IntervalPropertyMatcher<?>> intervalMatchers)
    {
        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        for (IntervalPropertyMatcher<?> intervalPropertyMatcher : intervalMatchers)
        {
            Object simplifiedBounds = intervalPropertyMatcher.getSimplifiedBounds();
            List<?> list = builder.get(intervalPropertyMatcher.getPropertyDescriptor());
            if (!list.isEmpty())
            {
                simplifiedBounds = IntervalPropertyValueSet.getSimpleValueUnion(list.get(0), simplifiedBounds);
            }
            builder.put(intervalPropertyMatcher.getPropertyDescriptor(), simplifiedBounds);
        }
        return builder.create();
    }

    /**
     * Combine the properties from some interval parameters into an interval
     * property value set.
     *
     * @param intervalParams The interval parameters.
     * @return The interval property value set, or {@code null} if the
     *         {@code intervalParams} are {@code null} or empty.
     */
    public static IntervalPropertyValueSet buildIntervalPropertyValueSet(
            List<? extends IntervalPropertyMatcher<?>> intervalParams)
    {
        IntervalPropertyValueSet result;
        if (CollectionUtilities.hasContent(intervalParams))
        {
            IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
            for (IntervalPropertyMatcher<?> intervalPropertyMatcher : intervalParams)
            {
                builder.add(intervalPropertyMatcher.getPropertyDescriptor(), intervalPropertyMatcher.getMinimumOverlapInterval());
            }
            result = builder.create();
        }
        else
        {
            result = null;
        }
        return result;
    }

    /**
     * Create a group matcher.
     *
     * @param propertyDescriptor The property descriptor.
     * @param object The operand.
     * @return The matcher.
     */
    @SuppressWarnings("unchecked")
    public static IntervalPropertyMatcher<?> createGroupMatcher(PropertyDescriptor<?> propertyDescriptor, Object object)
    {
        if (object instanceof Geometry)
        {
            return new GeometryMatcher(propertyDescriptor.getPropertyName(), GeometryMatcher.OperatorType.INTERSECTS_NO_TOUCH,
                    (Geometry)object);
        }
        else if (object instanceof TimeSpan)
        {
            return new TimeSpanMatcher(propertyDescriptor.getPropertyName(), (TimeSpan)object);
        }
        else if (object instanceof Serializable)
        {
            return new GeneralIntervalPropertyMatcher<Serializable>((PropertyDescriptor<Serializable>)propertyDescriptor,
                    (Serializable)object);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported interval property value: " + object);
        }
    }

    /**
     * Extract the group matchers from some property matchers. The group
     * matchers will match the groups that contain the elements that satisfy the
     * property matchers.
     *
     * @param parameters The matchers.
     * @return The interval property matchers.
     */
    public static List<IntervalPropertyMatcher<?>> getGroupMatchers(Collection<? extends PropertyMatcher<?>> parameters)
    {
        if (parameters.isEmpty())
        {
            return Collections.emptyList();
        }

        List<IntervalPropertyMatcher<?>> result = New.list(parameters.size());
        @SuppressWarnings("rawtypes")
        Iterator<IntervalPropertyMatcher> iter = CollectionUtilities.filterDowncast(parameters.iterator(),
                IntervalPropertyMatcher.class);
        while (iter.hasNext())
        {
            result.add(iter.next().getGroupMatcher());
        }

        return result;
    }

    /**
     * Get some group matchers from an interval property value set.
     *
     * @param set The interval property value set.
     * @return The interval property matchers.
     */
    public static Collection<Collection<? extends IntervalPropertyMatcher<?>>> getGroupMatchers(IntervalPropertyValueSet set)
    {
        Collection<Collection<? extends IntervalPropertyMatcher<?>>> results = New.collection();
        List<PropertyDescriptor<?>> descs = New.list(set.getMap().size());
        List<Collection<?>> cols = New.list(set.getMap().size());
        List<Iterator<?>> iters = New.list(set.getMap().size());
        for (Entry<PropertyDescriptor<?>, List<? extends Object>> entry : set.getMap().entrySet())
        {
            descs.add(entry.getKey());
            cols.add(entry.getValue());
            iters.add(entry.getValue().iterator());
        }

        // Create the combinations of the objects.
        Object[] values = new Object[iters.size()];
        int index = 0;
        while (true)
        {
            if (index == iters.size())
            {
                // Create the matchers.
                Collection<IntervalPropertyMatcher<?>> result = New.collection(iters.size());
                for (index = 0; index < iters.size(); ++index)
                {
                    result.add(createGroupMatcher(descs.get(index), values[index]));
                }
                results.add(result);
                values[--index] = null;
            }
            else if (values[index] == null)
            {
                Iterator<?> iter = iters.get(index);
                if (iter == null)
                {
                    iter = cols.get(index).iterator();
                    iters.set(index, iter);
                }
                if (iter.hasNext())
                {
                    values[index++] = iter.next();
                }
                else if (index > 0)
                {
                    iters.set(index, null);
                    values[--index] = null;
                }
                else
                {
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Search a collection of property matchers to find one with the given
     * descriptor, and return its operand.
     *
     * @param <T> The parameter type.
     * @param parameters The collection of parameters.
     * @param desc The desired description.
     * @return The operand of the matching parameter.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOperand(Collection<? extends PropertyMatcher<?>> parameters, PropertyDescriptor<T> desc)
    {
        for (PropertyMatcher<?> param : parameters)
        {
            if (param.getPropertyDescriptor().equals(desc))
            {
                return (T)param.getOperand();
            }
        }

        return null;
    }

    /**
     * Split some property matchers into interval matchers and non-interval
     * matchers.
     *
     * @param parameters The input matchers.
     * @param intervalMatchers The optional return collection of interval
     *            matchers.
     * @param scalarMatchers The optional return collection of non-interval
     *            matchers.
     */
    public static void splitIntervalMatchers(Collection<? extends PropertyMatcher<?>> parameters,
            List<? super IntervalPropertyMatcher<?>> intervalMatchers, List<? super PropertyMatcher<?>> scalarMatchers)
    {
        if (parameters.isEmpty())
        {
            return;
        }

        for (PropertyMatcher<?> param : parameters)
        {
            if (param instanceof IntervalPropertyMatcher)
            {
                if (intervalMatchers != null)
                {
                    intervalMatchers.add((IntervalPropertyMatcher<?>)param);
                }
            }
            else
            {
                if (scalarMatchers != null)
                {
                    scalarMatchers.add(param);
                }
            }
        }
    }

    /** Disallow instantiation. */
    private PropertyMatcherUtilities()
    {
    }
}
