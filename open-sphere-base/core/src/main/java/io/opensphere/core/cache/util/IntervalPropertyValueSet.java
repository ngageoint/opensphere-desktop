package io.opensphere.core.cache.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Represents a set of interval property values. This may comprise multiple
 * property types, as well as multiple disjoint values for each property type.
 * <p>
 * For example, a single {@link IntervalPropertyValueSet} could comprise the
 * time intervals (0:00, 1:00) and (1:30, 2:30), as well as the bounding boxes
 * (0, 0, 10, 10) and (20, 20, 40, 40). A model with time of 0:30 and space of
 * (5, 5) would be considered a part of this set. A model with time of 0:30 and
 * space of (15, 15) would not be part of this set, since (15, 15) is not part
 * of any of the bounding boxes in the set. A model with time of 1:15 and space
 * of (5, 5) would also not be part of the set, since the time 1:15 is not
 * within any of the time intervals.
 */
@SuppressWarnings("PMD.GodClass")
public class IntervalPropertyValueSet
{
    /** A map of property descriptors to lists of values. */
    private final Map<PropertyDescriptor<?>, List<? extends Object>> myMap;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(IntervalPropertyValueSet.class);

    /**
     * Determine the simple union of two intervals. The intervals must either
     * both be {@link TimeSpan}s or {@link Geometry}s. If the values are
     * disjoint, the result may contain the space between them.
     *
     * @param <T> The type of interval.
     * @param value1 The first value.
     * @param value2 The second value.
     * @return The simple union of the values.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSimpleValueUnion(T value1, T value2)
    {
        if (value1 instanceof TimeSpan && value2 instanceof TimeSpan)
        {
            return (T)((TimeSpan)value1).simpleUnion((TimeSpan)value2);
        }
        else if (value1 instanceof Geometry && value2 instanceof Geometry)
        {
            return (T)((Geometry)value1).getEnvelope().union((Geometry)value2).getEnvelopeInternal();
        }
        else
        {
            throw createIllegalArgumentException(value1, value2);
        }
    }

    /**
     * Determine the intersection of two intervals. The intervals must either
     * both be {@link TimeSpan}s or {@link Geometry}s to get the true
     * intersection; otherwise it will be all or nothing.
     *
     * @param <T> The type of interval.
     * @param value1 The first value.
     * @param value2 The second value.
     * @return The intersection.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValueIntersection(T value1, T value2)
    {
        if (value1 instanceof TimeSpan && value2 instanceof TimeSpan)
        {
            return (T)((TimeSpan)value1).getIntersection((TimeSpan)value2);
        }
        else if (value1 instanceof Geometry && value2 instanceof Geometry)
        {
            if (((Geometry)value1).touches((Geometry)value2))
            {
                return null;
            }
            T intersection = (T)((Geometry)value1).intersection((Geometry)value2);
            return ((Geometry)intersection).isEmpty() ? null : intersection;
        }
        else
        {
            return Objects.equals(value1, value2) ? value1 : null;
        }
    }

    /**
     * Subtract some interval property value sets from some other interval
     * property value sets.
     *
     * @param operands The property values sets from which the subtraction is to
     *            occur.
     * @param subtrahends The property value sets being subtracted.
     * @return {@code true} if there was any overlap.
     */
    public static boolean subtract(List<IntervalPropertyValueSet> operands,
            Collection<? extends IntervalPropertyValueSet> subtrahends)
    {
        boolean result = false;
        ListIterator<IntervalPropertyValueSet> resultIter = operands.listIterator();
        for (IntervalPropertyValueSet subtrahend : subtrahends)
        {
            result |= doSubtract(resultIter, subtrahend);
        }
        return result;
    }

    /**
     * Subtract an interval property value set from a collection of interval
     * property value sets.
     *
     * @param operands The property values sets from which the subtraction is to
     *            occur.
     * @param subtrahend The property value set being subtracted.
     * @return If any operands were modified.
     */
    public static boolean subtract(List<IntervalPropertyValueSet> operands, IntervalPropertyValueSet subtrahend)
    {
        ListIterator<IntervalPropertyValueSet> resultIter = operands.listIterator();
        return doSubtract(resultIter, subtrahend);
    }

