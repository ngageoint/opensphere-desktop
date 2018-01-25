package io.opensphere.core.util.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/** A button which displays a different icon for each available state. */
public class QuadStateIconButton extends JButton
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The icon to use when in the DEFAULT state. */
    private final ImageIcon myDefaultIcon;

    /** The icon to use when in the PRESSED state. */
    private final ImageIcon myPressedIcon;

    /** The icon to use when in the ROLLOVER state. */
    private final ImageIcon myRolloverIcon;

    /** The icon to use when in the SELECTED state. */
    private final ImageIcon mySelectedIcon;

    /** The Current icon. */
    private ImageIcon myCurrentIcon;

    /** The current state of the button. */
    private ButtonState myState;

    /** The hidden. */
    private boolean myHidden;

    /** The Base icon to render icon map. */
    private final Map<ImageIcon, ImageIcon> myBaseIconToRenderIconMap = New.map();

    /** The Mix in color. */
    private Color myMixInColor;

    /**
     * Constructor.
     *
     * @param defaultIcon The icon name for the default icon.
     * @param rolloverIcon The icon name for the rollover icon.
     * @param pressedIcon The icon name for the pressed icon.
     * @param selectedIcon the selected icon
     */
    public QuadStateIconButton(ImageIcon defaultIcon, ImageIcon rolloverIcon, ImageIcon pressedIcon, ImageIcon selectedIcon)
    {
        myDefaultIcon = defaultIcon;
        myRolloverIcon = rolloverIcon;
        myPressedIcon = pressedIcon;
        mySelectedIcon = selectedIcon;
        myState = ButtonState.DEFAULT;
        myCurrentIcon = myDefaultIcon;
        setState(myState);
    }

    /**
     * Gets the mix in color.
     *
     * @return the mix in color
     */
    public Color getMixInColor()
    {
        return myMixInColor;
    }

    /**
     * Get the state of the button.
     *
     * @return The current state of the button.
     */
    public ButtonState getState()
    {
        return myState;
    }

    /**
     * Checks if is hidden.
     *
     * @return true, if is hidden
     */
    public boolean isHidden()
    {
        return myHidden;
    }

    /**
     * Sets the hidden.
     *
     * @param hidden the new hidden
     */
    public void setHidden(boolean hidden)
    {
        myHidden = hidden;
    }

    /**
     * Sets the mix in color, color that will be alpha composite into the
     * existing icon when drawn, or if null nothing will happen.
     *
     * @param mixInColor the new mix in color
     */
    public void setMixInColor(Color mixInColor)
    {
        myMixInColor = mixInColor;
    }

    /**
     * Set the state of the button.
     *
     * @param state The new state.
     */
    public final void setState(ButtonState state)
    {
        if (!myState.equals(state))
        {
            myState = state;
            switch (myState)
            {
                case DEFAULT:
                    myCurrentIcon = myDefaultIcon;
                    setIcon(myDefaultIcon);
                    break;
                case SELECTED:
                    myCurrentIcon = mySelectedIcon == null ? myDefaultIcon : mySelectedIcon;
                    break;
                case ROLLOVER:
                    myCurrentIcon = mySelectedIcon == null ? myRolloverIcon : isSelected() ? mySelectedIcon : myDefaultIcon;
                    break;
                case PRESSED:
                    myCurrentIcon = myPressedIcon;
                    break;
                default:
                    throw new UnexpectedEnumException(myState);
            }
            setIcon(myCurrentIcon);
        }
        if (myMixInColor != null)
        {
            setIcon(useMixInColor(myCurrentIcon, myMixInColor));
        }
        else
        {
            setIcon(myCurrentIcon);
        }
        setSize(18, 18);
        setPreferredSize(getSize());
    }

    /**
     * Mix color with icon.
     *
     * @param anIcon the an icon
     * @param typeColor the type color
     * @return the image icon
     */
    private ImageIcon useMixInColor(ImageIcon anIcon, Color typeColor)
    {
        ImageIcon mixIcon = myBaseIconToRenderIconMap.get(anIcon);
        if (mixIcon == null)
        {
            BufferedImage bi = new BufferedImage(anIcon.getIconWidth(), anIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            mixIcon = new ImageIcon();
            mixIcon.setImage(bi);
            myBaseIconToRenderIconMap.put(anIcon, mixIcon);
        }
        Graphics graphics = mixIcon.getImage().getGraphics();
        graphics.setColor(typeColor);
        ((Graphics2D)graphics).setComposite(AlphaComposite.SrcOver);
        graphics.fillRect(0, 0, mixIcon.getIconWidth(), mixIcon.getIconHeight());
        ((Graphics2D)graphics).setComposite(AlphaComposite.DstIn);
        graphics.drawImage(anIcon.getImage(), 0, 0, null);
        return mixIcon;
    }

    /** The current state of the button which determines which icon to use. */
    public enum ButtonState
    {
        /** The default state. */
        DEFAULT,

        /** The selected state. */
        SELECTED,

        /** The button is currently pressed. */
        PRESSED,

        /** The mouse is currently over the button. */
        ROLLOVER;
    }
}
