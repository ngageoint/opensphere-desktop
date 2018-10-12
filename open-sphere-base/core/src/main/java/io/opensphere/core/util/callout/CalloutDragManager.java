package io.opensphere.core.util.callout;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.CompoundEventMouseAdapter;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DefaultPickListener;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.geometry.GeoScreenBubbleGeometry;
import io.opensphere.core.geometry.GeoScreenBubbleGeometry.Builder;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.model.GeoScreenBoundingBox;
import io.opensphere.core.model.GeographicBoxAnchor;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;

/** A manager for dragging callouts. */
public class CalloutDragManager
{
    /** Executor to handle operations for the arc length. */
    private final ProcrastinatingExecutor myExecutor;

    /** Hold a reference to my listeners. */
    private final List<BoundEventListener> myControlEventListeners = new ArrayList<>();

    /** The system control registry. */
    private final ControlRegistry myControlRegistry;

    /** The handler which owns the dragged callout. */
    private final CalloutDragHandler myDragHandler;

    /** The tile which is currently being dragged. */
    private TileGeometry myDragTile;

    /** The bounding box at the time the mouse drag began. */
    private GeoScreenBoundingBox myMouseDownBox;

    /** Listener for pick changes. */
    private final DefaultPickListener myPickListener = new DefaultPickListener();

    /** The location at which the mouse drag began. */
    private Point myTileDragMouseDown;

    /**
     * Constructor.
     *
     * @param controlRegistry The system control registry used to registry for
     *            drag events.
     * @param dragHandler The handler which owns the dragged callout.
     * @param bindingCategory The category for the mouse bindings.
     * @param executor The executor used for drag actions.
     */
    public CalloutDragManager(ControlRegistry controlRegistry, CalloutDragHandler dragHandler, String bindingCategory,
            ScheduledThreadPoolExecutor executor)
    {
        myControlRegistry = Utilities.checkNull(controlRegistry, "controlRegistry");
        myDragHandler = dragHandler;

        myExecutor = new ProcrastinatingExecutor(executor);
        ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
        gluiCtx.addPickListener(myPickListener);

        ControlContext controlContext = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);

        CompoundEventMouseAdapter mousePressedListener = new ButtonOneAdapter(bindingCategory, "Mouse Pressed",
                "Handles presses for button one");
        mousePressedListener.setReassignable(false);
        myControlEventListeners.add(mousePressedListener);
        controlContext.addListener(mousePressedListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK));

        DiscreteEventAdapter mouseDragListener = new ButtonOneDragListener(bindingCategory, "Mouse Drag",
                "Mouse dragged with button one");
        myControlEventListeners.add(mouseDragListener);
        controlContext.addListener(mouseDragListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK));
    }

    /** Perform any required cleanup with closing this manager. */
    public void close()
    {
        ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
        gluiCtx.removePickListener(myPickListener);

        myControlEventListeners.clear();
    }

    /**
     * Handle a mouse dragged event. If this occurs over a label, the label
     * should be repositioned.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleMouseDragged(MouseEvent mouseEvent)
    {
        if (myDragTile != null)
        {
            TileGeometry oldTile = myDragTile;
            GeographicBoxAnchor anchor = myDragHandler.getAnchor(mouseEvent, myMouseDownBox, myTileDragMouseDown);
            GeoScreenBoundingBox gsbb = new GeoScreenBoundingBox(myMouseDownBox.getUpperLeft(), myMouseDownBox.getLowerRight(),
                    anchor);
            TileGeometry newTile = CalloutGeometryUtil.createRepositionedTile(myDragTile, gsbb);
            GeoScreenBubbleGeometry oldBubble = myDragHandler.getAssociatedBubble(oldTile);
            if (oldBubble != null)
            {
                Builder builder = oldBubble.createBuilder();
                builder.setBoundingBox(gsbb);
                GeoScreenBubbleGeometry newBubble = new GeoScreenBubbleGeometry(builder, oldBubble.getRenderProperties(),
                        oldBubble.getConstraints());
                myDragTile = newTile;
                myDragHandler.replaceCallout(oldTile, newTile, newBubble);
            }
        }
    }

    /**
     * Handle a mouse pressed event for button 1. If the mouse is over a label,
     * store the pertinent information.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleMousePressed(MouseEvent mouseEvent)
    {
        PickEvent lastPickEvent = myPickListener.getLatestEvent();
        if (lastPickEvent != null && lastPickEvent.getPickedGeometry() instanceof TileGeometry
                && myDragHandler.handles((TileGeometry)lastPickEvent.getPickedGeometry()))
        {
            TileGeometry geom = (TileGeometry)lastPickEvent.getPickedGeometry();
            myDragTile = geom;
            myTileDragMouseDown = mouseEvent.getPoint();
            myMouseDownBox = (GeoScreenBoundingBox)myDragTile.getBounds();
        }
    }

    /**
     * Handle a mouse released event.
     *
     * @param mouseEvent Mouse event.
     */
    public void handleMouseReleased(MouseEvent mouseEvent)
    {
        myDragTile = null;
        myTileDragMouseDown = null;
        myMouseDownBox = null;
    }

    /** Adapter for events when button one is pressed. */
    private class ButtonOneAdapter extends CompoundEventMouseAdapter
    {
        /**
         * Construct the listener.
         *
         * @param category The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public ButtonOneAdapter(String category, String title, String description)
        {
            super(category, title, description);
        }

        @Override
        public void eventEnded(InputEvent event)
        {
            if (event instanceof MouseEvent)
            {
                mouseReleased((MouseEvent)event);
            }
        }

        @Override
        public void eventStarted(InputEvent event)
        {
            if (event instanceof MouseEvent)
            {
                mousePressed((MouseEvent)event);
            }
        }

        @Override
        public void mousePressed(final MouseEvent mouseEvent)
        {
            myExecutor.execute(() -> handleMousePressed(mouseEvent));
        }

        @Override
        public void mouseReleased(final MouseEvent mouseEvent)
        {
            myExecutor.execute(() -> handleMouseReleased(mouseEvent));
        }
    }

    /**
     * The event for dragging must be targeted to the tiles that owned by the
     * arc length transformer, so that it can consume the event.
     */
    private class ButtonOneDragListener extends DiscreteEventAdapter
    {
        /**
         * Construct the listener.
         *
         * @param category The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public ButtonOneDragListener(String category, String title, String description)
        {
            super(category, title, description);
        }

        @Override
        public void eventOccurred(InputEvent event)
        {
            if (event instanceof MouseEvent)
            {
                final MouseEvent mouseEvent = (MouseEvent)event;
                if (mouseEvent.getID() == MouseEvent.MOUSE_DRAGGED)
                {
                    mouseEvent.consume();
                    myExecutor.execute(() -> handleMouseDragged(mouseEvent));
                }
            }
        }

        @Override
        public int getTargetPriority()
        {
            return 9000;
        }

        @Override
        public boolean isReassignable()
        {
            return false;
        }

        @Override
        public boolean isTargeted()
        {
            return myDragTile != null;
        }

        @Override
        public boolean mustBeTargeted()
        {
            return true;
        }
    }
}