    /**
     * Determine if two intervals intersect. The intervals must either both be
     * {@link TimeSpan}s or {@link Geometry}s to check true intersection;
     * otherwise this will return {@code true} only if the intervals are equal.
     * If the intervals simply touch, they do not overlap.
     *
     * @param value1 The first value.
     * @param value2 The second value.
     * @return {@code true} if the values intersect.
     */
    public static boolean valuesIntersect(Object value1, Object value2)
    {
        if (value1 instanceof TimeSpan && value2 instanceof TimeSpan)
        {
            return ((TimeSpan)value1).overlaps((TimeSpan)value2);
        }
        else if (value1 instanceof Geometry && value2 instanceof Geometry)
        {
            if (((Geometry)value1).touches((Geometry)value2))
            {
                return false;
            }
            return ((Geometry)value1).intersects((Geometry)value2);
        }
        else
        {
            return Objects.equals(value1, value2);
        }
    }

    /**
     * Helper method to check if two objects are equal. This was needed due to
     * some weirdness of JTS Geometry not using the proper equals. If the values
     * are of type Geometry they are treated special, otherwise standard equals
     * is used.
     *
     * @param value1 The first value to test.
     * @param value2 The second value to test.
     * @return True if the two values are equal, false otherwise.
     */
    private static boolean checkEquality(Object value1, Object value2)
    {
        if (value1 instanceof Geometry && value2 instanceof Geometry)
        {
            return ((Geometry)value1).equals((Geometry)value2);
        }
        return value1.equals(value2);
    }

    /**
     * Throw an illegal argument exception indicating the values are unknown
     * types or not the same type.
     *
     * @param value1 A value.
     * @param value2 A value.
     * @return The exception.
     */
    private static IllegalArgumentException createIllegalArgumentException(Object value1, Object value2)
    {
        return new IllegalArgumentException("Unsupported value types: " + value1.getClass() + ", " + value2.getClass());
    }

    /**
     * Helper method that subtracts an interval property value set from a
     * collection of interval property value sets, given a list iterator over
     * the operand. The operand is modified by this method if there is any
     * overlap.
     *
     * @param resultIter A list iterator over the result.
     * @param subtrahend The property value set being subtracted.
     * @return If the operand was modified.
     */
    private static boolean doSubtract(ListIterator<IntervalPropertyValueSet> resultIter, IntervalPropertyValueSet subtrahend)
    {
        boolean result = false;
        while (resultIter.hasNext())
        {
            IntervalPropertyValueSet operand = resultIter.next();
            List<IntervalPropertyValueSet> remains;
            try
            {
                remains = operand.subtract(subtrahend);
            }
            catch (TopologyException e)
            {
                LOGGER.error("Failed to subtract " + subtrahend + " from " + operand + ": " + e, e);
                continue;
            }
            resultIter.remove();
            for (IntervalPropertyValueSet remnant : remains)
            {
                resultIter.add(remnant);
            }
            result |= remains.size() != 1 || !Utilities.sameInstance(remains.get(0), operand);
        }

        return result;
    }

    /**
     * Determine the complement of one list of values with respect to another.
     *
     * @param values1 The values whose complement is returned.
     * @param values2 The values to restrict the complement.
     * @return The complement of {@code values1} with respect to {@code values2}
     *         .
     */
    private static List<Object> getComplement(List<?> values1, List<?> values2)
    {
        List<Object> complement = New.linkedList(values2);

        boolean notDone;
        do
        {
            notDone = false;
            for (ListIterator<Object> iter2 = complement.listIterator(); iter2.hasNext();)
            {
                Object val2 = iter2.next();
                for (int index1 = 0; index1 < values1.size();)
                {
                    Object val1 = values1.get(index1++);

                    // For each intersection of values, remove the value from
                    // the result and replace it with the portion that is the
                    // complement of val1.
                    List<? extends Object> comp = subtractValues(val2, val1);
                    if (comp.size() != 1 || !checkEquality(comp.get(0), val2))
                    {
                        iter2.remove();
                        for (int i = 0; i < comp.size();)
                        {
                            iter2.add(comp.get(i++));
                        }
                        if (comp.isEmpty())
                        {
                            break;
                        }
                        iter2.previous();
                        val2 = iter2.next();

                        // Since there was an intersection with a remainder,
                        // set the flag to re-generate the combinations in
                        // case values1 has overlapping values.
                        notDone = true;
                    }
                }
            }
        }
        while (notDone);

        return complement;
    }

