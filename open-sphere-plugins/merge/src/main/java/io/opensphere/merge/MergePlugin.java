package io.opensphere.merge;

import java.awt.EventQueue;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfo.DataGroupContextKey;
import io.opensphere.mantle.data.DataGroupInfo.MultiDataGroupContextKey;
import io.opensphere.merge.controller.MergeController;
import io.opensphere.merge.model.JoinModel;
import io.opensphere.merge.model.MergePrefs;
import io.opensphere.merge.ui.ConfigGui;
import io.opensphere.merge.ui.GuiUtil;
import io.opensphere.merge.ui.JoinManager;
import io.opensphere.merge.ui.MergeContextMenuProvider;
import io.opensphere.merge.ui.MergeContextSingleSelectionMenuProvider;

/** The Merge plugin. */
public class MergePlugin extends PluginAdapter
{
    /** Preferences key. */
    private static final String PREFS_KEY = "merge";

    /** Preferences object obtained from the PreferencesRegistry. */
    private Preferences mySystemPreferences;

    /** JAXB-enabled preferences stored by this Plugin. */
    private MergePrefs myMergePreferences;

    /** GUI for managing join/merge configurations. */
    private ConfigGui myConfigGui;

    /** Provides the menu options when right clicking on multiple layers. */
    private MergeContextMenuProvider myMenuProvider;

    /** Provides the menu options when right clicking on a single layer. */
    private MergeContextSingleSelectionMenuProvider mySingleSelectionMenuProvider;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        // if sysPrefs are null, probably the system is broken
        mySystemPreferences = toolbox.getPreferencesRegistry().getPreferences(MergePlugin.class);
        if (mySystemPreferences != null)
        {
            myMergePreferences = mySystemPreferences.getJAXBObject(MergePrefs.class, PREFS_KEY, null);
        }

        // Jam in an empty MergePrefs in case a real one was not found
        if (myMergePreferences == null)
        {
            myMergePreferences = new MergePrefs();
        }

        JoinManager joinManager = new JoinManager(toolbox);
        MergeController mergeController = new MergeController(toolbox, myMergePreferences);

        myMenuProvider = new MergeContextMenuProvider(toolbox, mergeController);
        myMenuProvider.setJoinManager(joinManager);
        myMenuProvider.setJoinListener(m -> addJoin(m));

        mySingleSelectionMenuProvider = new MergeContextSingleSelectionMenuProvider(toolbox, mergeController);
        mySingleSelectionMenuProvider.setJoinManager(joinManager);
        mySingleSelectionMenuProvider.setJoinListener(m -> addJoin(m));

        toolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                MultiDataGroupContextKey.class, myMenuProvider);
        toolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupContextKey.class, mySingleSelectionMenuProvider);

        // add a menu item that maps to the showEditor method
        EventQueue.invokeLater(() ->
        {
            GuiUtil.addMenuItem(GuiUtil.getMainMenu(toolbox, MenuBarRegistry.EDIT_MENU), "Joins/Merges",
                () -> myConfigGui.show());

            myConfigGui = new ConfigGui(toolbox, joinManager, mergeController, () -> writePrefs());
            myConfigGui.setData(myMergePreferences);
        });
    }

    /**
     * Persist a copy of a new join config created by the JoinGui.
     *
     * @param m join parameters
     */
    private void addJoin(JoinModel m)
    {
        MergePrefs.Join join = myMergePreferences.addJoinModel(m);
        FXUtilities.runOnFXThread(() -> myConfigGui.addJoin(join));

        writePrefs();
    }

    /** Persist preferences to disk. */
    private void writePrefs()
    {
        if (mySystemPreferences != null)
        {
            mySystemPreferences.putJAXBObject(PREFS_KEY, myMergePreferences, false, null);
        }
    }
}
