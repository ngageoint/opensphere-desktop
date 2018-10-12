package io.opensphere.core.geometry;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.Image.CompressionType;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.image.ImmediateImageProvider;
import io.opensphere.core.image.ObservableImageProvider;
import io.opensphere.core.util.Utilities;
import net.jcip.annotations.GuardedBy;

/**
 * {@link ImmediateImageProvider} that provides a single image.
 */
public class SingletonImageProvider implements ImmediateImageProvider<Void>, ObservableImageProvider<Void>
{
    /**
     * The type of compression to use for the image when uploading to the card.
     */
    private final CompressionType myCompressionHint;

    /** The observer for the TileProcessor to know when data is available. */
    @GuardedBy("this")
    private ObservableImageProvider.Observer myDataReadyObserver;

    /** Buffered image used to construct the image I provide. */
    @GuardedBy("this")
    private BufferedImage myImage;

    /** The classpath url for the image. */
    private String myUrl;

    /**
     * Constructor.
     *
     * @param image Image which I will provide.
     */
    public SingletonImageProvider(BufferedImage image)
    {
        this(image, CompressionType.D3DFMT_A8R8G8B8);
    }

    /**
     * When using DXT1, DXT3 or DXT5 compression, the image width and height
     * must both be divisible by 4.
     *
     * @param image Image which I will provide.
     * @param compressionHint Compression to use for the texture.
     */
    public SingletonImageProvider(BufferedImage image, CompressionType compressionHint)
    {
        myCompressionHint = compressionHint;
        myImage = image;
    }

    /**
     * Constructor.
     *
     * @param url String that describes image file that is in resource path.
     */
    public SingletonImageProvider(String url)
    {
        this(url, Image.CompressionType.D3DFMT_A8R8G8B8);
    }

    /**
     * Constructor.
     *
     * @param url String that describes image file that is in resource path.
     * @param compressionHint Compression to use for the image.
     */
    public SingletonImageProvider(String url, Image.CompressionType compressionHint)
    {
        myUrl = Utilities.checkNull(url, "url");
        myCompressionHint = compressionHint;
    }

    @Override
    public synchronized void addObserver(ObservableImageProvider.Observer observer)
    {
        myDataReadyObserver = observer;
    }

    @Override
    public synchronized boolean canProvideImageImmediately()
    {
        return myImage != null || myUrl != null;
    }

    @Override
    public synchronized Image getImage(Void key)
    {
        BufferedImage image = myImage;
        String url = myUrl;
        if (image == null && url != null)
        {
            try
            {
                BufferedInputStream inputStream = new BufferedInputStream(SingletonImageProvider.class.getResourceAsStream(url));
                image = ImageIO.read(inputStream);
                if (image == null)
                {
                    // Clear the URL to avoid trying again.
                    myUrl = null;
                    throw new IllegalArgumentException("Unable to load image at url: " + url);
                }
            }
            catch (IOException ex)
            {
                // Clear the URL to avoid trying again.
                myUrl = null;
                throw new IllegalArgumentException("Unable to load image at url: " + url, ex);
            }
        }
        if (image == null)
        {
            return null;
        }
        Image result = new ImageIOImage(image);
        result.setCompressionHint(myCompressionHint);
        return result;
    }

    /**
     * Standard getter.
     *
     * @return The observer.
     */
    public synchronized ObservableImageProvider.Observer getObserver()
    {
        return myDataReadyObserver;
    }

    /**
     * Set the image.
     *
     * @param image the image to set
     */
    public void setImage(BufferedImage image)
    {
        synchronized (this)
        {
            myImage = image;
        }
        ObservableImageProvider.Observer observer = getObserver();
        if (observer != null)
        {
            observer.dataReady();
        }
    }
}
