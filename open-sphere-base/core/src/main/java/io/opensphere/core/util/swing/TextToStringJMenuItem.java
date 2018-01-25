package io.opensphere.core.util.swing;

import javax.swing.JMenuItem;

/**
 * This JMenuItem can be used as an item in a JCombobox. Overriding the toString
 * will display the menuitem's text in the combo box drop down.
 */
public class TextToStringJMenuItem extends JMenuItem
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new text to string j menu item.
     *
     * @param text the text
     */
    public TextToStringJMenuItem(String text)
    {
        super(text);
    }

    @Override
    public String toString()
    {
        return getText();
    }
}
