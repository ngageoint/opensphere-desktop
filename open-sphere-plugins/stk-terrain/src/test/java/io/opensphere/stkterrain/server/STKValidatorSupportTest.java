package io.opensphere.stkterrain.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Notify;
import io.opensphere.core.Toolbox;
import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * Unit test for {@link STKValidatorSupport}.
 */
public class STKValidatorSupportTest
{
    /**
     * Tests setting validation result of success.
     */
    @Test
    public void testSetValidationResult()
    {
        EasyMockSupport support = new EasyMockSupport();

        IDataSource server = support.createMock(IDataSource.class);
        STKServerController serverController = support.createMock(STKServerController.class);

        support.replayAll();

        STKValidatorSupport validatorSupport = new STKValidatorSupport(server, serverController);
        validatorSupport.setValidationResult(ValidationStatus.VALID, null);

        support.verifyAll();
    }

    /**
     * Tests setting validation result of success.
     */
    @Test
    public void testSetValidationResultError()
    {
        EasyMockSupport support = new EasyMockSupport();

        IDataSource server = createServer(support);
        STKServerController serverController = createController(support, server);
        List<UserMessageEvent> events = New.list();
        createToolbox(support, events);

        support.replayAll();

        STKValidatorSupport validatorSupport = new STKValidatorSupport(server, serverController);
        validatorSupport.setValidationResult(ValidationStatus.ERROR, "Error Message");

        assertEquals(1, events.size());
        UserMessageEvent event = events.get(0);
        assertEquals(Type.ERROR, event.getType());
        assertEquals("Error Message", event.getMessage());
        assertFalse(event.isMakeVisible());
        assertTrue(event.isShowToast());

        support.verifyAll();
    }

    /**
     * Tests setting validation result of success.
     */
    @Test
    public void testSetValidationResultWarning()
    {
        EasyMockSupport support = new EasyMockSupport();

        IDataSource server = support.createMock(IDataSource.class);
        STKServerController serverController = support.createMock(STKServerController.class);
        List<UserMessageEvent> events = New.list();
        createToolbox(support, events);

        support.replayAll();

        STKValidatorSupport validatorSupport = new STKValidatorSupport(server, serverController);
        validatorSupport.setValidationResult(ValidationStatus.WARNING, "Warning Message");

        assertEquals(1, events.size());
        UserMessageEvent event = events.get(0);
        assertEquals(Type.WARNING, event.getType());
        assertEquals("Warning Message", event.getMessage());
        assertFalse(event.isMakeVisible());
        assertTrue(event.isShowToast());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link STKServerController}.
     *
     * @param support Used to create the mock.
     * @param server The expected server to be deactivated.
     * @return The mocked {@link STKServerController}.
     */
    private STKServerController createController(EasyMockSupport support, IDataSource server)
    {
        STKServerController controller = support.createMock(STKServerController.class);

        controller.deactivateSource(EasyMock.eq(server));

        return controller;
    }

    /**
     * Creates an easy mocked server source.
     *
     * @param support Used to create the mock.
     * @return The mocked server source.
     */
    private IDataSource createServer(EasyMockSupport support)
    {
        IDataSource source = support.createMock(IDataSource.class);

        source.setLoadError(EasyMock.eq(true), EasyMock.isA(STKValidatorSupport.class));

        return source;
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the mock.
     * @param publishedEvents The list to add published events to.
     * @return The mocked {@link Toolbox}
     */
    private Toolbox createToolbox(EasyMockSupport support, List<UserMessageEvent> publishedEvents)
    {
        EventManager eventManager = support.createMock(EventManager.class);

        eventManager.publishEvent(EasyMock.isA(UserMessageEvent.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            publishedEvents.add((UserMessageEvent)EasyMock.getCurrentArguments()[0]);
            return null;
        });

        Toolbox toolbox = support.createMock(Toolbox.class);

        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);

        Notify.setToolbox(toolbox);

        return toolbox;
    }
}
