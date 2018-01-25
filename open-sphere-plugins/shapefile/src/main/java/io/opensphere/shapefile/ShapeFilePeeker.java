package io.opensphere.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.predicate.EndsWithPredicate;
import io.opensphere.core.util.zip.ZipEntryNameIterable;

/**
 * A helper class to inspect a data source and determine whether it provides
 * shape data.
 */
public class ShapeFilePeeker
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFilePeeker.class);

    /**
     * Inspect the stream and determine whether it might be a shape file. If the
     * zip contains an entry whose name ends with "shp", "shx" or "dbf", this
     * method will return true. The reason for checking for these three
     * extensions it to minimize the amount of the stream we need to read (
     * {@link ZipInputStream#getNextEntry()} reads the entire entry from the
     * stream).
     */
    private final Function<InputStream, Boolean> myStreamInspector = new Function<InputStream, Boolean>()
    {
        @Override
        public Boolean apply(InputStream is)
        {
            return Boolean.valueOf(StreamUtilities.anyMatch(new ZipEntryNameIterable(is),
                    new EndsWithPredicate(Arrays.asList(".shp", ".shx", ".dbf"), true)));
        }
    };

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new shapefile peeker.
     *
     * @param toolbox The system toolbox.
     */
    public ShapeFilePeeker(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    /**
     * Determine whether this file provides shape data.
     *
     * @param file The file to check.
     * @return true when shape data is available at this location.
     */
    public boolean isShape(File file)
    {
        if (file != null && file.canRead())
        {
            String fileName = file.getAbsolutePath().toLowerCase();
            if (fileName.endsWith(".shp"))
            {
                return true;
            }
            else if (fileName.endsWith(".zip"))
            {
                try
                {
                    return applyFunction(file, myStreamInspector).booleanValue();
                }
                catch (FileNotFoundException e)
                {
                    LOGGER.error("Could not open file." + e, e);
                }
            }
        }
        return false;
    }

    /**
     * Determine whether this URL provides shape data.
     *
     * @param url The URL to check.
     * @return true when shape data is available at this location.
     */
    public boolean isShape(URL url)
    {
        try
        {
            if (url.getProtocol().equalsIgnoreCase("file"))
            {
                return isShape(new File(url.getFile()));
            }
            else
            {
                return applyFunction(url, myToolbox, myStreamInspector);
            }
        }
        catch (GeneralSecurityException e)
        {
            LOGGER.error("Unable to connect to source." + e, e);
        }
        catch (IOException | URISyntaxException e)
        {
            LOGGER.error("Source is not readable." + e, e);
        }

        return false;
    }

    /**
     * Open a connection to a URL and apply a function to the input stream.
     *
     * @param url The URL.
     * @param toolbox The system toolbox.
     * @param function The function.
     * @return The result of the function.
     * @throws GeneralSecurityException If there was an issue with the users
     *             security.
     * @throws IOException If there was an issue communicating with the server
     *             at that url.
     * @throws URISyntaxException If the url could not be converted to a url.
     */
    private boolean applyFunction(URL url, Toolbox toolbox, Function<InputStream, Boolean> function)
        throws GeneralSecurityException, IOException, URISyntaxException
    {
        InputStream is = null;

        boolean returnValue = false;

        ServerProvider<HttpServer> serverProvider = toolbox.getServerProviderRegistry().getProvider(HttpServer.class);

        if (serverProvider != null)
        {
            HttpServer server = serverProvider.getServer(url);
            ResponseValues response = new ResponseValues();

            is = server.sendGet(url, response);
        }

        if (is != null)
        {
            try
            {
                returnValue = function.apply(is).booleanValue();
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    LOGGER.error("Could not close stream." + e, e);
                }
            }
        }

        return returnValue;
    }

    /**
     * Apply a function that takes a FileInputStream to a File.
     *
     * @param <T> The type of the return value.
     * @param file The file.
     * @param function The function.
     * @return The return value from the function.
     * @throws FileNotFoundException If the file cannot be opened.
     */
    private static <T> T applyFunction(File file, Function<? super FileInputStream, T> function) throws FileNotFoundException
    {
        FileInputStream is = new FileInputStream(file);
        try
        {
            return function.apply(is);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                LOGGER.error("Could not close stream." + e, e);
            }
        }
    }
}
