package io.opensphere.core.util.image;

import java.awt.image.RGBImageFilter;

import io.opensphere.core.util.MathUtil;

/**
 * An image filter that colors images with a constant color.
 */
public class ColorizeFilter extends RGBImageFilter
{
    /** The RGB color. */
    private final int myRGBColor;

    /**
     * Constructor.
     *
     * @param rgb The rgb color.
     */
    public ColorizeFilter(int rgb)
    {
        myRGBColor = rgb;
    }

    @Override
    public int filterRGB(int x, int y, int rgb)
    {
        final double redFactor = 0.30;
        final double greenFactor = 0.59;
        final double blueFactor = 0.11;
        final double brightness = 2.5;
        double gray = MathUtil
                .clamp((redFactor * (rgb >> 16 & 0xff) + greenFactor * (rgb >> 8 & 0xff) + blueFactor * (rgb & 0xff)) / 3 / 255
                        * brightness, 0, 1);
        return rgb & 0xff000000 | 0xffffff & (0xff0000 & (int)((myRGBColor & 0xff0000) * gray))
                + (0x00ff00 & (int)((myRGBColor & 0x00ff00) * gray)) + (0xff & (int)((myRGBColor & 0xff) * gray));
    }
}
