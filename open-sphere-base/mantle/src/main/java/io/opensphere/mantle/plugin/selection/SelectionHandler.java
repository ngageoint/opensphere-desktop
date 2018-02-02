package io.opensphere.mantle.plugin.selection;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ContextMenuSelectionAdapter;
import io.opensphere.core.control.action.ActionContext;
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
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
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
    /** The Constant DEFAULT_BUFFER_DISTANCE. */
    private static final Length DEFAULT_BUFFER_DISTANCE = Length.create(Kilometers.class, 5.0);

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(SelectionHandler.class);

    /** The my command to processor map. */
    private final Map<SelectionCommand, List<WeakReference<SelectionCommandProcessor>>> myCommandToProcessorMap;

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
                return null;
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
            List<JMenuItem> menuItems = null;
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

    /**
     * Instantiates a new selection handler.
     *
     * @param tb The toolbox.
     * @param dataGroupController The data group controller.
     * @param pTypeController The controller through which data type lookups are
     *            performed.
     * @param queryRegionManager The query region manager.
     * @param dataElementCache The data element cache
     * @param dataElementUpdateUtils The data element update utilities
     */
    public SelectionHandler(Toolbox tb, DataGroupController dataGroupController, DataTypeController pTypeController,
            QueryRegionManager queryRegionManager, DataElementCache dataElementCache,
            DataElementUpdateUtils dataElementUpdateUtils)
    {
        myDataTypeController = pTypeController;
        myExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("SelectionHandler:Dispatch", 3, 4));
        myCommandToProcessorMap = new HashMap<>();

        myToolbox = tb;
        myDataGroupController = dataGroupController;

        myQueryRegionManager = queryRegionManager;
        myDataElementCache = dataElementCache;
        myDataElementUpdateUtils = dataElementUpdateUtils;
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
     * @param tb the {@link Toolbox}
     */
    public void install(Toolbox tb)
    {
        ContextActionManager actionManager = tb.getUIRegistry().getContextActionManager();
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
            List<WeakReference<SelectionCommandProcessor>> scpList = myCommandToProcessorMap.get(command);
            if (scpList == null)
            {
                scpList = new LinkedList<>();
                myCommandToProcessorMap.put(command, scpList);
            }

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
     * @param tb the {@link Toolbox}
     */
    public void uninstall(Toolbox tb)
    {
        ContextActionManager actionManager = tb.getUIRegistry().getContextActionManager();
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
                createBuffer();
            }
        }
        else if (cmd == SelectionCommand.CREATE_BUFFER_REGION_FOR_SELECTED_SEGMENT)
        {
            createBuffer();
        }
        else if (myLastGeometry instanceof PolygonGeometry)
        {
            Set<PolygonGeometry> geom = Collections.singleton((PolygonGeometry)myLastGeometry);
            myLastGeometry = null;
            doPurgeCheck(cmd, geom);
        }
        else if (myLastGeometry instanceof PolylineGeometry || myLastGeometry instanceof PointGeometry)
        {
            createBuffer();
        }
    }

    /**
     * Respond to a change in the buffer distance editor. If the distance is
     * well-formed and valid, show a preview; otherwise kill the preview.
     *
     * @param pLength the buffer distance supplied by the editor.
     */
    private void handleBufferEdit(Length pLength)
    {
        if (lengthOkay(pLength))
        {
            handlePreviewBuffer(pLength.inMeters());
        }
        else
        {
            destroyPreview();
        }
    }

    /**
     * Creates the buffer region for last selection geometry.
     */
    protected void createBuffer()
    {
        UnitsProvider<Length> uProv = myToolbox.getUnitsRegistry().getUnitsProvider(Length.class);
        Length defaultBuffer = defaultBuffer(uProv);
        // show the buffer when the dialog pops up:
        handleBufferEdit(defaultBuffer);
        BufferRegionInputPanel inputPanel = new BufferRegionInputPanel(defaultBuffer, uProv, getBufferRangeMessage(),
                this::handleBufferEdit);
        Length val = getBufferUserInput(inputPanel);
        if (val != null)
        {
            handleCreateBuffer(MouseInfo.getPointerInfo().getLocation(), val.inMeters());
        }
    }

    /**
     * Creates a buffer for all segments in a polyline. Uses a mantle lookup to
     * find segments related to the selected segment, then creates the buffer.
     */
    protected void createLineBuffer()
    {
        myLastGeometry = getCompleteGeometryGroup(myLastGeometry);
        createBuffer();
    }

    /**
     * Gathers input from the user to construct the buffer. The user may cancel
     * the dialog, indicating that no buffer should be drawn. As part of the
     * dialog, the buffer is drawn on the screen as the user changes the input
     * values, as a preview. The preview is a transient geometry, and should be
     * destroyed when the dialog closes (regardless of the user's choice). If
     * the user elects to accept the input, then a new, permanent buffer is
     * drawn for later use. The {@link Length} return reflects the user's input,
     * and is used to draw the permanent buffer. If the user cancels the dialog,
     * a null value is returned.
     *
     * @param inputPanel the panel in which the user enters data.
     * @return the {@link Length} from the user's input, or null if the dialog
     *         has been canceled.
     */
    protected Length getBufferUserInput(BufferRegionInputPanel inputPanel)
    {
        while (true)
        {
            // if the user cancels, then forget it
            if (!showPopup("Input Buffer Distance", inputPanel))
            {
                destroyPreview();
                return null;
            }
            try
            {
                Length returnValue = inputPanel.getDistance();
                if (lengthOkay(returnValue))
                {
                    destroyPreview();
                    return returnValue;
                }
                errorPopup("Invalid Distance Error", "The distance must be greater than zero.");
            }
            catch (NumberFormatException e)
            {
                errorPopup("Invalid Distance Error", "The distance must be a valid number.");
            }
        }
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
     * Validate the buffer distance.
     *
     * @param pLength the buffer distance to validate.
     * @return true if and only if the given distance is acceptable
     */
    private boolean lengthOkay(Length pLength)
    {
        if (myLastGeometry instanceof PolygonGeometry)
        {
            return pLength.getMagnitude() != 0.0;
        }
        return pLength.getMagnitude() > 0.0;
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
     * May not be necessary, but this is the way I found it. Create a "buffer"
     * Geometry for the argument, if it is of a supported type. If it is not,
     * then the argument is returned unchanged.
     *
     * @param g bla
     * @param distM buffer distance in meters
     * @return the buffer geometry if supported, or <i>g</i>
     */
    private static Geometry bufferOrSame(Geometry g, double distM)
    {
        Geometry newG = JTSCoreGeometryUtilities.getBufferGeom(g, distM);
        if (newG != null)
        {
            return newG;
        }
        return g;
    }

    /**
     * Creates a preview of the supplied distance, expressed in meters.
     *
     * @param pDistanceInMeters the distance from the origin, expressed in
     *            meters.
     */
    private void handlePreviewBuffer(double pDistanceInMeters)
    {
        if (myLastGeometry != null)
        {
            if (myPreviewGeometry != null)
            {
                unregisterGeometry(myPreviewGeometry);
            }
            myPreviewGeometry = bufferOrSame(myLastGeometry, pDistanceInMeters);
            if (myPreviewGeometry != null)
            {
                registerGeometry(myPreviewGeometry);
            }
        }
    }

    /**
     * Handle create buffer.
     *
     * @param pt the pt
     * @param distM the buffer distance
     */
    private void handleCreateBuffer(java.awt.Point pt, double distM)
    {
        myLastGeometry = bufferOrSame(myLastGeometry, distM);
        if (myLastGeometry == null)
        {
            errorPopup("Buffer Region Failure", "Failed to create buffer region for this item.");
            return;
        }

        myPreviewGeometry = myLastGeometry;
        registerGeometry(myPreviewGeometry);
        ActionContext<GeometryContextKey> context = myToolbox.getUIRegistry().getContextActionManager()
                .getActionContext(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class);

        Frame mainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
        SwingUtilities.convertPointFromScreen(pt, mainFrame);
        context.doAction(new GeometryContextKey(myPreviewGeometry), mainFrame, pt.x, pt.y, new PreviewKiller());
    }

    /**
     * Registers the supplied geometry for display.
     *
     * @param g the geometry to register.
     */
    private void registerGeometry(Geometry g)
    {
        myToolbox.getGeometryRegistry().addGeometriesForSource(this, Collections.singletonList(g));
    }

    /**
     * Removes the supplied geometry from display.
     *
     * @param g the geometry to remove from display.
     */
    private void unregisterGeometry(Geometry g)
    {
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this, Collections.singletonList(g));
    }

    /** Kills the preview, sometimes. */
    private class PreviewKiller extends ContextMenuSelectionAdapter
    {
        @Override
        public void popupMenuCanceled(PopupMenuEvent e)
        {
            destroyPreview();
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
        {
            destroyPreview();
        }
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
     * Show a modal dialog with the given "message". In the only current use,
     * the so-called "message" is actually a GUI.
     *
     * @param title the popup title
     * @param msg the "message"
     * @return true if and only if the user dismissed by selecting "Okay"
     */
    private boolean showPopup(String title, Object msg)
    {
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), msg,
                title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Show an error popup. Who would have guessed?
     *
     * @param title title
     * @param msg message
     */
    private void errorPopup(String title, String msg)
    {
        JOptionPane.showMessageDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), msg, title,
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Gets a message in which the user is informed of the constraints of buffer
     * region creation.
     *
     * @return a message in which the user is informed of the constraints of
     *         buffer region creation.
     */
    private String getBufferRangeMessage()
    {
        if (myLastGeometry instanceof PolygonGeometry)
        {
            return "Distance may be positive or negative, but not zero";
        }
        return "Distance must be greater than zero";
    }

    /**
     * Gets menu items for geometry.
     * 
     * @param geom the geometry
     * @return menu items
     */
    public List<JMenuItem> getGeometryMenuItems(Geometry geom)
    {
        List<JMenuItem> menuItems = null;
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

    public List<JMenuItem> getMultiGeometryMenu(Collection<? extends Geometry> geom)
    {
        List<JMenuItem> menuItems = SelectionCommand.getRoiMenuItems(new PolygonCommandActionListener(geom),
                hasLoadFilters());
        return menuItems;
    }

    /**
     * Gets the default buffer size from the supplied unit provider, using the
     * default buffer distance.
     *
     * @param p the provider from which units are provided.
     * @return the default length extracted from the supplied units provider.
     */
    private static Length defaultBuffer(UnitsProvider<Length> p)
    {
        return p.convert(p.getPreferredFixedScaleUnits(DEFAULT_BUFFER_DISTANCE), DEFAULT_BUFFER_DISTANCE);
    }
}
