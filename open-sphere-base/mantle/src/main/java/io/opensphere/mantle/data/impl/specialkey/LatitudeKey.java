package io.opensphere.mantle.data.impl.specialkey;

/**
 * The Class LatitudeKey.
 */
public class LatitudeKey extends AbstractSpecialKey
{
    /** The Default Latitude Key. */
    public static final LatitudeKey DEFAULT = new LatitudeKey();

    /** The Constant LATITUDE_SPECIAL_KEY_NAME. */
    public static final String LATITUDE_SPECIAL_KEY_NAME = "Latitude";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new latitude key.
     */
    public LatitudeKey()
    {
        super(LATITUDE_SPECIAL_KEY_NAME);
    }
}
