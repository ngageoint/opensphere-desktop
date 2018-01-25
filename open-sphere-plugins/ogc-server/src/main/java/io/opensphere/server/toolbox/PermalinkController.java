package io.opensphere.server.toolbox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import io.opensphere.core.server.HttpServer;

/**
 * Interface to a controller responsible for permalink functionalities on a
 * server.
 */
public interface PermalinkController extends PermalinkUrlProvider
{
    /**
     * Downloads a file from the specified url.
     *
     * @param fileUrl The url of the file to download.
     * @param server The server connection to use to download the file.
     * @return The file data.
     * @throws IOException Thrown if error with communicating with the server.
     * @throws URISyntaxException If the server url could not be converted to a
     *             URI.
     */
    InputStream downloadFile(String fileUrl, HttpServer server) throws IOException, URISyntaxException;

    /**
     * Uploads the file to the server.
     *
     * @param payload The file data to upload to the server.
     * @param server The server connection to use to upload the file.
     * @return The URL pointing to the uploaded file.
     *
     * @throws IOException Thrown if error communicating with server.
     * @throws URISyntaxException If the server URL could not be converted to a
     *             URI.
     *
     */
    String uploadFile(FilePayload payload, HttpServer server) throws IOException, URISyntaxException;
}
