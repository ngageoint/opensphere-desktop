package io.opensphere.core.util.swing;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

/** Swing Utilities. */
public final class SwingUtilities
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(SwingUtilities.class);

    /**
     * The {@link Font} object in which the font-awesome package is contained.
     */
    public static final Font FONT_AWESOME_FONT = getFontAwesomeFont();

    /**
     * Creates a new menu item.
     *
     * @param text the text
     * @param listener the listener
     * @return the menu item
     */
    public static JMenuItem newMenuItem(String text, ActionListener listener)
    {
        return newMenuItem(text, listener, null);
    }

    /**
     * Creates a new menu item.
     *
     * @param text the text
     * @param listener the listener
     * @param accelerator the optional accelerator
     * @return the menu item
     */
    public static JMenuItem newMenuItem(String text, ActionListener listener, KeyStroke accelerator)
    {
        return newMenuItem(text, null, listener, accelerator);
    }

    /**
     * Creates a new menu item using the supplied icon and text. If the icon is
     * null, only the text is used.
     *
     * @param text the text to place in the menu item.
     * @param icon the (optional) icon to place in the menu item, may be null,
     *            causing the new menu item to only use text.
     * @param listener the listener to receive events.
     * @return a new menu item.
     */
    public static JMenuItem newMenuItem(String text, Icon icon, ActionListener listener)
    {
        return newMenuItem(text, icon, listener, null);
    }

    /**
     * Creates a new menu item using the supplied icon and text. If the icon is
     * null, only the text is used.
     *
     * @param text the text to place in the menu item.
     * @param icon the (optional) icon to place in the menu item, may be null,
     *            causing the new menu item to only use text.
     * @param listener the listener to receive events.
     * @param accelerator the optional accelerator
     * @return a new menu item.
     */
    public static JMenuItem newMenuItem(String text, Icon icon, ActionListener listener, KeyStroke accelerator)
    {
        JMenuItem item;
        if (icon == null)
        {
            item = new JMenuItem(text);
        }
        else
        {
            item = new JMenuItem(text, icon);
        }
        item.addActionListener(listener);
        if (accelerator != null)
        {
            item.setAccelerator(accelerator);
        }
        return item;
    }

    /**
     * Creates the font-awesome {@link Font} object, loading all icons contained
     * within the package.
     *
     * @return the {@link Font} object containing the font-awesome icons.
     */
    public static Font getFontAwesomeFont()
    {
        try (InputStream in = SwingUtilities.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf"))
        {
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, in);
            Font awesomeFont = baseFont.deriveFont(Font.BOLD, 24);
            return awesomeFont;
        }
        catch (IOException | FontFormatException e)
        {
            LOG.error("Unable to load font-awesome package.", e);
        }
        return null;
    }

    /** Private constructor. */
    private SwingUtilities()
    {
    }
}
