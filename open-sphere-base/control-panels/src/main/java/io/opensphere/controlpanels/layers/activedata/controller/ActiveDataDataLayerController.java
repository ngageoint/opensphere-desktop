package io.opensphere.controlpanels.layers.activedata.controller;

import java.awt.event.MouseListener;
import java.util.List;
import java.util.Objects;

import javax.swing.tree.TreePath;

import io.opensphere.controlpanels.layers.activedata.tree.DragAndDropTreeTransferHandler;
import io.opensphere.controlpanels.layers.activedata.zorder.ZOrderGroupComparator;
import io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataLayerController;
import io.opensphere.controlpanels.layers.base.UserConfirmer;
import io.opensphere.controlpanels.layers.groupby.ActiveTreeBuilderProvider;
import io.opensphere.controlpanels.layers.prefs.DataDiscoveryPreferences;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.tree.DirectionalTransferHandler;
import io.opensphere.core.util.swing.tree.OrderTreeEventController;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsAddedEvent;
import io.opensphere.mantle.controller.event.impl.DataElementsRemovedEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataGroupInfoDisplayNameChangedEvent;
import io.opensphere.mantle.data.event.DataTypeInfoColorChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoTagsChangeEvent;
import io.opensphere.mantle.data.event.DataTypePropertyChangeEvent;
import io.opensphere.mantle.data.event.DataTypeVisibilityChangeEvent;
import io.opensphere.mantle.data.impl.ActiveGroupByTreeBuilder;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;
import io.opensphere.mantle.data.impl.GroupByTreeBuilder;
import io.opensphere.mantle.data.impl.NodeUserObjectGenerator;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The active data data data layer controller.
 */
@SuppressWarnings("PMD.GodClass")
public class ActiveDataDataLayerController extends AbstractDiscoveryDataLayerController
{
    /**
     * The default view by type.
     */
    private static final String DEFAULT_VIEW_TYPE = "Type";

    /** The Constant VIEW_TYPE_PREFERENCE, in case you didn't know. */
    private static final String VIEW_TYPE_PREFERENCE = "ActiveViewByType";

    /** The Data type controller event listener. */
    private EventListener<AbstractDataTypeControllerEvent> myDataTypeControllerEventListener;

    /** The data type visibility change event listener. */
    private final EventListener<DataTypeVisibilityChangeEvent> myDataTypeVisibilityChangeEventListener = this::handleDataTypeVisibilityChanged;

    /**
     * The drag and drop handler.
     */
    private final DragAndDropTreeTransferHandler myDragAndDropController;

    /** The data type color change event listener. */
    private final EventListener<DataTypeInfoColorChangeEvent> myDTIColorChangeListener;

    /** The data type property change event listener. */
    private final EventListener<DataTypePropertyChangeEvent> myDataTypePropertyChangeListener = this::handleDataTypePropertyChange;

    /**
     * The mouse listener.
     */
    private final ActiveDataMouseListener myMouseListener;

    /** The Drag and Drop Controller. */
    private final OrderTreeEventController myOrderTreeEventController = new OrderTreeEventController()
    {
        /** Whether to allow drag. */
        private boolean myAllowDrag = true;

        @Override
        public void dragEnd()
        {
        }

        @Override
        public void dragInProgress()
        {
        }

        @Override
        public boolean isAllowDrag()
        {
            return myAllowDrag;
        }

        @Override
        public void selectionChanged(final TreePath path, final boolean isSelected)
        {
        }

        @Override
        public void setAllowDrag(boolean allowDrag)
        {
            myAllowDrag = allowDrag;
        }
    };

    /** The z order change listener. */
    private final EventListener<DataGroupInfoDisplayNameChangedEvent> myRenameListener = this::handleRename;

    /**
     * The currently selected data type.
     */
    private DataTypeInfo mySelectedDataType;

    /** The Show feature counts. */
    private PreferenceChangeListener myShowFeatureCountsPreferencesChangeListener;

    /** The Show layer source labels preferences change listener. */
    private PreferenceChangeListener myShowLayerSourceLabelsPreferencesChangeListener;

