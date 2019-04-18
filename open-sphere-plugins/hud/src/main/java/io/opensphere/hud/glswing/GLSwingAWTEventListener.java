package io.opensphere.hud.glswing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import io.opensphere.core.control.awt.AWTMouseTargetFinder;
import io.opensphere.core.control.ui.InternalComponentRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;

/**
 * AWT event listener which enables interaction with JInterframes that are
 * underneath the GLCanvas.
 */
public class GLSwingAWTEventListener implements AWTEventListener
{
    /** The registry for HUD windows. */
    private InternalComponentRegistry myComponentRegistry;

    /** Events which were dispatched by me. These events should be ignored. */
    private final List<AWTEvent> myDispatchedEvents = New.list();

    /**
     * The mouse target which had the mouse over it at the time of the last
     * mouse press and the frame which is the parent of the mouse target, it
     * should be reset to null upon mouse release.
     */
    private Pair<GLSwingInternalFrame, Component> myHeldByMouse;

    /** The manager which owns me. */
    private final GLSwingEventManager myManager;

    /**
     * This is the component which is currently under the mouse. It is used to
     * determine when to generate enter and exit events.
     */
    private Component myOverComponent;

    /**
     * Constructor.
     *
     * @param manager The manager which owns me.
     */
    public GLSwingAWTEventListener(GLSwingEventManager manager)
    {
        myManager = manager;
    }

    /** Clear my held and over components. */
    public synchronized void clearHeld()
    {
        myHeldByMouse = null;
        myOverComponent = null;
    }

    @Override
    public synchronized void eventDispatched(AWTEvent event)
    {
        Set<GLSwingInternalFrame> internalFrames = null;
        if (preProcessEvent(event) && !(internalFrames = myManager.getFrames()).isEmpty())
        {
            MouseEvent origEvent = (MouseEvent)event;

            checkForMissingMouseReleasedEvent(event);
            if (!checkOwnSource(origEvent, internalFrames))
            {
                return;
            }

            Component mouseTarget = null;
            MouseEvent targetAdjustedEvent = null;
            MouseEvent frameAdjustedEvent = null;
            GLSwingInternalFrame glFrame = null;
            JInternalFrame frame = null;
            if (myHeldByMouse != null)
            {
                glFrame = myHeldByMouse.getFirstObject();
                frame = glFrame.getHUDFrame().getInternalFrame();
                frameAdjustedEvent = SwingUtilities.convertMouseEvent((Component)event.getSource(), origEvent, frame);
                mouseTarget = AWTMouseTargetFinder.findMouseTarget(frame, frameAdjustedEvent.getX(), frameAdjustedEvent.getY());
                targetAdjustedEvent = SwingUtilities.convertMouseEvent(frame, frameAdjustedEvent, mouseTarget);
            }
            else
            {
                for (GLSwingInternalFrame ref : internalFrames)
                {
                    glFrame = ref;
                    frame = glFrame.getHUDFrame().getInternalFrame();
                    if (!frame.isVisible())
                    {
                        glFrame = null;
                        frame = null;
                        continue;
                    }
                    frameAdjustedEvent = SwingUtilities.convertMouseEvent((Component)event.getSource(), origEvent, frame);
                    mouseTarget = AWTMouseTargetFinder.findMouseTarget(frame, frameAdjustedEvent.getX(),
                            frameAdjustedEvent.getY());
                    targetAdjustedEvent = SwingUtilities.convertMouseEvent(frame, frameAdjustedEvent, mouseTarget);

                    if (mouseTarget != null)
                    {
                        break;
                    }
                }
            }

            handleTargetedEvent(glFrame, origEvent, mouseTarget, targetAdjustedEvent, frameAdjustedEvent);

            // TODO we probably don't need to do this for every event maybe
            // check to see if the cursor has changed?
            Component source = (Component)origEvent.getSource();
            if (myHeldByMouse != null)
            {
                if (!Utilities.sameInstance(source.getCursor(), myHeldByMouse.getSecondObject().getCursor()))
                {
                    source.setCursor(myHeldByMouse.getSecondObject().getCursor());
                }
            }
            else if (mouseTarget != null)
            {
                if (!Utilities.sameInstance(source.getCursor(), mouseTarget.getCursor()))
                {
                    source.setCursor(mouseTarget.getCursor());
                }
            }
            else
            {
                if (source.getCursor() != null)
                {
                    source.setCursor(null);
                }
            }
        }
    }

    /**
     * Set the componentRegistry.
     *
     * @param componentRegistry the componentRegistry to set
     */
    public void setComponentRegistry(InternalComponentRegistry componentRegistry)
    {
        myComponentRegistry = componentRegistry;
    }

