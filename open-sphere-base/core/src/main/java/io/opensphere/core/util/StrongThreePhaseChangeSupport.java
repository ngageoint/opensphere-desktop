package io.opensphere.core.util;

import io.opensphere.core.util.ref.Reference;
import io.opensphere.core.util.ref.StrongReference;

/**
 * {@link ThreePhaseChangeSupport} that uses strong references for its
 * listeners.
 *
 * @param <S> the state type of the listener.
 * @param <T> the listener type used by this support.
 */
public class StrongThreePhaseChangeSupport<S, T extends ThreePhaseChangeListener<S>> extends ThreePhaseChangeSupport<S, T>
{
    @Override
    protected Reference<T> createReference(T listener)
    {
        return new StrongReference<>(listener);
    }
}
