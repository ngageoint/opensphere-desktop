package io.opensphere.core.util;

import java.awt.Color;
import java.lang.reflect.Field;

import org.apache.log4j.Logger;

/** Helper class for color conversion from a string. */
@SuppressWarnings("PMD.GodClass")
public final class ColorUtilities
{
    /** The maximum value of a color component. */
    public static final int COLOR_COMPONENT_MAX_VALUE = 255;

    /**
     * A somewhat arbitrary factor used to determine the minimum amount that
     * colors close to pure black should be adjusted when brightened.
     */
    private static final float BLACK_BRIGHTEN_MIN = 3.0f;

    /** The maximum value for the darken/brighten adjustment factor. */
    private static final float BRIGHTNESS_FACTOR_MAX = 0.99f;

    /** The minimum value for the darken/brighten adjustment factor. */
    private static final float BRIGHTNESS_FACTOR_MIN = 0.01f;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ColorUtilities.class);

    /**
     * Blend two colors using the opacity of the first color. The resulting
     * color will be fully opaque.
     *
     * @param color1 The first color.
     * @param color2 The second color.
     * @return The blended color.
     */
    public static Color blendColors(Color color1, Color color2)
    {
        if (color1.getAlpha() < COLOR_COMPONENT_MAX_VALUE)
        {
            float alpha = (float)color1.getAlpha() / COLOR_COMPONENT_MAX_VALUE;
            return new Color((color1.getRed() * alpha + color2.getRed() * (1f - alpha)) / COLOR_COMPONENT_MAX_VALUE,
                    (color1.getGreen() * alpha + color2.getGreen() * (1f - alpha)) / COLOR_COMPONENT_MAX_VALUE,
                    (color1.getBlue() * alpha + color2.getBlue() * (1f - alpha)) / COLOR_COMPONENT_MAX_VALUE);
        }
        else
        {
            return color1;
        }
    }

    /**
     * Creates a new <code>Color</code> that is a brighter version of this
     * <code>Color</code>. This is based on the java.awt.Color#brighter method,
     * but preserves alpha and allows the user to control how much the color is
     * brightened. It also brightens colors close to black a bit faster.
     * <p>
     * This method applies a specified scale factor to each of the three RGB
     * components of this <code>Color</code> to create a brighter version of
     * this <code>Color</code>. Although <code>brighter</code> and
     * <code>darker</code> are inverse operations, the results of a series of
     * invocations of these two methods might be inconsistent because of
     * rounding errors.
     *
     * @param input the color to brighten
     * @param factor the multiplicative factor by which to brighten. Range is
     *            0.01-0.99 where numbers closer to 0 brighten more than numbers
     *            closer to 1. java.awt.Color default is 0.7.
     * @return a new <code>Color</code> object that is a brighter version of
     *         this <code>Color</code>.
     * @see java.awt.Color#brighter
     */
    public static Color brighten(Color input, double factor)
    {
        if (factor < BRIGHTNESS_FACTOR_MIN || factor > BRIGHTNESS_FACTOR_MAX)
        {
            throw new IllegalArgumentException("Color brighten factor must be between 0.01 and 0.99");
        }

        int r = input.getRed();
        int g = input.getGreen();
        int b = input.getBlue();

        /* From 2D group: 1. black.brighter() should return grey 2. applying
         * brighter to blue will always return blue, brighter 3. non pure color
         * (non zero RGB) will eventually return white */
        int i = (int)(BLACK_BRIGHTEN_MIN / (1.0 - factor));
        if (r == 0 && g == 0 && b == 0)
        {
            return new Color(i, i, i);
        }
        if (r > 0 && r < i)
        {
            r = i;
        }
        if (g > 0 && g < i)
        {
            g = i;
        }
        if (b > 0 && b < i)
        {
            b = i;
        }

        return new Color(Math.min((int)(r / factor), 255), Math.min((int)(g / factor), 255), Math.min((int)(b / factor), 255),
                input.getAlpha());
    }

    /**
     * Convert from string representation of the color.
     *
     * @param color The string representation of the color.
     * @return The Color or null if it cannot be converted.
     */
    public static Color convertFromColorString(String color)
    {
        Color aColor = null;
        if (color != null)
        {
            try
            {
                // see if the property corresponds to a Color.<name> static
                // field
                Field colorField = Color.class.getField(color.toUpperCase());
                try
                {
                    aColor = (Color)colorField.get(null);
                }
                catch (IllegalAccessException e)
                {
                    LOGGER.error("Could not access field by reflection.", e);
                }
            }
            catch (NoSuchFieldException e)
            {
                // takes open ended formats like: 45-32-18-9,100-100-100-100
                // in r-g-b-a format
                String[] colors = color.split("-");
                if (colors.length == 4)
                {
                    try
                    {
                        aColor = new java.awt.Color(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]),
                                Integer.parseInt(colors[2]), Integer.parseInt(colors[3]));
                    }
                    catch (NumberFormatException nfe)
                    {
                        LOGGER.warn("Failed to parse color [" + color + "]: " + nfe, nfe);
                    }
                    catch (IllegalArgumentException iae)
                    {
                        LOGGER.warn("Failed to parse color [" + color + "]: " + iae, iae);
                    }
                }
            }
        }
        return aColor;
    }

    /**
     * Convert a string to a {@link Color}, getting the color components from
     * the string at the locations specified. The color indices are zero-based,
     * increasing left-to-right, two characters per color. For example, if the
     * input string is "ffbb00cc", "ff" is index 0, "bb" is index 1, etc.
     *
     * @param color The input color string (e.g., "ffbb00cc").
     * @param redIndex The position of the red channel in the input string.
     * @param greenIndex The position of the green channel in the input string.
     * @param blueIndex The position of the blue channel in the input string.
     * @param alphaIndex The position of the alpha channel in the input string.
     * @return The {@link Color}.
     * @throws NumberFormatException If the color string does not contain a
     *             parse-able int.
     */
    public static Color convertFromHexString(String color, int redIndex, int greenIndex, int blueIndex, int alphaIndex)
        throws NumberFormatException
    {
        if (color == null || color.length() == 0 || color.length() > 8)
        {
            throw new NumberFormatException("For input string: \"" + color + "\"");
        }

        long inputVal = Long.parseLong(color, 16);
        int outputVal = 0;

        outputVal |= 0xff0000 & MathUtil.byteShift(inputVal, redIndex - 1);
        outputVal |= 0xff00 & MathUtil.byteShift(inputVal, greenIndex - 2);
        outputVal |= 0xff & MathUtil.byteShift(inputVal, blueIndex - 3);
        outputVal |= 0xff000000 & MathUtil.byteShift(inputVal, alphaIndex);

        return new Color(outputVal, true);
    }

    /**
     * Converts a color to a hex string in the format similar to "bb00cc", using
     * the given indices to position the colors in the string.
     *
     * @param color The color to convert.
     * @param redIndex The position of the red channel in the output string.
     * @param greenIndex The position of the green channel in the output string.
     * @param blueIndex The position of the blue channel in the output string.
     * @return The color hex string.
     */
    public static String convertToHexString(Color color, int redIndex, int greenIndex, int blueIndex)
    {
        int inputVal = color.getRGB();

        int outputVal = 0;

        outputVal |= MathUtil.byteShift(inputVal & 0x00ff0000, 0 - redIndex);
        outputVal |= MathUtil.byteShift(inputVal & 0x0000ff00, 1 - greenIndex);
        outputVal |= MathUtil.byteShift(inputVal & 0x000000ff, 2 - blueIndex);

        return Integer.toHexString(outputVal);
    }

    /**
     * Converts a color to a hex string in the format similar to "ffbb00cc",
     * using the given indices to position the colors in the string.
     *
     * @param color The color to convert.
     * @param redIndex The position of the red channel in the output string.
     * @param greenIndex The position of the green channel in the output string.
     * @param blueIndex The position of the blue channel in the output string.
     * @param alphaIndex The position of the alpha channel in the output string.
     * @return The color hex string.
     */
    public static String convertToHexString(Color color, int redIndex, int greenIndex, int blueIndex, int alphaIndex)
    {
        int inputVal = color.getRGB();

        int outputVal = 0;

        outputVal |= MathUtil.byteShift(inputVal & 0x00ff0000, 1 - redIndex);
        outputVal |= MathUtil.byteShift(inputVal & 0x0000ff00, 2 - greenIndex);
        outputVal |= MathUtil.byteShift(inputVal & 0x000000ff, 3 - blueIndex);
        outputVal |= MathUtil.byteShift(inputVal & 0xff000000, 0 - alphaIndex);

        return Integer.toHexString(outputVal);
    }

    /**
     * Convert a color to an rgba color string. In the format
     * "Red-Green-Blue-Alpha" where the RGB are the integer values for the color
     * components.
     *
     * @param aColor the a color
     * @return the string
     */
    public static String convertToRGBAColorString(Color aColor)
    {
        if (aColor == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(aColor.getRed()).append('-').append(aColor.getGreen()).append('-').append(aColor.getBlue()).append('-')
                .append(aColor.getAlpha());
        return sb.toString();
    }

    /**
     * Creates a new <code>Color</code> that is a darker version of this
     * <code>Color</code>. This is based on the java.awt.Color#darker method,
     * but preserves alpha and allows the user to control how much the color is
     * darkened.
     * <p>
     * This method applies an arbitrary scale factor to each of the three RGB
     * components of this <code>Color</code> to create a darker version of this
     * <code>Color</code>. Although <code>brighter</code> and
     * <code>darker</code> are inverse operations, the results of a series of
     * invocations of these two methods might be inconsistent because of
     * rounding errors.
     *
     * @param input the color to darken
     * @param factor the multiplicative factor by which to darken. Range is
     *            0.01-0.99 where numbers closer to 0 darken more than numbers
     *            closer to 1. java.awt.Color default is 0.7.
     * @return a new <code>Color</code> object that is a darker version of this
     *         <code>Color</code>.
     * @see java.awt.Color#darker
     */
    public static Color darken(Color input, double factor)
    {
        if (factor < BRIGHTNESS_FACTOR_MIN || factor > BRIGHTNESS_FACTOR_MAX)
        {
            throw new IllegalArgumentException("Color brighten factor must be between 0.01 and 0.99");
        }

        return new Color(Math.max((int)(input.getRed() * factor), 0), Math.max((int)(input.getGreen() * factor), 0),
                Math.max((int)(input.getBlue() * factor), 0), input.getAlpha());
    }

    /**
     * Get the relative brightness of a color. The typical linear calculations
     * for this (e.g. (red + green + blue)/3) tend to give terrible results for
     * colors in the yellow and green ranges, so this method uses a non-linear
     * weighted average which is slower and takes more CPU, but yields much
     * better results across the spectrum.
     * <p>
     * This is used, for example, to determine whether the text over a given
     * background color should be black or white based on that background's
     * brightness (fg = getBrightness(bg) &lt; 130 ? Color.WHITE : Color.BLACK)
     *
     * @param color the color whose brightness should be determined
     * @return the relative brightness of the specified color between 0 and 255
     *         where 0 is darkest (Color.BLACK) and 255 is brightest
     *         (Color.WHITE)
     */
    public static int getBrightness(Color color)
    {
        Utilities.checkNull(color, "color");

        /* These factors are somewhat arbitrary but yield good results. If they
         * need to change, Photoshop apparently uses red=0.299, green=0.587, and
         * blue=0.114 */
        final float redFactor = 0.241f;
        final float greenFactor = 0.691f;
        final float blueFactor = 0.068f;

        return (int)Math.round(Math.sqrt(redFactor * color.getRed() * color.getRed()
                + greenFactor * color.getGreen() * color.getGreen() + blueFactor * color.getBlue() * color.getBlue()));
    }

    /**
     * Get a color which contrasts relatively well with the given color.
     *
     * @param color The color for which a contrasting color is desired.
     * @return the contrasting color.
     */
    public static Color getContrastingColor(Color color)
    {
        return opacitizeColor(getBrightness(color) < 130 ? Colors.RELATIVE_LIGHT_COLOR : Color.BLACK, color.getAlpha());
    }

    /**
     * Gets a brighter or darker version of the given color.
     *
     * @param color the normal color
     * @param shift the amount to brighten by (0-255)
     * @return the alternate color
     */
    public static Color getAlternateColor(Color color, int shift)
    {
        Color selectedColor = color;
        if (color != null)
        {
            if (ColorUtilities.getBrightness(color) >= 130)
            {
                selectedColor = color.darker();
            }
            else
            {
                int red = Math.min(color.getRed() + shift, 255);
                int green = Math.min(color.getGreen() + shift, 255);
                int blue = Math.min(color.getBlue() + shift, 255);
                selectedColor = new Color(red, green, blue);
            }
        }
        return selectedColor;
    }

    /**
     * Checks if the two colors are equal including the alpha channel.
     *
     * @param c1 the first {@link Color}
     * @param c2 the second {@link Color}
     * @return true, if both are null or both have the same RGBA values.
     */
    public static boolean isEqual(Color c1, Color c2)
    {
        return Utilities.sameInstance(c1, c2) || c1 != null && c1.equals(c2);
    }

    /**
     * Checks if the two colors are equal ignoring the alpha channel. Compares
     * only RGB values.
     *
     * @param c1 the first {@link Color}
     * @param c2 the second {@link Color}
     * @return true, if both are null or both have the same RGB values.
     */
    public static boolean isEqualIgnoreAlpha(Color c1, Color c2)
    {
        boolean equal = false;
        if (c1 == null && c2 == null)
        {
            equal = true;
        }
        else if (c1 != null && c2 != null)
        {
            equal = c1.getRed() == c2.getRed() && c1.getBlue() == c2.getBlue() && c1.getGreen() == c2.getGreen();
        }
        return equal;
    }

    /**
     * Opacitize color.
     *
     * @param startingColor The starting color.
     * @param alpha The alpha (0.0 to 1.0).
     * @return The color.
     */
    public static Color opacitizeColor(Color startingColor, float alpha)
    {
        if (alpha < 0f || alpha > 1f)
        {
            throw new IllegalArgumentException("Alpha [" + alpha + "] cannot be less than 0 or greater than 1.");
        }
        if (startingColor == null || startingColor.getAlpha() == alpha)
        {
            return startingColor;
        }
        else
        {
            return new Color(startingColor.getRGB() & 0xffffff | (int)(alpha * 0xff + 0.5) << 24, true);
        }
    }

    /**
     * Opacitize color.
     *
     * @param startingColor The starting color.
     * @param alpha The alpha (0 to 255).
     * @return The color.
     */
    public static Color opacitizeColor(Color startingColor, int alpha)
    {
        if (alpha < 0 || alpha > 255)
        {
            throw new IllegalArgumentException("Alpha [" + alpha + "] cannot be less than 0 or greater than 255.");
        }
        if (startingColor == null || startingColor.getAlpha() == alpha)
        {
            return startingColor;
        }
        else
        {
            return new Color(startingColor.getRGB() & 0xffffff | alpha << 24, true);
        }
    }

    /**
     * Disallow instantiation.
     */
    private ColorUtilities()
    {
    }
}
