package io.opensphere.core.control;

/**
 * The Class KeyBindingChangeEvent.
 */
public class KeyBindingChangeEvent
{
    /** The Source. */
    private final Object mySource;

    /** The Key binding name. */
    private final String myKeyBindingName;

    /** The Key binding change type. */
    private final KeyBindingChangeType myKeyBindingChangeType;

    /**
     * Instantiates a new key binding change event.
     *
     * @param source the source
     * @param bindingName the binding name
     * @param type the type
     */
    public KeyBindingChangeEvent(Object source, String bindingName, KeyBindingChangeType type)
    {
        mySource = source;
        myKeyBindingName = bindingName;
        myKeyBindingChangeType = type;
    }

    /**
     * Gets the key binding change type.
     *
     * @return the key binding change type
     */
    public KeyBindingChangeType getKeyBindingChangeType()
    {
        return myKeyBindingChangeType;
    }

    /**
     * Gets the key binding name.
     *
     * @return the key binding name
     */
    public String getKeyBindingName()
    {
        return myKeyBindingName;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public Object getSource()
    {
        return mySource;
    }
}
