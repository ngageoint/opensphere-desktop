package io.opensphere.core.pipeline.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.ImageGroup;
import io.opensphere.core.geometry.ImageGroupProvider;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImmediateImageProvider;
import io.opensphere.core.image.ObservableImageProvider;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.WeakChangeSupport;

/**
 * Provide the texture which was generated as the texture for the tile.
 */
public class RenderToTextureImageProvider
implements ImageGroupProvider<Void>, ImmediateImageProvider<Void>, ObservableImageProvider<Void>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(RenderToTextureImageProvider.class);

    /** The change support. */
    private final ChangeSupport<ObservableImageProvider.Observer> myChangeSupport = new WeakChangeSupport<>();

    /** The image for my Tile to render. */
    private ImageGroup myImageGroup;

    /** RenderToTexture mapping to the render mode. */
    private final Map<RenderMode, RenderToTexture> myRendToTexures = new EnumMap<>(RenderMode.class);

    /**
     * Listener for repainting when a texture is requested and needs to be
     * regenerated.
     */
    private RepaintListener myRepaintListener;

    @Override
    public void addObserver(ObservableImageProvider.Observer observer)
    {
        myChangeSupport.addListener(observer);
    }

    @Override
    public boolean canProvideImageImmediately()
    {
        return isReady();
    }

    @Override
    public Image getImage(Void key)
    {
        throw new UnsupportedOperationException(
                "The getImage returning a single image cannot be used for an ImageGroupProvider.");
    }

    @Override
    public ImageGroup getImages(Void key)
    {
        synchronized (this)
        {
            if (isReady())
            {
                ImageGroup retGroup = myImageGroup;
                myImageGroup = null;
                return retGroup;
            }
            if (myRepaintListener != null)
            {
                myRepaintListener.repaint();
            }
            return null;
        }
    }

    /**
     * Get the rendToTexures.
     *
     * @return the rendToTexures
     */
    public Map<RenderMode, RenderToTexture> getRendToTexures()
    {
        return myRendToTexures;
    }

    /**
     * Tell whether I can return all of the images required for rendering to
     * texture.
     *
     * @return true when all images are available.
     */
    public boolean isReady()
    {
        synchronized (this)
        {
            return myImageGroup != null && myImageGroup.getImageMap().get(AbstractGeometry.RenderMode.DRAW) != null
                    && myImageGroup.getImageMap().get(RenderMode.PICK) != null;
        }
    }

    /**
     * Clean up all of my images.
     */
    public void resetImages()
    {
        synchronized (this)
        {
            myImageGroup = null;
        }
    }

    /**
     * Set the image.
     *
     * @param image The image to set.
     * @param mode The render mode associated with the image.
     */
    public void setImage(Image image, RenderMode mode)
    {
        synchronized (this)
        {
            Map<RenderMode, Image> imageMap;
            if (myImageGroup == null)
            {
                imageMap = Collections.singletonMap(mode, image);
            }
            else
            {
                imageMap = new EnumMap<>(myImageGroup.getImageMap());
                imageMap.put(mode, image);
            }
            myImageGroup = new ImageGroup(imageMap);
        }
        if (isReady())
        {
            myChangeSupport.notifyListeners(listener -> listener.dataReady());
        }
    }

    /**
     * Add an item to my render to texture map.
     *
     * @param mode render mode.
     * @param rendToTex Render to texture.
     */
    public void setRenderToTexture(RenderMode mode, RenderToTexture rendToTex)
    {
        RenderToTexture oldRend = myRendToTexures.put(mode, rendToTex);
        if (oldRend != null)
        {
            LOGGER.warn("Replaced RenderToTexture without deleting the buffers");
        }
    }

    /**
     * Set the repaint listener.
     *
     * @param repaintListener Listener for repainting when a texture is
     *            requested and needs to be regenerated.
     */
    public void setRepaintListener(RepaintListener repaintListener)
    {
        synchronized (this)
        {
            myRepaintListener = repaintListener;
        }
    }
}
