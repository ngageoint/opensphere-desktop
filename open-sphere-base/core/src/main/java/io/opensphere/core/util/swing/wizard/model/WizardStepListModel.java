package io.opensphere.core.util.swing.wizard.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.util.ChangeSupport;

/**
 * Model for the steps of a wizard.
 */
public interface WizardStepListModel extends Serializable
{
    /**
     * Get if all the steps are in the {@link StepState#VALID} state.
     *
     * @return If all steps are valid.
     */
    boolean allStepsAreValid();

    /**
     * Set the previous step to be the current step.
     */
    void decrementStep();

    /**
     * Get the change support.
     *
     * @return The change support.
     */
    ChangeSupport<WizardStepListModelChangeListener> getChangeSupport();

    /**
     * Get the current step.
     *
     * @return The current step index.
     */
    int getCurrentStep();

    /**
     * Get the current step title.
     *
     * @return The title for the current step.
     */
    String getCurrentStepTitle();

    /**
     * Get the number of steps.
     *
     * @return The step count.
     */
    int getStepCount();

    /**
     * Get the state of a step.
     *
     * @param step The step index.
     * @return The state.
     */
    StepState getStepState(int step);

    /**
     * Get the state of a step.
     *
     * @param stepTitle The step title.
     * @return The state.
     */
    StepState getStepState(String stepTitle);

    /**
     * Get the title for a step.
     *
     * @param index The index of the step.
     * @return The step title.
     */
    String getStepTitle(int index);

    /**
     * Get the step titles.
     *
     * @return The step titles.
     */
    List<? extends String> getStepTitles();

    /**
     * Set the next step to be the current step.
     */
    void incrementStep();

    /**
     * Set the current step.
     *
     * @param step The step index.
     */
    void setCurrentStep(int step);

    /**
     * Refresh current step.
     */
    void refreshCurrentStep();

    /**
     * Set the current step.
     *
     * @param stepTitle The step title.
     */
    void setCurrentStep(String stepTitle);

    /**
     * Set the step state.
     *
     * @param step The step index.
     * @param state The state for the step.
     */
    void setStepState(int step, StepState state);

    /**
     * Set the step state.
     *
     * @param stepTitle The step title.
     * @param state The state for the step.
     */
    void setStepState(String stepTitle, StepState state);

    /** Available states for each step. */
    enum StepState
    {
        /** The step cannot be made current yet. */
        DISABLED,

        /** The step can be visited, but has not been validated yet. */
        INDETERMINATE,

        /** The step has invalid information and requires attention. */
        INVALID,

        /**
         * The step is valid however there are warning that the user may or may
         * not want to address.
         */
        WARNING,

        /** The step has been validated. */
        VALID;
    }

    /** Listener interested in changes to the model. */
    public interface WizardStepListModelChangeListener
    {
        /**
         * The current step has changed.
         *
         * @param source The changed model.
         * @param step The new step index.
         * @param stepTitle The title of the new step.
         */
        void currentStepChanged(WizardStepListModel source, int step, String stepTitle);

        /**
         * A step state has changed.
         *
         * @param source The changed model.
         * @param step The step index.
         * @param stepTitle The step title.
         * @param state The new state.
         */
        void stepStateChanged(WizardStepListModel source, int step, String stepTitle, StepState state);
    }
}
