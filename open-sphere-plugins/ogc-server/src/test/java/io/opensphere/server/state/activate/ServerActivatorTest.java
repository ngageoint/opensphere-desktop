package io.opensphere.server.state.activate;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.w3c.dom.Node;

import io.opensphere.core.event.EventManager;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.server.services.ServerConfigEvent;
import io.opensphere.server.services.ServerConfigEvent.ServerEventAction;
import io.opensphere.server.state.activate.serversource.IActivationListener;
import io.opensphere.server.state.activate.serversource.ServerSourceFilterer;
import io.opensphere.server.state.activate.serversource.ServerSourceProvider;
import io.opensphere.server.state.activate.serversource.genericserver.StateNodeUtils;
import io.opensphere.server.toolbox.ServerSourceController;
import io.opensphere.server.toolbox.ServerSourceControllerManager;

/**
 * Tests the ServerActivator class.
 *
 */
public class ServerActivatorTest
{
    /**
     * The wfs only server for testing.
     */
    private static final String ourWfsServer = "http://wfsHost/ogc/wfs";

    /**
     * Indicates if the wfs server was activated.
     */
    private boolean myIsWfsActivated;

    /**
     * Indicates if the wms server was activated.
     */
    private boolean myIsWmsActivated;

    /**
     * Tests activating the servers.
     *
     * @throws JAXBException Bad jaxb.
     * @throws ParserConfigurationException Bad parse.
     * @throws XPathExpressionException Bad xpath.
     */
    @Test
    public void testActivateServers() throws XPathExpressionException, ParserConfigurationException, JAXBException
    {
        myIsWfsActivated = false;
        myIsWmsActivated = false;

        Node stateNode = StateNodeUtils.createWfsNodeWithData(ourWfsServer);

        ServerSourceProvider provider = ServerActivatorUtils.createSourceProvider(stateNode);
        ServerSourceFilterer filterer = ServerActivatorUtils.createSourceFilterer();
        ServerSourceController controller = ServerActivatorUtils.createServerController(provider, filterer, new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                IDataSource source = (IDataSource)EasyMock.getCurrentArguments()[0];

                if (source.getName().equals("wfsHost"))
                {
                    myIsWfsActivated = true;
                }
                else if (source.getName().equals("somehost"))
                {
                    myIsWmsActivated = true;
                }

                return null;
            }
        });
        ServerSourceControllerManager controllerManager = ServerActivatorUtils.createControllerManager(controller);
        EventManager eventManager = createEventManager();
        IActivationListener activationListener = createListener();

        EasyMock.replay(provider, filterer, controller, controllerManager, eventManager, activationListener);

        ServerActivator activator = new ServerActivator(controllerManager, eventManager, activationListener);
        activator.activateServers(stateNode);

        EasyMock.verify(provider, filterer, controller, controllerManager, eventManager, activationListener);

        assertTrue(myIsWfsActivated);
        assertTrue(myIsWmsActivated);
    }

    /**
     * Tests the close method.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testClose()
    {
        ServerSourceControllerManager controllerManager = EasyMock.createNiceMock(ServerSourceControllerManager.class);
        IActivationListener activationListener = EasyMock.createNiceMock(IActivationListener.class);
        EventManager eventManager = createEventManager();
        eventManager.<ServerConfigEvent>unsubscribe(EasyMock.isA(Class.class), EasyMock.isA(ServerActivator.class));

        EasyMock.replay(eventManager, controllerManager, activationListener);

        ServerActivator activator = new ServerActivator(controllerManager, eventManager, activationListener);
        activator.close();

        EasyMock.verify(eventManager, controllerManager, activationListener);
    }

    /**
     * Test notifying of config changes.
     */
    @Test
    public void testNotifyServerConfigEvent()
    {
        ServerConfigEvent expectedEvent = new ServerConfigEvent(toString(), null, ServerEventAction.LOADCOMPLETE);

        ServerSourceControllerManager controllerManager = EasyMock.createNiceMock(ServerSourceControllerManager.class);
        IActivationListener activationListener = EasyMock.createNiceMock(IActivationListener.class);
        activationListener.activationComplete(EasyMock.eq(expectedEvent));

        EventManager eventManager = createEventManager();

        EasyMock.replay(eventManager, controllerManager, activationListener);

        ServerActivator activator = new ServerActivator(controllerManager, eventManager, activationListener);
        activator.notify(new ServerConfigEvent(toString(), null, ServerEventAction.ACTIVATE));
        activator.notify(expectedEvent);

        EasyMock.verify(eventManager, controllerManager, activationListener);
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

        return eventManager;
    }

    /**
     * Creates an easy mocked activation listener.
     *
     * @return The listener.
     */
    @SuppressWarnings("unchecked")
    private IActivationListener createListener()
    {
        IActivationListener listener = EasyMock.createMock(IActivationListener.class);
        listener.activatingServers(EasyMock.isA(List.class));
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                List<IDataSource> servers = (List<IDataSource>)EasyMock.getCurrentArguments()[0];
                ServerActivatorUtils.assertActivatingServers(servers);
                return null;
            }
        });

        return listener;
    }
}
