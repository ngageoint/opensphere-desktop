package io.opensphere.mantle.data;

import java.util.List;
import java.util.concurrent.Phaser;

import org.apache.log4j.Logger;

import io.opensphere.core.util.PropertyChangeException;
import io.opensphere.core.util.ReferenceService;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.ThreePhaseChangeListener;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.CancellableThreePhaseProperty;
import io.opensphere.core.util.ref.Reference;

/** Property for the activation state of a data group. */
public class DataGroupActivationProperty extends CancellableThreePhaseProperty<ActivationState>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataGroupActivationProperty.class);

    /** The data group associated with this property. */
    private final DataGroupInfo myDataGroup;

    /**
     * Internal activation listener that ensures the state transitions are
     * legal.
     */
    private final ThreePhaseChangeListener<ActivationState> myListener = new ThreePhaseChangeListener<>()
    {
        @Override
        public void commit(ActivationState state, Phaser phaser)
        {
        }

        @Override
        public boolean preCommit(ActivationState pendingState, Phaser phaser)
        {
            return true;
        }

        @Override
        public boolean prepare(ActivationState pendingState, Phaser phaser)
        {
            return pendingState.isLegalTransitionFrom(getValue());
        }
    };

    /**
     * Constructor.
     *
     * @param dataGroup The data group associated with this property.
     */
    public DataGroupActivationProperty(DataGroupInfo dataGroup)
    {
        super(ActivationState.INACTIVE);
        myDataGroup = Utilities.checkNull(dataGroup, "dataGroup");
        getProperty().addListener(myListener);
    }

    /**
     * Adds the listener.
     *
     * @param listener The listener.
     */
    public void addListener(ActivationListener listener)
    {
        getProperty().addListener(new ListenerAdapter<>(this, listener));
    }

    /**
     * Get the data group.
     *
     * @return The data group.
     */
    public DataGroupInfo getDataGroup()
    {
        return myDataGroup;
    }

    /**
     * Gets the listeners that have been added via addListener calls.
     *
     * @return The added listeners.
     */
    public List<ActivationListener> getListeners()
    {
        List<ActivationListener> listeners = New.list();
        for (Reference<ThreePhaseChangeListener<ActivationState>> listener : getProperty().getListeners())
        {
            ThreePhaseChangeListener<ActivationState> adapter = listener.get();
            if (adapter instanceof ListenerAdapter)
            {
                @SuppressWarnings("unchecked")
                //@formatter:off
                CancellableThreePhasePropertyListener<ActivationState, DataGroupActivationProperty> reallyThisIsTheRealListener
                    = ((ListenerAdapter<ActivationState, DataGroupActivationProperty>)adapter).getListener();
                //@formatter:on
                if (reallyThisIsTheRealListener instanceof ActivationListener)
                {
                    listeners.add((ActivationListener)reallyThisIsTheRealListener);
                }
            }
        }

        return listeners;
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
    public ReferenceService<ThreePhaseChangeListener<ActivationState>> getListenerService(ActivationListener listener)
    {
        return getProperty().getListenerService(new ListenerAdapter<>(this, listener));
    }

    /**
     * Get if the data group is activating/deactivating.
     *
     * @return {@code true} if the group is activating or deactivating.
     */
    public boolean isActivatingOrDeactivating()
    {
        ActivationState value = getValue();
        return value == ActivationState.ACTIVATING || value == ActivationState.DEACTIVATING;
    }

    /**
     * Get if the data group is active.
     *
     * @return {@code true} if the group is active.
     */
    public boolean isActive()
    {
        return getValue() == ActivationState.ACTIVE;
    }

    /**
     * Get if the data group is activating/active.
     *
     * @return {@code true} if the group is activating or active.
     */
    public boolean isActiveOrActivating()
    {
        ActivationState value = getValue();
        return value == ActivationState.ACTIVATING || value == ActivationState.ACTIVE;
    }

    /**
     * Get if the data group is deactivating/inactive.
     *
     * @return {@code true} if the group is activating or deactivating.
     */
    public boolean isInactiveOrDeactivation()
    {
        ActivationState value = getValue();
        return value == ActivationState.DEACTIVATING || value == ActivationState.INACTIVE;
    }

    /**
     * Sets the data group active or inactive, with no timeout. Errors are
     * logged.
     *
     * @param active If the data group should be active or not.
     * @return true, if successful
     */
    public boolean setActive(boolean active)
    {
        try
        {
            return setActive(active, Integer.MAX_VALUE);
        }
        catch (PropertyChangeException | InterruptedException e)
        {
            LOGGER.error("Failed to set group [" + myDataGroup.getDisplayNameWithPostfixTopParentName() + "] to active [" + active
                    + "]: " + e, e);
            return false;
        }
    }

    /**
     * Sets the data group active.
     *
     * @param active If the data group should be active or not.
     * @param timeoutMillis The timeout in milliseconds.
     * @return true, if successful
     * @throws PropertyChangeException If there is a problem changing the state
     *             of the group.
     * @throws InterruptedException If the thread is interrupted.
     */
    public boolean setActive(boolean active, int timeoutMillis) throws PropertyChangeException, InterruptedException
    {
        try
        {
            if (active)
            {
                if (isActiveOrActivating() || setValue(ActivationState.ACTIVATING, timeoutMillis, true)
                        && setValue(ActivationState.ACTIVE, timeoutMillis, true))
                {
                    return true;
                }
                else
                {
                    setValue(ActivationState.ERROR, 0L, false);
                    return false;
                }
            }
            else
            {
                return isInactiveOrDeactivation() || setValue(ActivationState.DEACTIVATING, timeoutMillis, true)
                        && setValue(ActivationState.INACTIVE, timeoutMillis, true);
            }
        }
        catch (InterruptedException e)
        {
            getProperty().setValue(ActivationState.INACTIVE, 0L, false);
            throw e;
        }
        catch (PropertyChangeException | Error | RuntimeException e)
        {
            getProperty().setValue(ActivationState.ERROR, 0L, false);
            throw e;
        }
    }
}
