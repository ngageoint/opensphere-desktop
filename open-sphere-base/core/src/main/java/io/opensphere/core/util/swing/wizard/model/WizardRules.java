package io.opensphere.core.util.swing.wizard.model;

/**
 * Rules that determine what navigation can be executed on the wizard.
 */
public interface WizardRules
{
    /**
     * Get if the wizard can finish.
     *
     * @return {@code true} if the wizard can finish.
     */
    boolean canFinish();

    /**
     * Get if the wizard can go to the next step.
     *
     * @return {@code true} if the wizard can go to the next step.
     */
    boolean canGoNext();

    /**
     * Get if the wizard can go to the previous step.
     *
     * @return {@code true} if the wizard can go to the previous step.
     */
    boolean canGoPrevious();
}
