package io.opensphere.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import io.opensphere.core.common.shapefile.utils.DBFColumnInfo;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile.Mode;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.net.UrlUtilities;

/** Helper methods for reading shape files. */
public final class ShapeFileReadUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileReadUtilities.class);

    /**
     * Gets the data sample.
     *
     * @param shapeFile the a shape file
     * @param maxLines the max lines
     * @return the data sample
     */
    public static List<List<String>> getDataSample(ESRIShapefile shapeFile, int maxLines)
    {
        List<List<String>> result = New.list();
        int count = 0;
        if (shapeFile.size() > 0)
        {
            for (ShapefileRecord rec : shapeFile)
            {
                if (rec.metadata != null && rec.metadata.length > 0)
                {
                    List<String> values = New.list();
                    for (Object obj : rec.metadata)
                    {
                        values.add(obj == null ? "" : obj.toString());
                    }

                    result.add(values);
                }
                count++;
                if (count >= maxLines)
                {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Gets the header.
     *
     * @param shapeFile the a shape file
     * @return the header
     */
    public static List<String> getHeader(ESRIShapefile shapeFile)
    {
        List<DBFColumnInfo> dbHdr = shapeFile.getMetadataHeader();

        if (dbHdr != null)
        {
            List<String> arr = New.list();
            for (DBFColumnInfo field : dbHdr)
            {
                arr.add(field.fieldName);
            }
            return arr;
        }

        return null;
    }

    /**
     * Play with file permissions to allow better access. The underlying library
     * we use to access a file wants it to be writable.
     *
     * @param aFile - the file to open permissions
     */
    public static void openPermissions(String aFile)
    {
        try
        {
            File setPerm = new File(aFile);
            setPerm.setExecutable(true);
            setPerm.setWritable(true);
            setPerm.setReadable(true);
            setPerm = null;
        }
        catch (SecurityException e)
        {
            LOGGER.warn("Failed to open permissions for file: " + aFile);
        }
    }

    /**
     * Uses a worker Thread to invoke method readFile (q.v.), which should not
     * run on the AWT thread.  Regardless, the calling thread waits on the work
     * to be completed before returning.
     * @param path the file or URL path
     * @param svr the server provider thingy
     * @return an ESRIShapefile instance
     */
    public static ESRIShapefile readOnWorker(String path, ServerProvider<HttpServer> svr)
    {
        return workAndWaitFor(() -> readFile(path, svr));
    }

    /**
     * Create a shape file reader. Note that this does not actually read in the
     * file, records are read from as necessary.
     *
     * @param shapefile The path to the file.
     * @param serverProvider The provider of an HTTP server.
     * @return The shape file reader.
     */
    public static ESRIShapefile readFile(String shapefile, ServerProvider<HttpServer> serverProvider)
    {
        InputStream inputStream = getStream(shapefile, serverProvider);
        try
        {
            if (inputStream != null)
            {
                List<String> files = FileUtilities.explodeZip(inputStream, null, System.currentTimeMillis());
                for (String file : files)
                {
                    if (file.toLowerCase().endsWith(".shp"))
                    {
                        return new ESRIShapefile(Mode.READ, file);
                    }
                }
            }
            else
            {
                openPermissions(shapefile);
                openPermissions(shapefile.replace(".shp", ".dbf"));
                return new ESRIShapefile(ESRIShapefile.Mode.READ, shapefile);
            }
        }
        catch (IOException | RuntimeException e)
        {
            LOGGER.error("Could not read shape file: " + shapefile, e);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
            }
            catch (IOException e)
            {
                LOGGER.error("Failed to close stream." + e, e);
            }
        }

        return null;
    }

    /**
     * If the source specifies either a local zipped file or a URL which returns
     * a zipped file, create an InputStream for the source and return it.
     *
     * @param shapefile The location of the shape file (may be a URL or a
     *            filename).
     * @param serverProvider An HTTP server provider.
     * @return The InputStream if it was created and {@code null} if it was not.
     */
    private static InputStream getStream(String shapefile, ServerProvider<HttpServer> serverProvider)
    {
        try
        {
            URL url = new URL(shapefile);
            InputStream inputStream = null;
            try
            {
                // If this URL is actual a local file, process it like a normal
                // local file.
                if (UrlUtilities.isFile(url))
                {
                    String fileName = url.getFile();
                    if (fileName.toLowerCase().endsWith(".zip"))
                    {
                        return new FileInputStream(fileName);
                    }
                }
                else
                {
                    HttpServer server = serverProvider.getServer(url);
                    ResponseValues response = new ResponseValues();
                    inputStream = server.sendGet(url, response);

                    if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
                    {
                        inputStream = null;
                    }
                }
            }
            catch (IOException | URISyntaxException e)
            {
                LOGGER.error("Could not read shape file." + e, e);
            }

            return inputStream;
        }
        catch (MalformedURLException e1)
        {
            // This isn't a URL, so assume it is a file name.
            if (shapefile.toLowerCase().endsWith(".zip"))
            {
                try
                {
                    return new FileInputStream(shapefile);
                }
                catch (FileNotFoundException e)
                {
                    LOGGER.error("Could not read shapefile." + e, e);
                }
            }
        }
        return null;
    }

    /**
     * Invoke the Supplier on a worker Thread while the current Thread waits
     * for the result to be produced and returned to the caller.
     * @param src a Supplier to be invoked
     * @return the value from the Supplier
     */
    private static <T> T workAndWaitFor(Supplier<T> src)
    {
        Object lock = new Object();
        Ref<T> ref = new Ref<>();
        synchronized (lock)
        {
            ThreadUtilities.runBackground(() ->
            {
                synchronized (lock)
                {
                    ref.val = src.get();
                    lock.notify();
                }
            });
            try
            {
                lock.wait();
            }
            catch (InterruptedException eek)
            {
            }
        }
        return ref.val;
    }

    /**
     * Required because Java is stupid.
     * @param <T> reference type
     */
    private static class Ref<T>
    {
        /** Bla. */
        public T val;
    }

    /** Disallow instantiation. */
    private ShapeFileReadUtilities()
    {
    }
}
