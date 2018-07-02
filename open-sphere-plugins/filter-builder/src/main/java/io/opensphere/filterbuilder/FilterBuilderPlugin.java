package io.opensphere.filterbuilder;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.Nullable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import io.opensphere.controlpanels.util.ShowFilterDialogEvent;
import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterRegistryListener;
import io.opensphere.core.datafilter.impl.DataFilterRegistryAdapter;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.property.PluginPropertyUtils;
import io.opensphere.core.util.swing.AlertNotificationButton;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.filterbuilder.config.PluginConstants;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.impl.FbServiceImpl;
import io.opensphere.filterbuilder.impl.FilterBuilderControllerImpl;
import io.opensphere.filterbuilder.impl.FilterBuilderToolboxImpl;
import io.opensphere.filterbuilder.state.FilterManagerStateController;
import io.opensphere.filterbuilder2.editor.FilterEditorDialog;
import io.opensphere.filterbuilder2.manager.FilterManagerDialog;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/** The Filter Builder plugin. */
public class FilterBuilderPlugin extends PluginAdapter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FilterBuilderPlugin.class);

    /** The data group activation listener. */
    private final Runnable myActivationListener = this::updateFilterBuilderActivationButton;

    /** The Data filter registry listener. */
    private DataFilterRegistryListener myDataFilterRegistryListener;

    /** The toolbox. */
    private volatile FilterBuilderToolboxImpl myFbToolbox;

    /** The Activation button. */
    private FilterBuilderAlertButton myFilterBuilderActivationButton;

    /** The Filter manager state controller. */
    private FilterManagerStateController myFilterManagerStateController;

    /** Menu provider for geometry context. */
    private GeometryContextMenuProvider myGeometryContextMenuProvider;

    /** The service manager. */
    private volatile EventListenerService myServiceManager;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        final Properties properties = PluginPropertyUtils.convertToProperties(plugindata.getPluginProperty());
        final String runtimeDir = StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties());
        properties.put(PluginConstants.RUNTIME_DIR_KEY, runtimeDir);

        myFbToolbox = new FilterBuilderToolboxImpl(toolbox, properties);
        toolbox.getPluginToolboxRegistry().registerPluginToolbox(myFbToolbox);

        final FilterBuilderControllerImpl fbCtrl = new FilterBuilderControllerImpl(myFbToolbox);
        myFbToolbox.setFilterBuilderController(fbCtrl);
        myFbToolbox.setFbService(new FbServiceImpl(fbCtrl));

        EventQueueUtilities.invokeLater(() -> toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(
                ToolbarLocation.SOUTH, "FilterBuilder", getFilterBuilderActivationButton(), 201, SeparatorLocation.NONE));

        myDataFilterRegistryListener = createDataFilterRegistryListener();
        toolbox.getDataFilterRegistry().addListener(myDataFilterRegistryListener);

        final MantleToolbox mantleTb = myFbToolbox.getMantleToolBox();
        mantleTb.getDataGroupController().addActivationListener(myActivationListener);

        myServiceManager = new EventListenerService(toolbox.getEventManager(), 3);
        myServiceManager.bindEvent(ShowFilterDialogEvent.class, this::handleShowFilterDialogEvent);
        myServiceManager.open();

        myFilterManagerStateController = new FilterManagerStateController(fbCtrl, mantleTb.getDataGroupController(),
                toolbox.getDataFilterRegistry(), mantleTb.getQueryRegionManager(), toolbox.getGeometryRegistry());

        toolbox.getModuleStateManager().registerModuleStateController("Filters", myFilterManagerStateController);

        updateFilterBuilderActivationButton();

        toolbox.getUIRegistry().getIconLegendRegistry().addIconToLegend(IconUtil.getNormalIcon(IconType.FILTER), "Filters",
                "Opens the Filter Manager dialog where filters can be created, edited, imported and exported.");

        final ContextActionManager actionManager = toolbox.getUIRegistry().getContextActionManager();
        myGeometryContextMenuProvider = new GeometryContextMenuProvider(toolbox);
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
        actionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
    }

    @Override
    public void close()
    {
        final Toolbox toolbox = myFbToolbox.getMainToolBox();
        toolbox.getPluginToolboxRegistry().removePluginToolbox(myFbToolbox);

        toolbox.getUIRegistry().getToolbarComponentRegistry().deregisterToolbarComponent(ToolbarLocation.SOUTH, "FilterBuilder");

        toolbox.getDataFilterRegistry().removeListener(myDataFilterRegistryListener);

        myFbToolbox.getMantleToolBox().getDataGroupController().removeActivationListener(myActivationListener);

        myServiceManager.close();

        final ContextActionManager actionManager = toolbox.getUIRegistry().getContextActionManager();
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
        actionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                myGeometryContextMenuProvider);
    }

    /**
     * Show the editor for a filter, or show the filter manager for the layer,
     * if the filter is {@code null}.
     *
     * @param typeKey The type key.
     * @param filter The filter.
     */
    private void showEditor(String typeKey, @Nullable DataFilterGroup filter)
    {
        assert EventQueue.isDispatchThread();
        final DataTypeInfo dti = myFbToolbox.getDateTypeInfoForKey(typeKey);
        if (dti == null)
        {
            return;
        }
        if (filter == null)
        {
            FilterManagerDialog.showDataGroup(myFbToolbox, dti.getParent());
        }
        else
        {
            final Optional<Filter> found = myFbToolbox.getController().getAllFilters().stream()
                    .filter(f -> f.getTypeKey().equals(typeKey) && f.getName().equals(filter.getName())).findAny();
            if (found.isPresent())
            {
                final FilterEditorDialog editorDialog = new FilterEditorDialog(
                        myFbToolbox.getMainToolBox().getUIRegistry().getMainFrameProvider().get(), myFbToolbox, found.get(), dti,
                        false);
                editorDialog.buildAndShow();
            }
        }
    }

    /**
     * Creates the data filter registry listener.
     *
     * @return the data filter registry listener
     */
    private DataFilterRegistryListener createDataFilterRegistryListener()
    {
        final DataFilterRegistryListener aListener = new DataFilterRegistryAdapter()
        {
            @Override
            public void loadFilterAdded(String typeKey, DataFilter filter, Object source)
            {
                updateFilterBuilderActivationButton();
            }

            @Override
            public void loadFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
            {
                updateFilterBuilderActivationButton();
            }

            @Override
            public void showEditor(String typeKey, DataFilterGroup filter)
            {
                EventQueue.invokeLater(() -> FilterBuilderPlugin.this.showEditor(typeKey, filter));
            }

            @Override
            public void spatialFilterAdded(String typeKey, Geometry filter)
            {
                updateFilterBuilderActivationButton();
            }

            @Override
            public void spatialFilterRemoved(String typeKey, Geometry filter)
            {
                updateFilterBuilderActivationButton();
            }

            @Override
            public void viewFilterAdded(String typeKey, DataFilter filter, Object source)
            {
                updateFilterBuilderActivationButton();
            }

            @Override
            public void viewFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
            {
                updateFilterBuilderActivationButton();
            }
        };
        return aListener;
    }

    /**
     * Update filter builder activation button.
     */
    private void updateFilterBuilderActivationButton()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                final Set<DataTypeInfo> activeFilteredDataTypes = myFbToolbox.getMantleToolBox().getSelectionHandler()
                        .getActiveFilteredDataTypes();

                int filterCount = 0;
                for (final DataTypeInfo activeFilteredDataType : activeFilteredDataTypes)
                {
                    final DataFilter filter = myFbToolbox.getDataFilterRegistry().getLoadFilter(activeFilteredDataType.getTypeKey());
                    if (filter != null)
                    {
                        filterCount += filter.getFilterCount();
                    }

                    final Geometry spatialFilter = myFbToolbox.getDataFilterRegistry()
                            .getSpatialLoadFilter(activeFilteredDataType.getTypeKey());
                    if (spatialFilter instanceof GeometryCollection)
                    {
                        filterCount += ((GeometryCollection)spatialFilter).getNumGeometries();
                    }
                    else if (spatialFilter != null)
                    {
                        filterCount++;
                    }
                }

                final boolean activeFilters = filterCount > 0;
                IconUtil.setIcons(getFilterBuilderActivationButton(), IconType.FILTER,
                        activeFilters ? Color.GREEN : IconUtil.DEFAULT_ICON_FOREGROUND);

                getFilterBuilderActivationButton().setAlertCount(filterCount);
            }
        });
    }

    /**
     * Creates the toolbar activation button.
     *
     * @return the j button
     */
    private FilterBuilderAlertButton getFilterBuilderActivationButton()
    {
        if (myFilterBuilderActivationButton == null)
        {
            myFilterBuilderActivationButton = new FilterBuilderAlertButton(null);
            myFilterBuilderActivationButton.addActionListener(evt -> doFilterBuilderButtonAction());
        }
        return myFilterBuilderActivationButton;
    }

    /**
     * Do filter builder button action.
     */
    private void doFilterBuilderButtonAction()
    {
        final Collection<DataTypeInfo> dataTypes = myFbToolbox.getMantleToolBox().getSelectionHandler().getActiveFilteredDataTypes();
        if (dataTypes.isEmpty())
        {
            FilterManagerDialog.showDataType(myFbToolbox, null);
        }
        else
        {
            showPopupMenu(dataTypes);
        }
    }

    /**
     * Shows the popup menu for managing filters.
     *
     * @param dataTypes the data types to show
     */
    private void showPopupMenu(Collection<DataTypeInfo> dataTypes)
    {
        final List<DataTypeInfo> sortedDataTypes = new ArrayList<>(dataTypes);
        Collections.sort(sortedDataTypes);
        sortedDataTypes.add(null);

        final JPopupMenu menu = new JPopupMenu();
        for (final DataTypeInfo dataType : sortedDataTypes)
        {
            final JMenuItem item = new JMenuItem(FilterManagerDialog.menuLabel(dataType));
            item.addActionListener(e -> FilterManagerDialog.showDataType(myFbToolbox, dataType));
            if (dataType == null)
            {
                menu.add(new JSeparator());
            }
            menu.add(item);
        }

        final int menuHeight = (menu.getComponentCount() - 1) * 19 + 8;
        menu.show(myFilterBuilderActivationButton, 0, -menuHeight);
    }

    /**
     * Handles a ShowFilterDialogEvent.
     *
     * @param event the event
     */
    private void handleShowFilterDialogEvent(ShowFilterDialogEvent event)
    {
        if (StringUtils.isNotEmpty(event.getTypeKey()))
        {
            final DataGroupInfo dataGroup = myFbToolbox.getMantleToolBox().getDataGroupController()
                    .getDataGroupInfo(event.getTypeKey());
            if (dataGroup == null)
            {
                LOGGER.error("Could not find data group for key: " + event.getTypeKey());
            }
            else
            {
                EventQueueUtilities.invokeLater(() -> FilterManagerDialog.showDataGroup(myFbToolbox, dataGroup));
            }
        }
        else if (event.getDataType() != null)
        {
            final DataTypeInfo dataType = event.getDataType();
            EventQueueUtilities.invokeLater(() -> FilterManagerDialog.showDataType(myFbToolbox, dataType));
        }
    }

    /**
     * The Class FilterBuilderAlertButton.
     */
    private static class FilterBuilderAlertButton extends AlertNotificationButton
    {
        /** Serial. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new filter builder alert button.
         *
         * @param icon the icon
         */
        public FilterBuilderAlertButton(ImageIcon icon)
        {
            super(icon);
            setAlertColor(ColorUtilities.convertFromHexString("0000CCFF", 0, 1, 2, 3));
            setToolTipText(FilterManagerDialog.menuLabel(null));
            setFocusPainted(false);
        }

        @Override
        public void setAlertCount(int cnt)
        {
            setCount(cnt);
            setAlertCounterText(Integer.toString(cnt));
            repaint();
        }
    }
}
