package io.opensphere.core.util.swing.wizard.model;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;

/**
 * Basic rules that do not allow navigating outside the defined steps, do not
 * allow navigating to disabled steps, and only allow finishing if all steps are
 * valid.
 */
public class DefaultWizardRules implements WizardRules
{
    /** The wizard step list model. */
    private final WizardStepListModel myStepModel;

    /**
     * Constructor.
     *
     * @param stepModel The step list model.
     */
    public DefaultWizardRules(WizardStepListModel stepModel)
    {
        myStepModel = Utilities.checkNull(stepModel, "stepModel");
    }

    @Override
    public boolean canFinish()
    {
        return myStepModel.allStepsAreValid();
    }

    @Override
    public boolean canGoNext()
    {
        return myStepModel.getCurrentStep() < myStepModel.getStepCount() - 1
                && myStepModel.getStepState(myStepModel.getCurrentStep() + 1) != StepState.DISABLED;
    }

    @Override
    public boolean canGoPrevious()
    {
        return myStepModel.getCurrentStep() > 0
                && myStepModel.getStepState(myStepModel.getCurrentStep() - 1) != StepState.DISABLED;
    }
}
