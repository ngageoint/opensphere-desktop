package io.opensphere.core.util.swing;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

/**
 * Extension of {@link SwingWorker} that overrides the
 * {@link SwingWorker#done()} method to report exceptional conditions.
 *
 * @param <T> the result type returned by this {@code SwingWorker's}
 *            {@code doInBackground} and {@code get} methods
 * @param <V> the type used for carrying out intermediate results by this
 *            {@code SwingWorker's} {@code publish} and {@code process} methods
 */
public abstract class SwingWorkerAdapter<T, V> extends SwingWorker<T, V>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SwingWorkerAdapter.class);

    /**
     * Helper method that runs a {@link Runnable} in the background.
     *
     * @param r The runnable.
     */
    public static void runInBackground(final Runnable r)
    {
        new SwingWorkerAdapter<Void, Void>()
        {
            @Override
            protected Void doInBackground()
            {
                r.run();
                return null;
            }
        }.execute();
    }

    @Override
    protected void done()
    {
        try
        {
            get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            LOGGER.error(e, e);
        }
    }
}
