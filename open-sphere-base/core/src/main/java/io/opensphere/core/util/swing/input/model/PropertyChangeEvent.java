package io.opensphere.core.util.swing.input.model;

import java.util.EventObject;

/**
 * Model property change event.
 */
public class PropertyChangeEvent extends EventObject
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The event property. */
    private final Property myProperty;

    /**
     * Constructor.
     *
     * @param src The source
     * @param property The event property
     */
    public PropertyChangeEvent(Object src, Property property)
    {
        super(src);
        myProperty = property;
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public Property getProperty()
    {
        return myProperty;
    }

    @Override
    public String toString()
    {
        return "PropertyChangeEvent [property=" + myProperty + "]";
    }

    /**
     * The event property.
     */
    public enum Property
    {
        /** Change in whether the model is required. */
        REQUIRED,

        /** Change in whether the model is enabled. */
        ENABLED,

        /** Change in whether the model is visible. */
        VISIBLE,

        /** Change in model options. */
        OPTIONS,

        /** Change in validation criteria. */
        VALIDATION_CRITERIA,

        /** Change in the view parameters. */
        VIEW_PARAMETERS,

        /** Change in the name or the description. */
        NAME_AND_DESCRIPTION,

        /** Change in one of the values making up a wrapped model. */
        WRAPPED_VALUE_CHANGED,

        ;
    }
}
