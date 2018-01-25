package io.opensphere.server.toolbox.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.server.toolbox.ServerSourceController;

/**
 * Unit test for {@link ServerSourceControllerManagerImpl}.
 */
public class ServerSourceControllerManagerImplTests
{
    /**
     * Tests opening the controllers.
     */
    @Test
    public void testOpenControllers()
    {
        EasyMockSupport support = new EasyMockSupport();

        Toolbox toolbox = createToolbox(support);
        List<ServerSourceController> controllers = createControllers(support, toolbox);

        support.replayAll();

        ServerSourceControllerManagerImpl manager = new ServerSourceControllerManagerImpl(toolbox, getClass(), null);
        manager.openControllers(controllers);

        Iterator<ServerSourceController> iterator = manager.getControllers().iterator();
        assertEquals(controllers.get(2), iterator.next());
        assertEquals(controllers.get(1), iterator.next());
        assertFalse(iterator.hasNext());

        support.verifyAll();
    }

    /**
     * Creates easy mocked test {@link ServerSourceController}s.
     *
     * @param support Used to create the mock.
     * @param toolbox The expected {@link Toolbox} to be passed to the
     *            controller.
     * @return A list of mocked controllers.
     */
    private List<ServerSourceController> createControllers(EasyMockSupport support, Toolbox toolbox)
    {
        ServerSourceController overidable = support.createMock(ServerSourceController.class);
        EasyMock.expect(Boolean.valueOf(overidable.overridesController(EasyMock.isA(ServerSourceController.class)))).andReturn(Boolean.FALSE)
                .atLeastOnce();

        ServerSourceController other = support.createMock(ServerSourceController.class);
        EasyMock.expect(Boolean.valueOf(other.overridesController(EasyMock.isA(ServerSourceController.class)))).andReturn(Boolean.FALSE).atLeastOnce();
        other.open(EasyMock.eq(toolbox), EasyMock.anyObject());
        EasyMock.expect(other.getTypeNames()).andReturn(New.list("otherTypeName"));
        EasyMock.expect(Integer.valueOf(other.getOrdinal())).andReturn(Integer.valueOf(2)).atLeastOnce();

        ServerSourceController overider = support.createMock(ServerSourceController.class);
        EasyMock.expect(Boolean.valueOf(overider.overridesController(overidable))).andReturn(Boolean.TRUE).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(overider.overridesController(overider))).andReturn(Boolean.FALSE).atLeastOnce();
        EasyMock.expect(Boolean.valueOf(overider.overridesController(other))).andReturn(Boolean.FALSE).atLeastOnce();
        overider.open(EasyMock.eq(toolbox), EasyMock.anyObject());
        EasyMock.expect(overider.getTypeNames()).andReturn(New.list("overiderTypeName"));
        EasyMock.expect(Integer.valueOf(overider.getOrdinal())).andReturn(Integer.valueOf(1)).atLeastOnce();

        return New.list(overidable, other, overider);
    }

    /**
     * Creates an easy mocked {@link Toolbox}.
     *
     * @param support Used to create the {@link Toolbox}.
     * @return The mocked toolbox.
     */
    @SuppressWarnings("unchecked")
    private Toolbox createToolbox(EasyMockSupport support)
    {
        EventManager eventManager = support.createMock(EventManager.class);
        eventManager.subscribe(EasyMock.eq(ApplicationLifecycleEvent.class), EasyMock.isA(EventListener.class));

        Toolbox toolbox = support.createMock(Toolbox.class);
        EasyMock.expect(toolbox.getEventManager()).andReturn(eventManager);

        return toolbox;
    }
}
