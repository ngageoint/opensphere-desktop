package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Collection;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.LengthBinding;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * A length AbstractStyleParameterEditorPanel with more control.
 */
public class AdvancedLengthParameterEditorPanel extends AbstractStyleParameterEditorPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The maximum value spinner. */
    private final JSpinner myMaxValueSpinner;

    /** The length slider. */
    private final JSlider myLengthSlider;

    /** A binding to the current {@link #myLengthSlider} value. */
    private final LengthBinding myLengthBinding;

    /** The value label. */
    private final JLabel myValueLabel;

    /** The units combo box. */
    private final LengthUnitsComboBox myUnitsCombo;

    /**
     * Constructor.
     *
     * @param label the {@link PanelBuilder}
     * @param style the style
     * @param paramKey the param key
     * @param unitOptions the unit options
     */
    public AdvancedLengthParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            Collection<Class<? extends Length>> unitOptions)
    {
        super(label, style, paramKey);

        Length value = (Length)getParamValue();
        int magnitude = (int)value.getMagnitude();
        int maxValue = Math.max(2 * magnitude, 1);

        myLengthBinding = new LengthBinding(value);

        myMaxValueSpinner = new JSpinner(new SpinnerNumberModel(maxValue, 1, 9_999_999, 1));
        JFormattedTextField textField = ((JSpinner.DefaultEditor)myMaxValueSpinner.getEditor()).getTextField();
        textField.setColumns(4);
        // Make typing in the text field actually work
        ((DefaultFormatter)textField.getFormatter()).setCommitsOnValidEdit(true);
        myMaxValueSpinner.setToolTipText("The maximum value of the slider");
        myMaxValueSpinner.addChangeListener(e -> updateSliderMax());

        myLengthSlider = new JSlider(0, maxValue, maxValue);
        myLengthSlider.setToolTipText("The length value");
        myLengthSlider.addChangeListener(e -> handleValueChange());
        myValueLabel = new JLabel();

        myUnitsCombo = new LengthUnitsComboBox(unitOptions);
        myUnitsCombo.addActionListener(e -> handleValueChange());
        ComponentUtilities.setPreferredHeight(myUnitsCombo, 24);

        GridBagPanel panel = new GridBagPanel();
        panel.fillNone().add(myMaxValueSpinner);
        panel.fillHorizontal().add(myLengthSlider);
        panel.fillNone().add(myValueLabel);
        panel.setInsets(0, 4, 0, 0).fillNone().add(myUnitsCombo);

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);

        update();
    }

    /**
     * Retrieves {@link #myLengthBinding}.
     *
     * @return the binding
     */
    public LengthBinding getLengthBinding()
    {
        return myLengthBinding;
    }

    @Override
    public final void update()
    {
        assert EventQueue.isDispatchThread();

        Object value = getParamValue();
        if (value instanceof Length)
        {
            Length length = (Length)value;
            int magnitude = (int)length.getMagnitude();
            if (magnitude > getMaxValue())
            {
                SwingUtilities.setSpinnerValue(myMaxValueSpinner, Integer.valueOf(magnitude));
            }
            SwingUtilities.setComboBoxValue(myUnitsCombo, value.getClass());
            SwingUtilities.setSliderValue(myLengthSlider, magnitude);
        }
    }

    /**
     * Updates the maximum value of the slider.
     */
    private void updateSliderMax()
    {
        myLengthSlider.setMaximum(getMaxValue());
    }

    /**
     * Handles a change in the slider value.
     */
    private void handleValueChange()
    {
        Length value = getValue();
        setParamValue(value);
        // Our binding is to a LOB arrow; the maximum size of the arrow head is
        // half the size of the line.
        myLengthBinding.changeLength(value.multiplyBy(0.5));
        myValueLabel.setText(String.valueOf((int)value.getMagnitude()));
    }

    /**
     * Gets the current length value.
     *
     * @return the length value
     */
    private Length getValue()
    {
        int magnitude = myLengthSlider.getValue();
        @SuppressWarnings("unchecked")
        Class<? extends Length> unit = (Class<? extends Length>)myUnitsCombo.getSelectedItem();
        Length value = Length.create(unit, magnitude);
        return value;
    }

    /**
     * Gets the value from the max value spinner.
     *
     * @return the max value
     */
    private int getMaxValue()
    {
        return ((Integer)myMaxValueSpinner.getValue()).intValue();
    }
}