    /**
     * Some operations, such as drag and drop, allow the mouse to be released
     * without the corresponding event being dispatched, when this happens we
     * need to release any components which we think are held.
     *
     * @param event The most recent mouse event.
     */
    private void checkForMissingMouseReleasedEvent(AWTEvent event)
    {
        // If we are receiving a MOUSE_MOVED event, then we should not have a
        // held component. Otherwise it would be a MOUSE_DRAGGED event.
        if (event.getID() == MouseEvent.MOUSE_MOVED)
        {
            myHeldByMouse = null;
        }
    }

    /**
     * Check to see if the event is being dispatched by the canvas or by one of
     * the internal frames.
     *
     * @param event The event which was dispatched.
     * @param internalFrames The frames which may own the event.
     * @return true when the event was dispatched by the canvas or by one of the
     *         internal frames.
     */
    private boolean checkOwnSource(MouseEvent event, Set<GLSwingInternalFrame> internalFrames)
    {
        boolean ownSource = true;
        // When the mouse event is not coming from the canvas, and it is not
        // coming from a component that is owned by an internal frame, do
        // not process it.
        String sourceClass = event.getSource().getClass().getCanonicalName();
        if (!"javax.media.opengl.awt.GLCanvas".equals(sourceClass) && !"com.jogamp.newt.awt.NewtCanvasAWT".equals(sourceClass)
                && !"io.opensphere.core.pipeline.PipelineGLCanvas".equals(sourceClass))
        {
            ownSource = false;
            if (event.getID() != MouseEvent.MOUSE_WHEEL && event.getSource() instanceof Component)
            {
                Component source = (Component)event.getSource();
                JInternalFrame frameForSource;
                if (source instanceof JInternalFrame)
                {
                    frameForSource = (JInternalFrame)source;
                }
                else
                {
                    frameForSource = (JInternalFrame)SwingUtilities.getAncestorOfClass(JInternalFrame.class, source);
                }
                for (GLSwingInternalFrame frame : internalFrames)
                {
                    if (Utilities.sameInstance(frame.getHUDFrame().getInternalFrame(), frameForSource))
                    {
                        ownSource = true;
                        break;
                    }
                }

                if (!ownSource)
                {
                    handleExitEnter(null, event);
                }
            }
        }
        return ownSource;
    }

    /**
     * Dispatch an event for when a component has been entered or exited.
     *
     * @param event The original mouse event.
     * @param enteredOrExited either {@link MouseEvent#MOUSE_ENTERED} or
     *            {@link MouseEvent#MOUSE_EXITED}.
     * @param recipient The component to which the event will be dispatched.
     */
    private void dispatchEnteredExitedEvent(MouseEvent event, int enteredOrExited, Component recipient)
    {
        Point p = SwingUtilities.convertPoint((Component)event.getSource(), event.getPoint(), recipient);

        MouseEvent derived = new MouseEvent(recipient, enteredOrExited, System.currentTimeMillis(), 0, p.x, p.y, 0, false);
        myDispatchedEvents.add(derived);
        recipient.dispatchEvent(derived);
        if (enteredOrExited == MouseEvent.MOUSE_EXITED)
        {
            myOverComponent = null;
        }
    }

    /**
     * Generate enter or exit events based on the current mouse event and the
     * previous component the mouse was over.
     *
     * @param comp Component the mouse was over at the time of the event.
     * @param event Event adjusted to the comp's coordinate system.
     */
    private void handleExitEnter(Component comp, MouseEvent event)
    {
        if (comp != null)
        {
            if (!Utilities.sameInstance(comp, myOverComponent))
            {
                // I am over a new component and am not dragging. If there
                // is a previous component, generated a exit event for it.
                if (myOverComponent != null)
                {
                    dispatchEnteredExitedEvent(event, MouseEvent.MOUSE_EXITED, myOverComponent);
                }

                myOverComponent = comp;

                // Generate an enter event for the newly entered component.
                dispatchEnteredExitedEvent(event, MouseEvent.MOUSE_ENTERED, myOverComponent);
            }
        }
        else if (myOverComponent != null && myHeldByMouse == null)
        {
            // Since comp is null, we have missed cleaning up unless we are
            // still holding a component.

            // I am no longer over any component and am not dragging, so
            // generate an exit event.
            dispatchEnteredExitedEvent(event, MouseEvent.MOUSE_EXITED, myOverComponent);
            myOverComponent = null;
        }
    }

