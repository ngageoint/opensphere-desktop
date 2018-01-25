package io.opensphere.core.util.swing;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JButton;

/**
 * A button that looks like a link.
 */
public class LinkButton extends JButton
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param text the string used to set the text
     */
    public LinkButton(String text)
    {
        super(text);
        @SuppressWarnings("unchecked")
        Map<TextAttribute, Object> attr = (Map<TextAttribute, Object>)getFont().getAttributes();
        attr.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        Font newFont = getFont().deriveFont(attr);
        setFont(newFont);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(null);
        Insets i = getMargin();
        setMargin(new Insets(i.top, 0, i.bottom, 0));
        setListener();
    }

    @Override
    public void setText(String text)
    {
        super.setText(text);
        setToolTipText(text);
    }

    /**
     * Sets the listener.
     */
    private void setListener()
    {
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }
}
