package io.opensphere.core.util.CrashReporter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.bitsys.common.http.entity.StringEntity;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.ContentType;
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

    private final ResponseValues myResponseValues2 = new ResponseValues();

    private static final Logger LOGGER = Logger.getLogger(SendLogController.class);

    private URL myURL;

    private JSONObject theJson = new JSONObject();

    /**
     * Constructs a new controller.
     *
     * @param toolbox The system toolbox.
     * @throws MalformedURLException
     */
    public SendLogController(Toolbox toolbox)
    {
        myToolbox = toolbox;
        try
        {
            this.setMyUrl("http://localhost:8080");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    public void ConnectToServer()
    {
        ThreadUtilities.runBackground(() ->
        {
            try
            {
                System.out.println("Connecting to Server");
                InputStream theStream = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class).getServer(myURL)
                        .sendGet(myURL, myResponseValues);

                OkHttpClient tester = new OkHttpClient();

                // System.out.println(new
                // StreamReader(theStream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
                System.out.println(myResponseValues.toString());
                System.out.println("Connected to Server");
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
    @SuppressWarnings("unchecked")
    public void AuthenticateServer()
    {
//        OkHttpUtil bypass = new OkHttpUtil();
//        try
//        {
//            bypass.init(true);
//        }
//        catch (Exception e1)
//        {
//        }
        // OkHttpClient client = bypass.getClient();
        OkHttpClient tester = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/xml");
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(mediaType,
                "{\n    \"fields\": {\n        \"project\": {\n            \"key\": \"BUGS\"\n        },\n        \"summary\": \"the details of the bug\",\n        \"issuetype\": {\n            \"name\": \"Task\"\n        }\n    }\n}");
        Request request = new Request.Builder().url("http://localhost:8080/rest/api/2/issue/").post(body)
                .addHeader("Content-Type", "application/xml").addHeader("Authorization", "Basic QWRtaW4xOkJvdWxkZXIyMCE=")
                .addHeader("User-Agent", "PostmanRuntime/7.13.0").addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "7d69cbef-81a2-4b9e-bda9-95f34fbd7cb3,6eb9d942-f435-40f3-9428-bc21b92fc07a")
                .addHeader("Host", "localhost:8080")
                .addHeader("cookie",
                        "atlassian.xsrf.token=BLMR-FBOU-G5CJ-Z0PN_0fcf7a399459251c4d9d5b7912b5e3da03e6cd6d_lin; JSESSIONID=FAC488CFFC60C8A16163045BBF5CC775")
                .addHeader("accept-encoding", "gzip, deflate").addHeader("content-length", "119")
                .addHeader("Connection", "keep-alive").addHeader("cache-control", "no-cache").build();
        ThreadUtilities.runBackground(() ->
        {
            AccessController.doPrivileged(new PrivilegedAction<Object>()
            {
                private Response response;

                @Override
                public Object run()
                {
                    try
                    {
                        response = tester.newCall(request).execute();
                    }
                    catch (IOException e)
                    {
                    }
                    return response;
                }

            });
        });
    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void SendFile()
    {
        Map<String, String> Headers = Map.ofEntries(Map.entry("Content-Type", "application/json"),
                Map.entry("Authorization", "Basic QWRtaW4xOkJvdWxkZXIyMCE="), Map.entry("User-Agent", "PostmanRuntime/7.13.0")
//                Map.entry("Accept", "*/*"), Map.entry("Cache-Control", "no-cache"),
//                Map.entry("Postman-Token", "b4f2926c-5f17-498c-9a1b-a1d55c491d55,5d7b23ea-b2ba-4103-a8ce-38e4ac511388"),
//                Map.entry("Host", "localhost:8080"),
//                Map.entry("cookie",
//                        "atlassian.xsrf.token=BLMR-FBOU-G5CJ-Z0PN_0fcf7a399459251c4d9d5b7912b5e3da03e6cd6d_lin; JSESSIONID=ED8948E3D7FD741A1D7D9ACD50B73797"),
//                Map.entry("accept-encoding", "gzip, deflate"), Map.entry("Connection", "keep-alive"),
//                Map.entry("cache-control", "no-cache"));
        );
        // Body Values Attempt 2
        String string = new String("{\n" + "\"fields\":{\n" + "\"project\": \n" + "\n" + "{\n" + "    \"key\":\"BUGS\"\n" + "},\n"
                + "\"summary\":\"Send From Eclipse \",\n" + "\"issuetype\": {\n" + "    \"name\": \"Task\"\n" + "    }\n" + "}\n"
                + "}" + "\n");

        InputStream is2 = new ByteArrayInputStream(string.getBytes());
        try
        {
            new StreamReader(is2).copyStream(System.out);
        }
        catch (IOException e1)
        {
        }
        ThreadUtilities.runBackground(() ->
        {
            // Attempt 3
            System.out.println("Submitting Files");
            try
            {
                // File initialFile = new
                // File("/home/crombiek/Desktop/it.JSON");
                // InputStream targetStream = new FileInputStream(initialFile);
                InputStream theStream2 = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class).getServer(myURL)
                        .sendPost(myURL, is2, Headers, myResponseValues2, ContentType.JSON);
                // InputStream theStream2 =
                // myToolbox.getServerProviderRegistry().getProvider(HttpServer.class).getServer(myURL)
                // .sendPost(myURL, is2, Headers, myResponseValues2,
                // ContentType.JSON);
//                InputStream theStream2 = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class).getServer(myURL)
//                        .sendPost(myURL, new ByteArrayInputStream(theJson.toJSONString().getBytes()), myResponseValues2);
            }
            catch (IOException | URISyntaxException e)
            {
            }
            // Attempt 4
            // System.out.println(myResponseValues2.toString());

            System.out.println("Files Submitted");
        });
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