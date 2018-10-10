package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** The FontIcon interface. */
public interface FontIcon
{
    /**
     * Gets the icon's color.
     *
     * @return the color
     */
    Color getColor();

    /**
     * Sets the icon's color.
     *
     * @param pColor the color to set
     */
    void setColor(Color pColor);

    /**
     * Gets the icon's image.
     *
     * @return the image.
     */
    BufferedImage getImage();

    /**
     * Gets the icon's size.
     *
     * @return the size
     */
    int getSize();

    /**
     * Sets the icon's size.
     *
     * @param pSize the size to set
     */
    void setSize(int pSize);
}
