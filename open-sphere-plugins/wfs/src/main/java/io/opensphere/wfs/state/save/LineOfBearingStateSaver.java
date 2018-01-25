package io.opensphere.wfs.state.save;

import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.AbstractLOBFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.DynamicLOBFeatureVisualization;
import io.opensphere.wfs.state.model.LineOfBearingStyle;

/**
 * Saves the state of line of bearing WFS style parameters.
 */
public class LineOfBearingStateSaver extends StyleStateSaver
{
    /** The Line of bearing style. */
    private final LineOfBearingStyle myLineOfBearingStyle;

    {
        getStyleKeys().add(AbstractLOBFeatureVisualizationStyle.ourLOBLengthPropertyKey);
        getStyleKeys().add(AbstractLOBFeatureVisualizationStyle.ourLOBLineWidthPropertyKey);
        getStyleKeys().add(AbstractLOBFeatureVisualizationStyle.ourLOBOriginPointSizePropertyKey);
        getStyleKeys().add(AbstractLOBFeatureVisualizationStyle.ourShowArrowPropertyKey);
        getStyleKeys().add(AbstractLOBFeatureVisualizationStyle.ourArrowLengthPropertyKey);
        getStyleKeys().add(DynamicLOBFeatureVisualization.ourLOBOrientationColumnKey);
    }

    /**
     * Constructor.
     */
    public LineOfBearingStateSaver()
    {
        myLineOfBearingStyle = new LineOfBearingStyle();
    }

    /**
     * Constructor.
     *
     * @param style A saved state style for use by this saver. In general, it is
     *            assumed that the values are pre-populated with known or saved
     *            values.
     */
    public LineOfBearingStateSaver(LineOfBearingStyle style)
    {
        myLineOfBearingStyle = style;
    }

    /**
     * Get the lineOfBearingStyle.
     *
     * @return the lineOfBearingStyle
     */
    public LineOfBearingStyle getLineOfBearingStyle()
    {
        return myLineOfBearingStyle;
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

            if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourLOBLengthPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myLineOfBearingStyle.getLobLength()));
            }
            else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourLOBLineWidthPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myLineOfBearingStyle.getLineWidth()));
            }
            else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourLOBOriginPointSizePropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myLineOfBearingStyle.getOriginPointSize()));
            }
            else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourShowArrowPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Boolean.valueOf(myLineOfBearingStyle.isShowArrow()));
            }
            else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourArrowLengthPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myLineOfBearingStyle.getArrowLength()));
            }
            else if (styleKey.equals(DynamicLOBFeatureVisualization.ourLOBOrientationColumnKey))
            {
                vsp = vsp.deriveWithNewValue(myLineOfBearingStyle.getLOBOrientationColumn());
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
                if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourLOBLengthPropertyKey))
                {
                    int lobLength = (int)Double.parseDouble(styleParam.toString());
                    myLineOfBearingStyle.setLobLength(lobLength);
                }
                else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourLOBLineWidthPropertyKey))
                {
                    int lobLineWidth = (int)Double.parseDouble(styleParam.toString());
                    myLineOfBearingStyle.setLineWidth(lobLineWidth);
                }
                else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourLOBOriginPointSizePropertyKey))
                {
                    int pointSize = (int)Double.parseDouble(styleParam.toString());
                    myLineOfBearingStyle.setOriginPointSize(pointSize);
                }
                else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourShowArrowPropertyKey))
                {
                    boolean showArrow = ((Boolean)styleParam).booleanValue();
                    myLineOfBearingStyle.setShowArrow(showArrow);
                }
                else if (styleKey.equals(AbstractLOBFeatureVisualizationStyle.ourArrowLengthPropertyKey))
                {
                    int arrowLength = (int)Double.parseDouble(styleParam.toString());
                    myLineOfBearingStyle.setArrowLength(arrowLength);
                }
                else if (styleKey.equals(DynamicLOBFeatureVisualization.ourLOBOrientationColumnKey))
                {
                    myLineOfBearingStyle.setLOBOrientationColumn(styleParam.toString());
                }
            }
        }
    }
}
