package io.opensphere.core.api;

/**
 * Interface for a data model. Data models are created by {@link Envoy}s.
 */
public interface Model
{
    /**
     * Get if the model is displayable.
     *
     * @return <code>true</code> if the model is displayable.
     */
    boolean isDisplayable();

    /**
     * Set if the model is displayable.
     *
     * @param displayable If the model is displayable.
     * @param source - the object making the change.
     */
    void setDisplayable(boolean displayable, Object source);
}
