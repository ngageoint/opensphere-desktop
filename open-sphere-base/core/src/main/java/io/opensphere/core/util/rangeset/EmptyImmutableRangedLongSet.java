package io.opensphere.core.util.rangeset;

/**
 * An empty immutable {@link RangedLongSet}.
 */
public class EmptyImmutableRangedLongSet extends ImmutableRangedLongSet
{
    /** The a default instance of the empty immutable ranged long set. */
    public static final EmptyImmutableRangedLongSet DEFAULT = new EmptyImmutableRangedLongSet();

    /**
     * Instantiates a new empty immutable ranged long set.
     */
    public EmptyImmutableRangedLongSet()
    {
        super(new DefaultRangedLongSet());
    }
}
