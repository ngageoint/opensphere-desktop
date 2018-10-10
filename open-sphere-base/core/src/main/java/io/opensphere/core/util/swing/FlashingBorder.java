package io.opensphere.core.util.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

/** A border that flashes to get the user's attention. */
public class FlashingBorder implements Border
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FlashingBorder.class);

    /** The alternate color. */
    private final Color myAlternateColor;

    /** The Stroke to use with the alternate color. */
    private final BasicStroke myAlternateStroke = new BasicStroke(2.0f);

    /** The Component which this border surround. */
    private final JComponent myContainedComponent;

    /** The primary color. */
    private final Color myPrimaryColor;

    /** The stroke to use with the primary color. */
    private final BasicStroke myPrimaryStroke = new BasicStroke(1.0f);

    /**
     * Swing timer used to repaint this border to change the color.
     */
    private Timer myRepaintTimer;

    /** When true, use the primary color. Otherwise, use the alternate color. */
    private boolean myUsePrimary;

    /**
     * Constructor.
     *
     * @param comp the component which this border surrounds.
     * @param primaryColor The primary color to use for the flash.
     * @param alternateColor The alternate color to use for the flash.
     * @param flashDelayMillis The amount of time between color changes.
     */
    public FlashingBorder(JComponent comp, Color primaryColor, Color alternateColor, int flashDelayMillis)
    {
        myContainedComponent = comp;
        myPrimaryColor = primaryColor;
        myAlternateColor = alternateColor;
        setActive(true);
    }

    @Override
    public Insets getBorderInsets(java.awt.Component arg0)
    {
        return new Insets(2, 2, 2, 2);
    }

    @Override
    public boolean isBorderOpaque()
    {
        return false;
    }

    @Override
    public void paintBorder(java.awt.Component comp, Graphics g, int x, int y, int width, int height)
    {
        Graphics2D g2D = (Graphics2D)g;

        if (!myUsePrimary)
        {
            g2D.setStroke(myPrimaryStroke);
            g2D.setColor(myPrimaryColor);
            g2D.drawRect(x, y, width - 1, height - 1);
        }
        else
        {
            g2D.setStroke(myAlternateStroke);
            g2D.setColor(myAlternateColor);
            g2D.drawRect(x + 2, y + 2, width - 4, height - 4);
        }
    }

    /**
     * Turn the flashing on and off. When it is off, the primary color will be
     * used.
     *
     * @param active When true the border will flash.
     */
    public final void setActive(boolean active)
    {
        cleanup();
        if (active)
        {
            if (!myContainedComponent.isVisible())
            {
                LOGGER.warn("A flashing border cannot be made active when the component it contains is not visible.");
            }
            myRepaintTimer = new RepaintTimer(300);
            myRepaintTimer.start();
        }
    }

    /** Stop flashing and set the border back to the primary color. */
    private void cleanup()
    {
        if (myRepaintTimer != null)
        {
            myRepaintTimer.stop();
            myRepaintTimer = null;
        }
        myUsePrimary = true;
    }

    /**
     * A timer that changes the color of the border repaints periodically.
     */
    private class RepaintTimer extends Timer
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Construct the repaint timer.
         *
         * @param periodMillis How often in milliseconds to repaint.
         */
        public RepaintTimer(int periodMillis)
        {
            super(periodMillis, e ->
            {
                if (!myContainedComponent.isVisible())
                {
                    cleanup();
                    return;
                }

                myUsePrimary = !myUsePrimary;
                myContainedComponent.repaint();
            });
        }
    }
}
