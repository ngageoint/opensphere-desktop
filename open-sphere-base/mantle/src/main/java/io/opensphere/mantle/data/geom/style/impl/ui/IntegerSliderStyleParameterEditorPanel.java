package io.opensphere.mantle.data.geom.style.impl.ui;

import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class IntegerSliderStyleParameterEditorPanel.
 *
 * Same as the float slider parameter editor panel but set to work only with
 * integer values.
 */
@SuppressWarnings("PMD.AvoidUsingShortType")
public class IntegerSliderStyleParameterEditorPanel extends FloatSliderStyleParameterEditorPanel
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new abstract style parameter editor panel.
     *
     * @param label the label
     * @param style the style
     * @param paramKey the param key
     * @param previewable the previewable
     * @param textEntry the text entry
     * @param min the min
     * @param max the max
     * @param unit the unit
     */
    public IntegerSliderStyleParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            boolean previewable, boolean textEntry, int min, int max, String unit)
    {
        super(label, style, paramKey, previewable, textEntry, min, max, new IntegerOnlyConvertor(unit));
    }

    /**
     * Sets the parameter.
     *
     * @param sliderVal the new parameter
     */
    @Override
    protected void setParameter(int sliderVal)
    {
        Object newVal = null;
        Class<?> t = getParameter().getValueType();
        if (t == Integer.class)
        {
            newVal = Integer.valueOf(sliderVal);
        }
        else if (t == Long.class)
        {
            newVal = Long.valueOf(sliderVal);
        }
        else if (t == Short.class)
        {
            newVal = Short.valueOf((short)sliderVal);
        }
        else if (t == Byte.class)
        {
            newVal = Byte.valueOf((byte)sliderVal);
        }
        else if (t == Float.class)
        {
            newVal = Float.valueOf(sliderVal);
        }
        else if (t == Double.class)
        {
            newVal = Double.valueOf(sliderVal);
        }

        setParamValue(newVal);
    }
}
