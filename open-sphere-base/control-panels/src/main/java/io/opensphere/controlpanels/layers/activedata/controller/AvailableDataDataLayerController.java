package io.opensphere.controlpanels.layers.activedata.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataLayerController;
import io.opensphere.controlpanels.layers.base.UserConfirmer;
import io.opensphere.controlpanels.layers.groupby.AvailableTreeBuilderProvider;
import io.opensphere.controlpanels.layers.prefs.DataDiscoveryPreferences;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupEvent;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.event.DataTypeInfoTagsChangeEvent;
import io.opensphere.mantle.data.impl.AvailableGroupByTreeBuilder;
import io.opensphere.mantle.data.impl.DataGroupInfoGroupByUtility.DefaultNodeUserObjectGenerator;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;
import io.opensphere.mantle.data.impl.GroupByTreeBuilder;
import io.opensphere.mantle.data.impl.NodeUserObjectGenerator;

/**
 * The Class AddDataDataLayerController.
 */
@SuppressWarnings("PMD.GodClass")
public class AvailableDataDataLayerController extends AbstractDiscoveryDataLayerController
{
    /** The Constant VIEW_TYPE_PREFERENCE. */
    private static final String VIEW_TYPE_PREFERENCE = "AddDataViewByType";

    /** The Show layer type labels preferences change listener. */
    private PreferenceChangeListener myShowLayerTypeLabelsPreferencesChangeListener;

    /**
     * The available layers tree builder provider.
     */
    private final AvailableTreeBuilderProvider myTreeBuilderProvider = new AvailableTreeBuilderProvider();

    /** The my view by type. */
    private String myViewByType;

    /**
     * The default view type.
     */
    private static final String DEFAULT_VIEW_TYPE = "Source";

    /**
     * Instantiates a new timeline data layer controller.
     *
     * @param pBox the box
     * @param confirmer Asks the user yes no questions.
     */
    public AvailableDataDataLayerController(Toolbox pBox, UserConfirmer confirmer)
    {
        super(pBox, confirmer);

        PreferencesRegistry prefsRegistry = pBox.getPreferencesRegistry();
        myViewByType = prefsRegistry.getPreferences(AvailableDataDataLayerController.class).getString(VIEW_TYPE_PREFERENCE,
                DEFAULT_VIEW_TYPE);
        // Get the correct view by type name, older configs may have invalid ones.
        myViewByType = myTreeBuilderProvider.getBuilder(myViewByType).getGroupByName();

        createPreferenceChangeListeners(prefsRegistry.getPreferences(DataDiscoveryPreferences.class));
    }

    /**
     * Force refresh.
     */
    public void forceRefresh()
    {
        setTreeNeedsRebuild(true);
        notifyDataGroupsChanged();
    }

