package io.opensphere.mantle.data.impl.specialkey;

/**
 * The Class EndTimeKey. This defines the Special Key type for features that
 * have an explicit end time (also called "down time," "down date time," or
 * "off time.") It is paired with the {@link TimeKey} which serves as the Start
 * time (or "up time"/"up date time"/"on time").
 */
public class EndTimeKey extends AbstractSpecialKey
{
    /** Serial Version UID. */
    private static final long serialVersionUID = 1L;

    /** The Default TimeKey. */
    public static final EndTimeKey DEFAULT = new EndTimeKey();

    /** The Constant END_TIME_SPECIAL_KEY_NAME. */
    public static final String END_TIME_SPECIAL_KEY_NAME = "EndTime";

    /**
     * Instantiates a new End-Time key.
     */
    public EndTimeKey()
    {
        super(END_TIME_SPECIAL_KEY_NAME);
    }
}
