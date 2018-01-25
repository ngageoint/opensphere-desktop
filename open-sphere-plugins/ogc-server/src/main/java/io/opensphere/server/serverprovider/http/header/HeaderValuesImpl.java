package io.opensphere.server.serverprovider.http.header;

import java.util.StringJoiner;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * Provides some constant header values.
 */
public class HeaderValuesImpl implements HeaderValues
{
    /**
     * The media types we accept.
     */
    private static final String ourAccept = "application/xml, application/json, text/xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";

    /**
     * The zip encoding.
     */
    private static final String ourZipppedEncoding = "gzip";

    /**
     * The Accept-Encoding header value.
     */
    private static final String ourEncoding = ourZipppedEncoding + ",default";

    /**
     * The beginning of the User-Agent header value.
     */
    private static final String ourUserAgentPrefix = StringUtilities.expandProperties(System.getProperty("opensphere.useragent"),
            System.getProperties()) + " (ver: "
            + StringUtilities.expandProperties(System.getProperty("opensphere.version"), System.getProperties()) + " "
            + StringUtilities.expandProperties(System.getProperty("opensphere.deployment.name"), System.getProperties()) + ")";

    /**
     * The User-Agent header value.
     */
    private final String myUserAgent;

    /**
     * Constructs a new Header constants.
     *
     * @param rendererIdentifier The identifier.
     */
    public HeaderValuesImpl(String rendererIdentifier)
    {
        StringJoiner setup = new StringJoiner("/", "(", ")");
        setup.add(System.getProperty("user.name"));
        setup.add(System.getProperty("os.name"));
        setup.add(System.getProperty("os.version"));
        setup.add(System.getProperty("os.arch"));
        setup.add(System.getProperty("java.version"));
        setup.add(StringUtilities.trim(rendererIdentifier));
        myUserAgent = ourUserAgentPrefix + setup;
    }

    @Override
    public String getAccept()
    {
        return ourAccept;
    }

    @Override
    public String getEncoding()
    {
        return ourEncoding;
    }

    @Override
    public String getUserAgent()
    {
        return myUserAgent;
    }

    @Override
    public String getZippedEncoding()
    {
        return ourZipppedEncoding;
    }
}
