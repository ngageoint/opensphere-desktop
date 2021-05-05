package io.opensphere.overlay;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.BoundEventListener;
import io.opensphere.core.control.CompoundEventMouseAdapter;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultKeyPressedBinding;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.projection.ProjectionChangeSupport;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.overlay.arc.ArcTransformer;
import io.opensphere.overlay.util.MousePositionUtils;

/** Helper for handling event listeners. */
@SuppressWarnings("PMD.GodClass")
public class OverlayListenerHelper
{
    /** Transformer for the arc length feature. */
    private final ArcTransformer myArcLengthTransformer;

    /** Hold a reference to my listeners. */
    private final List<BoundEventListener> myControlEventListeners = new ArrayList<>();

    /** Executor to handle changes to the cursor position label. */
    private final ProcrastinatingExecutor myCursorChangeExecutor;

    /** The transformer for cursor position. */
    private final CursorPositionTransformer myCursorPositionTransformer;

    /** Executor to handle operations for the arc length. */
    private final ProcrastinatingExecutor myExecutor;

    /** The transformer for MGRS grids. */
    private final MGRSTransformer myMGRSTransformer;

    /** Listener for projection change events. */
    private ProjectionChangeSupport.ProjectionChangeListener myProjectionChangeListener;

    /** The transformer for time span display. */
    private final TimeDisplayTransformer myTimeDisplayTransformer;

    /** Listener for the time span changes. */
    private ActiveTimeSpanChangeListener myTimeSpanChangeListener;

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /** Listener for view change events. */
    private ViewChangeSupport.ViewChangeListener myViewChangeListener;

