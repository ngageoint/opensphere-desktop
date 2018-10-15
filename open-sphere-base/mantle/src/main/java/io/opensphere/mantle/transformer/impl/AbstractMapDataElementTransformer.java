package io.opensphere.mantle.transformer.impl;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gnu.trove.set.TLongSet;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.element.event.DataElementDoubleClickedEvent;
import io.opensphere.mantle.data.element.event.DataElementHighlightChangeEvent;
import io.opensphere.mantle.data.element.event.consolidated.AbstractConsolidatedDataElementChangeEvent;
import io.opensphere.mantle.plugin.selection.SelectionCommandFactory;
import io.opensphere.mantle.plugin.selection.SelectionCommandProcessor;
import io.opensphere.mantle.transformer.MapDataElementTransformer;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * This class transforms MapDataElement geometry support information into
 * Geometry for the Geometry Registry.
 */
public abstract class AbstractMapDataElementTransformer implements MapDataElementTransformer
{
//    /** The Constant EMPTY_GEOM_SET. */
//    private static final Set<Geometry> EMPTY_GEOM_SET = Collections.<Geometry>emptySet();

    /** The Constant SwitchCurrentDataTypeOnMouseOver. */
    public static final String SwitchCurrentDataTypeOnMouseOverPreference = "SwitchCurrentDataTypeOnMouseOver";

//    /** Logger reference. */
//    private static final Logger LOGGER = Logger.getLogger(AbstractMapDataElementTransformer.class);

    /** The my consolidated data element event listener. */
    private final EventListener<AbstractConsolidatedDataElementChangeEvent> myConsolidatedDataElementEventListener;

    /** The data type change listener. */
    private final EventListener<AbstractDataTypeInfoChangeEvent> myDataTypeChangeListener;

    /** The my data type info. */
    private final DataTypeInfo myDataTypeInfo;

    /** The ExecutorService. */
    private final ExecutorService myExecutor;

    /** The last highlighted data element. */
    private long myLastHighlightedDataElement = -1;

    /** True if my action geometry is currently picked. */
    private PickEvent myLastPickEvent;

    /** The event adapter. */
    private final DiscreteEventListener myMouseClickListener;

    /** The pick listener. */
    private final PickListener myPickListener;

    /** The purge command processor. */
    private final SelectionCommandProcessor myPurgeCommandProcessor;

    /** The selection command processor. */
    private final SelectionCommandProcessor mySelectionCommandProcessor;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new data type map data element transformer.
     *
     * @param aToolbox the a toolbox
     * @param dti the dti
     * @param source the source
     * @param category the category
     */
    public AbstractMapDataElementTransformer(Toolbox aToolbox, DataTypeInfo dti, String source, String category)
    {
        myExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("DataTypeMapDataElementTransformer:Worker", 3, 4));
        myToolbox = aToolbox;
        myDataTypeInfo = dti;
        myPickListener = createPickListener();
        myMouseClickListener = createMouseClickListener();
        mySelectionCommandProcessor = createSelectionCommandProcessor();
        myPurgeCommandProcessor = createPurgeCommandProcessor();

        myDataTypeChangeListener = createDataTypeChangeListener();
        myToolbox.getEventManager().subscribe(AbstractDataTypeInfoChangeEvent.class, myDataTypeChangeListener);

        myConsolidatedDataElementEventListener = createDataElementChangeListener();
        myToolbox.getEventManager().subscribe(AbstractConsolidatedDataElementChangeEvent.class,
                myConsolidatedDataElementEventListener);

        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT).addPickListener(myPickListener);
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT).addListener(myMouseClickListener,
                new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED));

        MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler()
                .registerSelectionCommandProcessor(SelectionCommandFactory.SELECT, mySelectionCommandProcessor);
        MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler()
                .registerSelectionCommandProcessor(SelectionCommandFactory.DESELECT, mySelectionCommandProcessor);
        MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler()
                .registerSelectionCommandProcessor(SelectionCommandFactory.SELECT_EXCLUSIVE, mySelectionCommandProcessor);
        MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler()
                .registerSelectionCommandProcessor(SelectionCommandFactory.REMOVE_ALL, myPurgeCommandProcessor);
    }

    @Override
    public abstract void addMapDataElements(Collection<? extends MapDataElement> dataElements, long[] ids);

    /**
     * Creates the data element change listener.
     *
     * @return the event listener
     */
    public abstract EventListener<AbstractConsolidatedDataElementChangeEvent> createDataElementChangeListener();

    /**
     * Creates the data type change listener.
     *
     * @return the event listener
     */
    public abstract EventListener<AbstractDataTypeInfoChangeEvent> createDataTypeChangeListener();

    /**
     * Creates the selection command processor.
     *
     * @return the selection command processor
     */
    public abstract SelectionCommandProcessor createPurgeCommandProcessor();

    /**
     * Creates the selection command processor.
     *
     * @return the selection command processor
     */
    public abstract SelectionCommandProcessor createSelectionCommandProcessor();

    @Override
    public DataTypeInfo getDataType()
    {
        return myDataTypeInfo;
    }

    /**
     * Gets the id set.
     *
     * @return the id set
     */
    public abstract TLongSet getIdSet();

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    @Override
    public abstract void removeMapDataElements(long[] ids);

    @Override
    public void shutdown()
    {
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT).removePickListener(myPickListener);
        myToolbox.getControlRegistry().getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT)
                .removeListener(myMouseClickListener);
        myToolbox.getEventManager().unsubscribe(AbstractDataTypeInfoChangeEvent.class, myDataTypeChangeListener);
        myToolbox.getEventManager().unsubscribe(AbstractConsolidatedDataElementChangeEvent.class,
                myConsolidatedDataElementEventListener);

        MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler()
                .unregisterSelectionCommandProcessor(mySelectionCommandProcessor);

        MantleToolboxUtils.getMantleToolbox(myToolbox).getSelectionHandler()
                .unregisterSelectionCommandProcessor(myPurgeCommandProcessor);
    }

    /**
     * Gets the executor service.
     *
     * @return the executor service
     */
    protected ExecutorService getExecutorService()
    {
        return myExecutor;
    }

