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

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
public class FloatSliderStyleParameterEditorPanel extends AbstractStyleParameterEditorPanel
        implements ChangeListener, ActionListener
{
    /** The Constant SHOW_SLIDER_LABELS. */
    public static final String SHOW_SLIDER_LABELS = "SHOW_SLIDER_LABELS";

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Convertor. */
    private final IntFloatConvertor myConvertor;

    /** The Min. */
    private final double myMin;

    /** The Slider. */
    private final JSlider mySlider;

    /** The Slider panel. */
    private final JPanel mySliderPanel;

    /** The Text entry. */
    private final boolean myTextEntry;

    /** The Text field. */
    private final JTextField myTextField;

    /** The Timer. */
    private final Timer myTimer;

    /** The Value. */
    private final JLabel myValueLabel;

//    /** The Max. */
//    private final double myMax;

    /**
     * Creates the label.
     *
     * @param sliderValue the slider value
     * @param convertor the convertor
     * @return the string
     */
    private static String createLabelWithUnit(int sliderValue, IntFloatConvertor convertor)
    {
        String unit = convertor.getUnit() == null ? "" : " (" + convertor.getUnit() + ")";
        return convertor.labelValue(convertor.intToFloat(sliderValue)) + unit;
    }

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param builder the label
     * @param style the style
     * @param paramKey the param key
     * @param previewable the previewable
     * @param textEntry the text entry
     * @param min the min
     * @param max the max
     * @param convertor the convertor
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public FloatSliderStyleParameterEditorPanel(PanelBuilder builder, MutableVisualizationStyle style, String paramKey,
            boolean previewable, boolean textEntry, double min, double max, IntFloatConvertor convertor)
    {
        super(builder, style, paramKey);
        Utilities.checkNull(convertor, "convertor");
        myConvertor = convertor;
        myTextEntry = textEntry;
//        myMax = max;
        myMin = min;

        int iMax = myConvertor.floatToInt(max);
        int iMin = myConvertor.floatToInt(min);
        int initialVal = myConvertor.floatToInt(getParameterValue());

        mySlider = new JSlider(iMin, iMax, initialVal);
        Dictionary<Integer, Component> ht = new Hashtable<>(2);
        ht.put(Integer.valueOf(iMin), new JLabel(myConvertor.labelValue(min)));
        ht.put(Integer.valueOf(iMax), new JLabel(myConvertor.labelValue(max)));
        mySlider.setLabelTable(ht);
        mySlider.setPaintLabels((Boolean)myPanelBuilder.getOtherParameter(SHOW_SLIDER_LABELS, Boolean.TRUE));

        myValueLabel = new JLabel(myTextEntry ? myConvertor.getUnit() : createLabelWithUnit(initialVal, myConvertor));
        myTextField = new JTextField(myConvertor.labelValue(myConvertor.intToFloat(mySlider.getValue())));
        myTextField.addActionListener(this);
        myTextField.setMinimumSize(new Dimension(80, 20));

        Box aBox = Box.createHorizontalBox();
        Component c = getPrefixComponent(myPanelBuilder);
        if (c != null)
        {
            aBox.add(c);
        }
        mySliderPanel = new JPanel();
        mySliderPanel.setLayout(new BoxLayout(mySliderPanel, BoxLayout.X_AXIS));
        mySliderPanel.add(Box.createHorizontalStrut(5));

        if (myTextEntry)
        {
            JPanel subPanel = new JPanel(new GridLayout(1, 2, 5, 0));
            subPanel.add(mySlider);
            subPanel.add(myTextField);
            mySliderPanel.add(subPanel);
            mySliderPanel.add(Box.createHorizontalStrut(5));

            if (myConvertor.getUnit() != null)
            {
                mySliderPanel.add(myValueLabel);
            }
        }
        else
        {
            mySliderPanel.add(mySlider);
            mySliderPanel.add(Box.createHorizontalStrut(5));
            mySliderPanel.add(myValueLabel);
        }
        mySliderPanel.add(Box.createHorizontalGlue());
        aBox.add(mySliderPanel);

        myControlPanel.setLayout(new BorderLayout());
        myControlPanel.add(aBox, BorderLayout.CENTER);
        mySlider.addChangeListener(this);
        myTimer = new Timer(300, this);
        myTimer.setRepeats(false);

        setMaximumSize(new Dimension(1000, getPanelHeightFromBuilder()));
        setPreferredSize(new Dimension(100, getPanelHeightFromBuilder()));
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myTimer)
        {
            int sliderVal = mySlider.getValue();
            setParameter(sliderVal);
        }
        else if (e.getSource() == myTextField)
        {
            String text = myTextField.getText();
            double dVal = 0.0;
            try
            {
                double max = myConvertor.intToFloat(mySlider.getMaximum());
                double min = myConvertor.intToFloat(mySlider.getMinimum());
                dVal = Double.parseDouble(text);
//                myValueLabel.setText(myTextEntry ? myConvertor.getUnit() : createLabelWithUnit(value, myConvertor));
                if (dVal > max)
                {
                    dVal = max;
                }
                else if (dVal < min)
                {
                    dVal = min;
                }
                mySlider.setValue(myConvertor.floatToInt(dVal));
            }
            catch (NumberFormatException ex)
            {
                myTextField.setText(myConvertor.labelValue(myConvertor.intToFloat(mySlider.getValue())));
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == mySlider)
        {
            myTimer.restart();
            int value = mySlider.getValue();
            updateValueLabel(value);
        }
    }

    /**
     * Update.
     */
    @Override
    public void update()
    {
        double sliderValue = getParameterValue();
        if (myConvertor.intToFloat(mySlider.getValue()) != sliderValue)
        {
            final double fSliderValue = sliderValue;
            EventQueueUtilities.runOnEDT(new Runnable()
            {
                @Override
                public void run()
                {
                    mySlider.removeChangeListener(FloatSliderStyleParameterEditorPanel.this);
                    if (myConvertor.intToFloat(mySlider.getValue()) != fSliderValue)
                    {
                        mySlider.setValue(myConvertor.floatToInt(fSliderValue));
                    }
                    mySlider.addChangeListener(FloatSliderStyleParameterEditorPanel.this);
                    myTextField.setText(myConvertor.labelValue(myConvertor.intToFloat(mySlider.getValue())));
                    myValueLabel
                            .setText(myTextEntry ? myConvertor.getUnit() : createLabelWithUnit(mySlider.getValue(), myConvertor));
                }
            });
        }
    }

    /**
     * Gets the prefix component.
     *
     * @param builder the builder
     * @return the prefix component
     */
    protected Component getPrefixComponent(PanelBuilder builder)
    {
        return null;
    }

    /**
     * Gets the slider panel.
     *
     * @return the slider panel
     */
    protected JPanel getSliderPanel()
    {
        return mySliderPanel;
    }

    /**
     * Sets the parameter.
     *
     * @param sliderVal the new parameter
     */
    protected void setParameter(int sliderVal)
    {
        Object newVal = null;
        Class<?> t = getParameter().getValueType();
        if (t == Float.class)
        {
            newVal = Float.valueOf((float)myConvertor.intToFloat(sliderVal));
        }
        else if (t == Double.class)
        {
            newVal = Double.valueOf(myConvertor.intToFloat(sliderVal));
        }

        setParamValue(newVal);
    }

    /**
     * Gets the parameter value.
     *
     * @return the parameter value
     */
    private double getParameterValue()
    {
        double sliderValue = myMin;
        Object value = getParamValue();
        if (value instanceof Number)
        {
            sliderValue = ((Number)value).doubleValue();
        }
        return sliderValue;
    }

    /**
     * Update value label.
     *
     * @param value the value
     */
    private void updateValueLabel(final int value)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myValueLabel.setText(myTextEntry ? myConvertor.getUnit() : createLabelWithUnit(value, myConvertor));
                myTextField.setText(myConvertor.labelValue(myConvertor.intToFloat(value)));
            }
        });
    }

    /**
     * The Class BasicIntFloatConvertor.
     */
    public static class BasicIntFloatConvertor implements IntFloatConvertor
    {
        /** The Adjust factor. */
        private final double myAdjustFactor;

        /** The String format. */
        private final String myStringFormat;

        /** The Unit. */
        private final String myUnit;

        /**
         * Instantiates a new basic int float convertor.
         *
         * @param decimalsOfPrecision the decimals of precision
         * @param unit the unit
         */
        public BasicIntFloatConvertor(int decimalsOfPrecision, String unit)
        {
            this(decimalsOfPrecision, unit, "%." + Integer.toString(decimalsOfPrecision) + "f");
        }

        /**
         * Instantiates a new basic int float convertor.
         *
         * @param decimalsOfPrecision the decimals of precision
         * @param unit the unit
         * @param labelStringFormat the label string format
         */
        public BasicIntFloatConvertor(int decimalsOfPrecision, String unit, String labelStringFormat)
        {
            myUnit = unit;
            myStringFormat = labelStringFormat;
            myAdjustFactor = Math.pow(10.0, decimalsOfPrecision);
        }

        @Override
        public int floatToInt(double val)
        {
            return (int)Math.floor(val * myAdjustFactor);
        }

        /**
         * Gets the string format.
         *
         * @return the string format
         */
        public String getStringFormat()
        {
            return myStringFormat;
        }

        @Override
        public String getUnit()
        {
            return myUnit;
        }

        @Override
        public double intToFloat(int val)
        {
            return val / myAdjustFactor;
        }

        @Override
        public String labelValue(double val)
        {
            return String.format(myStringFormat, val);
        }
    }

    /**
     * The Class IntegerOnlyConvertor.
     */
    public static class IntegerOnlyConvertor implements IntFloatConvertor
    {
        /** The Unit. */
        private final String myUnit;

        /**
         * Instantiates a new integer convertor.
         *
         * @param unit the unit
         */
        public IntegerOnlyConvertor(String unit)
        {
            myUnit = unit;
        }

        @Override
        public int floatToInt(double val)
        {
            return (int)Math.floor(val);
        }

        @Override
        public String getUnit()
        {
            return myUnit;
        }

        @Override
        public double intToFloat(int val)
        {
            return val;
        }

        @Override
        public String labelValue(double val)
        {
            return Integer.toString((int)Math.floor(val));
        }
    }

    /**
     * The Interface IntFloatConvertor.
     */
    public interface IntFloatConvertor
    {
        /**
         * Float to int.
         *
         * @param val the val
         * @return the int
         */
        int floatToInt(double val);

        /**
         * Gets the unit.
         *
         * @return the unit
         */
        String getUnit();

        /**
         * Int to float.
         *
         * @param val the val
         * @return the double
         */
        double intToFloat(int val);

        /**
         * Label value.
         *
         * @param val the val
         * @return the string
         */
        String labelValue(double val);
    }
}
