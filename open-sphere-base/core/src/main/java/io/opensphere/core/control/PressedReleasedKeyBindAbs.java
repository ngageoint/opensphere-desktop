package io.opensphere.core.control;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;

/**
 * Internal binding used to translate key pressed/released events to external
 * bindings.
 */
abstract class PressedReleasedKeyBindAbs extends KeyBindingAbs
{
    /** The key code. */
    private final int myKeyCode;

    /**
     * Construct the binding.
     *
     * @param listener The listener.
     * @param context The context.
     * @param keyCode The key code.
     * @param modifiersEx The modifier bitmask.
     */
    protected PressedReleasedKeyBindAbs(BoundEventListener listener, ControlContext context, int keyCode, int modifiersEx)
    {
        super(modifiersEx, listener, context);
        myKeyCode = keyCode;
    }

    /**
     * Get the key code.
     *
     * @return The key code.
     * @see KeyEvent#getKeyCode()
     */
    public int getKeyCode()
    {
        return myKeyCode;
    }

    /**
     * Method called when a key is pressed.
     *
     * @param e The key event.
     */
    public abstract void keyPressed(KeyEvent e);

    /**
     * Method called when the key is released.
     *
     * @param e The key event.
     */
    public abstract void keyReleased(KeyEvent e);

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if (getModifiersEx() != 0)
        {
            sb.append(InputEvent.getModifiersExText(getModifiersEx()));
            sb.append('+');
        }
        String keyCodeText = KeyEvent.getKeyText(myKeyCode);
        if (keyCodeText.length() == 1 && getModifiersEx() == 0)
        {
            char c = keyCodeText.charAt(0);
            if (Character.isUpperCase(c))
            {
                sb.append(keyCodeText.toLowerCase(Locale.US));
            }
            else
            {
                sb.append(c);
            }
        }
        else
        {
            sb.append(KeyEvent.getKeyText(myKeyCode));
        }
        return sb.toString();
    }
}
