package io.opensphere.core.units;

/**
 * Exception thrown when an attempt is made to convert between two units that
 * are incompatible.
 */
public class InconvertibleUnits extends RuntimeException
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param type1 The first type.
     * @param type2 The second type.
     */
    public InconvertibleUnits(Class<?> type1, Class<?> type2)
    {
        super("Units are inconvertible: " + type1 + ", " + type2);
    }
}
