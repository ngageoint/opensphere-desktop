package io.opensphere.core.util.collections.petrifyable;

/** Convenience implementation of {@link Petrifyable}. */
public abstract class AbstractPetrifyable implements Petrifyable
{
    /** Flag indicating if the object is petrified. */
    private boolean myPetrified;

    @Override
    public synchronized boolean isPetrified()
    {
        return myPetrified;
    }

    @Override
    public synchronized void petrify()
    {
        myPetrified = true;
    }
}
