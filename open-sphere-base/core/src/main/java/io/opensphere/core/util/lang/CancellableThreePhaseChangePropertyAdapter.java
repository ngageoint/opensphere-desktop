package io.opensphere.core.util.lang;

/**
 * A listener for changes to the property that can also reject those changes or
 * delay the changes. This listener also supports being cancelled.
 *
 * @param <S> The type of the property.
 */
public abstract class CancellableThreePhaseChangePropertyAdapter<S>
implements CancellableThreePhaseProperty.CancellableThreePhasePropertyListener<S, CancellableThreePhaseProperty<S>>
{
    @Override
    public void commit(CancellableThreePhaseProperty<S> property, S state, PhasedTaskCanceller canceller)
    {
    }

    @Override
    public boolean preCommit(CancellableThreePhaseProperty<S> property, S pendingState, PhasedTaskCanceller canceller)
    {
        return true;
    }

    @Override
    public boolean prepare(CancellableThreePhaseProperty<S> property, S pendingState, PhasedTaskCanceller canceller)
    {
        return true;
    }
}
