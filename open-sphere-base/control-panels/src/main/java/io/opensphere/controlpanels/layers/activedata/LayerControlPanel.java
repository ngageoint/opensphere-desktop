package io.opensphere.controlpanels.layers.activedata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import io.opensphere.controlpanels.layers.activedata.controller.LayerColorController;
import io.opensphere.controlpanels.layers.activedata.controller.OpacityColorAdapter;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.HighlightedBorder;
import io.opensphere.core.util.swing.LinkedSliderTextField;
import io.opensphere.core.util.swing.LinkedSliderTextField.PanelSizeParameters;
import io.opensphere.core.util.swing.input.model.ColorModel;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TileLevelController;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.DefaultTileLevelController;
import io.opensphere.mantle.gui.color.DataTypeColorChooser;

/**
 * The unnecessary base class of the lower panel of the Active Layers panel.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class LayerControlPanel extends AbstractHUDPanel
{
    /** The component background color. */
    private static final Color ourComponentBackground = ColorUtilities
            .opacitizeColor(AbstractHUDPanel.ourComponentBackgroundColor, 100);

    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Data type visibility change event listener. */
    private EventListener<DataTypeVisibilityChangeEvent> myDataTypeVisibilityChangeEventListener;

    /** The feature color model. */
    private final ColorModel myFeatureColorModel;

    /** The feature opacity model. */
    private final OpacityColorAdapter myFeatureOpacityModel;

    /** The Feature color button. */
    private final DataTypeColorChooser myFeatureColorButton;

    /** The Feature color panel. */
    private final JPanel myFeatureColorPanel;

    /** The layer color controller. */
    private final LayerColorController myLayerColorController;

    /** The Opacity panel's action listener. */
    private ActionListener myOpacityPanelActionListener;

    /** The selected data groups. */
    private Collection<DataGroupInfo> mySelectedDataGroups;

    /** The selected data types. */
    private Collection<DataTypeInfo> mySelectedDataTypes;

    /** The opacity panel. */
    private LinkedSliderTextField myOpacityPanel;

    /** The Tile level spinner label. */
    private JLabel mySpinnerLabel;

    /** The Tile level control panel. */
    private JPanel myTileLevelControlPanel;

    /** The Tile level hold check box. */
    private JCheckBox myTileLevelHoldCheckBox;

    /** The Tile level spinner. */
    private JSpinner myTileLevelSpinner;

    /** The Tile level spinner panel. */
    private JPanel myTileLevelSpinnerPanel;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Instantiates a new layer control panel.
     *
     * @param toolbox the toolbox
     */
    public LayerControlPanel(Toolbox toolbox)
    {
        super(toolbox.getPreferencesRegistry());
        myToolbox = toolbox;

        myFeatureColorModel = new ColorModel();
        myFeatureColorModel.set(Color.WHITE);
        myFeatureOpacityModel = new OpacityColorAdapter(myFeatureColorModel);
        myFeatureColorButton = new DataTypeColorChooser(null, myFeatureColorModel);
        myFeatureColorPanel = new JPanel(new BorderLayout());
        myFeatureColorPanel.setOpaque(false);
        myFeatureColorPanel.add(myFeatureColorButton, BorderLayout.CENTER);
        myLayerColorController = new LayerColorController(myFeatureColorModel, myToolbox.getEventManager());

        initialize();
    }

    /**
     * Gets the feature color panel.
     *
     * @return the feature color panel
     */
    public JPanel getFeatureColorPanel()
    {
        return myFeatureColorPanel;
    }

    /**
     * Gets the opacity panel.
     *
     * @return the opacity panel
     */
    public LinkedSliderTextField getOpacityPanel()
    {
        if (myOpacityPanel == null)
        {
            myOpacityPanel = new LinkedSliderTextField("Opacity", 0, 100, myFeatureOpacityModel.get().intValue(),
                    new PanelSizeParameters(35, 24, 0));
            myFeatureOpacityModel.addListener(new io.opensphere.core.util.ChangeListener<Integer>()
            {
                @Override
                public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
                {
                    int modelOpacity = myFeatureOpacityModel.get().intValue();
                    if (myOpacityPanel.getSliderValue() != modelOpacity)
                    {
                        myOpacityPanel.setValues(modelOpacity);
                    }
                }
            });
            myOpacityPanelActionListener = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myFeatureOpacityModel.set(Integer.valueOf(myOpacityPanel.getSliderValue()));
                }
            };
            myOpacityPanel.addSliderFieldChangeListener(myOpacityPanelActionListener);
        }
        return myOpacityPanel;
    }

    /**
     * Gets the selected data group.
     *
     * @return the selected data group
     */
    public final DataGroupInfo getSelectedDataGroup()
    {
        return CollectionUtilities.hasContent(mySelectedDataGroups) ? mySelectedDataGroups.iterator().next() : null;
    }

    /**
     * Gets the selected data groups.
     *
     * @return the selected data groups
     */
    public final Collection<DataGroupInfo> getSelectedDataGroups()
    {
        return mySelectedDataGroups;
    }

    /**
     * Gets the selected data type.
     *
     * @return the selected data type
     */
    public final DataTypeInfo getSelectedDataType()
    {
        return CollectionUtilities.hasContent(mySelectedDataTypes) ? mySelectedDataTypes.iterator().next() : null;
    }

    /**
     * Gets the selected data types.
     *
     * @return the selected data types
     */
    public final Collection<DataTypeInfo> getSelectedDataTypes()
    {
        return mySelectedDataTypes;
    }

    /**
     * Accessor for the toolbox.
     *
     * @return The toolbox.
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Handle data type visibility change.
     *
     * @param event the event
     */
    private void handleDataTypeVisibilityChange(final DataTypeVisibilityChangeEvent event)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            DataGroupInfo selectedGroup = getSelectedDataGroup();
            if (selectedGroup != null && selectedGroup.hasMember(event.getDataTypeInfo(), false))
            {
                updateTileLevelControlPanel();
            }
        });
    }

    /**
     * Sets the selected data groups and types.
     *
     * @param selectedDataGroups the selected data groups
     * @param selectedDataTypes the selected data types
     */
    public final void setSelectedItems(Collection<DataGroupInfo> selectedDataGroups, Collection<DataTypeInfo> selectedDataTypes)
    {
        mySelectedDataGroups = selectedDataGroups;
        mySelectedDataTypes = selectedDataTypes;
        myLayerColorController.setSelectedItems(mySelectedDataGroups, mySelectedDataTypes);
        DataTypeInfo selectedDataType = myLayerColorController.getSelectedDataType();
        if (selectedDataType != null)
        {
            myFeatureColorButton.setDefaultColor(selectedDataType.getBasicVisualizationInfo().getDefaultTypeColor());
        }
    }

    /**
     * Gets the tile hold level check box.
     *
     * @return the tile hold level check box
     */
    private JCheckBox getTileHoldLevelCheckBox()
    {
        if (myTileLevelHoldCheckBox == null)
        {
            myTileLevelHoldCheckBox = new JCheckBox("Hold Level", false);
            myTileLevelHoldCheckBox.setFocusable(false);
            myTileLevelHoldCheckBox.setBorder(null);
            myTileLevelHoldCheckBox.addActionListener(e ->
            {
                Set<TileLevelController> tileLevelControllers = getTileLevelControllers();
                for (TileLevelController controller : tileLevelControllers)
                {
                    if (controller.getMaxGeneration() == -1)
                    {
                        continue;
                    }
                    controller.setDivisionOverride(myTileLevelHoldCheckBox.isSelected());
                    try
                    {
                        if (controller instanceof DefaultTileLevelController)
                        {
                            DefaultTileLevelController defaultController = (DefaultTileLevelController)controller;
                            getTileLevelSpinner().setModel(new SpinnerNumberModel(defaultController.getCurrentHoldLevel(),
                                    defaultController.getMinimumHoldLevel(), defaultController.getMaxGeneration(), 1));
                        }
                        else
                        {
                            getTileLevelSpinner().setModel(
                                    new SpinnerNumberModel(controller.getDivisionHoldGeneration(), 0, controller.getMaxGeneration(), 1));
                        }
                    }
                    catch (IllegalArgumentException ex)
                    {
                        getTileLevelSpinner()
                                .setModel(new SpinnerNumberModel(controller.getMaxGeneration(), 0, controller.getMaxGeneration(), 1));
                    }
                }
                getTileLevelSpinner().setEnabled(myTileLevelHoldCheckBox.isSelected());
                getSpinnerLabel().setEnabled(myTileLevelHoldCheckBox.isSelected());
            });
            myTileLevelHoldCheckBox.setEnabled(false);
            getTileLevelSpinner().setEnabled(false);
            getSpinnerLabel().setEnabled(false);
        }
        return myTileLevelHoldCheckBox;
    }

    /**
     * Gets the tile level controller.
     *
     * @return the tile level controller
     */
    private Set<TileLevelController> getTileLevelControllers()
    {
        Set<TileLevelController> tileLevelControllers = New.set();
        DataTypeInfo selectedType = getSelectedDataType();
        DataGroupInfo selectedGroup = getSelectedDataGroup();
        if (selectedType != null)
        {
            if (selectedType.getMapVisualizationInfo() != null
                    && selectedType.getMapVisualizationInfo().getTileLevelController() != null)
            {
                tileLevelControllers.add(selectedType.getMapVisualizationInfo().getTileLevelController());
            }
        }
        else if (selectedGroup != null)
        {
            for (DataTypeInfo dti : selectedGroup.getMembers(false))
            {
                if (dti.getMapVisualizationInfo() != null && dti.getMapVisualizationInfo().getTileLevelController() != null)
                {
                    tileLevelControllers.add(dti.getMapVisualizationInfo().getTileLevelController());
                }
            }
        }
        return tileLevelControllers;
    }

    /**
     * Gets the tile level control panel.
     *
     * @return the tile level control panel
     */
    protected JPanel getTileLevelControlPanel()
    {
        if (myTileLevelControlPanel == null)
        {
            myTileLevelControlPanel = new JPanel(new BorderLayout());
            myTileLevelControlPanel.setBackground(getBackground());
            myTileLevelControlPanel.add(getTileHoldLevelCheckBox(), BorderLayout.WEST);
            myTileLevelControlPanel.add(getTileLevelSpinnerPanel(), BorderLayout.CENTER);
            getTileLevelSpinnerPanel().setVisible(true);
        }
        return myTileLevelControlPanel;
    }

    /**
     * Gets the tile level spinner.
     *
     * @return the tile level spinner
     */
    private JSpinner getTileLevelSpinner()
    {
        if (myTileLevelSpinner == null)
        {
            myTileLevelSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
            myTileLevelSpinner.setBackground(ourComponentBackground);
            myTileLevelSpinner.setFocusable(false);
            myTileLevelSpinner.addChangeListener(e ->
            {
                Object spinnerValue = myTileLevelSpinner.getValue();
                if (spinnerValue instanceof Number)
                {
                    Number spinnerNumber = (Number)spinnerValue;
                    for (TileLevelController controller : getTileLevelControllers())
                    {
                        controller.setDivisionHoldGeneration(spinnerNumber.intValue());
                        if (controller instanceof DefaultTileLevelController)
                        {
                            ((DefaultTileLevelController)controller).setCurrentHoldLevel(spinnerNumber.intValue());
                        }
                    }
                }
            });
            myTileLevelSpinner.setEnabled(false);
        }
        return myTileLevelSpinner;
    }

    /**
     * Gets the tile level spinner panel.
     *
     * @return the tile level spinner panel
     */
    private JPanel getTileLevelSpinnerPanel()
    {
        if (myTileLevelSpinnerPanel == null)
        {
            myTileLevelSpinnerPanel = new JPanel(new BorderLayout());
            myTileLevelSpinnerPanel.setBackground(ourComponentBackground);
            myTileLevelSpinnerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            myTileLevelSpinnerPanel.add(getSpinnerLabel(), BorderLayout.WEST);
            myTileLevelSpinnerPanel.add(getTileLevelSpinner(), BorderLayout.CENTER);
            myTileLevelSpinnerPanel.setFocusable(false);
        }
        return myTileLevelSpinnerPanel;
    }

    /**
     * Checks for active tile data type.
     *
     * @param dti the dti
     * @return true, if successful
     */
    private boolean hasActiveTileDataType(DataTypeInfo dti)
    {
        return dti.isVisible() && dti.getMapVisualizationInfo() != null
                && (dti.getMapVisualizationInfo().isImageType() || dti.getMapVisualizationInfo().isImageTileType());
    }

    /**
     * Checks for active tile data types.
     *
     * @return true, if successful
     */
    private boolean hasActiveTileDataTypes()
    {
        boolean hasActiveTileDataTypes = false;
        DataTypeInfo selectedType = getSelectedDataType();
        if (selectedType != null)
        {
            return hasActiveTileDataType(selectedType);
        }
        else
        {
            DataGroupInfo selectedGroup = getSelectedDataGroup();
            if (selectedGroup != null)
            {
                Collection<DataTypeInfo> memberSet = selectedGroup.getMembers(false);
                if (memberSet != null && !memberSet.isEmpty())
                {
                    for (DataTypeInfo dti : memberSet)
                    {
                        if (hasActiveTileDataType(dti))
                        {
                            hasActiveTileDataTypes = true;
                            break;
                        }
                    }
                }
            }
        }
        return hasActiveTileDataTypes;
    }

    /**
     * Update tile level control panel.
     */
    protected void updateTileLevelControlPanel()
    {
        final Set<TileLevelController> tileLevelControllers = getTileLevelControllers();
        if (!tileLevelControllers.isEmpty())
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                TileLevelController firstController = tileLevelControllers.iterator().next();
                boolean hasActiveTypes = hasActiveTileDataTypes();
                getTileHoldLevelCheckBox().setEnabled(firstController != null && hasActiveTypes);
                if (firstController != null && hasActiveTypes)
                {
                    getTileHoldLevelCheckBox().setSelected(firstController.isDivisionOverride());
                    int currentGeneration = firstController.getCurrentGeneration();
                    if (currentGeneration > firstController.getMaxGeneration() && firstController.getMaxGeneration() >= 0)
                    {
                        tileLevelControllers.forEach(c -> c.setDivisionHoldGeneration(firstController.getMaxGeneration()));
                    }

                    if (firstController instanceof DefaultTileLevelController)
                    {
                        DefaultTileLevelController defaultController = (DefaultTileLevelController)firstController;
                        getTileLevelSpinner().setModel(new SpinnerNumberModel(defaultController.getCurrentHoldLevel(),
                                defaultController.getMinimumHoldLevel(), defaultController.getMaxGeneration(), 1));
                    }
                    else
                    {
                        int spinnerMax = firstController.getMaxGeneration() > 0 ? firstController.getMaxGeneration() : 1;
                        getTileLevelSpinner().setModel(new SpinnerNumberModel(currentGeneration, 0, spinnerMax, 1));
                    }
                    getTileLevelSpinner().setEnabled(firstController.getMaxGeneration() != 0 && getTileHoldLevelCheckBox().isSelected());
                    getSpinnerLabel().setEnabled(firstController.getMaxGeneration() != 0 && getTileHoldLevelCheckBox().isSelected());
                    getTileLevelControlPanel().setVisible(true);
                }
                else
                {
                    getTileHoldLevelCheckBox().setSelected(false);
                    getTileLevelSpinner().setEnabled(false);
                    getSpinnerLabel().setEnabled(false);
                    getTileLevelControlPanel().setVisible(false);
                }
            });
        }
        else
        {
            EventQueueUtilities.runOnEDT(() ->
            {
                getTileLevelSpinner().setEnabled(false);
                getTileHoldLevelCheckBox().setEnabled(false);
                getSpinnerLabel().setEnabled(false);
                getTileLevelControlPanel().setVisible(false);
            });
        }
    }

    /**
     * Accessor for the tile level spinner label.
     *
     * @return The tile level spinner label.
     */
    private JLabel getSpinnerLabel()
    {
        if (mySpinnerLabel == null)
        {
            mySpinnerLabel = new JLabel("Level");
            mySpinnerLabel.setEnabled(false);
        }
        return mySpinnerLabel;
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setSize(new Dimension(220, 135));
        setMinimumSize(getSize());
        setPreferredSize(getSize());

        myFeatureOpacityModel.open();
        myFeatureColorButton.open();
        myLayerColorController.open();

        HighlightedBorder hb = new HighlightedBorder(BorderFactory.createLineBorder(getBorderColor(), 1), "Layer Control",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, getTitleFont(), Color.BLACK,
                getBorderHighlightColor());
        hb.setWidthOffset(3);
        hb.setXOffset(-1);
        setBorder(hb);
        myDataTypeVisibilityChangeEventListener = new EventListener<DataTypeVisibilityChangeEvent>()
        {
            @Override
            public void notify(DataTypeVisibilityChangeEvent event)
            {
                handleDataTypeVisibilityChange(event);
            }
        };
        myToolbox.getEventManager().subscribe(DataTypeVisibilityChangeEvent.class, myDataTypeVisibilityChangeEventListener);
    }
}
