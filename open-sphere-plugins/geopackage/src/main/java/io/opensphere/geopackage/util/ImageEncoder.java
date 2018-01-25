package io.opensphere.geopackage.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;

/**
 * Encodes the image data from a geopackage file to a {@link DDSImage} if the
 * image coming from the geopackage file is an actual image and not a
 * QuantizedMesh.
 */
public class ImageEncoder
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(ImageEncoder.class);

    /**
     * Encodes the passed in image into a {@link DDSImage} stream or just keeps
     * it in its current format.
     *
     * @param imageBytes The image data.
     * @return The encoded image stream.
     */
    public InputStream encodeImage(byte[] imageBytes)
    {
        String formatName = determineImageFormat(imageBytes);

        InputStream inputStream = new ByteArrayInputStream(imageBytes);

        if (StringUtils.isNotEmpty(formatName))
        {
            try
            {
                InputStream ddsStream = Image.getDDSImageStream(inputStream, formatName, imageBytes.length, null);
                inputStream.close();
                inputStream = ddsStream;
            }
            catch (ImageFormatUnknownException | IOException e)
            {
                LOGGER.error(e, e);
                inputStream = new ByteArrayInputStream(imageBytes);
            }
        }

        return inputStream;
    }

    /**
     * Determines the type of image this is.
     *
     * @param imageBytes The image data.
     * @return The image type or null if it is not a typical image.
     */
    private String determineImageFormat(byte[] imageBytes)
    {
        ByteArrayInputStream bStream = new ByteArrayInputStream(imageBytes);
        String formatName = null;

        try
        {
            ImageInputStream imgStream = ImageIO.createImageInputStream(bStream);

            Iterator<ImageReader> iter = ImageIO.getImageReaders(imgStream);

            if (iter.hasNext())
            {
                ImageReader imgReader = iter.next();
                formatName = imgReader.getFormatName();
            }
        }
        catch (IOException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
        }

        return formatName;
    }
}
