package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.DefaultFormatter;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * A length AbstractStyleParameterEditorPanel with more control.
 */
public class AdvancedLengthParameterEditorPanel extends AbstractStyleParameterEditorPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The maximum value spinner. */
    private JSpinner myMaxValueSpinner;

    /** The length slider. */
    private final JSlider myLengthSlider;

    /** The value label. */
    private JLabel myValueLabel;

    /** The units combo box. */
    private JComboBox<Class<? extends Length>> myUnitsCombo;

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
        int maxValue = 2 * magnitude;

        myMaxValueSpinner = new JSpinner(new SpinnerNumberModel(maxValue, 1, 9_999_999, 1));
        JFormattedTextField textField = ((JSpinner.DefaultEditor)myMaxValueSpinner.getEditor()).getTextField();
        textField.setColumns(4);
        // Make typing in the text field actually work
        ((DefaultFormatter)textField.getFormatter()).setCommitsOnValidEdit(true);
        myMaxValueSpinner.setToolTipText("The maximum value of the slider");
        myMaxValueSpinner.addChangeListener(e -> updateSliderMax());

        myLengthSlider = new JSlider(0, maxValue, maxValue);
        myValueLabel = new JLabel();
        myLengthSlider.addChangeListener(e -> handleValueChange());

        myUnitsCombo = new JComboBox<>();
        for (Class<? extends Length> unit : unitOptions)
        {
            myUnitsCombo.addItem(unit);
        }
        BasicComboBoxRenderer renderer = new BasicComboBoxRenderer();
        myUnitsCombo.setRenderer(new ListCellRenderer<Class<? extends Length>>()
        {
            @Override
            public Component getListCellRendererComponent(JList<? extends Class<? extends Length>> list,
                    Class<? extends Length> value, int index, boolean isSelected, boolean cellHasFocus)
            {
                String displayValue = Length.create(value, 0.).getShortLabel(false);
                return renderer.getListCellRendererComponent(list, displayValue, index, isSelected, cellHasFocus);
            }
        });
        myUnitsCombo.addActionListener(e -> handleValueChange());

        GridBagPanel panel = new GridBagPanel();
        panel.fillNone().add(myMaxValueSpinner);
        panel.fillHorizontal().add(myLengthSlider);
        panel.fillNone().add(myValueLabel);
        panel.setInsets(0, 4, 0, 0).fillNone().add(myUnitsCombo);

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);

        update();
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
                myMaxValueSpinner.setValue(magnitude);
            }
            myUnitsCombo.setSelectedItem(value.getClass());
            myLengthSlider.setValue(magnitude);
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
