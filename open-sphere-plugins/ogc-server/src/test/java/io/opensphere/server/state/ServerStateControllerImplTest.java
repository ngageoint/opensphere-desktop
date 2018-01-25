package io.opensphere.server.state;

import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.easymock.EasyMock;
import org.junit.Test;
import org.w3c.dom.Node;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.state.activate.ServerActivator;
import io.opensphere.server.state.activate.ServerActivatorUtils;
import io.opensphere.server.state.activate.serversource.ServerSourceFilterer;
import io.opensphere.server.state.activate.serversource.ServerSourceProvider;
import io.opensphere.server.state.activate.serversource.genericserver.StateNodeUtils;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;
import io.opensphere.server.util.ServerConstants;

/**
 * Tests the ServerStateController.
 */
public class ServerStateControllerImplTest
{
    /**
     * The wfs url.
     */
    private static final String ourWfsUrl = "http://wfsHost/ogc/wfs";

    /**
     * Tests activating an empty list of servers.
     *
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad path.
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActivateEmptyListOfServers()
        throws ParserConfigurationException, XPathExpressionException, InterruptedException
    {
        Node stateNode = StateNodeUtils.createStateNode();

        ServerSourceProvider provider = ServerActivatorUtils.createSourceProvider(stateNode);
        ServerSourceController serverController = ServerActivatorUtils.createServerController(provider, null, null);
        ServerSourceControllerManager manager = ServerActivatorUtils.createControllerManager(serverController);
        EventManager eventManager = createEventManager();
        Toolbox toolbox = createToolbox(eventManager, null);

        EasyMock.replay(provider, serverController, manager, eventManager, toolbox);

        long currentTime = System.currentTimeMillis();
        final ServerStateControllerImpl controller = new ServerStateControllerImpl(toolbox, manager);
        controller.activateServers(stateNode);
        assertTrue(System.currentTimeMillis() - currentTime < 60000);

        EasyMock.verify(provider, serverController, manager, eventManager, toolbox);
    }

    /**
     * Tests activating servers.
     *
     * @throws XPathExpressionException Bad xpath.
     * @throws ParserConfigurationException Bad parse.
     * @throws JAXBException Bad jaxb.
     * @throws InterruptedException If the thread is interrupted.
     */
    @Test
    public void testActivateServers()
        throws XPathExpressionException, ParserConfigurationException, JAXBException, InterruptedException
    {
        Node stateNode = StateNodeUtils.createWfsNodeWithData(ourWfsUrl);

        ServerSourceFilterer filterer = ServerActivatorUtils.createSourceFilterer();
        ServerSourceProvider provider = ServerActivatorUtils.createSourceProvider(stateNode);
        ServerSourceController serverController = ServerActivatorUtils.createServerController(provider, filterer, null);
        ServerSourceControllerManager manager = ServerActivatorUtils.createControllerManager(serverController);
        EventManager eventManager = createEventManager();
        Preferences prefs = createPrefs();
        PreferencesRegistry prefsRegistry = createPrefsRegistry(prefs);

        Toolbox toolbox = createToolbox(eventManager, prefsRegistry);
        final ServerConnectionParams someHost = createParams("somehost");
        final ServerConnectionParams wfsHost = createParams("wfsHost");

        EasyMock.replay(filterer, provider, serverController, manager, eventManager, toolbox, someHost, wfsHost, prefsRegistry,
                prefs);

        final ServerStateControllerImpl controller = new ServerStateControllerImpl(toolbox, manager);

        ThreadUtilities.runBackground(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                }
                controller.activationComplete(new ServerConfigEvent(toString(), someHost, ServerEventAction.LOADCOMPLETE));
                try
                {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e)
                {
                }
                controller.activationComplete(new ServerConfigEvent(toString(), wfsHost, ServerEventAction.LOADCOMPLETE));
            }
        });

        long currentTime = System.currentTimeMillis();
        controller.activateServers(stateNode);
        assertTrue(System.currentTimeMillis() - currentTime < 7000);

        EasyMock.verify(filterer, provider, serverController, manager, eventManager, toolbox, someHost, wfsHost, prefsRegistry,
                prefs);
    }

    /**
     * Creates an easy mocked event manager.
     *
     * @return The event manager.
     */
    @SuppressWarnings("unchecked")
    private EventManager createEventManager()
    {
        EventManager eventManager = EasyMock.createMock(EventManager.class);
        eventManager.<ServerConfigEvent>subscribe(EasyMock.isA(Class.class), EasyMock.isA(ServerActivator.class));
        eventManager.<ServerConfigEvent>unsubscribe(EasyMock.isA(Class.class), EasyMock.isA(ServerActivator.class));

        return eventManager;
    }

    /**
     * Creates server parameters with the specified host name.
     *
     * @param hostName The host name.
     * @return The server connection params.
     */
    private ServerConnectionParams createParams(String hostName)
    {
        ServerConnectionParams params = EasyMock.createMock(ServerConnectionParams.class);

        params.getServerTitle();
        EasyMock.expectLastCall().andReturn(hostName);

        return params;
    }

    /**
     * Creates the prefs.
     *
     * @return the preferences
     */
    private Preferences createPrefs()
    {
        Preferences prefs = EasyMock.createMock(Preferences.class);
        prefs.getInt(EasyMock.isA(String.class), EasyMock.eq(ServerConstants.DEFAULT_SERVER_ACTIVATE_TIMEOUT));
        EasyMock.expectLastCall().andReturn(ServerConstants.DEFAULT_SERVER_ACTIVATE_TIMEOUT);
        return prefs;
    }

    /**
     * Creates the prefs registry.
     *
     * @param prefs the prefs
     * @return the preferences registry
     */
    private PreferencesRegistry createPrefsRegistry(Preferences prefs)
    {
        PreferencesRegistry prefsRegistry = EasyMock.createMock(PreferencesRegistry.class);

        prefsRegistry.getPreferences(EasyMock.isA(Class.class));
        EasyMock.expectLastCall().andReturn(prefs);

        return prefsRegistry;
    }

    /**
     * Create the system toolbox.
     *
     * @param eventManager The event manager.
     * @param prefsRegistry the prefs registry
     * @return The system toolbox.
     */
    private Toolbox createToolbox(EventManager eventManager, PreferencesRegistry prefsRegistry)
    {
        Toolbox toolbox = EasyMock.createMock(Toolbox.class);
        toolbox.getEventManager();
        EasyMock.expectLastCall().andReturn(eventManager);

        if (prefsRegistry != null)
        {
            toolbox.getPreferencesRegistry();
            EasyMock.expectLastCall().andReturn(prefsRegistry);
        }

        return toolbox;
    }
}
