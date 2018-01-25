package io.opensphere.featureactions.model;

/**
 * An abstract action representing what needs to be done to a feature.
 */
public abstract class Action implements Cloneable
{
    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * Gets the User readable name of the action.
     *
     * @return The name presented to the user.
     */
    protected abstract String getName();
}
