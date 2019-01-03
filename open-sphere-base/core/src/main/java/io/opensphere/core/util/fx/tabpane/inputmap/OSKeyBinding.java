package io.opensphere.core.util.fx.tabpane.inputmap;

import java.util.Objects;

import io.opensphere.core.util.fx.OSUtil;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * KeyBindings are used to describe which action should occur based on some
 * KeyEvent state and Control state. These bindings are used to populate the
 * keyBindings variable on BehaviorBase. The KeyBinding can be subclassed to add
 * additional matching criteria. A match in a subclass should always have a
 * specificity that is 1 greater than its superclass in the case of a match, or
 * 0 in the case where there is no match.
 *
 * Note that this API is, at present, quite odd in that you use a constructor
 * and then use shift(), control(), alt(), or meta() separately. It gave me an
 * object-literal like approach but isn't ideal. We will want some builder
 * approach here (similar as in other places).
 */
public class OSKeyBinding
{
    /** A flag to denote that the myAlt key is part of the binding. */
    private OSOptionalBoolean myAlt = OSOptionalBoolean.FALSE;

    /** The key code bound in this binding instance. */
    private final KeyCode myCode;

    /** A flag to denote that the control key is part of the binding. */
    private OSOptionalBoolean myControl = OSOptionalBoolean.FALSE;

    /** The event type associated with the key binding, may be null. */
    private final EventType<KeyEvent> myEventType;

    /** A flag to denote that the 'myMeta' key is part of the binding. */
    private OSOptionalBoolean myMeta = OSOptionalBoolean.FALSE;

    /** A flag to denote that the shift key is part of the binding. */
    private OSOptionalBoolean myShift = OSOptionalBoolean.FALSE;

    /**
     * Designed for 'catch-all' situations, e.g. all KeyTyped events.
     *
     * @param type the event type to bind.
     */
    public OSKeyBinding(EventType<KeyEvent> type)
    {
        this(null, type);
    }

    /**
     * Creates a key binding for the raw key code (e.g.: no additional event
     * type).
     *
     * @param code the key myCode for which to create the key binding.
     */
    public OSKeyBinding(KeyCode code)
    {
        this(code, null);
    }

    /**
     * Creates a new binding for the supplied key myCode and optional event
     * type.
     *
     * @param code the key myCode for which to create the key binding.
     * @param type the event type to bind (may be null).
     */
    public OSKeyBinding(KeyCode code, EventType<KeyEvent> type)
    {
        this.myCode = code;
        this.myEventType = type != null ? type : KeyEvent.KEY_PRESSED;
    }

    /**
     * Converts the supplied event to a key binding instance.
     *
     * @param keyEvent the event to convert to a key binding.
     * @return a key binding generated from the supplied event.
     */
    public static OSKeyBinding toKeyBinding(KeyEvent keyEvent)
    {
        OSKeyBinding newKeyBinding = new OSKeyBinding(keyEvent.getCode(), keyEvent.getEventType());
        if (keyEvent.isShiftDown())
        {
            newKeyBinding.shift();
        }
        if (keyEvent.isControlDown())
        {
            newKeyBinding.control();
        }
        if (keyEvent.isAltDown())
        {
            newKeyBinding.alt();
        }
        if (keyEvent.isShortcutDown())
        {
            newKeyBinding.shortcut();
        }
        return newKeyBinding;
    }

    /**
     * A utility method to assign the alt value to true. Returns this instance
     * for call chaining.
     *
     * @return this instance for call chaining.
     */
    public final OSKeyBinding alt()
    {
        return alt(OSOptionalBoolean.TRUE);
    }

    /**
     * Assigns the alt value to the supplied value, and returns this instance
     * for call chaining.
     *
     * @param value the value to assign to the alt field.
     * @return this instance for call chaining.
     */
    public final OSKeyBinding alt(OSOptionalBoolean value)
    {
        myAlt = value;
        return this;
    }

    /**
     * A utility method to assign the control value to true. Returns this
     * instance for call chaining.
     *
     * @return this instance for call chaining.
     */
    public final OSKeyBinding control()
    {
        return control(OSOptionalBoolean.TRUE);
    }