    /**
     * Determine the intersection of two lists of values.
     *
     * @param values1 A list of values.
     * @param values2 A list of values.
     * @return The intersection.
     */
    private static List<Object> getListIntersection(List<?> values1, List<?> values2)
    {
        List<Object> intersection = New.list(Math.max(values1.size(), values2.size()));

        for (int index1 = 0; index1 < values1.size();)
        {
            Object val1 = values1.get(index1++);
            for (int index2 = 0; index2 < values2.size();)
            {
                Object val2 = values2.get(index2++);

                Object obj = getValueIntersection(val1, val2);
                if (obj != null)
                {
                    intersection.add(obj);
                }
            }
        }

        return intersection;
    }

    /**
     * Determine the difference between two values.
     *
     * @param value1 The first value.
     * @param value2 The second value.
     * @return The difference.
     */
    private static List<? extends Object> subtractValues(Object value1, Object value2)
    {
        if (value1 instanceof TimeSpan && value2 instanceof TimeSpan)
        {
            return ((TimeSpan)value1).subtract((TimeSpan)value2);
        }
        else if (value1 instanceof Geometry && value2 instanceof Geometry)
        {
            Geometry diff = ((Geometry)value1).difference((Geometry)value2);
            List<Geometry> result;
            if (diff.isEmpty())
            {
                result = Collections.emptyList();
            }
            else if (diff instanceof GeometryCollection)
            {
                result = New.list(diff.getNumGeometries());
                for (int index = 0; index < diff.getNumGeometries(); ++index)
                {
                    result.add(((GeometryCollection)diff).getGeometryN(index));
                }
            }
            else if (diff.getDimension() < 2)
            {
                result = Collections.emptyList();
            }
            else
            {
                result = New.list(diff);
            }

            for (int resultIndex = result.size() - 1; resultIndex >= 0; --resultIndex)
            {
                Geometry geom = result.get(resultIndex);
                if (geom instanceof Polygon)
                {
                    CoordinateSequence coordinateSequence = ((Polygon)geom).getExteriorRing().getCoordinateSequence();

                    long t0 = System.nanoTime();
                    CoordinateSequence replacement = JTSUtilities.removeColinearCoordinates(coordinateSequence);

                    if (LOGGER.isTraceEnabled())
                    {
                        long t1 = System.nanoTime();
                        LOGGER.trace(StringUtilities.formatTimingMessage("Time to remove colinear coordinates: ", t1 - t0));
                    }

                    if (replacement != null)
                    {
                        if (replacement.size() > 3)
                        {
                            com.vividsolutions.jts.geom.LinearRing linearRing = JTSUtilities.GEOMETRY_FACTORY
                                    .createLinearRing(replacement);
                            Polygon poly = JTSUtilities.GEOMETRY_FACTORY.createPolygon(linearRing, null);
                            result.set(resultIndex, poly);
                        }
                        else
                        {
                            result.remove(resultIndex);
                        }
                    }
                }
            }

            return result;
        }
        else
        {
            return Objects.equals(value1, value2) ? Collections.emptyList() : New.list(value1);
        }
    }

    /**
     * Create an empty interval property value set.
     */
    public IntervalPropertyValueSet()
    {
        myMap = Collections.emptyMap();
    }

    /**
     * Create an interval property value set from a builder.
     *
     * @param builder The builder.
     */
    public IntervalPropertyValueSet(IntervalPropertyValueSet.Builder builder)
    {
        myMap = Collections.<PropertyDescriptor<?>, List<? extends Object>>unmodifiableMap(builder.getMap());
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj != null && getClass() == obj.getClass() && myMap.equals(((IntervalPropertyValueSet)obj).myMap);
    }

