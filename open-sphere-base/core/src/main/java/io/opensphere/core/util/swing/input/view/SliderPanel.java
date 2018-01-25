package io.opensphere.core.util.swing.input.view;

import java.awt.Component;
import java.awt.GridBagConstraints;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JTextField;

import io.opensphere.core.util.swing.GridBagPanel;

/**
 * A panel with a slider and text box.
 */
public class SliderPanel extends GridBagPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Builds the UI.
     *
     * @param slider The slider
     * @param textField The text field
     */
    public void buildUI(JSlider slider, JTextField textField)
    {
        setFill(GridBagConstraints.HORIZONTAL).setWeightx(8);
        add(slider);
        setFill(GridBagConstraints.HORIZONTAL).setWeightx(2);
        textField.setColumns(3);
        add(textField);
    }

    @Override
    public void setOpaque(boolean isOpaque)
    {
        super.setOpaque(isOpaque);
        for (Component comp : getComponents())
        {
            if (comp instanceof JComponent)
            {
                ((JComponent)comp).setOpaque(isOpaque);
            }
        }
    }
}
