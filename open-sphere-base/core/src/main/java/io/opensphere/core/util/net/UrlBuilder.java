package io.opensphere.core.util.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * URL Builder.
 */
@SuppressWarnings("PMD.GodClass")
public class UrlBuilder
{
    /**
     * The protocol to use (ftp, http, nntp, ... etc.) .
     */
    private String myProtocol;

    /**
     * The host name to connect to.
     */
    private String myHost;

    /**
     * The protocol port to connect to.
     */
    private int myPort = -1;

    /**
     * The path part of this URL.
     */
    private String myPath;

    /**
     * The query part of this URL.
     */
    private String myQuery;

    /**
     * The query parameters.
     */
    private Map<String, String> myQueryParameters;

    /**
     * # reference.
     */
    private String myRef;

    /**
     * Formats query parameters into a query string.
     *
     * @param queryParameters The query parameters
     * @return The formatted query string
     */
    private static String getQuery(Map<String, String> queryParameters)
    {
        StringBuilder queryBuilder = new StringBuilder();
        if (queryParameters != null)
        {
            boolean isFirst = true;
            for (Map.Entry<String, String> entry : queryParameters.entrySet())
            {
                String key = entry.getKey();
                String value = entry.getValue();

                if (!isFirst)
                {
                    queryBuilder.append('&');
                }
                isFirst = false;
                queryBuilder.append(key);
                if (value != null)
                {
                    queryBuilder.append('=').append(value);
                }
            }
        }
        return queryBuilder.toString();
    }

    /**
     * Gets the query parameters as a key value map.
     *
     * @param query The query string
     * @return The map of query parameters
     */
    private static Map<String, String> getQueryParameters(String query)
    {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        if (query != null)
        {
            StringBuilder keyBuilder = new StringBuilder();
            StringBuilder valueBuilder = new StringBuilder();
            boolean inKey = true;

            char[] queryChars = query.toCharArray();
            for (int i = 0; i < queryChars.length; i++)
            {
                char c = queryChars[i];
                boolean atEnd = i == queryChars.length - 1;
                if (inKey)
                {
                    if (c == '=')
                    {
                        inKey = false;
                    }
                    else if (c == '&' || atEnd)
                    {
                        if (atEnd)
                        {
                            keyBuilder.append(c);
                        }

                        queryParameters.put(keyBuilder.toString(), null);
                        keyBuilder.setLength(0);
                        valueBuilder.setLength(0);
                    }
                    else
                    {
                        keyBuilder.append(c);
                    }
                }
                else
                {
                    if (c == '&' || atEnd)
                    {
                        inKey = true;

                        if (atEnd)
                        {
                            valueBuilder.append(c);
                        }

                        queryParameters.put(keyBuilder.toString(), valueBuilder.toString());
                        keyBuilder.setLength(0);
                        valueBuilder.setLength(0);
                    }
                    else
                    {
                        valueBuilder.append(c);
                    }
                }
            }
        }
        return queryParameters;
    }

    /**
     * Constructor.
     */
    public UrlBuilder()
    {
    }

    /**
     * Constructor.
     *
     * @param url the url
     * @throws MalformedURLException If the string specifies an unknown
     *             protocol.
     */
    public UrlBuilder(String url) throws MalformedURLException
    {
        setUrlInternal(new URL(url));
    }

    /**
     * Constructor.
     *
     * @param url the url
     */
    public UrlBuilder(URL url)
    {
        setUrlInternal(url);
    }

    /**
     * Adds a path to the URL.
     *
     * @param path The path
     */
    public void addPath(String path)
    {
        myPath = StringUtilities.concat(myPath, ensureStartsWith(path, '/'));
    }

