package io.opensphere.mantle.plugin.selection;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.control.action.context.MultiGeometryContextKey;
import io.opensphere.core.control.action.context.ScreenPositionContextKey;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryGroupGeometry;
import io.opensphere.core.geometry.PointGeometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.ref.WeakReference;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.util.DataElementUpdateUtils;
import io.opensphere.mantle.data.util.purge.PurgeConfirmHelper;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.transformer.MapDataElementTransformer;

/**
 * The Class SelectionHandler.
 */
@SuppressWarnings("PMD.GodClass")
public class SelectionHandler
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(SelectionHandler.class);

    /** The my command to processor map. */
    private final EnumMap<SelectionCommand, List<WeakReference<SelectionCommandProcessor>>> myCommandToProcessorMap;

    /** The data group controller. */
    private final DataGroupController myDataGroupController;

    /**
     * The point geometry representing screen position, for creating a buffer
     * with no geometry.
     */
    private PointGeometry myNoGeometryPoint;

    /** The Default context menu provider. */
    private final ContextMenuProvider<ScreenPositionContextKey> myDefaultContextMenuProvider = new ContextMenuProvider<ScreenPositionContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, ScreenPositionContextKey key)
        {
            final GeographicPosition pos = myToolbox.getMapManager().convertToPosition(new Vector2i(key.getPosition().asPoint()),
                    ReferenceLevel.ELLIPSOID);
            if (pos != null)
            {
                myLastGeometry = null;
                PointGeometry.Builder<GeographicPosition> pointGeometry = new PointGeometry.Builder<>();
                pointGeometry.setPosition(pos);
                myNoGeometryPoint = new PointGeometry(pointGeometry, new DefaultPointRenderProperties(0, true, true, true), null);
                return SelectionCommand.getNoGeometryMenuItems(myMenuActionListener);
            }
            else
            {
                return Collections.emptyList();
            }
        }

        @Override
        public int getPriority()
        {
            return 10000;
        }
    };

    /** The ExecutorService. */
    private final ExecutorService myExecutor;

    /**
     * The menu provider for events related to single geometry selection or
     * completion.
     */
    private final ContextMenuProvider<GeometryContextKey> myGeometryContextMenuProvider = new ContextMenuProvider<GeometryContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, GeometryContextKey key)
        {
            List<JMenuItem> menuItems = New.list();
            if (contextId.equals(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT))
            {
                Geometry geom = key.getGeometry();
                menuItems = getGeometryMenuItems(geom);
            }
            else if (contextId.equals(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT)
                    && key.getGeometry() instanceof PolygonGeometry)
            {
                return SelectionCommand.getSelectionRegionMenuItems(
                        new PolygonCommandActionListener(Collections.singleton(key.getGeometry())), hasLoadFilters());
            }
            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 10000;
        }
    };

    /** The Last selection geometry. */
    private Geometry myLastGeometry;

    /** Listener for menu actions. */
    private final ActionListener myMenuActionListener = e -> handleCommand(e.getActionCommand());

    /**
     * The menu provider for events which occur on multiple polygon geometries.
     */
    private final ContextMenuProvider<MultiGeometryContextKey> myMultiGeometryContextMenuProvider = new ContextMenuProvider<MultiGeometryContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, MultiGeometryContextKey key)
        {
            List<JMenuItem> menuItems = getMultiGeometryMenu(key.getGeometries());
            if (key.getGeometries().isEmpty())
            {
                for (JMenuItem item : menuItems)
                {
                    item.setEnabled(false);
                    item.setToolTipText("No geometries selected for action.");
                }
            }
            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 10001;
        }
    };

    /** The Preview geometry. */
    private Geometry myPreviewGeometry;

    /** The query region manager. */
    private final QueryRegionManager myQueryRegionManager;

    /** The my toolbox. */
    private final Toolbox myToolbox;

    /** The controller through which data type lookups are performed. */
    private final DataTypeController myDataTypeController;

    /** The data element cache. */
    private final DataElementCache myDataElementCache;

    /** The data element update utilities. */
    private final DataElementUpdateUtils myDataElementUpdateUtils;

    /** The buffer region creator **/
    private final BufferRegionImpl myCreateBufferRegion;

    /**
     * Instantiates a new selection handler.
     *
     * @param toolbox The toolbox.
     * @param dataGroupController The data group controller.
     * @param pTypeController The controller through which data type lookups are
     *            performed.
     * @param queryRegionManager The query region manager.
     * @param dataElementCache The data element cache
     * @param dataElementUpdateUtils The data element update utilities
     */
    public SelectionHandler(Toolbox toolbox, DataGroupController dataGroupController, DataTypeController pTypeController,
            QueryRegionManager queryRegionManager, DataElementCache dataElementCache,
            DataElementUpdateUtils dataElementUpdateUtils)
    {
        myDataTypeController = pTypeController;
        myExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("SelectionHandler:Dispatch", 3, 4));
        myCommandToProcessorMap = new EnumMap<>(SelectionCommand.class);
        myToolbox = toolbox;
        myDataGroupController = dataGroupController;
        myQueryRegionManager = queryRegionManager;
        myDataElementCache = dataElementCache;
        myDataElementUpdateUtils = dataElementUpdateUtils;
        myCreateBufferRegion = new BufferRegionImpl(toolbox);
    }

    /**
     * Gets the set of active data types that have filters applied.
     *
     * @return the set of active data types that have filters applied
     */
    public Set<DataTypeInfo> getActiveFilteredDataTypes()
    {
        Set<String> typeKeys = new HashSet<>();
        myToolbox.getDataFilterRegistry().getLoadFilters().stream().forEach(f -> typeKeys.add(f.getTypeKey()));
        typeKeys.addAll(myToolbox.getDataFilterRegistry().getSpatialLoadFilterKeys());
        return typeKeys.stream().map(k -> myDataGroupController.findMemberById(k))
                .filter(t -> t != null && myDataGroupController.isTypeActive(t) && t.isFilterable()).collect(Collectors.toSet());
    }

    /**
     * Returns whether there are any active filters for active layers.
     *
     * @return Whether there are any active filters for active layers
     */
    public boolean hasLoadFilters()
    {
        return !getActiveFilteredDataTypes().isEmpty();
    }

    /**
     * Install the selection handler.
     *
     * @param toolbox the {@link Toolbox}
     */
    public void install(Toolbox toolbox)
    {
        ContextActionManager actionManager = toolbox.getUIRegistry().getContextActionManager();
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.SCREEN_POSITION_CONTEXT, ScreenPositionContextKey.class,
                myDefaultContextMenuProvider);
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.ROI_CONTEXT, MultiGeometryContextKey.class,
                myMultiGeometryContextMenuProvider);
    }

    /**
     * Register selection command processor.
     *
     * @param command the command to be processed
     * @param processor the processor to process the command
     */
    public void registerSelectionCommandProcessor(SelectionCommand command, SelectionCommandProcessor processor)
    {
        synchronized (myCommandToProcessorMap)
        {
            List<WeakReference<SelectionCommandProcessor>> scpList = myCommandToProcessorMap.computeIfAbsent(command,
                    k -> new LinkedList<>());
            // Make sure we don't already have this processor in our set, remove
            // any garbage collected listeners from the set.
            Iterator<WeakReference<SelectionCommandProcessor>> wrItr = scpList.iterator();
            boolean found = false;
            WeakReference<SelectionCommandProcessor> wr = null;
            SelectionCommandProcessor proc = null;
            while (wrItr.hasNext())
            {
                wr = wrItr.next();
                proc = wr.get();
                if (proc == null)
                {
                    wrItr.remove();
                }
                else if (Utilities.sameInstance(processor, proc))
                {
                    found = true;
                }
            }
            // If we didn't find it in the set already add it.
            if (!found)
            {
                scpList.add(new WeakReference<>(processor));
            }
        }
    }

    /**
     * Handle the creation of a selection region.
     *
     * @param bounds The bounds of the selection regions.
     * @param command The command causing region creation.
     */
    public void selectionRegionCreated(List<PolygonGeometry> bounds, String command)
    {
        doPurgeCheck(selectionCommand(command), bounds);
    }

    /**
     * Uninstall the selection handler.
     *
     * @param toolbox the {@link Toolbox}
     */
    public void uninstall(Toolbox toolbox)
    {
        ContextActionManager actionManager = toolbox.getUIRegistry().getContextActionManager();
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.SCREEN_POSITION_CONTEXT,
                ScreenPositionContextKey.class, myDefaultContextMenuProvider);
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.ROI_CONTEXT, MultiGeometryContextKey.class,
                myMultiGeometryContextMenuProvider);
    }

    /**
     * Unregister selection command processor for the specified command.
     *
     * @param command the command
     * @param processor the processor
     */
    public void unregisterSelectionCommandProcessor(SelectionCommand command, SelectionCommandProcessor processor)
    {
        synchronized (myCommandToProcessorMap)
        {
            List<WeakReference<SelectionCommandProcessor>> scpList = myCommandToProcessorMap.get(command);
            if (scpList != null)
            {
                // Search for the listener in our set and remove if found,
                // also remove any garbage collected listeners from the set.
                Iterator<WeakReference<SelectionCommandProcessor>> wrItr = scpList.iterator();
                WeakReference<SelectionCommandProcessor> wr = null;
                SelectionCommandProcessor lstr = null;
                while (wrItr.hasNext())
                {
                    wr = wrItr.next();
                    lstr = wr.get();
                    if (lstr == null || Utilities.sameInstance(processor, lstr))
                    {
                        wrItr.remove();
                    }
                }
            }
        }
    }

    /**
     * Unregister selection command processor for all commands for which that
     * processors is registered.
     *
     * @param processor the processor
     */
    public void unregisterSelectionCommandProcessor(SelectionCommandProcessor processor)
    {
        synchronized (myCommandToProcessorMap)
        {
            for (SelectionCommand command : SelectionCommand.values())
            {
                unregisterSelectionCommandProcessor(command, processor);
            }
        }
    }

    /**
     * Destroy preview.
     */
    private void destroyPreview()
    {
        if (myPreviewGeometry != null)
        {
            unregisterGeometry(myPreviewGeometry);
        }
        myPreviewGeometry = null;
    }

    /**
     * Removes the supplied geometry from display.
     *
     * @param geometry the geometry to remove from display.
     */
    private void unregisterGeometry(Geometry geometry)
    {
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this, Collections.singletonList(geometry));
    }

    /**
     * Method called when a menu button is selected.
     *
     * @param act The action command.
     */
    private void handleCommand(String act)
    {
        assert EventQueue.isDispatchThread();
        destroyPreview();
        SelectionCommand cmd = selectionCommand(act);
        if (cmd == null)
        {
            return;
        }
        if (myLastGeometry == null)
        {
            if (cmd == SelectionCommand.CREATE_BUFFER_REGION)
            {
                myLastGeometry = myNoGeometryPoint;
            }
            else
            {
                doPurgeCheck(cmd, null);
            }
        }
        if (cmd == SelectionCommand.CREATE_BUFFER_REGION)
        {
            if (myLastGeometry instanceof PolylineGeometry && !(myLastGeometry instanceof PolygonGeometry))
            {
                createLineBuffer();
            }
            else
            {
                myCreateBufferRegion.createBuffer(myLastGeometry);
            }
        }
        else if (cmd == SelectionCommand.CREATE_BUFFER_REGION_FOR_SELECTED_SEGMENT)
        {
            myCreateBufferRegion.createBuffer(myLastGeometry);
        }
        else if (myLastGeometry instanceof PolygonGeometry)
        {
            Set<PolygonGeometry> geom = Collections.singleton((PolygonGeometry)myLastGeometry);
            myLastGeometry = null;
            doPurgeCheck(cmd, geom);
        }
        else if (myLastGeometry instanceof PolylineGeometry || myLastGeometry instanceof PointGeometry)
        {
            myCreateBufferRegion.createBuffer(myLastGeometry);
        }
    }

    /**
     * Creates a buffer for all segments in a polyline. Uses a mantle lookup to
     * find segments related to the selected segment, then creates the buffer.
     */
    protected void createLineBuffer()
    {
        myLastGeometry = getCompleteGeometryGroup(myLastGeometry);
        myCreateBufferRegion.createBuffer(myLastGeometry);
    }

    /**
     * For the supplied data model ID (which represents a single geometry
     * component, such as a line segment in the case of a displayed track),
     * gather the set of geometries that make up the complete group. The
     * complete group is generated from the set of segments as a
     * {@link GeometryGroupGeometry}.
     *
     * @param pGeometry the geometry component for which the complete group will
     *            be constructed.
     * @return a {@link GeometryGroupGeometry} composed of the set of components
     *         related to the currently selected geometry.
     */
    protected Geometry getCompleteGeometryGroup(Geometry pGeometry)
    {
        DataTypeInfo dataType = myDataTypeController.getDataTypeInfoForGeometryId(pGeometry.getDataModelId());
        if (dataType != null)
        {
            MapDataElementTransformer transformer = myDataTypeController.getTransformerForType(dataType.getTypeKey());
            GeometryGroupGeometry.Builder builder = new GeometryGroupGeometry.Builder(GeographicPosition.class);
            builder.setInitialGeometries(myToolbox.getGeometryRegistry()
                    .getGeometriesForSource(transformer, PolylineGeometry.class).stream().collect(Collectors.toList()));
            return new GeometryGroupGeometry(builder, pGeometry.getRenderProperties());
        }
        return pGeometry;
    }

    /**
     * Do purge check.
     *
     * @param cmd the cmd
     * @param selectionBounds the selection bounds
     */
    private void doPurgeCheck(SelectionCommand cmd, Collection<? extends PolygonGeometry> selectionBounds)
    {
        if (cmd == null)
        {
            return;
        }
        /* Do a special confirmation for purge here before notifying the command
         * processors. Probably not the best way to do this, refactor later into
         * something more generic. */
        if (cmd != SelectionCommand.PURGE)
        {
            // special case: deselect with no bounds, so deselect all
            if (selectionBounds == null && cmd == SelectionCommand.DESELECT)
            {
                myDataElementUpdateUtils.setDataElementsSelected(false, myDataElementCache.getAllElementIdsAsList(), null, this);
            }
            else
            {
                notifySelectionCommandProcessors(selectionBounds, cmd);
            }
        }
        else if (PurgeConfirmHelper.confirmProceedWithPurge(myToolbox, null, this))
        {
            notifySelectionCommandProcessors(selectionBounds, cmd);
        }
    }

    /**
     * Gets the selection command from its string action command counterpart.
     *
     * @param actionCommand the command to convert to a {@link SelectionCommand}
     * @return the selection command or null if not valid.
     */
    private SelectionCommand selectionCommand(String actionCommand)
    {
        try
        {
            return SelectionCommand.valueOf(actionCommand);
        }
        catch (IllegalArgumentException ex)
        {
            // Unknown command returned.
            LOGGER.warn("Illegal Selection Command Recieved: " + actionCommand);
        }
        return null;
    }

    /**
     * Notify selection command processors.
     *
     * @param bounds the bounds
     * @param command the command
     */
    private void notifySelectionCommandProcessors(Collection<? extends PolygonGeometry> bounds, SelectionCommand command)
    {
        myExecutor.execute(() ->
        {
            synchronized (myCommandToProcessorMap)
            {
                List<WeakReference<SelectionCommandProcessor>> wrList = myCommandToProcessorMap.get(command);
                if (wrList != null && !wrList.isEmpty())
                {
                    Iterator<WeakReference<SelectionCommandProcessor>> wrItr = wrList.iterator();
                    while (wrItr.hasNext())
                    {
                        SelectionCommandProcessor lstr = wrItr.next().get();
                        if (lstr == null)
                        {
                            wrItr.remove();
                        }
                        else
                        {
                            lstr.selectionOccurred(bounds, command);
                        }
                    }
                }
            }
        });
    }

    /** Action listener for actions on the menu items. */
    private final class PolygonCommandActionListener implements ActionListener
    {
        /** The geometry associated with the menu action. */
        private final List<PolygonGeometry> myGeometries = new LinkedList<>();

        /**
         * Constructor.
         *
         * @param geoms The geometries associated with the menu action.
         */
        public PolygonCommandActionListener(Collection<? extends Geometry> geoms)
        {
            for (Geometry geom : geoms)
            {
                if (geom instanceof PolygonGeometry)
                {
                    myGeometries.add((PolygonGeometry)geom);
                }
            }
        }

        @Override
        public void actionPerformed(ActionEvent evt)
        {
            selectionRegionCreated(myGeometries, ((JMenuItem)evt.getSource()).getActionCommand());
        }
    }

    /**
     * Gets menu items for geometry.
     *
     * @param geom the geometry
     * @return menu items
     */
    public List<JMenuItem> getGeometryMenuItems(Geometry geom)
    {
        List<JMenuItem> menuItems = New.list();
        if (myQueryRegionManager.getQueryRegion(geom) != null)
        {
            myLastGeometry = geom;
            menuItems = SelectionCommand.getQueryRegionMenuItems(myMenuActionListener, hasLoadFilters());
        }
        else if (geom instanceof PolygonGeometry)
        {
            myLastGeometry = geom;
            menuItems = SelectionCommand.getPolygonMenuItems(myMenuActionListener, hasLoadFilters(), false);
        }
        else if (geom instanceof PolylineGeometry)
        {
            myLastGeometry = geom;
            menuItems = SelectionCommand.getPolylineMenuItems(myMenuActionListener, hasLoadFilters());
        }
        else if (geom instanceof PointGeometry)
        {
            myLastGeometry = geom;
            menuItems = SelectionCommand.getPointMenuItems(myMenuActionListener, hasLoadFilters());
        }
        else
        {
            LOGGER.warn("Unrecognized geometry type: '" + geom.getClass().getName() + "' cannot be used to create a buffer.");
        }
        return menuItems;
    }

    /**
     * Gets menu when you have multiple geometries.
     *
     * @param geometries the geometries
     * @return menuItems the menu
     */
    public List<JMenuItem> getMultiGeometryMenu(Collection<? extends Geometry> geometries)
    {
        return SelectionCommand.getRoiMenuItems(new PolygonCommandActionListener(geometries), hasLoadFilters());
    }
}
