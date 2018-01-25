package io.opensphere.controlpanels.layers.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.LoadsTo;

/**
 * The Class ClockAndOrColorLabel.
 */
public class ClockAndOrColorLabel extends JComponent
{
    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ClockAndOrColorLabel.class);

    /** The points feature icon. */
    private static ImageIcon ourBaseIcon;

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Constant size for this component. */
    private static final Dimension SIZE = new Dimension(16, 17);

    /** The color. */
    private Color myColor;

    /** The Colored base icon. */
    private transient ImageIcon myColoredBaseIcon;

    /** The loads to. */
    private LoadsTo myLoadsTo = LoadsTo.BASE;

    static
    {
        try
        {
            ourBaseIcon = new ImageIcon(ImageIO.read(ClockAndOrColorLabel.class.getResource("/images/base.png")));
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to load image icons for AddDataLeafNodePanel. " + e);
        }
    }

    /**
     * Instantiates a new clock and or color label.
     */
    public ClockAndOrColorLabel()
    {
        super();
        setMinimumSize(SIZE);
        setMaximumSize(SIZE);
        setPreferredSize(SIZE);
    }

    /**
     * Checks if is time driven.
     *
     * @return true, if is time driven
     */
    public boolean isTimeDriven()
    {
        return myLoadsTo != null && myLoadsTo.isTimelineEnabled();
    }

    /**
     * Sets the type.
     *
     * @param dti the new type
     */
    public void setType(DataTypeInfo dti)
    {
        myColor = Color.white;
        myLoadsTo = LoadsTo.BASE;
        if (dti != null && dti.getBasicVisualizationInfo() != null)
        {
            myColor = dti.getBasicVisualizationInfo().getTypeColor();
            myLoadsTo = dti.getBasicVisualizationInfo().getLoadsTo();
            mixColorIntoIcon(myColor);
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Color c = myColor == null ? Color.white : myColor;
        LoadsTo lt = myLoadsTo == null ? LoadsTo.BASE : myLoadsTo;

        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D)g;

        if (LoadsTo.STATIC == lt || lt.isTimelineEnabled())
        {
            g2D.setColor(c);
            int circ = getWidth() - 2;
            g2D.fillOval(0, 1, circ, circ);
            g2D.setColor(ColorUtilities.getBrightness(c) < 130 ? Color.LIGHT_GRAY : Color.BLACK);
            g2D.drawOval(0, 1, circ, circ);
            if (lt.isTimelineEnabled())
            {
                g2D.drawLine(7, 8, 11, 8);
                g2D.drawLine(7, 8, 7, 3);
            }
        }
        else
        {
            g2D.drawImage(myColoredBaseIcon.getImage(), -2, 1, null);
        }
    }

    /**
     * Mix color with icon.
     *
     * @param typeColor the type color
     */
    private void mixColorIntoIcon(Color typeColor)
    {
        if (myColoredBaseIcon == null)
        {
            BufferedImage bi = new BufferedImage(ourBaseIcon.getIconWidth(), ourBaseIcon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            myColoredBaseIcon = new ImageIcon();
            myColoredBaseIcon.setImage(bi);
        }
        Graphics g = myColoredBaseIcon.getImage().getGraphics();
        g.setColor(Color.black);
        ((Graphics2D)g).setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, myColoredBaseIcon.getIconWidth(), myColoredBaseIcon.getIconHeight());
        g.setColor(typeColor);
        ((Graphics2D)g).setComposite(AlphaComposite.SrcOver);
        g.fillRect(0, 0, myColoredBaseIcon.getIconWidth(), myColoredBaseIcon.getIconHeight());
        ((Graphics2D)g).setComposite(AlphaComposite.DstIn);
        g.drawImage(ourBaseIcon.getImage(), 0, 0, null);
    }
}
