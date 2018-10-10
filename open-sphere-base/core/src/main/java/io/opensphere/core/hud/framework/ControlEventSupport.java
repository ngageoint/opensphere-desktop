package io.opensphere.core.hud.framework;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DefaultMouseWheelBinding;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.Utilities;

/**
 * Abstract base class for HUD components which have a single drag-able
 * geometry.
 */
public class ControlEventSupport
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ControlEventSupport.class);

    /** Geometry which can be picked. */
    private final List<Geometry> myActionGeometries = new ArrayList<>();

    /**
     * If I am currently being dragged, do not remove my event listeners until
     * the drag is complete.
     */
    private boolean myAwaitingCleanup;

    /** Set when dragging is occurring. */
    private Geometry myHeldGeometry;

    /**
     * Set when the mouse is pressed, but not cleared until after the click
     * event is received.
     */
    private Geometry myMouseDownGeometry;

    /** Last known position for the mouse while dragging. */
    private Point myLastPosition;

    /** Hold a reference to my listeners. */
    private final ControlContextListener myMouseListener = new ControlContextListener();

    /** Control Registry. */
    private final ControlRegistry myControlRegistry;

    /** True if my action geometry is currently picked. */
    private Geometry myPickedGeometry;

    /** Listener for pick changes. */
    private final PickListener myPickListener = evt ->
    {
        // If I have a picked geometry, then receiving a pick event means
        // that either no geometry is picked, or a different geometry is
        // picked.
        if (myPickedGeometry != null)
        {
            mySelectionListener.mouseExited(myPickedGeometry, evt.getLocation());
        }

        if (myActionGeometries.contains(evt.getPickedGeometry()))
        {
            myPickedGeometry = evt.getPickedGeometry();
            mySelectionListener.mouseEntered(myPickedGeometry, evt.getLocation());
        }
        else
        {
            myPickedGeometry = null;
        }
    };

    /** The listener to whom I send events. */
    private final ControlEventListener mySelectionListener;

    /**
     * Construct me.
     *
     * @param listener component which owns me.
     * @param controlRegistry The control registry.
     */
    public ControlEventSupport(ControlEventListener listener, ControlRegistry controlRegistry)
    {
        mySelectionListener = listener;
        myControlRegistry = controlRegistry;
        register();
    }

    /** Clean up my listeners if I am done with them. */
    public void cleanupListeners()
    {
        if (myHeldGeometry != null)
        {
            myAwaitingCleanup = true;
        }
        else
        {
            if (myControlRegistry != null)
            {
                ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
                gluiCtx.removePickListener(myPickListener);

                ControlContext globeCtx = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
                globeCtx.removeListener(myMouseListener);
            }
            else
            {
                LOGGER.error("ControlContext is NULL: unable to remove listeners.");
            }
        }
    }

    /**
     * Get the picked geometry.
     *
     * @return the picked geometry.
     */
    public Geometry getPickedGeometry()
    {
        return myPickedGeometry;
    }

    /** Initialize my listeners. */
    public final void register()
    {
        ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
        gluiCtx.addPickListener(myPickListener);

        ControlContext globeCtx = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);

        // create bindings for the events we are interested in.
        DefaultMouseBinding press1 = new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK);
        DefaultMouseBinding press2 = new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON2_DOWN_MASK);
        DefaultMouseBinding press3 = new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON3_DOWN_MASK);
        DefaultMouseBinding drag1 = new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK);
        DefaultMouseBinding drag2 = new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON2_DOWN_MASK);
        DefaultMouseBinding drag3 = new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON3_DOWN_MASK);
        DefaultMouseBinding release1 = new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED);
        DefaultMouseBinding clicked1 = new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED);
        DefaultMouseBinding moved = new DefaultMouseBinding(MouseEvent.MOUSE_MOVED);
        DefaultMouseWheelBinding wheelUp = new DefaultMouseWheelBinding(DefaultMouseWheelBinding.WheelDirection.UP);
        DefaultMouseWheelBinding wheelDown = new DefaultMouseWheelBinding(DefaultMouseWheelBinding.WheelDirection.DOWN);

        globeCtx.addListener(myMouseListener, moved, press1, press2, press3, release1, drag1, drag2, drag3, clicked1, wheelUp,
                wheelDown);
    }

    /**
     * Set the actionGeometry.
     *
     * @param actionGeometry the actionGeometry to set
     */
    public void setActionGeometry(Geometry actionGeometry)
    {
        myActionGeometries.add(actionGeometry);
    }

    /** Listener for events from the control context. */
    private class ControlContextListener implements DiscreteEventListener
    {
        @Override
        public void eventOccurred(InputEvent event)
        {
            if (myPickedGeometry != null)
            {
                event.consume();
            }
            if (event instanceof MouseEvent)
            {
                MouseEvent mouseEvent = (MouseEvent)event;
                if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)
                {
                    if (myPickedGeometry != null && Utilities.sameInstance(myPickedGeometry, myMouseDownGeometry))
                    {
                        mySelectionListener.mouseClicked(myPickedGeometry, mouseEvent);
                    }
                    myMouseDownGeometry = null;
                }
                else if (mouseEvent.getID() == MouseEvent.MOUSE_DRAGGED)
                {
                    if (myHeldGeometry != null)
                    {
                        mySelectionListener.mouseDragged(myHeldGeometry, myLastPosition, mouseEvent);
                        myLastPosition = mouseEvent.getPoint();
                    }
                }
                else if (mouseEvent.getID() == MouseEvent.MOUSE_MOVED)
                {
                    if (myPickedGeometry != null)
                    {
                        mySelectionListener.mouseMoved(myPickedGeometry, mouseEvent);
                    }
                }
                else if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED)
                {
                    if (myPickedGeometry != null)
                    {
                        myLastPosition = mouseEvent.getPoint();
                        myHeldGeometry = myPickedGeometry;
                        myMouseDownGeometry = myPickedGeometry;
                        mySelectionListener.mousePressed(myHeldGeometry, mouseEvent);
                    }
                }
                else if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED)
                {
                    if (myHeldGeometry != null)
                    {
                        mySelectionListener.mouseReleased(myHeldGeometry, mouseEvent);
                    }
                    myLastPosition = null;
                    myHeldGeometry = null;
                    if (myAwaitingCleanup)
                    {
                        myAwaitingCleanup = false;
                        cleanupListeners();
                    }
                }
                else if (mouseEvent.getID() == MouseEvent.MOUSE_WHEEL && myPickedGeometry != null
                        && mouseEvent instanceof MouseWheelEvent)
                {
                    MouseWheelEvent wheel = (MouseWheelEvent)mouseEvent;
                    mySelectionListener.mouseWheelMoved(myPickedGeometry, wheel);
                }
            }
        }

        @Override
        public String getCategory()
        {
            return "HUD";
        }

        @Override
        public String getDescription()
        {
            return "HUD Mouse";
        }

        @Override
        public int getTargetPriority()
        {
            return 10000;
        }

        @Override
        public String getTitle()
        {
            return "HUD Mouse";
        }

        @Override
        public boolean isReassignable()
        {
            return false;
        }

        @Override
        public boolean isTargeted()
        {
            return myPickedGeometry != null && myActionGeometries.contains(myPickedGeometry);
        }

        @Override
        public boolean mustBeTargeted()
        {
            return true;
        }
    }
}
