package io.opensphere.core.util.swing.input.model;

/**
 * Boolean model.
 */
public class BooleanModel extends AbstractViewModel<Boolean>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Constructor with a null initial value. */
    public BooleanModel()
    {
    }

    /**
     * Constructor with a default value.
     *
     * @param initialValue The initial value.
     */
    public BooleanModel(boolean initialValue)
    {
        set(Boolean.valueOf(initialValue));
    }

    /**
     * Toggle the value of the model.
     */
    public void toggleValue()
    {
        Boolean value = get();
        if (value != null)
        {
            set(Boolean.valueOf(!value.booleanValue()));
        }
    }
}
