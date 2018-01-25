package io.opensphere.server.permalink.loaders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.server.util.JsonUtils;

/**
 * Uploads a specified file to a specified server.
 *
 */
public class FileUploader
{
    /**
     * The file to upload.
     */
    private final File myFile;

    /**
     * Used to communicate with the server.
     */
    private final HttpServer myServer;

    /**
     * The url to the permalink service.
     */
    private final String myUrl;

    /**
     * Constructs a new uploader.
     *
     * @param file The file data to upload.
     * @param server Used to communicate with the server.
     * @param url The permalink url.
     */
    public FileUploader(File file, HttpServer server, String url)
    {
        myFile = file;
        myServer = server;
        myUrl = url;
    }

    /**
     * Uploads the file to the server.
     *
     * @return The url pointing to the uploaded file.
     *
     * @throws IOException Thrown if error communicating with server.
     * @throws URISyntaxException If the url cannot be converted to a URI.
     */
    public String upload() throws IOException, URISyntaxException
    {
        ResponseValues response = new ResponseValues();

        String thePermalink = null;

        Map<String, String> metaDataParts = getMetadataParts();
        InputStream returnStream;
        if (metaDataParts.isEmpty())
        {
            returnStream = myServer.postFile(new URL(myUrl), myFile, response);
        }
        else
        {
            returnStream = getServer().postFile(new URL(getUrl()), metaDataParts, getFile(), response);
        }
        try
        {
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                ObjectMapper mapper = JsonUtils.createMapper();
                UploadResponse uploadResponse = mapper.readValue(returnStream, UploadResponse.class);
                if (uploadResponse.isSuccess())
                {
                    thePermalink = uploadResponse.getUrl();
                }
                else
                {
                    throw new IOException("File failed to upload.");
                }
            }
            else
            {
                throw new IOException(response.getResponseMessage());
            }
        }
        finally
        {
            returnStream.close();
        }

        return thePermalink;
    }

    /**
     * Gets the map of metadata fields to send to the server. Defaults to empty,
     * but may be overridden.
     *
     * @return the set of metadata fields to send to the server.
     */
    protected Map<String, String> getMetadataParts()
    {
        return Collections.emptyMap();
    }

    /**
     * Gets the value of the {@link #myFile} field.
     *
     * @return the value stored in the {@link #myFile} field.
     */
    public File getFile()
    {
        return myFile;
    }

    /**
     * Gets the value of the {@link #myServer} field.
     *
     * @return the value stored in the {@link #myServer} field.
     */
    public HttpServer getServer()
    {
        return myServer;
    }

    /**
     * Gets the value of the {@link #myUrl} field.
     *
     * @return the value stored in the {@link #myUrl} field.
     */
    public String getUrl()
    {
        return myUrl;
    }
}
