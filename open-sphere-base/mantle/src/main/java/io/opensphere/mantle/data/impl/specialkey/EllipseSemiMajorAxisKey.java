package io.opensphere.mantle.data.impl.specialkey;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.NauticalMiles;

/**
 * The Class EllipseSemiMajorAxisKey.
 */
public class EllipseSemiMajorAxisKey extends AbstractSpecialKey
{
    /** The Default EllipseSemiMajorAxis. */
    public static final EllipseSemiMajorAxisKey DEFAULT = new EllipseSemiMajorAxisKey();

    /** The Constant ELLIPS_SEMI_MAJOR_AXIS_KEY_NAME. */
    public static final String ELLIPS_SEMI_MAJOR_AXIS_KEY_NAME = "EllipseSemiMajorAxis";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EllipseSemiMajorAxisKey, defaulted to nautical miles
     * unit.
     */
    public EllipseSemiMajorAxisKey()
    {
        this(NauticalMiles.class);
    }

    /**
     * Instantiates a new EllipseSemiMajorAxisKey.
     *
     * @param semiMajorUnit the semi major unit
     */
    public EllipseSemiMajorAxisKey(Class<? extends Length> semiMajorUnit)
    {
        super(ELLIPS_SEMI_MAJOR_AXIS_KEY_NAME, semiMajorUnit);
    }

    /**
     * Gets the length units.
     *
     * @return the semi major unit
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Length> getSemiMajorUnit()
    {
        return (Class<? extends Length>)getKeyUnit();
    }
}
