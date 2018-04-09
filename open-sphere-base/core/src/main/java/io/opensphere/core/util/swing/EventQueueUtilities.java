package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.HappyCallable;
import io.opensphere.core.util.lang.ImpossibleException;

/**
 * Utilities for running tasks on the {@link EventQueue}.
 */
public final class EventQueueUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(EventQueueUtilities.class);

    /**
     * Wrapper for {@link EventQueue#invokeLater(Runnable)} that provides
     * instrumentation.
     *
     * @param runnable the runnable to run later
     */
    public static void invokeLater(Runnable runnable)
    {
        EventQueue.invokeLater(runnable);
//        EventQueueExecutor.invokeLaterInstrumented(runnable);
    }

    /**
     * Run a task in the background and set the cursor to the wait cursor while
     * the task is running.
     *
     * @param <T> The type of the return value.
     * @param cursorOwner The component to set the cursor on.
     * @param task The task to run.
     * @param resultConsumer The consumer for the result of the task.
     * @param errorHandler Handler that will be sent any exceptions that result
     *            from running the task.
     */
    public static <T> void runInBackgroundAndReturnResult(final Component cursorOwner, final Callable<T> task,
            Consumer<? super T> resultConsumer, Consumer<? super Exception> errorHandler)
    {
        assert EventQueue.isDispatchThread();
        final Cursor cursor;
        if (cursorOwner == null)
        {
            cursor = null;
        }
        else
        {
            cursor = cursorOwner.getCursor();
            cursorOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        SwingWorker<T, Void> worker = new SwingWorker<T, Void>()
        {
            @Override
            @SuppressWarnings("PMD.SignatureDeclareThrowsException")
            protected T doInBackground() throws Exception
            {
                return task.call();
            }

            @Override
            protected void done()
            {
                if (cursorOwner != null)
                {
                    cursorOwner.setCursor(cursor);
                }

                try
                {
                    resultConsumer.accept(get());
                }
                catch (InterruptedException | ExecutionException e)
                {
                    LOGGER.debug("Result consumer encountered an exception while acception data.", e);
                    errorHandler.accept(e);
                }
            }
        };
        worker.execute();
    }

    /**
     * Run a task in the background and set the cursor to the wait cursor while
     * the task is running.
     *
     * @param <T> The type of the return value.
     * @param cursorOwner The component to set the cursor on.
     * @param task The task to run.
     * @param consumer The consumer for the result of the task.
     */
    public static <T> void runInBackgroundAndReturnResult(final Component cursorOwner, final HappyCallable<T> task,
            Consumer<? super T> consumer)
    {
        runInBackgroundAndReturnResult(cursorOwner, (Callable<T>)task, consumer, null);
    }

    /**
     * If the current thread is not the event dispatch thread, schedule the task
     * on the event dispatch thread and return. If the current thread is the
     * event dispatch thread, run the task immediately.
     *
     * @param task The task.
     */
    public static void runOnEDT(final Runnable task)
    {
        if (EventQueue.isDispatchThread())
        {
            task.run();
        }
        else
        {
            EventQueue.invokeLater(task);
//            EventQueueExecutor.invokeLaterInstrumented(task);
        }
    }

    /**
     * Run a task on the EDT after a delay.
     *
     * @param delayMilliseconds The delay.
     * @param task The task.
     *
     * @return The {@code Timer}, which may be used to cancel the task if
     *         necessary.
     */
    public static Timer runOnEDTAfterDelay(int delayMilliseconds, final Runnable task)
    {
        javax.swing.Timer timer = new javax.swing.Timer(delayMilliseconds, e -> task.run());
        timer.setRepeats(false);
        timer.start();
        return timer;
    }

    /**
     * Run a task on the event dispatch thread and wait until it completes. If
     * the current thread is the event dispatch thread, simply run the task.
     * Return the result of the task.
     *
     * @param <T> The type of the return value.
     * @param task The task.
     * @return The result of the task.
     * @throws ExecutionException If the task throws a checked exception.
     */
    @SuppressWarnings({ "PMD.SignatureDeclareThrowsException", "PMD.AvoidRethrowingException" })
    public static <T> T callOnEdt(Callable<T> task) throws ExecutionException
    {
        if (EventQueue.isDispatchThread())
        {
            try
            {
                return task.call();
            }
            catch (RuntimeException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                throw new ExecutionException(e);
            }
        }
        FutureTask<T> futureTask = new FutureTask<>(task);
        runOnEDT(futureTask);
        while (true)
        {
            try
            {
                return futureTask.get();
            }
            catch (InterruptedException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Interrupted while waiting for EventQueue task.", e);
                }
            }
            catch (ExecutionException e)
            {
                LOGGER.debug("Excecution exception while waiting for EventQueue task.", e);
                if (e.getCause() instanceof RuntimeException)
                {
                    throw (RuntimeException)e.getCause();
                }
                else if (e.getCause() instanceof Error)
                {
                    throw (Error)e.getCause();
                }
                else
                {
                    throw e;
                }
            }
        }
    }

    /**
     * Run a task on the event dispatch thread and wait until it completes. If
     * the current thread is the event dispatch thread, simply run the task.
     * Return the result of the task.
     *
     * @param <T> The type of the return value.
     * @param task The task.
     * @return The result of the task.
     */
    public static <T> T happyOnEdt(HappyCallable<T> task)
    {
        try
        {
            return callOnEdt(task);
        }
        catch (ExecutionException e)
        {
            throw new ImpossibleException(e);
        }
    }

    /**
     * Run a task on the event dispatch thread and wait until it completes. If
     * the current thread is the event dispatch thread, simply run the task.
     *
     * @param task The task.
     */
    public static void runOnEDTAndWait(final Runnable task)
    {
        if (EventQueue.isDispatchThread())
        {
            task.run();
        }
        else
        {
            while (true)
            {
                try
                {
                    EventQueue.invokeAndWait(task);
                    return;
                }
                catch (InterruptedException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Interrupted while waiting for EventQueue task.", e);
                    }
                }
                catch (InvocationTargetException e)
                {
                    LOGGER.debug("Execution exception encountered while waiting for EventQueue task.", e);
                    if (e.getCause() instanceof RuntimeException)
                    {
                        throw (RuntimeException)e.getCause();
                    }
                    else
                    {
                        throw (Error)e.getCause();
                    }
                }
            }
        }
    }

    /**
     * Run a repeating task on the EDT after a delay.
     *
     * @param initialDelayMilliseconds The delay.
     * @param periodMilliseconds The time between invocations after the first
     *            one.
     * @param task The task.
     *
     * @return The {@code Timer}, which may be used to cancel the task if
     *         necessary.
     */
    public static javax.swing.Timer runOnEDTRepeating(int initialDelayMilliseconds, int periodMilliseconds, final Runnable task)
    {
        javax.swing.Timer timer = new javax.swing.Timer(initialDelayMilliseconds, e -> task.run());
        timer.setDelay(periodMilliseconds);
        timer.start();
        return timer;
    }

    /**
     * Run a repeating task on the EDT.
     *
     * @param periodMilliseconds The time between invocations after the first
     *            one.
     * @param task The task.
     *
     * @return The {@code Timer}, which may be used to cancel the task if
     *         necessary.
     */
    public static javax.swing.Timer runOnEDTRepeating(int periodMilliseconds, final Runnable task)
    {
        javax.swing.Timer timer = new javax.swing.Timer(periodMilliseconds, e -> task.run());
        timer.setInitialDelay(0);
        timer.start();
        return timer;
    }

    /**
     * Run a task in the background and set the cursor to the wait cursor while
     * the task is running.
     *
     * @param cursorOwner The component to set the cursor on.
     * @param task The task to run.
     */
    public static void waitCursorRun(final Component cursorOwner, final Runnable task)
    {
        waitCursorRun(cursorOwner, task, (Runnable)null);
    }

    /**
     * Run a task in the background and set the cursor to the wait cursor while
     * the task is running. Optionally run a second task on the EDT after the
     * background task is complete.
     *
     * @param cursorOwner The component to set the cursor on.
     * @param backgroundTask The task to run on the background.
     * @param doneTask The task to run on the EDT once the background task is
     *            complete.
     * @return The worker.
     */
    public static SwingWorker<Void, Void> waitCursorRun(final Component cursorOwner, final Runnable backgroundTask,
            final Runnable doneTask)
    {
        assert EventQueue.isDispatchThread();
        final Cursor cursor = cursorOwner.getCursor();
        cursorOwner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground()
            {
                backgroundTask.run();
                return null;
            }

            @Override
            protected void done()
            {
                cursorOwner.setCursor(cursor);

                try
                {
                    get();
                }
                catch (CancellationException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e);
                    }
                }
                catch (ExecutionException e)
                {
                    LOGGER.debug(e);
                    if (e.getCause() instanceof RuntimeException)
                    {
                        throw (RuntimeException)e.getCause();
                    }
                    else
                    {
                        throw (Error)e.getCause();
                    }
                }
                catch (InterruptedException e)
                {
                    throw new ImpossibleException(e);
                }
                finally
                {
                    if (doneTask != null)
                    {
                        doneTask.run();
                    }
                }
            }
        };
        worker.execute();
        return worker;
    }

    /**
     * Disallow instantiation.
     */
    private EventQueueUtilities()
    {
    }
}