    /** The Show layer type icons preferences change listener. */
    private PreferenceChangeListener myShowLayerTypeIconsPreferencesChangeListener;

    /** The Show layer type labels preferences change listener. */
    private PreferenceChangeListener myShowLayerTypeLabelsPreferencesChangeListener;

    /**
     * Provides the tree builders based on selected group by.
     */
    private final ActiveTreeBuilderProvider myTreeBuilderProvider = new ActiveTreeBuilderProvider();

    /** The view by type. */
    private String myViewByType;

    /**
     * Instantiates a new timeline data layer controller.
     *
     * @param pBox the box
     * @param confirmer Asks the user yes no questions.
     */
    public ActiveDataDataLayerController(Toolbox pBox, UserConfirmer confirmer)
    {
        super(pBox, confirmer);
        myMouseListener = new ActiveDataMouseListener(pBox);
        myDTIColorChangeListener = event -> notifyUpdateTreeLabelsRequest();
        getToolbox().getEventManager().subscribe(DataTypeVisibilityChangeEvent.class, myDataTypeVisibilityChangeEventListener);
        getToolbox().getEventManager().subscribe(DataTypeInfoColorChangeEvent.class, myDTIColorChangeListener);
        getToolbox().getEventManager().subscribe(DataGroupInfoDisplayNameChangedEvent.class, myRenameListener);
        getToolbox().getEventManager().subscribe(DataTypePropertyChangeEvent.class, myDataTypePropertyChangeListener);

        myViewByType = pBox.getPreferencesRegistry().getPreferences(ActiveDataDataLayerController.class)
                .getString(VIEW_TYPE_PREFERENCE, DEFAULT_VIEW_TYPE);
        // Get the correct view by type name, older configs may have invalid
        // ones.
        myViewByType = myTreeBuilderProvider.getBuilder(myViewByType).getGroupByName();
        createPreferenceChangeListeners(pBox.getPreferencesRegistry().getPreferences(DataDiscoveryPreferences.class));
        createDataTypeControllerEventListener();
        myDragAndDropController = new DragAndDropTreeTransferHandler();
    }

    /**
     * Either deletes the data type from the passed in group or removes the
     * group.
     *
     * @param dataTypeToDelete The data type to delete, or null if the group
     *            needs deleting.
     * @param typesGroup The group the type belongs to, or if the type is null
     *            the group to delete.
     */
    public void deleteDataTypeOrGroup(DataTypeInfo dataTypeToDelete, DataGroupInfo typesGroup)
    {
        if (dataTypeToDelete != null && typesGroup != null)
        {
            if (typesGroup.getAssistant().canDeleteGroup(typesGroup))
            {
                getDataGroupController().removeDataGroupInfo(typesGroup, this);
            }
            else
            {
                typesGroup.removeMember(dataTypeToDelete, true, this);
            }
        }
        else if (typesGroup != null)
        {
            getDataGroupController().removeDataGroupInfo(typesGroup, this);
        }
    }

    @Override
    public DirectionalTransferHandler getDragAndDropHandler()
    {
        return myDragAndDropController;
    }

    @Override
    public GroupByTreeBuilder getGroupByTreeBuilder()
    {
        ActiveGroupByTreeBuilder builder = myTreeBuilderProvider.getBuilder(myViewByType);
        builder.initializeForActive(getToolbox());
        builder.setGroupComparator(new ZOrderGroupComparator(getToolbox()));
        return builder;
    }

    /**
     * Gets the mouse listener.
     *
     * @return The mouse listener.
     */
    public MouseListener getMouseListener()
    {
        return myMouseListener;
    }

    @Override
    public NodeUserObjectGenerator getNodeUserObjectGenerator()
    {
        return new ActiveNodeUserObjectGenerator(getToolbox(), myViewByType);
    }

    /**
     * Get the orderTreeEventController.
     *
     * @return the orderTreeEventController
     */
    @Override
    public OrderTreeEventController getOrderTreeEventController()
    {
        return myOrderTreeEventController;
    }

