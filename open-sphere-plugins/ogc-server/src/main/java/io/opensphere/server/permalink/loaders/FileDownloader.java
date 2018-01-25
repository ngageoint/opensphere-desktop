package io.opensphere.server.permalink.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;

/**
 * Communicates with a server and downloads a specified file from that server.
 *
 */
public class FileDownloader
{
    /**
     * The url to the file.
     */
    private final String myFileUrl;

    /**
     * Used to communicate with the server.
     */
    private final HttpServer myServer;

    /**
     * Constructs a new file downloader.
     *
     * @param server Used to communicate with the server.
     * @param fileUrl The url to the file to download.
     */
    public FileDownloader(HttpServer server, String fileUrl)
    {
        myServer = server;
        myFileUrl = fileUrl;
    }

    /**
     * Downloads a file from the specified url.
     *
     * @return The file data.
     * @throws IOException Thrown if error with communicating with the server.
     * @throws URISyntaxException If the fileUrl cannot be converted to a URI.
     */
    public InputStream downloadFile() throws IOException, URISyntaxException
    {
        URL url = new URL(myFileUrl);
        ResponseValues response = new ResponseValues();

        @SuppressWarnings("PMD.PrematureDeclaration")
        InputStream stateStream = myServer.sendGet(url, response);

        if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
        {
            throw new IOException(response.getResponseMessage());
        }

        return stateStream;
    }
}
