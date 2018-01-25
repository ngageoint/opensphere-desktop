package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * A text field that provides ghost text until the user types something, and
 * provides an icon button that will clear the typed text.
 */
public class GhostTextField extends JTextField
{
    /** Property that indicates the reset button was pressed. */
    public static final String CLEAR_PROPERTY = "Clear";

    /**
     * The insets. The right inset is 6(default) + 14(image) + 5(space between
     * image and text).
     */
    private static final Insets INSETS = new Insets(5, 6, 5, 22);

    /** The LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(GhostTextField.class);

    /**
     * SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The current image. */
    private BufferedImage myCurrImage;

    /** The default image. */
    private BufferedImage myDefaultImage;

    /** The Ghost text. */
    private String myGhostText;

    /** The mouse click image. */
    private BufferedImage myMouseClickImage;

    /** The mouse over image. */
    private BufferedImage myMouseOverImage;

    /** The x coordinate of the left edge of the icon. */
    private int myX;

    /**
     * Instantiates a new search text field with no ghost text and the default
     * icons.
     */
    public GhostTextField()
    {
        this(null);
    }

    /**
     * Instantiates a new search text field with defined ghost text and the
     * default icons.
     *
     * @param ghostText the ghost text that appears when the search contents are
     *            empty.
     */
    public GhostTextField(String ghostText)
    {
        this(ghostText, "/images/close_window_14x14.png", "/images/close_window_rollover_14x14.png",
                "/images/close_window_rollover_14x14_1.png");
    }

    /**
     * Instantiates a new search text field with this icon set.
     *
     * @param ghostText the ghost text
     * @param defaultImage the default image for the clear button
     * @param rolloverImage the rollover image for the clear button
     * @param pressedImage the pressed image for the clear button
     */
    public GhostTextField(String ghostText, String defaultImage, String rolloverImage, String pressedImage)
    {
        myGhostText = ghostText;
        URL url = null;
        try
        {
            if (StringUtils.isNotEmpty(defaultImage))
            {
                url = this.getClass().getResource(defaultImage);
                myDefaultImage = javax.imageio.ImageIO.read(url);
                myCurrImage = myDefaultImage;
            }

            if (StringUtils.isNotEmpty(rolloverImage))
            {
                url = this.getClass().getResource(rolloverImage);
                myMouseOverImage = javax.imageio.ImageIO.read(url);
            }

            if (StringUtils.isNotEmpty(pressedImage))
            {
                url = this.getClass().getResource(pressedImage);
                myMouseClickImage = javax.imageio.ImageIO.read(url);
            }
        }
        catch (IOException e1)
        {
            LOGGER.warn(e1);
        }
        MouseListener mouseListener = new MouseListener();
        addMouseMotionListener(mouseListener);
        addMouseListener(mouseListener);
    }

    @Override
    public Insets getInsets()
    {
        return INSETS;
    }

    /**
     * Sets the ghost text.
     *
     * @param ghostText the new ghost text
     */
    public void setGhostText(String ghostText)
    {
        myGhostText = ghostText;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (StringUtils.isBlank(getText()))
        {
            if (myGhostText != null)
            {
                Graphics2D g2d = (Graphics2D)g;
                FontMetrics fm = g2d.getFontMetrics();
                g2d.setColor(hasFocus() ? Color.GRAY : Color.LIGHT_GRAY);
                int fontHeight = fm.getMaxAscent() + fm.getMaxDescent();
                int y2 = getHeight() / 2 - fontHeight / 2;
                g2d.drawString(myGhostText, 8, y2 + fm.getMaxAscent());
            }
        }
        else if (myCurrImage != null)
        {
            Border border = UIManager.getBorder("TextField.border");
            JTextField defaultField = new JTextField();
            myX = getWidth() - border.getBorderInsets(defaultField).right - myCurrImage.getWidth();
            // setMargin(new Insets(2, 2, 2, getWidth() - x));
            int y = (getHeight() - myCurrImage.getHeight()) / 2;
            g.drawImage(myCurrImage, myX, y, this);
        }
    }

    /**
     * Mouse listener that changes {@link #myCurrImage}.
     */
    private final class MouseListener extends MouseAdapter
    {
        @Override
        public void mouseExited(MouseEvent e)
        {
            if (myCurrImage != null && !myCurrImage.equals(myDefaultImage))
            {
                myCurrImage = myDefaultImage;
                paintComponent(getGraphics());
                revalidate();
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
            if (myCurrImage != null)
            {
                if (e.getX() >= myX && e.getX() <= getWidth())
                {
                    if (!myCurrImage.equals(myMouseOverImage))
                    {
                        myCurrImage = myMouseOverImage;
                        paintComponent(getGraphics());
                    }
                }
                else
                {
                    if (!myCurrImage.equals(myDefaultImage))
                    {
                        myCurrImage = myDefaultImage;
                        paintComponent(getGraphics());
                    }
                }
                revalidate();
                repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            if (e.getX() >= myX && e.getX() <= getWidth() && !myCurrImage.equals(myMouseClickImage))
            {
                myCurrImage = myMouseClickImage;
                paintComponent(getGraphics());
                revalidate();
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (e.getX() >= myX && e.getX() <= getWidth())
            {
                if (!myCurrImage.equals(myMouseOverImage))
                {
                    myCurrImage = myMouseOverImage;
                    paintComponent(getGraphics());
                    revalidate();
                    repaint();
                }

                if (getText().length() > 0)
                {
                    setText("");
                }
                else
                {
                    firePropertyChange(CLEAR_PROPERTY, null, Boolean.TRUE);
                }
            }
            else
            {
                if (!myCurrImage.equals(myDefaultImage))
                {
                    myCurrImage = myDefaultImage;
                    paintComponent(getGraphics());
                    revalidate();
                    repaint();
                }
            }
        }
    }
}
