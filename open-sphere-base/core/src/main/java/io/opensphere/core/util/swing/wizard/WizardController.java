package io.opensphere.core.util.swing.wizard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.wizard.model.WizardRules;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;
import io.opensphere.core.util.swing.wizard.view.WizardDialog;

/**
 * A controller for a wizard dialog.
 * <p>
 * Typical usage:
 *
 * <pre>
 * WizardController wizardController = new WizardController(dialog);
 * wizardController.setRules(rules);
 * wizardController.setFinishAction(finishAction);
 * wizardController.startWizard();
 * </pre>
 */
public class WizardController
{
    /** Listener for button presses. */
    private final ActionListener myActionListener = new ActionListener()
    {
        @Override
        @SuppressWarnings("PMD.CollapsibleIfStatements")
        public void actionPerformed(ActionEvent e)
        {
            if (WizardDialog.NEXT_BUTTON_LABEL.equals(e.getActionCommand()))
            {
                myDialog.getStepPanel().getModel().incrementStep();
            }
            else if (WizardDialog.BACK_BUTTON_LABEL.equals(e.getActionCommand()))
            {
                myDialog.getStepPanel().getModel().decrementStep();
            }
            else if (WizardDialog.FINISH_BUTTON_LABEL.equals(e.getActionCommand()) && myFinishAction != null)
            {
                myFinishAction.run(WizardController.this);
            }
        }
    };

    /** The dialog that I control. */
    private final WizardDialog myDialog;

    /** Task to be called when the wizard finishes. */
    private WizardCallback myFinishAction;

    /** The rules governing wizard navigation. */
    private WizardRules myWizardRules;

    /**
     * Constructor.
     *
     * @param dialog The dialog being controlled.
     */
    public WizardController(WizardDialog dialog)
    {
        myDialog = Utilities.checkNull(dialog, "dialog");

        myDialog.addActionListener(myActionListener);

        myDialog.getStepPanel().getModel().getChangeSupport()
                .addListener(new WizardStepListModel.WizardStepListModelChangeListener()
                {
                    @Override
                    public void currentStepChanged(WizardStepListModel source, int step, String stepTitle)
                    {
                        setButtonsEnabled();
                    }

                    @Override
                    public void stepStateChanged(WizardStepListModel source, int step, String stepTitle, StepState state)
                    {
                        setButtonsEnabled();
                    }
                });
    }

    /**
     * Get the wizard dialog.
     *
     * @return The dialog.
     */
    public WizardDialog getDialog()
    {
        return myDialog;
    }

    /**
     * Set the task to be called on the EDT when the wizard finishes.
     *
     * @param callback The finish action.
     */
    public void setFinishAction(WizardCallback callback)
    {
        myFinishAction = callback;
    }

    /**
     * Set the wizard rules.
     *
     * @param rules The rules.
     */
    public void setRules(WizardRules rules)
    {
        myWizardRules = rules;
        setButtonsEnabled();
    }

    /**
     * Sets the minimum size of the dialog.
     *
     * @param width The width.
     * @param height The height.
     */
    public void setMinimumSize(int width, int height)
    {
        myDialog.setMinimumSize(new Dimension(width, height));
    }

    /**
     * Start the wizard dialog.
     */
    public void startWizard()
    {
        myDialog.buildAndShow();
    }

    /**
     * Set the dialog buttons enabled based on the wizard rules.
     */
    private void setButtonsEnabled()
    {
        if (myWizardRules != null)
        {
            myDialog.getFinishButton().setEnabled(myWizardRules.canFinish());
            myDialog.getNextButton().setEnabled(myWizardRules.canGoNext());
            myDialog.getBackButton().setEnabled(myWizardRules.canGoPrevious());
        }
    }
}
