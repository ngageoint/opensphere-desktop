package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * A panel for editing a length style parameter.
 */
public class LengthSliderStyleParameterEditorPanel extends AbstractStyleParameterEditorPanel
        implements ChangeListener, ActionListener, io.opensphere.core.util.ChangeListener<Length>
{
    /** The Constant SHOW_SLIDER_LABELS. */
    public static final String SHOW_SLIDER_LABELS = "SHOW_SLIDER_LABELS";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Timer. */
    private final Timer myChangeTimer;

    /** The units for display. */
    private final Class<? extends Length> myDisplayUnits;

    /** The Slider. */
    private final JSlider myLengthSlider;

    /** The value Spinner. */
    private final JSpinner myLengthSpinner;

    /** The Value. */
    private final JLabel myLengthValueLabel;

    /** The units for the parameter. */
    private final Class<? extends Length> myParamUnits;

    /** The Text entry. */
    private final boolean myTextEntry;

    /**
     * Constructor.
     *
     * @param pb The label for the slider.
     * @param style The visualization style being modified.
     * @param paramKey The parameter key.
     * @param previewable Indicates if the style is previewable.
     * @param textEntry Indicates if text entry should be available.
     * @param minLength The minimum value.
     * @param maxLength The maximum value.
     * @param displayUnits The units used on the display.
     * @param paramUnits The units for the parameter value.
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    public LengthSliderStyleParameterEditorPanel(PanelBuilder pb, MutableVisualizationStyle style, String paramKey,
            boolean previewable, boolean textEntry, Length minLength, Length maxLength, Class<? extends Length> displayUnits,
            Class<? extends Length> paramUnits)
    {
        super(pb, style, paramKey);
        myTextEntry = textEntry;
        myDisplayUnits = displayUnits;
        myParamUnits = paramUnits;

        int iMax = Math.abs((int)Math.floor(Length.create(displayUnits, maxLength).getMagnitude()));
        int iMin = Math.abs((int)Math.ceil(Length.create(displayUnits, minLength).getMagnitude()));

        // Slider range exception means that these might be backwards.
        int realMax = Math.max(iMax, iMin);
        int realMin = Math.min(iMax, iMin);

        iMax = realMax;
        iMin = realMin;

        int initialVal = MathUtil.clamp((int)Math.round(getParameterValue().getMagnitude()), iMin, iMax);

        myLengthSlider = new JSlider(iMin, iMax, initialVal);
        Dictionary<Integer, Component> ht = new Hashtable<>(2);
        ht.put(Integer.valueOf(iMin), new JLabel(Integer.toString(iMin)));
        ht.put(Integer.valueOf(iMax), new JLabel(Integer.toString(iMax)));
        myLengthSlider.setLabelTable(ht);
        myLengthSlider
                .setPaintLabels(((Boolean)myPanelBuilder.getOtherParameter(SHOW_SLIDER_LABELS, Boolean.TRUE)).booleanValue());

        myLengthValueLabel = new JLabel(myTextEntry ? Length.getShortLabel(displayUnits, true)
                : Length.create(displayUnits, initialVal).toShortLabelString());

        myLengthSpinner = new JSpinner(new SpinnerNumberModel(initialVal, iMin, iMax, 1));
        JFormattedTextField textField = ((JSpinner.DefaultEditor)myLengthSpinner.getEditor()).getTextField();
        textField.setColumns(4);
        ((DefaultFormatter)textField.getFormatter()).setCommitsOnValidEdit(true);
        myLengthSpinner.addChangeListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(5));

        if (myTextEntry)
        {
            GridBagPanel subPanel = new GridBagPanel();
            subPanel.fillHorizontal().add(myLengthSlider);
            subPanel.fillNone().setInsets(0, 5, 0, 0).add(myLengthSpinner);
            panel.add(subPanel);
            panel.add(Box.createHorizontalStrut(5));
            panel.add(myLengthValueLabel);
        }
        else
        {
            panel.add(myLengthSlider);
            panel.add(Box.createHorizontalStrut(5));
            panel.add(myLengthValueLabel);
        }
        panel.add(Box.createHorizontalGlue());

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(panel, BorderLayout.CENTER);
        myLengthSlider.addChangeListener(this);
        myChangeTimer = new Timer(300, this);
        myChangeTimer.setRepeats(false);
        int panelHeight = getPanelHeightFromBuilder();
        setMaximumSize(new Dimension(1000, panelHeight));
        setPreferredSize(new Dimension(100, panelHeight));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myChangeTimer)
        {
            int sliderVal = myLengthSlider.getValue();
            setParameter(sliderVal);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == myLengthSlider)
        {
            myChangeTimer.restart();
            int value = myLengthSlider.getValue();
            updateValueLabel(value);
        }
        else if (e.getSource() == myLengthSpinner)
        {
            int val = ((Integer)myLengthSpinner.getValue()).intValue();
            int max = myLengthSlider.getMaximum();
            int min = myLengthSlider.getMinimum();
            val = MathUtil.clamp(val, min, max);

            myLengthSlider.setValue(val);
        }
    }

    @Override
    public void changed(ObservableValue<? extends Length> observable, Length oldValue, Length newValue)
    {
        int val = myLengthSlider.getValue();
        int convertedMax = Length.create(myDisplayUnits, newValue).getMagnitudeObj().intValue();
        convertedMax = Math.max(convertedMax, myLengthSlider.getMinimum());

        if (convertedMax < val)
        {
            val = convertedMax;
            myLengthSlider.setValue(val);
        }

        myLengthSlider.setMaximum(convertedMax);
        myLengthSpinner.setModel(new SpinnerNumberModel(val, myLengthSlider.getMinimum(), convertedMax, 1));
    }

    /**
     * Update.
     */
    @Override
    public final void update()
    {
        double paramValue = getParameterValue().getMagnitude();

        myLengthSlider.removeChangeListener(this);

        if (myLengthSlider.getValue() != paramValue)
        {
            updateValueLabel((int)paramValue);
            myLengthSlider.setValue((int)paramValue);
        }

        myLengthSlider.addChangeListener(this);
    }

    /**
     * Sets the parameter.
     *
     * @param sliderVal the new parameter
     */
    protected void setParameter(int sliderVal)
    {
        double value = Length.create(myParamUnits, Length.create(myDisplayUnits, sliderVal)).getMagnitude();
        Object objValue;
        Class<?> t = getParameter().getValueType();
        if (t == Float.class)
        {
            objValue = Float.valueOf((float)value);
        }
        else if (t == Double.class)
        {
            objValue = Double.valueOf(value);
        }
        else
        {
            objValue = null;
        }

        setParamValue(objValue);
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private Length getParameterValue()
    {
        double value;
        Number paramValue = (Number)getParamValue();
        if (paramValue == null)
        {
            value = myLengthSlider.getMinimum();
        }
        else
        {
            value = paramValue.doubleValue();
        }

        return Length.create(myDisplayUnits, Length.create(myParamUnits, value));
    }

    /**
     * Update value label.
     *
     * @param value the value
     */
    @SuppressWarnings("PMD.SimplifiedTernary")
    private void updateValueLabel(final int value)
    {
        EventQueueUtilities.runOnEDT(() ->
        {
            myLengthSpinner.setValue(Integer.valueOf(value));
            myLengthValueLabel.setText(myTextEntry ? Length.getShortLabel(myDisplayUnits, true)
                    : Length.create(myDisplayUnits, value).toShortLabelString());
        });
    }
}