    /**
     * Gets the selected data type.
     *
     * @return The selected data type.
     */
    public DataTypeInfo getSelectedDataType()
    {
        return mySelectedDataType;
    }

    /**
     * Gets the available view by types for the layers window.
     *
     * @return The view by types.
     */
    public String[] getViewByTypes()
    {
        List<String> viewByTypes = myTreeBuilderProvider.getGroupByTypes();
        return viewByTypes.toArray(new String[viewByTypes.size()]);
    }

    @Override
    public String getViewByTypeString()
    {
        return myViewByType.toLowerCase();
    }

    /**
     * Sets the selected data type.
     *
     * @param selectedDataType The selected data type.
     */
    public void setSelected(DataTypeInfo selectedDataType)
    {
        mySelectedDataType = selectedDataType;
    }

    @Override
    public void setViewByTypeFromString(String vbt)
    {
        if (!Objects.equals(myViewByType, vbt))
        {
            myViewByType = vbt;
            getToolbox().getPreferencesRegistry().getPreferences(ActiveDataDataLayerController.class)
                    .putString(VIEW_TYPE_PREFERENCE, myViewByType, this);
            setTreeNeedsRebuild(true);
            notifyDataGroupsChanged();
        }
    }

    /**
     * Activate deactivate group.
     *
     * @param selected the selected
     * @param dataGroupInfo the data group info
     */
    public void toggleGroupVisibility(boolean selected, DataGroupInfo dataGroupInfo)
    {
        if (dataGroupInfo != null && dataGroupInfo.hasMembers(false))
        {
            for (DataTypeInfo dti : dataGroupInfo.getMembers(false))
            {
                dti.setVisible(selected, this);
            }
        }
    }

    /**
     * Activate deactivate group.
     *
     * @param selected the selected
     * @param dti the dti
     */
    public void toggleTypeVisibility(boolean selected, DataTypeInfo dti)
    {
        if (dti != null)
        {
            dti.setVisible(selected, this);
        }
    }

    @Override
    protected void handleTagsChanged(DataTypeInfoTagsChangeEvent event)
    {
        if ("Tag".equals(myViewByType))
        {
            setTreeNeedsRebuild(true);
            notifyDataGroupsChanged();
        }
    }

