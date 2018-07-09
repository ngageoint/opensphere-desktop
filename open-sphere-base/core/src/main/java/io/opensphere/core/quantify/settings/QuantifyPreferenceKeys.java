package io.opensphere.core.quantify.settings;

/**
 *
 */
public final class QuantifyPreferenceKeys
{
    /** The prefix common to all keys. */
    private static final String KEY_PREFIX = "quantify";

    /** The suffix in which the remote endpoint is controlled. */
    public static final String URL_KEY = KEY_PREFIX + ".url";

    /** The suffix in which the enabled state is controlled. */
    public static final String ENABLED_KEY = KEY_PREFIX + ".enabled";

    /** The suffix in which the capture-to-log state is controlled. */
    public static final String CAPTURE_TO_LOG_KEY = KEY_PREFIX + ".capture-to-log";

    /**
     * Private constructor hidden from use.
     */
    private QuantifyPreferenceKeys()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }
}
