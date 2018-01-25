package io.opensphere.overlay;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.MapManager;
import io.opensphere.core.UnitsRegistry;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.ControlRegistryImpl;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.util.SelectionMode;

/**
 * Test for Overlay controls.
 */
public class SelectionRegionControlsTest
{
    /** The screen coordinates for point 1. */
    private static final Vector2i POINT1 = new Vector2i(5, 10);

    /** The screen coordinates for point 2. */
    private static final Vector2i POINT2 = new Vector2i(10, 15);

    /**
     * Inject some events that shouldn't cause any response.
     */
    @Test
    public void testWrongControls()
    {
        ControlRegistry controlRegistry = new ControlRegistryImpl();
        SelectionModeController smc = new SelectionModeControllerImpl(null);
        ControlContext controlContext = controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        SelectionRegionTransformer transformer = new SelectionRegionTransformer();
        SelectionHandler handler = EasyMock.createNiceMock(SelectionHandler.class);
        EasyMock.replay(handler);
        SelectionRegionControls controls = new SelectionRegionControls(null, null, null, null, transformer, handler, smc);
        controls.register(controlRegistry);

        Component source = new EmptyComponent();

        int maskWithoutShift = -1 ^ InputEvent.SHIFT_DOWN_MASK ^ InputEvent.SHIFT_MASK;
        MouseEvent e4 = new MouseEvent(source, MouseEvent.MOUSE_PRESSED, 0, maskWithoutShift | InputEvent.BUTTON1_DOWN_MASK,
                POINT1.getX(), POINT1.getY(), 0, 0, 1, false, MouseEvent.BUTTON1);
        controlContext.mousePressed(e4);
        assertFalse(e4.isConsumed());

        MouseEvent e5 = new MouseEvent(source, MouseEvent.MOUSE_DRAGGED, 0, maskWithoutShift | InputEvent.BUTTON1_DOWN_MASK,
                POINT2.getX(), POINT2.getY(), 0, 0, 1, false, MouseEvent.BUTTON1);
        controlContext.mouseDragged(e5);
        assertFalse(e5.isConsumed());
        assertTrue(transformer.getGeometries().isEmpty());

        // Test different buttons
        MouseEvent e6 = new MouseEvent(source, MouseEvent.MOUSE_PRESSED, 0,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK, POINT1.getX(), POINT1.getY(), 0, 0, 1, false,
                MouseEvent.BUTTON2);
        controlContext.mousePressed(e6);
        assertFalse(e6.isConsumed());

        MouseEvent e7 = new MouseEvent(source, MouseEvent.MOUSE_DRAGGED, 0,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK, POINT2.getX(), POINT2.getY(), 0, 0, 1, false,
                MouseEvent.BUTTON2);
        controlContext.mouseDragged(e7);
        assertFalse(e7.isConsumed());
        assertTrue(transformer.getGeometries().isEmpty());

        MouseEvent e8 = new MouseEvent(source, MouseEvent.MOUSE_PRESSED, 0,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK, POINT1.getX(), POINT1.getY(), 0, 0, 1, false,
                MouseEvent.BUTTON3);
        controlContext.mousePressed(e8);
        assertFalse(e8.isConsumed());

        MouseEvent e9 = new MouseEvent(source, MouseEvent.MOUSE_DRAGGED, 0,
                InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK, POINT2.getX(), POINT2.getY(), 0, 0, 1, false,
                MouseEvent.BUTTON3);
        controlContext.mouseDragged(e9);
        assertFalse(e9.isConsumed());
        assertTrue(transformer.getGeometries().isEmpty());
    }

    /**
     * Tests relinquishing context.
     */
    @Test
    public void testRegionContext()
    {
        EasyMockSupport support = new EasyMockSupport();

        SelectionModeController controller = createController(support);

        EventManager eventManager = support.createMock(EventManager.class);
        MapManager mapManager = support.createMock(MapManager.class);
        UnitsRegistry unitsRegistry = support.createMock(UnitsRegistry.class);
        SelectionHandler handler = support.createMock(SelectionHandler.class);

        support.replayAll();

        SelectionRegionControls controls = new SelectionRegionControls(eventManager, mapManager, unitsRegistry, null, null, handler, controller);
        controls.usurpRegionContext("context", SelectionMode.BOUNDING_BOX);
        controls.relinquishRegionContext("context");

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link SelectionModeController}.
     *
     * @param support Used to create the controller.
     * @return The controller.
     */
    private SelectionModeController createController(EasyMockSupport support)
    {
        SelectionModeController controller = support.createMock(SelectionModeController.class);

        controller.addSelectionModeChangeListener(EasyMock.isA(SelectionModeChangeListener.class));
        EasyMock.expect(controller.getDefaultSelectionMode()).andReturn(SelectionMode.CIRCLE);
        controller.setSelectionMode(SelectionMode.BOUNDING_BOX);
        controller.setSelectionMode(SelectionMode.CIRCLE);
        controller.setSelectionMode(SelectionMode.NONE);

        return controller;
    }

    /** Just an empty Component implementation. */
    @SuppressWarnings("serial")
    private static final class EmptyComponent extends Component
    {
    }
}
