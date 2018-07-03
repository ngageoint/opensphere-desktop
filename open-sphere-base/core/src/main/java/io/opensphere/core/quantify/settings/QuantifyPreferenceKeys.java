package io.opensphere.core.quantify.settings;

/**
 *
 */
public final class QuantifyPreferenceKeys
{
    /** The prefix common to all keys. */
    private static final String KEY_PREFIX = "quantify";

    /** The suffix in which the enabled state is controlled. */
    public static final String ENABLED_KEY = KEY_PREFIX + ".enabled";

    /**
     * Private constructor hidden from use.
     */
    private QuantifyPreferenceKeys()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }
}
