package io.opensphere.server.permalink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.toolbox.SimpleFilePayload;
import io.opensphere.test.core.matchers.EasyMockHelper;

/**
 * Tests the permalink controller.
 *
 */
public class PermalinkControllerImplTest
{
    /**
     * The download url returned by the server.
     */
    private static final String ourDownloadUrl = "/tools/fileDownload.do";

    /**
     * The full download url.
     */
    private static final String ourFullDownloadURL = "http://somehost/tools/fileDownload.do";

    /**
     * The test host.
     */
    private static final String ourHost = "someHost";

    /**
     * The expected permalink.
     */
    private static final String ourPermalink = "/file-store/v1";

    /**
     * The test protocol.
     */
    private static final String ourProtocol = "http";

    /**
     * The expected server.
     */
    private static final String ourServerName = "the server";

    /**
     * The test file.
     */
    private static final File ourTestFile = new File(".");

    /**
     * Tests downloading a state file.
     *
     * @throws IOException Bad server.
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testDownloadStateFile() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();
        ServerListManager serverListManager = support.createMock(ServerListManager.class);
        ServerSourceControllerManager controllerManager = support.createMock(ServerSourceControllerManager.class);
        HttpServer server = createServerForDownload(support);

        support.replayAll();

        PermalinkControllerImpl permalinker = new PermalinkControllerImpl(serverListManager, controllerManager);
        InputStream stream = permalinker.downloadFile(ourFullDownloadURL, server);

        assertNotNull(stream);

        support.verifyAll();
    }

    /**
     * Tests getting the permalink url.
     */
    @Test
    public void testGetPermalinkUrl()
    {
        EasyMockSupport support = new EasyMockSupport();
        ServerListManager serverListManager = createServerManager(support);
        ServerSourceControllerManager controllerManager = createManager(support);

        support.replayAll();

        PermalinkControllerImpl permalinker = new PermalinkControllerImpl(serverListManager, controllerManager);
        String permaLink = permalinker.getPermalinkUrl(ourHost);

        assertEquals(ourPermalink, permaLink);

        support.verifyAll();
    }

    /**
     * Tests uploading a state file.
     *
     * @throws IOException Bad server
     * @throws URISyntaxException Bad URI.
     */
    @Test
    public void testUploadStateFile() throws IOException, URISyntaxException
    {
        EasyMockSupport support = new EasyMockSupport();
        ServerListManager serverListManager = createServerManager(support);
        ServerSourceControllerManager controllerManager = createManager(support);
        HttpServer server = createServerForUpload(support);

        support.replayAll();

        PermalinkControllerImpl permalinker = new PermalinkControllerImpl(serverListManager, controllerManager);
        String permalinkUrl = permalinker.uploadFile(new SimpleFilePayload(ourTestFile), server);

        StringBuilder builder = new StringBuilder();
        builder.append(ourProtocol);
        builder.append("://");
        builder.append(ourHost);
        builder.append(ourDownloadUrl);

        assertEquals(builder.toString(), permalinkUrl);

        support.verifyAll();
    }

    /**
     * Creates the manager.
     *
     * @param support The easy mock support to create mocks with.
     * @return The easy mock manager.
     */
    private ServerSourceControllerManager createManager(EasyMockSupport support)
    {
        ServerSourceController controller = support.createMock(ServerSourceController.class);
        controller.getSourceList();
        EasyMock.expectLastCall().andReturn(createSources());

        ServerSourceControllerManager manager = support.createMock(ServerSourceControllerManager.class);
        manager.getControllers();
        EasyMock.expectLastCall().andReturn(New.list(controller));

        return manager;
    }

    /**
     * Creates an easy mocked rest server for download.
     *
     * @param support Used to create the mock.
     * @return The server.
     * @throws MalformedURLException Bad url
     * @throws IOException Bad IO.
     * @throws URISyntaxException Bad URI.
     */
    private HttpServer createServerForDownload(EasyMockSupport support)
        throws MalformedURLException, IOException, URISyntaxException
    {
        HttpServer server = support.createMock(HttpServer.class);
        IAnswer<CancellableInputStream> answer = new IAnswer<CancellableInputStream>()
        {
            @Override
            public CancellableInputStream answer()
            {
                ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[1];
                response.setResponseCode(HttpURLConnection.HTTP_OK);

                ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
                return new CancellableInputStream(stream, null);
            }
        };
        EasyMock.expect(server.sendGet(EasyMockHelper.eq(new URL(ourFullDownloadURL)), EasyMock.isA(ResponseValues.class)))
                .andAnswer(answer);

        return server;
    }

    /**
     * Creates the rest server for upload file.
     *
     * @param support Used to create the mock.
     * @return The rest server.
     * @throws IOException Bad server.
     * @throws URISyntaxException bad uri.
     */
    private HttpServer createServerForUpload(EasyMockSupport support) throws IOException, URISyntaxException
    {
        HttpServer server = support.createMock(HttpServer.class);
        IAnswer<CancellableInputStream> answer = new IAnswer<CancellableInputStream>()
        {
            @Override
            public CancellableInputStream answer()
            {
                ResponseValues response = (ResponseValues)EasyMock.getCurrentArguments()[2];
                response.setResponseCode(HttpURLConnection.HTTP_OK);

                String jsonString = "{ \"success\": true, \"url\": \"" + ourDownloadUrl + "\" }";
                return new CancellableInputStream(new ByteArrayInputStream(jsonString.getBytes(StringUtilities.DEFAULT_CHARSET)),
                        null);
            }
        };
        EasyMock.expect(server.postFile(EasyMock.isA(URL.class), EasyMock.eq(ourTestFile), EasyMock.isA(ResponseValues.class)))
                .andAnswer(answer);
        server.getProtocol();
        EasyMock.expectLastCall().andReturn(ourProtocol);
        server.getHost();
        EasyMock.expectLastCall().andReturn(ourHost);

        return server;
    }

    /**
     * Creates an easy mocked ServerListManager.
     *
     * @param support Used to create the mocks.
     * @return The server list manager.
     */
    private ServerListManager createServerManager(EasyMockSupport support)
    {
        ServerListManager manager = support.createMock(ServerListManager.class);

        ServerConnectionParams params = support.createMock(ServerConnectionParams.class);
        params.getServerTitle();
        EasyMock.expectLastCall().andReturn(ourServerName);
        params.getServerConfiguration();

        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setHost(ourHost);

        EasyMock.expectLastCall().andReturn(configuration);

        manager.getActiveServers();
        EasyMock.expectLastCall().andReturn(New.list(params));

        return manager;
    }

    /**
     * Creates some mocked up server sources.
     *
     * @return The list of test sources.
     */
    private List<IDataSource> createSources()
    {
        List<IDataSource> serverSources = New.list();

        OGCServerSource source = new OGCServerSource();
        source.setPermalinkUrl("permalink");
        source.setName("a server");
        serverSources.add(source);

        source = new OGCServerSource();
        source.setPermalinkUrl(ourPermalink);
        source.setName(ourServerName);
        serverSources.add(source);

        return serverSources;
    }
}
