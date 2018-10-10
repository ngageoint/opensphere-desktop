package io.opensphere.mantle.data.geom.style.tilecontroller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.event.impl.RootDataGroupAddedEvent;
import io.opensphere.mantle.controller.event.impl.RootDataGroupStructureChangeEvent;
import io.opensphere.mantle.data.AbstractDataGroupInfoChangeEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.event.DataGroupInfoChildAddedEvent;
import io.opensphere.mantle.data.event.DataGroupInfoMemberAddedEvent;
import io.opensphere.mantle.data.geom.style.TileVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;
import io.opensphere.mantle.data.geom.style.dialog.VisualizationStyleDataGroupTreeFilter;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class TileStyleTransformController.
 */
@SuppressWarnings("PMD.GodClass")
public class TileStyleTransformController implements VisualizationStyleRegistryChangeListener
{
    /** The DGI change listener. */
    private final EventListener<AbstractDataGroupInfoChangeEvent> myDGIChangeListener;

//
//    /**
//     * Checks if is applicable data group.
//     *
//     * @param dgi the dgi
//     * @return true, if is applicable data group
//     */
//    private static boolean isApplicableDataGroup(DataGroupInfo dgi)
//    {
//        boolean applicable = false;
//        if (dgi != null && dgi.hasMembers(false))
//        {
//            for (DataTypeInfo member : dgi.getMembers(false))
//            {
//                if (isApplicableDataType(member))
//                {
//                    applicable = true;
//                    break;
//                }
//            }
//        }
//        return applicable;
//    }

    /** The ExecutorService. */
    private final ExecutorService myExecutor;

    /** The Tile style monitor. */
    private final TileStyleMonitor myTileStyleMonitor;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Gets the data type info.
     *
     * @param mtb the {@link MantleToolbox}
     * @param dtiKey the dti key
     * @return the data type info
     */
    private static DataTypeInfo getDataTypeInfo(MantleToolbox mtb, String dtiKey)
    {
        DataTypeInfo dti = mtb.getDataTypeController().getDataTypeInfoForType(dtiKey);
        if (dti == null)
        {
            dti = mtb.getDataGroupController().findMemberById(dtiKey);
        }
        return dti;
    }

    /**
     * Checks if is applicable data type.
     *
     * @param dti the dti
     * @return true, if is applicable data type
     */
    private static boolean isApplicableDataType(DataTypeInfo dti)
    {
        return dti != null && dti.getMapVisualizationInfo() != null && dti.getBasicVisualizationInfo() != null
                && dti.getMapVisualizationInfo().getVisualizationType() == MapVisualizationType.IMAGE_TILE
                && dti.getMapVisualizationInfo().getTileRenderProperties() != null
                && dti.getMapVisualizationInfo().usesVisualizationStyles();
    }

