package io.opensphere.subterrain.xraygoggles.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.PolylineGeometry.Builder;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Unit test for {@link WindowResizer}.
 */
public class WindowResizerTest
{
    /**
     * The discrete listener.
     */
    private DiscreteEventListener myDiscreteListener;

    /**
     * The pick listener.
     */
    private PickListener myPickListener;

    /**
     * Tests handling the geometry pick event.
     */
    @Test
    public void testPick()
    {
        EasyMockSupport support = new EasyMockSupport();

        ControlRegistry controlRegistry = createControlRegistry(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(15, 5), new ScreenPosition(21, 5), new ScreenPosition(15, 10),
                new ScreenPosition(21, 10));
        PolylineGeometry geometry = createGeometry(model);
        model.setWindowGeometry(geometry);
        WindowResizer resizer = new WindowResizer(controlRegistry, model);

        PickListener.PickEvent pickEvent = new PickListener.PickEvent(geometry, new Point(10, 5));
        myPickListener.handlePickEvent(pickEvent);
        assertTrue(myDiscreteListener.isTargeted());

        pickEvent = new PickListener.PickEvent(null, new Point(0, 0));
        myPickListener.handlePickEvent(pickEvent);
        assertFalse(myDiscreteListener.isTargeted());

        resizer.close();

        assertNull(myPickListener);
        assertNull(myDiscreteListener);

        support.verifyAll();
    }

