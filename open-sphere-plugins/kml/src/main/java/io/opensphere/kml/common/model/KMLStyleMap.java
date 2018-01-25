package io.opensphere.kml.common.model;

import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleState;

/** Represents a KML StyleMap suitable for use in the KML plugin. */
public class KMLStyleMap
{
    /** The normal style. */
    private Style myNormalStyle;

    /** The highlight style. */
    private Style myHighlightStyle;

    /**
     * Sets the style for the given style state.
     *
     * @param styleState the style state
     * @param style the style
     */
    public void put(StyleState styleState, Style style)
    {
        if (styleState == StyleState.NORMAL)
        {
            myNormalStyle = style;
        }
        else
        {
            myHighlightStyle = style;
        }
    }

    /**
     * Gets the style for the given style state.
     *
     * @param styleState the style state
     * @return the style
     */
    public Style get(StyleState styleState)
    {
        return styleState == StyleState.NORMAL ? myNormalStyle : myHighlightStyle;
    }

    @Override
    public String toString()
    {
        return "KMLStyleMap [myNormalStyle=" + myNormalStyle + ", myHighlightStyle=" + myHighlightStyle + "]";
    }
}
