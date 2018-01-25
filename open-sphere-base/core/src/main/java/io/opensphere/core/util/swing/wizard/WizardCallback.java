package io.opensphere.core.util.swing.wizard;

/**
 * A functor to be called when a wizard is complete.
 */
@FunctionalInterface
public interface WizardCallback
{
    /**
     * Called on the EDT when the wizard is complete.
     *
     * @param controller The wizard controller.
     */
    void run(WizardController controller);
}
