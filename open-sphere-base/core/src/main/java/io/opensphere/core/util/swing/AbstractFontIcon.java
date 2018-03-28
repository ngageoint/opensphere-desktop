package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import io.opensphere.core.util.FontIconEnum;

/**
 * A rendered text icon in which the {@link FontIconEnum} is drawn as a
 * Swing-compatible {@link Icon} instance.
 * <p>
 * This class is abstract and cannot be instantiated. Extensions need to
 * statically register their specific font with the GraphicsEnvironment.
 */
public abstract class AbstractFontIcon implements Icon, FontIcon
{
    /** The default color of the icon. */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    /** The default size of the icon. */
    private static final int DEFAULT_SIZE = 16;

    /** A lock used to enforce synchronization. */
    private static final Object PAINT_LOCK = new Object[0];

    /**
     * The size of the icon, expressed as a font size. Defaults to
     * {@value #DEFAULT_SIZE}.
     */
    private int mySize;

    /**
     * The width of the icon paint area, calculated based on the value of
     * {@link #mySize}.
     */
    private int myWidth;

    /**
     * The height of the icon paint area, calculated based on the value of
     * {@link #mySize}.
     */
    private int myHeight;

    /** The buffer into which the icon is painted. */
    private BufferedImage myBuffer;

    /** The color in which the image will be drawn. */
    private Color myColor;

    /** The icon to be drawn on the screen. */
    private final FontIconEnum myIcon;

    /** The font icons are generated from. */
    private final Font myFont;

    /**
     * Creates a new icon, painted with the default color
     * ({@link #DEFAULT_COLOR}), at the default size ({@link #DEFAULT_SIZE}).
     *
     * @param pFont the font to use.
     * @param pIcon the icon to draw.
     */
    public AbstractFontIcon(Font pFont, FontIconEnum pIcon)
    {
        this(pFont, pIcon, DEFAULT_COLOR);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the default size
     * ({@value #DEFAULT_SIZE}).
     *
     * @param pFont the font to use.
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     */
    public AbstractFontIcon(Font pFont, FontIconEnum pIcon, Color pColor)
    {
        this(pFont, pIcon, pColor, DEFAULT_SIZE);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the supplied
     * size.
     *
     * @param pFont the font to use.
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     * @param pSize the size of the icon.
     */
    public AbstractFontIcon(Font pFont, FontIconEnum pIcon, Color pColor, int pSize)
    {
        myFont = pFont;
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
     * @override
     */
    @Override
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Sets the value of the {@link #myColor} field.
     *
     * @param pColor the value to store in the {@link #myColor} field.
     * @override
     */
    @Override
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
    public FontIconEnum getIcon()
    {
        return myIcon;
    }

    /**
     * Gets the value of the {@link #mySize} field.
     *
     * @return the value stored in the {@link #mySize} field.
     * @override
     */
    @Override
    public int getSize()
    {
        return mySize;
    }

    /**
     * Gets the value of the {@link #myBuffer} field.
     *
     * @return the value stored in the {@link #myBuffer} field.
     * @override
     */
    @Override
    public BufferedImage getImage()
    {
        createBufferIfNull();
        return myBuffer;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics,
     *      int, int)
     */
    @Override
    public void paintIcon(Component pC, Graphics pG, int pX, int pY)
    {
        createBufferIfNull();
        pG.drawImage(myBuffer, pX, pY, null);
    }

    /**
     * Creates {@link #myBuffer} if null.
     */
    private void createBufferIfNull()
    {
        synchronized (PAINT_LOCK)
        {
            if (myBuffer == null)
            {
                myBuffer = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);

                Graphics2D graphics = (Graphics2D)myBuffer.getGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.setFont(myFont.deriveFont(Font.PLAIN, getSize()));
                graphics.setColor(getColor());

                int sy = getSize() - (getSize() / 4) + (getSize() / 16);
                graphics.drawString(myIcon.getFontCode(), 0, sy);
                graphics.dispose();
            }
        }
    }

    /**
     * Sets the value of the {@link #mySize} field.
     *
     * @param pSize the value to store in the {@link #mySize} field.
     * @override
     */
    @Override
    public void setSize(int pSize)
    {
        if (mySize != pSize)
        {
            synchronized (PAINT_LOCK)
            {
                mySize = pSize;
                Font font = myFont.deriveFont(Font.PLAIN, getSize());

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
