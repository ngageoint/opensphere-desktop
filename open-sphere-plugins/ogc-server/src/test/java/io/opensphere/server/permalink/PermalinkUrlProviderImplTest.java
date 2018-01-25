package io.opensphere.server.permalink;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.common.connection.ServerConfiguration;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.server.toolbox.ServerListManager;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;

/**
 * Tests the PermalinkUrlProviderImpl class.
 */
public class PermalinkUrlProviderImplTest
{
    /**
     * The test host name.
     */
    private static final String ourHost = "host";

    /**
     * The expected permalink.
     */
    private static final String ourPermalink = "/tools/fileDownload.do";

    /**
     * The expected server.
     */
    private static final String ourServerName = "the server";

    /**
     * Tests getting the permalink url.
     */
    @Test
    public void testGetPermalinkUrl()
    {
        EasyMockSupport support = new EasyMockSupport();

        ServerSourceController controller = createController(support);
        ServerSourceControllerManager controllerManager = createManager(support, controller);
        ServerListManager listManager = createServerManager(support);

        support.replayAll();

        PermalinkUrlProviderImpl provider = new PermalinkUrlProviderImpl(controllerManager, listManager);
        String permalink = provider.getPermalinkUrl(ourHost);

        assertEquals(ourPermalink, permalink);

        support.verifyAll();
    }

    /**
     * Creates the easy mocked server source controller.
     *
     * @param support Used to create the mock.
     * @return The controller.
     */
    private ServerSourceController createController(EasyMockSupport support)
    {
        ServerSourceController controller = support.createMock(ServerSourceController.class);
        controller.getSourceList();
        EasyMock.expectLastCall().andReturn(createSources());

        return controller;
    }

    /**
     * Creates the manager.
     *
     * @param support Used to create the mock.
     * @param controller The controller.
     * @return The easy mock manager.
     */
    private ServerSourceControllerManager createManager(EasyMockSupport support, ServerSourceController controller)
    {
        ServerSourceControllerManager manager = support.createMock(ServerSourceControllerManager.class);
        manager.getControllers();
        EasyMock.expectLastCall().andReturn(New.list(controller));

        return manager;
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
