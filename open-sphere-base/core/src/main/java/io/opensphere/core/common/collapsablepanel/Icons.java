/**
 *
 */
package io.opensphere.core.common.collapsablepanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 *
 *
 * BIT Systems, Inc <br/>
 * Jan 18, 2012
 * </dl>
 */

public class Icons
{
    private static final Color BACKGROUND = new Color(0, 0, 0, 0);

    private static final Color BORDER_COLOR = Color.GRAY;

    private static final Color FORGROUND = Color.black;

    private static final double OFFSET_RATIO = 0.125;

    private static final double LINEWIDTH_RATIO = 0.125;

    /**
     * Default size
     */
    private static final int SIZE = 16;

    private static final int BORDER_WIDTH = 1;

    /**
     * This will create a square "+" {@link ImageIcon} of dimension
     * {@link #SIZE}.
     *
     * @return
     */
    public static ImageIcon createBoxedPlusIcon()
    {
        return createBoxedPlusIcon(SIZE, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

    public static ImageIcon createBoxedPlusIcon(int pSize)
    {
        return createBoxedPlusIcon(pSize, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

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

    public static ImageIcon createBoxedMinusIcon()
    {
        return createBoxedMinusIcon(SIZE, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

    public static ImageIcon createBoxedMinusIcon(int pSize)
    {
        return createBoxedMinusIcon(pSize, BORDER_COLOR, BORDER_WIDTH, BACKGROUND, FORGROUND);
    }

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

    public static void main(String[] args)
    {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(300, 300));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new FlowLayout());
        
        frame.add(new JLabel("31", createBoxedPlusIcon(31), SwingConstants.LEADING));
        frame.add(new JLabel("30", createBoxedPlusIcon(30), SwingConstants.LEADING));

        frame.add(new JLabel("15", createBoxedPlusIcon(15), SwingConstants.LEADING));
        frame.add(new JLabel("16", createBoxedPlusIcon(16), SwingConstants.LEADING));

        frame.add(new JLabel("10", createBoxedPlusIcon(10), SwingConstants.LEADING));
        frame.add(new JLabel("11", createBoxedPlusIcon(11), SwingConstants.LEADING));

        frame.add(new JLabel("132", createBoxedPlusIcon(132), SwingConstants.LEADING));
        frame.add(new JLabel("131", createBoxedPlusIcon(131), SwingConstants.LEADING));

        frame.add(new JLabel("31", createBoxedMinusIcon(31), SwingConstants.LEADING));
        frame.add(new JLabel("30", createBoxedMinusIcon(30), SwingConstants.LEADING));

        frame.add(new JLabel("15", createBoxedMinusIcon(15), SwingConstants.LEADING));
        frame.add(new JLabel("16", createBoxedMinusIcon(16), SwingConstants.LEADING));

        frame.add(new JLabel("10", createBoxedMinusIcon(10), SwingConstants.LEADING));
        frame.add(new JLabel("11", createBoxedMinusIcon(11), SwingConstants.LEADING));

        frame.add(new JLabel("132", createBoxedMinusIcon(132), SwingConstants.LEADING));
        frame.add(new JLabel("131", createBoxedMinusIcon(131), SwingConstants.LEADING));

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                frame.setVisible(true);
            }
        });
    }
}

/**
 *
 */
