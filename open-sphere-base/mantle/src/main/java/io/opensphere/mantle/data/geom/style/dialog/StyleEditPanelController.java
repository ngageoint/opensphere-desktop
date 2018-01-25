package io.opensphere.mantle.data.geom.style.dialog;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.EventQueueExecutor;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.VisualizationSupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationControlPanel.FeatureVisualizationControlPanelListener;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeEvent;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterChangeListener;
import io.opensphere.mantle.data.geom.style.VisualizationStyleRegistry;
import io.opensphere.mantle.data.geom.style.dialog.DataTypeNodeUserObject.NodeType;
import io.opensphere.mantle.data.geom.style.impl.AbstractVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractVisualizationControlPanel;
import io.opensphere.mantle.data.tile.InterpolatedTileVisualizationSupport;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class StyleEditPanelController.
 */
@SuppressWarnings("PMD.GodClass")
public class StyleEditPanelController implements FeatureVisualizationControlPanelListener
{
    /** The Candidate style update. */
    private FeatureVisualizationControlPanel myCandidateControlPanel;

    /** The Candidate style select. */
    private StyleNodeUserObject myCandidateStyleSelect;

    /** The Candidate style update node. */
    private StyleNodeUserObject myCandidateStyleUpdateNode;

    /** The Change support. */
    private final WeakChangeSupport<StyleEditPanelControllerListener> myChangeSupport;

    /** The Current style. */
    private VisualizationStyle myCurrentStyle;

    /** The Data type. */
    private final DataTypeNodeUserObject myDataType;

    /** The Has style select changes. */
    private boolean myHasStyleSelectChanges;

    /** The MGS primary classes. */
    private final List<Class<? extends VisualizationSupport>> myMGSPrimaryClasses;

    /** The MGS primary type to selected style map. */
    private final Map<Class<? extends VisualizationSupport>, StyleNodeUserObject> myMGSPrimaryTypeToSelectedStyleMap;

    /** The MGS primary type to style node list map. */
    private final Map<Class<? extends VisualizationSupport>, List<StyleNodeUserObject>> myMGSPrimaryTypeToStyleNodeListMap;

    /** The Style manager controller. */
    private final StyleManagerController myStyleManagerController;

    /** The Style parameter change listener. */
    private final transient VisualizationStyleParameterChangeListener myStyleParameterChangeListener = new VisualizationStyleParameterChangeListener()
    {
        @Override
        public void styleParametersChanged(VisualizationStyleParameterChangeEvent evt)
        {
            if (myCandidateControlPanel != null && !Utilities.sameInstance(evt.getSource(), StyleEditPanelController.this)
                    && myCandidateControlPanel instanceof AbstractVisualizationControlPanel)
            {
                myCandidateControlPanel.getStyle().setParameters(evt.getChangedParameterSet(),
                        VisualizationStyle.NO_EVENT_SOURCE);
                myCandidateControlPanel.getChangedStyle().setParameters(evt.getChangedParameterSet(),
                        VisualizationStyle.NO_EVENT_SOURCE);
                ((AbstractVisualizationControlPanel)myCandidateControlPanel).updateSync();
            }
        }
    };

    /** The tile primary classes. */
    private final List<Class<? extends TileVisualizationSupport>> myTilePrimaryClasses;

    /** The tile primary type to selected style map. */
    private final Map<Class<? extends TileVisualizationSupport>, StyleNodeUserObject> myTilePrimaryTypeToSelectedStyleMap;

    /** The tile primary type to style node list map. */
    private final Map<Class<? extends TileVisualizationSupport>, List<StyleNodeUserObject>> myTilePrimaryTypeToStyleNodeListMap;

    /** The primary support classes used for heatmaps. */
    private final List<Class<? extends InterpolatedTileVisualizationSupport>> myHeatmapPrimaryClasses;

    /** The map of heatmap primary classes to selected styles. */
    private final Map<Class<? extends InterpolatedTileVisualizationSupport>, StyleNodeUserObject> myHeatmapPrimaryTypeToSelectedStyleMap;

