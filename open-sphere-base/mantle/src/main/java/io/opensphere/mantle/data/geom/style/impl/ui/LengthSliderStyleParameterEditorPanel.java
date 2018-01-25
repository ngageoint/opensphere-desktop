package io.opensphere.mantle.data.geom.style.impl.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * A panel for editing a length style parameter.
 */
public class LengthSliderStyleParameterEditorPanel extends AbstractStyleParameterEditorPanel
        implements ChangeListener, ActionListener
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

    /** The Text field. */
    private final JTextField myLengthTextField;

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

        int iMax = (int)Math.floor(Length.create(displayUnits, maxLength).getMagnitude());
        int iMin = (int)Math.ceil(Length.create(displayUnits, minLength).getMagnitude());
        int initialVal = (int)Math.round(getParameterValue().getMagnitude());

        myLengthSlider = new JSlider(iMin, iMax, initialVal);
        Dictionary<Integer, Component> ht = new Hashtable<>(2);
        ht.put(Integer.valueOf(iMin), new JLabel(Integer.toString(iMin)));
        ht.put(Integer.valueOf(iMax), new JLabel(Integer.toString(iMax)));
        myLengthSlider.setLabelTable(ht);
        myLengthSlider.setPaintLabels((Boolean)myPanelBuilder.getOtherParameter(SHOW_SLIDER_LABELS, Boolean.TRUE));

        myLengthValueLabel = new JLabel(myTextEntry ? Length.getLongLabel(displayUnits, true)
                : Length.create(displayUnits, initialVal).toLongLabelString());
        myLengthTextField = new JTextField(Integer.toString(myLengthSlider.getValue()));
        myLengthTextField.addActionListener(this);
        myLengthTextField.setMinimumSize(new Dimension(80, 20));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalStrut(5));

        if (myTextEntry)
        {
            JPanel subPanel = new JPanel(new GridLayout(1, 2, 5, 0));
            subPanel.add(myLengthSlider);
            subPanel.add(myLengthTextField);
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
        else if (e.getSource() == myLengthTextField)
        {
            String text = myLengthTextField.getText();
            double dVal = 0.0;
            try
            {
                double max = myLengthSlider.getMaximum();
                double min = myLengthSlider.getMinimum();
                dVal = Double.parseDouble(text);
                dVal = MathUtil.clamp(dVal, min, max);
                myLengthSlider.setValue((int)dVal);
            }
            catch (NumberFormatException ex)
            {
                myLengthTextField.setText(Integer.toString(myLengthSlider.getValue()));
            }
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
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myLengthTextField.setText(Integer.toString(value));
                myLengthValueLabel.setText(myTextEntry ? Length.getLongLabel(myDisplayUnits, true)
                        : Length.create(myDisplayUnits, value).toLongLabelString());
            }
        });
    }
}
