package io.opensphere.core.util.lang;

/**
 * Interface that actually includes the clone method.
 */
public interface ActuallyCloneable extends Cloneable
{
    /**
     * Produce a clone of this object.
     *
     * @return The cloned object.
     */
    ActuallyCloneable clone();
}
