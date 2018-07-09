package io.opensphere.mantle.iconproject.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import io.opensphere.core.util.FontIconEnum;
import io.opensphere.mantle.icon.IconRecord;

/**
 * A rendered text icon in which the {@link FontIconEnum} is drawn as a
 * Swing-compatible {@link Icon} instance.
 * <p>
 * This class is abstract and cannot be instantiated. Extensions need to
 * statically register their specific font with the GraphicsEnvironment.
 */
public class IconProjProps implements Icon
{
    /** The default color of the icon. */
    private static final Color DEFAULT_COLOR = Color.BLACK;

    /** The default size of the icon. */
    private static final int DEFAULT_SIZE = 40;

    /** A lock used to enforce synchronization. */
    private final Object PAINT_LOCK = new Object[0];

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

    /** The Y position of the icon in the paint area. */
    private Integer myYPos;

    /** The X position of the icon in the paint area. */
    private Integer myXPos;

    /** The buffer into which the icon is painted. */
    private BufferedImage myBuffer;

    /** The color in which the image will be drawn. */
    private Color myColor;

    /** The icon to be drawn on the screen. */
    private final IconRecord myIcon;

    /**
     * Creates a new icon, painted with the default color
     * ({@link #DEFAULT_COLOR}), at the default size ({@link #DEFAULT_SIZE}).
     *
     * @param pIcon the icon to draw.
     */
    public IconProjProps(IconRecord pIcon)
    {
        this(pIcon, DEFAULT_COLOR);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the default size
     * ({@value #DEFAULT_SIZE}).
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     */
    public IconProjProps(IconRecord pIcon, Color pColor)
    {
        this(pIcon, pColor, DEFAULT_SIZE);
    }

    /**
     * Creates a new icon, painted with the supplied color, at the supplied
     * size.
     *
     * @param pIcon the icon to draw.
     * @param pColor the color to draw the icon.
     * @param pSize the size of the icon.
     */
    public IconProjProps(IconRecord pIcon, Color pColor, int pSize)
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
     * @override
     */
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
    public IconRecord getIcon()
    {
        return myIcon;
    }

    /**
     * Gets the value of the {@link #mySize} field.
     *
     * @return the value stored in the {@link #mySize} field.
     * @override
     */
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
        if (pG instanceof Graphics2D)
        {
            ((Graphics2D)pG).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        double centerY = pC.getHeight() / 2;

        int characterWidth = 75;
        int characterHeight = 75;;

        // make sure that things are centered:
        double xpos = pX + centerY - characterWidth / 2 - 3 + getXPos();
        double ypos = centerY + characterHeight / 2 - 1;

        pG.setColor(getColor());

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
                int width = mySize > myWidth ? mySize : myWidth;
                int height = mySize > myHeight ? mySize : myHeight;
                myBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                Graphics2D graphics = myBuffer.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setColor(getColor());

                graphics.drawImage(myBuffer, null, width, height);
                graphics.dispose();
            }
        }
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
                myXPos = null;
                myYPos = null;

                mySize = pSize;
         //       Font font = myFont.deriveFont(Font.PLAIN, getSize());

                BufferedImage temp = new BufferedImage(mySize, mySize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = temp.createGraphics();
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

    /**
     * Sets the value of the {@link #myYPos} field.
     *
     * @param yPos the value to store in the {@link #myYPos} field.
     */
    public void setYPos(int yPos)
    {
        myYPos = Integer.valueOf(yPos);
        invalidate();
    }

    /**
     * Sets the value of the {@link #myXPos} field.
     *
     * @param xPos the value to store in the {@link #myXPos} field.
     */
    public void setXPos(int xPos)
    {
        myXPos = Integer.valueOf(xPos);
        invalidate();
    }

    /**
     * Returns the value of the {@link #myYPos} field, or calculates a value
     * based on {@link #mySize} if null.
     *
     * @return the Y position
     */
    public int getYPos()
    {
        return myYPos == null ? mySize - mySize / 4 + mySize / 16 : myYPos.intValue();
    }

    /**
     * Returns the value of the {@link #myXPos} field, 0 if null.
     *
     * @return the X position
     */
    public int getXPos()
    {
        return myXPos == null ? 0 : myXPos.intValue();
    }

    /**
     * Returns a copy of the icon with the given color.
     *
     * @param color the color
     * @return the icon
     */
    public IconProjProps withColor(Color color)
    {
        IconProjProps icon = new IconProjProps(myIcon, color, mySize);
        icon.myXPos = myXPos;
        icon.myYPos = myYPos;
        icon.myColor = color;
        return icon;
    }
}