    /**
     * Instantiates a new tile style transform controller.
     *
     * @param tb the {@link Toolbox}
     */
    public TileStyleTransformController(Toolbox tb)
    {
        myExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("TileStyleTransformController", 3, 4));
        myToolbox = tb;
        myTileStyleMonitor = new TileStyleMonitor(tb, myExecutor);
        myDGIChangeListener = createDGIChangeListener();
        MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleRegistry().addVisualizationStyleRegistryChangeListener(this);
        myToolbox.getEventManager().subscribe(AbstractDataGroupInfoChangeEvent.class, myDGIChangeListener);
    }

    @Override
    public void defaultStyleChanged(Class<? extends VisualizationSupport> mgsClass,
            Class<? extends VisualizationStyle> styleClass, Object source)
    {
        if (TileVisualizationSupport.class.isAssignableFrom(mgsClass))
        {
            myExecutor.execute(new UpdateDefaultTileStyles(myToolbox, myTileStyleMonitor));
        }
    }

    @Override
    public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
    {
        if (TileVisualizationSupport.class.isAssignableFrom(evt.getMGSClass()) && evt.getDTIKey() != null)
        {
            myExecutor.execute(new UpdateSpecificTileStyle(myToolbox, myTileStyleMonitor, evt));
        }
    }

    @Override
    public void visualizationStyleInstalled(Class<? extends VisualizationStyle> styleClass, Object source)
    {
        // Do nothing.
    }

    /**
     * Creates the dgi change listener.
     *
     * @return the event listener
     */
    private EventListener<AbstractDataGroupInfoChangeEvent> createDGIChangeListener()
    {
        EventListener<AbstractDataGroupInfoChangeEvent> listener = new EventListener<>()
        {
            @Override
            public void notify(AbstractDataGroupInfoChangeEvent event)
            {
                handleDGIChangeEvent(event);
            }
        };
        return listener;
    }

    /**
     * Handle dgi change event.
     *
     * @param event the event
     */
    private void handleDGIChangeEvent(AbstractDataGroupInfoChangeEvent event)
    {
        if (event instanceof RootDataGroupStructureChangeEvent)
        {
            RootDataGroupStructureChangeEvent evt = (RootDataGroupStructureChangeEvent)event;
            if (evt.getOriginEvent() instanceof DataGroupInfoChildAddedEvent)
            {
                DataGroupInfoChildAddedEvent childAddedEvent = (DataGroupInfoChildAddedEvent)evt.getOriginEvent();
                if (childAddedEvent.getGroup().hasMembers(false))
                {
                    for (DataTypeInfo dti : childAddedEvent.getGroup().getMembers(false))
                    {
                        if (isApplicableDataType(dti))
                        {
                            myExecutor.execute(new UpdateDataTypeTileStyle(myToolbox, myTileStyleMonitor, dti));
                        }
                    }
                }
            }
            else if (evt.getOriginEvent() instanceof DataGroupInfoMemberAddedEvent)
            {
                DataGroupInfoMemberAddedEvent addedEvent = (DataGroupInfoMemberAddedEvent)evt.getOriginEvent();
                DataTypeInfo addedType = addedEvent.getAdded();
                if (isApplicableDataType(addedType))
                {
                    myExecutor.execute(new UpdateDataTypeTileStyle(myToolbox, myTileStyleMonitor, addedType));
                }
            }
        }
        else if (event instanceof RootDataGroupAddedEvent && event.getGroup().hasMembers(true))
        {
            for (DataTypeInfo dti : event.getGroup().getMembers(true))
            {
                if (isApplicableDataType(dti))
                {
                    myExecutor.execute(new UpdateDataTypeTileStyle(myToolbox, myTileStyleMonitor, dti));
                }
            }
        }
    }

    /**
     * The Class TileStyleMonitor.
     */
    private static class TileStyleMonitor implements VisualizationStyleParameterChangeListener
    {
        /** The Change lock. */
        private final ReentrantReadWriteLock myChangeLock;

        /** The Default style. */
        private TileVisualizationStyle myDefaultStyle;

        /** The DTI key to style map. */
        private final Map<String, TileVisualizationStyle> myDTIKeyToStyleMap;

        /** The Executor. */
        private final ExecutorService myExecutor;

        /** The Toolbox. */
        private final Toolbox myToolbox;

        /**
         * Instantiates a new tile style monitor.
         *
         * @param tb the tb
         * @param executor the executor
         */
        public TileStyleMonitor(Toolbox tb, ExecutorService executor)
        {
            myToolbox = tb;
            myExecutor = executor;
            myChangeLock = new ReentrantReadWriteLock();
            myDTIKeyToStyleMap = New.map();
        }

        /**
         * Default style changed.
         *
         * @param style the style
         */
        public void defaultStyleChanged(TileVisualizationStyle style)
        {
            myChangeLock.writeLock().lock();
            try
            {
                if (myDefaultStyle == null || !Utilities.sameInstance(myDefaultStyle, style))
                {
                    if (myDefaultStyle != null)
                    {
                        myDefaultStyle.removeStyleParameterChangeListener(this);
                    }
                    myDefaultStyle = style;
                    if (myDefaultStyle != null)
                    {
                        myDefaultStyle.addStyleParameterChangeListener(this);
                    }
                }
            }
            finally
            {
                myChangeLock.writeLock().unlock();
            }
        }

        /**
         * Style changed.
         *
         * @param dtiKey the dti key
         * @param newStyle the new style
         */
        public void styleChanged(String dtiKey, TileVisualizationStyle newStyle)
        {
            myChangeLock.writeLock().lock();
            try
            {
                TileVisualizationStyle oldStyle = myDTIKeyToStyleMap.remove(dtiKey);
                if (oldStyle != null)
                {
                    oldStyle.removeStyleParameterChangeListener(this);
                }
                if (!Utilities.sameInstance(myDefaultStyle, newStyle))
                {
                    myDTIKeyToStyleMap.put(dtiKey, newStyle);
                    newStyle.addStyleParameterChangeListener(this);
                }
            }
            finally
            {
                myChangeLock.writeLock().unlock();
            }
        }

        @Override
        public void styleParametersChanged(VisualizationStyleParameterChangeEvent evt)
        {
            String dtiKey = evt.getDTIKey();
            VisualizationStyle style = evt.getStyle();
            if (style instanceof TileVisualizationStyle)
            {
                final TileVisualizationStyle tileStyle = (TileVisualizationStyle)style;
                if (dtiKey != null)
                {
                    final DataTypeInfo dti = getDataTypeInfo(MantleToolboxUtils.getMantleToolbox(myToolbox), dtiKey);
                    if (dti != null)
                    {
                        myExecutor.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (dti.getMapVisualizationInfo() != null
                                        && dti.getMapVisualizationInfo().getTileRenderProperties() != null)
                                {
                                    tileStyle.updateTileRenderProperties(dti.getMapVisualizationInfo().getTileRenderProperties());
                                }
                            }
                        });
                    }
                }
                else
                {
                    myExecutor.execute(new UpdateDefaultTileStyles(myToolbox, this));
                }
            }
        }
    }

    /**
     * The Class UpdateDataTypeTileStyle.
     */
    private static class UpdateDataTypeTileStyle extends UpdateStyleWorker implements Runnable
    {
        /** The Data type info. */
        private final DataTypeInfo myDataTypeInfo;

        /**
         * Instantiates a new update initial tile style.
         *
         * @param tb the {@link Toolbox}
         * @param styleMonitor the style monitor
         * @param dti the {@link DataTypeInfo}
         */
        public UpdateDataTypeTileStyle(Toolbox tb, TileStyleMonitor styleMonitor, DataTypeInfo dti)
        {
            super(tb, styleMonitor);
            myDataTypeInfo = dti;
        }

        @Override
        public void run()
        {
            MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(getToolbox());
            VisualizationStyle style = mtb.getVisualizationStyleRegistry().getStyle(TileVisualizationSupport.class,
                    myDataTypeInfo.getTypeKey(), true);
            if (style instanceof TileVisualizationStyle)
            {
                getTileStyleMonitor().styleChanged(myDataTypeInfo.getTypeKey(), (TileVisualizationStyle)style);
                ((TileVisualizationStyle)style)
                        .updateTileRenderProperties(myDataTypeInfo.getMapVisualizationInfo().getTileRenderProperties());
            }
        }
    }

    /**
     * The Class UpdateDefaultTileStyle.
     */
    private static class UpdateDefaultTileStyles extends UpdateStyleWorker implements Runnable
    {
        /**
         * Instantiates a new update default tile style.
         *
         * @param tb the {@link Toolbox}
         * @param styleMonitor the style monitor
         */
        public UpdateDefaultTileStyles(Toolbox tb, TileStyleMonitor styleMonitor)
        {
            super(tb, styleMonitor);
        }

        @Override
        public void run()
        {
            MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(getToolbox());
            List<DataGroupInfo> dgiList = mtb.getDataGroupController().createGroupList(null,
                    new VisualizationStyleDataGroupTreeFilter(getToolbox()));

            VisualizationStyle defaultStyle = mtb.getVisualizationStyleRegistry().getDefaultStyle(TileVisualizationSupport.class);
            if (defaultStyle instanceof TileVisualizationStyle)
            {
                TileVisualizationStyle defaultTileVisStyle = (TileVisualizationStyle)defaultStyle;
                getTileStyleMonitor().defaultStyleChanged(defaultTileVisStyle);
                for (DataGroupInfo dgi : dgiList)
                {
                    for (DataTypeInfo dti : dgi.getMembers(false))
                    {
                        if (isApplicableDataType(dti))
                        {
                            VisualizationStyle assignedStyle = mtb.getVisualizationStyleRegistry()
                                    .getStyle(TileVisualizationSupport.class, dti.getTypeKey(), false);

                            // Don't change any style that has a specific
                            // assigned style.
                            if (assignedStyle == null)
                            {
                                getTileStyleMonitor().styleChanged(dti.getTypeKey(), defaultTileVisStyle);
                                defaultTileVisStyle
                                        .updateTileRenderProperties(dti.getMapVisualizationInfo().getTileRenderProperties());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The Class UpdateSpecificTileStyle.
     */
    private static class UpdateSpecificTileStyle extends UpdateStyleWorker implements Runnable
    {
        /** The Event. */
        private final VisualizationStyleDatatypeChangeEvent myEvent;

        /**
         * Instantiates a new update specific tile style.
         *
         * @param tb the {@link Toolbox}
         * @param styleMonitor the style monitor
         * @param evt the event
         */
        public UpdateSpecificTileStyle(Toolbox tb, TileStyleMonitor styleMonitor, VisualizationStyleDatatypeChangeEvent evt)
        {
            super(tb, styleMonitor);
            myEvent = evt;
        }

        @Override
        public void run()
        {
            MantleToolbox mtb = MantleToolboxUtils.getMantleToolbox(getToolbox());
            String dtiKey = myEvent.getDTIKey();
            DataTypeInfo dti = getDataTypeInfo(mtb, dtiKey);
            if (dti != null && dti.getMapVisualizationInfo() != null
                    && dti.getMapVisualizationInfo().getTileRenderProperties() != null)
            {
                VisualizationStyle newStyle = myEvent.getNewStyle();
                if (newStyle instanceof TileVisualizationStyle)
                {
                    TileVisualizationStyle newTileStyle = (TileVisualizationStyle)newStyle;
                    getTileStyleMonitor().styleChanged(dti.getTypeKey(), newTileStyle);
                    newTileStyle.updateTileRenderProperties(dti.getMapVisualizationInfo().getTileRenderProperties());
                }
            }
        }
    }

    /**
     * The Class UpdateStyleWorker.
     */
    private static class UpdateStyleWorker
    {
        /** The Tile style monitor. */
        private final TileStyleMonitor myTileStyleMonitor;

        /** The Toolbox. */
        private final Toolbox myToolbox;

        /**
         * Update style worker.
         *
         * @param tb the {@link Toolbox}
         * @param styleMonitor the style monitor
         */
        public UpdateStyleWorker(Toolbox tb, TileStyleMonitor styleMonitor)
        {
            myToolbox = tb;
            myTileStyleMonitor = styleMonitor;
        }

        /**
         * Gets the tile style monitor.
         *
         * @return the tile style monitor
         */
        public TileStyleMonitor getTileStyleMonitor()
        {
            return myTileStyleMonitor;
        }

        /**
         * Gets the toolbox.
         *
         * @return the toolbox
         */
        public Toolbox getToolbox()
        {
            return myToolbox;
        }
    }
}
