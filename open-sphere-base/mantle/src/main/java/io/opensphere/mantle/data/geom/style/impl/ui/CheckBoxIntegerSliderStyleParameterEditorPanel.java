package io.opensphere.mantle.data.geom.style.impl.ui;

import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;

/**
 * The Class AbstractStyleParameterEditorPanel.
 */
@SuppressWarnings("serial")
public class CheckBoxIntegerSliderStyleParameterEditorPanel extends CheckBoxFloatSliderStyleParameterEditorPanel
{
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
     * @param checkBoxParamKey the check box param key
     * @param cbSliderLinkType the cb slider link type
     */
    public CheckBoxIntegerSliderStyleParameterEditorPanel(PanelBuilder label, MutableVisualizationStyle style, String paramKey,
            boolean previewable, boolean textEntry, int min, int max, String unit, String checkBoxParamKey,
            SliderVisabilityLinkType cbSliderLinkType)
    {
        super(label, style, paramKey, previewable, textEntry, min, max, new IntegerOnlyConvertor(unit), checkBoxParamKey,
                cbSliderLinkType);
    }

    /**
     * Sets the parameter.
     *
     * @param aSliderValue the new parameter
     */
    @Override
    @SuppressWarnings("PMD.AvoidUsingShortType")
    protected void setParameter(int aSliderValue)
    {
        Object newValue = null;
        Class<?> t = getParameter().getValueType();
        if (t == Integer.class)
        {
            newValue = Integer.valueOf(aSliderValue);
        }
        else if (t == Long.class)
        {
            newValue = Long.valueOf(aSliderValue);
        }
        else if (t == Short.class)
        {
            newValue = Short.valueOf((short)aSliderValue);
        }
        else if (t == Byte.class)
        {
            newValue = Byte.valueOf((byte)aSliderValue);
        }
        else if (t == Float.class)
        {
            newValue = Float.valueOf(aSliderValue);
        }
        else if (t == Double.class)
        {
            newValue = Double.valueOf(aSliderValue);
        }

        setParamValue(newValue);
    }
}
