package io.opensphere.core.util.swing;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
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
     * The {@link Font} object in which the web-hosting-hub glyphs package is
     * contained.
     */
    public static final Font WEB_HOSTING_HUB_GLYPHS_FONT = getWebHostingHubGlyphFont();

    /**
     * The {@link Font} object in which the font-awesome package is contained.
     */
    public static final Font FONT_AWESOME_SOLID_FONT = getFontAwesomeSolidFont();

    /**
     * The {@link Font} object in which the font-awesome package is contained.
     */
    public static final Font FONT_AWESOME_REGULAR_FONT = getFontAwesomeRegularFont();

    /**
     * The {@link Font} object in which the font-awesome package is contained.
     */
    public static final Font FONT_AWESOME_BRANDS_FONT = getFontAwesomeBrandsFont();

    /**
     * The {@link Font} object in which the govicons package is contained.
     */
    public static final Font GOVICONS_FONT = getGovIconFont();

    /**
     * The {@link Font} object in which the military-rank-icons package is
     * contained.
     */
    public static final Font MILITARY_RANK_FONT = getMilitaryRankFont();

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
     * Creates the web-hosting-hub glyph {@link Font} object, loading all icons
     * contained within the package.
     *
     * @return the {@link Font} object containing the web-hosting-hub glyph
     *         icons.
     */
    public static Font getWebHostingHubGlyphFont()
    {
        return getFont("/fonts/webhostinghub-glyphs.ttf");
    }

    /**
     * Creates the font-awesome {@link Font} object, loading all icons contained
     * within the package.
     *
     * @return the {@link Font} object containing the font-awesome icons.
     */
    public static Font getFontAwesomeSolidFont()
    {
        return getFont("/fonts/fa-solid-900.ttf");
    }

    /**
     * Creates the font-awesome {@link Font} object, loading all icons contained
     * within the package.
     *
     * @return the {@link Font} object containing the font-awesome icons.
     */
    public static Font getFontAwesomeRegularFont()
    {
        return getFont("/fonts/fa-regular-400.ttf");
    }

    /**
     * Creates the font-awesome {@link Font} object, loading all icons contained
     * within the package.
     *
     * @return the {@link Font} object containing the font-awesome icons.
     */
    public static Font getFontAwesomeBrandsFont()
    {
        return getFont("/fonts/fa-brands-400.ttf");
    }

    /**
     * Creates the govicons {@link Font} object, loading all icons contained
     * within the package.
     *
     * @return the {@link Font} object containing the font-awesome icons.
     */
    public static Font getGovIconFont()
    {
        return getFont("/fonts/govicons-webfont.ttf");
    }

    /**
     * Creates the military-rank-icons {@link Font} object, loading all icons
     * contained within the package.
     *
     * @return the {@link Font} object containing the font-awesome icons.
     */
    public static Font getMilitaryRankFont()
    {
        return getFont("/fonts/military-rank-icons.ttf");
    }

    /**
     * Creates the {@link Font} object for the given font path, loading all
     * icons contained within the package.
     *
     * @param fontPath the path to the {@link Font} in resources.
     * @return the {@link Font} object containing the font-awesome icons.
     */
    private static Font getFont(String fontPath)
    {
        try (InputStream in = SwingUtilities.class.getResourceAsStream(fontPath))
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

    /**
     * Sets a combo box value only if the value is not null and is different
     * from the current value.
     *
     * @param <T> the type of the items
     * @param combo the combo box
     * @param value the value
     */
    public static <T> void setComboBoxValue(JComboBox<T> combo, Object value)
    {
        if (value != null && !value.equals(combo.getSelectedItem()))
        {
            combo.setSelectedItem(value);
        }
    }

    /**
     * Sets a text field value only if the value is different from the current
     * value.
     *
     * @param textField the text field
     * @param value the value
     */
    public static void setTextFieldValue(JTextField textField, String value)
    {
        if (!Objects.equals(textField.getText(), value))
        {
            textField.setText(value);
        }
    }

    /**
     * Sets a spinner value only if the value is not null and is different from
     * the current value.
     *
     * @param spinner the spinner
     * @param value the value
     */
    public static void setSpinnerValue(JSpinner spinner, Object value)
    {
        if (value != null && !value.equals(spinner.getValue()))
        {
            spinner.setValue(value);
        }
    }

    /**
     * Sets a slider value only if the value is different from the current
     * value.
     *
     * @param slider the slider
     * @param value the value
     */
    public static void setSliderValue(JSlider slider, int value)
    {
        if (slider.getValue() != value)
        {
            slider.setValue(value);
        }
    }

    /** Private constructor. */
    private SwingUtilities()
    {
    }
}
