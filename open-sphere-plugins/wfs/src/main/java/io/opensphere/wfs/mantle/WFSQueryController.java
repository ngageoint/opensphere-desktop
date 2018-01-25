package io.opensphere.wfs.mantle;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.animationhelper.PairGovernorManager;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.wfs.layer.WFSDataType;

/**
 * Controls when to query and what to query when WFS feature layers are active.
 */
public class WFSQueryController
        implements QueryRegionListener, EventListener<ActiveDataGroupsChangedEvent>, ListDataListener<TimeSpan>
{
    /**
     * Used to listen for layer deactivations.
     */
    private final EventManager myEventManager;

    /**
     * Manages all the {@link WFSGovernor}.
     */
    private final PairGovernorManager<DataTypeInfo, QueryRegion> myGovernorManager;

    /**
     * Responsible for notifying us of new query regions.
     */
    private final QueryRegionManager myQueryManager;

    /**
     * Notifies us when load times have changed.
     */
    private final TimeManager myTimeManager;

    /**
     * Used to get WFS layers.
     */
    private final DataTypeController myTypeController;

    /**
     * Constructs a new query controller.
     *
     * @param toolbox The system toolbox.
     */
    public WFSQueryController(Toolbox toolbox)
    {
        MantleToolbox mantle = MantleToolboxUtils.getMantleToolbox(toolbox);
        myQueryManager = mantle.getQueryRegionManager();
        myEventManager = toolbox.getEventManager();
        myTimeManager = toolbox.getTimeManager();
        myGovernorManager = new PairGovernorManager<>(p -> new WFSGovernor(mantle, toolbox.getDataRegistry(), p.getFirstObject(), p.getSecondObject()));
        myTypeController = mantle.getDataTypeController();
        myQueryManager.addQueryRegionListener(this);
        myEventManager.subscribe(ActiveDataGroupsChangedEvent.class, this);
        myTimeManager.getLoadTimeSpans().addChangeListener(this);
    }

    @Override
    public void allQueriesRemoved(boolean animationPlanCancelled)
    {
        myGovernorManager.clearData(new Pair<>(null, null));
    }

    /**
     * Stops listening for time and query region changes. Removes all wfs data
     * from the globe.
     */
    public void close()
    {
        myQueryManager.removeQueryRegionListener(this);
        myEventManager.unsubscribe(ActiveDataGroupsChangedEvent.class, this);
        myTimeManager.getLoadTimeSpans().removeChangeListener(this);
        myGovernorManager.clearData(new Pair<>(null, null));
    }

    @Override
    public void elementsAdded(ListDataEvent<TimeSpan> e)
    {
        myGovernorManager.requestData(new Pair<>(null, null), e.getChangedElements());
    }

    @Override
    public void elementsChanged(ListDataEvent<TimeSpan> e)
    {
        List<TimeSpan> removeSpans = New.list();
        int index = 0;
        for (TimeSpan previous : e.getPreviousElements())
        {
            TimeSpan newSpan = e.getChangedElements().get(index);
            if (!previous.overlaps(newSpan))
            {
                removeSpans.add(previous);
            }
            else
            {
                removeSpans.addAll(previous.subtract(newSpan));
            }
            index++;
        }
        if (!removeSpans.isEmpty())
        {
            myGovernorManager.clearData(new Pair<>(null, null), removeSpans);
        }
        myGovernorManager.requestData(new Pair<>(null, null), e.getChangedElements());
    }

    @Override
    public void elementsRemoved(ListDataEvent<TimeSpan> e)
    {
        myGovernorManager.clearData(new Pair<>(null, null), e.getChangedElements());
    }

    @Override
    public void notify(ActiveDataGroupsChangedEvent event)
    {
        for (DataGroupInfo group : event.getDeactivatedGroups())
        {
            for (DataTypeInfo dataType : group.getMembers(false))
            {
                if (dataType instanceof WFSDataType)
                {
                    myGovernorManager.clearData(new Pair<>(dataType, null));
                }
            }
        }
    }

    @Override
    public void queryRegionAdded(QueryRegion region)
    {
        Collection<WFSDataType> wfsLayers = CollectionUtilities.filterDowncast(myTypeController.getDataTypeInfo(),
                WFSDataType.class);
        for (DataTypeInfo wfsLayer : wfsLayers)
        {
            if (wfsLayer.isVisible())
            {
                Collection<? extends TimeSpan> timeSpans = myTimeManager.getLoadTimeSpans();
                if (wfsLayer.getTimeExtents() == null || wfsLayer.getTimeExtents().getExtent().isTimeless())
                {
                    timeSpans = New.list(TimeSpan.TIMELESS);
                }
                myGovernorManager.requestData(new Pair<DataTypeInfo, QueryRegion>(wfsLayer, region), timeSpans);
            }
        }
    }

    @Override
    public void queryRegionRemoved(QueryRegion region)
    {
        myGovernorManager.clearData(new Pair<>(null, region));
    }

    /**
     * Gets the governor manager.
     *
     * @return The governor manager.
     */
    protected PairGovernorManager<DataTypeInfo, QueryRegion> getGovernorManager()
    {
        return myGovernorManager;
    }
}
