package io.opensphere.mantle.data.impl.specialkey;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.NauticalMiles;

/**
 * The special key whick represents a radius.
 */
public class RadiusKey extends AbstractSpecialKey
{
    /** The Default Radius. */
    public static final RadiusKey DEFAULT = new RadiusKey();

    /** The Constant RADIUS_KEY_NAME. */
    public static final String RADIUS_KEY_NAME = "Radius";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public RadiusKey()
    {
        this(NauticalMiles.class);
    }

    /**
     * Constructor.
     *
     * @param radiusUnit the radius unit
     */
    public RadiusKey(Class<? extends Length> radiusUnit)
    {
        super(RADIUS_KEY_NAME, radiusUnit);
    }

    /**
     * Gets the length units.
     *
     * @return the semi major unit
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Length> getRadiusUnit()
    {
        return (Class<? extends Length>)getKeyUnit();
    }
}
