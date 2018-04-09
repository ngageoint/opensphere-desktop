package io.opensphere.core.debug;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A replacement event queue that can be swapped into the application to provide
 * notice when events have taken too long to process.
 */
public final class WatchdogDebugEventQueue extends EventQueue
{
    /**
     * The time with which excessively long events are detected.
     */
    private final Timer myTimer = new Timer(true);

    /**
     * A lock used for synchronization purposes.
     */
    private final Object myEventChangeLock = new Object();

    /**
     * The time at which the thread started execution.
     */
    private volatile long myEventDispatchThreadStart = -1;

    /**
     * The event currently being processed on the thread.
     */
    private volatile AWTEvent myEvent;

    /**
     * Hidden constructor, preventing use.
     */
    private WatchdogDebugEventQueue()
    {
        /* intentionally blank */
    }

    /**
     * Installs the watchdog event queue.
     *
     * @return the newly created watchdog event queue.
     */
    public static WatchdogDebugEventQueue install()
    {
        EventQueue systemEventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        WatchdogDebugEventQueue watchdogEventQueue = new WatchdogDebugEventQueue();
        systemEventQueue.push(watchdogEventQueue);
        return watchdogEventQueue;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.awt.EventQueue#dispatchEvent(java.awt.AWTEvent)
     */
    @Override
    protected void dispatchEvent(AWTEvent pEvent)
    {
        setEventDispatchingStart(pEvent, System.currentTimeMillis());
        super.dispatchEvent(pEvent);
        setEventDispatchingStart(null, -1);
    }

    /**
     * Stores the start time of the specified event in the monitor.
     *
     * @param pEvent the event for which to set the start time.
     * @param pTimestamp the start time of the event.
     */
    public void setEventDispatchingStart(AWTEvent pEvent, long pTimestamp)
    {
        synchronized (myEventChangeLock)
        {
            myEvent = pEvent;
            myEventDispatchThreadStart = pTimestamp;
        }
    }

    /**
     * Adds a new watchdog to the event queue, configured using the supplied
     * arguments. The watchdog will bet timed using the supplied long
     * (expressing milliseconds), and when the timer expires, the watchdog will
     * check to determine if the event is still active. If so, it will print a
     * stack trace. If the watchdog is repetitive, the timer will begin again.
     *
     * @param pMaxProcessingTime the maximum amount of time to use for the
     *            timer.
     * @param pListener The listener to notify when the watch dog is complete.
     * @param pRepetitive A flag used to allow multiple notifications for the
     *            same event.
     */
    public void addWatchdog(long pMaxProcessingTime, ActionListener pListener, boolean pRepetitive)
    {
        Watchdog watchdog = new Watchdog(pMaxProcessingTime, pListener, pRepetitive);
        myTimer.schedule(watchdog, pMaxProcessingTime, pMaxProcessingTime);
    }

    /**
     * A timer task responsible for notifying a listener when a task is taking
     * too long.
     */
    public class Watchdog extends TimerTask
    {
        /**
         * The maximum amount of time that the watch dog is allowed to process
         * before the listener is notified.
         */
        private final long myMaxProcessingTime;

        /**
         * The listener to notify when the watch dog is complete.
         */
        private final ActionListener myListener;

        /**
         * A flag used to allow multiple notifications for the same event.
         */
        private final boolean myRepetitive;

        /**
         * The last event reported by the watch dog.
         */
        private AWTEvent myLastReportedEvent;

        /**
         * Creates a new watch dog timer, which will notify a registered
         * listener after the timeout has occurred.
         *
         * @param pMaxProcessingTime The maximum amount of time that the watch
         *            dog is allowed to process before the listener is notified.
         * @param pListener The listener to notify when the watch dog is
         *            complete.
         * @param pRepetitive A flag used to allow multiple notifications for
         *            the same event.
         */
        public Watchdog(long pMaxProcessingTime, ActionListener pListener, boolean pRepetitive)
        {
            if (pListener == null)
            {
                throw new IllegalArgumentException("Listener cannot be null.");
            }
            myMaxProcessingTime = pMaxProcessingTime;
            myListener = pListener;
            myRepetitive = pRepetitive;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
            long time;
            AWTEvent currentEvent;
            synchronized (myEventChangeLock)
            {
                time = myEventDispatchThreadStart;
                currentEvent = myEvent;
            }

            long currentTime = System.currentTimeMillis();

            if (time != -1 && (currentTime - time) > myMaxProcessingTime && myRepetitive || currentEvent != myLastReportedEvent)
            {
                myListener.actionPerformed(new ActionEvent(currentEvent, -1, null));
                myLastReportedEvent = currentEvent;
            }
        }
    }
}