    /**
     * Assigns the control value to the supplied value, and returns this
     * instance for call chaining.
     *
     * @param value the value to assign to the control field.
     * @return this instance for call chaining.
     */
    public final OSKeyBinding control(OSOptionalBoolean value)
    {
        myControl = value;
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof OSKeyBinding))
        {
            return false;
        }
        OSKeyBinding that = (OSKeyBinding)o;
        return Objects.equals(getCode(), that.getCode()) && Objects.equals(myEventType, that.myEventType)
                && Objects.equals(getShift(), that.getShift()) && Objects.equals(getControl(), that.getControl())
                && Objects.equals(getAlt(), that.getAlt()) && Objects.equals(getMeta(), that.getMeta());
    }

    /**
     * Gets the value of the {@link #myAlt} field.
     *
     * @return the value stored in the {@link #myAlt} field.
     */
    public OSOptionalBoolean getAlt()
    {
        return myAlt;
    }

    /**
     * Gets the value of the {@link #myCode} field.
     *
     * @return the value stored in the {@link #myCode} field.
     */
    public KeyCode getCode()
    {
        return myCode;
    }

    /**
     * Gets the value of the {@link #myControl} field.
     *
     * @return the value stored in the {@link #myControl} field.
     */
    public OSOptionalBoolean getControl()
    {
        return myControl;
    }

    /**
     * Gets the value of the {@link #myMeta} field.
     *
     * @return the value stored in the {@link #myMeta} field.
     */
    public OSOptionalBoolean getMeta()
    {
        return myMeta;
    }

    /**
     * Gets the value of the {@link #myShift} field.
     *
     * @return the value stored in the {@link #myShift} field.
     */
    public OSOptionalBoolean getShift()
    {
        return myShift;
    }

    /**
     * Gets an integer value representing how closely the mapping matches the
     * given {@link Event}. The higher the number, the greater the match. This
     * allows the OSInputMap to determine which mapping is most specific, and to
     * therefore fire the appropriate mapping {@link Mapping#getEventHandler()
     * EventHandler}.
     *
     * @param event the event to test.
     * @return an integer value representing how closely the mapping matches the
     *         given {@link Event}.
     */
    public int getSpecificity(KeyEvent event)
    {
        if (myCode != null && myCode != event.getCode())
        {
            return 0;
        }
        int s = 1;
        if (!myShift.equals(event.isShiftDown()))
        {
            return 0;
        }
        else if (myShift != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (!myControl.equals(event.isControlDown()))
        {
            return 0;
        }
        else if (myControl != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (!myAlt.equals(event.isAltDown()))
        {
            return 0;
        }
        else if (myAlt != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (!myMeta.equals(event.isMetaDown()))
        {
            return 0;
        }
        else if (myMeta != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (myEventType != null && myEventType != event.getEventType())
        {
            return 0;
        }
        s++;
        // We can now trivially accept it
        return s;
    }

    /**
     * Gets the value of the {@link #myEventType} field.
     *
     * @return the value stored in the {@link #myEventType} field.
     */
    public EventType<KeyEvent> getType()
    {
        return myEventType;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(getCode(), myEventType, getShift(), getControl(), getAlt(), getMeta());
    }

    /**
     * A utility method to assign the meta value to true. Returns this instance
     * for call chaining.
     *
     * @return this instance for call chaining.
     */
    public final OSKeyBinding meta()
    {
        return meta(OSOptionalBoolean.TRUE);
    }

    /**
     * Assigns the meta value to the supplied value, and returns this instance
     * for call chaining.
     *
     * @param value the value to assign to the meta field.
     * @return this instance for call chaining.
     */
    public final OSKeyBinding meta(OSOptionalBoolean value)
    {
        myMeta = value;
        return this;
    }

    /**
     * A utility method to assign the shift value to true. Returns this instance
     * for call chaining.
     *
     * @return this instance for call chaining.
     */
    public final OSKeyBinding shift()
    {
        return shift(OSOptionalBoolean.TRUE);
    }

    /**
     * Assigns the shift value to the supplied value, and returns this instance
     * for call chaining.
     *
     * @param value the value to assign to the shift field.
     * @return this instance for call chaining.
     */
    public final OSKeyBinding shift(OSOptionalBoolean value)
    {
        myShift = value;
        return this;
    }

    /**
     * Gets the platform shortcut key for the user's current operating system.
     *
     * @return the platform shortcut key for the user's current operating
     *         system.
     */
    public final OSKeyBinding shortcut()
    {
        switch (OSUtil.getPlatformShortcutKey())
        {
            case SHIFT:
                return shift();

            case CONTROL:
                return control();

            case ALT:
                return alt();

            case META:
                return meta();

            default:
                return this;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "OSKeyBinding [myCode=" + myCode + ", myShift=" + myShift + ", ctrl=" + myControl + ", myAlt=" + myAlt
                + ", myMeta=" + myMeta + ", type=" + myEventType + "]";
    }
}
