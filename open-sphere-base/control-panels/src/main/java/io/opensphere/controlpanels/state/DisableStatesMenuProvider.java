package io.opensphere.controlpanels.state;

import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.Utilities;

/**
 * A menu provider for the clear states button.
 */
public class DisableStatesMenuProvider implements ContextMenuProvider<Void>
{
    /** The module state manager. */
    private final ModuleStateManager myModuleStateManager;

    /**
     * Constructor.
     *
     * @param moduleStateManager The module state manager.
     */
    public DisableStatesMenuProvider(ModuleStateManager moduleStateManager)
    {
        myModuleStateManager = Utilities.checkNull(moduleStateManager, "moduleStateManager");
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, Void key)
    {
        JMenuItem disableStatesMenuItem = new JMenuItem("Disable States");
        disableStatesMenuItem.addActionListener(e ->
        {
            Quantify.collectMetric("mist3d.control-panels.selection.disable-states");
            myModuleStateManager.deactivateAllStates();
        });
        disableStatesMenuItem.setToolTipText("Deactivate all states");
        return Collections.singletonList(disableStatesMenuItem);
    }

    @Override
    public int getPriority()
    {
        return 500;
    }
}
