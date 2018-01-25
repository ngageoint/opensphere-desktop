package io.opensphere.core.util.net;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * URL Encoding Utilities.
 */
public final class URLEncodingUtilities
{
    /**
     * Returns the given URL as a new URL with URL-encoded query values.
     *
     * @param url The URL
     * @return The URL with URL-encoded query values
     */
    public static URL encodeURL(URL url)
    {
        URL encodedURL = null;
        if (url != null)
        {
            UrlBuilder builder = new UrlBuilder(url);

            // Encode the query parameters
            builder.setQueryParameters(encodeQueryParameters(builder.getQueryParameters()));

            // Build the encoded URL
            try
            {
                encodedURL = builder.toURL();
            }
            catch (MalformedURLException e)
            {
                encodedURL = url;
            }
        }
        return encodedURL;
    }

    /**
     * Encodes query parameters.
     *
     * @param queryParameters The query parameters
     * @return The encoded query parameters
     */
    private static Map<String, String> encodeQueryParameters(Map<String, String> queryParameters)
    {
        Map<String, String> encodedQueryParameters = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : queryParameters.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();

            String encodedValue = value;
            if (value != null)
            {
                try
                {
                    encodedValue = URLEncoder.encode(value, "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    encodedValue = value;
                }
            }

            encodedQueryParameters.put(key, encodedValue);
        }
        return encodedQueryParameters;
    }

    /**
     * Private constructor.
     */
    private URLEncodingUtilities()
    {
    }
}
