package io.opensphere.core.appl;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

/**
 * A simple server to allow testing / demos of the auto update feature.
 */
public final class AutoUpdateDemoServer
{
    /** The demo server at "http://localhost:8080". */
    private static Server demoServer = new Server(8080);

    /** The resource handler. */
    private static ResourceHandler resourceHandler = new ResourceHandler();

    /**
     * Starts the server.
     *
     * @param args the args
     * @throws Exception if something goes wrong starting or joining the server
     */
    public static void main(String[] args) throws Exception
    {
        copyUpdateFiles();

        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase("src/test/resources/auto-update-demo/");

        GzipHandler gzip = new GzipHandler();
        demoServer.setHandler(gzip);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, new DefaultHandler() });
        gzip.setHandler(handlers);

        demoServer.start();
        demoServer.join();
    }

    /**
     * Copies the update files to the server demo folders.
     *
     * @throws IOException if the update files can't be copied to the demo
     *             server folders
     */
    private static void copyUpdateFiles() throws IOException
    {
        File linuxUpdateFiles = FileUtils.getFile(System.getProperty("user.dir"),
                "../../open-sphere-install/open-sphere-install-linux64/target/staging/update_pack");
        File linuxServerDirectory = FileUtils
                .getFile("src/test/resources/auto-update-demo/auto_deployers/mist/desktop/updates/5.2.1/linux64");
        FileUtils.copyDirectory(linuxUpdateFiles, linuxServerDirectory);

        File windowsUpdateFiles = FileUtils.getFile(System.getProperty("user.dir"),
                "../../open-sphere-install/open-sphere-install-windows64/target/staging/update_pack");
        File windowsServerDirectory = FileUtils
                .getFile("src/test/resources/auto-update-demo/auto_deployers/mist/desktop/updates/5.2.1/linux64");
        FileUtils.copyDirectory(windowsUpdateFiles, windowsServerDirectory);

        File latestFile = new File("src/test/resources/auto-update-demo/auto_deployers/mist/desktop/updates/", "mist.latest");
        PrintStream out = new PrintStream(latestFile);
        out.println("5.2.1");
        out.flush();
        out.close();
    }

    /**
     * Disallows instantiation.
     */
    private AutoUpdateDemoServer()
    {
        throw new UnsupportedOperationException("Instantiating a utility class");
    }
}
