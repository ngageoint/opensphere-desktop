package io.opensphere.core.appl;

import org.apache.log4j.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Handler for uncaught exceptions.
 */
class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(UncaughtExceptionHandler.class);

    /**
     * Install this handler as the default.
     */
    public static void install()
    {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        try
        {
            LOGGER.fatal("Uncaught Exception: " + e, e);
        }
        finally
        {
            if (e instanceof OutOfMemoryError)
            {
                exit();
            }
        }
    }

    /**
     * System exit.
     */
    @SuppressFBWarnings("DM_EXIT")
    private void exit()
    {
        System.exit(1);
    }
}
