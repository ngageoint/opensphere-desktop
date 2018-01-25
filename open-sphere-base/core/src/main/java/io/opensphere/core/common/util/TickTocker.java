package io.opensphere.core.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An amazing little class that wraps integer subtraction! Handy for slowing
 * down your code.
 */
public final class TickTocker
{
    private static final Log LOG = LogFactory.getLog(TickTocker.class);

    private final long start_time;

    private final String method_name;

    private final Log log;

    private static String lookupMethodName(final Thread thread)
    {
        try
        {
            return thread.getStackTrace()[2].getMethodName();
        }
        catch (final Exception e)
        {
            return "<unknown>";
        }
    }

    public TickTocker()
    {
        this(lookupMethodName(Thread.currentThread()), LOG);
    }

    public TickTocker(final String methodName)
    {
        this(methodName, LOG);
    }

    public TickTocker(final Log log)
    {
        this(lookupMethodName(Thread.currentThread()), log);
    }

    public TickTocker(final String methodName, final Log log)
    {
        method_name = methodName;
        start_time = System.currentTimeMillis();
        this.log = log;
    }

    public final long click()
    {
        return click(log);
    }

    public final long click(final Log log)
    {
        final long delta = (System.currentTimeMillis() - start_time) / 1000;
        if (log != null)
        {
            log.info(new StringBuilder(100).append("Completed ").append(method_name).append("() in ").append(delta)
                    .append(" seconds.").toString());
        }
        return delta;
    }
}