    /**
     * Set or clear the held frame and component. When releasing. dispatch the
     * event to the held component before clearing the held component.
     *
     * @param frame Frame which the mouse was over at the time of the event.
     * @param comp Component which the mouse was over at the time of the event.
     * @param event The mouse event.
     * @param componentAdjustedEvent The event in coordinates adjusted for the
     *            held component.
     * @return true when the event is dispatched to the held component.
     */
    private boolean handleHoldRelease(GLSwingInternalFrame frame, Component comp, MouseEvent event,
            MouseEvent componentAdjustedEvent)
    {
        boolean dispatched = false;
        if (comp != null && event.getID() == MouseEvent.MOUSE_PRESSED)
        {
            myHeldByMouse = new Pair<>(frame, comp);
        }
        else if (event.getID() == MouseEvent.MOUSE_RELEASED)
        {
            if (myHeldByMouse != null)
            {
                myDispatchedEvents.add(componentAdjustedEvent);
                myHeldByMouse.getSecondObject().dispatchEvent(componentAdjustedEvent);
                dispatched = true;
            }
            myHeldByMouse = null;
        }
        return dispatched;
    }

    /**
     * Once we have determined which component if any is the target of the mouse
     * event and converted the event as necessary to the local coordinates for
     * the component and frame, handle any necessary action associated with the
     * event.
     *
     * @param glFrame The frame which contains the mouse target.
     * @param origEvent The original event which was dispatched.
     * @param mouseTarget The component which is the target of the event.
     * @param targetAdjustedEvent The event adjusted to the target's
     *            coordinates.
     * @param frameAdjustedEvent The event adjusted to the frame's coordinates.
     */
    private void handleTargetedEvent(GLSwingInternalFrame glFrame, MouseEvent origEvent, Component mouseTarget,
            MouseEvent targetAdjustedEvent, MouseEvent frameAdjustedEvent)
    {
        JInternalFrame frame = null;
        if (glFrame != null)
        {
            frame = glFrame.getHUDFrame().getInternalFrame();
        }
        Component heldComponent = null;
        if (myHeldByMouse != null)
        {
            heldComponent = myHeldByMouse.getSecondObject();
        }

        if (origEvent.getID() == MouseEvent.MOUSE_DRAGGED && heldComponent != null)
        {
            if (frameAdjustedEvent != null)
            {
                MouseEvent heldEvent = SwingUtilities.convertMouseEvent(frame, frameAdjustedEvent, heldComponent);
                myDispatchedEvents.add(heldEvent);
                heldComponent.dispatchEvent(heldEvent);
            }
            origEvent.consume();
        }
        else
        {
            if (targetAdjustedEvent != null)
            {
                handleExitEnter(mouseTarget, targetAdjustedEvent);
            }
            MouseEvent heldAdjustedEvent = null;
            if (heldComponent != null)
            {
                heldAdjustedEvent = SwingUtilities.convertMouseEvent(frame, frameAdjustedEvent, heldComponent);
            }
            if (!handleHoldRelease(glFrame, mouseTarget, origEvent, heldAdjustedEvent))
            {
                if (heldComponent != null && frameAdjustedEvent != null)
                {
                    MouseEvent heldEvent = SwingUtilities.convertMouseEvent(frame, frameAdjustedEvent, heldComponent);
                    myDispatchedEvents.add(heldEvent);
                    heldComponent.dispatchEvent(heldEvent);
                    origEvent.consume();
                }
                else if (frameAdjustedEvent == null)
                {
                    heldComponent = null;
                }
                else if (mouseTarget != null)
                {
                    myDispatchedEvents.add(targetAdjustedEvent);
                    mouseTarget.dispatchEvent(targetAdjustedEvent);
                    origEvent.consume();
                }
            }
        }
    }

    /**
     * Perform any pre-processing and determine whether this is an event which
     * should be fully processed.
     *
     * @param event The event to pre-process.
     * @return true when this is an event which should be fully processed.
     */
    private boolean preProcessEvent(AWTEvent event)
    {
        if (myDispatchedEvents.remove(event))
        {
            return false;
        }

        if (myOverComponent != null)
        {
            JInternalFrame frameForComp = (JInternalFrame)SwingUtilities.getAncestorOfClass(JInternalFrame.class,
                    myOverComponent);
            if (frameForComp != null && !frameForComp.isVisible())
            {
                myOverComponent = null;
            }
        }

        if (myComponentRegistry != null)
        {
            myComponentRegistry.setMouseOverHUD(myOverComponent != null);
        }

        return event instanceof MouseEvent && !(event.getID() == MouseEvent.MOUSE_DRAGGED && myHeldByMouse == null);
    }
}
