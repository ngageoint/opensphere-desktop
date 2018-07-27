package io.opensphere.overlay.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import io.opensphere.core.util.AwesomeIconSolid;

/**
 * A utility used to assist with graphics-related tasks for HUD components.
 */
public final class HUDGraphicUtilities
{
    /**
     * Private constructor, hidden from use on utility classes.
     */
    private HUDGraphicUtilities()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Creates a square buffered image in which the supplied icon is drawn.
     * 
     * @param icon the icon to render on the image.
     * @param imageSize the length and width of the image to draw, expressed in
     *            pixels.
     * @param fontSize the font size for the rendering.
     * @param background the background color of the button.
     * @param foreground the foreground (icon) color of the button.
     * @return an image drawn using the supplied parameters.
     */
    public static BufferedImage drawIcon(AwesomeIconSolid icon, int imageSize, int fontSize, Color background, Color foreground)
    {
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(background);
        graphics.fillRect(0, 0, imageSize, imageSize);

        graphics.setFont(icon.getFont().deriveFont(Font.PLAIN, fontSize));
        graphics.setColor(foreground);

        FontRenderContext fontRenderContext = graphics.getFontRenderContext();
        TextLayout textLayout = new TextLayout(icon.getFontCode(), icon.getFont().deriveFont(Font.PLAIN, 14), fontRenderContext);
        Rectangle2D bounds = textLayout.getBounds();

        double width = bounds.getWidth();
        double height = bounds.getHeight();

        double boxSize = Math.max(width, height);

        int xLocation = (int)Math.round((imageSize - boxSize) / 2);
        int yLocation = (int)Math.round((imageSize - boxSize) / 2 + boxSize);
        textLayout.draw(graphics, xLocation, yLocation);

        graphics.dispose();
        return image;
    }

    /**
     * Creates a square buffered image in which the supplied text is drawn.
     * 
     * @param text the text to render on the image.
     * @param imageSize the length and width of the image to draw, expressed in
     *            pixels.
     * @param fontSize the font size for the rendering.
     * @param background the background color of the button.
     * @param foreground the foreground (icon) color of the button.
     * @return an image drawn using the supplied parameters.
     */
    public static BufferedImage drawIcon(String text, int imageSize, int fontSize, Color background, Color foreground)
    {
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(background);
        graphics.fillRect(0, 0, imageSize, imageSize);

        graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, fontSize));
        graphics.setColor(foreground);

        FontRenderContext fontRenderContext = graphics.getFontRenderContext();
        TextLayout textLayout = new TextLayout(text, graphics.getFont().deriveFont(Font.BOLD, 13), fontRenderContext);
        Rectangle2D bounds = textLayout.getBounds();

        double width = bounds.getWidth();
        double height = bounds.getHeight();

        int xLocation = (int)((imageSize - width) / 2);
        int yLocation = (int)((imageSize - height) / 2 + height);
        textLayout.draw(graphics, xLocation, yLocation);

        graphics.dispose();
        return image;
    }
}
