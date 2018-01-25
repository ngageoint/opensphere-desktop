package io.opensphere.core.dialog.alertviewer.event;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.core.util.Utilities;

/**
 * The Class UserMessageEvent.
 */
@Immutable
public class UserMessageEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The exception. */
    private final Throwable myException;

    /** The make visible. */
    private final boolean myMakeVisible;

    /** The message. */
    private final String myMessage;

    /**
     * True if a toast dialog should appear showing the message.
     */
    private final boolean myShowToast;

    /** The source. */
    private final Object mySource;

    /** The type. */
    private final Type myType;

    /**
     * Sends an ERROR message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     */
    public static void error(EventManager em, String msg)
    {
        error(em, msg, false);
    }

    /**
     * Sends an ERROR message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     */
    public static void error(EventManager em, String msg, boolean makeVisible)
    {
        error(em, msg, makeVisible, null, null, false);
    }

    /**
     * Sends an ERROR message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible True if the alert dialog should be made visible.
     * @param showToast True if a toast dialog should appear showing the
     *            message.
     */
    public static void error(EventManager em, String msg, boolean makeVisible, boolean showToast)
    {
        error(em, msg, makeVisible, null, null, showToast);
    }

    /**
     * Sends an ERROR message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param src - the source of the message ( may be null )
     * @param e - any associated exception ( may be null )
     */
    public static void error(EventManager em, String msg, boolean makeVisible, Object src, Exception e)
    {
        message(em, Type.ERROR, msg, makeVisible, src, e, false);
    }

    /**
     * Sends an ERROR message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param src - the source of the message ( may be null )
     * @param e - any associated exception ( may be null )
     * @param showToast True if a toast dialog should appear showing the
     *            message.
     */
    public static void error(EventManager em, String msg, boolean makeVisible, Object src, Throwable e, boolean showToast)
    {
        message(em, Type.ERROR, msg, makeVisible, src, e, showToast);
    }

    /**
     * Sends an INFO message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     */
    public static void info(EventManager em, String msg)
    {
        info(em, msg, false);
    }

    /**
     * Sends an INFO message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     */
    public static void info(EventManager em, String msg, boolean makeVisible)
    {
        info(em, msg, makeVisible, null);
    }

    /**
     * Sends an INFO message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     */
    public static void info(EventManager em, String msg, boolean makeVisible, Object source)
    {
        message(em, Type.INFO, msg, makeVisible, source, null, false);
    }

    /**
     * Sends an INFO message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     * @param showToast - True if this message should be displayed as a toast
     *            pop up message.
     */
    public static void info(EventManager em, String msg, boolean makeVisible, Object source, boolean showToast)
    {
        message(em, Type.INFO, msg, makeVisible, source, null, showToast);
    }

    /**
     * Sends a message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param type - the {@link Type} of message to add
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     * @param e - any exception associated with the message ( may be null )
     * @param showToast True if a toast dialog should appear showing the
     *            message.
     */
    public static void message(EventManager em, final Type type, final String msg, final boolean makeVisible, final Object source,
            final Throwable e, final boolean showToast)
    {
        Utilities.checkNull(em, "em").publishEvent(new UserMessageEvent(type, msg, makeVisible, source, e, showToast));
    }

    /**
     * Sends an WARNING message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     */
    public static void warn(EventManager em, String msg)
    {
        warn(em, msg, false);
    }

    /**
     * Sends an WARNING message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     */
    public static void warn(EventManager em, String msg, boolean makeVisible)
    {
        warn(em, msg, makeVisible, null);
    }

    /**
     * Sends an WARNING message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param src - the source of the message ( may be null )
     */
    public static void warn(EventManager em, String msg, boolean makeVisible, Object src)
    {
        warn(em, msg, makeVisible, src, null);
    }

    /**
     * Sends an WARNING message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     * @param e - any associated Throwable ( may be null )
     */
    public static void warn(EventManager em, String msg, boolean makeVisible, Object source, Throwable e)
    {
        message(em, Type.WARNING, msg, makeVisible, source, e, false);
    }

    /**
     * Sends an WARNING message to the UserMessageService.
     *
     * @param em the {@link EventManager}
     * @param msg - the message text
     * @param makeVisible - true to force the display to be visible and to
     *            auto-scroll to the message
     * @param source - the source of the message ( may be null )
     * @param e - any associated Throwable ( may be null )
     * @param showToast - True if the message should be displayed in a toaster
     *            popup.
     */
    public static void warn(EventManager em, String msg, boolean makeVisible, Object source, Throwable e, boolean showToast)
    {
        message(em, Type.WARNING, msg, makeVisible, source, e, showToast);
    }

    /**
     * Instantiates a new user message event.
     *
     * @param type the type
     * @param msg the msg
     * @param makeVisible the make visible
     * @param source the source
     * @param e the e
     * @param showToast True if a toast dialog should appear showing the
     *            message.
     */
    public UserMessageEvent(Type type, String msg, boolean makeVisible, Object source, Throwable e, boolean showToast)
    {
        myType = type;
        myMessage = msg;
        myMakeVisible = makeVisible;
        mySource = source;
        myException = e;
        myShowToast = showToast;
    }

    @Override
    public String getDescription()
    {
        return "User Message Event";
    }

    /**
     * Gets the exception if it is an error or warning, the cause that
     * precipitated the message.
     *
     * @return the {@link Exception}
     */
    public Throwable getException()
    {
        return myException;
    }

    /**
     * Gets the message for the user.
     *
     * @return the message for the user.
     */
    public String getMessage()
    {
        return myMessage;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Gets the message type.
     *
     * @return the {@link Type}
     */
    public Type getType()
    {
        return myType;
    }

    /**
     * Checks if the user message display should be made visible.
     *
     * @return true, if requested to make display visible.
     */
    public boolean isMakeVisible()
    {
        return myMakeVisible;
    }

    /**
     * True if a toast dialog should appear showing the message.
     *
     * @return If a toast message should be shown.
     */
    public boolean isShowToast()
    {
        return myShowToast;
    }
}
