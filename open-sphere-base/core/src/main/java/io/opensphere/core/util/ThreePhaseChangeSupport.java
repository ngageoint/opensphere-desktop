package io.opensphere.core.util;

import java.awt.EventQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.WeakReference;
import net.jcip.annotations.GuardedBy;

/**
 * Support for notifying interested parties of generic changes. This
 * implementation requests permission from interested parties before providing
 * notification. A latch is provided to each listener. Once each listener has
 * counted down the latch to signify readiness to receive the notification, then
 * the notification may be sent. If one or more {@link PhasedChangeArbitrator}
 * are added to this support, phased changes will only be done when an
 * arbitrator requires it, otherwise only the COMMIT phase will be executed.
 * When no arbitrators are present, all phases will be executed.
 *
 * @param <S> the state type of the listener.
 * @param <T> the listener type used by this support.
 */
public abstract class ThreePhaseChangeSupport<S, T extends ThreePhaseChangeListener<S>> extends AbstractChangeSupport<T>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ThreePhaseChangeSupport.class);

    /**
     * When present the arbitrators dictate whether to go straight to the commit
     * phase.
     */
    @SuppressWarnings("unchecked")
    @GuardedBy("this")
    private volatile WeakReference<PhasedChangeArbitrator>[] myArbitrators = (WeakReference<PhasedChangeArbitrator>[])new WeakReference<?>[0];

    /** Lock to prevent attempting multiple state changes concurrently. */
    private final Lock myUpdateLock = new ReentrantLock();

    /**
     * Add an arbitrator for phased changes.
     *
     * @param arbitrator The arbitrator.
     */
    public synchronized void addPhasedChangeArbitrator(PhasedChangeArbitrator arbitrator)
    {
        checkArbitratorReferences();

        @SuppressWarnings("unchecked")
        WeakReference<PhasedChangeArbitrator>[] arr = (WeakReference<PhasedChangeArbitrator>[])new WeakReference<?>[myArbitrators.length
                + 1];
        System.arraycopy(myArbitrators, 0, arr, 0, myArbitrators.length);
        arr[arr.length - 1] = new WeakReference<>(arbitrator);
        myArbitrators = arr;
    }

    /**
     * Update the state to the pending state. This will commit immediately with
     * no timeouts and the PREPARE and PRE_COMMIT stages will be skipped.
     *
     * @param pendingState The state to which the source will be changing.
     * @param timeout The time to wait for the phase listeners to complete, in
     *            milliseconds. If the current thread is the EDT thread, then no
     *            timeout will occur.
     * @return return {@code true} unless a fail condition has occurred.
     * @throws PropertyChangeException If there is a problem attempting the
     *             state change.
     * @throws InterruptedException If the thread is interrupted.
     */
    public boolean commit(S pendingState, long timeout) throws PropertyChangeException, InterruptedException
    {
        long theRealTimeout = timeout;
        if (EventQueue.isDispatchThread())
        {
            theRealTimeout = 0;
        }

        return processPhase(Phase.COMMIT, pendingState, theRealTimeout, false);
    }

    /**
     * Remove an arbitrator for phased changes.
     *
     * @param arbitrator The arbitrator.
     * @return {@code true} when the arbitrator was removed.
     */
    public synchronized boolean removePhasedChangeArbitrator(PhasedChangeArbitrator arbitrator)
    {
        checkArbitratorReferences();

        boolean removed = false;
        for (int index = 0; index < myArbitrators.length; ++index)
        {
            if (myArbitrators[index].get() == arbitrator)
            {
                removePhasedChangeArbitrator(index--);
                removed = true;
            }
        }
        return removed;
    }

    /**
     * Update the state to the pending state. This will block until the state
     * has been updated or an error condition has occurred.
     *
     * @param pendingState The state to which the source will be changing.
     * @param perPhaseTimeoutMillis Wait timeout in milliseconds. Each phase
     *            gets its own timeout, so the actual time to complete
     *            processing may be up to three times the timeout.
     * @param failOnTimeout When {@code true}, the update will be aborted when
     *            the timeout is reached. When {@code false}, once the timeout
     *            is reached, processing will continue without waiting for
     *            listeners until the commit is performed.
     *
     * @return {@code true} when all listeners are ready or the timeout has been
     *         reached. If a wait is already in progress or a fail condition
     *         occurs, {@code false} will be returned.
     * @throws PropertyChangeException If there is a problem attempting the
     *             state change.
     * @throws InterruptedException If the thread is interrupted.
     */
    public boolean updateState(S pendingState, long perPhaseTimeoutMillis, boolean failOnTimeout)
        throws PropertyChangeException, InterruptedException
    {
        if (myUpdateLock.tryLock())
        {
            try
            {
                if (requiresPhasedCommit())
                {
                    for (Phase phase : new Phase[] { Phase.PREPARE, Phase.PRE_COMMIT, Phase.COMMIT })
                    {
                        if (!processPhase(phase, pendingState, perPhaseTimeoutMillis, failOnTimeout))
                        {
                            return false;
                        }
                    }
                }
                else
                {
                    commit(pendingState, perPhaseTimeoutMillis);
                }
                return true;
            }
            finally
            {
                myUpdateLock.unlock();
            }
        }

        return false;
    }

    /**
     * Check for and remove any arbitrator whose referent is {@code null}.
     */
    private synchronized void checkArbitratorReferences()
    {
        for (int index = 0; index < myArbitrators.length; ++index)
        {
            if (myArbitrators[index].get() == null)
            {
                removePhasedChangeArbitrator(index--);
            }
        }
    }

    /**
     * Block until either all listeners have pre-committed or the timeout has
     * been reached.
     *
     * @param phase The phase to process.
     * @param pendingState The state to which the source will be changing.
     * @param timeout Wait timeout in milliseconds.
     * @param failOnTimeout When {@code true}, the update will be aborted when
     *            the timeout is reached. When {@code false}, once the timeout
     *            is reached, processing will continue without waiting for
     *            listeners until the commit is performed.
     *
     * @return {@code true} when all listeners are ready or the timeout has been
     *         reached. If a fail condition occurs, {@code false} will be
     *         returned.
     * @throws PropertyChangeException If there is a problem attempting the
     *             state change.
     * @throws InterruptedException If the thread is interrupted.
     */
    private boolean processPhase(Phase phase, S pendingState, long timeout, boolean failOnTimeout)
        throws PropertyChangeException, InterruptedException
    {
        @SuppressWarnings("PMD.PrematureDeclaration")
        long t0 = System.nanoTime();
        Reference<T>[] listeners = getListeners();
        Phaser phaser = new Phaser(1);

        boolean accepted = true;
        for (final Reference<T> listener : listeners)
        {
            T ref = listener.get();
            if (ref != null)
            {
                switch (phase)
                {
                    case PREPARE:
                        accepted = ref.prepare(pendingState, phaser);
                        break;
                    case PRE_COMMIT:
                        accepted = ref.preCommit(pendingState, phaser);
                        break;
                    case COMMIT:
                        ref.commit(pendingState, phaser);
                        break;
                    default:
                        break;
                }
            }

            if (!accepted)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Phase " + phase + " rejected by listener : " + ref);
                }
                return false;
            }
        }

        try
        {
            if (timeout > 0)
            {
                phaser.awaitAdvanceInterruptibly(phaser.arrive(), timeout, TimeUnit.MILLISECONDS);
            }
        }
        catch (TimeoutException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Phase " + phase + " timed out with " + phaser.getUnarrivedParties() + " listeners remaining.");
            }
            if (failOnTimeout)
            {
                return false;
            }
        }

        if (LOGGER.isTraceEnabled())
        {
            long t1 = System.nanoTime();
            LOGGER.trace("Phase " + phase + " completed successfully in " + (t1 - t0) / Constants.NANO_PER_MILLI + " ms");
        }

        return true;
    }

    /**
     * Remove an arbitrator for phased changes.
     *
     * @param index The index of the arbitrator which is to be removed.
     */
    private synchronized void removePhasedChangeArbitrator(int index)
    {
        @SuppressWarnings("unchecked")
        WeakReference<PhasedChangeArbitrator>[] arr = (WeakReference<PhasedChangeArbitrator>[])new WeakReference<?>[myArbitrators.length
                - 1];
        if (arr.length > 0)
        {
            if (index > 0)
            {
                System.arraycopy(myArbitrators, 0, arr, 0, index);
            }
            if (index < arr.length)
            {
                System.arraycopy(myArbitrators, index + 1, arr, index, arr.length - index);
            }
        }
        myArbitrators = arr;
    }

    /**
     * Tell whether phased commits are required.
     *
     * @return {@code true} when phased commits are required.
     */
    private boolean requiresPhasedCommit()
    {
        WeakReference<PhasedChangeArbitrator>[] arbitrators = myArbitrators;

        boolean phasedCommitRequired = false;
        boolean checkReferences = false;
        for (WeakReference<PhasedChangeArbitrator> ref : arbitrators)
        {
            PhasedChangeArbitrator listener = ref.get();
            if (listener == null)
            {
                checkReferences = true;
            }
            else
            {
                if (listener.isPhasedCommitRequired())
                {
                    phasedCommitRequired = true;
                    break;
                }
            }
        }

        if (checkReferences)
        {
            checkArbitratorReferences();
        }

        if (myArbitrators.length == 0)
        {
            phasedCommitRequired = true;
        }

        return phasedCommitRequired;
    }

    /** The phases of this support. */
    private enum Phase
    {
        /** Commit the state change. */
        COMMIT,

        /**
         * Handle any pre-state change processing once all listeners are
         * prepared. This is intended for processing which is expensive and will
         * only be done if the PREPARE phase succeeds or for processing which
         * depends on other listeners having completed the PREPARE phase.
         */
        PRE_COMMIT,

        /** Prepare for state change. */
        PREPARE

        ;
    }
}
