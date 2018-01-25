package io.opensphere.geopackage.export.tile;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractTileGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.image.DDSImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;

/**
 * Converts the image returned from a {@link TileGeometry} to something the
 * geopackage file can handle.
 */
public class TileImageByteProvider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(TileImageByteProvider.class);

    /**
     * Gets the image of the tile that is compatible with a geopackage file.
     *
     * @param tile The tile to get its image for.
     * @return The image compatible with the geopackage file.
     */
    @SuppressWarnings("unchecked")
    public GeoPackageImage getImageBytes(AbstractTileGeometry<?> tile)
    {
        ImageProvider<Object> imageProvider = (ImageProvider<Object>)tile.getImageManager().getImageProvider();
        byte[] imageBytes = new byte[0];
        int width = 512;
        int height = 512;
        try (Image tileImage = imageProvider.getImage(tile.getImageManager().getImageKey()))
        {
            if (tileImage != null)
            {
                width = tileImage.getWidth();
                height = tileImage.getHeight();

                if (tileImage instanceof DDSImage)
                {
                    try
                    {
                        imageBytes = ((DDSImage)tileImage).toJpg();
                    }
                    catch (IOException e)
                    {
                        LOGGER.error(e, e);
                    }
                }
                else
                {
                    imageBytes = tileImage.getByteBuffer().array();
                }
            }
        }

        return new GeoPackageImage(ByteBuffer.wrap(imageBytes), width, height);
    }
}
