package io.opensphere.core.util.concurrent;

import java.util.concurrent.Executor;

/**
 * An executor that simply runs commands submitted to it, on the current thread.
 */
public class InlineExecutor implements Executor
{
    @Override
    public final void execute(Runnable command)
    {
        command.run();
    }
}
