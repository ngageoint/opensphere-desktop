package io.opensphere.core.util.swing.wizard.model;

import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.WizardStepListModelChangeListener;

/** Convenience adapter. */
public class WizardStepListModelChangeAdapter implements WizardStepListModelChangeListener
{
    @Override
    public void currentStepChanged(WizardStepListModel source, int step, String stepTitle)
    {
    }

    @Override
    public void stepStateChanged(WizardStepListModel source, int step, String stepTitle, StepState state)
    {
    }
}
