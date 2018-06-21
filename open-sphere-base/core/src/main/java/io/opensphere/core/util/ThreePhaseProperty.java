package io.opensphere.core.util;

import java.util.Arrays;
import java.util.function.Predicate;

import net.jcip.annotations.ThreadSafe;

import io.opensphere.core.util.ref.Reference;

/**
 * An object property that supports having three phase change listeners.
 *
 * @param <S> the state type of the listener.
 *
 * @see ThreePhaseChangeSupport
 */
@ThreadSafe
public class ThreePhaseProperty<S>
{
    /** The change support. */
    private final ThreePhaseChangeSupport<S, ThreePhaseChangeListener<S>> myChangeSupport = new StrongThreePhaseChangeSupport<>();

    /** The value of the property. */
    private volatile S myValue;

    /**
     * Constructor.
     *
     * @param initialValue The initial value.
     */
    public ThreePhaseProperty(S initialValue)
    {
        myValue = initialValue;
    }

    /**
     * Adds the listener.
     *
     * @param listener The listener.
     */
    public void addListener(ThreePhaseChangeListener<S> listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Gets the listeners of this property.
     *
     * @return The listeners.
     */
    public Reference<ThreePhaseChangeListener<S>>[] getListeners()
    {
        return myChangeSupport.getListeners();
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
    public ReferenceService<ThreePhaseChangeListener<S>> getListenerService(ThreePhaseChangeListener<S> listener)
    {
        return myChangeSupport.getListenerService(listener);
    }

    /**
     * Get the value.
     *
     * @return The value.
     */
    public S getValue()
    {
        return myValue;
    }

    /**
     * Removes the listener that satisfies a predicate.
     *
     * @param predicate The predicate.
     */
    public synchronized void removeListener(Predicate<? super ThreePhaseChangeListener<?>> predicate)
    {
        Arrays.stream(myChangeSupport.getListeners()).map(r -> r.get()).filter(l -> l != null && predicate.test(l))
                .forEach(l -> removeListener(l));
    }

    /**
     * Removes the listener.
     *
     * @param listener The listener.
     */
    public void removeListener(ThreePhaseChangeListener<S> listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Set the value of the property. This may timeout or a listener may refuse
     * the new value.
     *
     * @param value The new value.
     * @param perPhaseTimeoutMillis Wait timeout in milliseconds. Each phase
     *            gets its own timeout, so the actual time to complete
     *            processing may be up to three times the timeout.
     * @param failOnTimeout When {@code true}, the update will be aborted when
     *            the timeout is reached. When {@code false}, once the timeout
     *            is reached, processing will continue without waiting for
     *            listeners until the commit is performed.
     *
     * @return {@code true} if the value was changed successfully.
     * @throws PropertyChangeException If there is a problem attempting the
     *             state change.
     * @throws InterruptedException If the thread is interrupted.
     */
    public boolean setValue(S value, long perPhaseTimeoutMillis, boolean failOnTimeout)
        throws PropertyChangeException, InterruptedException
    {
        synchronized (this)
        {
            if (myChangeSupport.updateState(value, perPhaseTimeoutMillis, failOnTimeout))
            {
                myValue = value;
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    @Override
    public String toString()
    {
        return new StringBuilder(32).append(getClass().getSimpleName()).append(" [").append(myValue).append(']').toString();
    }
}
