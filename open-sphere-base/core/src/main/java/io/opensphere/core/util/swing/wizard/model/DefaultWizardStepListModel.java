package io.opensphere.core.util.swing.wizard.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Model for the steps of a wizard.
 */
public class DefaultWizardStepListModel implements WizardStepListModel
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The change support. */
    private final transient ChangeSupport<WizardStepListModelChangeListener> myChangeSupport = StrongChangeSupport.create();

    /** The current step index. */
    private int myCurrentStep;

    /** The current state of each step. */
    private final List<StepState> myStepStates;

    /** The titles for the wizard steps. */
    private final List<? extends String> myStepTitles;

    /**
     * Constructor.
     *
     * @param stepTitles The titles for the states.
     * @param initialStep The initial step.
     */
    public DefaultWizardStepListModel(List<? extends String> stepTitles, int initialStep)
    {
        myStepTitles = New.unmodifiableList(stepTitles);
        myStepStates = Arrays.asList(new StepState[myStepTitles.size()]);
        Collections.fill(myStepStates, StepState.INDETERMINATE);
        setCurrentStep(initialStep);
    }

    /**
     * Constructor.
     *
     * @param stepTitles The titles for the states.
     * @param initialStepTitle The initial step title.
     */
    public DefaultWizardStepListModel(List<? extends String> stepTitles, String initialStepTitle)
    {
        myStepTitles = New.unmodifiableList(stepTitles);
        myStepStates = Arrays.asList(new StepState[myStepTitles.size()]);
        Collections.fill(myStepStates, StepState.INDETERMINATE);
        setCurrentStep(initialStepTitle);
    }

    @Override
    public boolean allStepsAreValid()
    {
        for (int index = 0; index < myStepStates.size(); ++index)
        {
            if (myStepStates.get(index) != StepState.VALID && myStepStates.get(index) != StepState.WARNING)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void decrementStep()
    {
        setCurrentStep(getCurrentStep() - 1);
    }

    @Override
    public ChangeSupport<WizardStepListModelChangeListener> getChangeSupport()
    {
        return myChangeSupport;
    }

    @Override
    public int getCurrentStep()
    {
        return myCurrentStep;
    }

    @Override
    public String getCurrentStepTitle()
    {
        return myStepTitles.get(myCurrentStep);
    }

    @Override
    public final int getStepCount()
    {
        return myStepTitles.size();
    }

    @Override
    public StepState getStepState(int step)
    {
        return myStepStates.get(assertValidStep(step));
    }

    @Override
    public StepState getStepState(String stepTitle)
    {
        return getStepState(myStepTitles.indexOf(stepTitle));
    }

    @Override
    public String getStepTitle(int index)
    {
        return myStepTitles.get(assertValidStep(index));
    }

    @Override
    public List<? extends String> getStepTitles()
    {
        return myStepTitles;
    }

    @Override
    public void incrementStep()
    {
        setCurrentStep(getCurrentStep() + 1);
    }

    @Override
    public final void setCurrentStep(int step)
    {
        if (doSetCurrentStep(step))
        {
            notifyCurrentStepChanged(step);
        }
    }

    @Override
    public final void setCurrentStep(String stepTitle)
    {
        setCurrentStep(myStepTitles.indexOf(stepTitle));
    }

    @Override
    public void refreshCurrentStep()
    {
        notifyCurrentStepChanged(getCurrentStep());
    }

    @Override
    public void setStepState(int step, StepState state)
    {
        if (doSetStepState(step, state))
        {
            notifyStepStateChanged(step, state);
        }
    }

    @Override
    public void setStepState(String stepTitle, StepState state)
    {
        setStepState(myStepTitles.indexOf(stepTitle), state);
    }

    /**
     * Set the current step without notifying listeners.
     *
     * @param step The step index.
     * @return {@code true} if the step was changed.
     */
    protected final boolean doSetCurrentStep(int step)
    {
        if (myCurrentStep != step)
        {
            myCurrentStep = assertValidStep(step);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Set the step state without notifying listeners.
     *
     * @param step The step index.
     * @param state The state for the step.
     * @return {@code true} if the state was changed.
     */
    protected boolean doSetStepState(int step, StepState state)
    {
        StepState old = myStepStates.set(assertValidStep(step), Utilities.checkNull(state, "state"));
        return !Utilities.sameInstance(state, old);
    }

    /**
     * Notify listeners that the current step has changed.
     *
     * @param step The new step.
     */
    protected final void notifyCurrentStepChanged(final int step)
    {
        final String stepTitle = myStepTitles.get(step);
        myChangeSupport.notifyListeners(new Callback<WizardStepListModelChangeListener>()
        {
            @Override
            public void notify(WizardStepListModelChangeListener listener)
            {
                listener.currentStepChanged(DefaultWizardStepListModel.this, step, stepTitle);
            }
        });
    }

    /**
     * Notify listeners that a step state has changed.
     *
     * @param step The step index.
     * @param state The new state.
     */
    protected final void notifyStepStateChanged(final int step, final StepState state)
    {
        final String stepTitle = myStepTitles.get(step);
        myChangeSupport.notifyListeners(new Callback<WizardStepListModelChangeListener>()
        {
            @Override
            public void notify(WizardStepListModelChangeListener listener)
            {
                listener.stepStateChanged(DefaultWizardStepListModel.this, step, stepTitle, state);
            }
        });
    }

    /**
     * Verify that a step is valid.
     *
     * @param step The step index.
     * @return The step, if valid.
     * @throws IllegalArgumentException If the step is not valid.
     */
    private int assertValidStep(int step)
    {
        if (step < 0 || step >= getStepCount())
        {
            throw new IllegalArgumentException("Step out of range: " + step);
        }
        return step;
    }
}
