package io.opensphere.mantle.data.impl.specialkey;

/**
 * The Class LineOfBearingKey.
 */
public class LineOfBearingKey extends AbstractSpecialKey
{
    /** The Default LineOfBearingKey. */
    public static final LineOfBearingKey DEFAULT = new LineOfBearingKey();

    /** The Constant LINE_OF_BEARING_SPECIAL_KEY_NAME. */
    public static final String LINE_OF_BEARING_SPECIAL_KEY_NAME = "LineOfBearing";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new line of bearing key with the
     * DEGREES_CLOCKWISE_FROM_NORTH unit.
     */
    public LineOfBearingKey()
    {
        super(LINE_OF_BEARING_SPECIAL_KEY_NAME, OrientationUnit.DEGREES_CLOCKWISE_FROM_NORTH);
    }

    /**
     * Instantiates a new line of bearing key.
     *
     * @param unit the line of bearing unit
     */
    public LineOfBearingKey(OrientationUnit unit)
    {
        super(LINE_OF_BEARING_SPECIAL_KEY_NAME, unit);
    }

    /**
     * Gets the {@link OrientationUnit}.
     *
     * @return the unit
     */
    public OrientationUnit getOrientationUnit()
    {
        return (OrientationUnit)getKeyUnit();
    }

    /**
     * The Enum OrientationUnit.
     */
    public enum OrientationUnit
    {
        /** DEGREES_CLOCKWISE_FROM_NORTH. */
        DEGREES_CLOCKWISE_FROM_NORTH
    }
}
