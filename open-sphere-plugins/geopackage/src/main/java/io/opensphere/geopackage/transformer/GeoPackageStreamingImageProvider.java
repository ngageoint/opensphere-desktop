package io.opensphere.geopackage.transformer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import io.opensphere.core.image.ImageStreamProvider;
import io.opensphere.core.model.ZYXImageKey;

/**
 * A {@link ImageStreamProvider} that streams a {@link ByteBuffer} into input
 * streams.
 */
public class GeoPackageStreamingImageProvider implements ImageStreamProvider<ZYXImageKey>
{
    /**
     * The image data.
     */
    private final byte[] myBuffer;

    /**
     * Constructs a new {@link GeoPackageStreamingImageProvider}.
     *
     * @param buffer The data to stream.
     */
    public GeoPackageStreamingImageProvider(ByteBuffer buffer)
    {
        myBuffer = new byte[buffer.limit()];
        buffer.get(myBuffer);
    }

    @Override
    public InputStream getImageStream(ZYXImageKey key)
    {
        return new ByteArrayInputStream(myBuffer);
    }
}
