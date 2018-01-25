package io.opensphere.core.util.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * Consumes a process by reading the required InputStreams.
 */
public class ProcessConsumer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ProcessConsumer.class);

    /** The process. */
    private final Process myProcess;

    /** The executor service for reading input streams. */
    private static ExecutorService ourExecutorService;

    /**
     * Constructor.
     *
     * @param proc The Process to consume
     */
    public ProcessConsumer(Process proc)
    {
        myProcess = proc;
    }

    /**
     * Consume the Process.
     */
    public void consume()
    {
        initExecutor();
        ourExecutorService.execute(new InputStreamConsumer(myProcess.getInputStream(), "STDOUT"));
        ourExecutorService.execute(new InputStreamConsumer(myProcess.getErrorStream(), "STDERR"));
    }

    /**
     * Consume the Process and wait for completion.
     *
     * @return the exit value of the process. By convention, <code>0</code>
     *         indicates normal termination.
     * @exception InterruptedException if the current thread is
     *                {@linkplain Thread#interrupt() interrupted} by another
     *                thread while it is waiting, then the wait is ended and an
     *                {@link InterruptedException} is thrown.
     */
    public int consumeAndWait() throws InterruptedException
    {
        consume();
        return myProcess.waitFor();
    }

    /**
     * Initializes the executor service.
     *
     * @return The executor service
     */
    private synchronized ExecutorService initExecutor()
    {
        if (ourExecutorService == null)
        {
            ourExecutorService = Executors.newSingleThreadExecutor();
        }
        return ourExecutorService;
    }

    /**
     * Consumes an input stream.
     */
    private static class InputStreamConsumer implements Runnable
    {
        /** The input stream. */
        private final InputStream myInputStream;

        /** The type of input stream. */
        private final String myType;

        /**
         * Constructor.
         *
         * @param inputStream The input stream
         * @param type The type of input stream
         */
        public InputStreamConsumer(InputStream inputStream, String type)
        {
            myInputStream = inputStream;
            myType = type;
        }

        @Override
        public void run()
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(myInputStream, StringUtilities.DEFAULT_CHARSET));
            StringBuilder sb = new StringBuilder();
            sb.append(myType).append(": ");
            String line;
            try
            {
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line);
                }
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage());
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(sb.toString());
            }
        }
    }
}
