package io.opensphere.merge.ui;

import java.util.function.Consumer;

import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.mantle.data.ActivationListener;
import io.opensphere.mantle.data.ActivationState;
import io.opensphere.mantle.data.DataGroupActivationProperty;

/**
 * ActivationListener that reacts only when a state is commited (i.e., it
 * ignores prepare and preCommit phases). The resident ActivationState Consumer
 * is then invoked with the committed state.
 */
public class ActivationStateListener implements ActivationListener
{
    /** The callback for state changes. */
    protected Consumer<ActivationState> impl;

    /**
     * Construct with the desired callback for state commits.
     *
     * @param ear the actual listener
     */
    public ActivationStateListener(Consumer<ActivationState> ear)
    {
        impl = ear;
    }

    /**
     * Factory method for instances that respond only when a specified target
     * state is committed.
     *
     * @param ear the callback
     * @param target the target state
     * @return an state-specific listener
     */
    public static ActivationStateListener forState(Runnable ear, ActivationState target)
    {
        return new ActivationStateListener(a ->
        {
            if (a == target)
            {
                ear.run();
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.lang.CancellableThreePhaseProperty.CancellableThreePhasePropertyListener#commit(io.opensphere.core.util.lang.CancellableThreePhaseProperty,
     *      java.lang.Object, io.opensphere.core.util.lang.PhasedTaskCanceller)
     */
    @Override
    public void commit(DataGroupActivationProperty p, ActivationState st, PhasedTaskCanceller can)
    {
        if (impl != null)
        {
            impl.accept(st);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.lang.CancellableThreePhaseProperty.CancellableThreePhasePropertyListener#preCommit(io.opensphere.core.util.lang.CancellableThreePhaseProperty,
     *      java.lang.Object, io.opensphere.core.util.lang.PhasedTaskCanceller)
     */
    @Override
    public boolean preCommit(DataGroupActivationProperty p, ActivationState st, PhasedTaskCanceller can)
        throws PropertyChangeException, InterruptedException
    {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.lang.CancellableThreePhaseProperty.CancellableThreePhasePropertyListener#prepare(io.opensphere.core.util.lang.CancellableThreePhaseProperty,
     *      java.lang.Object, io.opensphere.core.util.lang.PhasedTaskCanceller)
     */
    @Override
    public boolean prepare(DataGroupActivationProperty p, ActivationState st, PhasedTaskCanceller can)
        throws PropertyChangeException, InterruptedException
    {
        return true;
    }
}
