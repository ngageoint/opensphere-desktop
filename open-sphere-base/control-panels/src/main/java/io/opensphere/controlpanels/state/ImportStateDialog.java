package io.opensphere.controlpanels.state;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;

import io.opensphere.core.modulestate.AbstractStateDialog;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * A dialog for importing a state.
 */
public class ImportStateDialog extends AbstractStateDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Construct the dialog.
     *
     * @param parent The parent.
     * @param availableModules The modules to choose from.
     * @param stateDependencies The map of state module depencencies.
     * @param disallowedStateNames List of state names that are already used.
     */
    public ImportStateDialog(Component parent, final Collection<? extends String> availableModules,
            Map<String, Collection<? extends String>> stateDependencies, Collection<? extends String> disallowedStateNames)
    {
        super(parent, "Import State", availableModules, stateDependencies, disallowedStateNames);

        GridBagPanel panel = new GridBagPanel();
        panel.init0();

        initialize(availableModules, null, disallowedStateNames, panel, "Choose which parts of the state to load");
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        getDialogButtonPanel().getButton(ButtonPanel.OK).requestFocus();
    }
}
