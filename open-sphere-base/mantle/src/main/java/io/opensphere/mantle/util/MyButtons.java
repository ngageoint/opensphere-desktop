package io.opensphere.mantle.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * The Class MyButtons.
 */
public final class MyButtons
{
    /** The Constant BUTTONX. */
    private static final int BUTTONX = 20;

    /** The Constant BUTTONY. */
    private static final int BUTTONY = 22;

    /** The Constant PLUS_FOREGROUND_COLOR. */
    private static final Color PLUS_FOREGROUND_COLOR = new Color(0x00DD00);

    /** The Constant MINUS_FOREGROUND_COLOR. */
    private static final Color MINUS_FOREGROUND_COLOR = new Color(0xDD0000);

    /** The Constant PLUS_TEXT. */
    private static final String PLUS_TEXT = "<HTML><FONT SIZE=+0><B>+</B></FONT></HTML>";

    /** The Constant MINUS_TEXT. */
    private static final String MINUS_TEXT = "<HTML><FONT SIZE=+0><B>-</B></FONT></HTML>";

    /** The Constant UP_ARROW_TEXT. */
    private static final String UP_ARROW_TEXT = "<HTML><FONT SIZE=+0><B>^</B></FONT></HTML>";

    /** The Constant DN_ARROW_TEXT. */
    private static final String DN_ARROW_TEXT = "<HTML><FONT SIZE=+0><B>V</B></FONT></HTML>";

    /** The Constant BUTTON_MARGINS. */
    private static final Insets BUTTON_MARGINS = new Insets(1, 3, 1, 3);

    // In the event that the HTML rendered buttons don't look correct for a
    // particular
    // look and feel we can use the following images instead. Set
    // useImagesNotHTML to true
    // to use the images, and to false to use the HTML version.
    /** The Constant useImagesNotHTML. */
    private static final boolean UseImagesNotHTML = true;

    /** The minus_red_url. */
    private static final URL MINUS_RED_URL = MyButtons.class.getClassLoader().getResource("images/minus_red.png");

    /** The plus_green_ url. */
    private static final URL PLUS_GREEN_URL = MyButtons.class.getClassLoader().getResource("images/plus_green.png");

    /** The v_green_url. */
    private static final URL V_GREEN_URL = MyButtons.class.getClassLoader().getResource("images/v_green.png");

    /** The up_v_green_url. */
    private static final URL UP_V_GREEN_URL = MyButtons.class.getClassLoader().getResource("images/up_v_green.png");

    /** The minus_red_icon. */
    private static final ImageIcon MINUS_RED_ICON = new ImageIcon(MINUS_RED_URL);

    /** The plus_green_icon. */
    private static final ImageIcon PLUS_GREEN_ICON = new ImageIcon(PLUS_GREEN_URL);

    /** The v_green_icon. */
    private static final ImageIcon V_GREEN_ICON = new ImageIcon(V_GREEN_URL);

    /** The up_v_green_icon. */
    private static final ImageIcon UP_V_GREEN_ICON = new ImageIcon(UP_V_GREEN_URL);

    /**
     * Creates the dn arrow button.
     *
     * @return the j button
     */
    public static JButton createDnArrowButton()
    {
        JButton mb = null;
        mb = UseImagesNotHTML ? new JButton(V_GREEN_ICON) : new JButton(DN_ARROW_TEXT);
        mb.setMargin(BUTTON_MARGINS);
        mb.setSize(BUTTONX, BUTTONY);
        mb.setPreferredSize(mb.getSize());
        mb.setHorizontalAlignment(SwingConstants.CENTER);
        mb.setVerticalAlignment(SwingConstants.CENTER);
        mb.setHorizontalTextPosition(SwingConstants.CENTER);
        mb.setVerticalTextPosition(SwingConstants.CENTER);
        mb.setForeground(PLUS_FOREGROUND_COLOR);
        mb.setFocusPainted(false);
        return mb;
    }

    /**
     * Creates the i button.
     *
     * @return the j button
     */
    public static JButton createIButton()
    {
        JButton mb = null;
        mb = new JButton("i");
        mb.setFont(mb.getFont().deriveFont(Font.BOLD, 16));
        mb.setMargin(BUTTON_MARGINS);
        mb.setSize(BUTTONX, BUTTONY);
        mb.setPreferredSize(mb.getSize());
        mb.setHorizontalAlignment(SwingConstants.CENTER);
        mb.setVerticalAlignment(SwingConstants.CENTER);
        mb.setHorizontalTextPosition(SwingConstants.CENTER);
        mb.setVerticalTextPosition(SwingConstants.CENTER);
        mb.setForeground(Color.white);
        mb.setFocusPainted(false);
        return mb;
    }

    /**
     * Creates the minus button.
     *
     * @return the j button
     */
    public static JButton createMinusButton()
    {
        JButton mb = null;
        mb = UseImagesNotHTML ? new JButton(MINUS_RED_ICON) : new JButton(MINUS_TEXT);
        mb.setMargin(BUTTON_MARGINS);
        mb.setSize(BUTTONX, BUTTONY);
        mb.setPreferredSize(mb.getSize());
        mb.setHorizontalAlignment(SwingConstants.CENTER);
        mb.setVerticalAlignment(SwingConstants.CENTER);
        mb.setHorizontalTextPosition(SwingConstants.CENTER);
        mb.setVerticalTextPosition(SwingConstants.CENTER);
        mb.setForeground(MINUS_FOREGROUND_COLOR);
        mb.setFocusPainted(false);
        return mb;
    }

    /**
     * Creates the plus button.
     *
     * @return the j button
     */
    public static JButton createPlusButton()
    {
        JButton pb = null;
        pb = UseImagesNotHTML ? new JButton(PLUS_GREEN_ICON) : new JButton(PLUS_TEXT);
        pb.setMargin(BUTTON_MARGINS);
        pb.setSize(BUTTONX, BUTTONY);
        pb.setPreferredSize(pb.getSize());
        pb.setHorizontalAlignment(SwingConstants.CENTER);
        pb.setVerticalAlignment(SwingConstants.CENTER);
        pb.setHorizontalTextPosition(SwingConstants.CENTER);
        pb.setVerticalTextPosition(SwingConstants.CENTER);
        pb.setForeground(PLUS_FOREGROUND_COLOR);
        pb.setFocusPainted(false);
        return pb;
    }

    /**
     * Creates the up arrow button.
     *
     * @return the j button
     */
    public static JButton createUpArrowButton()
    {
        JButton mb = null;
        mb = UseImagesNotHTML ? new JButton(UP_V_GREEN_ICON) : new JButton(UP_ARROW_TEXT);
        mb.setMargin(BUTTON_MARGINS);
        mb.setSize(BUTTONX, BUTTONY);
        mb.setPreferredSize(mb.getSize());
        mb.setHorizontalAlignment(SwingConstants.CENTER);
        mb.setVerticalAlignment(SwingConstants.CENTER);
        mb.setHorizontalTextPosition(SwingConstants.CENTER);
        mb.setVerticalTextPosition(SwingConstants.CENTER);
        mb.setForeground(PLUS_FOREGROUND_COLOR);
        mb.setFocusPainted(false);
        return mb;
    }

    /**
     * Disallow instantiation.
     */
    private MyButtons()
    {
    }
}
