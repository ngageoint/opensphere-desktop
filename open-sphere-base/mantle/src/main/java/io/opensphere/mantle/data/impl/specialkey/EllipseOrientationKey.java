package io.opensphere.mantle.data.impl.specialkey;

/**
 * The Class EllipseOrientationKey.
 */
public class EllipseOrientationKey extends AbstractSpecialKey
{
    /** The Default EllipseOrientationKey. */
    public static final EllipseOrientationKey DEFAULT = new EllipseOrientationKey();

    /** The Constant ELLIPSE_ORIENTATION_SPECIAL_KEY_NAME. */
    public static final String ELLIPSE_ORIENTATION_SPECIAL_KEY_NAME = "EllipseOrientation";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ellipse orientation key with the
     * DEGREES_CLOCKWISE_FROM_NORTH unit.
     */
    public EllipseOrientationKey()
    {
        super(ELLIPSE_ORIENTATION_SPECIAL_KEY_NAME, EllipseOrientationUnit.DEGREES_CLOCKWISE_FROM_NORTH);
    }

    /**
     * Instantiates a new ellipse orientation key.
     *
     * @param unit the orientation unit
     */
    public EllipseOrientationKey(EllipseOrientationUnit unit)
    {
        super(ELLIPSE_ORIENTATION_SPECIAL_KEY_NAME, unit);
    }

    /**
     * Gets the {@link EllipseOrientationUnit}.
     *
     * @return the unit
     */
    public EllipseOrientationUnit getOrientationUnit()
    {
        return (EllipseOrientationUnit)getKeyUnit();
    }

    /**
     * The Enum EllipseOrientationUnit.
     */
    public enum EllipseOrientationUnit
    {
        /** DEGREES_CLOCKWISE_FROM_NORTH. */
        DEGREES_CLOCKWISE_FROM_NORTH
    }
}
