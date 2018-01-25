package io.opensphere.server.toolbox;

import java.io.File;

import io.opensphere.core.server.HttpServer;
import io.opensphere.server.permalink.loaders.FileUploader;

/**
 * The payload for a file upload transaction.
 */
public interface FilePayload
{
    /**
     * Gets the file handle for the upload transaction, representing the local
     * reference of the file to be uploaded.
     *
     * @return the local reference of the file to be uploaded.
     */
    File getFile();

    /**
     * A factory method used to create a file uploader, configured to upload the
     * payload instance to the supplied server.
     *
     * @param server the server to which the uploader will point.
     * @param url the URL on the server to which the file will be uploaded.
     * @return a {@link FileUploader} configured to send the payload to the
     *         remote server.
     */
    FileUploader createUploader(HttpServer server, String url);
}
