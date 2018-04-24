package io.opensphere.merge;

import javax.swing.SwingUtilities;

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

    /** The system Toolbox. */
    private Toolbox tools;

    /** Preferences object obtained from the PreferencesRegistry. */
    private Preferences sysPrefs;

    /** JAXB-enabled preferences stored by this Plugin. */
    private MergePrefs prefs;

    /** Manager for join operations and layers. */
    private final JoinManager joinMan = new JoinManager();

    /** GUI for managing join configurations. */
    private final ConfigGui gui = new ConfigGui();
    {
        gui.setJoinMan(joinMan);
        gui.setSaveEar(() -> writePrefs());
    }

    /** Provides the menu options when right clicking on multiple layers. */
    private MergeContextMenuProvider menuProvider;

    /** Provides the menu options when right clicking on a single layer. */
    private MergeContextSingleSelectionMenuProvider singleSelectionMenuProvider;

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox tb)
    {
        tools = tb;
        gui.setTools(tools);
        joinMan.setTools(tools);
        menuProvider = new MergeContextMenuProvider(tools);
        menuProvider.setJoinManager(joinMan);
        menuProvider.setJoinListener(m -> addJoin(m));

        singleSelectionMenuProvider = new MergeContextSingleSelectionMenuProvider(tools);
        singleSelectionMenuProvider.setJoinManager(joinMan);
        singleSelectionMenuProvider.setJoinListener(m -> addJoin(m));

        tools.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                MultiDataGroupContextKey.class, menuProvider);
        tools.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(DataGroupInfo.ACTIVE_DATA_CONTEXT,
                DataGroupContextKey.class, singleSelectionMenuProvider);

        // add a menu item that maps to the showEditor method
        SwingUtilities.invokeLater(() -> GuiUtil.addMenuItem(GuiUtil.getMainMenu(tools, MenuBarRegistry.EDIT_MENU),
                "Joins/Merges", () -> gui.show()));

        // if sysPrefs are null, probably the system is broken
        sysPrefs = tools.getPreferencesRegistry().getPreferences(MergePlugin.class);
        if (sysPrefs != null)
        {
            prefs = sysPrefs.getJAXBObject(MergePrefs.class, PREFS_KEY, null);
        }

        // Jam in an empty MergePrefs in case a real one was not found
        if (prefs == null)
        {
            prefs = new MergePrefs();
        }
        FXUtilities.runOnFXThread(() -> gui.setData(prefs));
    }

    /**
     * Persist a copy of a new join config created by the JoinGui.
     *
     * @param m join parameters
     */
    private void addJoin(JoinModel m)
    {
        prefs.addJoinModel(m);
        FXUtilities.runOnFXThread(() -> gui.setData(prefs));
        writePrefs();
    }

    /** Persist preferences to disk. */
    private void writePrefs()
    {
        if (sysPrefs != null)
        {
            sysPrefs.putJAXBObject(PREFS_KEY, prefs, false, null);
        }
    }
}
