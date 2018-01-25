package io.opensphere.core;

import javax.swing.JOptionPane;

import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Notifies the user of things in various ways. */
public final class Notify
{
    /** The toolbox. */
    private static volatile Toolbox ourToolbox;

    /** The default methods. */
    private static final Method[] DEFAULT_METHODS = new Method[] { Method.TOAST };

    /**
     * Sets the toolbox.
     *
     * @param toolbox the toolbox
     */
    public static void setToolbox(Toolbox toolbox)
    {
        ourToolbox = toolbox;
    }

    /**
     * Notifies the user of an informational message.
     *
     * @param message the message
     * @param methods the notification methods
     */
    public static void info(String message, Method... methods)
    {
        notify(Type.INFO, message, methods);
    }

    /**
     * Notifies the user of a warning.
     *
     * @param message the message
     * @param methods the notification methods
     */
    public static void warn(String message, Method... methods)
    {
        notify(Type.WARNING, message, methods);
    }

    /**
     * Notifies the user of an error.
     *
     * @param message the message
     * @param methods the notification methods
     */
    public static void error(String message, Method... methods)
    {
        notify(Type.ERROR, message, methods);
    }

    /**
     * Gets the JOptionPane message type for the given type.
     *
     * @param type the type
     * @return the JOptionPane message type
     */
    public static int getMessageType(Type type)
    {
        return type == Type.ERROR ? JOptionPane.ERROR_MESSAGE
                : type == Type.WARNING ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
    }

    /**
     * Notifies the user of an error.
     *
     * @param type the message type
     * @param message the message
     * @param methods the notification methods
     */
    private static void notify(Type type, String message, Method... methods)
    {
        Method[] methodsToUse = methods.length > 0 ? methods : DEFAULT_METHODS;
        for (Method method : methodsToUse)
        {
            method.notify(type, message);
        }
    }

    /** The notification method. */
    public enum Method
    {
        /** Adds an alert to the alert dialog, but doesn't show it. */
        ALERT_HIDDEN
        {
            @Override
            void notify(Type type, String message)
            {
                if (ourToolbox != null)
                {
                    UserMessageEvent.message(ourToolbox.getEventManager(), type, message, false, null, null, false);
                }
            }
        },

        /** Displays an alert in the alert dialog. */
        ALERT
        {
            @Override
            void notify(Type type, String message)
            {
                if (ourToolbox != null)
                {
                    UserMessageEvent.message(ourToolbox.getEventManager(), type, message, true, null, null, false);
                }
            }
        },

        /** Displays a toast message. */
        TOAST
        {
            @Override
            void notify(Type type, String message)
            {
                if (ourToolbox != null)
                {
                    UserMessageEvent.message(ourToolbox.getEventManager(), type, message, false, null, null, true);
                }
            }
        },

        /** Displays a popup message. */
        POPUP
        {
            @Override
            void notify(final Type type, final String message)
            {
                if (ourToolbox != null)
                {
                    EventQueueUtilities.runOnEDT(() ->
                    {
                        String title = type == Type.ERROR ? "Error" : type == Type.WARNING ? "Warning" : "Information";
                        JOptionPane.showMessageDialog(ourToolbox.getUIRegistry().getMainFrameProvider().get(), message, title,
                                getMessageType(type));
                    });
                }
            }
        };

        /**
         * Performs the notification.
         *
         * @param type the type
         * @param message the message
         */
        abstract void notify(Type type, String message);
    }

    /** Private constructor. */
    private Notify()
    {
    }
}
