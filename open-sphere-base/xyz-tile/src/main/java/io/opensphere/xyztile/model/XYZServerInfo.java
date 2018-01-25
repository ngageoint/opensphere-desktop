package io.opensphere.xyztile.model;

/**
 * Contains certain information about an XYZ server.
 */
public class XYZServerInfo
{
    /**
     * The user friendly name of the server.
     */
    private final String myServerName;

    /**
     * The url to the server.
     */
    private final String myServerUrl;

    /**
     * Constructs a new server info.
     *
     * @param serverName The user friendly name of the server.
     * @param serverUrl The url to the server.
     */
    public XYZServerInfo(String serverName, String serverUrl)
    {
        myServerName = serverName;
        myServerUrl = serverUrl;
    }

    /**
     * Gets the user friendly name of the server.
     *
     * @return The server name.
     */
    public String getServerName()
    {
        return myServerName;
    }

    /**
     * Gets the url to the server.
     *
     * @return The server url.
     */
    public String getServerUrl()
    {
        return myServerUrl;
    }
}
