package io.opensphere.core.util.collections.petrifyable;

/**
 * Interface for classes that can be petrified. They can be changed until
 * {@link #petrify()} is called, after which point they are immutable.
 */
public interface Petrifyable
{
    /**
     * Get if this object is petrified.
     *
     * @return {@code true} if the object can no longer be changed.
     */
    boolean isPetrified();

    /**
     * Petrify this object so it can no longer be changed.
     */
    void petrify();
}
