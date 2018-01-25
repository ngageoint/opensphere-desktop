package io.opensphere.core.util.awt;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.SwingConstants;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.MathUtil;

/**
 * General AWT utilities.
 */
public final class AWTUtilities
{
    /**
     * Gets the maximum x value of the rectangle.
     *
     * @param rect the rectangle
     * @return the maximum x value
     */
    public static int getMaxX(Rectangle rect)
    {
        return rect.x + rect.width;
    }

    /**
     * Gets the maximum y value of the rectangle.
     *
     * @param rect the rectangle
     * @return the maximum y value
     */
    public static int getMaxY(Rectangle rect)
    {
        return rect.y + rect.height;
    }

    /**
     * Gets the text x location for the given parameters.
     *
     * @param text the text
     * @param anchorX the anchor position of the text
     * @param pad the padding between the text and the anchor (ignored for
     *            {@link SwingConstants#CENTER})
     * @param position the position ({@link SwingConstants#LEFT},
     *            {@link SwingConstants#RIGHT}, or {@link SwingConstants#CENTER}
     *            ) of the text relative to the anchor
     * @param g the graphics context
     * @return the text x location
     */
    public static int getTextXLocation(String text, int anchorX, int pad, int position, Graphics g)
    {
        int x;
        if (position == SwingConstants.LEFT)
        {
            int labelWidth = (int)Math.round(getTextWidth(text, g));
            x = MathUtil.subtractSafe(anchorX, pad + labelWidth);
        }
        else if (position == SwingConstants.RIGHT)
        {
            x = MathUtil.addSafe(anchorX, pad);
        }
        else if (position == SwingConstants.CENTER)
        {
            int labelWidth = (int)Math.round(getTextWidth(text, g));
            x = MathUtil.subtractSafe(anchorX, labelWidth >> 1);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported position: " + position);
        }
        return x;
    }

    /**
     * Gets the width of given text for the graphics context.
     *
     * @param text the text
     * @param g the graphics context
     * @return the text width
     */
    public static double getTextWidth(String text, Graphics g)
    {
        return g.getFontMetrics().getStringBounds(text, g).getWidth();
    }

    /**
     * Gets a String representation of the Font in a format supported by
     * {@link Font#decode(String)}.
     *
     * @param font the font
     * @return the font string
     */
    public static String encode(Font font)
    {
        String style;
        if (font.isBold())
        {
            style = font.isItalic() ? "bolditalic" : "bold";
        }
        else
        {
            style = font.isItalic() ? "italic" : "plain";
        }
        return String.join("-", font.getName(), style, String.valueOf(font.getSize()));
    }

    /**
     * Gets the font size from the encoded font string.
     *
     * @param encodedFont the encoded font string
     * @return the font size, or 12 if it couldn't be parsed
     */
    public static int getFontSize(String encodedFont)
    {
        int fontSize = 12;
        if (StringUtils.isNotEmpty(encodedFont))
        {
            String[] tokens = encodedFont.split("[ -]");
            if (tokens.length > 1)
            {
                String lastToken = tokens[tokens.length - 1];
                try
                {
                    fontSize = Integer.parseInt(lastToken);
                }
                catch (NumberFormatException e)
                {
                    fontSize = 12;
                }
            }
        }
        return fontSize;
    }

    /**
     * Private constructor.
     */
    private AWTUtilities()
    {
    }
}
