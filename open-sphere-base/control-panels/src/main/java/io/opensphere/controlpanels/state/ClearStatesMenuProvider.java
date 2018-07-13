package io.opensphere.controlpanels.state;

import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.Utilities;

/**
 * A menu provider for the clear states button.
 */
public class ClearStatesMenuProvider implements ContextMenuProvider<Void>
{
    /** The module state manager. */
    private final ModuleStateManager myModuleStateManager;

    /** The toolbox. */
    private Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public ClearStatesMenuProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myModuleStateManager = Utilities.checkNull(myToolbox.getModuleStateManager(), "moduleStateManager");
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, Void key)
    {
        JMenuItem clearStatesMenuItem = new JMenuItem("Clear States");
        clearStatesMenuItem.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.control-panels.selection.clear-states");
            myModuleStateManager.deactivateAllStates();
        });
        clearStatesMenuItem.setToolTipText("Deactivate all states");
        return Collections.singletonList(clearStatesMenuItem);
    }

    @Override
    public int getPriority()
    {
        return 500;
    }
}
