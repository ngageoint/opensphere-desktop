package io.opensphere.server.permalink;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import io.opensphere.core.server.HttpServer;
import io.opensphere.server.toolbox.FilePayload;

/**
 * Extension to {@link HttpServer} for servers that provide permalink
 * functionality.
 */
public interface PermalinkHttpServer extends HttpServer
{
    /**
     * Upload the file to the server.
     *
     * @param payload The file data to upload to the server.
     * @return The url pointing to the uploaded file.
     *
     * @throws IOException Thrown if error communicating with server.
     * @throws URISyntaxException If the server url could not be converted to a
     *             URI.
     *
     */
    String uploadFile(FilePayload payload) throws IOException, URISyntaxException;

    /**
     * Download a file from the specified url.
     *
     * @param fileUrl The url of the file to download.
     * @return The file data.
     * @throws IOException Thrown if error with communicating with the server.
     * @throws URISyntaxException If the server url could not be converted to a
     *             URI.
     */
    InputStream downloadFile(String fileUrl) throws IOException, URISyntaxException;
}
