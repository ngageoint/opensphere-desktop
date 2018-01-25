package io.opensphere.controlpanels.state;

import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.util.Utilities;

/**
 * A menu provider for the clear states button.
 */
public class ClearStatesMenuProvider implements ContextMenuProvider<Void>
{
    /** The module state manager. */
    private final ModuleStateManager myModuleStateManager;

    /**
     * Constructor.
     *
     * @param moduleStateManager The module state manager.
     */
    public ClearStatesMenuProvider(ModuleStateManager moduleStateManager)
    {
        myModuleStateManager = Utilities.checkNull(moduleStateManager, "moduleStateManager");
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, Void key)
    {
        JMenuItem clearStatesMenuItem = new JMenuItem("Clear States");
        clearStatesMenuItem.addActionListener(e -> myModuleStateManager.deactivateAllStates());
        clearStatesMenuItem.setToolTipText("Deactivate all states");
        return Collections.singletonList(clearStatesMenuItem);
    }

    @Override
    public int getPriority()
    {
        return 500;
    }
}
