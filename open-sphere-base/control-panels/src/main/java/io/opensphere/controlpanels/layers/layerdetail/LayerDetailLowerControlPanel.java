package io.opensphere.controlpanels.layers.layerdetail;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.event.AnimationChangeExtentRequestEvent;
import io.opensphere.controlpanels.layers.tagmanager.TagUtility;
import io.opensphere.controlpanels.util.ShowFilterDialogEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterRegistryListener;
import io.opensphere.core.datafilter.impl.DataFilterRegistryAdapter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.SplitButton;
import io.opensphere.mantle.controller.util.DataGroupInfoUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.data.geom.style.dialog.ShowTypeVisualizationStyleEvent;
import io.opensphere.mantle.data.geom.style.dialog.VisualizationStyleControlDialog;
import io.opensphere.mantle.data.impl.DefaultDataGroupActivator;

/**
 * The Class LayerDetailLowerControlPanel.
 */
@SuppressWarnings("PMD.GodClass")
public class LayerDetailLowerControlPanel extends GridBagPanel
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LayerDetailLowerControlPanel.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The active check box. */
    private JCheckBox myActiveCheckBox;

    /** The animation span button. */
    private IconButton myAnimationSpanButton;

    /** The button panel. */
    private GridBagPanel myButtonPanel;

    /** The Data filter registry listener. */
    private final transient DataFilterRegistryListener myDataFilterRegistryListener;

    /** The data group info. */
    private transient DataGroupInfo myDGI;

    /** The vis style button. */
    private IconButton myFeatureVisStyleButton;

    /** The Filter. */
    private final transient DataLayerFilter myFilter = new DataLayerFilter();

    /** The Filter button. */
    private IconButton myFilterButton;

    /** The Layer detail panel. */
    private final transient LayerDetailPanel myLayerDetailPanel;

    /** The reload button. */
    private IconButton myReimportButton;

    /** The remove layer button. */
    private IconButton myRemoveLayerButton;

    /** The Tag type button. */
    private SplitButton myTagTypeButton;

    /** The vis style button. */
    private IconButton myTileVisStyleButton;

    /** The time column chooser button. */
    private IconButton myTimeColumnButton;

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /**
     * Instantiates a new layer detail lower control panel.
     *
     * @param tb the {@link Toolbox}
     * @param ldp the {@link LayerDetailPanel}
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public LayerDetailLowerControlPanel(Toolbox tb, LayerDetailPanel ldp)
    {
        super();
        myToolbox = tb;
        myLayerDetailPanel = ldp;

        setBorder(BorderFactory.createLineBorder(Color.lightGray));
        init0();

        setInsets(4, 4, 0, 4);
        fillHorizontal();
        addRow(getButtonPanel());

        GridBagPanel hBox = new GridBagPanel();
        hBox.add(getActivationCheckBox());
        hBox.fillHorizontalSpace().fillNone();

        setInsets(4, 4, 4, 4);
        addRow(hBox);

        myDataFilterRegistryListener = createDataFilterRegistryListener();
        myToolbox.getDataFilterRegistry().addListener(myDataFilterRegistryListener);
    }

    /**
     * Update ui details.
     *
     * @param dgi the dgi
     */
    public final void updateUIDetails(DataGroupInfo dgi)
    {
        myDGI = dgi;
        if (myDGI != null)
        {
            boolean groupActive = myDGI.activationProperty().isActiveOrActivating();
            TimeSpan timeSpan = DataGroupInfoUtilities.findTimeExtentFromDGICollection(Collections.singleton(myDGI));
            getSetAnimationSpanButton().setVisible(!(timeSpan.isTimeless() || timeSpan.isZero() || timeSpan.isInstantaneous()));
            getFilterButton().setVisible(isFilterButtonVisible());
            getTimeColumnButton().setVisible(isTimeColumnButtonVisible());
            updateFilterButtonColors();
            getFeatureVisStyleButton().setVisible(myDGI.hasFeatureTypes(false) && myDGI.hasVisualizationStyles(false));
            getTileVisStyleButton().setVisible(myDGI.hasImageTileTypes(false));
            getFeatureVisStyleButton().setEnabled(groupActive);
            getTileVisStyleButton().setEnabled(groupActive);
            getRemoveLayerButton().setVisible(myDGI.getAssistant().canDeleteGroup(myDGI));
            getRemoveLayerButton().setEnabled(!groupActive);
            getReimportButton().setVisible(myDGI.getAssistant().canReImport(myDGI));
            getActivationCheckBox().setVisible(myDGI.userActivationStateControl());
            getReimportButton().setEnabled(!groupActive);
            getActivationCheckBox().setSelected(groupActive);
            if (groupActive)
            {
                getReimportButton().setToolTipText("First deactivate layer to re-import this layer");
                getRemoveLayerButton().setToolTipText("First deactivate layer to remove this layer from the application");
            }
            else
            {
                getReimportButton().setToolTipText("Re-import this layer.");
                getRemoveLayerButton().setToolTipText("Remove this layer from the application.");
            }

            getTagTypeButton().setVisible(myDGI.isTaggable());
        }
        else
        {
            getFeatureVisStyleButton().setVisible(false);
            getTileVisStyleButton().setVisible(false);
            getRemoveLayerButton().setVisible(false);
            getReimportButton().setVisible(false);
            getActivationCheckBox().setVisible(false);
        }
    }

    /**
     * Creates the data filter registry listener.
     *
     * @return the data filter registry listener
     */
    private DataFilterRegistryListener createDataFilterRegistryListener()
    {
        DataFilterRegistryListener listener = new DataFilterRegistryAdapter()
        {
            @Override
            public void loadFilterAdded(String typeKey, DataFilter filter, Object source)
            {
                updateFilterIcons();
            }

            @Override
            public void loadFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
            {
                updateFilterIcons();
            }

            @Override
            public void viewFilterAdded(String typeKey, DataFilter filter, Object source)
            {
                updateFilterIcons();
            }

            @Override
            public void viewFiltersRemoved(Set<? extends DataFilter> removedFilters, Object source)
            {
                updateFilterIcons();
            }

            private void updateFilterIcons()
            {
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateFilterButtonColors();
                    }
                });
            }
        };
        return listener;
    }

    /**
     * Gets the activation check box.
     *
     * @return the activation check box
     */
    private JCheckBox getActivationCheckBox()
    {
        if (myActiveCheckBox == null)
        {
            myActiveCheckBox = new JCheckBox("Active");
            myActiveCheckBox.setFocusPainted(false);
            myActiveCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    EventQueueUtilities.waitCursorRun(myActiveCheckBox, () ->
                    {
                        try
                        {
                            new DefaultDataGroupActivator(myToolbox.getEventManager()).setGroupActive(myDGI,
                                    myActiveCheckBox.isSelected());
                        }
                        catch (InterruptedException e1)
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                LOGGER.debug(e1, e1);
                            }
                        }
                    });
                }
            });
        }
        return myActiveCheckBox;
    }

    /**
     * Gets the button panel.
     *
     * @return the button panel
     */
    private JPanel getButtonPanel()
    {
        if (myButtonPanel == null)
        {
            myButtonPanel = new GridBagPanel();
            myButtonPanel.setInsets(0, 0, 0, 4);
            myButtonPanel.add(getTagTypeButton());
            myButtonPanel.add(getFeatureVisStyleButton());
            myButtonPanel.add(getTileVisStyleButton());
            myButtonPanel.add(getSetAnimationSpanButton());
            myButtonPanel.add(getFilterButton());
            myButtonPanel.add(getTimeColumnButton());
            myButtonPanel.fillHorizontalSpace().fillNone();
            myButtonPanel.add(getReimportButton());
            myButtonPanel.setInsets(0, 0, 0, 0);
            myButtonPanel.add(getRemoveLayerButton());
        }
        return myButtonPanel;
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton getFeatureVisStyleButton()
    {
        if (myFeatureVisStyleButton == null)
        {
            myFeatureVisStyleButton = new IconButton();
            IconUtil.setIcons(myFeatureVisStyleButton, "/images/cog-feature.png");
            myFeatureVisStyleButton.setToolTipText(VisualizationStyleControlDialog.TITLE + " For Features");
            myFeatureVisStyleButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    Set<DataTypeInfo> found = myDGI.findMembers(new Predicate<DataTypeInfo>()
                    {
                        @Override
                        public boolean test(DataTypeInfo value)
                        {
                            return value != null && value.getMapVisualizationInfo() != null
                                    && value.getMapVisualizationInfo().usesMapDataElements();
                        }
                    }, false, true);
                    if (!found.isEmpty())
                    {
                        ShowTypeVisualizationStyleEvent event = new ShowTypeVisualizationStyleEvent(found.iterator().next(),
                                myLayerDetailPanel);
                        myToolbox.getEventManager().publishEvent(event);
                    }
                }
            });
        }
        return myFeatureVisStyleButton;
    }

    /**
     * Gets the layer filter button.
     *
     * @return the layer filter button
     */
    private JButton getFilterButton()
    {
        if (myFilterButton == null)
        {
            myFilterButton = new IconButton(IconType.FILTER);
            myFilterButton.setToolTipText("Open filter manager for this layer");
            myFilterButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    myToolbox.getEventManager().publishEvent(new ShowFilterDialogEvent(myDGI.getId()));
                }
            });
        }
        return myFilterButton;
    }

    /**
     * Gets the re-import button.
     *
     * @return the re-import button
     */
    private JButton getReimportButton()
    {
        if (myReimportButton == null)
        {
            myReimportButton = new IconButton(IconType.RELOAD);
            myReimportButton.setToolTipText("Re-import this layer.");
            myReimportButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    int result = JOptionPane.showConfirmDialog(myLayerDetailPanel,
                            "Are you sure you want to re-import this layer?", "Re-import Layer Confirmation",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION)
                    {
                        myDGI.getAssistant().reImport(myDGI, myLayerDetailPanel);
                        myLayerDetailPanel.setNullAndHide();
                    }
                }
            });
        }
        return myReimportButton;
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton getRemoveLayerButton()
    {
        if (myRemoveLayerButton == null)
        {
            myRemoveLayerButton = new IconButton(IconType.DELETE);
            myRemoveLayerButton.setToolTipText("Remove this layer from the application.");
            myRemoveLayerButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    int result = JOptionPane.showConfirmDialog(myLayerDetailPanel,
                            "Are you sure you want to remove this layer from the application?\n\nYou will not be able to undo this action.",
                            "Remove Layer Confirmation", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION)
                    {
                        Runnable backgroundTask = () -> myDGI.getAssistant().deleteGroup(myDGI, myLayerDetailPanel);
                        EventQueueUtilities.waitCursorRun(SwingUtilities.getWindowAncestor(myRemoveLayerButton), backgroundTask,
                                myLayerDetailPanel::setNullAndHide);
                    }
                }
            });
        }
        return myRemoveLayerButton;
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton getSetAnimationSpanButton()
    {
        if (myAnimationSpanButton == null)
        {
            myAnimationSpanButton = new IconButton();
            IconUtil.setIcons(myAnimationSpanButton, "/images/arrows-collapse.png");
            myAnimationSpanButton.setToolTipText("Set Animation Time Span To Match Layer Span");
            myAnimationSpanButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    Quantify.collectMetric("mist3d.layer-detail.extents-button");
                    TimeSpan extent = DataGroupInfoUtilities.findTimeExtentFromDGICollection(Collections.singleton(myDGI));
                    if (extent != null && !extent.isTimeless())
                    {
                        myToolbox.getEventManager()
                                .publishEvent(new AnimationChangeExtentRequestEvent(extent, myLayerDetailPanel));
                    }
                }
            });
        }
        return myAnimationSpanButton;
    }

    /**
     * Gets the tag type button.
     *
     * @return the tag type button
     */
    private SplitButton getTagTypeButton()
    {
        if (myTagTypeButton == null)
        {
            myTagTypeButton = new SplitButton(null, null);
            IconUtil.setIcons(myTagTypeButton, IconType.TAG);

            myTagTypeButton.setToolTipText("Tag Manager");
            myTagTypeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    TagUtility.showTagDataGroupDialog(myLayerDetailPanel, myDGI, myLayerDetailPanel);
                }
            });

            JMenuItem addTagMI = new JMenuItem("Add Tag");
            addTagMI.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    TagUtility.showTagDataGroupDialog(myLayerDetailPanel, myDGI, myLayerDetailPanel);
                }
            });
            myTagTypeButton.addMenuItem(addTagMI);

            JMenuItem manageTagMI = new JMenuItem("Manage Tags");
            manageTagMI.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    TagUtility.showTagManagerForGroup(myToolbox, myLayerDetailPanel, myDGI, myLayerDetailPanel);
                }
            });
            myTagTypeButton.addMenuItem(manageTagMI);
        }
        return myTagTypeButton;
    }

    /**
     * Gets the vis style button.
     *
     * @return the vis style button
     */
    private JButton getTileVisStyleButton()
    {
        if (myTileVisStyleButton == null)
        {
            myTileVisStyleButton = new IconButton();
            IconUtil.setIcons(myTileVisStyleButton, "/images/cog-tile.png");
            myTileVisStyleButton.setToolTipText(VisualizationStyleControlDialog.TITLE + " For Tiles");
            myTileVisStyleButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    Set<DataTypeInfo> found = myDGI.findMembers(new Predicate<DataTypeInfo>()
                    {
                        @Override
                        public boolean test(DataTypeInfo value)
                        {
                            return value != null && value.getMapVisualizationInfo() != null
                                    && value.getMapVisualizationInfo().isImageTileType();
                        }
                    }, false, true);
                    if (!found.isEmpty())
                    {
                        ShowTypeVisualizationStyleEvent event = new ShowTypeVisualizationStyleEvent(found.iterator().next(),
                                myLayerDetailPanel);
                        myToolbox.getEventManager().publishEvent(event);
                    }
                }
            });
        }
        return myTileVisStyleButton;
    }

    /**
     * Gets the time column button.
     *
     * @return the time column button
     */
    private JButton getTimeColumnButton()
    {
        if (myTimeColumnButton == null)
        {
            myTimeColumnButton = new IconButton();
            IconUtil.setIcons(myTimeColumnButton, IconType.CLOCK);
            myTimeColumnButton.setToolTipText("Change the columns used for time");
            myTimeColumnButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    ThreadUtilities.runBackground(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            for (DataTypeInfo dataType : myDGI.getMembers(false))
                            {
                                if (dataType.isTimeColumnChangeable())
                                {
                                    dataType.changeTimeColumns();
                                }
                            }
                        }
                    });
                }
            });
        }
        return myTimeColumnButton;
    }

    /**
     * Checks if the filter button should be visible.
     *
     * @return true, if is filter button visible
     */
    private boolean isFilterButtonVisible()
    {
        boolean vis = false;
        if (myDGI != null)
        {
            vis = myFilter.test(myDGI);
        }
        return vis;
    }

    /**
     * Checks if the time column button should be visible.
     *
     * @return true, if is time column button visible
     */
    private boolean isTimeColumnButtonVisible()
    {
        boolean vis = false;
        if (myDGI != null)
        {
            vis = myDGI.getMembers(false).stream().anyMatch(dataType -> dataType.isTimeColumnChangeable());
        }
        return vis;
    }

    /**
     * Update filter button colors.
     */
    private void updateFilterButtonColors()
    {
        IconUtil.setIcons(myFilterButton, IconType.FILTER, myDGI != null && DataLayerFilter.hasActiveLoadFilter(myToolbox, myDGI)
                ? Color.GREEN : IconUtil.DEFAULT_ICON_FOREGROUND);
    }
}
