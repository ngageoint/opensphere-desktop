/**
 *
 */
package io.opensphere.core.common.collapsablepanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 *
 *
 * BIT Systems, Inc <br/>
 * Jan 18, 2012
 * </dl>
 */

public class Icons
{
    /** Default background color. */
    private static final Color BACKGROUND = new Color(0, 0, 0, 0);

    /** Default border color. */
    private static final Color BORDER_COLOR = Color.GRAY;

    /** Default foreground color. */
    private static final Color FORGROUND = Color.black;

    /** Ratio to determine icon offset compared to size. */
    private static final double OFFSET_RATIO = 0.125;

    /** Ratio to determine line width. */
    private static final double LINEWIDTH_RATIO = 0.125;

    /** Default size */
    private static final int SIZE = 16;

    /** Default border width. */
    private static final int BORDER_WIDTH = 1;

    /**
     * This will create a square "+" {@link ImageIcon} of dimension
     * {@link #SIZE}.
     *
     * @return the icon
     */
    public static ImageIcon createBoxedPlusIcon()
    {
        return createBoxedPlusIcon(SIZE, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

    /**
     * This will create a square "+" {@link ImageIcon}.
     *
     * @param pSize the icon size
     * @return the icon
     */
    public static ImageIcon createBoxedPlusIcon(int pSize)
    {
        return createBoxedPlusIcon(pSize, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

    /**
     * This will create a square "+" {@link ImageIcon}.
     *
     * @param pSize the icon size
     * @param pBorderColor the border color
     * @param pBorderWidth the border width
     * @param pBackground the background color
     * @param pForeground the foreground color
     * @return the icon
     */
    public static final ImageIcon createBoxedPlusIcon(int pSize, Color pBorderColor, int pBorderWidth, Color pBackground,
            Color pForeground)
    {
        ImageIcon icon = null;
        BufferedImage image = new BufferedImage(pSize, pSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        g.setBackground(pBackground);
        g.clearRect(0, 0, pSize, pSize);

        // draw the border;
        g.setPaint(pBorderColor);
        int endX = 0;
        int endY = 0;
        int startX = 0;
        int startY = 0;
        for (int i = 0; i < pBorderWidth; i++)
        {
            endX = pSize - 1 - i;
            endY = pSize - 1 - i;
            startX = i;
            startY = i;
            g.drawLine(startX, startY, startX, endY);
            g.drawLine(startX, startY, endX, startY);
            g.drawLine(endX, startY, endX, endY);
            g.drawLine(startX, endY, endX, endY);
        }

        // draw the plus
        g.setPaint(pForeground);
        int interiorDim = pSize - 2 * pBorderWidth;

        int edgeOffset = (int)(OFFSET_RATIO * interiorDim);
        //        int yEdgeOffset = (int)(OFFSET_RATIO * interiorDim);
        int lineWidth = 0;

        // compute line width
        lineWidth = (int)(interiorDim * LINEWIDTH_RATIO);
        if (interiorDim % 2 == 0) // even width
        {
            if (lineWidth % 2 != 0)
            {
                lineWidth += 1;
            }
        }
        else // odd icon width
        {
            if (lineWidth % 2 == 0)
            {
                lineWidth += 1;
            }
        }

        // draw vertical
        startY = pBorderWidth + edgeOffset;
        endY = pSize - 1 - pBorderWidth - edgeOffset;
        startX = pSize / 2 - lineWidth / 2;
        for (int i = 0; i < lineWidth; i++)
        {
            g.drawLine(startX + i, startY, startX + i, endY);
        }

        // draw horizontal
        startX = pBorderWidth + edgeOffset;
        endX = pSize - 1 - pBorderWidth - edgeOffset;
        startY = pSize / 2 - lineWidth / 2;
        for (int i = 0; i < lineWidth; i++)
        {
            g.drawLine(startX, startY + i, endX, startY + i);
        }

        icon = new ImageIcon(image);
        return icon;
    }

    /**
     * This will create a square "-" {@link ImageIcon} of dimension
     * {@link #SIZE}.
     *
     * @return the icon
     */
    public static ImageIcon createBoxedMinusIcon()
    {
        return createBoxedMinusIcon(SIZE, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

    /**
     * This will create a square "-" {@link ImageIcon}.
     *
     * @param pSize the icon size
     * @return the icon
     */
    public static ImageIcon createBoxedMinusIcon(int pSize)
    {
        return createBoxedMinusIcon(pSize, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

    /**
     * This will create a square "-" {@link ImageIcon}.
     *
     * @param pSize the icon size
     * @param pBorderColor the border color
     * @param pBorderWidth the border width
     * @param pBackground the background color
     * @param pForeground the foreground color
     * @return the icon
     */
    public static final ImageIcon createBoxedMinusIcon(int pSize, Color pBorderColor, int pBorderWidth, Color pBackground,
            Color pForeground)
    {
        ImageIcon icon = null;
        BufferedImage image = new BufferedImage(pSize, pSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();
        g.setBackground(pBackground);
        g.clearRect(0, 0, pSize, pSize);

        // draw the border;
        g.setPaint(pBorderColor);
        int endX = 0;
        int endY = 0;
        int startX = 0;
        int startY = 0;
        for (int i = 0; i < pBorderWidth; i++)
        {
            endX = pSize - 1 - i;
            endY = pSize - 1 - i;
            startX = i;
            startY = i;
            g.drawLine(startX, startY, startX, endY);
            g.drawLine(startX, startY, endX, startY);
            g.drawLine(endX, startY, endX, endY);
            g.drawLine(startX, endY, endX, endY);
        }

        // draw the Minus
        g.setPaint(pForeground);
        int interiorDim = pSize - 2 * pBorderWidth;

        int xEdgeOffset = (int)(OFFSET_RATIO * interiorDim);
        int yEdgeOffset = (int)(OFFSET_RATIO * interiorDim);
        int lineWidth = 0;

        // compute line width
        lineWidth = (int)(interiorDim * LINEWIDTH_RATIO);
        if (interiorDim % 2 == 0) // even width
        {
            if (lineWidth % 2 != 0)
            {
                lineWidth += 1;
            }
        }
        else // odd icon width
        {
            if (lineWidth % 2 == 0)
            {
                lineWidth += 1;
            }
        }

        // draw horizontal
        startX = pBorderWidth + yEdgeOffset;
        endX = pSize - 1 - pBorderWidth - xEdgeOffset;
        startY = pSize / 2 - lineWidth / 2;
        for (int i = 0; i < lineWidth; i++)
        {
            g.drawLine(startX, startY + i, endX, startY + i);
        }

        icon = new ImageIcon(image);
        return icon;
    }
}
