package io.opensphere.mantle.data.impl;

import java.util.function.Consumer;

import io.opensphere.core.util.lang.CancellableThreePhaseChangePropertyAdapter;
import io.opensphere.core.util.lang.CancellableThreePhaseProperty;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.mantle.data.ActivationState;

/**
 * Simplified listener that just runs a given {@link Runnable} when a group is
 * activated and another {@link Runnable} when a group is deactivated.
 */
public class SimpleDataGroupActivationListener extends CancellableThreePhaseChangePropertyAdapter<ActivationState>
{
    /** The activator. */
    private final Consumer<CancellableThreePhaseProperty<ActivationState>> myActivator;

    /** The de-activator. */
    private final Consumer<CancellableThreePhaseProperty<ActivationState>> myDeactivator;

    /**
     * Constructor.
     *
     * @param activator The activator.
     * @param deactivator The deactivator.
     */
    public SimpleDataGroupActivationListener(Consumer<CancellableThreePhaseProperty<ActivationState>> activator,
            Consumer<CancellableThreePhaseProperty<ActivationState>> deactivator)
    {
        myActivator = activator;
        myDeactivator = deactivator;
    }

    @Override
    public void commit(CancellableThreePhaseProperty<ActivationState> property, ActivationState state,
            PhasedTaskCanceller canceller)
    {
        switch (state)
        {
            case ACTIVE:
                myActivator.accept(property);
                break;
            case INACTIVE:
                myDeactivator.accept(property);
                break;
            default:
                break;
        }
    }
}
