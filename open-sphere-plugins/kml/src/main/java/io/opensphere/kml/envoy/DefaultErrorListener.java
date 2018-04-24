package io.opensphere.kml.envoy;

import java.io.PrintWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

/**
 * ErrorListener class that replicates functionality from
 * com.sun.org.apache.xml.internal.utils.DefaultErrorHandler.
 */
public class DefaultErrorListener implements ErrorListener
{
    /** PrintWriter for error output. */
    private final PrintWriter m_pw;

    /** Constructs a DefaultErrorListener connected to System.err. */
    public DefaultErrorListener()
    {
        m_pw = new PrintWriter(System.err, true);
    }

    @Override
    public void warning(TransformerException exception) throws TransformerException
    {
        printLocation(m_pw, exception);
        m_pw.println(exception.getMessage());
    }

    @Override
    public void error(TransformerException exception) throws TransformerException
    {
        throw exception;
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException
    {
        throw exception;
    }

    /**
     * Prints the location of the given exception.
     *
     * @param pw the print writer
     * @param exception the exception to locate
     */
    private static void printLocation(PrintWriter pw, Throwable exception)
    {
        SourceLocator locator = null;
        Throwable cause = exception;

        // Try to find the locator closest to the cause.
        do
        {
            SourceLocator causeLocator = ((TransformerException)cause).getLocator();
            if (null != causeLocator)
            {
                locator = causeLocator;
            }

            if (cause instanceof TransformerException)
            {
                cause = ((TransformerException)cause).getCause();
            }
            else
            {
                cause = null;
            }
        }
        while (null != cause);

        if (null != locator)
        {
            // m_pw.println("Parser fatal error: "+exception.getMessage());
            String id = (null != locator.getPublicId()) ? locator.getPublicId()
                    : (null != locator.getSystemId()) ? locator.getSystemId() : "SystemId Unknown";

            pw.print(id + "; " + "line" + locator.getLineNumber() + "; " + "column" + locator.getColumnNumber() + "; ");
        }
        else
        {
            pw.print("(Error Location Unknown)");
        }
    }
}
