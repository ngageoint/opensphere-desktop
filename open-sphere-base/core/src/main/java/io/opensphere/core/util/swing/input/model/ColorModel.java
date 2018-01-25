package io.opensphere.core.util.swing.input.model;

import java.awt.Color;

import io.opensphere.core.util.ColorUtilities;

/**
 * Color model.
 */
public class ColorModel extends AbstractViewModel<Color>
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Sets the alpha component in the range 0-255.
     *
     * @param alpha the alpha component.
     */
    public void setAlpha(int alpha)
    {
        set(ColorUtilities.opacitizeColor(get(), alpha));
    }
}