    /** The map of heatmap primary classes to support styles. */
    private final Map<Class<? extends InterpolatedTileVisualizationSupport>, List<StyleNodeUserObject>> myHeatmapPrimaryTypeToStyleNodeListMap;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Visualization style registry. */
    private final VisualizationStyleRegistry myVisualizationStyleRegistry;

    /**
     * Instantiates a new style edit panel controller.
     *
     * @param tb the {@link Toolbox}
     * @param smc the {@link StyleManagerController}
     * @param dataType the {@link DataTypeNodeUserObject}
     */
    public StyleEditPanelController(Toolbox tb, StyleManagerController smc, DataTypeNodeUserObject dataType)
    {
        Utilities.checkNull(tb, "tb");
        Utilities.checkNull(dataType, "obj");
        myStyleManagerController = smc;
        myDataType = dataType;
        myVisualizationStyleRegistry = MantleToolboxUtils.getMantleToolbox(tb).getVisualizationStyleRegistry();
        myToolbox = tb;
        myChangeSupport = new WeakChangeSupport<>();

        DataTypeInfo dti = myDataType.getDataTypeInfo() != null ? myDataType.getDataTypeInfo()
                : StyleManagerUtils.getDataTypeInfo(myToolbox, myDataType.getNodeKey());
        myMGSPrimaryClasses = StyleManagerUtils.getDefaultFeatureClassesForDataType(myToolbox, myDataType.getNodeType(), dti);
        myTilePrimaryClasses = StyleManagerUtils.getDefaultTileClassesForDataType(myToolbox, myDataType.getNodeType(), dti);
        myHeatmapPrimaryClasses = StyleManagerUtils.getDefaultHeatmapClassesForDataType(myDataType.getNodeType(), dti);
        myMGSPrimaryTypeToStyleNodeListMap = New.map();
        myMGSPrimaryTypeToSelectedStyleMap = New.map();
        myTilePrimaryTypeToStyleNodeListMap = New.map();
        myTilePrimaryTypeToSelectedStyleMap = New.map();
        myHeatmapPrimaryTypeToStyleNodeListMap = New.map();
        myHeatmapPrimaryTypeToSelectedStyleMap = New.map();

        for (Class<? extends VisualizationSupport> mgsClass : myMGSPrimaryClasses)
        {
            Class<? extends VisualizationStyle> selStyle = myStyleManagerController.getSelectedVisualizationStyleClass(mgsClass,
                    myDataType.getNodeKey());
            Class<? extends VisualizationStyle> baseStyleClass = StyleManagerUtils.getBaseStyleClassesForFeatureClass(mgsClass);

            List<StyleNodeUserObject> nodeList = createStyleNodeList(baseStyleClass, mgsClass);

            for (StyleNodeUserObject node : nodeList)
            {
                if (EqualsHelper.equals(node.getStyleClass(), selStyle))
                {
                    myMGSPrimaryTypeToSelectedStyleMap.put(mgsClass, node);
                }
            }

            myMGSPrimaryTypeToStyleNodeListMap.put(mgsClass, nodeList);
        }

        for (Class<? extends TileVisualizationSupport> tileClass : myTilePrimaryClasses)
        {
            Class<? extends VisualizationStyle> selStyle = myStyleManagerController.getSelectedVisualizationStyleClass(tileClass,
                    myDataType.getNodeKey());
            Class<? extends VisualizationStyle> baseStyleClass = StyleManagerUtils.getBaseStyleClassesForTileClass(tileClass);

            List<StyleNodeUserObject> nodeList = createStyleNodeList(baseStyleClass, tileClass);

            for (StyleNodeUserObject node : nodeList)
            {
                if (EqualsHelper.equals(node.getStyleClass(), selStyle))
                {
                    myTilePrimaryTypeToSelectedStyleMap.put(tileClass, node);
                }
            }

            myTilePrimaryTypeToStyleNodeListMap.put(tileClass, nodeList);
        }

        for (Class<? extends InterpolatedTileVisualizationSupport> heatmapClass : myHeatmapPrimaryClasses)
        {
            // selected style not getting set properly:
            Class<? extends VisualizationStyle> selectedStyle = myStyleManagerController
                    .getSelectedVisualizationStyleClass(heatmapClass, myDataType.getNodeKey());
            Class<? extends VisualizationStyle> baseStyleClass = StyleManagerUtils
                    .getBaseStyleClassesForHeatmapClass(heatmapClass);

            List<StyleNodeUserObject> nodeList = createStyleNodeList(baseStyleClass, heatmapClass);
            for (StyleNodeUserObject node : nodeList)
            {
                if (EqualsHelper.equals(node.getStyleClass(), selectedStyle))
                {
                    myHeatmapPrimaryTypeToSelectedStyleMap.put(heatmapClass, node);
                }
            }

            myHeatmapPrimaryTypeToStyleNodeListMap.put(heatmapClass, nodeList);
        }
    }

