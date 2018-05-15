package io.opensphere.arcgis2.esri;

import java.awt.Color;
import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;

/**
 * The Class EsriColor.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriColor extends ArrayList<Integer>
{
    /** Default Serial Version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        Color returnColor = null;
        if (size() == 3)
        {
            returnColor = new Color(get(0).intValue(), get(1).intValue(), get(2).intValue());
        }
        if (size() == 4)
        {
            returnColor = new Color(get(0).intValue(), get(1).intValue(), get(2).intValue(), get(3).intValue());
        }
        return returnColor;
    }

    /**
     * Sets the color.
     *
     * @param color the new color
     */
    public void setColor(Color color)
    {
        clear();
        add(Integer.valueOf(color.getRed()));
        add(Integer.valueOf(color.getGreen()));
        add(Integer.valueOf(color.getBlue()));
        add(Integer.valueOf(color.getAlpha()));
    }
}
