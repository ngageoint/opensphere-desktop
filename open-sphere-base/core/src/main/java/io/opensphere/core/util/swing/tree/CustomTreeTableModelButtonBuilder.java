package io.opensphere.core.util.swing.tree;

import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * The Class CustomTreeTableModelButtonBuilder.
 */
public class CustomTreeTableModelButtonBuilder
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CustomTreeTableModelButtonBuilder.class);

    /** The button name. */
    private final String myButtonName;

    /** The action command. */
    private final String myActionCommand;

    /** The action listener. */
    private final ActionListener myActionListener;

    /** The default icon. */
    private final ImageIcon myDefaultIcon;

    /** The rollover icon. */
    private final ImageIcon myRolloverIcon;

    /** The pressed icon. */
    private final ImageIcon myPressedIcon;

    /** The selected icon. */
    private final ImageIcon mySelectedIcon;

    /** The updater. */
    private ButtonStateUpdater myUpdater;

    /**
     * Instantiates a new custom tree table model button builder.
     *
     * @param buttonName the button name
     * @param actionCommand the action command
     * @param listener the listener
     * @param defaultIcon the default icon
     * @param rollOverIcon the roll over icon
     * @param pressedIcon the pressed icon
     * @param selectedIcon the selected icon
     */
    public CustomTreeTableModelButtonBuilder(String buttonName, String actionCommand, ActionListener listener,
            ImageIcon defaultIcon, ImageIcon rollOverIcon, ImageIcon pressedIcon, ImageIcon selectedIcon)
    {
        myButtonName = buttonName;
        myActionCommand = actionCommand;
        myActionListener = listener;
        myDefaultIcon = defaultIcon;
        myRolloverIcon = rollOverIcon;
        myPressedIcon = pressedIcon;
        mySelectedIcon = selectedIcon;
    }

    /**
     * Instantiates a new custom tree table model button builder.
     *
     * @param buttonName the button name
     * @param actionCommand the action command
     * @param listener the listener
     * @param defaultIcon the default icon
     * @param rollOverIcon the roll over icon
     * @param pressedIcon the pressed icon
     * @param selectedIcon the selected icon
     */
    public CustomTreeTableModelButtonBuilder(String buttonName, String actionCommand, ActionListener listener, String defaultIcon,
            String rollOverIcon, String pressedIcon, String selectedIcon)
    {
        myButtonName = buttonName;
        myActionCommand = actionCommand;
        myActionListener = listener;
        ImageIcon defaultIc = null;
        ImageIcon rolloverIc = null;
        ImageIcon pressedIc = null;
        ImageIcon selectedIc = null;
        try
        {
            defaultIc = new ImageIcon(
                    ImageIO.read(CustomTreeTableModelButtonBuilder.class.getResource("/images/" + defaultIcon)));
            rolloverIc = new ImageIcon(
                    ImageIO.read(CustomTreeTableModelButtonBuilder.class.getResource("/images/" + rollOverIcon)));
            pressedIc = new ImageIcon(
                    ImageIO.read(CustomTreeTableModelButtonBuilder.class.getResource("/images/" + pressedIcon)));
            selectedIc = StringUtils.isBlank(selectedIcon) ? null
                    : new ImageIcon(ImageIO.read(CustomTreeTableModelButtonBuilder.class.getResource("/images/" + selectedIcon)));
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to load image for icon." + e, e);
        }
        myDefaultIcon = defaultIc;
        myRolloverIcon = rolloverIc;
        myPressedIcon = pressedIc;
        mySelectedIcon = selectedIc;
    }

    /**
     * Gets the action command.
     *
     * @return the action command
     */
    public String getActionCommand()
    {
        return myActionCommand;
    }

    /**
     * Gets the action listener.
     *
     * @return the action listener
     */
    public ActionListener getActionListener()
    {
        return myActionListener;
    }

    /**
     * Gets the button name.
     *
     * @return the button name
     */
    public String getButtonName()
    {
        return myButtonName;
    }

    /**
     * Gets the default icon.
     *
     * @return the default icon
     */
    public ImageIcon getDefaultIcon()
    {
        return myDefaultIcon;
    }

    /**
     * Gets the pressed icon.
     *
     * @return the pressed icon
     */
    public ImageIcon getPressedIcon()
    {
        return myPressedIcon;
    }

    /**
     * Gets the rollover icon.
     *
     * @return the rollover icon
     */
    public ImageIcon getRolloverIcon()
    {
        return myRolloverIcon;
    }

    /**
     * Gets the selected icon.
     *
     * @return the selected icon
     */
    public ImageIcon getSelectedIcon()
    {
        return mySelectedIcon;
    }

    /**
     * Gets the updater.
     *
     * @return the updater
     */
    public ButtonStateUpdater getUpdater()
    {
        return myUpdater;
    }

    /**
     * Sets the updater.
     *
     * @param updater the new updater
     */
    public void setUpdater(ButtonStateUpdater updater)
    {
        myUpdater = updater;
    }
}
