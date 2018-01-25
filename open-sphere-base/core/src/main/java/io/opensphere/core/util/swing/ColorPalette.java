package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.apache.log4j.Logger;

/**
 * The Class ColorPalette.
 */
public final class ColorPalette extends AbstractHUDPanel
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ColorPalette.class);

    /** The White button. */
    private JButton myWhiteButton;

    /** The Red button. */
    private JButton myRedButton;

    /** The Orange button. */
    private JButton myOrangeButton;

    /** The Yellow button. */
    private JButton myYellowButton;

    /** The Black button. */
    private JButton myBlackButton;

    /** The Green button. */
    private JButton myGreenButton;

    /** The Blue button. */
    private JButton myBlueButton;

    /** The Gray button. */
    private JButton myGrayButton;

    /** The Violet button. */
    private JButton myVioletButton;

    /** The Button dim. */
    private final Dimension myButtonDim = new Dimension(10, 10);

    /** The pressed. */
    private ImageIcon myPressedIcon;

    /**
     * Instantiates a new color palette.
     */
    public ColorPalette()
    {
        super();
        initialize();
    }

    /**
     * Gets the black button.
     *
     * @return the black button
     */
    public JButton getBlackButton()
    {
        if (myBlackButton == null)
        {
            myBlackButton = getColorButton("black", 0x000000);
        }
        return myBlackButton;
    }

    /**
     * Gets the blue button.
     *
     * @return the blue button
     */
    public JButton getBlueButton()
    {
        if (myBlueButton == null)
        {
            myBlueButton = getColorButton("blue", 0x000fff);
        }
        return myBlueButton;
    }

    /**
     * Gets the gray button.
     *
     * @return the gray button
     */
    public JButton getGrayButton()
    {
        if (myGrayButton == null)
        {
            myGrayButton = getColorButton("gray", 0x9091a0);
        }
        return myGrayButton;
    }

    /**
     * Gets the green button.
     *
     * @return the green button
     */
    public JButton getGreenButton()
    {
        if (myGreenButton == null)
        {
            myGreenButton = getColorButton("green", 0x00ff00);
        }
        return myGreenButton;
    }

    /**
     * Gets the orange button.
     *
     * @return the orange button
     */
    public JButton getOrangeButton()
    {
        if (myOrangeButton == null)
        {
            myOrangeButton = getColorButton("orange", 0xffc800);
        }
        return myOrangeButton;
    }

    /**
     * Gets the red button.
     *
     * @return the red button
     */
    public JButton getRedButton()
    {
        if (myRedButton == null)
        {
            myRedButton = getColorButton("red", 0xff0000);
        }
        return myRedButton;
    }

    /**
     * Gets the violet button.
     *
     * @return the violet button
     */
    public JButton getVioletButton()
    {
        if (myVioletButton == null)
        {
            myVioletButton = getColorButton("violet", 0x7030a0);
        }
        return myVioletButton;
    }

    /**
     * Gets the white button.
     *
     * @return the white button
     */
    public JButton getWhiteButton()
    {
        if (myWhiteButton == null)
        {
            myWhiteButton = getColorButton("white", 0xffffff);
        }
        return myWhiteButton;
    }

    /**
     * Gets the yellow button.
     *
     * @return the yellow button
     */
    public JButton getYellowButton()
    {
        if (myYellowButton == null)
        {
            myYellowButton = getColorButton("yellow", 0xffff00);
        }
        return myYellowButton;
    }

    /**
     * Gets the color button.
     *
     * @param pColorStr the color str
     * @param pHexColor the hex color
     * @return the color button
     */
    private JButton getColorButton(String pColorStr, int pHexColor)
    {
        String imageStr = "/images/" + pColorStr + "Square.png";
        ImageIcon icon = null;
        try
        {
            if (myPressedIcon == null)
            {
                myPressedIcon = new ImageIcon(ImageIO.read(ColorPalette.class.getResource("/images/pressedSquare.png")));
            }
            icon = new ImageIcon(ImageIO.read(ColorPalette.class.getResource(imageStr)));
        }
        catch (IOException e)
        {
            LOGGER.error(getReadImageErrorMessage(), e);
        }
        JButton button = new JButton(icon);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        setComponentSize(button, myButtonDim);
        button.setPressedIcon(myPressedIcon);
        button.setBackground(new Color(pHexColor));
        return button;
    }

    /**
     * Initialize.
     */
    private void initialize()
    {
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(getWhiteButton(), gbc);

        gbc.gridx = 2;
        add(getRedButton(), gbc);

        gbc.gridx = 3;
        add(getOrangeButton(), gbc);

        gbc.gridx = 4;

        add(getYellowButton(), gbc);

        gbc.gridx = 5;
        add(getBlackButton(), gbc);

        gbc.gridx = 6;
        add(getGreenButton(), gbc);

        gbc.gridx = 7;
        add(getBlueButton(), gbc);

        gbc.gridx = 8;
        add(getGrayButton(), gbc);

        gbc.gridx = 9;
        add(getVioletButton(), gbc);
    }
}
