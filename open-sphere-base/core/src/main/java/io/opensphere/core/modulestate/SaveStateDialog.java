package io.opensphere.core.modulestate;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * A dialog for configuring a state to be saved.
 */
public class SaveStateDialog extends AbstractStateDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The check boxes indicating what outputs to save to.
     */
    private final Collection<? extends JCheckBox> mySaveTos;

    /**
     * Construct the dialog.
     *
     * @param parent The parent.
     * @param availableModules The modules to choose from.
     * @param stateDependencies the modules this state depends on
     * @param disallowedStateNames the disallowed state names
     * @param list The collection of check boxes representing the outputs the
     *            dialog can save to.
     */
    public SaveStateDialog(Component parent, final Collection<? extends String> availableModules,
            Map<String, Collection<? extends String>> stateDependencies, Collection<? extends String> disallowedStateNames,
            Collection<? extends JCheckBox> list)
    {
        super(parent, "Save State", availableModules, stateDependencies, disallowedStateNames);
        mySaveTos = list;

        GridBagPanel panel = new GridBagPanel();
        panel.init0();

        panel.style("label").fillNone().anchorWest();
        panel.style("titleField").fillHorizontal().setGridwidth(2);

        if (!mySaveTos.isEmpty())
        {
            panel.style("label", "titleField").addRow(new JLabel("Save to:"), createSaveToPanel());
        }

        initialize(availableModules, stateDependencies, disallowedStateNames, panel, "Choose which parts of the state to save");
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        getDialogButtonPanel().getButton(ButtonPanel.OK).requestFocus();
    }

    /**
     * Create the save-to panel.
     *
     * @return The panel.
     */
    private JPanel createSaveToPanel()
    {
        JPanel saveToPanel = new JPanel();
        for (JCheckBox checkbox : mySaveTos)
        {
            saveToPanel.add(checkbox);
        }
        return saveToPanel;
    }
}