    @Override
    public GroupByTreeBuilder getGroupByTreeBuilder()
    {
        AvailableGroupByTreeBuilder builder = myTreeBuilderProvider.getBuilder(myViewByType);
        builder.initializeForAvailable(getToolbox());
        return builder;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.controlpanels.layers.base.AbstractDiscoveryDataLayerController#getNodeUserObjectGenerator()
     */
    @Override
    public NodeUserObjectGenerator getNodeUserObjectGenerator()
    {
        if (StringUtils.equalsIgnoreCase(DEFAULT_VIEW_TYPE, myViewByType))
        {
            return new DefaultNodeUserObjectGenerator();
        }
        return new AvailableNodeUserObjectGenerator(getToolbox());
    }

    @Override
    public String getViewByTypeString()
    {
        return myViewByType.toLowerCase();
    }

    /**
     * Removes the data group.
     *
     * @param dgi the dgi
     */
    public void removeDataGroup(final DataGroupInfo dgi)
    {
        if (dgi != null && dgi.getAssistant() != null && dgi.getAssistant().canDeleteGroup(dgi))
        {
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    int result = JOptionPane.showConfirmDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                            "Are you sure you want to remove this layer from the application?\n\nYou will not be able to undo this action.",
                            "Remove Layer Confirmation", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION)
                    {
                        dgi.getAssistant().deleteGroup(dgi, this);
                    }
                }
            });
        }
    }

    /**
     * Removes the data groups.
     *
     * @param dgiSet the dgi set
     */
    public void removeDataGroups(final Collection<DataGroupInfo> dgiSet)
    {
        if (CollectionUtilities.hasContent(dgiSet))
        {
            final Set<DataGroupInfo> qualifyingGroups = New.set();
            for (DataGroupInfo dgi : dgiSet)
            {
                if (dgi != null && dgi.getAssistant() != null && dgi.getAssistant().canDeleteGroup(dgi))
                {
                    qualifyingGroups.add(dgi);
                }
            }
            if (!qualifyingGroups.isEmpty())
            {
                EventQueueUtilities.runOnEDT(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        StringBuilder sb = new StringBuilder(128);
                        sb.append("<html>");
                        if (dgiSet.size() != qualifyingGroups.size())
                        {
                            sb.append(
                                    "<center color=\"yellow\">Note that one or more of your selected layers can not be removed.</center><br>");
                            dgiSet.removeAll(qualifyingGroups);
                            for (DataGroupInfo dgi : dgiSet)
                            {
                                sb.append("<center color=\"yellow\">");
                                sb.append(dgi.getDisplayNameWithPostfixTopParentName());
                                sb.append("</center>");
                            }
                            sb.append("<br>");
                        }
                        sb.append(
                                "<center>Are you sure you want to remove the following layers from the application?</center><br>");
                        for (DataGroupInfo dgi : qualifyingGroups)
                        {
                            sb.append("<center>");
                            sb.append(dgi.getDisplayNameWithPostfixTopParentName());
                            sb.append("</center>");
                        }
                        sb.append("<br>" + "<center>You will not be able to undo this action.</center><br><br>" + "</html>");

                        int result = JOptionPane.showConfirmDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                                sb.toString(), "Remove Layer Confirmation", JOptionPane.OK_CANCEL_OPTION);
                        if (result == JOptionPane.OK_OPTION)
                        {
                            for (DataGroupInfo dgi : qualifyingGroups)
                            {
                                dgi.getAssistant().deleteGroup(dgi, this);
                            }
                        }
                    }
                });
            }
            else
            {
                JOptionPane.showMessageDialog(getToolbox().getUIRegistry().getMainFrameProvider().get(),
                        "None of the selected layers can be removed.", "Can Not Remove Layers Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    @Override
    public void setViewByTypeFromString(String vbt)
    {
        if (!EqualsHelper.equals(myViewByType, vbt))
        {
            myViewByType = vbt;
            getToolbox().getPreferencesRegistry().getPreferences(AvailableDataDataLayerController.class)
                    .putString(VIEW_TYPE_PREFERENCE, myViewByType, this);
            setTreeNeedsRebuild(true);
            notifyDataGroupsChanged();
        }
    }

    /**
     * Gets the available view types.
     *
     * @return The available view type for the available data panel.
     */
    public String[] getViewTypes()
    {
        List<String> viewTypes = myTreeBuilderProvider.getGroupByTypes();
        return viewTypes.toArray(new String[viewTypes.size()]);
    }

    /**
     * Fires a "trigger" event to the supplied collection of data groups.
     *
     * @param pSource the source of the event.
     * @param pGroups the groups to which to fire the event.
     */
    public void trigger(Object pSource, Collection<DataGroupInfo> pGroups)
    {
        for (DataGroupInfo group : pGroups)
        {
            group.trigger(new DataGroupEvent(pSource, null, DataGroupEvent.TRIGGER, group));
        }
    }

    @Override
    protected void handleTagsChanged(DataTypeInfoTagsChangeEvent event)
    {
        if (myViewByType == "Tag")
        {
            setTreeNeedsRebuild(true);
            notifyDataGroupsChanged();
        }
    }

    /**
     * Creates the preference change listeners.
     *
     * @param prefs The preferences.
     */
    private void createPreferenceChangeListeners(Preferences prefs)
    {
        myShowLayerTypeLabelsPreferencesChangeListener = getShowLayerTypeLabelsPreferencesChangeListener();
        prefs.addPreferenceChangeListener(DataDiscoveryPreferences.SHOW_AVAILABLE_SOURCE_TYPE_LABELS,
                myShowLayerTypeLabelsPreferencesChangeListener);
    }

    /**
     * The Class ActiveGroupByNodeUserObject.
     */
    private static class AvailableGroupByNodeUserObject extends GroupByNodeUserObject
    {
        /** The Toolbox. */
        private final Toolbox myToolbox;

        /**
         * Instantiates a new group by node user object.
         *
         * @param tb the {@link Toolbox}
         * @param dgi the {@link DataGroupInfo}
         */
        public AvailableGroupByNodeUserObject(Toolbox tb, DataGroupInfo dgi)
        {
            super(dgi);
            myToolbox = tb;
        }

        /**
         * Instantiates a new group by node user object.
         *
         * @param tb the {@link Toolbox}
         * @param dgi the {@link DataGroupInfo}
         * @param dti the {@link DataTypeInfo}
         */
        public AvailableGroupByNodeUserObject(Toolbox tb, DataGroupInfo dgi, DataTypeInfo dti)
        {
            super(dgi, dti);
            myToolbox = tb;
        }

        /**
         * Instantiates a new group by node user object.
         *
         * @param tb the {@link Toolbox}
         * @param label the label
         */
        public AvailableGroupByNodeUserObject(Toolbox tb, String label)
        {
            super(label);
            myToolbox = tb;
        }

        @Override
        public void generateLabel()
        {
            DataGroupInfo dgi = getDataGroupInfo();
            DataTypeInfo dti = getDataTypeInfo();
            if (dgi != null && dti == null)
            {
                setId(dgi.getId());
                if (DataDiscoveryPreferences.isShowAvailableSourceTypeLabels(myToolbox.getPreferencesRegistry()))
                {
                    setLabel(dgi.getParent() == null ? dgi.getDisplayName()
                            : dgi.getDisplayName() + " (" + dgi.getTopParentDisplayName() + ")");
                }
                else
                {
                    setLabel(dgi.getDisplayName());
                }
            }
            else if (dti != null)
            {
                setId(dti.getTypeKey());
                if (DataDiscoveryPreferences.isShowAvailableSourceTypeLabels(myToolbox.getPreferencesRegistry()))
                {
                    setLabel(dgi == null || dgi.getParent() == null ? dti.getDisplayName()
                            : dti.getDisplayName() + " (" + dgi.getTopParentDisplayName() + ")");
                }
                else
                {
                    setLabel(dti.getDisplayName());
                }
            }
            else if (isCategoryNode())
            {
                setLabel(generateCategoryLabel());
            }
        }
    }

    /**
     * The Class AvailableNodeUserObjectGenerator.
     */
    private static class AvailableNodeUserObjectGenerator implements NodeUserObjectGenerator
    {
        /** The Toolbox. */
        private final Toolbox myToolbox;

        /**
         * Instantiates a new active node user object generator.
         *
         * @param tb the tb
         */
        public AvailableNodeUserObjectGenerator(Toolbox tb)
        {
            myToolbox = tb;
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi)
        {
            return new AvailableGroupByNodeUserObject(myToolbox, dgi);
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi, DataTypeInfo dti)
        {
            return new AvailableGroupByNodeUserObject(myToolbox, dgi, dti);
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(String label)
        {
            return new AvailableGroupByNodeUserObject(myToolbox, label);
        }
    }
}
