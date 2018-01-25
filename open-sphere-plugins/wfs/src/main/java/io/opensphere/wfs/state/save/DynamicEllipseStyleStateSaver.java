package io.opensphere.wfs.state.save;

import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.enums.EnumUtilities;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.AbstractEllipseFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractEllipseFeatureVisualizationStyle.EllipseFillStyle;
import io.opensphere.mantle.data.geom.style.impl.DynamicEllipseFeatureVisualization;
import io.opensphere.wfs.state.model.EllipseStyle;

/**
 * Saves the state of ellipse WFS style parameters.
 */
public class DynamicEllipseStyleStateSaver extends StyleStateSaver
{
    /** The Ellipse style. */
    private final EllipseStyle myEllipseStyle;

    {
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourEllipseLineWidthPropertyKey);
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourEllipseShowEdgeLinePropertyKey);
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourShowCenterPointPropertyKey);
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourCenterPointSizePropertyKey);
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourEllipseFillStylePropertyKey);
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourEllipseShowOnSelectPropertyKey);
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourRimFadePropertyKey);
        getStyleKeys().add(AbstractEllipseFeatureVisualizationStyle.ourAxisUnitKey);
        getStyleKeys().add(DynamicEllipseFeatureVisualization.ourSemiMajorAxisColumnKey);
        getStyleKeys().add(DynamicEllipseFeatureVisualization.ourSemiMinorAxisColumnKey);
        getStyleKeys().add(DynamicEllipseFeatureVisualization.ourOrientationColumnKey);
    }

    /**
     * Constructor.
     */
    public DynamicEllipseStyleStateSaver()
    {
        myEllipseStyle = new EllipseStyle();
    }

    /**
     * Constructor.
     *
     * @param style A saved state style for use by this saver. In general, it is
     *            assumed that the values are pre-populated with known or saved
     *            values.
     */
    public DynamicEllipseStyleStateSaver(EllipseStyle style)
    {
        myEllipseStyle = style;
    }

    /**
     * Get the ellipseStyle.
     *
     * @return the ellipseStyle
     */
    public EllipseStyle getEllipseStyle()
    {
        return myEllipseStyle;
    }

    @Override
    public Set<VisualizationStyleParameter> populateVisualizationStyle(VisualizationStyle visStyle)
    {
        Set<VisualizationStyleParameter> vspSet = New.set();
        for (String styleKey : getStyleKeys())
        {
            VisualizationStyleParameter vsp = visStyle.getStyleParameter(styleKey);
            if (vsp == null)
            {
                // This should only happen if the style key is invalid for the
                // style type.
                continue;
            }

            if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseLineWidthPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myEllipseStyle.getEdgeLineWidth()));
            }
            else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseShowEdgeLinePropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Boolean.valueOf(myEllipseStyle.isEdgeLine()));
            }
            else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourShowCenterPointPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Boolean.valueOf(myEllipseStyle.isCenterPoint()));
            }
            else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourCenterPointSizePropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myEllipseStyle.getCenterPointSize()));
            }
            else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseFillStylePropertyKey))
            {
                vsp = vsp.deriveWithNewValue(EnumUtilities.fromString(EllipseFillStyle.class, myEllipseStyle.getFillStyle()));
            }
            else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseShowOnSelectPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Boolean.valueOf(myEllipseStyle.isEllipseOnSelect()));
            }
            else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourAxisUnitKey))
            {
                vsp = vsp.deriveWithNewValue(myEllipseStyle.getAxisUnits());
            }
            else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourRimFadePropertyKey))
            {
                vsp = vsp.deriveWithNewValue(myEllipseStyle.getRimFade());
            }
            else if (styleKey.equals(DynamicEllipseFeatureVisualization.ourSemiMajorAxisColumnKey))
            {
                vsp = vsp.deriveWithNewValue(myEllipseStyle.getSemiMajorColumn());
            }
            else if (styleKey.equals(DynamicEllipseFeatureVisualization.ourSemiMinorAxisColumnKey))
            {
                vsp = vsp.deriveWithNewValue(myEllipseStyle.getSemiMinorColumn());
            }
            else if (styleKey.equals(DynamicEllipseFeatureVisualization.ourOrientationColumnKey))
            {
                vsp = vsp.deriveWithNewValue(myEllipseStyle.getOrientationColumn());
            }
            vspSet.add(vsp);
        }

        return vspSet;
    }

    @Override
    public void saveStyleParams(VisualizationStyle visStyle)
    {
        for (String styleKey : getStyleKeys())
        {
            VisualizationStyleParameter param = visStyle.getStyleParameter(styleKey);
            Object styleParam = param.getValue();
            if (styleParam != null)
            {
                if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseLineWidthPropertyKey))
                {
                    // This may be a double value, but we will only use int for
                    // compatibility
                    myEllipseStyle.setEdgeLineWidth((int)Double.parseDouble(styleParam.toString()));
                }
                else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseShowEdgeLinePropertyKey))
                {
                    myEllipseStyle.setEdgeLine(((Boolean)styleParam).booleanValue());
                }
                else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourShowCenterPointPropertyKey))
                {
                    myEllipseStyle.setCenterPoint(((Boolean)styleParam).booleanValue());
                }
                else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourCenterPointSizePropertyKey))
                {
                    // This may be a double value, but we will only use int for
                    // compatibility
                    myEllipseStyle.setCenterPointSize((int)Double.parseDouble(styleParam.toString()));
                }
                else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseFillStylePropertyKey))
                {
                    myEllipseStyle.setFillStyle(styleParam.toString());
                }
                else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourEllipseShowOnSelectPropertyKey))
                {
                    myEllipseStyle.setEllipseOnSelect(((Boolean)styleParam).booleanValue());
                }
                else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourRimFadePropertyKey))
                {
                    myEllipseStyle.setRimFade(Integer.parseInt(styleParam.toString()));
                }
                else if (styleKey.equals(AbstractEllipseFeatureVisualizationStyle.ourAxisUnitKey))
                {
                    myEllipseStyle.setAxisUnits(styleParam.toString());
                }
                else if (styleKey.equals(DynamicEllipseFeatureVisualization.ourSemiMajorAxisColumnKey))
                {
                    myEllipseStyle.setSemiMajorColumn(styleParam.toString());
                }
                else if (styleKey.equals(DynamicEllipseFeatureVisualization.ourSemiMinorAxisColumnKey))
                {
                    myEllipseStyle.setSemiMinorColumn(styleParam.toString());
                }
                else if (styleKey.equals(DynamicEllipseFeatureVisualization.ourOrientationColumnKey))
                {
                    myEllipseStyle.setOrientationColumn(styleParam.toString());
                }
            }
        }
    }
}
