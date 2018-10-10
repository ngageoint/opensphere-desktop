package io.opensphere.mantle.data.impl;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.InterruptibleCallable;
import io.opensphere.core.util.lang.TaskCanceller;
import io.opensphere.mantle.data.DataGroupActivationProperty;
import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Helper class that wraps data group activation to provide a consistent message
 * reporting mechanism to the user.
 */
public class DefaultDataGroupActivator implements DataGroupActivator
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultDataGroupActivator.class);

    /** The event manager. */
    private final EventManager myEventManager;

    /**
     * Constructor with no user event support.
     */
    public DefaultDataGroupActivator()
    {
        myEventManager = null;
    }

    /**
     * Constructor.
     *
     * @param eventManager The event manager.
     */
    public DefaultDataGroupActivator(EventManager eventManager)
    {
        myEventManager = Utilities.checkNull(eventManager, "eventManager");
    }

    @Override
    public boolean reactivateGroup(DataGroupInfo group) throws InterruptedException
    {
        Utilities.checkNull(group, "group");
        return setGroupActive(group, false) && setGroupActive(group, true);
    }

    @Override
    public boolean reactivateGroups(Collection<DataGroupInfo> groups) throws InterruptedException
    {
        /* If the thread is interrupted while the group is de/activating, it
         * should be caught within the mapper that's running on the current
         * thread, which will then interrupt the other involved threads and mark
         * the canceller cancelled. Once everything returns, the canceller is
         * checked and throws a new InterruptedException to signal the caller
         * that the thread was interrupted. */
        TaskCanceller canceller = new TaskCanceller();
        Function<? super DataGroupInfo, ? extends Boolean> mapper = new Function<>()
        {
            @Override
            public Boolean apply(DataGroupInfo g)
            {
                try
                {
                    return canceller.wrap((InterruptibleCallable<Boolean>)() -> Boolean
                            .valueOf(setGroupActive(g, false) && setGroupActive(g, true))).call();
                }
                catch (InterruptedException e)
                {
                    canceller.cancel();
                    return Boolean.FALSE;
                }
            }
        };
        @SuppressWarnings("PMD.PrematureDeclaration")
        boolean result = groups.parallelStream().map(mapper).allMatch(v -> v.booleanValue());
        if (canceller.isCancelled())
        {
            throw new InterruptedException();
        }
        return result;
    }

    @Override
    public boolean setGroupActive(DataGroupActivationProperty property, boolean active) throws InterruptedException
    {
        try
        {
            return property.setActive(active, Integer.MAX_VALUE);
        }
        catch (PropertyChangeException e)
        {
            LOGGER.error(
                    "Failed to set group [" + property.getDataGroup().getDisplayName() + "] to active [" + active + "]: " + e, e);
            if (myEventManager != null)
            {
                UserMessageEvent.error(myEventManager, "Failed to activate " + property.getDataGroup().getDisplayName(), false,
                        true);
            }
            return false;
        }
    }

    @Override
    public boolean setGroupActive(DataGroupInfo group, boolean active) throws InterruptedException
    {
        return setGroupActive(group.activationProperty(), active);
    }

    @Override
    public boolean setGroupsActive(Collection<DataGroupInfo> groups, boolean active) throws InterruptedException
    {
        return setGroupsActive(Utilities.checkNull(groups, "groups").parallelStream(), active);
    }

    @Override
    public boolean setGroupsActive(Stream<DataGroupInfo> groupStream, boolean active) throws InterruptedException
    {
        /* If the thread is interrupted while the group is de/activating, it
         * should be caught within the mapper that's running on the current
         * thread, which will then interrupt the other involved threads and mark
         * the canceller cancelled. Once everything returns, the canceller is
         * checked and throws a new InterruptedException to signal the caller
         * that the thread was interrupted. */
        TaskCanceller canceller = new TaskCanceller();
        Function<? super DataGroupInfo, ? extends Boolean> mapper = new Function<>()
        {
            @Override
            public Boolean apply(DataGroupInfo g)
            {
                try
                {
                    return canceller.wrap((InterruptibleCallable<Boolean>)() -> Boolean.valueOf(setGroupActive(g, active)))
                            .call();
                }
                catch (InterruptedException e)
                {
                    canceller.cancel();
                    return Boolean.FALSE;
                }
            }
        };
        @SuppressWarnings("PMD.PrematureDeclaration")
        boolean result = groupStream.map(mapper).allMatch(v -> v.booleanValue());
        if (canceller.isCancelled())
        {
            throw new InterruptedException();
        }
        return result;
    }
}
