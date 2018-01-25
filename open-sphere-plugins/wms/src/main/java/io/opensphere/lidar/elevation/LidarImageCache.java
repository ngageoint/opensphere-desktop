package io.opensphere.lidar.elevation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.StreamingImage;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.lidar.util.GeotiffFileReader;

/**
 * Cache's any lidar temp files.
 *
 */
public class LidarImageCache
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(LidarImageCache.class);

    /**
     * The instance of this class.
     */
    private static final LidarImageCache ourInstance = new LidarImageCache();

    /**
     * The cached geotiff image readers.
     */
    private final Map<String, GeotiffFileReader> myGeotiffs = Collections.synchronizedMap(New.map());

    /**
     * Gets the instance of this class.
     *
     * @return The cache.
     */
    public static final LidarImageCache getInstance()
    {
        return ourInstance;
    }

    /**
     * Closes all readers and removes the cache from the syste
     */
    public void close()
    {
        synchronized (myGeotiffs)
        {
            for (GeotiffFileReader reader : myGeotiffs.values())
            {
                File file = new File(reader.getFilePath());
                reader.close();
                if (!file.delete())
                {
                    LOGGER.warn("Unable to delete temp file " + file);
                }
            }

            myGeotiffs.clear();
        }
    }

    /**
     * Gets the reader to read elevation images from the specified image.
     *
     * @param layerId the id of the layer.
     * @param image The image to read.
     * @param box The bounding box of the image.
     * @return The reader or null if it could not be initialized.
     */
    public GeotiffFileReader getReader(String layerId, Image image, GeographicBoundingBox box)
    {
        GeotiffFileReader reader = myGeotiffs.get(layerId + box.toString());
        if (reader == null)
        {
            File file = writeToTempFile(image);
            if (file != null)
            {
                reader = new GeotiffFileReader(file, true);
                if (reader.isInitSuccess())
                {
                    myGeotiffs.put(layerId + box.toString(), reader);
                }
            }
        }

        return reader;
    }

    /**
     * Writes the image to a temp file.
     *
     * @param image the image
     * @return the file, or null if there was an error
     */
    private File writeToTempFile(Image image)
    {
        // Create the file
        File file = null;
        try
        {
            file = File.createTempFile("lidarImage", ".tif");
            file.deleteOnExit();
        }
        catch (IOException e)
        {
            LOGGER.error(e);
        }

        // Write to the file
        if (file != null)
        {
            try (FileOutputStream outStream = new FileOutputStream(file))
            {
                InputStream inStream = ((StreamingImage<?>)image).getInputStream();
                new StreamReader(inStream).readStreamToOutputStream(outStream);
            }
            catch (IOException e)
            {
                LOGGER.error(e);
                if (!file.delete())
                {
                    LOGGER.warn("Failed to delete " + file);
                }
                file = null;
            }
        }

        return file;
    }
}
