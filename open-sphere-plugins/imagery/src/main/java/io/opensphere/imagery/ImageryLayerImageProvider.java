package io.opensphere.imagery;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.imagery.ImageryEnvoy.TileQuery;

/** Facility that retrieves an image that matches a bounding box. */
public class ImageryLayerImageProvider implements ImageProvider<ImageryImageKey>, Serializable
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ImageryLayerImageProvider.class);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The Layer key. */
    private final String myLayerKey;

    /** The Layer name. */
    private final String myLayerName;

    /** The toolbox. */
    private transient Toolbox myToolbox;

    /**
     * Construct the texture provider.
     *
     * @param layerConfig The layer.
     * @param toolbox The toolbox.
     */
    public ImageryLayerImageProvider(ImageryFileSource layerConfig, Toolbox toolbox)
    {
        myLayerName = layerConfig.getName();
        myLayerKey = layerConfig.generateTypeKey();
        myToolbox = toolbox;
    }

    @Override
    public Image getImage(ImageryImageKey imageKey)
    {
        if (myToolbox == null)
        {
            return null;
        }

        // Query the image from the registry.
        TileQuery query = new TileQuery(toString(), myLayerKey, imageKey);
        myToolbox.getDataRegistry().performQuery(query).logException();
        List<Image> values = query.getResults();
        boolean retrieved = !values.isEmpty() && values.get(0) != null;
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Attempt to retrieve imagery tile from cache [" + (retrieved ? "HIT" : "MISS") + "] Layer[" + myLayerKey
                    + "] Image[" + imageKey + "]");
        }
        if (retrieved)
        {
            return values.get(0);
        }
        return null;
    }

    /**
     * Set the toolbox.
     *
     * @param toolbox The toolbox.
     */
    public void setToolbox(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public String toString()
    {
        String layerName = myLayerName;
        return new StringBuilder(128).append(ImageryLayerImageProvider.class.getSimpleName()).append(',').append(layerName)
                .toString();
    }
}
