package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.event.DataTypeInfoFocusEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TypeFocusEvent.FocusType;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;

/**
 * Handles firing hover events for data types associated with query regions when
 * they're picked.
 */
@ThreadSafe
public class QueryRegionPickHandler implements Service
{
    /** The control context, used to get the pick notifications. */
    private final ControlContext myControlContext;

    /** The data group controller, for looking up data types. */
    private final DataGroupController myDataGroupController;

    /** The event manager, for publishing hover events. */
    private final EventManager myEventManager;

    /** The data type for the last query region picked. */
    @GuardedBy("this")
    private final Set<DataTypeInfo> myPickedDataTypes = New.set();

    /** Listener for pick events. */
    private final PickListener myPickListener = this::handlePick;

    /** Supplier for the active query regions. */
    private final Supplier<Collection<? extends QueryRegion>> myQueryRegionSupplier;

    /**
     * Constructor.
     *
     * @param controlContext The control context, used to get the pick
     *            notifications.
     * @param eventManager The event manager, for publishing hover events.
     * @param dataGroupController The data group controller, for looking up data
     *            types.
     * @param queryRegionSupplier The query region supplier.
     */
    public QueryRegionPickHandler(ControlContext controlContext, EventManager eventManager,
            DataGroupController dataGroupController, Supplier<Collection<? extends QueryRegion>> queryRegionSupplier)
    {
        myControlContext = controlContext;
        myEventManager = eventManager;
        myDataGroupController = dataGroupController;
        myQueryRegionSupplier = queryRegionSupplier;
    }

    @Override
    public void close()
    {
        myControlContext.removePickListener(myPickListener);
    }

    @Override
    public void open()
    {
        myControlContext.addPickListener(myPickListener);
    }

    /**
     * Handle a query region picked.
     *
     * @param event The event.
     */
    private synchronized void handlePick(PickEvent event)
    {
        // @formatter:off
        Set<DataTypeInfo> types = myQueryRegionSupplier.get().stream()
            .filter((Predicate<QueryRegion>)r -> r.getGeometries().contains(event.getPickedGeometry()))
            .map(r -> r.getTypeKeys())
            .flatMap(k -> k.stream())
            .map(k -> myDataGroupController.findMemberById(k))
            .filter(t -> t != null)
            .collect(Collectors.toSet());
        // @formatter:on

        Set<DataTypeInfo> lost = New.set(myPickedDataTypes);
        lost.removeAll(types);
        if (!lost.isEmpty())
        {
            myEventManager.publishEvent(new DataTypeInfoFocusEvent(lost, this, FocusType.HOVER_LOST));
        }

        Set<DataTypeInfo> gained = New.set(types);
        types.removeAll(myPickedDataTypes);
        if (!gained.isEmpty())
        {
            myEventManager.publishEvent(new DataTypeInfoFocusEvent(gained, this, FocusType.HOVER_GAINED));
        }

        myPickedDataTypes.clear();
        myPickedDataTypes.addAll(types);
    }
}
