package io.opensphere.core.util.swing.tags;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;

/**
 * A simple component to display text and a dismiss icon for use in a
 * {@link JTagField} or JTagArea.
 */
public class JTag extends JPanel
{
    /** The unique identifier used for serialization operations. */
    private static final long serialVersionUID = -6104352908941725751L;

    /** The default tag color, used if no background is supplied. */
    public static final Color DEFAULT_COLOR = new Color(210, 219, 248);

    /** The text displayed in the component. */
    private final String myText;

    /** The component in which the content is rendered. */
    private final JLabel myTextComponent;

    /** The button used to dismiss the tag. */
    private final JButton myCloseButton;

    /**
     * The consumer which will receive the text of the tag when the close button
     * is pressed, may be null.
     */
    private Consumer<String> myCloseHandler;

    /**
     * Creates a new tag with the supplied text, no listener, and the default
     * color. By default, the border color of the tag is derived from the
     * background color.
     *
     * @param text the text with which to populate the tag.
     */
    public JTag(String text)
    {
        this(text, DEFAULT_COLOR);
    }

    /**
     * Creates a new tag with the supplied text, the supplied listener, and the
     * default color. By default, the border color of the tag is derived from
     * the background color.
     *
     * @param text the text with which to populate the tag.
     * @param closeHandler the consumer which will receive the text of the tag
     *            when the close button is pressed, may be null.
     */
    public JTag(String text, Consumer<String> closeHandler)
    {
        this(text, closeHandler, DEFAULT_COLOR);
    }

    /**
     * Creates a new tag with the supplied text, no listener, and the supplied
     * color. By default, the border color of the tag is derived from the
     * background color.
     *
     * @param text the text with which to populate the tag.
     * @param color the color with which to shade the tag component.
     */
    public JTag(String text, Color color)
    {
        this(text, null, color);
    }

    /**
     * Creates a new tag with the supplied text, the supplied listener, and the
     * supplied color. By default, the border color of the tag is derived from
     * the background color.
     *
     * @param text the text with which to populate the tag.
     * @param closeHandler the consumer which will receive the text of the tag
     *            when the close button is pressed, may be null.
     * @param color the color with which to shade the tag component.
     */
    public JTag(String text, Consumer<String> closeHandler, Color color)
    {
        this(text, closeHandler, color, null);
    }

    /**
     * Creates a new tag with the supplied text, the supplied listener, the
     * supplied color, and the supplied text color. By default, the border
     * color of the tag is derived from the background color.
     *
     * @param text the text with which to populate the tag.
     * @param closeHandler the consumer which will receive the text of the tag
     *            when the close button is pressed, may be null.
     * @param color the color with which to shade the tag component.
     * @param textColor the color the text will be, may be null.
     */
    public JTag(String text, Consumer<String> closeHandler, Color color, Color textColor)
    {
        super(new FlowLayout(FlowLayout.CENTER, 5, 0));
        setBorder(new RoundedBorder(getBorderColor(color)));
        getInsets().set(0, 5, 0, 5);
        myCloseHandler = closeHandler;
        setBackground(color);

        myText = text;

        myTextComponent = new JLabel(myText);
        if (textColor != null)
        {
            myTextComponent.setForeground(textColor);
        }
        myTextComponent.setBorder(BorderFactory.createEmptyBorder(4,0,3,0));
        add(myTextComponent);

        ImageIcon icon = IconUtil.getIcon(IconType.CLOSE);
        icon.setImage(icon.getImage().getScaledInstance(10, 10, 0));

        myCloseButton = new JButton(icon);
        myCloseButton.setBorder(BorderFactory.createEmptyBorder());
        myCloseButton.setContentAreaFilled(false);

        if (myCloseHandler != null)
        {
            myCloseButton.addActionListener(e -> myCloseHandler.accept(myText));
        }

        add(myCloseButton);
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);

        myCloseButton.setEnabled(enabled);
    }

    /**
     * Sets the color of the border for the tag.
     *
     * @param borderColor the color of the border for the tag.
     */
    public void setBorderColor(Color borderColor)
    {
        setBorder(new RoundedBorder(getBorderColor(borderColor)));
    }

    /**
     * Sets the value of the {@link #myCloseHandler} field.
     *
     * @param closeHandler the value to store in the {@link #myCloseHandler}
     *            field.
     */
    public void setCloseHandler(Consumer<String> closeHandler)
    {
        myCloseHandler = closeHandler;
        if (myCloseHandler != null)
        {
            myCloseButton.addActionListener(e -> myCloseHandler.accept(myText));
        }
    }

    /**
     * Derives the a border color from the supplied background color.
     *
     * @param backgroundColor the background color from which to derive the
     *            border color.
     * @return the color to use for the border.
     */
    protected Color getBorderColor(Color backgroundColor)
    {
        int backgroundRed = backgroundColor.getRed();
        int backgroundGreen = backgroundColor.getGreen();
        int backgroundBlue = backgroundColor.getBlue();

        int r = getBorderColorComponent(backgroundRed, .8);
        int g = getBorderColorComponent(backgroundGreen, .83);
        int b = getBorderColorComponent(backgroundBlue, .87);

        Color borderColor = new Color(r, g, b);
        return borderColor;
    }

    /**
     * Derives a border color component from the supplied color value and
     * percentage. If the color is very dark (less than 50), then the border
     * color will be lighter than the supplied value, otherwise, it will be
     * darker.
     *
     * @param sourceColor the source color from which to derive the new value.
     * @param percentage the percentage by which to move the source color to
     *            derive the new value.
     * @return a derived color component calculated from the source color.
     */
    protected int getBorderColorComponent(int sourceColor, double percentage)
    {
        int r;
        if (sourceColor < 50)
        {
            sourceColor = 255 - sourceColor;
            r = 255 - (int)(sourceColor * percentage);
        }
        else
        {
            r = (int)(sourceColor * percentage);
        }
        return r;
    }

    /**
     * Gets the value of the {@link #myText} field.
     *
     * @return the value stored in the {@link #myText} field.
     */
    public String getText()
    {
        return myText;
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D)g).setColor(getBackground());
        ((Graphics2D)g).fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

        paintChildren(g);
        paintBorder(g);
    }
}
