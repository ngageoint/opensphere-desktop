package io.opensphere.mantle.data;

import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.core.util.lang.ThreadControl;

/** Listener for changes to an activation state property. */
public abstract class AbstractActivationListener implements ActivationListener
{
    @Override
    public void commit(DataGroupActivationProperty property, ActivationState state, PhasedTaskCanceller canceller)
    {
        if (state == ActivationState.INACTIVE || state == ActivationState.ERROR)
        {
            DataGroupInfo dgi = property.getDataGroup();
            handleDeactivating(dgi);
            handleCommit(false, dgi, canceller);
        }
        else if (state == ActivationState.ACTIVE)
        {
            handleCommit(true, property.getDataGroup(), canceller);
        }
    }

    /**
     * Hook for handling activation.
     *
     * @param dgi The data group.
     * @param canceller A canceller that should be used to wrap any subordinate
     *            tasks associated with this state change. The state change will
     *            not be completed until the wrapped tasks complete, and the
     *            wrapped tasks will be eligible to be interrupted if the state
     *            change is cancelled.
     * @return {@code true} if successful.
     * @throws DataGroupActivationException If the activation fails.
     * @throws InterruptedException If the thread is interrupted.
     */
    public boolean handleActivating(DataGroupInfo dgi, PhasedTaskCanceller canceller)
        throws DataGroupActivationException, InterruptedException
    {
        return true;
    }

    /**
     * Convenience hook for handling an active or inactive state commit.
     *
     * @param active {@code true} if active.
     * @param dgi The data group.
     * @param canceller A canceller that should be used to wrap any subordinate
     *            tasks associated with this state change. The state change will
     *            not be completed until the wrapped tasks complete.
     */
    public void handleCommit(boolean active, DataGroupInfo dgi, PhasedTaskCanceller canceller)
    {
    }

    /**
     * Hook for handling deactivation.
     *
     * @param dgi The data group.
     */
    public void handleDeactivating(DataGroupInfo dgi)
    {
    }

    @Override
    public boolean preCommit(DataGroupActivationProperty property, ActivationState pendingState, PhasedTaskCanceller canceller)
        throws PropertyChangeException, InterruptedException
    {
        if (pendingState == ActivationState.ACTIVE)
        {
            ThreadControl.check();
            DataGroupInfo dgi = property.getDataGroup();
            try
            {
                return handleActivating(dgi, canceller);
            }
            catch (DataGroupActivationException e)
            {
                throw new PropertyChangeException(e);
            }
        }
        return true;
    }

    @Override
    public boolean prepare(DataGroupActivationProperty property, ActivationState pendingState, PhasedTaskCanceller canceller)
    {
        return true;
    }
}
