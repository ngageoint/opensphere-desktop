package io.opensphere.mantle.controller.impl;

import java.util.Set;
import java.util.concurrent.Executor;

import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Listens for {@link ActiveDataGroupsChangedEvent} and automatically adds and
 * removes {@link DataTypeInfo}s to and from the {@link DataTypeController}.
 *
 */
public class DataTypeAutoAdder implements EventListener<ActiveDataGroupsChangedEvent>
{
    /**
     * Used to listen for data types being activated.
     */
    private final EventManager myEventManager;

    /**
     * The data type controller to add data types to.
     */
    private final DataTypeController myTypeController;

    /** The event handler executor. */
    private Executor myExecutor = ThreadUtilities.getCpuExecutorService();

    /**
     * Constructs a new data type auto updater.
     *
     * @param eventManager The system event manager used to listen for data type
     *            being activated.
     * @param typeController The type controller to add and remove data types to
     *            and from.
     */
    public DataTypeAutoAdder(EventManager eventManager, DataTypeController typeController)
    {
        myTypeController = typeController;
        myEventManager = eventManager;
        myEventManager.subscribe(ActiveDataGroupsChangedEvent.class, this);
    }

    /**
     * Stops listening for data type activation.
     */
    public void close()
    {
        myEventManager.unsubscribe(ActiveDataGroupsChangedEvent.class, this);
    }

    @Override
    public void notify(ActiveDataGroupsChangedEvent event)
    {
        myExecutor.execute(() -> manageDataTypes(event));
    }

    /**
     * Sets the executor (for unit test).
     *
     * @param executor the executor
     */
    void setExecutor(Executor executor)
    {
        myExecutor = executor;
    }

    /**
     * Adds and removes {@link DataTypeInfo}s to and from the
     * {@link DataTypeController}.
     *
     * @param event the event
     */
    private void manageDataTypes(ActiveDataGroupsChangedEvent event)
    {
        Set<DataGroupInfo> actives = event.getActivatedGroups();
        for (DataGroupInfo active : actives)
        {
            if (active.hasMembers(false))
            {
                for (DataTypeInfo dataTypeInfo : active.getMembers(false))
                {
                    if (dataTypeInfo.getMetaDataInfo() != null && !dataTypeInfo.getMetaDataInfo().getKeyNames().isEmpty()
                            && !myTypeController.hasDataTypeInfoForTypeKey(dataTypeInfo.getTypeKey()))
                    {
                        myTypeController.addDataType(this.getClass().getName(), this.getClass().getName(), dataTypeInfo, this);
                    }
                }
            }
        }

        Set<DataGroupInfo> deactives = event.getDeactivatedGroups();
        for (DataGroupInfo deactive : deactives)
        {
            if (deactive.hasMembers(false))
            {
                for (DataTypeInfo dataTypeInfo : deactive.getMembers(false))
                {
                    if (myTypeController.hasDataTypeInfoForTypeKey(dataTypeInfo.getTypeKey()))
                    {
                        myTypeController.removeDataType(dataTypeInfo, this);
                    }
                }
            }
        }
    }
}
