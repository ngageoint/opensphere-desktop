package io.opensphere.mantle.util.taskactivity;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;

/**
 * The Class UseRegistryUpdateTaskActivity.
 *
 * A task activity that registers users and displays the activity label when
 * there is at least one user.
 */
public class UseRegistryUpdateTaskActivity extends TaskActivity
{
    /** The Update set. */
    private final Set<Object> myUpdateSet;

    /** The Update set lock. */
    private final ReentrantLock myUpdateSetLock;

    /**
     * Instantiates a UseRegistryUpdateTaskActivity.
     *
     * @param activityLabel the activity label to display when there is at least
     *            one user.
     */
    public UseRegistryUpdateTaskActivity(String activityLabel)
    {
        super();
        myUpdateSet = New.set();
        myUpdateSetLock = new ReentrantLock();
        setActive(false);
        setLabelValue(activityLabel);
    }

    /**
     * Register update in progress.
     *
     * @param o the o
     */
    public void registerUpdateInProgress(Object o)
    {
        myUpdateSetLock.lock();
        try
        {
            myUpdateSet.add(o);
        }
        finally
        {
            myUpdateSetLock.unlock();
        }
        update();
    }

    /**
     * Unregister update in progress.
     *
     * @param o the o
     */
    public void unregisterUpdateInProgress(Object o)
    {
        myUpdateSetLock.lock();
        try
        {
            myUpdateSet.remove(o);
        }
        finally
        {
            myUpdateSetLock.unlock();
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
                myUpdateSetLock.lock();
                try
                {
                    hasUpdates = !myUpdateSet.isEmpty();
                }
                finally
                {
                    myUpdateSetLock.unlock();
                }
                setActive(hasUpdates);
            }
        });
    }
}
