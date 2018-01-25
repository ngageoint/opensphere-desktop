package io.opensphere.mantle.data.impl.specialkey;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;

/**
 * The Class AltitudeKey.
 */
public class AltitudeKey extends AbstractSpecialKey
{
    /** The Constant ALTITUDE_SPECIAL_KEY_NAME. */
    public static final String ALTITUDE_SPECIAL_KEY_NAME = "Altitude";

    /** The Default AltitudeKey. */
    public static final AltitudeKey DEFAULT = new AltitudeKey();

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new altitude key with the default unit of METERS.
     */
    public AltitudeKey()
    {
        this(Meters.class);
    }

    /**
     * Instantiates a new altitude key.
     *
     * @param altitudeUnit the altitude unit
     */
    public AltitudeKey(Class<? extends Length> altitudeUnit)
    {
        super(ALTITUDE_SPECIAL_KEY_NAME, altitudeUnit);
    }

    /**
     * Gets the distance unit.
     *
     * @return the altitude unit
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Length> getAltitudeUnit()
    {
        return (Class<? extends Length>)getKeyUnit();
    }
}
