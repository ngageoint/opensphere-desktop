package io.opensphere.core.util.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.Colors;

/**
 * Helper to setup a common look and feel for icon buttons.
 */
public class IconButtonHelper
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(IconButtonHelper.class);

    /** The default size. */
    private static final Dimension SIZE = new Dimension(22, 22);

    /** The button being helped. */
    private final AbstractButton myButton;

    /**
     * Gets an Icon for the given URL.
     *
     * @param iconUrl the icon URL.
     * @return the Icon
     */
    private static Icon getIcon(String iconUrl)
    {
        Icon icon = null;
        try
        {
            icon = iconUrl == null ? null : new ImageIcon(ImageIO.read(IconButtonHelper.class.getResource(iconUrl)));
        }
        catch (IOException e)
        {
            LOGGER.error(e.getMessage());
        }
        return icon;
    }

    /**
     * Constructor.
     *
     * @param button the button being helped
     */
    public IconButtonHelper(AbstractButton button)
    {
        myButton = button;
        myButton.setFocusPainted(false);
        sizeToFitInternal();
    }

    /**
     * Handles addNotify().
     */
    public void handleAddNotify()
    {
        if (isInHUD())
        {
            myButton.setBackground(Colors.LF_SECONDARY3);
        }
    }

    /**
     * Sets the default icon.
     *
     * @param defaultIcon the default icon
     */
    public void setIcon(String defaultIcon)
    {
        myButton.setIcon(getIcon(defaultIcon));
    }

    /**
     * Sets the pressed icon.
     *
     * @param pressedIcon the pressed icon
     */
    public void setPressedIcon(String pressedIcon)
    {
        myButton.setPressedIcon(getIcon(pressedIcon));
    }

    /**
     * Sets the rollover icon.
     *
     * @param rolloverIcon the rollover icon
     */
    public void setRolloverIcon(String rolloverIcon)
    {
        myButton.setRolloverIcon(getIcon(rolloverIcon));
    }

    /**
     * Sets the selected icon.
     *
     * @param selectedIcon the selected icon
     */
    public void setSelectedIcon(String selectedIcon)
    {
        myButton.setSelectedIcon(getIcon(selectedIcon));
    }

    /**
     * Sizes the button to fit the text and/or icon.
     */
    public void sizeToFit()
    {
        sizeToFitInternal();
    }

    /**
     * Determines if the button is in a HUD.
     *
     * @return Whether it's in a HUD
     */
    private boolean isInHUD()
    {
        boolean inHUD = false;
        Container parent = myButton;
        while (parent.getParent() != null)
        {
            parent = parent.getParent();
            if (parent instanceof AbstractHUDPanel)
            {
                inHUD = true;
                break;
            }
        }
        return inHUD;
    }

    /**
     * Sizes the button to fit the text and/or icon.
     */
    private void sizeToFitInternal()
    {
        boolean hasText = StringUtils.isNotEmpty(myButton.getText());
        if (hasText)
        {
            myButton.setMargin(new Insets(0, 6, 0, 6));
        }
        else
        {
            myButton.setMargin(new Insets(0, 2, 0, 2));
        }

        myButton.setPreferredSize(null);
        Dimension size = new Dimension(Math.max(myButton.getPreferredSize().width, SIZE.width), SIZE.height);
        myButton.setPreferredSize(size);
        myButton.setMinimumSize(size);
        myButton.setMaximumSize(size);
    }
}
