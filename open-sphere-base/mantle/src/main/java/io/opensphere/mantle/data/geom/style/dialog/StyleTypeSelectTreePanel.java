package io.opensphere.mantle.data.geom.style.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleDatatypeChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry.VisualizationStyleRegistryChangeListener;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeType;
import io.opensphere.mantle.data.geom.style.dialog.StyleEditPanelController.StyleEditPanelControllerListener;
import io.opensphere.mantle.data.geom.style.impl.VisualizationStyleRegistryChangeAdapter;
import io.opensphere.mantle.data.tile.InterpolatedTileVisualizationSupport;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class StyleTypeSelectTreePanel.
 */
@SuppressWarnings("PMD.GodClass")
public class StyleTypeSelectTreePanel extends JPanel implements StyleEditPanelControllerListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Data type. */
    private final transient DataTypeNodeUserObject myDataType;

    /** The Main panel in scroll. */
    private final JPanel myMainPanelInScroll;

    /** The Registry change listener. */
    private transient VisualizationStyleRegistryChangeListener myRegistryChangeListener;

    /** The Scroll pane. */
    @SuppressWarnings("PMD.SingularField")
    private final JScrollPane myScrollPane;

    /** The Select label button group. */
    private final transient SelectableLabelGroup mySelectLabelButtonGroup;

    /** The Style edit panel controller. */
    private final transient StyleEditPanelController myStyleEditPanelController;

    /** The Type choice panels. */
    private final Set<TypeChoicePanel> myTypeChoicePanels;

    /**
     * Instantiates a new style type select tree panel.
     *
     * @param tb the tb
     * @param editPanelController the edit panel controller
     * @param dataType the data type
     */
    public StyleTypeSelectTreePanel(Toolbox tb, StyleEditPanelController editPanelController, DataTypeNodeUserObject dataType)
    {
        super(new BorderLayout());
        myStyleEditPanelController = editPanelController;
        myDataType = dataType;
        setMinimumSize(new Dimension(210, 10));
        setPreferredSize(new Dimension(210, 10));
        setBorder(BorderFactory.createRaisedBevelBorder());
        mySelectLabelButtonGroup = new SelectableLabelGroup();
        myTypeChoicePanels = New.set();
        myMainPanelInScroll = new JPanel(new BorderLayout());
        myScrollPane = new JScrollPane(myMainPanelInScroll);
        switch (dataType.getNodeType())
        {
            case DEFAULT_ROOT_FEATURE:
            case FEATURE_TYPE_LEAF:
                buildFeatureTypePanels(mySelectLabelButtonGroup);
                break;
            case DEFAULT_ROOT_TILE:
            case TILE_TYPE_LEAF:
                buildTileTypePanels(mySelectLabelButtonGroup);
                break;
            case DEFAULT_ROOT_HEATMAP:
            case HEATMAP_TYPE_LEAF:
                buildHeatmapTypePanels(mySelectLabelButtonGroup);
                break;
            default:
                throw new UnsupportedOperationException("Node type not supported: " + dataType.getNodeType());
        }

        add(myScrollPane, BorderLayout.CENTER);
        JTextArea jta = new JTextArea();
        jta.setText("Select name to edit style.\n\nSelect radio button to chose style type.");
        jta.setWrapStyleWord(true);
        jta.setLineWrap(true);
        jta.setPreferredSize(new Dimension(200, 80));
        jta.setMaximumSize(new Dimension(200, 80));
        jta.setBorder(BorderFactory.createEmptyBorder(6, 10, 15, 10));
        jta.setEditable(false);
        jta.setBackground(getBackground());
        add(jta, BorderLayout.SOUTH);
        editPanelController.addListener(this);

        MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleRegistry()
                .addVisualizationStyleRegistryChangeListener(getStyleRegistryChangeListener());
    }

    /**
     * Ensure label selected.
     *
     * @param uo the {@link StyleNodeUserObject}
     */
    public void ensureLabelSelected(final StyleNodeUserObject uo)
    {
        myTypeChoicePanels.forEach(t -> t.selectLabelNoEventIfMatch(uo));
    }

    @Override
    public void lockFromChanges(boolean lock)
    {
        setEnabled(!lock);
    }

    @Override
    public void refreshDisplay()
    {
        mySelectLabelButtonGroup.removeAllLabels();
        if (myDataType.getNodeType() == NodeType.DEFAULT_ROOT_FEATURE || myDataType.getNodeType() == NodeType.FEATURE_TYPE_LEAF)
        {
            buildFeatureTypePanels(mySelectLabelButtonGroup);
        }
        else
        {
            buildTileTypePanels(mySelectLabelButtonGroup);
        }
        revalidate();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        setChildrenEnabled(myMainPanelInScroll.getComponents(), enabled);
    }

    @Override
    public void styleEditSelectionChanged(StyleNodeUserObject styleToEdit, final FeatureVisualizationControlPanel editorPanel)
    {
    }

    /**
     * Builds the type panels.
     *
     * @param slBG the SelectableLabelGroup
     */
    private void buildFeatureTypePanels(SelectableLabelGroup slBG)
    {
        myMainPanelInScroll.removeAll();
        myTypeChoicePanels.clear();
        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Style");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, titleLabel.getFont().getSize() + 2));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.setMaximumSize(new Dimension(200, 20));
        typePanel.add(titlePanel);

        List<Class<? extends VisualizationSupport>> featureTypes = myStyleEditPanelController.getPrimaryFeatureClasses();

        TypeChoicePanel firstTCP = null;
        for (Class<? extends VisualizationSupport> featureClass : featureTypes)
        {
            String catName = StyleManagerUtils.getStyleCategoryNameForFeatureClass(featureClass);
            TypeChoicePanel tpn = getFeaturePanel(catName, slBG, featureClass);
            if (firstTCP == null)
            {
                firstTCP = tpn;
            }
            myTypeChoicePanels.add(tpn);
            typePanel.add(tpn);
        }

        myMainPanelInScroll.add(typePanel, BorderLayout.CENTER);

        if (firstTCP != null)
        {
            final TypeChoicePanel fTCP = firstTCP;
            EventQueueUtilities.runOnEDT(() -> nodeEditSelectChanged(fTCP.getSelectedStyleNodeUserObject(), true));
        }
    }

    /**
     * @param selectLabelButtonGroup
     */
    private void buildHeatmapTypePanels(SelectableLabelGroup selectLabelButtonGroup)
    {
        myMainPanelInScroll.removeAll();
        myTypeChoicePanels.clear();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        List<Class<? extends InterpolatedTileVisualizationSupport>> heatmapTypes = myStyleEditPanelController
                .getPrimaryHeatmapClasses();

        TypeChoicePanel firstChoice = null;
        for (Class<? extends InterpolatedTileVisualizationSupport> heatmapClass : heatmapTypes)
        {
            String categoryName = StyleManagerUtils.getStyleCategoryNameForHeatmapClass(heatmapClass);
            TypeChoicePanel choicePanel = getHeatmapPanel(categoryName, selectLabelButtonGroup, heatmapClass);
            if (firstChoice == null)
            {
                firstChoice = choicePanel;
            }
            myTypeChoicePanels.add(choicePanel);
            panel.add(choicePanel, BorderLayout.CENTER);
        }

        panel.add(Box.createVerticalGlue());
        panel.add(new JPanel());
        myMainPanelInScroll.add(panel, BorderLayout.CENTER);

        if (firstChoice != null)
        {
            final TypeChoicePanel choicePanel = firstChoice;
            EventQueueUtilities.runOnEDT(() -> nodeEditSelectChanged(choicePanel.getSelectedStyleNodeUserObject(), true));
        }
    }

    /**
     * Builds the tile type panels.
     *
     * @param slBG the sl bg
     */
    private void buildTileTypePanels(SelectableLabelGroup slBG)
    {
        myMainPanelInScroll.removeAll();
        myTypeChoicePanels.clear();
        JPanel tpPanel = new JPanel();
        tpPanel.setLayout(new BoxLayout(tpPanel, BoxLayout.Y_AXIS));

        List<Class<? extends TileVisualizationSupport>> tileTypes = myStyleEditPanelController.getPrimaryTileClasses();

        TypeChoicePanel firstChoice = null;
        for (Class<? extends TileVisualizationSupport> tileClass : tileTypes)
        {
            String catName = StyleManagerUtils.getStyleCategoryNameForTileClass(tileClass);
            TypeChoicePanel choicePanel = getTilePanel(catName, slBG, tileClass);
            if (firstChoice == null)
            {
                firstChoice = choicePanel;
            }
            myTypeChoicePanels.add(choicePanel);
            tpPanel.add(choicePanel, BorderLayout.CENTER);
        }

        tpPanel.add(Box.createVerticalGlue());
        tpPanel.add(new JPanel());
        myMainPanelInScroll.add(tpPanel, BorderLayout.CENTER);

        if (firstChoice != null)
        {
            final TypeChoicePanel fTCP = firstChoice;
            EventQueueUtilities.runOnEDT(() -> nodeEditSelectChanged(fTCP.getSelectedStyleNodeUserObject(), true));
        }
    }

    /**
     * Gets the location panel.
     *
     * @param title the title
     * @param slBG the SelectableLabelGroup
     * @param baseMGSClass the base mgs class
     * @return the location panel
     */
    private TypeChoicePanel getFeaturePanel(String title, SelectableLabelGroup slBG,
            Class<? extends VisualizationSupport> baseMGSClass)
    {
        StyleNodeUserObject selNode = myStyleEditPanelController.getSelectedNodeForFeatureType(baseMGSClass);
        List<StyleNodeUserObject> nodeList = myStyleEditPanelController.getFeatureStyleNodeList(baseMGSClass);

        return new TypeChoicePanel(title, slBG, nodeList, selNode);
    }

    /**
     * Gets the style registry change listener.
     *
     * @return the style registry change listener
     */
    private VisualizationStyleRegistryChangeListener getStyleRegistryChangeListener()
    {
        if (myRegistryChangeListener == null)
        {
            myRegistryChangeListener = new VisualizationStyleRegistryChangeAdapter()
            {
                @Override
                public void visualizationStyleDatatypeChanged(VisualizationStyleDatatypeChangeEvent evt)
                {
                    handleVisualizationStyleDatatypeChangeEvent(evt);
                }
            };
        }
        return myRegistryChangeListener;
    }

    /**
     * Gets the location panel.
     *
     * @param title the title
     * @param slBG the SelectableLabelGroup
     * @param baseMGSClass the base mgs class
     * @return the location panel
     */
    private TypeChoicePanel getTilePanel(String title, SelectableLabelGroup slBG,
            Class<? extends TileVisualizationSupport> baseMGSClass)
    {
        StyleNodeUserObject selNode = myStyleEditPanelController.getSelectedNodeForTileType(baseMGSClass);
        List<StyleNodeUserObject> nodeList = myStyleEditPanelController.getTileStyleNodeList(baseMGSClass);

        return new TypeChoicePanel(title, slBG, nodeList, selNode);
    }

    /**
     * Gets the heatmap panel.
     *
     * @param categoryName the name of the panel.
     * @param selectLabelButtonGroup the button group used to unite the buttons.
     * @param heatmapClass the base class for heatmaps.
     * @return a heatmap panel.
     */
    private TypeChoicePanel getHeatmapPanel(String categoryName, SelectableLabelGroup selectLabelButtonGroup,
            Class<? extends InterpolatedTileVisualizationSupport> heatmapClass)
    {
        StyleNodeUserObject selNode = myStyleEditPanelController.getSelectedNodeForHeatmapType(heatmapClass);
        List<StyleNodeUserObject> nodeList = myStyleEditPanelController.getHeatmapStyleNodeList(heatmapClass);

        return new TypeChoicePanel(categoryName, selectLabelButtonGroup, nodeList, selNode);
    }

    /**
     * Handle visualization style datatype change event.
     *
     * @param evt the evt
     */
    private void handleVisualizationStyleDatatypeChangeEvent(final VisualizationStyleDatatypeChangeEvent evt)
    {
        if (myDataType != null && myDataType.getDataTypeInfo() != null
                && Objects.equals(evt.getDTIKey(), myDataType.getDataTypeInfo().getTypeKey())
                && !(evt.getSource() instanceof StyleEditPanelController))
        {
            EventQueueUtilities.runOnEDT(
                    () -> myTypeChoicePanels.stream().forEach(t -> t.selectIfPossible(evt.getMGSClass(), evt.getNewStyle())));
        }
    }

    /**
     * Node edit select changed.
     *
     * @param node the node
     * @param selected the selected
     */
    private void nodeEditSelectChanged(StyleNodeUserObject node, boolean selected)
    {
        if (selected)
        {
            myStyleEditPanelController.setEditSelectedStyle(node, selected);
        }
        else
        {
            if (mySelectLabelButtonGroup.getSelectedLabel() == null)
            {
                myStyleEditPanelController.setEditSelectedStyle(null, false);
            }
        }
    }

    /**
     * Node style type select changed.
     *
     * @param node the node
     * @param hasConfirmChanges the has confirm changes
     */
    private void nodeStyleTypeSelectChanged(StyleNodeUserObject node, boolean hasConfirmChanges)
    {
        myStyleEditPanelController.setSelectedStyleForFeatureType(node, hasConfirmChanges);
    }

    /**
     * Sets the children enabled.
     *
     * @param children the children
     * @param enabled the enabled
     */
    private void setChildrenEnabled(Component[] children, boolean enabled)
    {
        if (children != null && children.length > 0)
        {
            for (Component child : children)
            {
                if (child != null)
                {
                    child.setEnabled(enabled);
                    if (child instanceof Container)
                    {
                        setChildrenEnabled(((Container)child).getComponents(), enabled);
                    }
                }
            }
        }
    }

    /**
     * The Class TypeChoicePanel.
     */
    private class TypeChoicePanel extends JPanel implements ActionListener
    {
        /**
         * serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /** The Button to label map. */
        private final Map<JToggleButton, SelectableLabel> myButtonToLabelMap;

        /** The Button to user obj map. */
        private final Map<JToggleButton, StyleNodeUserObject> myButtonToUserObjMap;

        /** The Label to user obj map. */
        private final Map<SelectableLabel, StyleNodeUserObject> myLabelToUserObjMap;

        /**
         * Instantiates a new type choice panel.
         *
         * @param title the title
         * @param slBG the sl bg
         * @param nodeList the node list
         * @param selectedNode the selected style
         */
        public TypeChoicePanel(String title, SelectableLabelGroup slBG, List<StyleNodeUserObject> nodeList,
                StyleNodeUserObject selectedNode)
        {
            super(new BorderLayout());
            setBorder(BorderFactory.createEtchedBorder());
            ButtonGroup bg = new ButtonGroup();
            myButtonToUserObjMap = New.map();
            myLabelToUserObjMap = New.map();
            myButtonToLabelMap = New.map();

            JPanel btPanel = new JPanel();
            btPanel.setLayout(new GridLayout(nodeList.size(), 1, 0, 3));

            for (StyleNodeUserObject node : nodeList)
            {
                JRadioButton sl = new JRadioButton();
                boolean isSelectedNode = Utilities.sameInstance(node, selectedNode);
                sl.setSelected(isSelectedNode);
                sl.setFocusable(false);
                sl.addActionListener(this);
                bg.add(sl);

                SelectableLabel lb = new SelectableLabel(node.toString(), false);
                String description = node.getDefaultStyleInstance().getStyleDescription();
                if (StringUtils.isEmpty(description))
                {
                    description = "No Description For Style";
                }
                lb.setToolTipText(description);
                lb.addActionListener(this);
                myLabelToUserObjMap.put(lb, node);
                slBG.addLabel(lb);

                Box subPanel = Box.createHorizontalBox();
                subPanel.add(sl, BorderLayout.WEST);
                subPanel.add(lb, BorderLayout.CENTER);
                subPanel.add(Box.createHorizontalGlue());

                btPanel.add(subPanel);
                myButtonToUserObjMap.put(sl, node);
                myButtonToLabelMap.put(sl, lb);
            }
            add(btPanel, BorderLayout.CENTER);
            setMaximumSize(new Dimension(200, nodeList.size() * 20 + 20));
            setPreferredSize(new Dimension(200, nodeList.size() * 20 + 20));
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() instanceof SelectableLabel)
            {
                StyleNodeUserObject node = myLabelToUserObjMap.get(e.getSource());
                if (node != null)
                {
                    nodeEditSelectChanged(node, ((SelectableLabel)e.getSource()).isSelected());
                }
            }
            else if (e.getSource() instanceof JRadioButton)
            {
                StyleNodeUserObject node = myButtonToUserObjMap.get(e.getSource());
                if (node != null)
                {
                    nodeStyleTypeSelectChanged(node, true);

                    // If the user switches to a new type, make sure the label
                    // for that type is selected and that the control panel for
                    // that style appears on the right.
                    SelectableLabel lb = myButtonToLabelMap.get(e.getSource());
                    lb.setSelected(true, true);
                }
            }
        }

        /**
         * Gets the selected style node user object.
         *
         * @return the selected style node user object
         */
        public StyleNodeUserObject getSelectedStyleNodeUserObject()
        {
            return myButtonToUserObjMap.entrySet().stream().filter(e -> e.getKey().isSelected()).findFirst()
                    .map(e -> e.getValue()).orElse(null);
        }

        /**
         * Programmatically switch the selected feature type radio button and
         * cause the editor to refresh, don't ask for an accept/cancel
         * confirmation of the change as the change is already comitted to the
         * style registry.
         *
         * @param mgsClass the mgs class
         * @param newStyle the new style
         */
        public void selectIfPossible(Class<? extends VisualizationSupport> mgsClass, VisualizationStyle newStyle)
        {
            if (myButtonToUserObjMap != null && !myButtonToUserObjMap.isEmpty())
            {
                JToggleButton foundButton = null;
                StyleNodeUserObject foundNode = null;
                for (Map.Entry<JToggleButton, StyleNodeUserObject> entry : myButtonToUserObjMap.entrySet())
                {
                    if (entry.getValue().getBaseMGSClass().isAssignableFrom(mgsClass)
                            && newStyle.getClass().getName().equals(entry.getValue().getStyleClass().getName()))
                    {
                        foundButton = entry.getKey();
                        foundNode = entry.getValue();
                        break;
                    }
                }
                if (foundButton != null && !foundButton.isSelected())
                {
                    foundButton.setSelected(true);
                    nodeStyleTypeSelectChanged(foundNode, false);

                    // If the user switches to a new type, make sure the label
                    // for that type is selected and that the control panel for
                    // that style appears on the right.
                    SelectableLabel lb = myButtonToLabelMap.get(foundButton);
                    lb.setSelected(true, true);
                }
            }
        }

        /**
         * Fire selected label event.
         *
         * @param uo the uo
         */
        public void selectLabelNoEventIfMatch(StyleNodeUserObject uo)
        {
            myLabelToUserObjMap.entrySet().stream().filter(e -> Utilities.sameInstance(uo, e.getValue()))
                    .forEach(e -> e.getKey().setSelected(true, false));
        }
    }
}
