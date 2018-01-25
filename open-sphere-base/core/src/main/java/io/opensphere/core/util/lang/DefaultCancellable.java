package io.opensphere.core.util.lang;

import javax.annotation.concurrent.ThreadSafe;

/** Default implementation that simply provides the cancelled state. */
@ThreadSafe
public class DefaultCancellable implements Cancellable
{
    /** The cancelled flag. */
    private volatile boolean myCancelled;

    @Override
    public void cancel()
    {
        myCancelled = true;
    }

    @Override
    public boolean isCancelled()
    {
        return myCancelled;
    }
}
