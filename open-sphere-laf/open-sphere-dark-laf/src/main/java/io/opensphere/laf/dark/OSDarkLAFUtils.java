package io.opensphere.laf.dark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

public final class OSDarkLAFUtils
{
    protected static Color rolloverColor;

    static final int NARROW = 0;

    static final int WIDE = 1;

    /**
     * Defines the thickness of the shadow of the title.
     */
    static final int TITLE_SHADOW_THICKNESS = 5;

    static Kernel titleShaodowKernel;

    /**
     * Defines the thickness of the shadow of the menus.
     */
    static final int MENU_SHADOW_THICKNESS = 3;

    static Kernel menuShadowKernel;

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private OSDarkLAFUtils()
    {
        throw new UnsupportedOperationException("Instantation of utility classes is not permitted.");
    }

    private static int adjustValueByProportion(int a, int b, int prop)
    {
        return b + (a - b) / prop;
    }

    public static void focusPaint(Graphics graph, int x, int y, int width, int height, int arcWidth, int arcHeight, Color c)
    {
        focusPaint(graph, x, y, width, height, arcWidth, arcHeight, 2.0f, c);
    }

    public static void focusPaint(Graphics g, int x, int y, int width, int height, int arcWidth, int arcHeight, float thickness,
            Color c)
    {
        final Graphics2D graph2D = (Graphics2D)g;
        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final Stroke origStroke = graph2D.getStroke();

        graph2D.setColor(c);
        graph2D.setStroke(new BasicStroke(thickness));
        if (arcWidth != 0 || arcHeight != 0)
        {
            g.drawRoundRect(x, y, width - 1, height - 1, arcWidth, arcHeight);
        }
        else
        {
            g.drawRect(x, y, width, height);
        }

        graph2D.setStroke(origStroke);
        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    public static Color getActiveColor()
    {
        return opacitizeColor(getThirdColor(OpenSphereDarkLookAndFeel.getControlHighlight(), Color.white), 64);
    }

    public static Color getActiveMenuColor()
    {
        return new Color(255, 255, 255, 64);
    }

    public static int getFrameOpacity()
    {
        try
        {
            final OSDarkLAFTheme theme = (OSDarkLAFTheme)OpenSphereDarkLookAndFeel.theme;
            return theme.getFrameOpacity();
        }
        catch (final Throwable ex)
        {
            return OSDarkLAFTheme.DEFAULT_FRAME_OPACITY;
        }
    }

    public static float getFrameOpacityFloat()
    {
        return getFrameOpacity() / 255f;
    }

    public static int getMenuOpacity()
    {
        try
        {
            final OSDarkLAFTheme theme = (OSDarkLAFTheme)OpenSphereDarkLookAndFeel.theme;
            return theme.getMenuOpacity();
        }
        catch (final Throwable e)
        {
            return OSDarkLAFTheme.DEFAULT_MENU_OPACITY;
        }
    }

    public static float getMenuOpacityFloat()
    {
        return getMenuOpacity() / 255f;
    }

    public static Color getMiddleColor(Color a, Color b)
    {
        return new Color(adjustValueByProportion(a.getRed(), b.getRed(), 2),
                adjustValueByProportion(a.getGreen(), b.getGreen(), 2), adjustValueByProportion(a.getBlue(), b.getBlue(), 2));
    }

    public static int getOpacity()
    {
        return getMenuOpacity();
    }

    public static Color getRolloverColor()
    {
        if (rolloverColor == null)
        {
            rolloverColor = opacitizeColor(UIManager.getColor("Button.focus"), 40);
        }
        return rolloverColor;
    }

    public static Color getShadowColor()
    {
        return opacitizeColor(getThirdColor(OpenSphereDarkLookAndFeel.getControlDarkShadow(), Color.black), 64);
    }

    public static Color getShadowMenuColor()
    {
        return new Color(20, 20, 20, 50);
    }

    public static ColorUIResource getThirdColor(Color a, Color b)
    {
        return new ColorUIResource(adjustValueByProportion(a.getRed(), b.getRed(), 3),
                adjustValueByProportion(a.getGreen(), b.getGreen(), 3), adjustValueByProportion(a.getBlue(), b.getBlue(), 3));
    }

    /**
     * Loads an image resource from a specified resource path, most likely a
     * jar.
     *
     * @param name the resource name of the image to load.
     * @return the ImageIcon or null if a problem was encountered while loading.
     */
    public static ImageIcon loadImageResource(String name)
    {
        try
        {
            return new ImageIcon(Toolkit.getDefaultToolkit()
                    .createImage(readStream(OpenSphereDarkLookAndFeel.class.getResourceAsStream(name))));
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            System.out.println("Can't find OSDarkLAF Image resource: " + name);
            return null;
        }
    }

    public static Color opacitizeColor(Color c, int alpha)
    {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /**
     * Paints a menu bar.
     *
     * @param graph the {@link Graphics} object with which to render the menu
     *            bar.
     * @param muItem the menu bar to render.
     * @param backgroundColor the color with which to render the menu bar.
     */
    public static void paintMenuBar(Graphics graph, JMenuItem muItem, Color backgroundColor)
    {
        final ButtonModel buttonModel = muItem.getModel();
        final Color origColor = graph.getColor();

        final int muWidth = muItem.getWidth();
        final int muHeight = muItem.getHeight();

        if (muItem.isOpaque())
        {
            graph.setColor(muItem.getBackground());
            graph.fillRect(0, 0, muWidth, muHeight);
        }

        if (buttonModel.isArmed() || muItem instanceof JMenu && !((JMenu)muItem).isTopLevelMenu() && buttonModel.isSelected())
        {
            final RoundRectangle2D.Float rec2D = new RoundRectangle2D.Float();
            rec2D.width = muWidth - 3;
            rec2D.height = muHeight - 1;
            rec2D.x = 1;
            rec2D.y = 0;
            rec2D.arcwidth = 8;
            rec2D.archeight = 8;

            final GradientPaint gradientPaint = new GradientPaint(1, 1, getActiveMenuColor(), 0, muHeight, getShadowMenuColor());

            final Graphics2D graph2D = (Graphics2D)graph;
            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            graph.setColor(backgroundColor);
            graph2D.fill(rec2D);

            graph.setColor(backgroundColor.darker());
            graph2D.draw(rec2D);

            graph2D.setPaint(gradientPaint);
            graph2D.fill(rec2D);

            graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }

        graph.setColor(origColor);
    }

    public static void paintShadowTitle(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor,
            int displacement, int lineType, int orient)
    {
        // Rotate font if it is supposed to be vertical
        Font titleFont = graph.getFont();
        if (orient == SwingConstants.VERTICAL)
        {
            final AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2);
            titleFont = titleFont.deriveFont(rotate);
        }

        // Paint the shadow and stack all the stuff on top
        if (shadowColor != null)
        {
            final int shadowThickness = lineType == NARROW ? MENU_SHADOW_THICKNESS : TITLE_SHADOW_THICKNESS;

            final Rectangle2D rect = graph.getFontMetrics().getStringBounds(title, graph);

            final int width = 6 * shadowThickness
                    + (orient == SwingConstants.HORIZONTAL ? (int)rect.getWidth() : (int)rect.getHeight());
            final int height = 6 * shadowThickness
                    + (orient == SwingConstants.HORIZONTAL ? (int)rect.getHeight() : (int)rect.getWidth());

            // Title Shadow
            final BufferedImage titleImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            final BufferedImage shadowImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            final Graphics2D graph2D = titleImage.createGraphics();
            graph2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            graph2D.setFont(titleFont);
            graph2D.setColor(shadowColor);
            graph2D.drawString(title, 3 * shadowThickness, 3 * shadowThickness);

            final ConvolveOp convolveOperation = new ConvolveOp(lineType == NARROW ? menuShadowKernel : titleShaodowKernel,
                    ConvolveOp.EDGE_NO_OP, null);

            convolveOperation.filter(titleImage, shadowImage);

            graph.drawImage(shadowImage, x - 3 * shadowThickness + displacement, y - 3 * shadowThickness + displacement, null);
        }

        // Now draw in the title text
        if (null != textColor)
        {
            graph.setFont(titleFont);
            graph.setColor(textColor);
            graph.drawString(title, x, y);
        }
    }

    public static void paintShadowTitleFat(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor,
            int displacement)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadowColor, displacement, WIDE, SwingConstants.HORIZONTAL);
    }

    public static void paintShadowTitleFatV(Graphics graph, String title, int x, int y, Color textColor)
    {
        paintShadowTitle(graph, title, x, y, textColor, Color.black, 1, WIDE, SwingConstants.VERTICAL);
    }

    public static void paintShadowTitleFatV(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadowColor, 1, WIDE, SwingConstants.VERTICAL);
    }

    public static void paintShadowTitleFatV(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor,
            int dispacement)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadowColor, dispacement, WIDE, SwingConstants.VERTICAL);
    }

    public static void paintShadowTitleThin(Graphics graph, String title, int x, int y, Color textColor)
    {
        paintShadowTitle(graph, title, x, y, textColor, Color.black, 1, NARROW, SwingConstants.HORIZONTAL);
    }

    public static void paintShadowTitleThin(Graphics graph, String title, int x, int y, Color textColor, Color shadow)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadow, 1, NARROW, SwingConstants.HORIZONTAL);
    }

    public static void paintShadowTitleThin(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor,
            int displacement)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadowColor, displacement, NARROW, SwingConstants.HORIZONTAL);
    }

    public static void paintShadowTitleThinV(Graphics graph, String title, int x, int y, Color textColor)
    {
        paintShadowTitle(graph, title, x, y, textColor, Color.black, 1, NARROW, SwingConstants.VERTICAL);
    }

    public static void paintShadowTitleThinV(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadowColor, 1, NARROW, SwingConstants.VERTICAL);
    }

    public static void paintShadowTitleThinV(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor,
            int displacement)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadowColor, displacement, NARROW, SwingConstants.VERTICAL);
    }

    public static void paintShadowTitleWide(Graphics graph, String title, int x, int y, Color textColor)
    {
        paintShadowTitle(graph, title, x, y, textColor, Color.black, 1, WIDE, SwingConstants.HORIZONTAL);
    }

    public static void paintShadowTitleWide(Graphics graph, String title, int x, int y, Color textColor, Color shadowColor)
    {
        paintShadowTitle(graph, title, x, y, textColor, shadowColor, 1, WIDE, SwingConstants.HORIZONTAL);
    }

    /**
     * Read all the byes from an input stream into a byte array.
     *
     * @param input the input stream from which to read the byte array.
     * @return the byte array read from the input stream.
     * @throws IOException if an error is encountered while reading from the
     *             supplied input stream.
     */
    public static byte[] readStream(InputStream input) throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        final byte[] buff = new byte[256];

        while ((bytesRead = input.read(buff, 0, 256)) != -1)
        {
            baos.write(buff, 0, bytesRead);
        }

        return baos.toByteArray();
    }

    /**
     * Scales an icon to a new width and height.
     *
     * @param anIcon the icon to scale, must not be null.
     * @param newWidth the width to which to scale the icon.
     * @param newHeight the height to which to scale the icon.
     * @return the scaled icon.
     */
    public static Icon rescaleIcon(Icon anIcon, int newWidth, int newHeight)
    {
        if (null == anIcon)
        {
            return null;
        }

        if (anIcon.getIconHeight() == newHeight && anIcon.getIconWidth() == newWidth)
        {
            return anIcon;
        }

        final BufferedImage buff = new BufferedImage(anIcon.getIconHeight(), anIcon.getIconWidth(), BufferedImage.TYPE_INT_ARGB);

        final Graphics graph = buff.createGraphics();
        anIcon.paintIcon(null, graph, 0, 0);
        graph.dispose();

        final Image scaledImage = buff.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        return new ImageIcon(scaledImage);
    }
}
