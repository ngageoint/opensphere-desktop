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

import com.bitsys.common.http.entity.StringEntity;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import javafx.application.Platform;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    private URL myURL;

    /**
     * Constructs a new controller.
     *
     * @param toolbox The system toolbox.
     * @throws MalformedURLException
     */
    public SendLogController(Toolbox toolbox) throws MalformedURLException
    {
        myToolbox = toolbox;
        this.setMyUrl("http://localhost:8080");
    }

    /**
     * @throws IOException
     * 
     */
    public void ConnectToServer()
    {
        ThreadUtilities.runBackground(() ->
        {
            try
            {
                InputStream theStream = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class).getServer(myURL)
                        .sendGet(myURL, myResponseValues);
                // This code is used to output as a JSON.
                // System.out.println(new
                // StreamReader(theStream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
                System.out.println(myResponseValues.toString());
            }
            catch (URISyntaxException | IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * 
     */
    public void AuthenticateServer()
    {

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(mediaType,
                "{\n\"fields\":{\n\"project\": \n\n{\n\t\"key\":\"BUGS\"\n},\n\"summary\":\"shit\",\n\"issuetype\": {\n\t\"name\": \"Task\"\n\t}\n}\n}\n");
        Request request = new Request.Builder().url("http://localhost:8080/rest/api/2/issue/").post(body)
                .addHeader("Content-Type", "application/json").addHeader("Authorization", "Basic QWRtaW4xOkJvdWxkZXIyMCE=")
                .addHeader("User-Agent", "PostmanRuntime/7.13.0").addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "a8f43762-e394-4ee5-9463-d63aae711347,dc32ab72-f533-4ef8-9b96-7923e8768404")
                .addHeader("Host", "localhost:8080")
                .addHeader("cookie",
                        "atlassian.xsrf.token=BLMR-FBOU-G5CJ-Z0PN_0fcf7a399459251c4d9d5b7912b5e3da03e6cd6d_lin; JSESSIONID=18BA40A32104FF0A05B1273AD18CB909")
                .addHeader("accept-encoding", "gzip, deflate").addHeader("content-length", "101")
                .addHeader("Connection", "keep-alive").addHeader("cache-control", "no-cache").build();

        ThreadUtilities.runBackground(() ->
        {
            try
            {
                Response response = client.newCall(request).execute();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        Codebeautify theJSON = new Codebeautify();
        theJSON.setKey("jira-software");
        theJSON.setName("JIRA Software");
        theJSON.setSelectedByDefault(true);
        theJSON.defaultGroups.add("jira-software-users");
    }

    /**
     * 
     */
    public void SendFile()
    {
    }

    public URL getMyUrl()
    {
        return myURL;
    }

    public URL setMyUrl(String string) throws MalformedURLException
    {
        myURL = new URL(string);
        return myURL;
    }

}