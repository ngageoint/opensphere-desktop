package io.opensphere.core.util.swing;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * A simple button that functions as a label, but displays additional
 * information when clicked.
 */
public class InfoPopupLabel extends JButton
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The font to use when the popup is disabled. */
    private Font myDisabledFont;

    /**
     * The font to use as a visual cue to let the user know that the popup is
     * enabled.
     */
    private Font myEnabledFont;

    /** The text which will appear in the info popup. */
    private String myInfoText;

    /**
     * The action listener for showing the info popup. This may be null when the
     * popup is disabled.
     */
    private transient ActionListener myPopupAction;

    /** Constructor. */
    public InfoPopupLabel()
    {
        setBorder(null);
        setContentAreaFilled(false);
        if (getFont() != null)
        {
            createEnabledFont(getFont());
        }
    }

    /** Turn the popup off. */
    public void disableInfoPopup()
    {
        if (myPopupAction != null)
        {
            removeActionListener(myPopupAction);
            myPopupAction = null;
            super.setFont(myDisabledFont);
        }
    }

    /** Turn the popup on. */
    public void enableInfoPopup()
    {
        if (myPopupAction == null)
        {
            myPopupAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    JOptionPane.showOptionDialog(InfoPopupLabel.this, myInfoText, "", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE, null, null, null);
                }
            };
            addActionListener(myPopupAction);
            super.setFont(myEnabledFont);
        }
    }

    /**
     * Gets the info text.
     *
     * @return the info text
     */
    public String getInfoText()
    {
        return myInfoText;
    }

    @Override
    public void setFont(Font font)
    {
        myDisabledFont = font;
        createEnabledFont(font);

        if (myPopupAction == null)
        {
            super.setFont(myDisabledFont);
        }
        else
        {
            super.setFont(myEnabledFont);
        }
    }

    /**
     * Set the infoText.
     *
     * @param infoText the infoText to set
     */
    public void setInfoText(String infoText)
    {
        myInfoText = infoText;
    }

    /**
     * Create the font to use as a visual cue to let the user know that the
     * popup is enabled.
     *
     * @param font The base font from which the enabled font is derived.
     */
    private void createEnabledFont(Font font)
    {
        @SuppressWarnings("unchecked")
        Map<TextAttribute, Object> attr = (Map<TextAttribute, Object>)font.getAttributes();
        attr.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        myEnabledFont = font.deriveFont(attr);
    }
}
