package com.bitsys.common.http.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * This class provides utility methods for dealing with URLs.
 */
public final class UrlUtils
{
    /**
     * Hide the default constructor.
     */
    private UrlUtils()
    {
    }

    /**
     * Creates a URL from the given URL string. If a protocol is not provided
     * with the string, the protocol is assumed to be "<code>file</code>".
     *
     * @param urlString the URL string to convert.
     * @return the resulting URL or <code>null</code> if the URL string is
     *         {@link StringUtils#isBlank(CharSequence) blank}.
     * @throws IllegalArgumentException if the string cannot be converted to a
     *             URL.
     */
    public static URL toUrl(final String urlString)
    {
        URL url = null;
        if (StringUtils.isNotBlank(urlString))
        {
            try
            {
                url = new URL(urlString);
            }
            catch (final MalformedURLException e)
            {
                try
                {
                    url = new URL("file:" + urlString);
                }
                catch (final MalformedURLException e2)
                {
                    throw new IllegalArgumentException("Failed to convert the URL string '" + urlString
                            + "' to a URL even when specifying the 'file:' protocol.", e);
                }
            }
        }

        return url;
    }

    /**
     * Converts the given URL to a URI. If the built-in {@link URL#toURI()}
     * fails, the URL query parameters are checked for proper encoding.
     *
     * @param url the URL to convert.
     * @return the resulting URI.
     * @throws IllegalArgumentException if the given URL cannot be converted to
     *             a URI.
     */
    public static URI toUri(final URL url)
    {
        URI uri;
        try
        {
            uri = url.toURI();
        }
        catch (final URISyntaxException e)
        {
            // If an exception is thrown, attempt encoding the query parameter
            // values.
            try
            {
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), null, null);
                final URIBuilder builder = new URIBuilder(uri);
                final List<NameValuePair> params = URLEncodedUtils.parse(url.getQuery(), Charset.forName("UTF-8"));
                for (final NameValuePair pair : params)
                {
                    builder.addParameter(pair.getName(), pair.getValue());
                }
                uri = builder.build();
            }
            catch (final URISyntaxException e2)
            {
                throw new IllegalArgumentException("Failed to convert the URL '" + url + "' to a URI.", e2);
            }
        }
        return uri;
    }

    /**
     * Adds the given parameter to the URL.
     *
     * @param baseUrl the base URL.
     * @param name the parameter name.
     * @return the new URL.
     */
    public static URL addParameter(final URL baseUrl, final String name)
    {
        return addParameter(baseUrl, name, null);
    }

    /**
     * Adds the given parameter to the URL.
     *
     * @param baseUrl the base URL.
     * @param name the parameter name.
     * @param value the parameter value or <code>null</code>.
     * @return the new URL.
     */
    public static URL addParameter(final URL baseUrl, final String name, final String value)
    {
        if (baseUrl == null)
        {
            throw new IllegalArgumentException("The base URL is null");
        }

        final String urlString = baseUrl.toExternalForm();
        try
        {
            return addParameter(urlString, name, value);
        }
        catch (final MalformedURLException e)
        {
            // In the unlikely event of this exception, throw a runtime
            // exception.
            throw new IllegalArgumentException(
                    "Failed to create a URL from '" + urlString + "' with '" + name + (value == null ? "" : '=' + value) + "'",
                    e);
        }
    }

    /**
     * Adds the given parameter to the URL.
     *
     * @param urlString the base URL string.
     * @param name the parameter name.
     * @return the new URL.
     * @throws MalformedURLException if the new URL string is malformed.
     */
    public static URL addParameter(final String urlString, final String name) throws MalformedURLException
    {
        return addParameter(urlString, name, null);
    }

    /**
     * Adds the given parameter to the URL.
     *
     * @param urlString the base URL string.
     * @param name the parameter name.
     * @param value the parameter value or <code>null</code>.
     * @return the new URL.
     * @throws MalformedURLException if the new URL string is malformed.
     */
    public static URL addParameter(final String urlString, final String name, final String value) throws MalformedURLException
    {
        if (urlString == null)
        {
            throw new IllegalArgumentException("The base URL is null");
        }
        if (name == null)
        {
            throw new IllegalArgumentException("The parameter name is null");
        }

        // Ensure that the last character is a ? or &.
        String newUrl = urlString;
        if (!newUrl.contains("?"))
        {
            newUrl += '?';
        }
        else if (!newUrl.endsWith("&") && !newUrl.endsWith("?"))
        {
            newUrl += '&';
        }
        try
        {
            newUrl += URLEncoder.encode(name, "ISO-8859-1");
            if (value != null)
            {
                newUrl += '=' + URLEncoder.encode(value, "ISO-8859-1");
            }
            return new URL(newUrl);
        }
        catch (final UnsupportedEncodingException e)
        {
            // ISO-8859-1 should always be defined.
            throw new IllegalStateException("Failed to encode parameter '" + name + (value == null ? "" : '=' + value) + "'", e);
        }
    }
}
