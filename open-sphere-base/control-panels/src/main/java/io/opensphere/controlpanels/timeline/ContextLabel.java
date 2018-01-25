package io.opensphere.controlpanels.timeline;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import io.opensphere.core.util.ColorUtilities;

/** Context label layer. */
public class ContextLabel extends AbstractTimelineLayer
{
    /** The text. */
    private String myText;

    /** The x location. */
    private int myX;

    /** The y location. */
    private int myY;

    /** Opacity for the label. */
    private float myAlpha = 1f;

    /** Option font for the label. */
    private final Font myFont;

    /**
     * Constructor.
     */
    public ContextLabel()
    {
        this((Font)null);
    }

    /**
     * Constructor.
     *
     * @param font The font for the label.
     */
    public ContextLabel(Font font)
    {
        super();
        myFont = font;
    }

    /**
     * Constructor.
     *
     * @param text the text
     * @param x the x location
     * @param y the y location
     */
    public ContextLabel(String text, int x, int y)
    {
        this((Font)null, text, x, y);
    }

    /**
     * Constructor.
     *
     * @param font the font
     * @param text the text
     * @param x the x location
     * @param y the y location
     */
    public ContextLabel(Font font, String text, int x, int y)
    {
        this(font);
        myText = text;
        myX = x;
        myY = y;
    }

    @Override
    public void paint(Graphics2D g)
    {
        super.paint(g);

        Graphics2D g2d = (Graphics2D)g.create();
        try
        {
            if (myFont != null)
            {
                g2d.setFont(myFont);
            }

            // Paint the background
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(myText, g2d);
            int height = (int)Math.round(bounds.getHeight());
            int width = (int)Math.round(bounds.getWidth());
            int x = myX;
            int maxX = (int)getUIModel().getLabelPanelBounds().getMaxX();
            if (x < 0 && x >= -width - 15)
            {
                x = 0;
            }
            else if (x > maxX - width && x <= maxX + 15)
            {
                x = maxX - width;
            }
            Rectangle fillRect = new Rectangle(x - 1, myY - height + 3, width + 2, height - 1);
            Color background = Color.GRAY;
            if (myAlpha != 1f)
            {
                background = ColorUtilities.opacitizeColor(background, myAlpha);
            }
            g2d.setColor(background);
            g2d.fill(fillRect);

            // Paint the text
            Color foreground = getUIModel().getComponent().getForeground();
            if (myAlpha != 1f)
            {
                foreground = ColorUtilities.opacitizeColor(foreground, myAlpha);
            }
            g2d.setColor(foreground);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawString(myText, x, myY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
        finally
        {
            g2d.dispose();
        }
    }

    /**
     * Updates the values.
     *
     * @param text the text
     * @param x the x location
     * @param y the y location
     */
    public void update(String text, int x, int y)
    {
        update(text, x, y, 1f);
    }

    /**
     * Updates the values.
     *
     * @param text the text
     * @param x the x location
     * @param y the y location
     * @param alpha the opacity
     */
    public void update(String text, int x, int y, float alpha)
    {
        myText = text;
        myX = x;
        myY = y;
        myAlpha = alpha;
    }
}
