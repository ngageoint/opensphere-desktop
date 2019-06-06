package io.opensphere.core.util.CrashReporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import javafx.application.Platform;

/**
 * Sends crash log files to the server. -eventually integrate to send to JIRA
 */
public class SendLogController

{
    /**
     * Used to log messages.
     */

    private final Toolbox myToolbox;

    private final ResponseValues myResponseValues = new ResponseValues();

    private static final Logger LOGGER = Logger.getLogger(SendLogController.class);

    /**
     * Constructs a new controller.
     *
     * @param toolbox The system toolbox.
     */
    public SendLogController(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * @throws IOException
     * 
     */
    public void ConnectToServer(boolean check) throws IOException
    {
        ThreadUtilities.runBackground(() ->
        {
            URL test = null;
            try
            {
                test = new URL("https://api.github.com/users");
                InputStream theStream = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class).getServer(test)
                        .sendGet(test, myResponseValues);
                // System.out.println(new
                // StreamReader(theStream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
                 System.out.println(myResponseValues.toString());
            }
            catch (URISyntaxException | IOException e)
            {
                e.printStackTrace();
            }
        });
        System.out.println(check);
        System.out.println("results below");
    }

    /**
     * 
     */
    public void AuthenticateServer()
    {
    }

    /**
     * 
     */
    public void SendFile()
    {
    }

}