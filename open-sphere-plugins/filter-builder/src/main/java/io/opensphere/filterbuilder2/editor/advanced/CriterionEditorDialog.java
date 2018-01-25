package io.opensphere.filterbuilder2.editor.advanced;

import java.awt.Component;

import javax.swing.JOptionPane;

import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent.Property;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;
import io.opensphere.filterbuilder2.common.Constants;
import io.opensphere.filterbuilder2.editor.common.CriterionEditorPanel;
import io.opensphere.filterbuilder2.editor.model.CriterionModel;

/**
 * The dialog for editing a single criterion in advanced mode.
 */
public final class CriterionEditorDialog extends OptionDialog
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private final CriterionModel myModel;

    /** The copy of the model. */
    private final CriterionModel myModelCopy;

    /**
     * Show the dialog.
     *
     * @param parent the parent
     * @param model the criterion model
     */
    public static void showDialog(Component parent, CriterionModel model)
    {
        CriterionEditorDialog dialog = new CriterionEditorDialog(parent, model);
        dialog.getRootPane().setDefaultButton(dialog.getDialogButtonPanel().getButton(ButtonPanel.OK));
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            dialog.applyChanges();
        }
    }

    /**
     * Constructor.
     *
     * @param parent the parent
     * @param model the criterion model
     */
    private CriterionEditorDialog(Component parent, CriterionModel model)
    {
        super(parent);
        setTitle("Edit " + Constants.EXPRESSION);
        myModel = model;
        myModelCopy = new CriterionModel(model);
        final CriterionEditorPanel panel = new CriterionEditorPanel(myModelCopy, 12);
        setComponent(panel);

        myModelCopy.getCriterionMaxValue().addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void stateChanged(PropertyChangeEvent e)
            {
                if (e.getProperty() == Property.VISIBLE)
                {
                    packLater();
                }
            }
        });
    }

    /**
     * Applies the changes made by the user.
     */
    private void applyChanges()
    {
        myModel.setFromModel(myModelCopy);
    }

    /**
     * Invoke pack later to ensure the panel has time to rebuild before packing.
     */
    private void packLater()
    {
        EventQueueUtilities.invokeLater(this::pack);
    }
}
