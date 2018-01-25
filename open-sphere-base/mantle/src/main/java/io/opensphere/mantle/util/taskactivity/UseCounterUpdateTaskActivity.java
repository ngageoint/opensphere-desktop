package io.opensphere.mantle.util.taskactivity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * The Class UseCounterUpdateTaskActivity.
 *
 * A task activity that registers users and displays the activity label when
 * there is at least one count.
 */
public class UseCounterUpdateTaskActivity extends TaskActivity
{
    /** The Update set lock. */
    private final ReentrantLock myCounterLock;

    /** The Update set. */
    private final AtomicInteger myUseCounter = new AtomicInteger(0);

    /**
     * Instantiates a UseCounterUpdateTaskActivity.
     *
     * @param activityLabel the activity label to display when there is at a
     *            count of one.
     */
    public UseCounterUpdateTaskActivity(String activityLabel)
    {
        super();
        myCounterLock = new ReentrantLock();
        setActive(false);
        setLabelValue(activityLabel);
    }

    /**
     * Decrement update in progress.
     */
    public void decrementUseCounter()
    {
        myCounterLock.lock();
        try
        {
            myUseCounter.decrementAndGet();
        }
        finally
        {
            myCounterLock.unlock();
        }
        update();
    }

    /**
     * Increment update in progress.
     *
     */
    public void incrementUseCounter()
    {
        myCounterLock.lock();
        try
        {
            myUseCounter.incrementAndGet();
        }
        finally
        {
            myCounterLock.unlock();
        }
        update();
    }

    /**
     * Update.
     */
    private void update()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                boolean hasUpdates = false;
                myCounterLock.lock();
                try
                {
                    hasUpdates = myUseCounter.get() != 0;
                }
                finally
                {
                    myCounterLock.unlock();
                }
                setActive(hasUpdates);
            }
        });
    }
}