    /** The transformer for viewer position. */
    private final ViewerPositionTransformer myViewerPositionTransformer;

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     * @param arc The arc length transformer.
     * @param curs The cursor position transformer
     * @param view The cursor position transformer
     * @param mgrs The MGRS transformer.
     * @param time The time display transformer.
     */
    public OverlayListenerHelper(Toolbox toolbox, ArcTransformer arc, CursorPositionTransformer curs,
            ViewerPositionTransformer view, MGRSTransformer mgrs, TimeDisplayTransformer time)
    {
        myToolbox = toolbox;
        myArcLengthTransformer = arc;
        myCursorPositionTransformer = curs;
        myViewerPositionTransformer = view;
        myMGRSTransformer = mgrs;
        myTimeDisplayTransformer = time;
        ScheduledExecutorService schedExec = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("Overlay Plugin"),
                SuppressableRejectedExecutionHandler.getInstance());
        myViewChangeExecutor = new ProcrastinatingExecutor(schedExec);
        myCursorChangeExecutor = new ProcrastinatingExecutor(schedExec);
        myExecutor = new ProcrastinatingExecutor(schedExec);
    }

    /** Handle any required cleanup. */
    public void close()
    {
        ControlContext controlContext = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        controlContext.removeListeners(myControlEventListeners);

        if (myTimeDisplayTransformer != null)
        {
            myToolbox.getTimeManager().removeActiveTimeSpanChangeListener(myTimeSpanChangeListener);
        }

        if (myCursorPositionTransformer != null)
        {
            myToolbox.getMapManager().getProjectionChangeSupport().removeProjectionChangeListener(myProjectionChangeListener);
        }

        if (myCursorPositionTransformer != null || myMGRSTransformer != null || myViewerPositionTransformer != null)
        {
            myToolbox.getMapManager().getViewChangeSupport().removeViewChangeListener(myViewChangeListener);
        }

        myViewChangeExecutor.shutdownNow();
        myCursorChangeExecutor.shutdownNow();
        myExecutor.shutdownNow();
    }

    /** Set up the listeners. */
    public void initialize()
    {
        ControlContext controlContext = myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);

        DiscreteEventAdapter mouseMovedListener = new MouseMovedAdapter("Overlay Plugin", "Mouse Movement",
                "Mouse moved with no buttons");
        mouseMovedListener.setReassignable(false);
        myControlEventListeners.add(mouseMovedListener);
        controlContext.addListener(mouseMovedListener, new DefaultMouseBinding(MouseEvent.MOUSE_MOVED),
                new DefaultMouseBinding(MouseEvent.MOUSE_MOVED, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK),
                new DefaultMouseBinding(MouseEvent.MOUSE_EXITED),
                new DefaultMouseBinding(MouseEvent.MOUSE_MOVED, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));

        DiscreteEventAdapter modMousePressedListener = new ArcButtonAdapter("Drawing", "Arc Generation Click",
                "Used for drawing arcs on the globe.");
        modMousePressedListener.setReassignable(false);
        myControlEventListeners.add(modMousePressedListener);
        controlContext.addListener(modMousePressedListener, new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED));

        DiscreteEventAdapter resetCompleteListener = new ArcKeyAdapter("Drawing", "Arc Generation Keys",
                "Used for drawing arcs on the globe.");
        resetCompleteListener.setReassignable(false);
        myControlEventListeners.add(resetCompleteListener);
        controlContext.addListener(resetCompleteListener, new DefaultKeyPressedBinding(KeyEvent.VK_ESCAPE),
                new DefaultKeyPressedBinding(KeyEvent.VK_SPACE), new DefaultKeyPressedBinding(KeyEvent.VK_ENTER));

        if (myCursorPositionTransformer != null)
        {
            CompoundEventMouseAdapter mousePressedListener = new CursorPositionUnmodifiedAdapter("View", "Overlay Populator",
                    "Populates overlay elements");
            mousePressedListener.setReassignable(false);
            mousePressedListener.setReassignable(false);
            myControlEventListeners.add(mousePressedListener);
            controlContext.addListener(mousePressedListener,
                    new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK));

            CompoundEventMouseAdapter mouseDraggedListener = new CursorPositionShiftAdapter("View", "Overlay Populator",
                    "Populates overlay elements");
            mouseDraggedListener.setReassignable(false);
            myControlEventListeners.add(mouseDraggedListener);
            controlContext.addListener(mouseDraggedListener,
                    new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

            myProjectionChangeListener = new ProjectionChangeSupport.ProjectionChangeListener()
            {
                @Override
                public void projectionChanged(ProjectionChangedEvent evt)
                {
                    myCursorChangeExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            myCursorPositionTransformer.resetPositionLabel();
                        }
                    });
                }
            };
            myToolbox.getMapManager().getProjectionChangeSupport().addProjectionChangeListener(myProjectionChangeListener);
        }

        if (myCursorPositionTransformer != null || myMGRSTransformer != null || myViewerPositionTransformer != null)
        {
            myViewChangeListener = new ViewChangeSupport.ViewChangeListener()
            {
                @Override
                public void viewChanged(final Viewer view, final ViewChangeSupport.ViewChangeType type)
                {
                    if (myCursorPositionTransformer != null)
                    {
                        myCursorChangeExecutor.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                myCursorPositionTransformer.resetPositionLabel();
                            }
                        });
                    }

                    myViewChangeExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (myViewerPositionTransformer != null)
                            {
                                myViewerPositionTransformer.handleViewChanged(view, type);
                            }

                            if (myMGRSTransformer != null && myMGRSTransformer.isEnabled()
                                    && myToolbox.getMapManager().getProjection() != null)
                            {
                                myMGRSTransformer.updateGrid(view, myToolbox.getMapManager().getProjection());
                            }
                        }
                    });
                }
            };
            myToolbox.getMapManager().getViewChangeSupport().addViewChangeListener(myViewChangeListener);
        }

        if (myTimeDisplayTransformer != null)
        {
            myTimeSpanChangeListener = active -> myTimeDisplayTransformer.setTimeDisplayLabel();
            myToolbox.getTimeManager().addActiveTimeSpanChangeListener(myTimeSpanChangeListener);
        }
    }

    /** Adapter for when mouse buttons are pressed for arc drawing. */
    private class ArcButtonAdapter extends ArcTargetedAdapter
    {
        /**
         * Construct the listener.
         *
         * @param cat The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public ArcButtonAdapter(String cat, String title, String description)
        {
            super(cat, title, description);
        }

        @Override
        public void eventOccurred(InputEvent event)
        {
            final MouseEvent mouseEvent = (MouseEvent)event;
            if (mouseEvent.getButton() == MouseEvent.BUTTON1)
            {
                event.consume();
                myExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (mouseEvent.getClickCount() == 1)
                        {
                            myArcLengthTransformer.getArcGenerator().handleNewSegment(mouseEvent);
                        }
                        else
                        {
                            myArcLengthTransformer.getArcGenerator().handleCompletion();
                        }
                    }
                });
            }

            if (mouseEvent.getButton() == MouseEvent.BUTTON3 && myArcLengthTransformer.getArcGenerator().arcInProgress())
            {
                event.consume();
                myExecutor.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myArcLengthTransformer.getArcGenerator().handleCancellation();
                    }
                });
            }
        }
    }

    /** Key handling for arc drawing functions. */
    private class ArcKeyAdapter extends ArcTargetedAdapter
    {
        /**
         * Construct the listener.
         *
         * @param category The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public ArcKeyAdapter(String category, String title, String description)
        {
            super(category, title, description);
        }

        @Override
        public void eventOccurred(InputEvent event)
        {
            final KeyEvent keyEvent = (KeyEvent)event;
            myExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    {
                        myArcLengthTransformer.getArcGenerator().handleCancellation();
                    }
                    else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE || keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        myArcLengthTransformer.getArcGenerator().handleCompletion();
                    }
                }
            });
        }
    }

    /**
     * Abstract class which handles targeting and priority for Arc event
     * listeners.
     */
    private abstract class ArcTargetedAdapter extends DiscreteEventAdapter
    {
        /**
         * Construct the listener.
         *
         * @param category The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public ArcTargetedAdapter(String category, String title, String description)
        {
            super(category, title, description);
        }

        @Override
        public int getTargetPriority()
        {
            return 2000;
        }

        @Override
        public boolean isTargeted()
        {
            return myArcLengthTransformer.getArcGenerator().isActive();
        }

        @Override
        public boolean mustBeTargeted()
        {
            return true;
        }
    }

    /**
     * When the shift and button one are pressed, the cursor position should
     * continue to be updated.
     */
    private class CursorPositionShiftAdapter extends CompoundEventMouseAdapter
    {
        /**
         * Construct the listener.
         *
         * @param category The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public CursorPositionShiftAdapter(String category, String title, String description)
        {
            super(category, title, description);
        }

        @Override
        public void mouseDragged(final MouseEvent mouseEvent)
        {
            myCursorChangeExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    myCursorPositionTransformer.setPositionLabel(mouseEvent.getPoint());
                }
            });
        }
    }

    /**
     * Adapter for events for updating the cursor position when button one is
     * pressed.
     */
    private class CursorPositionUnmodifiedAdapter extends CompoundEventMouseAdapter
    {
        /**
         * Construct the listener.
         *
         * @param category The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public CursorPositionUnmodifiedAdapter(String category, String title, String description)
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
        public void mouseReleased(final MouseEvent mouseEvent)
        {
            myCursorChangeExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    myCursorPositionTransformer.setPositionLabel(mouseEvent.getPoint());
                }
            });
        }
    }

    /** Adapter for mouse movement with no buttons pressed. */
    private class MouseMovedAdapter extends DiscreteEventAdapter
    {
        /**
         * Construct the listener.
         *
         * @param category The category to present to the user.
         * @param title The title to present to the user.
         * @param description The description to present to the user.
         */
        public MouseMovedAdapter(String category, String title, String description)
        {
            super(category, title, description);
        }

        @Override
        public void eventOccurred(InputEvent event)
        {
            if (event instanceof MouseEvent)
            {
                final MouseEvent mouseEvent = (MouseEvent)event;
                if (mouseEvent.getID() == MouseEvent.MOUSE_EXITED && myCursorPositionTransformer != null)
                {
                    myCursorChangeExecutor.execute(() -> myCursorPositionTransformer.clear());
                }
                else if (mouseEvent.getID() == MouseEvent.MOUSE_MOVED)
                {
                    myExecutor.execute(() -> myArcLengthTransformer.getArcGenerator().handleMouseMoved(mouseEvent));

                    if (myCursorPositionTransformer != null)
                    {
                        myCursorChangeExecutor.execute(() ->
                            myCursorPositionTransformer.setPositionLabel(mouseEvent.getPoint()));
                    }

                    myCursorChangeExecutor.execute(() -> MousePositionUtils.setMousePosition(mouseEvent.getPoint(), myToolbox));
                }
            }
        }
    }
}
