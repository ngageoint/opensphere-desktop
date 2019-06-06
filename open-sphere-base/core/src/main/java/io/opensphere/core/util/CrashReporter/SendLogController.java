package io.opensphere.core.util.CrashReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import javax.imageio.stream.FileImageInputStream;

import org.apache.log4j.Logger;
import io.opensphere.core.Toolbox;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;

/**
 * Sends crash log files to the server. -eventually integrate to send to JIRA
 */
public class SendLogController

{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(SendLogController.class);

    /**
     * Constructs a new controller.
     *
     * @param toolbox The system toolbox.
     */
    public SendLogController(Toolbox toolbox)
    {

        
    }

    /**
     * @throws IOException
     * 
     */
    public void ConnectToServer(boolean check) throws IOException
    {
      //  final File tempFile = File.createTempFile("attachment" + new Date().getTime(), null);
      //  FileOutputStream out = new FileOutputStream(tempFile);
//
//        System.out.println(check);
//        
//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder().url("https://api.github.com/users").get().addHeader("cache-control", "no-cache")
//                .addHeader("postman-token", "1e4f62ea-7682-ee09-94b4-f4fd7487da66").build();
//
//        try
//        {
//            Response response = client.newCall(request).execute();
//            System.out.println("Web Test Results Below");
//            System.out.println(response.isSuccessful());
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
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