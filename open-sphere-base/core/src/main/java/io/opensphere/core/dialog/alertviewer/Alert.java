package io.opensphere.core.dialog.alertviewer;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.opensphere.core.dialog.alertviewer.event.Type;
import io.opensphere.core.util.DateTimeFormats;
import net.jcip.annotations.Immutable;

/** Alert bean. */
@Immutable
class Alert
{
    /** Standard time format. */
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(DateTimeFormats.TIME_FORMAT);

    /** The level. */
    private final Type myLevel;

    /** The message. */
    private final String myMessage;

    /** The time. */
    private final long myTime;

    /** Whether to make the dialog visible. */
    private final boolean myMakeVisible;

    /**
     * Constructor.
     *
     * @param level The level
     * @param message The message
     * @param makeVisible Whether to make the dialog visible
     */
    public Alert(Type level, String message, boolean makeVisible)
    {
        myLevel = level;
        myMessage = message;
        myTime = System.currentTimeMillis();
        myMakeVisible = makeVisible;
    }

    /**
     * Gets the level.
     *
     * @return the level
     */
    public Type getLevel()
    {
        return myLevel;
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
     * Gets the time.
     *
     * @return the time
     */
    public long getTime()
    {
        return myTime;
    }

    /**
     * Gets the makeVisible.
     *
     * @return the makeVisible
     */
    public boolean isMakeVisible()
    {
        return myMakeVisible;
    }

    @Override
    public String toString()
    {
        StringBuilder text = new StringBuilder(64);
        text.append(format(new Date(myTime)));
        text.append(' ');
        text.append(myLevel);
        text.append(" - ").append(myMessage);
        return text.toString();
    }

    /**
     * Formats the date.
     *
     * @param date the date
     * @return the formatted date
     */
    private String format(Date date)
    {
        String text;
        synchronized (TIME_FORMAT)
        {
            text = TIME_FORMAT.format(date);
        }
        return text;
    }
}
