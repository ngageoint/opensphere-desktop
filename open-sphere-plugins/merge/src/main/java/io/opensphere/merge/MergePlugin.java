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

    /** Manager for join operations and layers. */
    private final JoinManager myJoinManager = new JoinManager();

    /** GUI for managing join configurations. */
    private ConfigGui myJoinConfigGui;

    /** Provides the menu options when right clicking on multiple layers. */
    private MergeContextMenuProvider myMenuProvider;

    /** Provides the menu options when right clicking on a single layer. */
    private MergeContextSingleSelectionMenuProvider mySingleSelectionMenuProvider;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        myJoinManager.setTools(toolbox);

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

        myMenuProvider = new MergeContextMenuProvider(toolbox, myMergePreferences);
        myMenuProvider.setJoinManager(myJoinManager);
        myMenuProvider.setJoinListener(m -> addJoin(m));

        mySingleSelectionMenuProvider = new MergeContextSingleSelectionMenuProvider(toolbox, myMergePreferences);
        mySingleSelectionMenuProvider.setJoinManager(myJoinManager);
        mySingleSelectionMenuProvider.setJoinListener(m -> addJoin(m));

        toolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                MultiDataGroupContextKey.class, myMenuProvider);
        toolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupContextKey.class, mySingleSelectionMenuProvider);

        // add a menu item that maps to the showEditor method
        EventQueue.invokeLater(() ->
        {
            GuiUtil.addMenuItem(GuiUtil.getMainMenu(toolbox, MenuBarRegistry.EDIT_MENU), "Joins/Merges",
                () -> myJoinConfigGui.show());

            myJoinConfigGui = new ConfigGui(toolbox, myJoinManager, () -> writePrefs());
            myJoinConfigGui.setData(myMergePreferences);
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
        FXUtilities.runOnFXThread(() -> myJoinConfigGui.addJoin(join));

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