    /**
     * Adds a query to the URL.
     *
     * @param query The query
     */
    public void addQuery(String query)
    {
        if (!StringUtils.isBlank(query))
        {
            getQueryParameters().putAll(getQueryParameters(query));
            myQuery = null;
        }
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost()
    {
        return myHost;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath()
    {
        return myPath;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort()
    {
        return myPort;
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol()
    {
        return myProtocol;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery()
    {
        if (myQuery == null)
        {
            myQuery = getQuery(myQueryParameters);
        }
        return myQuery;
    }

    /**
     * Gets the query parameters.
     *
     * @return the query parameters
     */
    public Map<String, String> getQueryParameters()
    {
        if (myQueryParameters == null)
        {
            myQueryParameters = getQueryParameters(myQuery);
        }
        return myQueryParameters;
    }

    /**
     * Gets the ref.
     *
     * @return the ref
     */
    public String getRef()
    {
        return myRef;
    }

    /**
     * Sets the host.
     *
     * @param host the new host
     */
    public void setHost(String host)
    {
        myHost = host;
    }

    /**
     * Sets the path.
     *
     * @param path the new path
     */
    public void setPath(String path)
    {
        myPath = ensureStartsWith(path, '/');
    }

    /**
     * Sets the port.
     *
     * @param port the new port
     */
    public void setPort(int port)
    {
        myPort = port;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol the new protocol
     */
    public void setProtocol(String protocol)
    {
        myProtocol = protocol;
    }

    /**
     * Sets the query.
     *
     * @param query the new query
     */
    public void setQuery(String query)
    {
        myQuery = query;
        myQueryParameters = null;
    }

    /**
     * Sets the query parameters.
     *
     * @param queryParameters the new query parameters
     */
    public void setQueryParameters(Map<String, String> queryParameters)
    {
        myQueryParameters = queryParameters;
        myQuery = null;
    }

    /**
     * Sets the ref.
     *
     * @param ref the new ref
     */
    public void setRef(String ref)
    {
        myRef = ref;
    }

    /**
     * Sets the URL.
     *
     * @param url the starting url
     */
    public void setUrl(URL url)
    {
        setUrlInternal(url);
    }

    @Override
    public String toString()
    {
        int length = getLength();
        StringBuilder result = new StringBuilder(length);
        result.append(myProtocol);
        result.append(':');
        if (StringUtils.isNotEmpty(myHost))
        {
            result.append("//");
            result.append(myHost);
        }
        if (myPort != -1)
        {
            result.append(':');
            result.append(myPort);
        }
        if (StringUtils.isNotEmpty(myPath))
        {
            result.append(myPath);
        }
        if (StringUtils.isNotEmpty(getQuery()))
        {
            result.append('?');
            result.append(getQuery());
        }
        if (StringUtils.isNotEmpty(myRef))
        {
            result.append('#');
            result.append(myRef);
        }
        assert length == result.length();
        return result.toString();
    }

    /**
     * Returns the URL representation of the builder.
     *
     * @return the URL
     * @throws MalformedURLException If the string specifies an unknown
     *             protocol.
     */
    public URL toURL() throws MalformedURLException
    {
        return new URL(toString());
    }

    /**
     * Appends the character to the start of the string if not already there.
     *
     * @param s the string
     * @param c the char
     * @return The new string
     */
    private String ensureStartsWith(String s, char c)
    {
        return StringUtilities.startsWith(s, c) ? s : StringUtilities.concat(String.valueOf(c), s);
    }

    /**
     * Gets the length of the URL.
     *
     * @return the length
     */
    private int getLength()
    {
        int len = myProtocol.length() + 1;
        if (StringUtils.isNotEmpty(myHost))
        {
            len += 2 + myHost.length();
        }
        if (myPort != -1)
        {
            len += 1 + String.valueOf(myPort).length();
        }
        if (StringUtils.isNotEmpty(myPath))
        {
            len += myPath.length();
        }
        if (StringUtils.isNotEmpty(getQuery()))
        {
            len += 1 + getQuery().length();
        }
        if (StringUtils.isNotEmpty(myRef))
        {
            len += 1 + myRef.length();
        }
        return len;
    }

    /**
     * Sets the URL.
     *
     * @param url the url
     */
    private void setUrlInternal(URL url)
    {
        myProtocol = url.getProtocol();
        myHost = url.getHost();
        myPort = url.getPort();
        myPath = url.getPath();
        myQuery = url.getQuery();
        myQueryParameters = null;
        myRef = url.getRef();
    }
}
