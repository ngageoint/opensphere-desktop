package io.opensphere.core.appl;

import org.apache.log4j.Logger;

import io.opensphere.core.MemoryManager;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.MemoryUtilities;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import net.jcip.annotations.GuardedBy;

/** Implementation class for {@link MemoryManager}. */
class MemoryManagerImpl implements MemoryManager
{
    /**
     * The delay in milliseconds before doing a garbage collect in the
     * {@link io.opensphere.core.MemoryManager.Status#WARNING} state.
     */
    private static final long GC_DELAY = Long.getLong("opensphere.memory.gcDelay", 1000).longValue();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(MemoryManagerImpl.class);

    /**
     * The amount of time in milliseconds that the memory must be below the low
     * water mark of the current status level before the status is downgraded.
     */
    private static final long STATUS_BOILDOWN_MILLIS = Long.getLong("opensphere.memory.boildownMillis", 5000).longValue();

    /** The change support. */
    private final ChangeSupport<MemoryListener> myChangeSupport = new WeakChangeSupport<>();

    /** The current status. */
    @GuardedBy("this")
    private Status myCurrentStatus = Status.NOMINAL;

    /**
     * The time of the last status change, as reported by
     * {@link System#currentTimeMillis()}.
     */
    private volatile long myLastStatusChangeTimeMillis;

    /**
     * When the memory goes below the low water mark of the current status, the
     * pending status is changed to the lower level. Once the memory has been
     * within the pending status for the boil-down time, the current status can
     * be changed to the pending status.
     */
    @GuardedBy("this")
    private Status myPendingStatus;

    /**
     * The time that the pending status was set, as reported by
     * {@link System#currentTimeMillis()}.
     */
    @GuardedBy("this")
    private long myPendingStatusTimeMillis;

    /**
     * Get the status that matches a ratio of used memory to total memory.
     *
     * @param ratio The ratio.
     * @return The status.
     */
    private static Status getStatusForRatio(double ratio)
    {
        Status worstStatus = null;
        for (final Status st : Status.values())
        {
            if (st.containsRatio(ratio) && (worstStatus == null || worstStatus.getLowwaterRatio() < st.getLowwaterRatio()))
            {
                worstStatus = st;
            }
        }
        if (worstStatus == null)
        {
            worstStatus = Status.CRITICAL;
        }
        return worstStatus;
    }

    /** Constructor. */
    public MemoryManagerImpl()
    {
        final double memoryDelta = .01;
        MemoryUtilities.addMemoryMonitor(memoryDelta, this::handleMemoryMonitored);
    }

    @Override
    public void addMemoryListener(MemoryListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public synchronized Status getMemoryStatus()
    {
        return myCurrentStatus;
    }

    @Override
    public Status getVMStatus()
    {
        final long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final double ratio = (double)usedMemory / maxMemory;
        return getStatusForRatio(ratio);
    }

    @Override
    public void removeMemoryListener(MemoryListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Handles a memory monitor update.
     *
     * @param usageString the usage string.
     */
    private void handleMemoryMonitored(String usageString)
    {
        final long timenow = System.currentTimeMillis();
        final long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        final long maxMemory = Runtime.getRuntime().maxMemory();
        final double ratio = (double)usedMemory / maxMemory;

        final Status oldStatus;
        final Status newStatus;
        synchronized (this)
        {
            oldStatus = myCurrentStatus;
            if (myCurrentStatus.containsRatio(ratio))
            {
                // Current status is appropriate.

                newStatus = oldStatus;
                myPendingStatus = null;
            }
            else
            {
                final Status statusForRatio = getStatusForRatio(ratio);
                if (statusForRatio.compareTo(oldStatus) < 0)
                {
                    // Memory usage has gone down, so check the pending status.
                    if (myPendingStatus == null)
                    {
                        // No pending status has been set, so set it.
                        newStatus = oldStatus;
                        myPendingStatus = statusForRatio;
                        myPendingStatusTimeMillis = timenow;
                    }
                    else
                    {
                        if (statusForRatio.compareTo(myPendingStatus) > 0)
                        {
                            /* The status for the current memory is higher than
                             * the pending status, so change the pending status,
                             * but leave the time the same. */
                            myPendingStatus = statusForRatio;
                        }

                        if (timenow - myPendingStatusTimeMillis >= STATUS_BOILDOWN_MILLIS)
                        {
                            /* The memory has been low long enough to change to
                             * the pending status. */
                            newStatus = myPendingStatus;
                            myPendingStatus = null;
                        }
                        else
                        {
                            // Still waiting for boil-down.
                            newStatus = oldStatus;
                        }
                    }
                }
                else
                {
                    // Memory is going up; change the status immediately.
                    newStatus = statusForRatio;
                    myPendingStatus = null;
                }
            }
            myCurrentStatus = newStatus;
        }
        if (!Utilities.sameInstance(newStatus, oldStatus))
        {
            myLastStatusChangeTimeMillis = System.currentTimeMillis();

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Memory ratio is now " + ratio);
            }
            LOGGER.info("Memory status changed from " + oldStatus + " to " + newStatus);
            myChangeSupport.notifyListeners(new ChangeSupport.Callback<MemoryManager.MemoryListener>()
            {
                @Override
                public void notify(MemoryListener listener)
                {
                    listener.handleMemoryStatusChange(oldStatus, newStatus);
                }
            });

            if (newStatus.compareTo(oldStatus) > 0)
            {
                System.gc();
            }
        }
        else if (Status.CRITICAL.equals(newStatus))
        {
            System.gc();
        }
        else if (Status.WARNING.equals(newStatus) && System.currentTimeMillis() - myLastStatusChangeTimeMillis > GC_DELAY)
        {
            myLastStatusChangeTimeMillis = Long.MAX_VALUE;
            System.gc();
        }
    }
}
