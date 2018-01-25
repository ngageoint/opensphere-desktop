package io.opensphere.core;

import io.opensphere.core.dialog.alertviewer.event.Type;

/**
 * The UserMessageManager. Responsible for showing user messages to the user in
 * the UserMessageDialog. Info/Error/Warning types. All Messages are also logged
 * at the requested level.
 */
public interface UserMessageService
{
    /**
     * Adds a message to the display.
     *
     * @param type - the {@link Type} of message to add
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     * @param e - any exception associated with the message ( may be null )
     * @param showToast True if a toast message should be shown, false otherwise
     */
    void addMessage(final Type type, final String msg, final boolean makeVisible, final Object source, final Throwable e,
            boolean showToast);

    /**
     * Clears all messages from the display.
     */
    void clearMessages();

    /**
     * Adds an ERROR message to the display.
     *
     * @param msg - the message text
     */
    void error(String msg);

    /**
     * Adds an ERROR message to the display.
     *
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     */
    void error(String msg, boolean makeVisible);

    /**
     * Adds an ERROR message to the display.
     *
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param src - the source of the message ( may be null )
     * @param e - any associated exception ( may be null )
     */
    void error(String msg, boolean makeVisible, Object src, Exception e);

    /**
     * Forces the display to be invisible.
     */
    void hide();

    /**
     * Adds an INFO message to the display.
     *
     * @param msg - the message text
     */
    void info(String msg);

    /**
     * Adds an INFO message to the display.
     *
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     */
    void info(String msg, boolean makeVisible);

    /**
     * Adds an INFO message to the display.
     *
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     */
    void info(String msg, boolean makeVisible, Object source);

    /**
     * Checks if the user message dialog is visible or not.
     *
     * @return true, if is visible
     */
    boolean isVisible();

    /**
     * Adds a message to the display.
     *
     * @param type - the {@link Type} of message to add
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     * @param e - any exception associated with the message ( may be null )
     */
    void message(final Type type, final String msg, final boolean makeVisible, final Object source, final Exception e);

    /**
     * Forces the display to be visible.
     */
    void show();

    /**
     * Adds an WARNING message to the display.
     *
     * @param msg - the message text
     */
    void warn(String msg);

    /**
     * Adds an WARNING message to the display.
     *
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     */
    void warn(String msg, boolean makeVisible);

    /**
     * Adds an WARNING message to the display.
     *
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param src - the source of the message ( may be null )
     */
    void warn(String msg, boolean makeVisible, Object src);

    /**
     * Adds an WARNING message to the display.
     *
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     * @param e - any associated exception ( may be null )
     */
    void warn(String msg, boolean makeVisible, Object source, Exception e);
}
