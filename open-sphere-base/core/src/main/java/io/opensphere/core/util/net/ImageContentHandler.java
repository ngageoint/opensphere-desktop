package io.opensphere.core.util.net;

import java.io.IOException;
import java.net.URLConnection;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFactory;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.image.ImageMetrics;

/**
 * Content handler for {@link Image}s.
 */
public class ImageContentHandler extends java.net.ContentHandler
{
    /** The image factory. */
    private final ImageFactory myImageFactory = new ImageFactory();

    @Override
    public Image getContent(URLConnection urlc) throws IOException
    {
        // TODO: can we start compressing the image while it's being downloaded?
        try
        {
            return myImageFactory.createImage(urlc.getInputStream(), urlc.getContentLength(), urlc.getContentType(), false, false,
                    (ImageMetrics)null);
        }
        catch (ImageFormatUnknownException e)
        {
            throw new IOException(e);
        }
    }
}
