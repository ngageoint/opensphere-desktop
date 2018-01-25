package io.opensphere.wms.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DefaultPropertyValueReceiver;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageStreamProvider;
import io.opensphere.core.image.StreamingImage;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.wms.layer.TileImageKey;

/**
 * Facility that retrieves an input stream backed by an image that matches a
 * {@link TileImageKey}.
 */
public class LayerStreamingImageProvider extends LayerImageProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LayerStreamingImageProvider.class);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The image height. */
    private final int myImageHeight;

    /** The image width. */
    private final int myImageWidth;

    /**
     * Constructor.
     *
     * @param layerName The layer name.
     * @param layerKey The layer key.
     * @param baseURL The base url.
     * @param imageHeight The image height.
     * @param imageWidth The image width.
     * @param dataRegistry The data registry.
     */
    public LayerStreamingImageProvider(String layerName, String layerKey, String baseURL, int imageHeight, int imageWidth,
            DataRegistry dataRegistry)
    {
        super(layerName, layerKey, baseURL, dataRegistry);
        myImageHeight = imageHeight;
        myImageWidth = imageWidth;
    }

    @Override
    public Image getImage(TileImageKey imageKey)
    {
        if (getDataRegistry() == null)
        {
            return null;
        }

        ImageStreamExistenceQuery query = new ImageStreamExistenceQuery(toString(), getLayerKey(), imageKey);
        QueryTracker tracker = getDataRegistry().performQuery(query);
        if (tracker.getIds().length != 0)
        {
            LayerImageStreamProvider streamProvider = new LayerImageStreamProvider(getLayerKey(), getDataRegistry(), toString());
            return new StreamingImage<TileImageKey>(streamProvider, imageKey, myImageWidth, myImageHeight);
        }
        else if (!ThreadControl.isThreadCancelled())
        {
            tracker.logException();
            LOGGER.warn("No data returned for layer [" + getLayerKey() + "] image key [" + imageKey + "]");
        }

        return null;
    }

    @Override
    public String toString()
    {
        String layerName = getLayerName();
        String url = getBaseURL();
        return new StringBuilder(128).append(LayerStreamingImageProvider.class.getSimpleName()).append(',').append(url)
                .append(',').append(layerName).toString();
    }

    /**
     * A service that provides stream which is backed by an image matching the
     * key.
     */
    public static class LayerImageStreamProvider implements ImageStreamProvider<TileImageKey>
    {
        /**
         * The registry used for managing caching and persisting of the image.
         */
        private final DataRegistry myDataRegistry;

        /** The data category family to which this image belongs. */
        private final String myImageFamily;

        /** The layer key. */
        private final String myLayerKey;

        /**
         * Constructor.
         *
         * @param layerKey The key for the layer to which the image belongs.
         * @param dataRegistry The registry used for managing caching and
         *            persisting of the image.
         * @param imageFamily The data category family to which this image
         *            belongs.
         */
        public LayerImageStreamProvider(String layerKey, DataRegistry dataRegistry, String imageFamily)
        {
            myLayerKey = layerKey;
            myDataRegistry = dataRegistry;
            myImageFamily = imageFamily;
        }

        @Override
        public InputStream getImageStream(TileImageKey key)
        {
            TileStreamQuery query = new TileStreamQuery(myImageFamily, myLayerKey, key);
            myDataRegistry.performLocalQuery(query);
            List<InputStream> results = query.getResults();
            if (results != null)
            {
                if (results.size() > 1)
                {
                    LOGGER.warn("Multiple images (" + results.size() + ") returned for tile key.");
                    for (int i = 1; i < results.size(); ++i)
                    {
                        try
                        {
                            results.get(i).close();
                        }
                        catch (IOException e)
                        {
                            LOGGER.error("Failed to close stream for extraneous image." + e, e);
                        }
                    }
                }
                return results.get(0);
            }
            return null;
        }
    }

    /**
     * A query used to determine whether an image stream is available without
     * retrieving the stream.
     */
    protected static class ImageStreamExistenceQuery extends BaseTileQuery
    {
        /**
         * Constructor.
         *
         * @param source The source of the query.
         * @param layerKey The layer key.
         * @param imageKey The key for the image.
         */
        public ImageStreamExistenceQuery(String source, String layerKey, TileImageKey imageKey)
        {
            super(source, StreamingImage.class.getName(), layerKey, imageKey, Collections.<PropertyValueReceiver<?>>emptyList());
        }
    }

    /**
     * Customized key-value query for WMS tiles.
     */
    protected static class TileStreamQuery extends BaseTileQuery
    {
        /**
         * Constructor.
         *
         * @param source The source of the query.
         * @param layerKey The layer key.
         * @param imageKey The key for the image.
         */
        public TileStreamQuery(String source, String layerKey, TileImageKey imageKey)
        {
            super(source, StreamingImage.class.getName(), layerKey, imageKey, Collections
                    .singletonList(new DefaultPropertyValueReceiver<InputStream>(WMSGetMapEnvoy.IMAGE_PROPERTY_DESCRIPTOR)));
        }

        /**
         * Get the results of the query.
         *
         * @return The results.
         */
        public List<InputStream> getResults()
        {
            @SuppressWarnings("unchecked")
            DefaultPropertyValueReceiver<InputStream> receiver = (DefaultPropertyValueReceiver<InputStream>)getPropertyValueReceivers()
                    .get(0);
            List<InputStream> results = receiver.getValues();
            if (!results.isEmpty())
            {
                return New.list(results);
            }
            return null;
        }
    }
}
