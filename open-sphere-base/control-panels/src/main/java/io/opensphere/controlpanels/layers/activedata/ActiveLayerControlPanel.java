package io.opensphere.controlpanels.layers.activedata;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import io.opensphere.controlpanels.ControlPanelToolbox;
import io.opensphere.controlpanels.event.AnimationChangeExtentRequestEvent;
import io.opensphere.controlpanels.layers.layerdetail.LayerDetailsCoordinator;
import io.opensphere.controlpanels.layers.util.LoadsToUtilities;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.export.Exporter;
import io.opensphere.core.export.Exporters;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.concurrent.ProcrastinatingEventQueueExecutor;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.IconToggleButton;
import io.opensphere.core.util.swing.SplitButton;
import io.opensphere.core.util.swing.VerticalList;
import io.opensphere.mantle.controller.util.DataGroupInfoUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.event.DataTypeInfoLoadsToChangeEvent;
import io.opensphere.mantle.data.export.ExportMenuProvider;
import io.opensphere.mantle.data.geom.style.dialog.MiniStylePanel;
import io.opensphere.mantle.data.geom.style.dialog.ShowTypeVisualizationStyleEvent;
import io.opensphere.mantle.data.geom.style.dialog.VisualizationStyleControlDialog;
import io.opensphere.mantle.layers.event.LayerSelectedEvent;

/**
 * The lower panel of the Active Layers panel.
 */
