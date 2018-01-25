package io.opensphere.core.util.lang;

import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.ThreadSafe;

import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.core.util.ReferenceService;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.ThreePhaseChangeListener;
import io.opensphere.core.util.ThreePhaseProperty;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.WeakReference;

/**
 * A property that supports notifying listeners during a change to its value,
 * and allowing the listeners to reject or delay the change. The change can also
 * be cancelled, in which case the listeners will subsequently be cancelled.
 *
 * @param <S> The type of the property.
 * @see ThreePhaseChangeListener
 * @see PhasedTaskCanceller
 */
@ThreadSafe
public class CancellableThreePhaseProperty<S>
{
    /** The canceller. */
    private final AtomicReference<TaskCanceller> myCanceller = new AtomicReference<>();

    /** The wrapped property. */
    private final ThreePhaseProperty<S> myProperty;

    /**
     * Constructor.
     *
     * @param initialValue The initial value.
     */
    public CancellableThreePhaseProperty(S initialValue)
    {
        myProperty = new ThreePhaseProperty<>(initialValue);
    }

    /**
     * Adds the listener.
     *
     * @param listener The listener.
     */
    public void addListener(CancellableThreePhasePropertyListener<S, CancellableThreePhaseProperty<S>> listener)
    {
        myProperty.addListener(new ListenerAdapter<S, CancellableThreePhaseProperty<S>>(this, listener));
    }

    /**
     * Cancel any pending operation.
     */
    public void cancel()
    {
        TaskCanceller canceller = getTaskCanceller();
        if (canceller != null)
        {
            canceller.cancel();
        }
    }

    /**
     * Get a service that handles adding and removing a listener. When
     * {@link Service#open()} is called, the listener will be added to this
     * change support. When {@link Service#close()} is called, the listener will
     * be removed. The service holds a strong reference to the listener, but no
     * reference is held to the service.
     *
     * @param listener The listener.
     * @return The service.
     */
    public ReferenceService<ThreePhaseChangeListener<S>> getListenerService(
            CancellableThreePhasePropertyListener<S, CancellableThreePhaseProperty<S>> listener)
    {
        return myProperty.getListenerService(new ListenerAdapter<S, CancellableThreePhaseProperty<S>>(this, listener));
    }

    /**
     * Get the task canceller if there is one. The task canceller will be
     *
     * @return The canceller. {@code null} when there is no pending value
     *         change.
     */
    public TaskCanceller getTaskCanceller()
    {
        return myCanceller.get();
    }

    /**
     * Get the value.
     *
     * @return The value.
     */
    public S getValue()
    {
        return myProperty.getValue();
    }

    /**
     * Removes the listener.
     *
     * @param listener The listener.
     */
    public void removeListener(CancellableThreePhasePropertyListener<S, ? extends CancellableThreePhaseProperty<S>> listener)
    {
        myProperty.removeListener(
            l -> l instanceof ListenerAdapter && Utilities.sameInstance(((ListenerAdapter<?, ?>)l).myListener, listener));
    }

    /**
     * Set the value of the property. This may timeout or a listener may refuse
     * the new value, or another thread may cancel the change.
     *
     * @param value The new value.
     * @param perPhaseTimeoutMillis Wait timeout in milliseconds. Each phase
     *            gets its own timeout, so the actual time to complete
     *            processing may be up to three times the timeout.
     * @param failOnTimeout When {@code true}, the update will be aborted when
     *            the timeout is reached. When {@code false}, once the timeout
     *            is reached, processing will continue without waiting for
     *            listeners until the commit is performed.
     * @return {@code true} if the value was changed successfully.
     * @throws PropertyChangeException If there is a problem attempting the
     *             state change.
     * @throws InterruptedException If the thread is interrupted.
     */
    @SuppressWarnings("PMD.AvoidRethrowingException")
    public boolean setValue(S value, long perPhaseTimeoutMillis, boolean failOnTimeout)
        throws PropertyChangeException, InterruptedException
    {
        TaskCanceller canceller = new TaskCanceller();
        TaskCanceller existing = myCanceller.getAndSet(canceller);
        try
        {
            if (existing != null)
            {
                existing.cancel();
            }
            return canceller.wrap(
                    (Callable<Boolean>)() -> Boolean.valueOf(myProperty.setValue(value, perPhaseTimeoutMillis, failOnTimeout)))
                    .call().booleanValue();
        }
        catch (RuntimeException | Error | PropertyChangeException | InterruptedException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ImpossibleException(e);
        }
        finally
        {
            myCanceller.compareAndSet(canceller, null);
        }
    }

    @Override
    public String toString()
    {
        return StringUtilities.concat(getClass().getSimpleName(), " [", myProperty.getValue(), "]");
    }

    /**
     * Accessor for the wrapped property.
     *
     * @return The property.
     */
    protected final ThreePhaseProperty<S> getProperty()
    {
        return myProperty;
    }

