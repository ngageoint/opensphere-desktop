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
            returnColor = new Color(get(0), get(1), get(2));
        }
        if (size() == 4)
        {
            returnColor = new Color(get(0), get(1), get(2), get(3));
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
        add(color.getRed());
        add(color.getGreen());
        add(color.getBlue());
        add(color.getAlpha());
    }
}