//    /**
//     * Publish geometries.
//     *
//     * @param geometrySet the geometry set
//     */
//    protected void publishGeometries(Set<Geometry> geometrySet)
//    {
//        LOGGER.info("Sent " + geometrySet.size() + " new geometries to the Geometry Registry at " + System.currentTimeMillis());
//        myToolbox.getGeometryRegistry().receiveObjects(this, geometrySet, EMPTY_GEOM_SET);
//    }
//
//    /**
//     * Unpublish geometries.
//     *
//     * @param geometrySet the geometry set
//     */
//    protected void unpublishGeometries(Set<Geometry> geometrySet)
//    {
//        myToolbox.getGeometryRegistry().receiveObjects(this, EMPTY_GEOM_SET, geometrySet);
//    }

    /**
     * Creates the mouse click listener.
     *
     * @return the discrete event listener
     */
    private DiscreteEventListener createMouseClickListener()
    {
        DiscreteEventAdapter eventAdapter = new DiscreteEventAdapter("DataElement", "DataElementClickListener",
                "Monitors for clicks on Data Elements")
        {
            @Override
            public void eventOccurred(InputEvent event)
            {
                if (event instanceof MouseEvent)
                {
                    final MouseEvent mouseEvent = (MouseEvent)event;
                    if (myLastPickEvent != null && mouseEvent.getID() == MouseEvent.MOUSE_CLICKED
                            && mouseEvent.getButton() == MouseEvent.BUTTON1)
                    {
                        final long geomId = myLastPickEvent.getPickedGeometry().getDataModelId();
                        final long cacheId = getDataModelIdFromGeometryId(geomId);
                        myExecutor.execute(() ->
                        {
                            MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController()
                                    .setCurrentDataType(myDataTypeInfo, AbstractMapDataElementTransformer.this);

                            VisualizationState vs = MantleToolboxUtils.getDataElementLookupUtils(myToolbox)
                                    .getVisualizationState(cacheId);
                            MantleToolboxUtils.getDataElementUpdateUtils(myToolbox).setDataElementSelected(!vs.isSelected(),
                                    cacheId, vs, myDataTypeInfo.getTypeKey(), AbstractMapDataElementTransformer.this);

                            if (mouseEvent.getClickCount() == 2)
                            {
                                myToolbox.getEventManager().publishEvent(new DataElementDoubleClickedEvent(cacheId,
                                        myDataTypeInfo.getTypeKey(), AbstractMapDataElementTransformer.this));
                            }
                        });
                    }
                }
            }
        };
        eventAdapter.setReassignable(false);
        return eventAdapter;
    }

    /**
     * Creates the pick listener.
     *
     * @return the pick listener
     */
    private PickListener createPickListener()
    {
        return evt ->
        {
            final Geometry picked = evt.getPickedGeometry();
            final long geomId = picked == null ? 0L : picked.getDataModelId();
            myExecutor.execute(() ->
            {
                long cacheId = getDataModelIdFromGeometryId(geomId);
                if (picked != null && hasGeometryForDataModelId(cacheId))
                {
                    myLastPickEvent = evt;
                    if (myLastHighlightedDataElement != -1 && myLastHighlightedDataElement != cacheId)
                    {
                        myToolbox.getEventManager()
                                .publishEvent(new DataElementHighlightChangeEvent(myLastHighlightedDataElement,
                                        myDataTypeInfo.getTypeKey(), false, AbstractMapDataElementTransformer.this));
                    }

                    if (myToolbox.getPreferencesRegistry().getPreferences(MapDataElementTransformer.class)
                            .getBoolean(SwitchCurrentDataTypeOnMouseOverPreference, false))
                    {
                        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataTypeController()
                                .setCurrentDataType(myDataTypeInfo, AbstractMapDataElementTransformer.this);
                    }
                    myLastHighlightedDataElement = cacheId;
                    myToolbox.getEventManager()
                            .publishEvent(new DataElementHighlightChangeEvent(myLastHighlightedDataElement,
                                    myDataTypeInfo.getTypeKey(), true, AbstractMapDataElementTransformer.this));
                }
                else
                {
                    myLastPickEvent = null;
                    if (myLastHighlightedDataElement != -1)
                    {
                        myToolbox.getEventManager()
                                .publishEvent(new DataElementHighlightChangeEvent(myLastHighlightedDataElement,
                                        myDataTypeInfo.getTypeKey(), false, AbstractMapDataElementTransformer.this));
                    }
                    myLastHighlightedDataElement = -1;
                }
            });
        };
    }
}
