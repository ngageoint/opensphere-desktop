package io.opensphere.core.video;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Nanoseconds;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A wrapper for a video content handler that delivers video content at
 * scheduled times. This should be run on a separate thread from the thread that
 * delivers images via {@link #handleContent(Object, long)}. The images will
 * then be delivered to the wrapped {@link VideoContentHandler} from the
 * {@link #run()} method.
 *
 * @param <T> The type of the video content.
 */
@ThreadSafe
@SuppressWarnings("PMD.GodClass")
public class ScheduledVideoContentHandlerWrapper<T> implements VideoContentHandler<T>, Runnable
{
    /** A value to indicate when a value has not been set. */
    public static final long NO_VALUE = Long.MIN_VALUE;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ScheduledVideoContentHandlerWrapper.class);

    /** Limit on how far in the future a frame can be. */
    private static final Duration MAX_SLEEP = new Seconds(60);

    /** The minimum sleep time. */
    private static final Duration MIN_SLEEP = new Milliseconds(10L);

    /** Indicates if {@link #close()} has been called. */
    private volatile boolean myClosed;

    /**
     * The queue populated by {@link #handleContent(Object, long)} and polled by
     * {@link #run} and {{@link #drainAndClose()}.
     */
    private final PriorityBlockingQueue<TimestampedContent<T>> myContentQueue = new PriorityBlockingQueue<>();

    /**
     * The system clock time when the first packet was received.
     */
    private volatile long myFirstNanoTime = NO_VALUE;

    /** The handle time. */
    private final ReadOnlyProperty<? extends TimeInstant> myHandleTime;

    /** Listener for changes to the handle time. */
    private final ChangeListener<TimeInstant> myHandleTimeListener = (observable, oldValue, newValue) -> kickQueue();

    /**
     * Condition used to signal when something is added to
     * {@link #myContentQueue}.
     */
    private final Condition myNotEmptyCondition;

    /**
     * Lock used to signal when something is added to {@link #myContentQueue}.
     */
    private final Lock myNotEmptyLock = new ReentrantLock();

    /** The reference PTS in the stream (in milliseconds). */
    @GuardedBy("this")
    private long myReferencePtsMillis;

    /** The time corresponding to {@link #myReferencePtsMillis}. */
    private final TimeInstant myReferenceTime;

    /** Lock used to signal when {@link #run()} is done. */
    private final Lock myRunningLock = new ReentrantLock();

    /** The video content handler. */
    private final VideoContentHandler<? super T> myVideoContentHandler;

    /**
     * Constructor.
     *
     * @param videoContentHandler The wrapped handler, which is not closed by
     *            the wrapper.
     * @param referencePtsMillis A reference presentation time stamp in
     *            milliseconds. If this is {@link #NO_VALUE}, it will be set
     *            when the first packet is received.
     * @param referenceTime The time corresponding to startPtsMillis.
     * @param handleTime The time of the frame to be displayed.
     */
    public ScheduledVideoContentHandlerWrapper(VideoContentHandler<? super T> videoContentHandler, long referencePtsMillis,
            TimeInstant referenceTime, ReadOnlyProperty<TimeInstant> handleTime)
    {
        myReferencePtsMillis = referencePtsMillis;
        myReferenceTime = Utilities.checkNull(referenceTime, "referenceTime");
        myHandleTime = Utilities.checkNull(handleTime, "handleTime");
        myVideoContentHandler = Utilities.checkNull(videoContentHandler, "videoContentHandler");
        myNotEmptyCondition = myNotEmptyLock.newCondition();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Start time " + myReferenceTime + " pts " + myReferencePtsMillis);
        }
    }

    /**
     * Close the content handler and wait for all remaining content to be
     * delivered.
     */
    @Override
    public void close()
    {
        synchronized (this) // synchronize with open()
        {
            myClosed = true;
        }
        kickQueue();
        // Don't lock this inside the synchronized block, to avoid dead-lock.
        myRunningLock.lock();
        myRunningLock.unlock();

        synchronized (this) // synchronize with open()
        {
            // Hopefully open() won't get called concurrently, but just
            // in case...
            if (myClosed)
            {
                myHandleTime.removeListener(myHandleTimeListener);
            }
        }
    }

    /**
     * Trash any content waiting in the queue and close the wrapper.
     */
    public synchronized void drainAndClose()
    {
        if (!myContentQueue.isEmpty())
        {
            Collection<TimestampedContent<T>> drained = New.collection(myContentQueue.size());
            myContentQueue.drainTo(drained);
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Drained queue of " + drained.size() + " items");
            }
            kickQueue();
            for (TimestampedContent<T> object : drained)
            {
                if (object.getContent() instanceof AutoCloseable)
                {
                    Utilities.close((AutoCloseable)object.getContent());
                }
            }
        }
        close();
    }

    @Override
    public synchronized void handleContent(T content, long ptsMillis)
    {
        if (myClosed || ptsMillis < 0)
        {
            if (content instanceof AutoCloseable)
            {
                Utilities.close((AutoCloseable)content);
            }
            return;
        }
        if (myFirstNanoTime == NO_VALUE)
        {
            myFirstNanoTime = System.nanoTime();
        }
        if (myReferencePtsMillis == NO_VALUE)
        {
            myReferencePtsMillis = ptsMillis;
        }

        long ptsOffsetMillis = ptsMillis - myReferencePtsMillis;
        TimestampedContent<T> timestamped = new TimestampedContent<>(content,
                myReferenceTime.plus(new Milliseconds(ptsOffsetMillis)), ptsMillis);
        if (timestamped.getTimestamp().minus(TimeInstant.get()).isLessThan(MAX_SLEEP))
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Queuing content " + timestamped);
            }
            myContentQueue.offer(timestamped);
            kickQueue();
        }
        else if (timestamped.getContent() != null)
        {
            LOGGER.warn("Skipping future content for " + timestamped);
            if (content instanceof AutoCloseable)
            {
                Utilities.close((AutoCloseable)content);
            }
        }
    }

    @Override
    public synchronized void open() // synchronize with close()
    {
        if (myClosed)
        {
            myClosed = false;
            myHandleTime.addListener(myHandleTimeListener);
        }
    }

    @Override
    public void run()
    {
        myRunningLock.lock();
        try
        {
            doRun();
        }
        finally
        {
            myRunningLock.unlock();
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Exiting run");
            }
        }
    }

    /**
     * Cause the {@link #run()} method to check the content at the head of the
     * queue to see if it should be delivered.
     */
    protected void kickQueue()
    {
        myNotEmptyLock.lock();
        try
        {
            myNotEmptyCondition.signalAll();
        }
        finally
        {
            myNotEmptyLock.unlock();
        }
    }

    /**
     * Poll the content queue and deliver content to the handler at the
     * appropriate times, until {@link #close()} is called.
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private void doRun()
    {
        long lastPtsOut = -1L;
        while (!myClosed || !myContentQueue.isEmpty())
        {
            TimestampedContent<T> obj = myContentQueue.peek();

            Duration sleepTime = obj == null ? Nanoseconds.MAXLONG : obj.getPtsMillis() == Long.MAX_VALUE ? Seconds.ZERO
                    : Nanoseconds.get(obj.getTimestamp().minus(getTargetTime()));

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(new StringBuilder(128).append("target: ").append(getTargetTime()).append(" earliest image time: ")
                        .append(obj == null ? "(no image)" : obj.getTimestamp()).append(" sleep time: ").append(sleepTime)
                        .toString());
            }
            if (obj == null || sleepTime.isGreaterThan(MIN_SLEEP))
            {
                try
                {
                    myNotEmptyLock.lockInterruptibly();
                    try
                    {
                        // Check one more time just in case close() was called
                        // from another thread.
                        if (!myClosed)
                        {
                            myNotEmptyCondition.awaitNanos(sleepTime.longValue());
                        }
                    }
                    finally
                    {
                        myNotEmptyLock.unlock();
                    }
                }
                catch (InterruptedException e)
                {
                    continue;
                }
            }
            else
            {
                obj = myContentQueue.poll();
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Queue size: " + myContentQueue.size());
                }
                if (obj != null)
                {
                    long ptsOut = obj.getPtsMillis();
                    if (ptsOut > lastPtsOut)
                    {
                        if (LOGGER.isTraceEnabled() && obj.getContent() != null)
                        {
                            LOGGER.trace("Delivering content " + obj);
                        }

                        myVideoContentHandler.handleContent(obj.getContent(), ptsOut);
                        lastPtsOut = ptsOut;
                    }
                    else if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Skipping out-of-order frame " + obj);
                        if (obj.getContent() instanceof AutoCloseable)
                        {
                            Utilities.close((AutoCloseable)obj.getContent());
                        }
                    }
                }
            }
        }
    }

    /**
     * Get the target time based on the handle time or the system time.
     *
     * @return The time.
     */
    private TimeInstant getTargetTime()
    {
        TimeInstant handle = myHandleTime.getValue();
        if (handle == null)
        {
            return TimeInstant
                    .get(myReferenceTime.getEpochMillis() + (System.nanoTime() - myFirstNanoTime) / Constants.NANO_PER_MILLI);
        }
        return handle;
    }

    /**
     * Association of content with a timestamp.
     *
     * @param <T> The type of the content.
     */
    private static final class TimestampedContent<T> implements Comparable<TimestampedContent<?>>
    {
        /** The content. */
        private final T myContent;

        /** The presentation timestamp for the content. */
        private final long myPtsMillis;

        /** The timestamp. */
        private final TimeInstant myTimestamp;

        /**
         * Constructor.
         *
         * @param content The content.
         * @param timestamp The timestamp.
         * @param ptsMillis The presentation timestamp for the content.
         */
        public TimestampedContent(T content, TimeInstant timestamp, long ptsMillis)
        {
            myContent = content;
            myTimestamp = Utilities.checkNull(timestamp, "timestamp");
            myPtsMillis = ptsMillis;
        }

        @Override
        public int compareTo(TimestampedContent<?> o)
        {
            return myPtsMillis == Long.MAX_VALUE ? 1 : myTimestamp.compareTo(o.myTimestamp);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            return myTimestamp.equals(((TimestampedContent<?>)obj).myTimestamp);
        }

        /**
         * Get the content.
         *
         * @return The content.
         */
        public T getContent()
        {
            return myContent;
        }

        /**
         * Get the presentation timestamp for the content.
         *
         * @return The timestamp.
         */
        public long getPtsMillis()
        {
            return myPtsMillis;
        }

        /**
         * Get the timestamp.
         *
         * @return The timestamp.
         */
        public TimeInstant getTimestamp()
        {
            return myTimestamp;
        }

        @Override
        public int hashCode()
        {
            return 31 + myTimestamp.hashCode();
        }

        @Override
        public String toString()
        {
            return "TimestampedContent [" + myTimestamp + " " + myPtsMillis + "]";
        }
    }
}
