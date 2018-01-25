package io.opensphere.mantle.data.impl.specialkey;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.NauticalMiles;

/**
 * The Class EllipseSemiMinorAxisKey.
 */
public class EllipseSemiMinorAxisKey extends AbstractSpecialKey
{
    /** The Default EllipseSemiMinorAxisKey. */
    public static final EllipseSemiMinorAxisKey DEFAULT = new EllipseSemiMinorAxisKey();

    /** The Constant ELLIPS_SEMI_MINOR_AXIS_KEY_NAME. */
    public static final String ELLIPS_SEMI_MINOR_AXIS_KEY_NAME = "EllipseSemiMinorAxis";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new EllipseSemiMinorAxisKey key. Defaulted to nautical
     * miles unit.
     */
    public EllipseSemiMinorAxisKey()
    {
        this(NauticalMiles.class);
    }

    /**
     * Instantiates a new EllipseSemiMinorAxisKey key.
     *
     * @param semiMinorUnit the semi minor unit
     */
    public EllipseSemiMinorAxisKey(Class<? extends Length> semiMinorUnit)
    {
        super(ELLIPS_SEMI_MINOR_AXIS_KEY_NAME, semiMinorUnit);
    }

    /**
     * Gets the length units.
     *
     * @return the semi minor unit
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Length> getAltitudeUnit()
    {
        return (Class<? extends Length>)getKeyUnit();
    }
}