    /**
     * Tests resizing the window.
     */
    @Test
    public void testResizingWindow()
    {
        EasyMockSupport support = new EasyMockSupport();

        ControlRegistry controlRegistry = createControlRegistry(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(5, 5), new ScreenPosition(10, 5), new ScreenPosition(5, 10),
                new ScreenPosition(10, 10));
        PolylineGeometry geometry = createGeometry(model);
        model.setWindowGeometry(geometry);
        WindowResizer resizer = new WindowResizer(controlRegistry, model);

        PickListener.PickEvent pickEvent = new PickListener.PickEvent(geometry, new Point(10, 5));
        myPickListener.handlePickEvent(pickEvent);
        assertTrue(myDiscreteListener.isTargeted());

        JPanel panel = new JPanel();
        MouseEvent event = new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 10, 5, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertTrue(event.isConsumed());

        event = new MouseEvent(panel, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, 11, 4, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertTrue(event.isConsumed());
        assertEquals(4, model.getUpperLeft().getX(), 0d);
        assertEquals(4, model.getUpperLeft().getY(), 0d);
        assertEquals(11, model.getUpperRight().getX(), 0d);
        assertEquals(4, model.getUpperRight().getY(), 0d);
        assertEquals(4, model.getLowerLeft().getX(), 0d);
        assertEquals(10, model.getLowerLeft().getY(), 0d);
        assertEquals(11, model.getLowerRight().getX(), 0d);
        assertEquals(10, model.getLowerRight().getY(), 0d);

        event = new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 11, 4, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertFalse(myDiscreteListener.isTargeted());

        resizer.close();

        assertNull(myPickListener);
        assertNull(myDiscreteListener);

        support.verifyAll();
    }

    /**
     * Tests resizing the window.
     */
    @Test
    public void testResizingWindowMax()
    {
        EasyMockSupport support = new EasyMockSupport();

        ControlRegistry controlRegistry = createControlRegistry(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(5, 5), new ScreenPosition(10, 5), new ScreenPosition(5, 10),
                new ScreenPosition(10, 10));
        PolylineGeometry geometry = createGeometry(model);
        model.setWindowGeometry(geometry);
        XrayWindowValidator validator = new XrayWindowValidator(model);
        WindowResizer resizer = new WindowResizer(controlRegistry, model);

        PickListener.PickEvent pickEvent = new PickListener.PickEvent(geometry, new Point(10, 5));
        myPickListener.handlePickEvent(pickEvent);
        assertTrue(myDiscreteListener.isTargeted());

        JPanel panel = new JPanel();
        MouseEvent event = new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 10, 5, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertTrue(event.isConsumed());

        event = new MouseEvent(panel, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, 12, 4, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertTrue(event.isConsumed());
        assertEquals(5, model.getUpperLeft().getX(), 0d);
        assertEquals(5, model.getUpperLeft().getY(), 0d);
        assertEquals(10, model.getUpperRight().getX(), 0d);
        assertEquals(5, model.getUpperRight().getY(), 0d);
        assertEquals(5, model.getLowerLeft().getX(), 0d);
        assertEquals(10, model.getLowerLeft().getY(), 0d);
        assertEquals(10, model.getLowerRight().getX(), 0d);
        assertEquals(10, model.getLowerRight().getY(), 0d);

        event = new MouseEvent(panel, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, 11, -1, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertTrue(event.isConsumed());
        assertEquals(5, model.getUpperLeft().getX(), 0d);
        assertEquals(5, model.getUpperLeft().getY(), 0d);
        assertEquals(10, model.getUpperRight().getX(), 0d);
        assertEquals(5, model.getUpperRight().getY(), 0d);
        assertEquals(5, model.getLowerLeft().getX(), 0d);
        assertEquals(10, model.getLowerLeft().getY(), 0d);
        assertEquals(10, model.getLowerRight().getX(), 0d);
        assertEquals(10, model.getLowerRight().getY(), 0d);

        event = new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 11, 4, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertFalse(myDiscreteListener.isTargeted());

        resizer.close();
        validator.close();

        assertNull(myPickListener);
        assertNull(myDiscreteListener);

        support.verifyAll();
    }

    /**
     * Tests resizing the window.
     */
    @Test
    public void testResizingWindowMin()
    {
        EasyMockSupport support = new EasyMockSupport();

        ControlRegistry controlRegistry = createControlRegistry(support);

        support.replayAll();

        XrayGogglesModel model = new XrayGogglesModel();
        model.setScreenPosition(new ScreenPosition(15, 5), new ScreenPosition(21, 5), new ScreenPosition(15, 10),
                new ScreenPosition(21, 10));
        PolylineGeometry geometry = createGeometry(model);
        model.setWindowGeometry(geometry);
        XrayWindowValidator validator = new XrayWindowValidator(model);
        WindowResizer resizer = new WindowResizer(controlRegistry, model);

        PickListener.PickEvent pickEvent = new PickListener.PickEvent(geometry, new Point(10, 5));
        myPickListener.handlePickEvent(pickEvent);
        assertTrue(myDiscreteListener.isTargeted());

        JPanel panel = new JPanel();
        MouseEvent event = new MouseEvent(panel, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 21, 5, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertTrue(event.isConsumed());

        event = new MouseEvent(panel, MouseEvent.MOUSE_DRAGGED, System.currentTimeMillis(), 0, 18, 4, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertTrue(event.isConsumed());
        assertEquals(15, model.getUpperLeft().getX(), 0d);
        assertEquals(5, model.getUpperLeft().getY(), 0d);
        assertEquals(21, model.getUpperRight().getX(), 0d);
        assertEquals(5, model.getUpperRight().getY(), 0d);
        assertEquals(15, model.getLowerLeft().getX(), 0d);
        assertEquals(10, model.getLowerLeft().getY(), 0d);
        assertEquals(21, model.getLowerRight().getX(), 0d);
        assertEquals(10, model.getLowerRight().getY(), 0d);

        event = new MouseEvent(panel, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 17, 4, 0, false);
        myDiscreteListener.eventOccurred(event);
        assertFalse(myDiscreteListener.isTargeted());

        resizer.close();
        validator.close();

        assertNull(myPickListener);
        assertNull(myDiscreteListener);

        support.verifyAll();
    }

    /**
     * Creates a mocked {@link ControlRegistry}.
     *
     * @param support Used to create the mock.
     * @return The {@link ControlRegistry}.
     */
    private ControlRegistry createControlRegistry(EasyMockSupport support)
    {
        ControlContext glui = support.createMock(ControlContext.class);
        glui.addPickListener(EasyMock.isA(PickListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myPickListener = (PickListener)EasyMock.getCurrentArguments()[0];
            return null;
        });
        glui.removePickListener(EasyMock.isA(PickListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myPickListener.equals(EasyMock.getCurrentArguments()[0]))
            {
                myPickListener = null;
            }
            return null;
        });

        ControlContext globe = support.createMock(ControlContext.class);
        globe.addListener(EasyMock.isA(DiscreteEventListener.class),
                EasyMock.eq(new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK)),
                EasyMock.eq(new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK)),
                EasyMock.eq(new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED, InputEvent.BUTTON1_DOWN_MASK)));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            myDiscreteListener = (DiscreteEventListener)EasyMock.getCurrentArguments()[0];
            return null;
        });
        globe.removeListener(EasyMock.isA(DiscreteEventListener.class));
        EasyMock.expectLastCall().andAnswer(() ->
        {
            if (myDiscreteListener.equals(EasyMock.getCurrentArguments()[0]))
            {
                myDiscreteListener = null;
            }
            return null;
        });

        ControlRegistry controlRegistry = support.createMock(ControlRegistry.class);
        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT)).andReturn(glui).atLeastOnce();
        EasyMock.expect(controlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)).andReturn(globe).atLeastOnce();

        return controlRegistry;
    }

    /**
     * Creates the test {@link PolylineGeometry}.
     *
     * @param model The model containing the screen positions.
     * @return A new {@link PolylineGeometry}.
     */
    private PolylineGeometry createGeometry(XrayGogglesModel model)
    {
        Builder<ScreenPosition> builder = new Builder<>();
        builder.setVertices(New.list(model.getUpperLeft(), model.getUpperRight(), model.getLowerLeft(), model.getLowerRight()));
        DefaultPolylineRenderProperties renderProperties = new DefaultPolylineRenderProperties(ZOrderRenderProperties.TOP_Z, true,
                true);
        return new PolylineGeometry(builder, renderProperties, null);
    }
}
