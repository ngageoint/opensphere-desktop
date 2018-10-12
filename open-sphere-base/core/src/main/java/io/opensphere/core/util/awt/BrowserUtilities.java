package io.opensphere.core.util.awt;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Browser utilities. */
public final class BrowserUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(BrowserUtilities.class);

    /**
     * Launches the default browser to display a URL string.
     *
     * @param urlString The URL string
     * @param parent The parent component
     */
    public static void browse(String urlString, Component parent)
    {
        try
        {
            browse(new URI(urlString), parent);
        }
        catch (URISyntaxException e)
        {
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("URI Syntax Exception for string '" + urlString + "'", e);
            }
            error(e.getMessage(), parent);
        }
    }

    /**
     * Launches the default browser to display a {@code URL}.
     *
     * @param url The URL
     * @param parent The parent component
     */
    public static void browse(URL url, Component parent)
    {
        try
        {
            browse(url.toURI(), parent);
        }
        catch (URISyntaxException e)
        {
            LOGGER.trace("URI Syntax Exception.", e);
            error(e.getMessage(), parent);
        }
    }

    /**
     * Launches the default browser to display a {@code URI}.
     *
     * @param uri The URI
     * @param parent The parent component
     */
    public static void browse(URI uri, Component parent)
    {
        browse(uri, message -> error(message, parent));
    }

    /**
     * Launches the default browser to display a {@code URI}.
     *
     * @param uri The URI
     * @param errorHandler The error handler
     */
    private static void browse(URI uri, Consumer<String> errorHandler)
    {
        ThreadUtilities.runBackground(() ->
        {
            if (Desktop.isDesktopSupported())
            {
                try
                {
                    Desktop.getDesktop().browse(uri);
                }
                catch (IOException e)
                {
                    LOGGER.trace("IO Exception for browsing.", e);
                    errorHandler.accept(e.getMessage());
                }
            }
            else
            {
                errorHandler.accept("Launching a browser is not supported on this system");
            }
        });
    }

    /**
     * Handles an error.
     *
     * @param message The error message
     * @param parent The parent component
     */
    private static void error(String message, Component parent)
    {
        LOGGER.error(message);
        EventQueueUtilities.runOnEDT(
                () -> JOptionPane.showMessageDialog(parent, message, "Open Hyperlink Error", JOptionPane.ERROR_MESSAGE));
    }

    /** Private constructor. */
    private BrowserUtilities()
    {
    }
}
