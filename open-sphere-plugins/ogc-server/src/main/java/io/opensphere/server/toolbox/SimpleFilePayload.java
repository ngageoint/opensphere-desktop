package io.opensphere.server.toolbox;

import java.io.File;

import io.opensphere.core.server.HttpServer;
import io.opensphere.server.permalink.loaders.FileUploader;

/**
 * Default implementation of the {@link FilePayload} interface, in which a
 * simple file is configured for transmission to the remote server.
 */
public class SimpleFilePayload implements FilePayload
{
    /**
     * the file to be sent to the remote server as the payload of the
     * transaction.
     */
    private final File myFile;

    /**
     * Creates a new payload wrapping the supplied file.
     *
     * @param file the file to be sent to the remote server as the payload of
     *            the transaction.
     */
    public SimpleFilePayload(File file)
    {
        myFile = file;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.FilePayload#getFile()
     */
    @Override
    public File getFile()
    {
        return myFile;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.server.toolbox.FilePayload#createUploader(io.opensphere.core.server.HttpServer,
     *      java.lang.String)
     */
    @Override
    public FileUploader createUploader(HttpServer server, String url)
    {
        return new FileUploader(getFile(), server, url);
    }
}
