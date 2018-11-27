package io.opensphere.core.util.fx.tabpane.inputmap;

import java.util.Objects;

import io.opensphere.core.util.fx.OSUtil;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 */
public class OSKeyBinding
{
    private final KeyCode code;

    private final EventType<KeyEvent> eventType;

    private OSOptionalBoolean shift = OSOptionalBoolean.FALSE;

    private OSOptionalBoolean ctrl = OSOptionalBoolean.FALSE;

    private OSOptionalBoolean alt = OSOptionalBoolean.FALSE;

    private OSOptionalBoolean meta = OSOptionalBoolean.FALSE;

    public OSKeyBinding(KeyCode code)
    {
        this(code, null);
    }

    /**
     * Designed for 'catch-all' situations, e.g. all KeyTyped events.
     *
     * @param type
     */
    public OSKeyBinding(EventType<KeyEvent> type)
    {
        this(null, type);
    }

    public OSKeyBinding(KeyCode code, EventType<KeyEvent> type)
    {
        this.code = code;
        this.eventType = type != null ? type : KeyEvent.KEY_PRESSED;
    }

    public final OSKeyBinding shift()
    {
        return shift(OSOptionalBoolean.TRUE);
    }

    public final OSKeyBinding shift(OSOptionalBoolean value)
    {
        shift = value;
        return this;
    }

    public final OSKeyBinding ctrl()
    {
        return ctrl(OSOptionalBoolean.TRUE);
    }

    public final OSKeyBinding ctrl(OSOptionalBoolean value)
    {
        ctrl = value;
        return this;
    }

    public final OSKeyBinding alt()
    {
        return alt(OSOptionalBoolean.TRUE);
    }

    public final OSKeyBinding alt(OSOptionalBoolean value)
    {
        alt = value;
        return this;
    }

    public final OSKeyBinding meta()
    {
        return meta(OSOptionalBoolean.TRUE);
    }

    public final OSKeyBinding meta(OSOptionalBoolean value)
    {
        meta = value;
        return this;
    }

    public final OSKeyBinding shortcut()
    {
        switch (OSUtil.getPlatformShortcutKey())
        {
            case SHIFT:
                return shift();

            case CONTROL:
                return ctrl();

            case ALT:
                return alt();

            case META:
                return meta();

            default:
                return this;
        }
    }

    public final KeyCode getCode()
    {
        return code;
    }

    public final EventType<KeyEvent> getType()
    {
        return eventType;
    }

    public final OSOptionalBoolean getShift()
    {
        return shift;
    }

    public final OSOptionalBoolean getCtrl()
    {
        return ctrl;
    }

    public final OSOptionalBoolean getAlt()
    {
        return alt;
    }

    public final OSOptionalBoolean getMeta()
    {
        return meta;
    }

    public int getSpecificity(KeyEvent event)
    {
        int s = 0;
        if (code != null && code != event.getCode())
        {
            return 0;
        }
        else
        {
            s = 1;
        }
        if (!shift.equals(event.isShiftDown()))
        {
            return 0;
        }
        else if (shift != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (!ctrl.equals(event.isControlDown()))
        {
            return 0;
        }
        else if (ctrl != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (!alt.equals(event.isAltDown()))
        {
            return 0;
        }
        else if (alt != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (!meta.equals(event.isMetaDown()))
        {
            return 0;
        }
        else if (meta != OSOptionalBoolean.ANY)
        {
            s++;
        }
        if (eventType != null && eventType != event.getEventType())
        {
            return 0;
        }
        else
        {
            s++;
        }
        // We can now trivially accept it
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OSKeyBinding [code=" + code + ", shift=" + shift + ", ctrl=" + ctrl + ", alt=" + alt + ", meta=" + meta
                + ", type=" + eventType + "]";
    }

    /** {@inheritDoc} */
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
        return Objects.equals(getCode(), that.getCode()) && Objects.equals(eventType, that.eventType)
                && Objects.equals(getShift(), that.getShift()) && Objects.equals(getCtrl(), that.getCtrl())
                && Objects.equals(getAlt(), that.getAlt()) && Objects.equals(getMeta(), that.getMeta());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Objects.hash(getCode(), eventType, getShift(), getCtrl(), getAlt(), getMeta());
    }

    public static OSKeyBinding toKeyBinding(KeyEvent keyEvent)
    {
        OSKeyBinding newKeyBinding = new OSKeyBinding(keyEvent.getCode(), keyEvent.getEventType());
        if (keyEvent.isShiftDown())
        {
            newKeyBinding.shift();
        }
        if (keyEvent.isControlDown())
        {
            newKeyBinding.ctrl();
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
     * A tri-state boolean used with OSKeyBinding.
     */
    public enum OSOptionalBoolean
    {
        TRUE, FALSE, ANY;

        public boolean equals(boolean b)
        {
            if (this == ANY)
            {
                return true;
            }
            if (b && this == TRUE)
            {
                return true;
            }
            if (!b && this == FALSE)
            {
                return true;
            }
            return false;
        }
    }

}
