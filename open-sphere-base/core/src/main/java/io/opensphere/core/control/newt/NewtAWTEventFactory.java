package io.opensphere.core.control.newt;

import java.awt.Canvas;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * A factory to create AWT mouse and keyboard events from NEWT events.
 */
public class NewtAWTEventFactory
{
    /** A map of NEWT events to AWT events. */
    private static final TIntIntMap ourNewtToAWT = new TIntIntHashMap();

    /** The canvas which is used by JOGL for rendering. */
    private final Canvas myCanvas;

    static
    {
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_DESTROY_NOTIFY,
                java.awt.event.WindowEvent.WINDOW_CLOSING);
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_DESTROYED, java.awt.event.WindowEvent.WINDOW_CLOSED);

        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_GAINED_FOCUS,
                java.awt.event.WindowEvent.WINDOW_ACTIVATED);
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_GAINED_FOCUS,
                java.awt.event.WindowEvent.WINDOW_GAINED_FOCUS);
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_GAINED_FOCUS, java.awt.event.FocusEvent.FOCUS_GAINED);
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_LOST_FOCUS,
                java.awt.event.WindowEvent.WINDOW_DEACTIVATED);
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_LOST_FOCUS, java.awt.event.WindowEvent.WINDOW_LOST_FOCUS);
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_LOST_FOCUS, java.awt.event.FocusEvent.FOCUS_LOST);

        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_MOVED, java.awt.event.ComponentEvent.COMPONENT_MOVED);
        ourNewtToAWT.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_RESIZED, java.awt.event.ComponentEvent.COMPONENT_RESIZED);

        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_CLICKED, java.awt.event.MouseEvent.MOUSE_CLICKED);
        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_PRESSED, java.awt.event.MouseEvent.MOUSE_PRESSED);
        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_RELEASED, java.awt.event.MouseEvent.MOUSE_RELEASED);
        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_MOVED, java.awt.event.MouseEvent.MOUSE_MOVED);
        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_ENTERED, java.awt.event.MouseEvent.MOUSE_ENTERED);
        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_EXITED, java.awt.event.MouseEvent.MOUSE_EXITED);
        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_DRAGGED, java.awt.event.MouseEvent.MOUSE_DRAGGED);
        ourNewtToAWT.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_WHEEL_MOVED, java.awt.event.MouseEvent.MOUSE_WHEEL);

        ourNewtToAWT.put(com.jogamp.newt.event.KeyEvent.EVENT_KEY_PRESSED, java.awt.event.KeyEvent.KEY_PRESSED);
        ourNewtToAWT.put(com.jogamp.newt.event.KeyEvent.EVENT_KEY_RELEASED, java.awt.event.KeyEvent.KEY_RELEASED);
    }

    /**
     * Constructor.
     *
     * @param canvas The canvas which is used by JOGL for rendering.
     */
    public NewtAWTEventFactory(Canvas canvas)
    {
        myCanvas = canvas;
    }

    /**
     * Create an AWT KeyEvent from a NEWT KeyEvent.
     *
     * @param event The NEWT KeyEvent.
     * @return The AWT KeyEvent.
     */
    public final java.awt.event.KeyEvent createKeyEvent(com.jogamp.newt.event.KeyEvent event)
    {
        int keyCode = event.getKeyCode();
        int type = ourNewtToAWT.get(event.getEventType());
        int modifiers = newtModifiers2Awt(event.getModifiers(), false);
        return new java.awt.event.KeyEvent(myCanvas, type, event.getWhen(), modifiers, keyCode, event.getKeyChar());
    }

    /**
     * Create an AWT MouseEvent from a NEWT MouseEvent.
     *
     * @param event The NEWT MouseEvent.
     * @return The AWT MouseEvent.
     */
    public final java.awt.event.MouseEvent createMouseEvent(com.jogamp.newt.event.MouseEvent event)
    {
        int type = ourNewtToAWT.get(event.getEventType());

        int mods = newtModifiers2Awt(event.getModifiers(),
                event.getEventType() == com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_CLICKED);
        int rotation = (int)(event.isShiftDown() ? event.getRotation()[0] : event.getRotation()[1]);

        Point absolute;
        if (!myCanvas.isVisible())
        {
            absolute = new Point();
        }
        else
        {
            absolute = myCanvas.getLocationOnScreen();
        }
        absolute.translate(event.getX(), event.getY());

        java.awt.event.MouseEvent evt;
        if (rotation != 0)
        {
            // NEWT erroneously reports button1 down when rolling the mouse
            // wheel, so remove that from the modifiers.
            mods &= ~java.awt.event.MouseEvent.BUTTON1_DOWN_MASK;
            evt = new java.awt.event.MouseWheelEvent(myCanvas, type, event.getWhen(), mods, event.getX(), event.getY(),
                    absolute.x, absolute.y, -rotation, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 3, -rotation);
        }
        else
        {
            int button = newtButton2Awt(event.getButton());
            int clicks = event.getClickCount();
            if (event.getEventType() == com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_DRAGGED)
            {
                // For drag events the buttons are included in the modifiers.
                button = 0;
                clicks = 0;
            }
            else if (event.getEventType() == com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_PRESSED)
            {
                // The NEWT press event does not include the button modifier,
                // but the AWT event should.
                mods |= addPressedButtonAsModifier(button);
            }

            evt = new java.awt.event.MouseEvent(myCanvas, type, event.getWhen(), mods, event.getX(), event.getY(), absolute.x,
                    absolute.y, clicks, false, button);
        }

        return evt;
    }

    /**
     * Create an AWT KEY_TYPED KeyEvent from a NEWT KeyEvent.
     *
     * @param event The NEWT KeyEvent.
     * @return The AWT KeyEvent.
     */
    public final java.awt.event.KeyEvent createSyntheticTypedEvent(com.jogamp.newt.event.KeyEvent event)
    {
        int modifiers = newtModifiers2Awt(event.getModifiers(), false);
        // The key code must be VK_UNDEFINED for key typed events.
        return new java.awt.event.KeyEvent(myCanvas, java.awt.event.KeyEvent.KEY_TYPED, event.getWhen(), modifiers,
                java.awt.event.KeyEvent.VK_UNDEFINED, event.getKeyChar());
    }

    /**
     * For button presses, we must add the button mask because it is not
     * included in the NEWT event.
     *
     * @param button The button which is pressed.
     * @return The AWT mask for the button.
     */
    private int addPressedButtonAsModifier(int button)
    {
        switch (button)
        {
            case java.awt.event.MouseEvent.BUTTON1:
                return java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
            case java.awt.event.MouseEvent.BUTTON2:
                return java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
            case java.awt.event.MouseEvent.BUTTON3:
                return java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
            default:
                return 0;
        }
    }

    /**
     * Determine the AWT mask given the NEWT modifiers and NEWT mask. If the
     * NEWT modifiers include the NEWT mask, the AWT mask is returned.
     * Otherwise, 0 is returned.
     *
     * @param newtMods The NEWT modifiers.
     * @param newtMask The NEWT mask.
     * @param awtMask The AWT mask.
     * @return The AWT mask or 0.
     */
    private int determineMask(int newtMods, int newtMask, int awtMask)
    {
        return (newtMods & newtMask) == 0 ? 0 : awtMask;
    }

    /**
     * Convert the button for a newt event to the equivalent button for an AWT
     * event.
     *
     * @param newtButton the NEWT button.
     * @return the AWT button.
     */
    private int newtButton2Awt(int newtButton)
    {
        switch (newtButton)
        {
            case com.jogamp.newt.event.MouseEvent.BUTTON1:
                return java.awt.event.MouseEvent.BUTTON1;
            case com.jogamp.newt.event.MouseEvent.BUTTON2:
                return java.awt.event.MouseEvent.BUTTON2;
            case com.jogamp.newt.event.MouseEvent.BUTTON3:
                return java.awt.event.MouseEvent.BUTTON3;
            default:
                return 0;
        }
    }

    /**
     * Convert the modifiers for a newt event to the equivalent modifiers for an
     * AWT event.
     *
     * @param newtMods the NEWT modifiers.
     * @param clicked true when this is for a mouse clicked event. In this case,
     *            the button down masks will be ignored.
     * @return the AWT modifiers.
     */
    private int newtModifiers2Awt(int newtMods, boolean clicked)
    {
        int awtMods = 0;
        awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.SHIFT_MASK,
                java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.CTRL_MASK, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.META_MASK, java.awt.event.InputEvent.META_DOWN_MASK);
        awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.ALT_MASK, java.awt.event.InputEvent.ALT_DOWN_MASK);
        awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.ALT_GRAPH_MASK,
                java.awt.event.InputEvent.ALT_GRAPH_DOWN_MASK);
        if (!clicked)
        {
            awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.BUTTON1_MASK,
                    java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
            awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.BUTTON2_MASK,
                    java.awt.event.InputEvent.BUTTON2_DOWN_MASK);
            awtMods |= determineMask(newtMods, com.jogamp.newt.event.InputEvent.BUTTON3_MASK,
                    java.awt.event.InputEvent.BUTTON3_DOWN_MASK);
        }
        return awtMods;
    }
}
