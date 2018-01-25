package io.opensphere.osh.envoy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.HttpUtilities;

/** Abstract OpenSensorHub envoy. */
public abstract class AbstractOSHEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractOSHEnvoy.class);

    /** The server provider. */
    private final ServerProvider<HttpServer> myServerProvider;

    /** The URL cache path. */
    private final Path myCachePath;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public AbstractOSHEnvoy(Toolbox toolbox)
    {
        super(toolbox);
        myServerProvider = toolbox != null ? toolbox.getServerProviderRegistry().getProvider(HttpServer.class) : null;
        final String runtimeDir = StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties());
        myCachePath = Paths.get(runtimeDir, "urlCache");
    }

    @Override
    public void open()
    {
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        return SingleSatisfaction.generateSatisfactions(intervalSets);
    }

    @Override
    public String getThreadPoolName()
    {
        return AbstractOSHEnvoy.class.getSimpleName();
    }

    /**
     * Performs the request.
     *
     * @param url the URL
     * @return the response stream
     * @throws IOException If something went wrong
     */
    protected CancellableInputStream performRequest(URL url) throws IOException
    {
        CancellableInputStream responseStream;

//        Boolean.getBoolean("opensphere.productionMode") ||
        if (url.toString().contains("phenomenonTime,now"))
        {
            responseStream = HttpUtilities.sendGet(url, myServerProvider);
        }
        else
        {
            final Path path = getPath(url);
            if (Files.exists(path))
            {
                LOGGER.info("Playback " + path);
                responseStream = new CancellableInputStream(new BufferedInputStream(Files.newInputStream(path)), () ->
                {
                });
            }
            else
            {
                responseStream = HttpUtilities.sendGet(url, myServerProvider);

                LOGGER.info("Record " + path);
                final Path parent = path.getParent();
                if (parent != null)
                {
                    Files.createDirectories(parent);
                }
                try (OutputStream fileStream = new BufferedOutputStream(Files.newOutputStream(path)))
                {
                    final InputStream copyStream = new StreamReader(responseStream).copyStream(fileStream);
                    responseStream = new CancellableInputStream(copyStream, () ->
                    {
                    });
                }
            }
        }

        return responseStream;
    }

    /**
     * Gets the file system path for the URL.
     *
     * @param url the URL
     * @return the path
     */
    private Path getPath(URL url)
    {
        final String server = StringUtilities.replaceSpecialCharacters(url.getHost());
        String file = StringUtilities.replaceSpecialCharacters(url.getFile());
        if (file.length() > 250)
        {
            file = file.substring(0, 250);
        }
        file = file.replace("_sensorhub_sos_service_SOS_version_2_0_request_", "");
        return Paths.get(myCachePath.toString(), server, file + ".dat");
    }
}
