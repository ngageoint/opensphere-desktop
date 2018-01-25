package io.opensphere.core.util.net;

import java.net.URL;
import java.util.Map;

/** Takes an input string and converts it to a URL. */
public interface Linker
{
    /**
     * Get the URLs for a key/value pair.
     *
     * @param key The key.
     * @param value The value.
     * @return A map of service descriptions to URLs.
     */
    Map<String, URL> getURLs(String key, String value);

    /**
     * Get if this Linker can provide URLs for a given key and value.
     *
     * @param key The key.
     * @param value The value.
     * @return {@code true} if URLs can be generated.
     */
    boolean hasURLFor(String key, String value);
}
