package io.opensphere.core.util;

import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.WeakReference;

/**
 * {@link ThreePhaseChangeSupport} that uses weak references for its listeners.
 *
 * @param <S> the state type of the listener.
 * @param <T> the listener type used by this support.
 */
public class WeakThreePhaseChangeSupport<S, T extends ThreePhaseChangeListener<S>> extends ThreePhaseChangeSupport<S, T>
{
    @Override
    protected Reference<T> createReference(T listener)
    {
        return new WeakReference<T>(listener);
    }
}
