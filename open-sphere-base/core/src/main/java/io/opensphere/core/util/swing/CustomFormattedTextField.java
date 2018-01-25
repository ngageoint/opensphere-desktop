package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.text.SimpleDateFormat;

import javax.swing.JFormattedTextField;

/**
 * The Class CustomFormattedTextField.
 */
public class CustomFormattedTextField extends JFormattedTextField
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -4150475460753626994L;

    /**
     * A flag used to show or hide text.
     */
    private boolean myShowText;

    /**
     * Instantiates a new custom formatted text field.
     */
    public CustomFormattedTextField()
    {
        super();
    }

    /**
     * Instantiates a new custom formatted text field.
     *
     * @param simpleDateFormat the simple date format
     */
    public CustomFormattedTextField(SimpleDateFormat simpleDateFormat)
    {
        super(simpleDateFormat);
    }

    /**
     * Disable and show text.
     */
    public void disableAndShowText()
    {
        super.setEnabled(false);
        myShowText = true;
        repaint();
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        myShowText = false;
        super.setEnabled(enabled);
        repaint();
    }

    /**
     * {@inheritDoc}
     *
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (myShowText)
        {
            g.setColor(Color.GRAY.brighter());
            g.drawString(getText(), 6, getHeight() - 6);
        }
    }
}
