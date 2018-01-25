package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * The Class HighlightedBorder.
 */
public class HighlightedBorder extends TitledBorder
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Border color. */
    private final Color myColor;

    /** The Opacity. */
    private static final float ourOpacity = .75f;

    /** The Width offset. */
    private int myWidthOffset;

    /** The X offset. */
    private int myXOffset;

    /**
     * Instantiates a new highlighted border.
     *
     * @param border1 the border1
     * @param s the border text
     * @param i the x location
     * @param j the y location
     * @param font the font
     * @param textColor the text color
     * @param borderColor the border color
     */
    public HighlightedBorder(Border border1, String s, int i, int j, Font font, Color textColor, Color borderColor)
    {
        super(border1, s, i, j, font, textColor);
        float[] colorTok = borderColor.getColorComponents(null);
        myColor = new Color(colorTok[0], colorTok[1], colorTok[2], ourOpacity);
    }

    @Override
    public void paintBorder(Component component, Graphics g, int pI, int j, int pK, int l)
    {
        int k = pK + myWidthOffset;
        int i = pI + myXOffset;
        int i1 = component.getFontMetrics(getFont(component)).getHeight();
        g.setColor(myColor);
        g.fillRect(i + 2, j + 2, k - 4, i1);
        super.paintBorder(component, g, i - 1, j, k, l);
    }

    /**
     * Sets the width offset.
     *
     * @param widthOffset the new width offset
     */
    public void setWidthOffset(int widthOffset)
    {
        myWidthOffset = widthOffset;
    }

    /**
     * Sets the x offset.
     *
     * @param xOffset the new x offset
     */
    public void setXOffset(int xOffset)
    {
        myXOffset = xOffset;
    }
}