@SuppressWarnings("PMD.GodClass")
public final class ActiveLayerControlPanel extends LayerControlPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** Typical space between components. */
    private static final int SPACE = 4;

    /** The panel for custom data group controls. */
    private Box myCustomControlsPanel;

    /**
     * Executor used to prevent showing/hiding the control panel too rapidly.
     */
    private final transient ProcrastinatingEventQueueExecutor myExecutor = new ProcrastinatingEventQueueExecutor(200);

    /** The export button. */
    private SplitButton myExportButton;

    /** The feature edit button. */
    private IconButton myFeatureEditButton;

    /** The vis style button. */
    private IconButton myFeatureVisStyleButton;

    /** The Layer details button. */
    private IconButton myLayerDetailsButton;

    /** A button for closing the selection panel. */
    private IconButton myCloseSelectionButton;

    /** The Layer details coordinator. */
    private final transient LayerDetailsCoordinator myLayerDetailsCoordinator;

    /** The Layer time span button. */
    private IconButton myLayerTimeSpanButton;

    /** The Mini style panel. */
    private JComponent myMiniStyleBox;

    /** The analyze button. */
    private IconToggleButton myAnalyzeButton;

    /** The timeline button. */
    private IconToggleButton myTimelineButton;

    /** The vis style button. */
    private IconButton myTileVisStyleButton;

    /** The vis style button. */
    private IconButton myHeatmapVisStyleButton;

    /** The upper button panel. */
    private GridBagPanel myUpperButtonPanel;

    /** The loads to subscriber. */
    private final transient EventListener<DataTypeInfoLoadsToChangeEvent> myLoadsToSubscriber = this::handleLoadsToChange;

    /** The control panel toolbox. **/
    private final transient ControlPanelToolbox myCpToolbox;

    /** The provider panel. **/
    private final GridBagPanel myProviderPanel;

    /**
     * Gets the MapVisualizationType for the given LayerSelectedEvent.
     *
     * @param selectEvent the LayerSelectedEvent
     * @return the MapVisualizationType or null
     */
    private static MapVisualizationType getMapVisualizationType(LayerSelectedEvent selectEvent)
    {
        MapVisualizationType mvt = null;
        DataTypeInfo dti = selectEvent.getDataTypeInfo();
        if (dti == null)
        {
            Collection<DataTypeInfo> members = selectEvent.getDataGroupInfo().getMembers(false);
            if (!members.isEmpty())
            {
                dti = members.iterator().next();
            }
        }
        if (dti != null && dti.getMapVisualizationInfo() != null)
        {
            mvt = dti.getMapVisualizationInfo().getVisualizationType();
        }
        return mvt;
    }

    /**
     * Determines if the opacity slider should be shown for the given
     * MapVisualizationType.
     *
     * @param mvt the MapVisualizationType
     * @return whether to show opacity
     */
    private static boolean showOpacity(MapVisualizationType mvt)
    {
        return mvt != MapVisualizationType.PLACE_NAME_ELEMENTS && mvt != MapVisualizationType.TERRAIN_TILE;
    }

    /**
     * Instantiates a new base layer control panel.
     *
     * @param tb the {@link Toolbox}
     * @param ldc the {@link LayerDetailsCoordinator}
     */
    public ActiveLayerControlPanel(Toolbox tb, LayerDetailsCoordinator ldc)
    {
        super(tb);
        myLayerDetailsCoordinator = ldc;
        myCpToolbox = tb.getPluginToolboxRegistry().getPluginToolbox(ControlPanelToolbox.class);
        myProviderPanel = new GridBagPanel();

        setBorder(null);
        setLayout(new BorderLayout());
        setMinimumSize(null);
        setPreferredSize(null);
        add(buildMainPanel(), BorderLayout.CENTER);

        getFeatureColorPanel().setVisible(false);
        getOpacityPanel().setVisible(false);
        myMiniStyleBox.setVisible(false);

        tb.getEventManager().subscribe(DataTypeInfoLoadsToChangeEvent.class, myLoadsToSubscriber);
    }

    /**
     * Set the layer selection after a slight delay used to coalesce events.
     *
     * @param layerSelectedEvent The event.
     */
    public void delaySetSelected(final LayerSelectedEvent layerSelectedEvent)
    {
        myExecutor.execute(() -> setSelected(layerSelectedEvent));
    }

    /**
     * Sets the selected layer.
     *
     * @param selectEvent the new selected layer
     */
    public void setSelected(LayerSelectedEvent selectEvent)
    {
        assert EventQueue.isDispatchThread();
        if (CollectionUtilities.hasContent(selectEvent.getDataGroupInfos())
                || CollectionUtilities.hasContent(selectEvent.getDataTypeInfos()))
        {
            setSelectedItems(selectEvent.getDataGroupInfos(), selectEvent.getDataTypeInfos());
            DataTypeInfo selectedType = getSelectedDataType();
            DataGroupInfo selectedGroup = getSelectedDataGroup();
            myProviderPanel.setVisible(false);
            getOpacityPanel().setVisible(showOpacity(getMapVisualizationType(selectEvent)));
            determineStyleShortCutButtonVisibility();
            determineEditButtonVisibility();
            determineLayerDetailsButtonVisibility();
            getFeatureColorPanel().setVisible(myFeatureVisStyleButton.isVisible());
            DataTypeInfo selectedLayer = getSelectedLayer();
            myAnalyzeButton.setVisible(LoadsToUtilities.allowAnalyzeSelection(selectedLayer));
            myAnalyzeButton.setSelected(LoadsToUtilities.isAnalyzeEnabled(selectedLayer));
            myTimelineButton.setVisible(LoadsToUtilities.allowTimelineSelection(selectedLayer));
            myTimelineButton.setSelected(LoadsToUtilities.isTimelineEnabled(selectedLayer));
            Collection<Object> objects = CollectionUtilities.concat(selectEvent.getDataGroupInfos(),
                    selectEvent.getDataTypeInfos());
            List<Exporter> exporters = Exporters.getExporters(objects, getToolbox(), java.io.File.class);
            if (exporters.isEmpty())
            {
                myExportButton.setVisible(false);
            }
            else
            {
                myExportButton.removeAll();
                ExportMenuProvider menuProvider = new ExportMenuProvider();
                for (JMenuItem menuItem : menuProvider.getMenuItems(getToolbox(), "Export to ", exporters))
                {
                    myExportButton.add(menuItem);
                }
                myExportButton.setVisible(true);
            }
            if (selectedType != null && selectedType.getMetaDataInfo() != null && selectedType.getMapVisualizationInfo() != null
                    && selectedType.getMapVisualizationInfo().usesMapDataElements())
            {
                myCpToolbox.getLayerControlProviderRegistry().getProviders()
                        .forEach(provider -> rebuildProviderLayerControl(provider.apply(selectedType)));
            }
            if (Arrays.stream(myUpperButtonPanel.getComponents()).anyMatch(c -> c instanceof AbstractButton && c.isVisible()))
            {
                myUpperButtonPanel.setVisible(true);
            }
            rebuildMiniStyleBox(selectedGroup, selectedType);
            myMiniStyleBox.setVisible(true);
            Component layerControlComponent = selectedGroup.getAssistant().getLayerControlUIComponent(null, selectedGroup,
                    selectedType);
            if (layerControlComponent != null)
            {
                myCustomControlsPanel.removeAll();
                myCustomControlsPanel.add(layerControlComponent);
                myCustomControlsPanel.setVisible(true);
                layerControlComponent.repaint();
            }
            else
            {
                myCustomControlsPanel.setVisible(false);
            }
            setVisible(true);
        }
        else
        {
            setSelectedItems(Collections.<DataGroupInfo>emptyList(), Collections.<DataTypeInfo>emptyList());
            myUpperButtonPanel.setVisible(false);
            getFeatureColorPanel().setVisible(false);
            getOpacityPanel().setVisible(false);
            myMiniStyleBox.removeAll();
            myMiniStyleBox.setVisible(false);
            myLayerTimeSpanButton.setVisible(false);
            myFeatureVisStyleButton.setVisible(false);
            myTileVisStyleButton.setVisible(false);
            myHeatmapVisStyleButton.setVisible(false);
            myCustomControlsPanel.setVisible(false);
            setVisible(false);
        }
        if (myTileVisStyleButton.isVisible() && getSelectedDataType() != null)
        {
            updateTileLevelControlPanel();
        }
        else
        {
            getTileLevelControlPanel().setVisible(false);
        }
    }

    /**
     * Builds the color/opacity panel.
     *
     * @return the color/opacity panel
     */
    private GridBagPanel buildColorOpacityPanel()
    {
        GridBagPanel colorOpacityPanel = new GridBagPanel();
        colorOpacityPanel.setInsets(0, 0, 0, SPACE);
        colorOpacityPanel.add(getFeatureColorPanel());
        colorOpacityPanel.fillHorizontal();
        colorOpacityPanel.setInsets(0, 0, 0, 0);
        colorOpacityPanel.add(getOpacityPanel());
        return colorOpacityPanel;
    }

    /**
     * Builds the main panel.
     *
     * @return the main panel
     */
    private GridBagPanel buildMainPanel()
    {
        GridBagPanel mainPanel = new GridBagPanel();
        mainPanel.init0();
        mainPanel.fillHorizontal();
        mainPanel.setInsets(SPACE, SPACE, 0, SPACE);
        mainPanel.addRow(buildUpperButtonPanel());
        mainPanel.add(myProviderPanel);
        mainPanel.addRow(myProviderPanel);
        mainPanel.addRow(buildColorOpacityPanel());
        mainPanel.addRow(getTileLevelControlPanel());
        myCustomControlsPanel = Box.createHorizontalBox();
        mainPanel.addRow(myCustomControlsPanel);
        mainPanel.setInsets(SPACE, SPACE, SPACE, SPACE);
        mainPanel.addRow(buildMiniStyleBox());
        return mainPanel;
    }

    /**
     * Create the button that sets the animation time to match the layer's time
     * extends.
     *
     * @return the time span button.
     */
    private IconButton buildLayerTimeSpanButton()
    {
        myLayerTimeSpanButton = new IconButton();
        IconUtil.setIcons(myLayerTimeSpanButton, "/images/arrows-collapse.png");
        myLayerTimeSpanButton.setToolTipText("Set Animation Time Span To Match Layer Span");
        myLayerTimeSpanButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.extents-button");
                DataTypeInfo dti = getSelectedDataType();
                DataGroupInfo dgi = getSelectedDataGroup();
                TimeSpan extent = null;
                if (dti != null && dti.getTimeExtents() != null)
                {
                    extent = dti.getTimeExtents().getExtent();
                }
                else if (dgi != null)
                {
                    extent = DataGroupInfoUtilities.findTimeExtentFromDGICollection(Collections.singleton(dgi));
                }
                if (extent != null && !extent.isTimeless())
                {
                    getToolbox().getEventManager()
                            .publishEvent(new AnimationChangeExtentRequestEvent(extent, ActiveLayerControlPanel.this));
                }
            }
        });

        return myLayerTimeSpanButton;
    }

    /**
     * Create the button to include a layer in the analyze tools.
     *
     * @return the button.
     */
    private IconToggleButton buildAnalyzeButton()
    {
        myAnalyzeButton = new IconToggleButton();
        IconUtil.setIcons(myAnalyzeButton, IconType.STATS);
        myAnalyzeButton.setToolTipText("Whether to include the layer in the analysis tools.");
        myAnalyzeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.include-in-analysis-tools");
                LoadsToUtilities.setIncludeInAnalyze(getSelectedLayer(), myAnalyzeButton.isSelected());
            }
        });
        myAnalyzeButton.setMargin(new Insets(1, 1, 1, 1));

        return myAnalyzeButton;
    }

    /**
     * Create the button to include a layer in the timeline.
     *
     * @return the button.
     */
    private IconToggleButton buildTimelineButton()
    {
        myTimelineButton = new IconToggleButton();
        IconUtil.setIcons(myTimelineButton, IconType.CLOCK);
        myTimelineButton.setToolTipText("Whether to include the layer in the timeline.");
        myTimelineButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.include-on-timeline");
                LoadsToUtilities.setIncludeInTimeline(getSelectedLayer(), myTimelineButton.isSelected());
            }
        });
        myTimelineButton.setMargin(new Insets(1, 1, 1, 1));

        return myTimelineButton;
    }

    /**
     * Determines the visibility of the edit button.
     */
    private void determineEditButtonVisibility()
    {
        boolean isVisible = true;
        Collection<DataTypeInfo> selectedTypes = getSelectedDataTypes();
        if (selectedTypes != null && !selectedTypes.isEmpty())
        {
            for (DataTypeInfo dataType : selectedTypes)
            {
                isVisible = dataType.isEditable();
            }
        }
        else
        {
            isVisible = false;
        }
        myFeatureEditButton.setVisible(isVisible);
    }

    /**
     * Determine if the layer details button should be visible.
     */
    private void determineLayerDetailsButtonVisibility()
    {
        boolean visible = false;
        Collection<DataGroupInfo> selectedGroups = getSelectedDataGroups();
        if (selectedGroups != null && !selectedGroups.isEmpty())
        {
            for (DataGroupInfo group : selectedGroups)
            {
                if (group.hasDetails())
                {
                    visible = true;
                    break;
                }
            }
        }
        Collection<DataTypeInfo> selectedTypes = getSelectedDataTypes();
        if (!visible && selectedTypes != null && !selectedTypes.isEmpty())
        {
            for (DataTypeInfo dataType : selectedTypes)
            {
                if (dataType.hasDetails())
                {
                    visible = true;
                    break;
                }
            }
        }
        myLayerDetailsButton.setVisible(visible);
    }

    /**
     * Determine style short cut button visibility.
     */
    private void determineStyleShortCutButtonVisibility()
    {
        boolean heatmapVisStyleVisible = false;
        boolean tileVisStyleVisible = false;
        boolean featureVisStyleVisible = false;
        boolean hasTimeExtents = false;
        TimeSpan extent = null;
        DataTypeInfo selectedType = getSelectedDataType();
        DataGroupInfo selectedGroup = getSelectedDataGroup();
        if (selectedType != null)
        {
            if (selectedType.getMapVisualizationInfo() != null
                    && selectedType.getMapVisualizationInfo().usesVisualizationStyles())
            {
                heatmapVisStyleVisible = selectedType.getMapVisualizationInfo().getVisualizationType().isHeatmapType();
                tileVisStyleVisible = selectedType.getMapVisualizationInfo().isImageTileType()
                        || selectedType.getMapVisualizationInfo().isImageType();
                featureVisStyleVisible = selectedType.getMapVisualizationInfo().usesMapDataElements();
            }
            hasTimeExtents = selectedType.getTimeExtents() != null && isValidTimeRange(selectedType.getTimeExtents().getExtent());
            if (hasTimeExtents)
            {
                extent = selectedType.getTimeExtents().getExtent();
            }
        }
        else if (selectedGroup != null)
        {
            if (selectedGroup.usesStyles(false))
            {
                tileVisStyleVisible = selectedGroup.hasImageTileTypes(false);
                featureVisStyleVisible = selectedGroup.hasFeatureTypes(false);
            }
            extent = DataGroupInfoUtilities.findTimeExtentFromDGICollection(Collections.singleton(selectedGroup));
            hasTimeExtents = extent != null && isValidTimeRange(extent);
        }
        myFeatureVisStyleButton.setVisible(featureVisStyleVisible);
        myTileVisStyleButton.setVisible(tileVisStyleVisible);
        myHeatmapVisStyleButton.setVisible(heatmapVisStyleVisible);
        myLayerTimeSpanButton.setVisible(hasTimeExtents);
        if (extent != null)
        {
            myLayerTimeSpanButton.setToolTipText("Set Animation Time Span To: " + extent.toDisplayString());
        }
        else
        {
            myLayerTimeSpanButton.setToolTipText("");
        }
    }

    /**
     * Gets the export button.
     *
     * @return the export button
     */
    private JButton buildExportButton()
    {
        myExportButton = new SplitButton(null, null);
        IconUtil.setIcons(myExportButton, IconType.EXPORT);
        myExportButton.setToolTipText("Export the selected layers");
        myExportButton.setAlwaysPopup(true);

        return myExportButton;
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton buildFeatureEditButton()
    {
        myFeatureEditButton = new IconButton(IconType.EDIT);
        myFeatureEditButton.setToolTipText("Edit Features");
        myFeatureEditButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.edit-features");
                Collection<DataTypeInfo> selectedTypes = getSelectedDataTypes();
                if (CollectionUtilities.hasContent(selectedTypes))
                {
                    DataGroupInfo selectedGroup = selectedTypes.iterator().next().getParent();
                    selectedTypes.iterator().next().launchEditor(selectedGroup, selectedTypes);
                }
            }
        });

        return myFeatureEditButton;
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton buildFeatureVisStyleButton()
    {
        myFeatureVisStyleButton = new IconButton();
        IconUtil.setIcons(myFeatureVisStyleButton, "/images/cog-feature.png");
        myFeatureVisStyleButton.setToolTipText(VisualizationStyleControlDialog.TITLE + " For Features");
        myFeatureVisStyleButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.show-feature-styles");
                Set<DataTypeInfo> found = null;
                DataTypeInfo selectedType = getSelectedDataType();
                DataGroupInfo selectedGroup = getSelectedDataGroup();
                if (selectedType != null)
                {
                    found = Collections.singleton(selectedType);
                }
                else if (selectedGroup != null)
                {
                    found = selectedGroup.findMembers(new Predicate<DataTypeInfo>()
                    {
                        @Override
                        public boolean test(DataTypeInfo value)
                        {
                            return value != null && value.getMapVisualizationInfo() != null
                                    && value.getMapVisualizationInfo().usesMapDataElements();
                        }
                    }, false, true);
                }
                if (found != null && !found.isEmpty())
                {
                    ShowTypeVisualizationStyleEvent event = new ShowTypeVisualizationStyleEvent(found.iterator().next(),
                            ActiveLayerControlPanel.this);
                    getToolbox().getEventManager().publishEvent(event);
                }
            }
        });

        return myFeatureVisStyleButton;
    }

    /**
     * Gets the layer details button.
     *
     * @return the layer details button
     */
    private JButton buildLayerDetailsButton()
    {
        myLayerDetailsButton = new IconButton(IconType.COG);
        myLayerDetailsButton.setToolTipText("Show layer details panel for layer");
        myLayerDetailsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.show-layer-details");
                DataGroupInfo dgi = getSelectedDataGroup();
                if (dgi != null)
                {
                    myLayerDetailsCoordinator.showLayerDetailsForGroup(dgi, null);
                }
            }
        });

        return myLayerDetailsButton;
    }

    /**
     * Gets the close selection button.
     *
     * @return te close selection button
     */
    private JButton buildCloseSelectionButton()
    {
        myCloseSelectionButton = new IconButton(IconType.CLOSE);
        myCloseSelectionButton.setToolTipText("Closes these style options");
        myCloseSelectionButton.addActionListener(evt ->
        {
            Quantify.collectMetric("mist3d.layer-manager.lower-controls.close-selected-styles");
            setSelected(new LayerSelectedEvent(false, null, null));
        });

        return myCloseSelectionButton;
    }

    /**
     * Gets the mini style box.
     *
     * @return the mini style box
     */
    private JComponent buildMiniStyleBox()
    {
        return myMiniStyleBox = new VerticalList();
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton buildTileVisStyleButton()
    {
        myTileVisStyleButton = new IconButton();
        IconUtil.setIcons(myTileVisStyleButton, "/images/cog-tile.png");
        myTileVisStyleButton.setToolTipText(VisualizationStyleControlDialog.TITLE + " For Tiles");
        myTileVisStyleButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.show-tile-styles");
                Set<DataTypeInfo> found = null;
                DataTypeInfo selectedType = getSelectedDataType();
                DataGroupInfo selectedGroup = getSelectedDataGroup();
                if (selectedType != null)
                {
                    found = Collections.singleton(selectedType);
                }
                else if (selectedGroup != null)
                {
                    found = selectedGroup.findMembers(new Predicate<DataTypeInfo>()
                    {
                        @Override
                        public boolean test(DataTypeInfo value)
                        {
                            return value != null && value.getMapVisualizationInfo() != null
                                    && value.getMapVisualizationInfo().isImageTileType();
                        }
                    }, false, true);
                }
                if (found != null && !found.isEmpty())
                {
                    ShowTypeVisualizationStyleEvent event = new ShowTypeVisualizationStyleEvent(found.iterator().next(),
                            ActiveLayerControlPanel.this);
                    getToolbox().getEventManager().publishEvent(event);
                }
            }
        });

        return myTileVisStyleButton;
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton buildHeatmapVisStyleButton()
    {
        myHeatmapVisStyleButton = new IconButton();
        IconUtil.setIcons(myHeatmapVisStyleButton, "/images/cog-heatmap.png");
        myHeatmapVisStyleButton.setToolTipText(VisualizationStyleControlDialog.TITLE + " For Heatmaps");
        myHeatmapVisStyleButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Quantify.collectMetric("mist3d.layer-manager.lower-controls.show-heatmap-styles");
                Set<DataTypeInfo> found = null;
                DataTypeInfo selectedType = getSelectedDataType();
                DataGroupInfo selectedGroup = getSelectedDataGroup();
                if (selectedType != null)
                {
                    found = Collections.singleton(selectedType);
                }
                else if (selectedGroup != null)
                {
                    found = selectedGroup.findMembers(new Predicate<DataTypeInfo>()
                    {
                        @Override
                        public boolean test(DataTypeInfo value)
                        {
                            return value != null && value.getMapVisualizationInfo() != null
                                    && value.getMapVisualizationInfo().getVisualizationType().isHeatmapType();
                        }
                    }, false, true);
                }
                if (found != null && !found.isEmpty())
                {
                    ShowTypeVisualizationStyleEvent event = new ShowTypeVisualizationStyleEvent(found.iterator().next(),
                            ActiveLayerControlPanel.this);
                    getToolbox().getEventManager().publishEvent(event);
                }
            }
        });

        return myHeatmapVisStyleButton;
    }

    /**
     * Gets the upper button panel.
     *
     * @return the upper button panel
     */
    private GridBagPanel buildUpperButtonPanel()
    {
        myUpperButtonPanel = new GridBagPanel();
        myUpperButtonPanel.setInsets(0, 0, 0, SPACE);

        myUpperButtonPanel.add(buildFeatureVisStyleButton());
        myUpperButtonPanel.add(buildTileVisStyleButton());
        myUpperButtonPanel.add(buildHeatmapVisStyleButton());
        myUpperButtonPanel.add(buildLayerTimeSpanButton());
        myUpperButtonPanel.add(buildFeatureEditButton());

        myUpperButtonPanel.fillHorizontalSpace().fillNone();

        myUpperButtonPanel.add(buildAnalyzeButton());
        myUpperButtonPanel.add(buildTimelineButton());
        myUpperButtonPanel.add(buildExportButton());

        myUpperButtonPanel.setInsets(0, 0, 0, 0);

        myUpperButtonPanel.add(buildLayerDetailsButton());
        myUpperButtonPanel.add(buildCloseSelectionButton());

        return myUpperButtonPanel;
    }

    /**
     * Handles a change in LoadsTo for all layers.
     *
     * @param event the event
     */
    private void handleLoadsToChange(DataTypeInfoLoadsToChangeEvent event)
    {
        EventQueue.invokeLater(() ->
        {
            if (Utilities.sameInstance(event.getDataTypeInfo(), getSelectedLayer()))
            {
                myAnalyzeButton.setSelected(LoadsToUtilities.isAnalyzeEnabled(event.getDataTypeInfo()));
                myTimelineButton.setSelected(LoadsToUtilities.isTimelineEnabled(event.getDataTypeInfo()));
            }
        });
    }

    /**
     * Checks if is valid time range.
     *
     * @param extent the extent
     * @return true, if is valid time range
     */
    private boolean isValidTimeRange(TimeSpan extent)
    {
        return extent != null && !extent.isZero() && !extent.isTimeless() && extent.isBounded() && !extent.isInstantaneous();
    }

    /**
     * Rebuild mini style box.
     *
     * @param dgi the dgi
     * @param dti the dti
     */
    private void rebuildMiniStyleBox(DataGroupInfo dgi, DataTypeInfo dti)
    {
        myMiniStyleBox.removeAll();
        if (dgi != null && dti != null)
        {
            if (dti.getMapVisualizationInfo() != null && dti.getMapVisualizationInfo().usesVisualizationStyles()
                    && !MapVisualizationType.TERRAIN_TILE.equals(dti.getMapVisualizationInfo().getVisualizationType()))
            {
                MiniStylePanel miniStylePanel = new MiniStylePanel(getToolbox(), dgi, dti);
                myMiniStyleBox.add(miniStylePanel);
            }
            else
            {
                Box hBox = Box.createHorizontalBox();
                hBox.add(Box.createHorizontalGlue());
                hBox.add(new JLabel("No Style Control For Layer"));
                hBox.add(Box.createHorizontalGlue());
                myMiniStyleBox.add(hBox);
            }
        }
        myMiniStyleBox.revalidate();
        myMiniStyleBox.repaint();
    }

    /**
     * Rebuild the provider panel.
     *
     * @param uniqueProvider the provider component.
     */
    private void rebuildProviderLayerControl(Component uniqueProvider)
    {
        myProviderPanel.removeAll();
        GridBagPanel providerPanel = new GridBagPanel();
        JLabel textField = new JLabel("Unique ID Column: ");
        providerPanel.add(textField);
        providerPanel.fillHorizontal();

        uniqueProvider.setMinimumSize(new Dimension(100, 24));
        providerPanel.add(uniqueProvider);

        myProviderPanel.setVisible(true);
        myProviderPanel.add(providerPanel);
        myProviderPanel.revalidate();
        myProviderPanel.repaint();
    }

    /**
     * Gets the selected layer.
     *
     * @return the selected layer
     */
    private DataTypeInfo getSelectedLayer()
    {
        DataTypeInfo selectedType = getSelectedDataType();
        DataGroupInfo selectedGroup = getSelectedDataGroup();
        if (selectedType == null && selectedGroup != null)
        {
            Collection<DataTypeInfo> types = selectedGroup.getMembers(false);
            if (types.size() == 1)
            {
                selectedType = types.iterator().next();
            }
        }
        return selectedType;
    }
}
