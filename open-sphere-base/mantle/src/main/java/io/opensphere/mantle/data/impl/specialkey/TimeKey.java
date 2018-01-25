package io.opensphere.mantle.data.impl.specialkey;

/**
 * The Class TimeKey.
 */
public class TimeKey extends AbstractSpecialKey
{
    /** The Default TimeKey. */
    public static final TimeKey DEFAULT = new TimeKey();

    /** The Constant TIME_SPECIAL_KEY_NAME. */
    public static final String TIME_SPECIAL_KEY_NAME = "Time";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Time key.
     */
    public TimeKey()
    {
        super(TIME_SPECIAL_KEY_NAME);
    }
}
