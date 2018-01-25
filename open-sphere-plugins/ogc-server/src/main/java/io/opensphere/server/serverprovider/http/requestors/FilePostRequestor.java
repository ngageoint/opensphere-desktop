package io.opensphere.server.serverprovider.http.requestors;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.CancellableInputStream;

/**
 * Posts a file to the server.
 */
public interface FilePostRequestor
{
    /**
     * Sends a file to the specified url.
     *
     * @param postToURL the post to url
     * @param metaDataParts map of name to data for parts to precede the main
     *            file part
     * @param fileToPost the file to post
     * @param response the response
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException Thrown if the url could not convert to a URI.
     */
    CancellableInputStream postFileToServer(URL postToURL, Map<String, String> metaDataParts, File fileToPost,
            ResponseValues response)
        throws IOException, URISyntaxException;

    /**
     * Sends a file to the specified url.
     *
     * @param postToURL the post to url
     * @param fileToPost the file to post
     * @param response the response
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException Thrown if the url could not convert to a URI.
     */
    CancellableInputStream postFileToServer(URL postToURL, File fileToPost, ResponseValues response)
        throws IOException, URISyntaxException;
}
