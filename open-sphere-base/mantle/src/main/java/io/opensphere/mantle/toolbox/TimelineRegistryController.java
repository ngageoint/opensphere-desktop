package io.opensphere.mantle.toolbox;

import java.util.Collection;
import java.util.List;

import gnu.trove.TLongCollection;
import gnu.trove.iterator.TLongIterator;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.timeline.TimelineDatum;
import io.opensphere.core.timeline.TimelineRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsRemovedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataTypeRemovedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.event.consolidated.ConsolidatedDataElementVisibilityChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;

/**
 * Listens for mantle events in order to notify the timeline registry.
 */
class TimelineRegistryController extends EventListenerService
{
    /** The data element cache. */
    private final DataElementCache myDataElementCache;

    /** The data type controller. */
    private final DataTypeController myDataTypeController;

    /** The timeline registry. */
    private final TimelineRegistry myTimelineRegistry;

    /**
     * Constructor.
     *
     * @param eventManager The event manager
     * @param dataElementCache The data element cache
     * @param dataTypeController The data type controller
     * @param timelineRegistry The timeline registry
     */
    public TimelineRegistryController(EventManager eventManager, DataElementCache dataElementCache,
            DataTypeController dataTypeController, TimelineRegistry timelineRegistry)
    {
        super(eventManager);
        myDataElementCache = dataElementCache;
        myDataTypeController = dataTypeController;
        myTimelineRegistry = timelineRegistry;
        bindEvent(AbstractDataTypeControllerEvent.class, this::handleDataTypeControllerEvent);
        bindEvent(DataTypeInfoColorChangeEvent.class, this::handleColorChange);
        bindEvent(DataTypeVisibilityChangeEvent.class, this::handleVisibilityChange);
        bindEvent(DataTypeInfoLoadsToChangeEvent.class, this::handleLoadsToChange);
        bindEvent(ConsolidatedDataElementVisibilityChangeEvent.class, this::handleDataElementVisibilityChange);
        bindEvent(ActiveDataGroupsChangedEvent.class, this::handleActiveDataGroupsChanged);
    }

    /**
     * Handles a AbstractDataTypeControllerEvent.
     *
     * @param event the event
     */
    private void handleDataTypeControllerEvent(AbstractDataTypeControllerEvent event)
    {
        if (event instanceof DataTypeAddedEvent)
        {
            handleDataTypeAdded((DataTypeAddedEvent)event);
        }
        else if (event instanceof DataTypeRemovedEvent)
        {
            handleDataTypeRemoved((DataTypeRemovedEvent)event);
        }
        else if (event instanceof DataElementsAddedEvent)
        {
            handleDataElementsAdded((DataElementsAddedEvent)event);
        }
        else if (event instanceof DataElementsRemovedEvent)
        {
            handleDataElementsRemoved((DataElementsRemovedEvent)event);
        }
    }

    /**
     * Handles a data type being added.
     *
     * @param event the event
     */
    private void handleDataTypeAdded(DataTypeAddedEvent event)
    {
        DataTypeInfo dataType = event.getDataType();
        if (handleDataTypeIgnoringLoadsTo(dataType))
        {
            myTimelineRegistry.addLayer(dataType.getOrderKey(), dataType.getDisplayName(),
                    dataType.getBasicVisualizationInfo().getTypeColor(), dataType.isVisible());
        }
    }

    /**
     * Handles a data type being removed.
     *
     * @param event the event
     */
    private void handleDataTypeRemoved(DataTypeRemovedEvent event)
    {
        DataTypeInfo dataType = event.getDataType();
        if (dataType.getOrderKey() != null)
        {
            myTimelineRegistry.removeLayer(dataType.getOrderKey());
        }
    }

    /**
     * Handles data elements being added.
     *
     * @param event the event
     */
    private void handleDataElementsAdded(DataElementsAddedEvent event)
    {
        DataTypeInfo dataType = event.getType();
        if (handleDataType(dataType))
        {
            RangedLongSet elementIds = event.getAddedDataElementIds();
            List<TimelineDatum> data = createTimelineDataList(elementIds);
            myTimelineRegistry.addData(dataType.getOrderKey(), data);
        }
    }

    /**
     * Handles data elements being removed.
     *
     * @param event the event
     */
    private void handleDataElementsRemoved(DataElementsRemovedEvent event)
    {
        DataTypeInfo dataType = event.getType();
        if (handleDataType(dataType))
        {
            /* Since the event's ids may be gone from the cache by now, ignore
             * them and re-set all the spans to what's currently in the
             * cache. */
            RangedLongSet elementIds = myDataElementCache.getElementIdsForTypeAsRangedLongSet(dataType);
            List<TimelineDatum> data = createTimelineDataList(elementIds);
            myTimelineRegistry.setData(dataType.getOrderKey(), data);
        }
    }

