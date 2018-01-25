package io.opensphere.wfs.state.save;

import java.net.URL;
import java.util.Set;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.enums.EnumUtilities;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.IconFeatureVisualizationStyle;
import io.opensphere.wfs.state.model.IconStyle;

/**
 * Saves the state of icon WFS style parameters.
 */
public class IconStyleStateSaver extends StyleStateSaver
{
    /** The Icon style. */
    private final IconStyle myIconStyle;

    {
        getStyleKeys().add(IconFeatureVisualizationStyle.ourIconSizePropertyKey);
        getStyleKeys().add(IconFeatureVisualizationStyle.ourMixIconColorWithElementColrPropertyKey);
        getStyleKeys().add(IconFeatureVisualizationStyle.ourDefaultIconURLPropertyKey);
        getStyleKeys().add(IconFeatureVisualizationStyle.ourIconPointerLocationOffsetXPropertyKey);
        getStyleKeys().add(IconFeatureVisualizationStyle.ourIconPointerLocationOffsetYPropertyKey);
        getStyleKeys().add(IconFeatureVisualizationStyle.ourDefaultPointSizePropertyKey);
        getStyleKeys().add(IconFeatureVisualizationStyle.ourDefaultToPropertyKey);
    }

    /**
     * Constructor.
     */
    public IconStyleStateSaver()
    {
        myIconStyle = new IconStyle();
    }

    /**
     * Constructor.
     *
     * @param style A saved state style for use by this saver. In general, it is
     *            assumed that the values are pre-populated with known or saved
     *            values.
     */
    public IconStyleStateSaver(IconStyle style)
    {
        myIconStyle = style;
    }

    /**
     * Get the iconStyle.
     *
     * @return the iconStyle
     */
    public IconStyle getIconStyle()
    {
        return myIconStyle;
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

            if (styleKey.equals(IconFeatureVisualizationStyle.ourIconSizePropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myIconStyle.getIconScale()));
            }
            else if (styleKey.equals(IconFeatureVisualizationStyle.ourMixIconColorWithElementColrPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Boolean.valueOf(myIconStyle.isMixIconElementColor()));
            }
            else if (styleKey.equals(IconFeatureVisualizationStyle.ourDefaultIconURLPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(convertGoogleUrl(myIconStyle.getIconURL()));
            }
            else if (styleKey.equals(IconFeatureVisualizationStyle.ourIconPointerLocationOffsetXPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Integer.valueOf(myIconStyle.getIconXOffset()));
            }
            else if (styleKey.equals(IconFeatureVisualizationStyle.ourIconPointerLocationOffsetYPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Integer.valueOf(myIconStyle.getIconYOffset()));
            }
            else if (styleKey.equals(IconFeatureVisualizationStyle.ourDefaultPointSizePropertyKey))
            {
                vsp = vsp.deriveWithNewValue(Float.valueOf(myIconStyle.getDefaultPointSize()));
            }
            else if (styleKey.equals(IconFeatureVisualizationStyle.ourDefaultToPropertyKey))
            {
                vsp = vsp.deriveWithNewValue(
                        EnumUtilities.fromString(IconFeatureVisualizationStyle.DefaultTo.class, myIconStyle.getDefaultTo()));
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
                if (styleKey.equals(IconFeatureVisualizationStyle.ourIconSizePropertyKey))
                {
                    myIconStyle.setIconScale((int)Double.parseDouble(styleParam.toString()));
                }
                else if (styleKey.equals(IconFeatureVisualizationStyle.ourMixIconColorWithElementColrPropertyKey))
                {
                    myIconStyle.setMixIconElementColor(((Boolean)styleParam).booleanValue());
                }
                else if (styleKey.equals(IconFeatureVisualizationStyle.ourDefaultIconURLPropertyKey))
                {
                    myIconStyle.setIconURL(styleParam.toString());
                }
                else if (styleKey.equals(IconFeatureVisualizationStyle.ourIconPointerLocationOffsetXPropertyKey))
                {
                    myIconStyle.setIconXOffset(Integer.parseInt(styleParam.toString()));
                }
                else if (styleKey.equals(IconFeatureVisualizationStyle.ourIconPointerLocationOffsetYPropertyKey))
                {
                    myIconStyle.setIconYOffset(Integer.parseInt(styleParam.toString()));
                }
                else if (styleKey.equals(IconFeatureVisualizationStyle.ourDefaultPointSizePropertyKey))
                {
                    myIconStyle.setDefaultPointSize((int)Double.parseDouble(styleParam.toString()));
                }
                else if (styleKey.equals(IconFeatureVisualizationStyle.ourDefaultToPropertyKey))
                {
                    myIconStyle.setDefaultTo(styleParam.toString());
                }
            }
        }
    }

    /**
     * Converts an http google icon URL to a local URL.
     *
     * @param httpUrl the http google icon URL
     * @return the local URL
     */
    private String convertGoogleUrl(String httpUrl)
    {
        String fileUrl = httpUrl;
        if (httpUrl.startsWith("http://maps.google.com"))
        {
            URL localUrl = getClass().getResource(httpUrl.replace("http://", "/images/"));
            if (localUrl != null)
            {
                fileUrl = localUrl.toExternalForm();
            }
        }
        return fileUrl;
    }
}
