package io.opensphere.mantle.transformer.impl;

import java.util.Collection;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.TroveUtilities;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.plugin.selection.SelectionCommandProcessor;
import io.opensphere.mantle.transformer.TransformerGeomRegistryUpdateTaskActivity;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class DefaultMapDataElementTransformer.
 */
public class StyleMapDataElementTransformer extends AbstractMapDataElementTransformer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StyleMapDataElementTransformer.class);

    /** The my active data groups changed listener. */
    @SuppressWarnings("PMD.SingularField")
    private final EventListener<ActiveDataGroupsChangedEvent> myActiveDataGroupsChangedListener;

    /** The Geometry processor. */
    private final StyleTransformerGeometryProcessor myGeometryProcessor;

    /** The Geom reg update activity. */
    private final TransformerGeomRegistryUpdateTaskActivity myGeomRegUpdateActivity;

    /** The my publish changes to geometry registry. */
    private boolean myPublishChangesToGeometryRegistry = true;

    /**
     * Instantiates a new default map data element transformer.
     *
     * @param aToolbox the a toolbox
     * @param dti the dti
     * @param source the source
     * @param category the category
     * @param activity the activity
     */
    public StyleMapDataElementTransformer(Toolbox aToolbox, DataTypeInfo dti, String source, String category,
            TransformerGeomRegistryUpdateTaskActivity activity)
    {
        super(aToolbox, dti, source, category);
        myGeomRegUpdateActivity = activity;
        myGeometryProcessor = new StyleTransformerGeometryProcessor(this, aToolbox, dti, getExecutorService());

        if (dti.getBasicVisualizationInfo() != null)
        {
            myActiveDataGroupsChangedListener = createActiveDataGroupsChangedListener();
            myPublishChangesToGeometryRegistry = MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController()
                    .isTypeActive(dti);
            aToolbox.getEventManager().subscribe(ActiveDataGroupsChangedEvent.class, myActiveDataGroupsChangedListener);
        }
        else
        {
            myActiveDataGroupsChangedListener = null;
        }
    }

    @Override
    public void addMapDataElements(final Collection<? extends MapDataElement> dataElements, final long[] ids)
    {
        myGeometryProcessor.addMapDataElements(dataElements, ids);
    }

    @Override
    public EventListener<AbstractConsolidatedDataElementChangeEvent> createDataElementChangeListener()
    {
        return event ->
        {
            if (event.getDataTypeKeys().contains(getDataType().getTypeKey()))
            {
                myGeometryProcessor.handleConsolidatedDataElementChangeEvent(event);
            }
        };
    }

    @Override
    public EventListener<AbstractDataTypeInfoChangeEvent> createDataTypeChangeListener()
    {
        return event ->
        {
            if (Utilities.sameInstance(getDataType(), event.getDataTypeInfo()))
            {
                myGeometryProcessor.handleDataTypeInfoChangeEvent(event);
            }
        };
    }

    @Override
    public SelectionCommandProcessor createPurgeCommandProcessor()
    {
        return (bounds, cmd) ->
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Purge Occured: " + cmd);
            }
            myGeometryProcessor.purgeOccurred(bounds, null, cmd);
        };
    }

    @Override
    public SelectionCommandProcessor createSelectionCommandProcessor()
    {
        return (bounds, cmd) ->
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Selecton Occured: " + cmd);
            }
            myGeometryProcessor.selectionOccurred(bounds, cmd);
        };
    }

    @Override
    public long getDataModelIdFromGeometryId(long geomId)
    {
        return myGeometryProcessor.hasGeometryId(geomId) ? myGeometryProcessor.getDataModelIdFromGeometryId(geomId) : -1;
    }

    @Override
    public TLongLongHashMap getElementIdsForGeometryIds(long[] geometryIds)
    {
        return myGeometryProcessor.getElementIdsForGeometryIds(geometryIds);
    }

    @Override
    public TLongSet getIdSet()
    {
        return TroveUtilities.unmodifiableSet(new TLongHashSet(myGeometryProcessor.getIdSet()));
    }

    /**
     * Gets the update source.
     *
     * @return the update source
     */
    public Object getUpdateSource()
    {
        return this;
    }

    /**
     * Gets the update task activity.
     *
     * @return the update task activity
     */
    public TransformerGeomRegistryUpdateTaskActivity getUpdateTaskActivity()
    {
        return myGeomRegUpdateActivity;
    }

    @Override
    public boolean hasGeometryForDataModelId(long id)
    {
        return myGeometryProcessor.hasGeometryDataModelId(id);
    }

    @Override
    public boolean hasGeometryId(long geomId)
    {
        return myGeometryProcessor.hasGeometryId(geomId);
    }

    /**
     * Checks if is publish updates to geometry registry.
     *
     * @return true, if is publish updates to geometry registry
     */
    public boolean isPublishUpdatesToGeometryRegistry()
    {
        return myPublishChangesToGeometryRegistry;
    }

    @Override
    public void removeMapDataElements(long[] ids)
    {
        myGeometryProcessor.removeMapDataElements(ids);
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        myGeometryProcessor.shutdown();
        getToolbox().getEventManager().unsubscribe(ActiveDataGroupsChangedEvent.class, myActiveDataGroupsChangedListener);
    }

    /**
     * Gets the geometryProcessor.
     *
     * @return the geometryProcessor
     */
    public StyleTransformerGeometryProcessor getGeometryProcessor()
    {
        return myGeometryProcessor;
    }

    /**
     * Creates the active data groups changed listener.
     *
     * @return the event listener
     */
    private EventListener<ActiveDataGroupsChangedEvent> createActiveDataGroupsChangedListener()
    {
        return event ->
        {
            boolean active = MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController()
                    .isTypeActive(getDataType());
            if (active != myPublishChangesToGeometryRegistry)
            {
                myGeometryProcessor.publishUnpublishGeometries(active);
                myPublishChangesToGeometryRegistry = active;
            }
        };
    }
}
