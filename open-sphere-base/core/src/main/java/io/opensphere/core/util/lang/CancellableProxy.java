package io.opensphere.core.util.lang;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A proxy for a {@link Cancellable} that manages the cancelled state until the
 * actual {@link Cancellable} is available.
 */
@ThreadSafe
public class CancellableProxy extends DefaultCancellable
{
    /** The cancellable. */
    private Cancellable myCancellable;

    @Override
    public synchronized void cancel()
    {
        if (!isCancelled())
        {
            super.cancel();
            if (myCancellable != null)
            {
                myCancellable.cancel();
            }
        }
    }

    /**
     * Get the cancellable.
     *
     * @return The cancellable.
     */
    public synchronized Cancellable getCancellable()
    {
        return myCancellable;
    }

    /**
     * Set the cancellable.
     *
     * @param cancellable The cancellable.
     */
    public synchronized void setCancellable(Cancellable cancellable)
    {
        myCancellable = cancellable;
        if (isCancelled())
        {
            myCancellable.cancel();
        }
    }
}
