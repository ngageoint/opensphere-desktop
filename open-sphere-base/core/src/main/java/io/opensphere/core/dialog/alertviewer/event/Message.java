package io.opensphere.core.dialog.alertviewer.event;

/** A message/severity. */
public class Message
{
    /** The message. */
    private final String myMessage;

    /** The severity. */
    private final Type mySeverity;

    /** The title. */
    private final String myTitle;

    /**
     * Constructor.
     *
     * @param message The message.
     * @param severity The severity
     */
    public Message(String message, Type severity)
    {
        this(message, severity, getDefaultTitle(severity));
    }

    /**
     * Constructor.
     *
     * @param message The message.
     * @param severity The severity
     * @param title The title
     */
    public Message(String message, Type severity, String title)
    {
        myMessage = message;
        mySeverity = severity;
        myTitle = title;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage()
    {
        return myMessage;
    }

    /**
     * Gets the severity.
     *
     * @return the severity
     */
    public Type getSeverity()
    {
        return mySeverity;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Gets the default title for the severity.
     *
     * @param severity the severity
     * @return the title
     */
    private static String getDefaultTitle(Type severity)
    {
        return severity == Type.ERROR ? "Error" : severity == Type.WARNING ? "Warning" : "Information";
    }
}
