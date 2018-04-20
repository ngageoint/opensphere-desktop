package io.opensphere.core.util.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreeTuple;

/**
 * Generic URL utilities.
 */
public final class UrlUtilities
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(UrlUtilities.class);

    /**
     * Extracts the base URL String from a URL. Inspired by
     * {@link java.net.URLStreamHandler}.
     *
     * @param u The URL
     * @return The base URL (e.g. http://localhost:8080)
     */
    public static String getBaseURL(URL u)
    {
        if (u == null)
        {
            return null;
        }
        // pre-compute length of StringBuilder
        int len = u.getProtocol().length() + 1;
        if (u.getAuthority() != null && u.getAuthority().length() > 0)
        {
            len += 2 + u.getAuthority().length();
        }

        StringBuilder result = new StringBuilder(len);
        result.append(u.getProtocol());
        result.append(':');
        if (u.getAuthority() != null && u.getAuthority().length() > 0)
        {
            result.append("//");
            result.append(u.getAuthority());
        }
        return result.toString();
    }

    /**
     * Extracts the base URL String from a URL String. Inspired by
     * {@link java.net.URLStreamHandler}.
     *
     * @param u The URL in String format
     * @return The base URL (e.g. http://localhost:8080)
     */
    public static String getBaseURL(String u)
    {
        return getBaseURL(toURL(u));
    }

    /**
     * Gets the protocol host and port.
     *
     * @param url The url to extract the information from.
     * @param defaultPort The default port to fill in the url does not specify a
     *            port.
     * @return String array where protocol at [0], host at [1], and port at [2].
     */
    public static ThreeTuple<String, String, Integer> getProtocolHostPort(String url, int defaultPort)
    {
        int wheresTheColon = url.indexOf(':');
        int hostBeginIndex = wheresTheColon + 3;
        String protocol = url.substring(0, wheresTheColon);
        int hostEndIndex = url.indexOf('/', hostBeginIndex);

        String host = url.substring(hostBeginIndex);

        if (hostEndIndex > 0)
        {
            host = host.substring(0, hostEndIndex - hostBeginIndex);
        }

        int rtmpPort = defaultPort;

        int portIndex = host.indexOf(':');
        if (portIndex > 0)
        {
            String portString = host.substring(portIndex + 1);
            rtmpPort = Integer.parseInt(portString);
            host = host.substring(0, portIndex);
        }

        return new ThreeTuple<>(protocol, host, rtmpPort);
    }

    /**
     * Checks to see if the url points to a file location.
     *
     * @param url The url to inspect.
     * @return True if the url points to a file, false otherwise.
     */
    public static boolean isFile(URL url)
    {
        boolean isFile = false;

        String protocol = url.getProtocol();
        if ("file".equalsIgnoreCase(protocol) || "jar".equalsIgnoreCase(protocol))
        {
            isFile = true;
        }

        return isFile;
    }

    /**
     * Converts the given string to a URL, or null if it's a crap string.
     *
     * @param urlString the string
     * @return the URL or null
     */
    public static URL toURL(String urlString)
    {
        URL url = null;
        if (StringUtils.isNotEmpty(urlString))
        {
            try
            {
                url = new URL(urlString);
            }
            catch (MalformedURLException e)
            {
                LOGGER.error(e.toString(), e);
            }
        }
        return url;
    }

    /**
     * Converts the given string to a URL, or null if it's a crap string.
     *
     * @param urlOrPathString the string
     * @return the URL or null
     */
    public static URL toURLNew(String urlOrPathString)
    {
        URL url = null;
        if (StringUtils.isNotEmpty(urlOrPathString))
        {
            String urlString = hasValidProtocol(urlOrPathString) ? fixFileProtocol(urlOrPathString)
                    : addFileProtocol(urlOrPathString);
            url = toURL(urlString);
        }
        return url;
    }

    /**
     * Concatenates URL fragments, taking care of adding/removing slashes as
     * necessary.
     *
     * @param fragments the URL fragments
     * @return the concatenated string
     */
    public static String concatUrlFragments(String... fragments)
    {
        StringJoiner joiner = new StringJoiner("/");
        for (String fragment : fragments)
        {
            joiner.add(StringUtilities.trimBoth(fragment, '/'));
        }
        return joiner.toString();
    }

    /**
     * Adds a file protocol to a path string.
     *
     * @param pathString the path string
     * @return the URL string
     */
    private static String addFileProtocol(String pathString)
    {
        return "file:///" + StringUtilities.removePrefix(StringUtilities.removePrefix(pathString, "/"), "\\");
    }

    /**
     * Fixes the file protocol if necessary.
     *
     * @param urlString the URL string
     * @return the fixed string
     */
    private static String fixFileProtocol(String urlString)
    {
        String fixedString = urlString;
        if (urlString.startsWith("file://") && !urlString.startsWith("file:///"))
        {
            fixedString = "file:///" + urlString.substring(7);
        }
        return fixedString;
    }

    /**
     * Returns true if specified string has a valid protocol.
     *
     * @param urlString the URL string
     * @return whether the string has a valid protocol
     */
    private static boolean hasValidProtocol(String urlString)
    {
        boolean valid = false;
        if (urlString.length() >= 8)
        {
            int colonIndex = urlString.indexOf(':');
            if (colonIndex != -1)
            {
                String protocol = urlString.substring(0, colonIndex);
                valid = protocol.length() > 1 && isValidProtocol(protocol);
            }
        }
        return valid;
    }

    /**
     * Returns true if specified string is a valid protocol name.
     *
     * @param protocol the potential protocol string
     * @return whether the string is a valid protocol name
     */
    private static boolean isValidProtocol(String protocol)
    {
        int len = protocol.length();
        if (len < 1)
        {
            return false;
        }
        char c = protocol.charAt(0);
        if (!Character.isLetter(c))
        {
            return false;
        }
        for (int i = 1; i < len; i++)
        {
            c = protocol.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' && c != '-')
            {
                return false;
            }
        }
        return true;
    }

    /** Disallow instantiation. */
    private UrlUtilities()
    {
    }
}
