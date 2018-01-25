package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import io.opensphere.core.util.AwesomeIcon;

/**
 * A rendered text icon in which the {@link AwesomeIcon} is drawn as a Swing-compatible {@link Icon} instance.
 */
public class FontAwesomeIcon implements Icon
{
    /**
     * The default color of the icon.
     */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    /**
     * The default size of the icon.
     */
    private static final int DEFAULT_SIZE = 16;

    /**
     * A lock used to enforce synchronization.
     */
    private static final Object PAINT_LOCK = new Object[0];

    /**
     * The size of the icon, expressed as a font size. Defaults to {@value #DEFAULT_SIZE}.
     */
    private int mySize;

    /**
     * The width of the icon paint area, calculated based on the value of {@link #mySize}.
     */
    private int myWidth;

    /**
     * The height of the icon paint area, calculated based on the value of {@link #mySize}.
     */
    private int myHeight;

    /**
     * The buffer into which the icon is painted.
     */
    private BufferedImage myBuffer;

    /**
     * The color in which the image will be drawn.
     */
    private Color myColor;

    /**
     * The icon to be drawn on the screen.
     */
    private AwesomeIcon myIcon;

    static
    {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(SwingUtilities.FONT_AWESOME_FONT);
    }

    /**
     * Creates a new icon, painted with the default color ({@link #DEFAULT_COLOR}), at the default size ({@link #DEFAULT_SIZE}).
     *
     * @param pIcon the icon to draw.
     */
    public FontAwesomeIcon(AwesomeIcon pIcon)
    {
        this(pIcon, DEFAULT_COLOR);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the default size ({@value #DEFAULT_SIZE}).
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     */
    public FontAwesomeIcon(AwesomeIcon pIcon, Color pColor)
    {
        this(pIcon, pColor, DEFAULT_SIZE);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the supplied size.
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     * @param pSize the size of the icon.
     */
    public FontAwesomeIcon(AwesomeIcon pIcon, Color pColor, int pSize)
    {
        myIcon = pIcon;
        myColor = pColor;
        setSize(pSize);
    }

    /**
     * Invalidates the backing buffer, forcing the icon to be repainted.
     */
    protected void invalidate()
    {
        synchronized (PAINT_LOCK)
        {
            myBuffer = null;
        }
    }

    /**
     * Gets the value of the {@link #myColor} field.
     *
     * @return the value stored in the {@link #myColor} field.
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Sets the value of the {@link #myColor} field.
     *
     * @param pColor the value to store in the {@link #myColor} field.
     */
    public void setColor(Color pColor)
    {
        myColor = pColor;
        invalidate();
    }

    /**
     * Gets the value of the {@link #myIcon} field.
     *
     * @return the value stored in the {@link #myIcon} field.
     */
    public AwesomeIcon getIcon()
    {
        return myIcon;
    }

    /**
     * Gets the value of the {@link #mySize} field.
     *
     * @return the value stored in the {@link #mySize} field.
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    @Override
    public void paintIcon(Component pC, Graphics pG, int pX, int pY)
    {
        synchronized (PAINT_LOCK)
        {
            if (myBuffer == null)
            {
                myBuffer = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);

                Graphics2D graphics = (Graphics2D)myBuffer.getGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.setFont(SwingUtilities.FONT_AWESOME_FONT.deriveFont(Font.PLAIN, getSize()));
                graphics.setColor(getColor());

                int sy = getSize() - (getSize() / 4) + (getSize() / 16);
                graphics.drawString(myIcon.getFontCode(), 0, sy);
                graphics.dispose();
            }
        }

        pG.drawImage(myBuffer, pX, pY, null);
    }

    /**
     * Sets the value of the {@link #mySize} field.
     *
     * @param pSize the value to store in the {@link #mySize} field.
     */
    public void setSize(int pSize)
    {
        if (mySize != pSize)
        {
            synchronized (PAINT_LOCK)
            {
                mySize = pSize;
                Font font = SwingUtilities.FONT_AWESOME_FONT.deriveFont(Font.PLAIN, getSize());

                BufferedImage temp = new BufferedImage(mySize, mySize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = (Graphics2D)temp.getGraphics();
                graphics.setFont(font);
                myWidth = graphics.getFontMetrics().charWidth(myIcon.getFontCode().charAt(0));
                myHeight = graphics.getFontMetrics().charWidth(myIcon.getFontCode().charAt(0));

                graphics.dispose();
            }
            invalidate();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Icon#getIconWidth()
     */
    @Override
    public int getIconWidth()
    {
        return myHeight;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Icon#getIconHeight()
     */
    @Override
    public int getIconHeight()
    {
        return myWidth;
    }
}
