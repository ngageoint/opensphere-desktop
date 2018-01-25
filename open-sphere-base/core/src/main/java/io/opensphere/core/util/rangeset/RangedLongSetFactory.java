package io.opensphere.core.util.rangeset;

import java.util.Collection;

/**
 * A factory for creating RangedLongSet objects.
 */
public final class RangedLongSetFactory
{
    /**
     * Returns an empty immutable {@link RangedLongSet}.
     *
     * @return the ranged long set
     */
    public static RangedLongSet emptyImmutableRangedLongSet()
    {
        return EmptyImmutableRangedLongSet.DEFAULT;
    }

    /**
     * Returns the provided {@link RangedLongSet} wrapped in an
     * {@link ImmutableRangedLongSet}.
     *
     * @param set the set
     * @return the ranged long set
     */
    public static RangedLongSet immutableSet(RangedLongSet set)
    {
        return set == null ? null : new ImmutableRangedLongSet(set);
    }

    /**
     * Returns a new {@link RangedLongSet}.
     *
     * @return the ranged long set
     */
    public static RangedLongSet newSet()
    {
        return new DefaultRangedLongSet();
    }

    /**
     * Returns a new {@link RangedLongSet} populated with the values from a
     * {@link Collection} of {@link Long}.
     *
     * @param values the values to use to construct the set.
     * @return the {@link RangedLongSet}
     */
    public static RangedLongSet newSet(Collection<Long> values)
    {
        return new DefaultRangedLongSet(values);
    }

    /**
     * Returns a new {@link RangedLongSet} populated with the single provided
     * value.
     *
     * @param singularValue to add to the new set.
     * @return the {@link RangedLongSet}.
     */
    public static RangedLongSet newSet(long singularValue)
    {
        return new DefaultRangedLongSet(singularValue);
    }

    /**
     * Returns a new {@link RangedLongSet} populated with the values from a an
     * array of long.
     *
     * @param values the values to use to construct the set.
     * @return the {@link RangedLongSet}
     */
    public static RangedLongSet newSet(long[] values)
    {
        return new DefaultRangedLongSet(values);
    }

    /**
     * Returns a new {@link RangedLongSet} populated with the values from a an
     * array of {@link Long}.
     *
     * @param values the values to use to construct the set.
     * @return the {@link RangedLongSet}
     */
    public static RangedLongSet newSet(Long[] values)
    {
        return new DefaultRangedLongSet(values);
    }

    /**
     * Returns a deep copy of an existing {@link RangedLongSet}.
     *
     * @param other the {@link RangedLongSet} to copy.
     * @return the copy.
     */
    public static RangedLongSet newSet(RangedLongSet other)
    {
        return new DefaultRangedLongSet(other);
    }

    /**
     * Instantiates a new ranged long set factory.
     */
    private RangedLongSetFactory()
    {
        // Don't allow instantiation.
    }
}