    /**
     * Get access to the (unmodifiable) map.
     *
     * @return The map of property descriptors to lists of property values.
     */
    public Map<PropertyDescriptor<?>, List<? extends Object>> getMap()
    {
        return myMap;
    }

    /**
     * Get the property values for a particular property. Return an unmodifiable
     * list.
     *
     * @param <T> The type of the property values.
     * @param desc The property descriptor.
     * @return The values, or {@code null} if the property does not exist in
     *         this set.
     */
    public <T> List<? extends T> getValues(PropertyDescriptor<T> desc)
    {
        List<? extends Object> list = myMap.get(desc);
        @SuppressWarnings("unchecked")
        List<? extends T> cast = (List<? extends T>)(list == null ? null : list);
        return cast;
    }

    @Override
    public int hashCode()
    {
        return 31 + myMap.hashCode();
    }

    /**
     * Determine if this set intersects another set.
     *
     * @param other The other set.
     * @return {@code true} if the sets intersect.
     */
    public boolean intersects(IntervalPropertyValueSet other)
    {
        Set<Entry<PropertyDescriptor<?>, List<? extends Object>>> entrySet = other.getMap().entrySet();
        for (Entry<PropertyDescriptor<?>, List<? extends Object>> entry : entrySet)
        {
            if (entry.getValue().isEmpty())
            {
                return false;
            }
            List<?> values = myMap.get(entry.getKey());
            if (values == null || !listsIntersect(entry.getValue(), values))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Subtract another interval set from this one. If this interval set does
     * not contain any properties, an empty list will be returned.
     *
     * @param other The interval set to be subtracted.
     * @return The result.
     */
    public List<IntervalPropertyValueSet> subtract(IntervalPropertyValueSet other)
    {
        if (getMap().isEmpty() || equals(other))
        {
            return Collections.emptyList();
        }

        // Array of properties in other set.
        PropertyDescriptor<?>[] properties = New.array(other.getMap().keySet(), PropertyDescriptor.class);

        // Array of property intersections; indices match properties array.
        List<?>[] intersections = new List<?>[properties.length];

        // Get the intersections first so that the no-intersection case can be
        // detected early.
        for (int propertiesIndex = 0; propertiesIndex < properties.length; ++propertiesIndex)
        {
            List<?> thisValues = getValues(properties[propertiesIndex]);
            if (thisValues != null)
            {
                List<?> otherValues = other.getValues(properties[propertiesIndex]);
                intersections[propertiesIndex] = getListIntersection(otherValues, thisValues);
                if (intersections[propertiesIndex].isEmpty())
                {
                    // If there's no intersection, the subtraction has
                    // no effect.
                    return Collections.singletonList(this);
                }
            }
        }

        List<IntervalPropertyValueSet> result = New.list();
        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.populate(this);

        for (int propertiesIndex = 0; propertiesIndex < properties.length; ++propertiesIndex)
        {
            List<?> thisValues = getValues(properties[propertiesIndex]);
            if (thisValues != null)
            {
                List<?> otherValues = other.getValues(properties[propertiesIndex]);
                List<?> complement = getComplement(otherValues, thisValues);
                if (!complement.isEmpty())
                {
                    for (int index = 0; index < propertiesIndex; ++index)
                    {
                        builder.putAll(properties[index], intersections[index]);
                    }
                    builder.putAll(properties[propertiesIndex], complement);

                    result.add(builder.create());
                }
            }
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(new StringBuilder().append(this).append(" minus ").append(other).append(" = ").append(result));
        }

        return result;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(256).append(getClass().getSimpleName()).append('[').append(myMap.toString()).append(']')
                .toString();
    }

    /**
     * Determine if two lists of values intersect.
     *
     * @param list1 A list.
     * @param list2 A list.
     * @return {@code true} if the lists intersect.
     */
    protected boolean listsIntersect(List<?> list1, List<?> list2)
    {
        for (int index1 = 0; index1 < list1.size(); ++index1)
        {
            for (int index2 = 0; index2 < list2.size(); ++index2)
            {
                if (valuesIntersect(list1.get(index1), list2.get(index2)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Builder for the interval property value set.
     */
    public static class Builder
    {
        /** A map of property descriptors to lists of values. */
        @SuppressWarnings("rawtypes")
        private final Map<PropertyDescriptor, List<Object>> myMap = new LazyMap<>(
                New.<PropertyDescriptor, List<Object>>insertionOrderMap(), PropertyDescriptor.class,
                key -> New.list());

        /**
         * Add a property value to the set.
         *
         * @param desc The property descriptor.
         * @param value The property value.
         */
        public void add(PropertyDescriptor<?> desc, Object value)
        {
            checkType(desc, value);
            myMap.get(desc).add(value);
        }

        /**
         * Add some property values into the set.
         *
         * @param desc The property descriptor.
         * @param values The values.
         */
        public void addAll(PropertyDescriptor<?> desc, Collection<? extends Object> values)
        {
            List<Object> list = myMap.get(desc);
            for (Object value : values)
            {
                checkType(desc, value);
                list.add(value);
            }
        }

        /**
         * Clear this builder of all values.
         */
        public void clear()
        {
            myMap.clear();
        }

        /**
         * Clear the values for a particular property in the builder.
         *
         * @param desc The property descriptor.
         */
        public void clear(PropertyDescriptor<?> desc)
        {
            myMap.remove(desc);
        }

        /**
         * Create an {@link IntervalPropertyValueSet} using this builder.
         *
         * @return The result.
         */
        public IntervalPropertyValueSet create()
        {
            return new IntervalPropertyValueSet(this);
        }

        /**
         * Get the values for a property.
         *
         * @param <T> The type of the property values.
         * @param desc The property descriptor.
         * @return The values.
         */
        @SuppressWarnings("unchecked")
        public <T> List<? extends T> get(PropertyDescriptor<T> desc)
        {
            return (List<? extends T>)Collections.unmodifiableList(myMap.get(desc));
        }

        /**
         * Get a copy of the current state of the map.
         *
         * @return The map.
         */
        @SuppressWarnings("rawtypes")
        public Map<PropertyDescriptor<?>, List<Object>> getMap()
        {
            Map<PropertyDescriptor<?>, List<Object>> result = New.insertionOrderMap(myMap.size());
            for (Entry<PropertyDescriptor, List<Object>> entry : myMap.entrySet())
            {
                result.put(entry.getKey(), Collections.unmodifiableList(New.randomAccessList(entry.getValue())));
            }
            return result;
        }

        /**
         * Populate this builder from an {@link IntervalPropertyValueSet}.
         *
         * @param in The input.
         */
        public void populate(IntervalPropertyValueSet in)
        {
            clear();
            for (Entry<PropertyDescriptor<?>, List<? extends Object>> entry : in.getMap().entrySet())
            {
                List<? extends Object> list = entry.getValue();
                for (Object object : list)
                {
                    add(entry.getKey(), object);
                }
            }
        }

        /**
         * Put a property value into the set, replacing any existing value(s).
         *
         * @param desc The property descriptor.
         * @param value The property value.
         */
        public void put(PropertyDescriptor<?> desc, Object value)
        {
            List<Object> list = myMap.get(desc);
            if (!list.isEmpty())
            {
                list.clear();
            }
            checkType(desc, value);
            list.add(value);
        }

        /**
         * Put some property values into the set, removing any existing values.
         *
         * @param desc The property descriptor.
         * @param values The values.
         */
        public void putAll(PropertyDescriptor<?> desc, Collection<? extends Object> values)
        {
            List<Object> list = myMap.get(desc);
            if (!list.isEmpty())
            {
                list.clear();
            }
            for (Object value : values)
            {
                checkType(desc, value);
                list.add(value);
            }
        }

        /**
         * Check that a value is appropriate for a property descriptor.
         *
         * @param desc The property descriptor.
         * @param value The property value.
         * @throws ClassCastException If the value cannot be cast to the
         *             property descriptor's type.
         */
        private void checkType(PropertyDescriptor<?> desc, Object value) throws ClassCastException
        {
            if (!desc.getType().isInstance(value))
            {
                throw new ClassCastException(value.getClass() + " cannot be cast to " + desc.getType());
            }
        }
    }
}
