package io.opensphere.mantle.data.impl.specialkey;

/**
 * The Class LongitudeKey.
 */
public class LongitudeKey extends AbstractSpecialKey
{
    /** The Default LongitudeKey. */
    public static final LongitudeKey DEFAULT = new LongitudeKey();

    /** The Constant LONGITUDE_SPECIAL_KEY_NAME. */
    public static final String LONGITUDE_SPECIAL_KEY_NAME = "Longitude";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Longitude key.
     */
    public LongitudeKey()
    {
        super(LONGITUDE_SPECIAL_KEY_NAME);
    }
}
