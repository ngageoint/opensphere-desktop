package io.opensphere.core.debug;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * An event queue replacement to determine if a thread has hung. This monitor is
 * a singleton.
 */
public class EventDispatchThreadHangMonitor extends EventQueue
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(EventDispatchThreadHangMonitor.class);

    /**
     * The singleton instance of the {@link EventDispatchThreadHangMonitor}.
     */
    private static final EventQueue INSTANCE = new EventDispatchThreadHangMonitor();

    /**
     * The interval at which the threads will be checked.
     */
    private static final long CHECK_INTERVAL_MS = 100;

    /**
     * The duration at which a thread is considered long running.
     */
    private static final long UNREASONABLE_DISPATCH_DURATION_MS = 500;

    /**
     * A time value used to mark that no current event is processing.
     */
    private static final long NO_CURRENT_EVENT = 0;

    /**
     * The time at which the last event dispatch began. This may be initialized
     * to {@link #NO_CURRENT_EVENT}.
     */
    private long myStartedLastEventDispatchAt = NO_CURRENT_EVENT;

    /**
     * A flag used to keep track of the reporting state.
     */
    private boolean myReportedHang;

    /**
     * The thread being monitored.
     */
    private Thread myEventDispatchThread;

    /**
     * Creates a new monitor.
     */
    public EventDispatchThreadHangMonitor()
    {
        initTimer();
    }

    /**
     * Initializes a new timer, and schedules it to timeout after
     * {@link #CHECK_INTERVAL_MS}.
     */
    private void initTimer()
    {
        final long initialDelayMS = 0;
        final boolean isDaemon = true;
        Timer timer = new Timer("EventDispatchThreadHangMonitor", isDaemon);
        timer.schedule(new HangTimer(), initialDelayMS, CHECK_INTERVAL_MS);
    }

    /**
     * Gets the number of milliseconds elapsed since
     * {@link #myStartedLastEventDispatchAt}.
     *
     * @return the number of milliseconds elapsed since
     *         {@link #myStartedLastEventDispatchAt}.
     */
    protected long getElapsedTime()
    {
        long currentTime = System.currentTimeMillis();
        return currentTime - myStartedLastEventDispatchAt;
    }

    /**
     * Initializes monitoring of the event queue, by inserting the singleton
     * instance into the system.
     */
    public static void initMonitoring()
    {
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(INSTANCE);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.EventQueue#dispatchEvent(java.awt.AWTEvent)
     */
    @Override
    protected void dispatchEvent(AWTEvent pEvent)
    {
        preDispatchEvent();
        super.dispatchEvent(pEvent);
        postDispatchEvent();
    }

    /**
     * Performs cleanup activity after a thread has completed.
     */
    protected void postDispatchEvent()
    {
        if (myReportedHang)
        {
            LOG.info("event dispatch thread unstuck after " + getElapsedTime() + " ms.");
        }
        myStartedLastEventDispatchAt = NO_CURRENT_EVENT;
    }

    /**
     * Performs setup when a new thread is started.
     */
    protected void preDispatchEvent()
    {
        if (myEventDispatchThread == null)
        {
            myEventDispatchThread = Thread.currentThread();
        }

        myReportedHang = false;
        myStartedLastEventDispatchAt = System.currentTimeMillis();
    }

    /**
     * A timer task used to detect if a thread has hung.
     */
    public class HangTimer extends TimerTask
    {
        /**
         * {@inheritDoc}
         *
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
            synchronized (INSTANCE)
            {
                checkForHang();
            }
        }

        /**
         * Examines if a thread's elapsed time has exceeded
         * {@link EventDispatchThreadHangMonitor#UNREASONABLE_DISPATCH_DURATION_MS}.
         */
        protected void checkForHang()
        {
            if (myStartedLastEventDispatchAt == NO_CURRENT_EVENT)
            {
                // don't destroy the timer when there's nothing happening,
                // because it would mean a lot more setup work
                // on every single AWT event that gets dispatched
                return;
            }

            if (getElapsedTime() > UNREASONABLE_DISPATCH_DURATION_MS)
            {
                reportHang();
            }
        }

        /**
         * Reports the hang to the long using a stack trace of the thread. This
         * reporting operation will only occur once, depending on the state of
         * the {@link EventDispatchThreadHangMonitor#myReportedHang} flag.
         */
        protected void reportHang()
        {
            if (!myReportedHang)
            {
                myReportedHang = true;

                StackTraceElement[] stackTrace = myEventDispatchThread.getStackTrace();
                LOG.info("Event dispatch thread stuck processing event for " + getElapsedTime() + " ms: "
                        + writeStackTrace(stackTrace));
            }
        }

        /**
         * Writes the stack trace to a String, and returns it.
         *
         * @param pElements the stack trace elements to write to string.
         * @return A multi-line formatted string containing a stack trace.
         */
        protected String writeStackTrace(StackTraceElement[] pElements)
        {
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement stackTraceElement : pElements)
            {
                builder.append("\n    ");
                builder.append(stackTraceElement.toString());
            }
            return builder.toString();
        }
    }
}
