package io.opensphere.mantle.util;

import java.awt.Component;

import javax.swing.JCheckBox;

import io.opensphere.core.util.swing.OptionDialog;

/**
 * Activation dialog.
 */
public class ActivationDialog extends OptionDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The active checkbox. */
    private JCheckBox myActiveCheckBox;

    /**
     * Constructor.
     *
     * @param owner The owner
     * @param title The title
     * @param component The component
     * @param errorStrategy the errorStrategy
     */
    public ActivationDialog(Component owner, String title, Component component, ErrorStrategy errorStrategy)
    {
        super(owner, component, title);
        setErrorStrategy(errorStrategy);
        init();
    }

    /**
     * Checks if is activated.
     *
     * @return true, if is activated
     */
    public boolean isActivated()
    {
        return myActiveCheckBox.isSelected();
    }

    /**
     * Sets the activated.
     *
     * @param isActivated the new activated
     */
    public void setActivated(boolean isActivated)
    {
        myActiveCheckBox.setSelected(isActivated);
    }

    /**
     * Init.
     */
    private void init()
    {
        myActiveCheckBox = new JCheckBox("Active", true);
        getContentButtonPanel().add(myActiveCheckBox);
    }
}
