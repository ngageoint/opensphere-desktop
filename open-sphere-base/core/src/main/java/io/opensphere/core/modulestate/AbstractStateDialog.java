package io.opensphere.core.modulestate;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.GhostTextField;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.table.CheckBoxTable;
import io.opensphere.core.util.swing.table.CheckBoxTableModel;

/**
 * Common base class for dialogs for save state.
 */
public abstract class AbstractStateDialog extends OptionDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The description field. */
    private final GhostTextField myDescriptionField = new GhostTextField("Add a custom description");

    /** The table for selecting modules to save. */
    private final CheckBoxTable myModuleTable;

    /** The State dependencies. */
    private final Map<String, Collection<? extends String>> myStateDependencies;

    /**
     * Listener that effects changes on the entire set of modules in the model.
     */
    private final transient TableModelListener myStateListener;

    /** The tags field. */
    private final GhostTextField myTagsField = new GhostTextField("Tags organize states: e.g., states, population");

    /** Model for the title field. */
    private final NameModel myTitleModel = new NameModel();

    /** Support for validation. */
    private final transient DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(null)
    {
        @Override
        public ValidationStatus getValidationStatus()
        {
            setValidationResult(myTitleModel.getValidationStatus(), myTitleModel.getErrorMessage());
            return super.getValidationStatus();
        }
    };

    /**
     * Construct the dialog.
     *
     * @param parent The parent.
     * @param title The title for the dialog.
     * @param availableModules The modules to choose from.
     * @param stateDependencies the state dependency
     * @param disallowedStateNames the disallowed state names
     */
    public AbstractStateDialog(Component parent, String title, final Collection<? extends String> availableModules,
            final Map<String, Collection<? extends String>> stateDependencies, Collection<? extends String> disallowedStateNames)
    {
        super(parent, null, title);
        myStateDependencies = stateDependencies;
        myStateListener = e ->
        {
            if (e.getSource() instanceof CheckBoxTableModel)
            {
                CheckBoxTableModel cbtm = (CheckBoxTableModel)e.getSource();
                Map<String, Boolean> moduleState = getModuleSelectionState(cbtm);
                // The module the user is currently clicking.
                String selectedModule = (String)cbtm.getValueAt(e.getFirstRow(), 1);
                Collection<? extends String> dependencyList = myStateDependencies.get(selectedModule);
                boolean forwardDependency = dependencyList != null && !dependencyList.isEmpty();
                setDependencies(cbtm, moduleState, selectedModule, forwardDependency);
                setModuleStates(cbtm, moduleState);
            }
        };

        myModuleTable = new CheckBoxTable("", "Title", Boolean.TRUE, availableModules);
        myModuleTable.getModel().addTableModelListener(myStateListener);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        getDialogButtonPanel().getButton(ButtonPanel.OK).requestFocus();
    }

    /**
     * Get the description of the state.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return myDescriptionField.getText();
    }

    /**
     * Get the selected modules.
     *
     * @return The modules.
     */
    public Collection<? extends String> getSelectedModules()
    {
        return myModuleTable.getCheckedValues();
    }

    /**
     * Get the id for the state.
     *
     * @return The id.
     */
    public String getStateId()
    {
        return myTitleModel.get();
    }

    /**
     * Get the tags for the state.
     *
     * @return The tags.
     */
    public Collection<? extends String> getTags()
    {
        return StringUtils.isBlank(myTagsField.getText()) ? Collections.<String>emptySet()
                : Arrays.asList(myTagsField.getText().split("\\s*,\\s*"));
    }

    /**
     * Set the description of the state.
     *
     * @param description The description.
     */
    public void setDescription(String description)
    {
        myDescriptionField.setText(description);
    }

    /**
     * Set the selected modules.
     *
     * @param modules The modules.
     */
    public void setSelectedModules(Collection<? extends String> modules)
    {
        myModuleTable.setCheckedValues(modules);
    }

    /**
     * Set the id for the state.
     *
     * @param id The id.
     */
    public void setStateId(String id)
    {
        myTitleModel.set(id);
    }

    /**
     * Set the tags for the state.
     *
     * @param tags The tags.
     */
    public void setTags(Collection<? extends String> tags)
    {
        myTagsField.setText(StringUtilities.join(", ", tags));
    }

    /**
     * Set the disallowed names in the title model.
     *
     * @param disallowedStateNames The disallowed names.
     */
    public final void setDisallowedNames(Collection<? extends String> disallowedStateNames)
    {
        myTitleModel.setDisallowedNames(disallowedStateNames);
    }

    /**
     * Initialize the dialog.
     *
     * @param availableModules The modules to choose from.
     * @param stateDependencies the modules this state is dependant on
     * @param disallowedStateNames the disallowed state names
     * @param panel The main panel.
     * @param moduleSelectionText The text to use for the module selection
     *            checkbox.
     */
    protected final void initialize(final Collection<? extends String> availableModules,
            Map<String, Collection<? extends String>> stateDependencies, Collection<? extends String> disallowedStateNames,
            GridBagPanel panel, String moduleSelectionText)
    {
        panel.style("label").fillNone().anchorWest();
        panel.style("titleField").fillHorizontal().setGridwidth(2);
        panel.style("widefield").fillHorizontal().setGridwidth(3);
        panel.style("fill").fillBoth().setGridwidth(3);
        panel.style("fill2").fillHorizontal().setGridwidth(3);

        initTitleModel(disallowedStateNames);
        JComponent titleView = ControllerFactory.createComponent(myTitleModel);
        if (titleView instanceof JTextComponent)
        {
            ((JTextComponent)titleView).selectAll();
        }
        titleView.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    getDialogButtonPanel().getButton(ButtonPanel.OK).doClick();
                }
            }
        });
        panel.style("label", "titleField").addRow(new JLabel("Title:"), titleView);

        myDescriptionField.setToolTipText("A description makes your data easier to search");
        panel.style("label").addRow(new JLabel("Description:"));
        panel.style("widefield").addRow(myDescriptionField);

        panel.style("label").addRow(new JLabel("Tags:"));
        myTagsField.setToolTipText("Tags aid in searching and organizing state files");
        panel.style("widefield").addRow(myTagsField);

        final JCheckBox chooseModulesCheckbox = new JCheckBox(moduleSelectionText);
        chooseModulesCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        panel.style("fill2").addRow(chooseModulesCheckbox);

        final JScrollPane scroll = new JScrollPane(myModuleTable);
        scroll.setPreferredSize(new Dimension(400, 200));
        scroll.setVisible(false);

        panel.style("fill").addRow(scroll);

        chooseModulesCheckbox.addActionListener(e ->
        {
            scroll.setVisible(chooseModulesCheckbox.isSelected());

            int width = getWidth();
            pack();
            setSize(width, getHeight());
        });

        setComponent(panel);

        setValidator(myValidatorSupport);

        myTitleModel.addListener((obs, ov, nv) -> myValidatorSupport.setValidationResult(myTitleModel.getValidatorSupport()));
        myTitleModel.addPropertyChangeListener(e ->
        {
            if (e.getProperty() == PropertyChangeEvent.Property.VALIDATION_CRITERIA)
            {
                myValidatorSupport.setValidationResult(myTitleModel.getValidatorSupport());
            }
        });
        myValidatorSupport.setValidationResult(myTitleModel.getValidatorSupport());
    }

    /**
     * Gets checkbox state of the all modules in the model.
     *
     * @param model the table model
     * @return the module selection states
     */
    private Map<String, Boolean> getModuleSelectionState(CheckBoxTableModel model)
    {
        Map<String, Boolean> moduleState = New.map();
        int rowCount = model.getRowCount();
        for (int i = 0; i < rowCount; i++)
        {
            String module = (String)model.getValueAt(i, 1);
            boolean checked = ((Boolean)model.getValueAt(i, 0)).booleanValue();
            moduleState.put(module, Boolean.valueOf(checked));
        }
        return moduleState;
    }

    /**
     * Initialize the title model.
     *
     * @param disallowedStateNames The disallowed state names.
     */
    private void initTitleModel(Collection<? extends String> disallowedStateNames)
    {
        myTitleModel.setName("Title");
        setDisallowedNames(disallowedStateNames);
        myTitleModel.set(StringUtilities.getUniqueName("state-", disallowedStateNames));
    }

    /**
     * Sets the state module dependencies.
     *
     * @param model the model
     * @param moduleState the module state
     * @param selectedModule the selected module
     * @param forward if true, the selected module has dependencies, so select
     *            all dependent modules also. if false, check if this module is
     *            one that another is dependent on and deselect that module.
     */
    private void setDependencies(CheckBoxTableModel model, Map<String, Boolean> moduleState, String selectedModule,
            boolean forward)
    {
        for (Entry<String, Boolean> entry : moduleState.entrySet())
        {
            Collection<? extends String> dependentModules = myStateDependencies.get(entry.getKey());
            if (dependentModules != null)
            {
                for (String dependantModule : dependentModules)
                {
                    if (forward)
                    {
                        if (moduleState.get(selectedModule).booleanValue())
                        {
                            moduleState.put(dependantModule, Boolean.valueOf(forward));
                        }
                    }
                    else
                    {
                        if (selectedModule.equals(dependantModule) && !moduleState.get(selectedModule).booleanValue())
                        {
                            moduleState.put(entry.getKey(), Boolean.valueOf(forward));
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the module states.
     *
     * @param cbtm the table model
     * @param moduleState the module state
     */
    private void setModuleStates(CheckBoxTableModel cbtm, Map<String, Boolean> moduleState)
    {
        Collection<String> checked = New.collection();
        moduleState.entrySet().stream().filter(e -> e.getValue().booleanValue()).forEach(e -> checked.add(e.getKey()));
        // Remove the listener to prevent looping.
        myModuleTable.getModel().removeTableModelListener(myStateListener);
        cbtm.setCheckedValues(checked);
        myModuleTable.getModel().addTableModelListener(myStateListener);
    }
}
