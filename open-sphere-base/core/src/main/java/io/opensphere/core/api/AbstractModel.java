package io.opensphere.core.api;

/**
 * Implementation of the {@link Model} interface that provides a flag indicating
 * if the model is displayable.
 */
public abstract class AbstractModel implements Model
{
    /** Flag indicating if the model is displayable. */
    private boolean myDisplayable = true;

    @Override
    public boolean isDisplayable()
    {
        return myDisplayable;
    }

    @Override
    public void setDisplayable(boolean displayable, Object source)
    {
        myDisplayable = displayable;
    }
}
