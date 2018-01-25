package io.opensphere.core.util.swing.wizard.model;

import java.awt.Component;

/** The model for the panels displayed for each wizard step. */
@FunctionalInterface
public interface WizardPanelModel
{
    /**
     * Get the wizard panel for a given step.
     *
     * @param stepTitle The title for the step.
     * @return The component.
     */
    Component getWizardPanel(String stepTitle);
}
