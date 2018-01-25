package io.opensphere.search.mapzen.model;

/** A collection of preference keys in MapZen search configuration files. */
public class MapZenPreferenceKeys
{
    /** The prefix common to all keys. */
    private static final String KEY_PREFIX = "security-banner";

    /** The key with which the API Key is persisted. */
    public static final String API_KEY = KEY_PREFIX + ".api-key";

    /** The key with which the Search URL template is persisted. */
    public static final String SEARCH_URL_TEMPLATE = KEY_PREFIX + ".search-url-template";

    /** Private constructor hidden from use. */
    private MapZenPreferenceKeys()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }
}
