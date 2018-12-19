package io.opensphere.mantle.icon;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.image.ImmediateImageProvider;
import io.opensphere.core.image.processor.ImageProcessor;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.io.IOUtilities;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.net.UrlUtilities;

/**
 * The Class IconImageProvider.
 */
public class IconImageProvider implements ImmediateImageProvider<Void>
{
    /** The Constant ourBrokenImageURL. */
    public static final URL ourBrokenImageURL = IconImageProvider.class.getClassLoader().getResource("images/brokenimage.png");

    /** The Constant DEFAULT_COMPRESSION_HINT. */
    private static final Image.CompressionType DEFAULT_COMPRESSION_HINT = Image.CompressionType.D3DFMT_A8R8G8B8;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(IconImageProvider.class);

    /** The Constant ourBrokenImage. */
    private static final ImageIOImage ourBrokenImage;

    /** Updater for myContent. */
    private static final AtomicReferenceFieldUpdater<IconImageProvider, Image> CONTENT_UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(IconImageProvider.class, Image.class, "myContent");

    /** The Compression hint. */
    private final Image.CompressionType myCompressionHint;

    /** Image which I provide. */
    private volatile Image myContent;

    /**
     * The unprocessed image if there is an image processor. This is to avoid
     * having to reload the image when we just want to re-process it.
     */
    private Image myUnprocessedContent;

    /** The optional ImageProcessor. */
    private final ImageProcessor myImageProcessor;

    /** The Load error. */
    private volatile boolean myLoadError;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /** The URL. */
    private final URL myURL;

    static
    {
        BufferedImage brokenImage;
        try
        {
            brokenImage = ImageIO.read(ourBrokenImageURL);
        }
        catch (IOException e)
        {
            brokenImage = null;
            LOGGER.error("Failed to load broken image from url: " + ourBrokenImageURL, e);
        }
        ourBrokenImage = new ImageIOImage(brokenImage);
    }

    /**
     * Constructor.
     *
     * @param url String that describes image file that is in resource path.
     * @param compressionHint Compression to use for the image.
     * @param imageProcessor the optional image processor
     * @param toolbox The system toolbox.
     */
    public IconImageProvider(URL url, Image.CompressionType compressionHint, ImageProcessor imageProcessor, Toolbox toolbox)
    {
        myURL = url;
        myCompressionHint = compressionHint;
        myImageProcessor = imageProcessor;
        myToolbox = toolbox;
    }

    /**
     * Instantiates a new icon image provider.
     *
     * @param imageURL the image url
     * @param imageProcessor the optional image processor
     * @param toolbox The system toolbox.
     */
    public IconImageProvider(URL imageURL, ImageProcessor imageProcessor, Toolbox toolbox)
    {
        this(imageURL, DEFAULT_COMPRESSION_HINT, imageProcessor, toolbox);
    }

    @Override
    public boolean canProvideImageImmediately()
    {
        return myContent != null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        IconImageProvider other = (IconImageProvider)obj;
        String urlStr1 = myURL == null ? null : myURL.toString();
        String urlStr2 = other.myURL == null ? null : other.myURL.toString();
        if (!Objects.equals(urlStr1, urlStr2))
        {
            return false;
        }
        return Objects.equals(myImageProcessor, other.myImageProcessor);
    }

    @Override
    public Image getImage(Void key)
    {
        Image content;
        do
        {
            content = CONTENT_UPDATER.getAndSet(this, null);
            if (content == null)
            {
                loadImage();
            }
        }
        while (content == null);
        return content;
    }

    /**
     * Gets the uRL.
     *
     * @return the uRL
     */
    public URL getURL()
    {
        return myURL;
    }

    /**
     * Had load error.
     *
     * @return true, if successful
     */
    public boolean hadLoadError()
    {
        return myLoadError;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myURL == null ? 0 : myURL.toString().hashCode());
        result = prime * result + HashCodeHelper.getHashCode(myImageProcessor);
        return result;
    }

    /**
     * Load image.
     */
    public synchronized void loadImage()
    {
        // If another thread already loaded the image, just return.
        if ((myContent != null || myLoadError) && myImageProcessor == null)
        {
            return;
        }

        Image content = null;

        // If there's an image processor, just re-process the image
        if (myImageProcessor != null && myUnprocessedContent != null)
        {
            content = processImage(myUnprocessedContent);
            content.setCompressionHint(myCompressionHint);
            myLoadError = false;
            myContent = content;
        }
        else
        {
            boolean loadError = true;

            ResponseValues response = new ResponseValues();
            try (InputStream stream = getStream(response))
            {
                if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    try
                    {
                        content = Image.read(stream);
                        if (content == null)
                        {
                            LOGGER.error("Failed to load image at url: " + myURL);
                        }
                        else
                        {
                            content = processImage(content);
                            content.setCompressionHint(myCompressionHint);
                            loadError = false;
                        }
                    }
                    catch (IOException | ImageFormatUnknownException ex)
                    {
                        LOGGER.error("Failed to generate image. " + ex.getMessage());
                        LOGGER.debug("Failed to generate image.", ex);
                    }
                }
                else
                {
                    LOGGER.error("Server returned response " + response.getResponseCode() + " for url " + myURL);
                }
            }
            catch (IOException | GeneralSecurityException | URISyntaxException ex)
            {
                LOGGER.error("Failed to open stream for image URL: " + myURL + ": " + ex);
            }
            finally
            {
                if (loadError)
                {
                    content = ourBrokenImage;
                }
                myLoadError = false;
                myContent = content;
            }
        }
    }

    /**
     * Processes the image if there is an image processor.
     *
     * @param content the image
     * @return the processed image
     */
    private Image processImage(Image content)
    {
        if (myImageProcessor != null)
        {
            myUnprocessedContent = content;
            if (content instanceof ImageIOImage)
            {
                BufferedImage bufferedImage = ((ImageIOImage)content).getAWTImage();
                if (bufferedImage == null)
                {
                    throw new IllegalArgumentException("Unable to load image at url: " + myURL);
                }

                bufferedImage = myImageProcessor.process(bufferedImage);
                content = new ImageIOImage(bufferedImage);
            }
            else
            {
                LOGGER.warn("Image is not an ImageIOImage; image processing is disabled for URL " + myURL);
            }
        }
        return content;
    }

    /**
     * Gets the stream for the image.
     *
     * @param response The response.
     * @return The image stream.
     * @throws GeneralSecurityException If the user's certs failed to retrieve.
     * @throws IOException If the there were issues connecting to the server.
     * @throws URISyntaxException If the url could not be converted to a URI.
     */
    private InputStream getStream(ResponseValues response) throws GeneralSecurityException, IOException, URISyntaxException
    {
        InputStream stream = null;
        if (UrlUtilities.isFile(myURL))
        {
            stream = IOUtilities.getInputStream(myURL);
            response.setResponseCode(HttpURLConnection.HTTP_OK);
        }
        else
        {
            ServerProvider<HttpServer> provider = myToolbox.getServerProviderRegistry().getProvider(HttpServer.class);
            HttpServer server = provider.getServer(myURL);
            stream = server.sendGet(myURL, response);
        }

        return stream;
    }
}
