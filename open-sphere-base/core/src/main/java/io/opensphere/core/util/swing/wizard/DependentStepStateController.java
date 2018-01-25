package io.opensphere.core.util.swing.wizard;

import java.util.Collection;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModel.StepState;
import io.opensphere.core.util.swing.wizard.model.WizardStepListModelChangeAdapter;

/**
 * A listener for changes to a {@link WizardStepListModel} that will update the
 * state of a particular step (the "target step") based on the states of the
 * other specified steps in the model.
 * <p>
 * If the current step is not the target step and any other specified steps are
 * not {@link StepState#VALID}, set the target step to
 * {@link StepState#DISABLED}. If all other steps are {@link StepState#VALID}
 * and the target step is {@link StepState#DISABLED}, set it to
 * {@link StepState#INDETERMINATE}.
 */
public class DependentStepStateController extends WizardStepListModelChangeAdapter
{
    /** Flag used to ignore changes to the model initiated by me. */
    private boolean myAdjusting;

    /**
     * The titles for the steps that the target step depends on. If this is
     * {@code null}, all other steps will be considered.
     */
    private final Collection<? extends String> myOtherStepTitles;

    /** The title for the "target" step. */
    private final String myTargetStepTitle;

    /**
     * Construct a controller that uses all other steps in the model to
     * determine the state of the target step.
     *
     * @param targetStepTitle The title for the "target" step.
     */
    public DependentStepStateController(String targetStepTitle)
    {
        this(targetStepTitle, (Collection<? extends String>)null);
    }

    /**
     * Construct a controller that uses some specified steps to determine the
     * state of the target step.
     *
     * @param targetStepTitle The title for the "target" step.
     * @param otherStepTitles The titles for the other steps that the target
     *            depends on.
     */
    public DependentStepStateController(String targetStepTitle, Collection<? extends String> otherStepTitles)
    {
        myTargetStepTitle = targetStepTitle;
        myOtherStepTitles = otherStepTitles == null ? null : New.unmodifiableCollection(otherStepTitles);
    }

    @Override
    public void stepStateChanged(WizardStepListModel source, int changedStep, String changedStepTitle, StepState state)
    {
        if (myAdjusting)
        {
            return;
        }

        myAdjusting = true;
        try
        {
            // If the finish step is not the current step, set it to DISABLED if
            // any other steps are not VALID. Otherwise set it to INDETERMINATE
            // if it's DISABLED.
            if (!myTargetStepTitle.equals(source.getCurrentStepTitle()))
            {
                boolean forceToDisabled = false;
                Collection<? extends String> stepTitles = myOtherStepTitles == null ? source.getStepTitles() : myOtherStepTitles;
                for (String step : stepTitles)
                {
                    if (!step.equals(myTargetStepTitle) && source.getStepState(step) != StepState.VALID
                            && source.getStepState(step) != StepState.WARNING)
                    {
                        forceToDisabled = true;
                        break;
                    }
                }
                if (forceToDisabled)
                {
                    source.setStepState(myTargetStepTitle, StepState.DISABLED);
                }
                else if (source.getStepState(myTargetStepTitle) == StepState.DISABLED)
                {
                    source.setStepState(myTargetStepTitle, StepState.INDETERMINATE);
                }
            }
        }
        finally
        {
            myAdjusting = false;
        }
    }
}