    /**
     * Accept changes.
     */
    public void acceptChanges()
    {
        if (myHasStyleSelectChanges)
        {
            myMGSPrimaryTypeToSelectedStyleMap.put(myCandidateStyleSelect.getBaseMGSClass(), myCandidateStyleSelect);
            myStyleManagerController.setSelectedStyleClass(myCandidateStyleSelect.getDefaultStyleInstance(),
                    myCandidateStyleSelect.getBaseMGSClass(), myDataType.getNodeKey(), this);

            myHasStyleSelectChanges = false;
            myCandidateStyleSelect = null;
        }

        if (myCandidateControlPanel != null && myCandidateControlPanel.hasChanges())
        {
            myCandidateControlPanel.applyChanges();
        }

        fireLockFromChanges(false);
    }

    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    public void addListener(StyleEditPanelControllerListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Cancel changes.
     */
    public void cancelChanges()
    {
        if (myHasStyleSelectChanges)
        {
            myCandidateStyleSelect = null;
            myHasStyleSelectChanges = false;
        }

        if (myCandidateControlPanel != null && myCandidateControlPanel.hasChanges())
        {
            myCandidateControlPanel.cancelChanges();
        }

        fireStyleEditSelectionChanged(null, null);
        fireLockFromChanges(false);
        fireRefreshDisplay();
    }

    /**
     * Gets the current editing style node.
     *
     * @return the current editing style node
     */
    public StyleNodeUserObject getCurrentEditingStyleNode()
    {
        return myCandidateStyleUpdateNode;
    }

    /**
     * Gets the style node list.
     *
     * @param primaryFeatureClass the primary feature class
     * @return the style node list
     */
    public List<StyleNodeUserObject> getFeatureStyleNodeList(Class<? extends VisualizationSupport> primaryFeatureClass)
    {
        List<StyleNodeUserObject> aList = myMGSPrimaryTypeToStyleNodeListMap.get(primaryFeatureClass);
        return aList == null ? Collections.<StyleNodeUserObject>emptyList() : Collections.unmodifiableList(aList);
    }

    /**
     * Gets the primary feature classes.
     *
     * @return the primary feature classes
     */
    public List<Class<? extends VisualizationSupport>> getPrimaryFeatureClasses()
    {
        return Collections.unmodifiableList(myMGSPrimaryClasses);
    }

    /**
     * Gets the primary tile classes.
     *
     * @return the primary tile classes
     */
    public List<Class<? extends TileVisualizationSupport>> getPrimaryTileClasses()
    {
        return Collections.unmodifiableList(myTilePrimaryClasses);
    }

    /**
     * Gets the primary heatmap classes.
     *
     * @return the primary heatmap classes.
     */
    public List<Class<? extends InterpolatedTileVisualizationSupport>> getPrimaryHeatmapClasses()
    {
        return Collections.unmodifiableList(myHeatmapPrimaryClasses);
    }

    /**
     * Gets the selected node for feature type.
     *
     * @param primaryFeatureClass the primary feature class
     * @return the selected node for feature type
     */
    public StyleNodeUserObject getSelectedNodeForFeatureType(Class<? extends VisualizationSupport> primaryFeatureClass)
    {
        return myMGSPrimaryTypeToSelectedStyleMap.get(primaryFeatureClass);
    }

    /**
     * Gets the selected node for feature type.
     *
     * @param primaryFeatureClass the primary feature class
     * @return the selected node for feature type
     */
    public StyleNodeUserObject getSelectedNodeForTileType(Class<? extends VisualizationSupport> primaryFeatureClass)
    {
        return myTilePrimaryTypeToSelectedStyleMap.get(primaryFeatureClass);
    }

    /**
     * Gets the selected node for feature type.
     *
     * @param primaryHeatmapClass the primary heatmap class
     * @return the selected node for heatmap type
     */
    public StyleNodeUserObject getSelectedNodeForHeatmapType(Class<? extends VisualizationSupport> primaryHeatmapClass)
    {
        return myHeatmapPrimaryTypeToSelectedStyleMap.get(primaryHeatmapClass);
    }

    /**
     * Gets the style node list.
     *
     * @param primaryFeatureClass the primary feature class
     * @return the style node list
     */
    public List<StyleNodeUserObject> getTileStyleNodeList(Class<? extends VisualizationSupport> primaryFeatureClass)
    {
        List<StyleNodeUserObject> aList = myTilePrimaryTypeToStyleNodeListMap.get(primaryFeatureClass);
        return aList == null ? Collections.<StyleNodeUserObject>emptyList() : Collections.unmodifiableList(aList);
    }

    /**
     * Gets the style node list for heatmap styles.
     *
     * @param primaryHeatmapClass the primary class for heatmaps.
     * @return the style node list for heatmap styles.
     */
    public List<StyleNodeUserObject> getHeatmapStyleNodeList(Class<? extends VisualizationSupport> primaryHeatmapClass)
    {
        List<StyleNodeUserObject> list = myHeatmapPrimaryTypeToStyleNodeListMap.get(primaryHeatmapClass);
        return list == null ? Collections.<StyleNodeUserObject>emptyList() : Collections.unmodifiableList(list);
    }

    @Override
    public void performLiveParameterUpdate(String dtiKey, Class<? extends VisualizationSupport> convertedClass,
            Class<? extends VisualizationStyle> vsClass, Set<VisualizationStyleParameter> updateSet)
    {
        VisualizationStyle style = null;
        if (dtiKey == null)
        {
            style = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                    .getDefaultStyle(convertedClass);
        }
        else
        {
            style = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry().getStyle(convertedClass,
                    dtiKey, false);
        }
        if (style instanceof AbstractVisualizationStyle && Utilities.sameInstance(style.getClass(), vsClass))
        {
            ((AbstractVisualizationStyle)style).setParameters(updateSet, this);
        }

        if (myCurrentStyle != null)
        {
            myStyleManagerController.updateStyleFromEditor(myCurrentStyle, convertedClass, myDataType.getNodeKey(), this);
        }
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    public void removeListener(StyleEditPanelControllerListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Sets the edits the selected style.
     *
     * @param styleNode the new edits the selected style
     * @param selected the selected
     */
    public void setEditSelectedStyle(StyleNodeUserObject styleNode, boolean selected)
    {
        if (myCandidateControlPanel != null)
        {
            myCandidateControlPanel.removeListener(this);
        }
        if (myCurrentStyle != null)
        {
            myCurrentStyle.removeStyleParameterChangeListener(myStyleParameterChangeListener);
        }
        if (selected)
        {
            myCandidateStyleUpdateNode = styleNode;
            if (myCandidateStyleUpdateNode != null && myCandidateStyleUpdateNode.getDefaultStyleInstance() != null)
            {
                myCandidateControlPanel = myCandidateStyleUpdateNode.getDefaultStyleInstance().getUIPanel();
                myCandidateControlPanel.addListener(this);
                myCurrentStyle = MantleToolboxUtils.getMantleToolbox(myToolbox).getVisualizationStyleRegistry()
                        .getStyle(styleNode.getBaseMGSClass(), myCandidateControlPanel.getStyle().getDTIKey(), false);
                if (myCurrentStyle != null)
                {
                    myCurrentStyle.addStyleParameterChangeListener(myStyleParameterChangeListener);
                }
            }
        }
        else
        {
            myCandidateStyleUpdateNode = null;
            myCandidateControlPanel = null;
            myCurrentStyle = null;
        }
        fireStyleEditSelectionChanged(myCandidateStyleUpdateNode, myCandidateControlPanel);
    }

    /**
     * Sets the selected style for feature type.
     *
     * @param node the new selected style for feature type
     * @param showConfirmChanges the has confirm changes
     */
    public void setSelectedStyleForFeatureType(StyleNodeUserObject node, boolean showConfirmChanges)
    {
        Class<? extends VisualizationSupport> baseMGSClass = node.getBaseMGSClass();
        VisualizationStyleGroup groupType = getStyleGroupType(baseMGSClass);
        switch (groupType)
        {
            case FEATURES:
                if (!Utilities.sameInstance(myMGSPrimaryTypeToSelectedStyleMap.get(baseMGSClass), node))
                {
                    myHasStyleSelectChanges = true;
                    myCandidateStyleSelect = node;
                    fireLockFromChanges(showConfirmChanges);
                }
                break;
            case TILES:
                if (!Utilities.sameInstance(myTilePrimaryTypeToStyleNodeListMap.get(baseMGSClass), node))
                {
                    myHasStyleSelectChanges = true;
                    myCandidateStyleSelect = node;
                    fireLockFromChanges(showConfirmChanges);
                }
                break;
            case HEATMAPS:
                if (!Utilities.sameInstance(myHeatmapPrimaryTypeToStyleNodeListMap.get(baseMGSClass), node))
                {
                    myHasStyleSelectChanges = true;
                    myCandidateStyleSelect = node;
                    fireLockFromChanges(showConfirmChanges);
                }
                break;
            default:
                // fail fast:
                throw new UnsupportedOperationException("Unable to set selected style group type " + groupType);
        }
    }

    /**
     * Gets the {@link VisualizationStyleGroup} enum value for the supplied
     * support class.
     *
     * @param visualizationSupportClass the class for which to get the enum
     *            type.
     * @return the enum type corresponding with the supplied support class.
     */
    protected VisualizationStyleGroup getStyleGroupType(Class<? extends VisualizationSupport> visualizationSupportClass)
    {
        // default to features unless we can determine otherwise:
        VisualizationStyleGroup returnValue = VisualizationStyleGroup.FEATURES;
        if (visualizationSupportClass.isAssignableFrom(TileVisualizationSupport.class))
        {
            returnValue = VisualizationStyleGroup.TILES;
        }
        else if (visualizationSupportClass.isAssignableFrom(InterpolatedTileVisualizationSupport.class))
        {
            returnValue = VisualizationStyleGroup.HEATMAPS;
        }
        return returnValue;
    }

    @Override
    public void styleChanged(boolean hasChangesFromBase)
    {
        if (myCandidateControlPanel != null && myCandidateStyleUpdateNode != null)
        {
            fireLockFromChanges(hasChangesFromBase);
        }
    }

    @Override
    public void styleChangesAccepted()
    {
        if (myCandidateControlPanel != null)
        {
            myStyleManagerController.updateStyleFromEditor(myCandidateControlPanel.getStyle(),
                    myCandidateStyleUpdateNode.getBaseMGSClass(), myDataType.getNodeKey(), this);
        }
    }

    @Override
    public void styleChangesCancelled()
    {
        if (myCandidateControlPanel != null && myCandidateControlPanel.isUpdateWithPreviewable())
        {
            myStyleManagerController.updateStyleFromEditor(myCandidateControlPanel.getStyle(),
                    myCandidateStyleUpdateNode.getBaseMGSClass(), myDataType.getNodeKey(), this);
        }
    }

    /**
     * Creates the style node list.
     *
     * @param baseStyleClass the base style class
     * @param baseMGSClass the base mgs class
     * @return the list
     */
    private List<StyleNodeUserObject> createStyleNodeList(Class<? extends VisualizationStyle> baseStyleClass,
            Class<? extends VisualizationSupport> baseMGSClass)
    {
        Set<Class<? extends VisualizationStyle>> styleSet = myVisualizationStyleRegistry.getStylesForStyleType(baseStyleClass);

        List<StyleNodeUserObject> nodeList = New.list();
        for (Class<? extends VisualizationStyle> styleClass : styleSet)
        {
            VisualizationStyle defaultInstance = myStyleManagerController.getStyleForEditorWithConfigValues(styleClass,
                    baseMGSClass, myDataType.getNodeKey());

            boolean addNode = true;
            if (defaultInstance instanceof FeatureVisualizationStyle)
            {
                FeatureVisualizationStyle fvs = (FeatureVisualizationStyle)defaultInstance;
                MapVisualizationType mvt = null;
                if (myDataType.getDataTypeInfo() != null && myDataType.getDataTypeInfo().getMapVisualizationInfo() != null)
                {
                    mvt = myDataType.getDataTypeInfo().getMapVisualizationInfo().getVisualizationType();
                }

                if (myDataType.getNodeType() == NodeType.DEFAULT_ROOT_FEATURE)
                {
                    addNode = !((FeatureVisualizationStyle)defaultInstance).requiresMetaData();
                }

                Set<MapVisualizationType> mvsTypes = fvs.getRequiredMapVisTypes();
                if (mvt != null && !mvsTypes.isEmpty() && !mvsTypes.contains(mvt))
                {
                    addNode = false;
                }
            }

            if (addNode)
            {
                nodeList.add(new StyleNodeUserObject(styleClass, defaultInstance, baseMGSClass));
            }
        }
        Collections.sort(nodeList, new Comparator<StyleNodeUserObject>()
        {
            @Override
            public int compare(StyleNodeUserObject o1, StyleNodeUserObject o2)
            {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return nodeList;
    }

    /**
     * Fire lock from changes.
     *
     * @param enable the enable
     */
    private void fireLockFromChanges(final boolean enable)
    {
        myChangeSupport.notifyListeners(new Callback<StyleEditPanelController.StyleEditPanelControllerListener>()
        {
            @Override
            public void notify(StyleEditPanelControllerListener listener)
            {
                listener.lockFromChanges(enable);
            }
        }, new EventQueueExecutor());
    }

    /**
     * Fire refresh display.
     *
     */
    private void fireRefreshDisplay()
    {
        myChangeSupport.notifyListeners(new Callback<StyleEditPanelController.StyleEditPanelControllerListener>()
        {
            @Override
            public void notify(StyleEditPanelControllerListener listener)
            {
                listener.refreshDisplay();
            }
        }, new EventQueueExecutor());
    }

    /**
     * Fire Style Edit Selection Changed.
     *
     * @param styleToEdit the style to edit
     * @param editorPanel the editor panel
     */
    private void fireStyleEditSelectionChanged(final StyleNodeUserObject styleToEdit,
            final FeatureVisualizationControlPanel editorPanel)
    {
        myChangeSupport.notifyListeners(new Callback<StyleEditPanelController.StyleEditPanelControllerListener>()
        {
            @Override
            public void notify(StyleEditPanelControllerListener listener)
            {
                listener.styleEditSelectionChanged(styleToEdit, editorPanel);
            }
        }, new EventQueueExecutor());
    }

    /**
     * The StyleEditPanelControllerListener.
     */
    public interface StyleEditPanelControllerListener
    {
        /**
         * Lock from changes.
         *
         * @param lock the lock
         */
        void lockFromChanges(boolean lock);

        /**
         * Refresh display.
         */
        void refreshDisplay();

        /**
         * Style edit selection changed.
         *
         * @param styleToEdit the style to edit
         * @param editorPanel the editor panel
         */
        void styleEditSelectionChanged(StyleNodeUserObject styleToEdit, FeatureVisualizationControlPanel editorPanel);
    }
}
