package io.opensphere.core.util.swing;

import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

/**
 * The UI class for the ExtendedComboBox.
 *
 */
public class ExtendedComboBoxUI extends MetalComboBoxUI
{
    /**
     * The combox box.
     */
    private final ExtendedComboBox<?> myComboBox;

    /**
     * Constructs a new UI class.
     *
     * @param comboBox The ExtendedComboBox.
     */
    public ExtendedComboBoxUI(ExtendedComboBox<?> comboBox)
    {
        myComboBox = comboBox;
    }

    @Override
    protected ComboPopup createPopup()
    {
        return new ExtendedComboPopup(myComboBox);
    }
}
