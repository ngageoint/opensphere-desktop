package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Utilities;

/**
 * The Class AlertNotificationButton. This class will draw a small oval counter
 * in the upper right portion of the button to keep track of various alerts.
 */
public abstract class AlertNotificationButton extends JButton
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Alert count. */
    private final JLabel myAlertCount;

    /** The FRC. */
    private transient FontRenderContext myFRC;

    /** The Glyph height. */
    private double myGlyphHeight;

    /** The Oval height. */
    private double myOvalHeight;

    /** The Single digit oval width. */
    private double mySingleDigitOvalWidth;

    /** The Double digit oval width. */
    private double myDoubleDigitOvalWidth;

    /** The Count. */
    private int myCount;

    /** The Constant ourFont. */
    private static final Font ourFont = new Font(Font.SANS_SERIF, Font.PLAIN, 9);

    /** The Constant ourSingleOvalWidthScaleFactor. */
    private static final double ourSingleOvalWidthScaleFactor = 2.5;

    /** The Constant ourDoubleOvalWidthScaleFactor. */
    private static final double ourDoubleOvalWidthScaleFactor = 1.75;

    /** The Constant ourSingleOvalWidthXoffset. */
    private static final double ourSingleOvalWidthXoffset = 1.45;

    /** The Constant ourDoubleOvalWidthXoffset. */
    private static final double ourDoubleOvalWidthXoffset = 1.2;

    /** The Constant myGlyphYpos. */
    private static final float ourGlyphYpos = 10.0f;

    /** The Constant ourOffsetBorder. */
    private static final Border ourOffsetBorder = BorderFactory.createEmptyBorder(4, 0, 0, 6);

    /** The Alert color. */
    private Color myAlertColor = Color.RED;

    /**
     * Instantiates a new alert notification button.
     */
    public AlertNotificationButton()
    {
        this(null);
    }

    /**
     * Instantiates a new alert notification button.
     *
     * @param icon the icon
     */
    public AlertNotificationButton(ImageIcon icon)
    {
        super(icon);
        setSize(28, 26);
        setPreferredSize(getSize());
        setMinimumSize(getSize());
        setBorder(null);
        setFocusPainted(false);
        setContentAreaFilled(false);
        myAlertCount = new JLabel("");
        myAlertCount.setFont(ourFont);
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount()
    {
        return myCount;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D graphics = (Graphics2D)g;
        super.paintComponent(g);

        if (myCount > 0)
        {
            if (getBorder() == null || Utilities.sameInstance(getBorder(), ourOffsetBorder))
            {
                setBorder(ourOffsetBorder);
            }
            // Get the font render context
            if (myFRC == null)
            {
                myFRC = graphics.getFontRenderContext();
            }
            GlyphVector glyph = null;
            Rectangle2D glyphBounds = null;
            // Setup a single digit and double digit with for the oval.
            if (mySingleDigitOvalWidth == 0 && myDoubleDigitOvalWidth == 0)
            {
                glyph = ourFont.createGlyphVector(myFRC, "9");
                glyphBounds = glyph.getVisualBounds();
                mySingleDigitOvalWidth = glyphBounds.getWidth() * ourSingleOvalWidthScaleFactor;

                glyph = ourFont.createGlyphVector(myFRC, "99");
                glyphBounds = glyph.getVisualBounds();
                myDoubleDigitOvalWidth = glyphBounds.getWidth() * ourDoubleOvalWidthScaleFactor;
            }

            glyph = ourFont.createGlyphVector(myFRC, Integer.toString(myCount));
            if (myGlyphHeight == 0 && glyphBounds != null)
            {
                myGlyphHeight = glyphBounds.getHeight();
                myOvalHeight = myGlyphHeight * 2.0;
            }

            g.setColor(myAlertColor);

            double w = -1;
            float xOffset = -1;
            if (myAlertCount.getText().length() == 1)
            {
                w = mySingleDigitOvalWidth;
                xOffset = (float)(getWidth() - w / ourSingleOvalWidthXoffset);
            }
            else if (myAlertCount.getText().length() > 1)
            {
                w = myDoubleDigitOvalWidth;
                xOffset = (float)(getWidth() - w / ourDoubleOvalWidthXoffset);
            }
            g.fillRect((int)(getWidth() - w), 0, (int)w, (int)myOvalHeight);

            graphics.setColor(Color.BLACK);
            g.drawRect((int)(getWidth() - w), 0, (int)w, (int)myOvalHeight);

            graphics.setColor(ColorUtilities.getBrightness(myAlertColor) < 130 ? Color.WHITE : Color.BLACK);
            graphics.drawGlyphVector(glyph, xOffset, ourGlyphYpos);
        }
        else
        {
            setBorder(null);
        }
    }

    /**
     * Sets the alert color.
     *
     * @param alertColor the new alert color
     */
    public void setAlertColor(Color alertColor)
    {
        myAlertColor = alertColor;
        repaint();
    }

    /**
     * Sets the alert count on the button.
     *
     * @param cnt the new count
     */
    public abstract void setAlertCount(int cnt);

    /**
     * Sets the alert counter text.
     *
     * @param text the new alert counter text
     */
    public void setAlertCounterText(String text)
    {
        myAlertCount.setText(text);
    }

    /**
     * Sets the count.
     *
     * @param cnt the new count
     */
    public void setCount(int cnt)
    {
        myCount = cnt;
    }
}
