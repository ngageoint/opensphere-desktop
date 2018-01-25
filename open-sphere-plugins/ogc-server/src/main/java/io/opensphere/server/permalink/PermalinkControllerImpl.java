package io.opensphere.server.permalink;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import io.opensphere.core.server.HttpServer;
import io.opensphere.server.permalink.loaders.FileDownloader;
import io.opensphere.server.permalink.loaders.FileUploader;
import io.opensphere.server.toolbox.FilePayload;
import io.opensphere.server.toolbox.PermalinkController;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerSourceControllerManager;

/**
 * Handles uploading and downloading of files from a specified server.
 */
public class PermalinkControllerImpl implements PermalinkController
{
    /**
     * The url provider.
     */
    private final PermalinkUrlProviderImpl myUrlProvider;

    /**
     * Constructs a new controller.
     *
     * @param serverListManager Gets the list of active servers.
     * @param controllerManager Used to get the server configurations.
     */
    public PermalinkControllerImpl(ServerListManager serverListManager, ServerSourceControllerManager controllerManager)
    {
        myUrlProvider = new PermalinkUrlProviderImpl(controllerManager, serverListManager);
    }

    @Override
    public InputStream downloadFile(String fileUrl, HttpServer server) throws IOException, URISyntaxException
    {
        FileDownloader downloader = new FileDownloader(server, fileUrl);
        return downloader.downloadFile();
    }

    @Override
    public String getPermalinkUrl(String host)
    {
        return myUrlProvider.getPermalinkUrl(host);
    }

    @Override
    public String uploadFile(FilePayload payload, HttpServer server) throws IOException, URISyntaxException
    {
        String host = server.getHost();

        StringBuilder builder = new StringBuilder();
        builder.append(server.getProtocol());
        builder.append("://");
        builder.append(host);
        String hostUrl = builder.toString();
        builder.append(getPermalinkUrl(host));
        FileUploader uploader = payload.createUploader(server, builder.toString());

        return hostUrl + uploader.upload();
    }
}