    /**
     * Creates the data elements added event listener.
     */
    private void createDataTypeControllerEventListener()
    {
        myDataTypeControllerEventListener = new EventListener<AbstractDataTypeControllerEvent>()
        {
            @Override
            public void notify(AbstractDataTypeControllerEvent event)
            {
                if (event instanceof DataElementsAddedEvent || event instanceof DataElementsRemovedEvent)
                {
                    ThreadUtilities.runBackground(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            updateGroupNodeUserObjectLabels();
                            notifyUpdateTreeLabelsRequest();
                        }
                    });
                }
            }
        };
        getToolbox().getEventManager().subscribe(AbstractDataTypeControllerEvent.class, myDataTypeControllerEventListener);
    }

    /**
     * Creates the preference change listeners.
     *
     * @param prefs The preferences.
     */
    private void createPreferenceChangeListeners(Preferences prefs)
    {
        myShowLayerTypeLabelsPreferencesChangeListener = getShowLayerTypeLabelsPreferencesChangeListener();
        prefs.addPreferenceChangeListener(DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_TYPE_LABELS,
                myShowLayerTypeLabelsPreferencesChangeListener);

        myShowLayerSourceLabelsPreferencesChangeListener = new PreferenceChangeListener()
        {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt)
            {
                updateGroupNodeUserObjectLabels();
                notifyUpdateTreeLabelsRequest();
            }
        };
        prefs.addPreferenceChangeListener(DataDiscoveryPreferences.SHOW_ACTIVE_SOURCE_LABELS,
                myShowLayerSourceLabelsPreferencesChangeListener);

        myShowLayerTypeIconsPreferencesChangeListener = new PreferenceChangeListener()
        {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt)
            {
                updateGroupNodeUserObjectLabels();
                notifyUpdateTreeLabelsRequest();
            }
        };
        prefs.addPreferenceChangeListener(DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_TYPE_ICONS,
                myShowLayerTypeIconsPreferencesChangeListener);

        myShowFeatureCountsPreferencesChangeListener = new PreferenceChangeListener()
        {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt)
            {
                updateGroupNodeUserObjectLabels();
                notifyUpdateTreeLabelsRequest();
            }
        };
        prefs.addPreferenceChangeListener(DataDiscoveryPreferences.SHOW_ACTIVE_LAYER_FEATURE_COUNTS,
                myShowFeatureCountsPreferencesChangeListener);
    }

    /**
     * Handle data type visibility changed.
     *
     * @param event the event
     */
    private void handleDataTypeVisibilityChanged(DataTypeVisibilityChangeEvent event)
    {
        if (MantleToolboxUtils.getMantleToolbox(getToolbox()).getDataGroupController().isTypeActive(event.getDataTypeInfo()))
        {
            setTreeNeedsRebuild(true);
            notifyGroupsVisibilityChanged(event);
        }
    }

    /**
     * Handle DataTypePropertyChangeEvent.
     *
     * @param event the event
     */
    private void handleDataTypePropertyChange(DataTypePropertyChangeEvent event)
    {
        // Refresh the tree when custom labels change
        notifyRepaintTreeRequest();
    }

    /**
     * Handle rename event.
     *
     * @param event the event
     */
    private void handleRename(DataGroupInfoDisplayNameChangedEvent event)
    {
        if (event.getSource() != this && event.getGroup().activationProperty().isActiveOrActivating())
        {
            setTreeNeedsRebuild(true);
            notifyDataGroupsChanged();
        }
    }

    /**
     * The Class ActiveGroupByNodeUserObject.
     */
    private static class ActiveGroupByNodeUserObject extends GroupByNodeUserObject
    {
        /** The Toolbox. */
        private final Toolbox myToolbox;

        /** The View by active type. */
        private final String myViewByActiveType;

        /**
         * Instantiates a new group by node user object.
         *
         * @param tb the {@link Toolbox}
         * @param dgi the {@link DataGroupInfo}
         * @param dti the {@link DataTypeInfo}
         * @param viewBy the view by
         */
        public ActiveGroupByNodeUserObject(Toolbox tb, DataGroupInfo dgi, DataTypeInfo dti, String viewBy)
        {
            super(dgi, dti);
            myToolbox = tb;
            myViewByActiveType = viewBy;
        }

        /**
         * Instantiates a new group by node user object.
         *
         * @param tb the {@link Toolbox}
         * @param dgi the {@link DataGroupInfo}
         * @param viewBy the group by value.
         */
        public ActiveGroupByNodeUserObject(Toolbox tb, DataGroupInfo dgi, String viewBy)
        {
            super(dgi);
            myToolbox = tb;
            myViewByActiveType = viewBy;
        }

        /**
         * Instantiates a new group by node user object.
         *
         * @param tb the {@link Toolbox}
         * @param label the label
         * @param viewBy the view by
         */
        public ActiveGroupByNodeUserObject(Toolbox tb, String label, String viewBy)
        {
            super(label);
            myToolbox = tb;
            myViewByActiveType = viewBy;
        }

        @Override
        public void generateLabel()
        {
            DataGroupInfo dgi = getDataGroupInfo();
            DataTypeInfo dti = getDataTypeInfo();
            if (dgi != null || dti != null)
            {
                StringBuilder sb = new StringBuilder();
                if (dgi != null && dti == null)
                {
                    sb.append(dgi.getDisplayName());

                    if (DataDiscoveryPreferences.isShowActiveSourceTypeLabels(myToolbox.getPreferencesRegistry()))
                    {
                        // Display types and parents.
                        setId(dgi.getId());
                        if (dgi.numMembers(false) == 1)
                        {
                            dti = dgi.getMembers(false).iterator().next();
                            sb.append(getTypeLabel(dti));
                            sb.append(getFeatureCount(dti));
                        }
                    }
                    else
                    {
                        // Display types and parents.
                        if (dgi.numMembers(false) == 1)
                        {
                            dti = dgi.getMembers(false).iterator().next();
                            sb.append(getFeatureCount(dti));
                        }
                    }
                }
                else if (dti != null)
                {
                    setId(dti.getTypeKey());
                    sb.append(dti.getDisplayName());
                    sb.append(getTypeLabel(dti));
                    sb.append(getFeatureCount(dti));
                }
                sb.append(getLayerSourceLabel(dgi));
                setLabel(sb.toString());
            }
            else
            {
                setLabel(generateCategoryLabel());
            }
        }

        @Override
        public boolean isSelected()
        {
            if (isSelectable() && getDataGroupInfo().activationProperty().isActiveOrActivating())
            {
                if (getDataTypeInfo() != null)
                {
                    return getDataTypeInfo().isVisible();
                }
                else
                {
                    boolean allVisible = true;
                    for (DataTypeInfo dti : getDataGroupInfo().getMembers(false))
                    {
                        if (!dti.isVisible())
                        {
                            allVisible = false;
                            break;
                        }
                    }
                    return allVisible;
                }
            }
            return false;
        }

        /**
         * Gets the feature count.
         *
         * @param dti the dti
         * @return the feature count
         */
        private String getFeatureCount(DataTypeInfo dti)
        {
            String result = "";
            boolean showFeatureCounts = DataDiscoveryPreferences
                    .isShowActiveLayerFeatureCounts(myToolbox.getPreferencesRegistry());
            if (showFeatureCounts && dti.getMapVisualizationInfo() != null
                    && dti.getMapVisualizationInfo().getVisualizationType().isMapDataElementType())
            {
                int count = MantleToolboxUtils.getMantleToolbox(myToolbox).getDataElementCache().getElementCountForType(dti);
                result = " (" + Integer.toString(count) + ")";
            }
            return result;
        }

        /**
         * Gets the layer source label.
         *
         * @param dgi the dgi
         * @return the layer source label
         */
        private String getLayerSourceLabel(DataGroupInfo dgi)
        {
            String source = "";
            if (dgi != null && !"Source".equals(myViewByActiveType)
                    && DataDiscoveryPreferences.isShowActiveSourceTypeLabels(myToolbox.getPreferencesRegistry()))
            {
                source = " [" + dgi.getTopParentDisplayName() + "]";
            }
            return source;
        }

        /**
         * Gets the type label.
         *
         * @param dti the dti
         * @return the type label
         */
        private String getTypeLabel(DataTypeInfo dti)
        {
            String type = "";
            if (dti.getMapVisualizationInfo() != null
                    && DataDiscoveryPreferences.isShowActiveLayerTypeLabels(myToolbox.getPreferencesRegistry()))
            {
                if (dti.getMapVisualizationInfo().getVisualizationType().isMapDataElementType())
                {
                    type = " Features";
                }
                else if (dti.getMapVisualizationInfo().isImageTileType())
                {
                    type = " Tiles";
                }
                else if (dti.getMapVisualizationInfo().isImageType())
                {
                    type = " Imagery";
                }
                else if (dti.getMapVisualizationInfo().getVisualizationType().isTerrainTileType())
                {
                    type = " Terrain";
                }
            }
            return type;
        }
    }

    /**
     * The Class ActiveNodeUserObjectGenerator.
     */
    private static class ActiveNodeUserObjectGenerator implements NodeUserObjectGenerator
    {
        /** The Toolbox. */
        private final Toolbox myToolbox;

        /** The View by. */
        private final String myViewBy;

        /**
         * Instantiates a new active node user object generator.
         *
         * @param tb the tb
         * @param viewBy the view by
         */
        public ActiveNodeUserObjectGenerator(Toolbox tb, String viewBy)
        {
            myToolbox = tb;
            myViewBy = viewBy;
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi)
        {
            return new ActiveGroupByNodeUserObject(myToolbox, dgi, myViewBy);
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi, DataTypeInfo dti)
        {
            return new ActiveGroupByNodeUserObject(myToolbox, dgi, dti, myViewBy);
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(String label)
        {
            return new ActiveGroupByNodeUserObject(myToolbox, label, myViewBy);
        }
    }
}