    /**
     * Handles data element visibility changes.
     *
     * @param event the event
     */
    private void handleDataElementVisibilityChange(ConsolidatedDataElementVisibilityChangeEvent event)
    {
        for (String dataTypeKey : event.getDataTypeKeys())
        {
            DataTypeInfo dataType = myDataTypeController.getDataTypeInfoForType(dataTypeKey);

            {
                Collection<Long> elementIds = toLongCollection(event.getVisibleIdSet());
                List<TimelineDatum> data = createTimelineDataList(elementIds);
                myTimelineRegistry.addData(dataType.getOrderKey(), data);
            }

            {
                Collection<Long> elementIds = toLongCollection(event.getInvisibleIdSet());
                myTimelineRegistry.removeData(dataType.getOrderKey(), elementIds);
            }
        }
    }

    /**
     * Handles a color change.
     *
     * @param event the event
     */
    private void handleColorChange(DataTypeInfoColorChangeEvent event)
    {
        DataTypeInfo dataType = event.getDataTypeInfo();
        if (handleDataType(dataType))
        {
            myTimelineRegistry.setColor(dataType.getOrderKey(), event.getColor());
        }
    }

    /**
     * Handles a visibility change.
     *
     * @param event the event
     */
    private void handleVisibilityChange(DataTypeVisibilityChangeEvent event)
    {
        DataTypeInfo dataType = event.getDataTypeInfo();
        if (handleDataType(dataType))
        {
            myTimelineRegistry.setVisible(dataType.getOrderKey(), event.isVisible());
        }
    }

    /**
     * Handles a loads to change.
     *
     * @param event the event
     */
    private void handleLoadsToChange(DataTypeInfoLoadsToChangeEvent event)
    {
        DataTypeInfo dataType = event.getDataTypeInfo();
        if (handleDataTypeIgnoringLoadsTo(dataType) && myTimelineRegistry.hasKey(dataType.getOrderKey()))
        {
            myTimelineRegistry.setVisible(dataType.getOrderKey(), event.getLoadsTo().isTimelineEnabled());
            if (event.getLoadsTo().isTimelineEnabled())
            {
                RangedLongSet elementIds = myDataElementCache.getElementIdsForTypeAsRangedLongSet(dataType);
                List<TimelineDatum> data = createTimelineDataList(elementIds);
                myTimelineRegistry.addData(dataType.getOrderKey(), data);
            }
        }
    }

    /**
     * Handles an active data groups change.
     *
     * @param event the event
     */
    private void handleActiveDataGroupsChanged(ActiveDataGroupsChangedEvent event)
    {
        setVisibility(event.getDeactivatedGroups(), false);
        setVisibility(event.getActivatedGroups(), true);
    }

    /**
     * Sets the visibility of the handled types in the given groups.
     *
     * @param groups the data groups
     * @param isVisible whether the types should be visible
     */
    private void setVisibility(Collection<? extends DataGroupInfo> groups, boolean isVisible)
    {
        groups.stream().flatMap(group -> group.findMembers(TimelineRegistryController::handleDataType, true, false).stream())
                .forEach(type -> myTimelineRegistry.setVisible(type.getOrderKey(), isVisible && type.isVisible()));
    }

    /**
     * Creates a list of TimelineDatum objects.
     *
     * @param elementIds the element IDs
     * @return the timeline data list
     */
    private List<TimelineDatum> createTimelineDataList(Collection<Long> elementIds)
    {
        List<TimeSpan> spans = myDataElementCache.getTimeSpans(elementIds);

        List<TimelineDatum> data = New.list(elementIds.size());
        int index = 0;
        for (Long id : elementIds)
        {
            TimeSpan timeSpan = spans.get(index++);
            if (timeSpan != null)
            {
                data.add(new TimelineDatum(id.longValue(), timeSpan));
            }
        }
        return data;
    }

    /**
     * Determines if this data type should be handled.
     *
     * @param dataType the data type
     * @return whether this data type should be handled
     */
    private static boolean handleDataType(DataTypeInfo dataType)
    {
        return handleDataTypeIgnoringLoadsTo(dataType) && dataType.getBasicVisualizationInfo().getLoadsTo().isTimelineEnabled();
    }

    /**
     * Determines if this data type should be handled (ignoring loads to).
     *
     * @param dataType the data type
     * @return whether this data type should be handled (ignoring loads to)
     */
    private static boolean handleDataTypeIgnoringLoadsTo(DataTypeInfo dataType)
    {
        MapVisualizationInfo mapVisualizationInfo = dataType.getMapVisualizationInfo();
        return mapVisualizationInfo != null && mapVisualizationInfo.getVisualizationType().isMapDataElementType()
                && dataType.getOrderKey() != null;
    }

    /**
     * Converts a TLongCollection to a Collection of Long.
     *
     * @param in the input collection
     * @return the converted collection
     */
    private static Collection<Long> toLongCollection(TLongCollection in)
    {
        Collection<Long> out = New.list(in.size());
        for (TLongIterator iter = in.iterator(); iter.hasNext();)
        {
            out.add(Long.valueOf(iter.next()));
        }
        return out;
    }
}