    /**
     * A listener for changes to the property that can also reject those changes
     * or delay the changes. This listener also supports being cancelled.
     *
     * @param <S> The type of the property.
     * @param <T> The type of the property value.
     */
    public interface CancellableThreePhasePropertyListener<S, T extends CancellableThreePhaseProperty<S>>
    {
        /**
         * Commit the pending state. A listener cannot reject a commit. The
         * thread this is called on should already be registered with the
         * canceller.
         *
         * @param property The property for the state.
         * @param state The state which is being committed.
         * @param canceller A canceller that should be used to wrap any
         *            subordinate tasks associated with this state change. The
         *            state change will not be completed until the wrapped tasks
         *            complete. The commit phase cannot be cancelled.
         */
        void commit(T property, S state, PhasedTaskCanceller canceller);

        /**
         * Allow listeners to handle an pre-commit details. Any listener which
         * returns true from this method should guarantee that it will be able
         * to commit. The thread this is called on should already be registered
         * with the canceller.
         *
         * @param property The property for the state.
         * @param pendingState The state which will be committed.
         * @param canceller A canceller that should be used to wrap any
         *            subordinate tasks associated with this state change. The
         *            state change will not be completed until the wrapped tasks
         *            complete, and the wrapped tasks will be eligible to be
         *            interrupted if the state change is cancelled.
         * @return true when the listener accepts the pre-commit request.
         * @throws PropertyChangeException If there is a problem attempting the
         *             state change.
         * @throws InterruptedException If the thread is interrupted.
         */
        boolean preCommit(T property, S pendingState, PhasedTaskCanceller canceller)
            throws PropertyChangeException, InterruptedException;

        /**
         * Request that the listener count down the latch when it is prepared to
         * switch to the pending state. The thread this is called on should
         * already be registered with the canceller.
         *
         * @param property The property for the state.
         * @param pendingState The state which will be committed.
         * @param canceller A canceller that should be used to wrap any
         *            subordinate tasks associated with this state change. The
         *            state change will not be completed until the wrapped tasks
         *            complete, and the wrapped tasks will be eligible to be
         *            interrupted if the state change is cancelled.
         * @return true when the listener accepts the prepare request.
         * @throws PropertyChangeException If there is a problem attempting the
         *             state change.
         * @throws InterruptedException If the thread is interrupted.
         */
        boolean prepare(T property, S pendingState, PhasedTaskCanceller canceller)
            throws PropertyChangeException, InterruptedException;
    }

    /**
     * Adapter that translates from a.
     *
     * @param <S> The type of the property value.
     * @param <T> The type of the property.
     *            {@link CancellableThreePhaseProperty.CancellableThreePhasePropertyListener}
     *            to a {@link ThreePhaseChangeListener}.
     */
    protected static final class ListenerAdapter<S, T extends CancellableThreePhaseProperty<S>>
            implements ThreePhaseChangeListener<S>
    {
        /** The wrapped listener. */
        private final Reference<CancellableThreePhasePropertyListener<S, T>> myListener;

        /** The property. */
        private final T myProperty;

        /**
         * Constructor.
         *
         * @param property The property.
         * @param listener The listener.
         */
        public ListenerAdapter(T property, CancellableThreePhasePropertyListener<S, T> listener)
        {
            myProperty = Utilities.checkNull(property, "property");
            myListener = new WeakReference<>(Utilities.checkNull(listener, "listener"));
        }

        @Override
        public void commit(S state, Phaser phaser)
        {
            CancellableThreePhasePropertyListener<S, T> listener = getListener();
            if (listener != null)
            {
                listener.commit(myProperty, state, new PhasedTaskCanceller(null, phaser));
            }
        }

        /**
         * Dereference the listener.
         *
         * @return The listener.
         */
        public CancellableThreePhasePropertyListener<S, T> getListener()
        {
            CancellableThreePhasePropertyListener<S, T> listener = myListener.get();
            if (listener == null)
            {
                myProperty.getProperty().removeListener(this);
            }
            return listener;
        }

        @Override
        public boolean preCommit(S pendingState, Phaser phaser) throws PropertyChangeException, InterruptedException
        {
            CancellableThreePhasePropertyListener<S, T> listener = getListener();
            return listener == null || listener.preCommit(myProperty, pendingState,
                    new PhasedTaskCanceller(myProperty.getTaskCanceller(), phaser));
        }

        @Override
        public boolean prepare(S pendingState, Phaser phaser) throws PropertyChangeException, InterruptedException
        {
            CancellableThreePhasePropertyListener<S, T> listener = getListener();
            return listener == null
                    || listener.prepare(myProperty, pendingState, new PhasedTaskCanceller(myProperty.getTaskCanceller(), phaser));
        }
    }
}
