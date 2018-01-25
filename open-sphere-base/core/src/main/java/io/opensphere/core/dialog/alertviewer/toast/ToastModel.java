package io.opensphere.core.dialog.alertviewer.toast;

import java.awt.Color;

/**
 * The model for the toast view.
 *
 */
class ToastModel
{
    /**
     * The message to display to the user.
     */
    private String myMessage;

    /**
     * The type of message (e.g. error, warn, info).
     */
    private Color myColor;

    /**
     * Gets the message color.
     *
     * @return The message color.
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * The message to display to the user.
     *
     * @return The message.
     */
    public String getMessage()
    {
        return myMessage;
    }

    /**
     * Sets the message color.
     *
     * @param color The message color.
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Sets the message to display to the user.
     *
     * @param message The message.
     */
    public void setMessage(String message)
    {
        myMessage = message;
    }
}
