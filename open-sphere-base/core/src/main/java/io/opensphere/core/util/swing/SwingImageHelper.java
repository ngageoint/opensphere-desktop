package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import io.opensphere.core.geometry.SingletonImageProvider;

/**
 * Convert an AWT Component to an Image. If you are rendering the Image as a
 * screen tile, you might use {@link SingletonImageProvider} to supply the
 * image.
 */
public final class SwingImageHelper
{
    /** The image type. */
    private static final int IMG_TYPE = BufferedImage.TYPE_4BYTE_ABGR;

    /** Use an empty border to pad between the edge and the text. */
    private static final Border ourEmptyBorder = BorderFactory.createEmptyBorder(0, 2, 0, 0);

    /**
     * A utility method for creating multi-line text. Give each line of text as
     * a separate string.
     * {@link SwingImageHelper#convertToImage(boolean, JComponent, Color, int)}
     * is used to produce the image.
     *
     * @param alignCompress Create a buffer which will allow compression.
     * @param lines The lines of text.
     * @param bgColor Background color or <code>null</code> of no fill is
     *            desired.
     * @param fgColor Foreground color.
     * @param border A border to go around the text.
     * @param font The font.
     * @return Image produced by rendering.
     */
    public static BufferedImage textToImage(boolean alignCompress, List<? extends String> lines, final Color bgColor,
            Color fgColor, Border border, Font font)
    {
        return textToImage(alignCompress, lines, bgColor, fgColor, border, font, 0);
    }

    /**
     * A utility method for creating multi-line text. Give each line of text as
     * a separate string.
     * {@link SwingImageHelper#convertToImage(boolean, JComponent, Color, int)}
     * is used to produce the image.
     *
     * @param alignCompress Create a buffer which will allow compression.
     * @param lines The lines of text.
     * @param bgColor Background color or <code>null</code> of no fill is
     *            desired.
     * @param fgColor Foreground color.
     * @param border A border to go around the text.
     * @param font The font.
     * @param cornerRadius The corner radius
     * @return Image produced by rendering.
     */
    public static BufferedImage textToImage(boolean alignCompress, List<? extends String> lines, final Color bgColor,
            Color fgColor, Border border, Font font, int cornerRadius)
    {
        StringBuilder bldr = new StringBuilder(128);
        bldr.append("<html><body>");
        boolean first = true;
        for (String line : lines)
        {
            if (!first)
            {
                bldr.append("<br>");
            }
            first = false;
            bldr.append(line);
        }
        bldr.append("</body></html>");
        JLabel text = new JLabel(bldr.toString());
        text.setFont(font);
        text.setForeground(fgColor);
        // null for the outer border is allowed.
        CompoundBorder compBorder = BorderFactory.createCompoundBorder(border, ourEmptyBorder);
        text.setBorder(compBorder);
        return convertToImage(alignCompress, text, bgColor, cornerRadius);
    }

    /**
     * Render the given component into a BufferedImage. Note that this method
     * only renders the top level component, child components such as components
     * added to a JPanel will not be rendered.
     *
     * @param alignCompress Create a buffer which will allow compression.
     * @param comp Component to render.
     * @param bgColor Background color or <code>null</code> of no fill is
     *            desired.
     * @param cornerRadius The corner radius
     * @return Image produced by rendering.
     */
    private static BufferedImage convertToImage(boolean alignCompress, JComponent comp, Color bgColor, int cornerRadius)
    {
        BufferedImage buf = null;
        Dimension dim = comp.getPreferredSize();
        int width = (int)dim.getWidth();
        int height = (int)dim.getHeight();
        if (alignCompress)
        {
            width = (int)(Math.ceil(dim.getWidth() / 4) * 4);
            height = (int)(Math.ceil(dim.getHeight() / 4) * 4);
        }
        buf = new BufferedImage(width, height, IMG_TYPE);

        Graphics graph = buf.createGraphics();
        try
        {
            if (bgColor != null)
            {
                Color oldColor = graph.getColor();
                graph.setColor(bgColor);
                if (cornerRadius > 0)
                {
                    int arc = 2 * cornerRadius;
                    graph.fillRoundRect(0, 0, width, height, arc, arc);
                }
                else
                {
                    graph.fillRect(0, 0, width, height);
                }
                graph.setColor(oldColor);
            }

            comp.setBounds(0, 0, width, height);

            boolean wasDoubleBuffered = false;
            if (comp.isDoubleBuffered())
            {
                wasDoubleBuffered = true;
                comp.setDoubleBuffered(false);
            }

            comp.paint(graph);

            if (wasDoubleBuffered)
            {
                comp.setDoubleBuffered(true);
            }

            comp.setBounds(-width, -height, 0, 0);
        }
        finally
        {
            graph.dispose();
        }

        return buf;
    }

    /** Disallow instantiation. */
    private SwingImageHelper()
    {
    }
}
