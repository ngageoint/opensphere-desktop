package io.opensphere.core.control;

import java.awt.event.KeyEvent;

/**
 * A binding that translates a key press to a compound event.
 */
public class CompoundPressedReleasedBind extends PressedReleasedKeyBindAbs
{
    /** Flag indicating if the key is currently pressed. */
    private volatile boolean myIsPressed;

    /**
     * Construct the binding from a default binding.
     *
     * @param defaultBind The default binding.
     * @param listener A listener for the compound event.
     * @param context The control context.
     */
    public CompoundPressedReleasedBind(DefaultKeyPressedBinding defaultBind, CompoundEventListener listener,
            ControlContext context)
    {
        this(defaultBind.getKeyCode(), defaultBind.getModifiersEx(), listener, context);
    }

    /**
     * Construct the binding from a key code.
     *
     * @param keyCode The key code. (e.g., {@link KeyEvent#VK_CANCEL})
     * @param listener A listener for the compound event.
     * @param context The control context.
     */
    public CompoundPressedReleasedBind(int keyCode, CompoundEventListener listener, ControlContext context)
    {
        this(keyCode, 0, listener, context);
    }

    /**
     * Construct the binding.
     *
     * @param keyCode The key code. (e.g., {@link KeyEvent#VK_CANCEL})
     * @param modifiersEx The modifier bits used to filter events.
     * @param listener A listener for the compound event.
     * @param context The control context.
     */
    public CompoundPressedReleasedBind(int keyCode, int modifiersEx, CompoundEventListener listener, ControlContext context)
    {
        super(listener, context, keyCode, modifiersEx);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (!myIsPressed)
        {
            CompoundEventListener listener = (CompoundEventListener)getListener();
            myIsPressed = true;
            listener.eventStarted(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        final CompoundEventListener listener = (CompoundEventListener)getListener();
        myIsPressed = false;
        listener.eventEnded(e);
    }
}
