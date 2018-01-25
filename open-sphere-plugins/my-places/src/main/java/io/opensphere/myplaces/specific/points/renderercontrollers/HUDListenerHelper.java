package io.opensphere.myplaces.specific.points.renderercontrollers;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.CompoundEventMouseAdapter;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.mappoint.impl.MapPointTransformer;

/** Helper for handling event listeners. */
public class HUDListenerHelper
{
    /** Hold a reference to my listeners. */
    private final List<BoundEventListener> myControlEventListeners = new ArrayList<>();

    /** Transformer for the arc length feature. */
    private final MapPointTransformer myMapPointTransformer;

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     * @param arc The arc length transformer.
     */
    public HUDListenerHelper(Toolbox toolbox, MapPointTransformer arc)
    {
        myToolbox = toolbox;
        myMapPointTransformer = arc;
        ScheduledExecutorService schedExec = ProcrastinatingExecutor.protect(new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("HUD Plugin"), SuppressableRejectedExecutionHandler.getInstance()));
        myViewChangeExecutor = new ProcrastinatingExecutor(schedExec);
    }

    /** Handle any required cleanup. */
    public void close()
    {
        ControlContext controlContext = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        controlContext.removeListeners(myControlEventListeners);

        myViewChangeExecutor.shutdownNow();
    }

    /** Set up the listeners. */
    public void initialize()
    {
        ControlContext controlContext = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);

        CompoundEventMouseAdapter mousePressedListener = new ButtonOneAdapter("HUD Plugin", "Mouse Pressed", "Mouse Pressed");
        mousePressedListener.setReassignable(false);
        myControlEventListeners.add(mousePressedListener);
        controlContext.addListener(mousePressedListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK));

        DiscreteEventAdapter mouseDragListener = new ButtonOneDragListener("HUD Plugin", "Mouse Drag",
                "Mouse dragged with button one");
        mouseDragListener.setReassignable(false);
        myControlEventListeners.add(mouseDragListener);
        controlContext.addListener(mouseDragListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK));
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
        public void mouseDragged(MouseEvent mouseEvent)
        {
            myMapPointTransformer.handleMouseDragged(mouseEvent);
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent)
        {
            myMapPointTransformer.handleMousePressed(mouseEvent);
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent)
        {
            myMapPointTransformer.handleMouseReleased(mouseEvent);
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
                    myMapPointTransformer.handleMouseDragged(mouseEvent);
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
            return myMapPointTransformer.isTargeted();
        }

        @Override
        public boolean mustBeTargeted()
        {
            return true;
        }
    }
}
