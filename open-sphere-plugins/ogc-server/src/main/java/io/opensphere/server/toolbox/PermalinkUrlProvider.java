package io.opensphere.server.toolbox;

/**
 * Gets the permalink url for a specified file. The url returned is not the full
 * url, just permalink url to be appended to the server url.
 *
 */
@FunctionalInterface
public interface PermalinkUrlProvider
{
    /**
     * Gets the permalink url for the specified server.
     *
     * @param host The server to get the permalink for.
     * @return The permalink URL to be appended to the server url.
     */
    String getPermalinkUrl(String host);
}
