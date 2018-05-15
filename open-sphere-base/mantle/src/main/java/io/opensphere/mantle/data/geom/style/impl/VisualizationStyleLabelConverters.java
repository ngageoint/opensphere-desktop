package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor;

/** Accessor class for label converters. */
public final class VisualizationStyleLabelConverters
{
    /** A convertor with basic percentage labeling. */
    public static final BasicIntFloatConvertor BASIC_PERCENT = new BasicIntFloatConvertor(2, null)
    {
        @Override
        public String labelValue(double val)
        {
            double aVal = val * 100.;
            return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
        }
    };

    /** {@link #BASIC_PERCENT} with alternate text formatting. */
    public static final BasicIntFloatConvertor BASIC_PERCENT_DECIMAL = new BasicIntFloatConvertor(2, null, "%.0f")
    {
        @Override
        public String labelValue(double val)
        {
            double aVal = val * 100.;
            return String.format(getStringFormat(), Double.valueOf(aVal)) + "%";
        }
    };

    /** A convertor with basic exponent labeling. */
    public static final BasicIntFloatConvertor BASIC_POWER = new BasicIntFloatConvertor(2, null)
    {
        @Override
        public String labelValue(double val)
        {
            double aVal = Math.exp(val);
            return String.format(getStringFormat(), Double.valueOf(aVal));
        }
    };

    /** Private constructor. */
    private VisualizationStyleLabelConverters()
    {
    }
}
