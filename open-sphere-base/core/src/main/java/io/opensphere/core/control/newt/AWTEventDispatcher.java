package io.opensphere.core.control.newt;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import io.opensphere.core.control.awt.AWTMouseTargetFinder;
import io.opensphere.core.util.Utilities;

/** Dispatch events which have been generated from NEWT Events. */
@SuppressWarnings("PMD.GodClass")
class AWTEventDispatcher
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AWTEventDispatcher.class);

    /**
     * Our own mouse event for when we're dragged over from another hw
     * container.
     */
    private static final int LWD_MOUSE_DRAGGED_OVER = 1500;

    /** The canvas used by JOGL for rendering. */
    private final Component myCanvas;

    /**
     * Is the mouse over the top-level container.
     */
    private transient boolean myIsMouseInTopContainer;

    /**
     * The current subcomponent being hosted by this windowed component that has
     * events being forwarded to it. If this is null, there are currently no
     * events being forwarded to a subcomponent.
     */
    private transient Component myMouseEventTarget;

    /**
     * The last component entered.
     */
    private transient Component myTargetLastEntered;

    /**
     * The windowed container that might be hosting events for subcomponents.
     */
    private final Container myTopContainer;

    /**
     * Constructor.
     *
     * @param topContainer The top-level container for handling events.
     * @param canvas The canvas used by JOGL for rendering.
     */
    public AWTEventDispatcher(Container topContainer, Component canvas)
    {
        myTopContainer = topContainer;
        myMouseEventTarget = null;
        myCanvas = canvas;
    }

    /**
     * Dispatches an event to a sub-component if necessary, and returns whether
     * or not the event was forwarded to a sub-component.
     *
     * @param evt the event
     */
    public void dispatchEvent(final AWTEvent evt)
    {
        if (evt instanceof MouseEvent)
        {
            // If we can find the mouse target, then deliver the event to the
            // top level container for regular processing. If not try to process
            // it ourselves.
            MouseEvent me = (MouseEvent)evt;
            MouseEvent convert = SwingUtilities.convertMouseEvent((Component)me.getSource(), me, myTopContainer);
            Component deepest = SwingUtilities.getDeepestComponentAt(myTopContainer, convert.getX(), convert.getY());
            boolean overCanvas = Utilities.sameInstance(myCanvas, deepest);

            if (overCanvas)
            {
                Component mouseTarget = AWTMouseTargetFinder.findMouseTarget(myTopContainer, convert.getX(), convert.getY());
                // If we can't find a mouse target, then the event will never be
                // dispatched to any specific component. When this is the case,
                // a situation sometimes occurs where the keyboard events are
                // generated neither by NEWT or by AWT. Request focus on the top
                // container to ensure that events are handled.
                if (mouseTarget == null)
                {
                    myTopContainer.requestFocus();
                }
            }
        }

        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(evt);
    }

    /**
     * This method attempts to distribute a mouse event to a lightweight
     * component. It tries to avoid doing any unnecessary probes down into the
     * component tree to minimize the overhead of determining where to route the
     * event, since mouse movement events tend to come in large and frequent
     * amounts. NOTE: Currently lightweight components are disable to prevent
     * them from displaying behind the canvas.
     *
     * @param event the mouse event.
     * @return true when the event has been consumed.
     */
    public boolean processMouseEvent(MouseEvent event)
    {
        int id = event.getID();
        Component mouseOver = AWTMouseTargetFinder.findMouseTarget(myTopContainer, event.getX(), event.getY());
        if (mouseOver == null)
        {
            // If I am over the canvas, the mouse target might be null, in this
            // case getDeepestComponentAt() should find the canvas.
            mouseOver = SwingUtilities.getDeepestComponentAt(myTopContainer, event.getX(), event.getY());
        }

        trackMouseEnterExit(mouseOver, event);

        if (!wasAMouseButtonDownBeforeThisEvent(event) && id != MouseEvent.MOUSE_CLICKED)
        {
            myMouseEventTarget = mouseOver;
        }

        if (myMouseEventTarget != null)
        {
            switch (id)
            {
                case MouseEvent.MOUSE_ENTERED:
                case MouseEvent.MOUSE_EXITED:
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    retargetMouseEvent(myMouseEventTarget, id, event);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    retargetMouseEvent(myMouseEventTarget, id, event);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    if (Utilities.sameInstance(mouseOver, myMouseEventTarget))
                    {
                        retargetMouseEvent(mouseOver, id, event);
                    }
                    break;
                case MouseEvent.MOUSE_MOVED:
                    retargetMouseEvent(myMouseEventTarget, id, event);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    if (wasAMouseButtonDownBeforeThisEvent(event))
                    {
                        retargetMouseEvent(myMouseEventTarget, id, event);
                    }
                    break;
                case MouseEvent.MOUSE_WHEEL:
                    retargetMouseEvent(mouseOver, id, event);
                    break;
                default:
                    LOGGER.warn("Received and unexpected mouse event target type : " + id);
                    break;
            }

            // Consuming of wheel events is implemented in "retargetMouseEvent".
            if (id != MouseEvent.MOUSE_WHEEL)
            {
                event.consume();
            }
        }
        return event.isConsumed();
    }

    /**
     * Sends a mouse event to the current mouse event recipient using the given
     * event (sent to the windowed host) as a srcEvent. If the mouse event
     * target is still in the component tree, the coordinates of the event are
     * translated to those of the target. If the target has been removed, we
     * don't bother to send the message.
     *
     * @param target The target for the event.
     * @param id The type of event.
     * @param me The mouse event.
     */
    private void retargetMouseEvent(Component target, int id, MouseEvent me)
    {
        if (target == null)
        {
            // mouse is over another heavy weight component or target is
            // disabled.
            return;
        }

        int x = me.getX();
        int y = me.getY();
        Component component;

        for (component = target; component != null
                && !Utilities.sameInstance(component, myTopContainer); component = component.getParent())
        {
            x -= component.getX();
            y -= component.getY();
        }
        MouseEvent retargeted;
        if (component != null)
        {
            if (id == MouseEvent.MOUSE_WHEEL)
            {
                retargeted = new MouseWheelEvent(target, id, me.getWhen(), me.getModifiersEx(), x, y,
                        me.getXOnScreen(), me.getYOnScreen(), me.getClickCount(), me.isPopupTrigger(),
                        ((MouseWheelEvent)me).getScrollType(), ((MouseWheelEvent)me).getScrollAmount(),
                        ((MouseWheelEvent)me).getWheelRotation());
            }
            else
            {
                retargeted = new MouseEvent(target, id, me.getWhen(), me.getModifiersEx(), x, y,
                        me.getXOnScreen(), me.getYOnScreen(), me.getClickCount(), me.isPopupTrigger(), me.getButton());
            }

            if (Utilities.sameInstance(target, myTopContainer))
            {
                // avoid recursively calling AWTEventDispatcher.
                myTopContainer.dispatchEvent(retargeted);
            }
            else
            {
                Container modalComp = AWTMouseTargetFinder.getModalComponent(myTopContainer);
                if (modalComp != null)
                {
                    if (modalComp.isAncestorOf(target))
                    {
                        target.dispatchEvent(retargeted);
                    }
                    else
                    {
                        me.consume();
                    }
                }
                else
                {
                    target.dispatchEvent(retargeted);
                }
            }
            if (id == MouseEvent.MOUSE_WHEEL && retargeted.isConsumed())
            {
                // An exception for wheel bubbling to the native system.
                // In "processMouseEvent" total event consuming for wheel events
                // is skipped.
                // Protection from bubbling of Java-accepted wheel events.
                me.consume();
            }
        }
    }

    /**
     * Generates enter/exit events as mouse moves over lw components.
     *
     * @param targetOver Target mouse is over (including top-level container)
     * @param e Mouse event in top-level container
     */
    private void trackMouseEnterExit(Component targetOver, MouseEvent e)
    {
        Component targetEnter = null;
        int id = e.getID();

        if (id != MouseEvent.MOUSE_EXITED && id != MouseEvent.MOUSE_DRAGGED && id != LWD_MOUSE_DRAGGED_OVER
                && !myIsMouseInTopContainer)
        {
            // any event but an exit or drag means we're in the top-level
            // container
            myIsMouseInTopContainer = true;
        }
        else if (id == MouseEvent.MOUSE_EXITED)
        {
            myIsMouseInTopContainer = false;
        }

        if (myIsMouseInTopContainer)
        {
            targetEnter = targetOver;
        }

        if (Utilities.sameInstance(myTargetLastEntered, targetEnter))
        {
            return;
        }

        if (myTargetLastEntered != null)
        {
            retargetMouseEvent(myTargetLastEntered, MouseEvent.MOUSE_EXITED, e);
        }
        if (id == MouseEvent.MOUSE_EXITED)
        {
            // consume exit event if we generate one
            e.consume();
        }

        if (targetEnter != null)
        {
            retargetMouseEvent(targetEnter, MouseEvent.MOUSE_ENTERED, e);
        }
        if (id == MouseEvent.MOUSE_ENTERED)
        {
            // consume enter event if we generate one
            e.consume();
        }

        myTargetLastEntered = targetEnter;
    }

    /**
     * This method effectively returns whether or not a mouse button was down
     * just BEFORE the event happened.
     *
     * @param event The mouse event.
     * @return true if a button was down before the event.
     */
    private boolean wasAMouseButtonDownBeforeThisEvent(MouseEvent event)
    {
        int modifiers = event.getModifiersEx();

        if (event.getID() == MouseEvent.MOUSE_PRESSED || event.getID() == MouseEvent.MOUSE_RELEASED)
        {
            switch (event.getButton())
            {
                case MouseEvent.BUTTON1:
                    modifiers ^= InputEvent.BUTTON1_DOWN_MASK;
                    break;
                case MouseEvent.BUTTON2:
                    modifiers ^= InputEvent.BUTTON2_DOWN_MASK;
                    break;
                case MouseEvent.BUTTON3:
                    modifiers ^= InputEvent.BUTTON3_DOWN_MASK;
                    break;
                default:
                    break;
            }
        }
        // modifiers now as just before event
        return (modifiers & (InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK)) != 0;
    }
}
