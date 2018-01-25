package io.opensphere.controlpanels.styles.model;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import io.opensphere.core.util.ColorUtilities;

/**
 * Converts {@link Color} to an xml representation.
 */
public class ColorAdapter extends XmlAdapter<String, Color>
{
    @Override
    public String marshal(Color v)
    {
        return ColorUtilities.convertToHexString(v, 1, 2, 3, 0);
    }

    @Override
    public Color unmarshal(String hex)
    {
        return ColorUtilities.convertFromHexString(hex, 1, 2, 3, 0);
    }
}
