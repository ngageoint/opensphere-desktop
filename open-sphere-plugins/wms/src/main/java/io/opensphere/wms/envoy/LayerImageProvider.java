package io.opensphere.wms.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultPropertyValueReceiver;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.PropertyValueReceiver;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.image.StreamingDDSImage;
import io.opensphere.core.util.collections.LazyMap.Factory;
import io.opensphere.core.util.collections.MappedObjectPool;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.wms.layer.TileImageKey;

/** Facility that retrieves an image that matches a {@link TileImageKey}. */
public class LayerImageProvider implements ImageProvider<TileImageKey>, Serializable
{
    /** Procrastinating executor used to clean up the byte buffer pool. */
    private static final Executor CLEANUP_EXECUTOR = CommonTimer.createProcrastinatingExecutor(10000, 30000);

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LayerImageProvider.class);

    /** Pool of byte buffers shared by all layer image providers. */
    private static final MappedObjectPool<Integer, ByteBuffer> ourByteBufferPool = new MappedObjectPool<Integer, ByteBuffer>(
            Integer.class, new Factory<Integer, ByteBuffer>()
            {
                @Override
                public ByteBuffer create(Integer capacity)
                {
                    return ByteBuffer.allocate(capacity.intValue());
                }
            }, 20, 20, CLEANUP_EXECUTOR);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The base url. */
    private final String myBaseURL;

    /** The data registry. */
    private final transient DataRegistry myDataRegistry;

    /** The layer key. */
    private final String myLayerKey;

    /** The layer name. */
    private final String myLayerName;

    /**
     * Constructor.
     *
     * @param layerName The layer name.
     * @param layerKey The layer key.
     * @param baseURL The base url.
     * @param dataRegistry The data registry.
     */
    public LayerImageProvider(String layerName, String layerKey, String baseURL, DataRegistry dataRegistry)
    {
        myLayerName = layerName;
        myLayerKey = layerKey;
        myBaseURL = baseURL;
        myDataRegistry = dataRegistry;
    }

    /**
     * Get the base url.
     *
     * @return The base url.
     */
    public String getBaseURL()
    {
        return myBaseURL;
    }

    /**
     * Get the dataRegistry.
     *
     * @return the dataRegistry
     */
    public DataRegistry getDataRegistry()
    {
        return myDataRegistry;
    }

    /**
     * Get the image.
     *
     * @param imageKey The image key.
     * @return The image.
     */
    @Override
    public Image getImage(TileImageKey imageKey)
    {
        if (myDataRegistry == null)
        {
            return null;
        }

        StreamingDDSImage.setThreadByteBufferPool(ourByteBufferPool);
        try
        {
            // Query the image from the registry.
            TileQuery query = new TileQuery(toString(), myLayerKey, imageKey);
            myDataRegistry.performQuery(query).logException();
            List<Image> values = query.getResults();
            if (values.isEmpty())
            {
                return null;
            }
            else
            {
                return values.get(0);
            }
        }
        finally
        {
            StreamingDDSImage.setThreadByteBufferPool(null);
        }
    }

    /**
     * Get the layer key.
     *
     * @return The layer key.
     */
    public String getLayerKey()
    {
        return myLayerKey;
    }

    /**
     * Get the layer name.
     *
     * @return The layer name.
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString()
    {
        String layerName = myLayerName;
        String url = myBaseURL;
        return new StringBuilder(128).append(LayerImageProvider.class.getSimpleName()).append(',').append(url).append(',')
                .append(layerName).toString();
    }

    /**
     * Abstract functionality for tile queries.
     */
    protected abstract static class BaseTileQuery extends DefaultQuery
    {
        /**
         * Build the data model category for the query.
         *
         * @param source The source of the query.
         * @param imageFamily the family name for the image, typically the image
         *            type.
         * @param layerKey The WMS layer key.
         * @return The data model category.
         */
        private static DataModelCategory buildCategory(String source, String imageFamily, String layerKey)
        {
            return new DataModelCategory(source, imageFamily, layerKey);
        }

        /**
         * Build the parameters for the query.
         *
         * @param imageKey The key for the image.
         * @return The list of query parameters.
         */
        private static List<PropertyMatcher<String>> buildParameters(TileImageKey imageKey)
        {
            return Collections.<PropertyMatcher<String>>singletonList(new WMSGetMapEnvoy.KeyPropertyMatcher(imageKey));
        }

        /**
         * Constructor.
         *
         * @param source The source of the query.
         * @param imageFamily The family for the category, typically the image
         *            type name.
         * @param layerKey The layer key.
         * @param imageKey The key for the image.
         * @param propertyValueReceivers The objects that define what properties
         *            are desired and also accept the results.
         */
        public BaseTileQuery(String source, String imageFamily, String layerKey, TileImageKey imageKey,
                List<? extends PropertyValueReceiver<?>> propertyValueReceivers)
        {
            super(buildCategory(source, imageFamily, layerKey), propertyValueReceivers, buildParameters(imageKey),
                    Collections.<OrderSpecifier>emptyList());
        }
    }

    /**
     * Customized key-value query for tiles.
     */
    protected static class TileQuery extends BaseTileQuery
    {
        /**
         * Constructor.
         *
         * @param source The source of the query.
         * @param layerKey The layer key.
         * @param imageKey The key for the image.
         */
        public TileQuery(String source, String layerKey, TileImageKey imageKey)
        {
            super(source, Image.class.getName(), layerKey, imageKey, Collections
                    .singletonList(new DefaultPropertyValueReceiver<InputStream>(WMSGetMapEnvoy.IMAGE_PROPERTY_DESCRIPTOR)));
        }

        /**
         * Get the results of the query.
         *
         * @return The results.
         */
        public List<Image> getResults()
        {
            @SuppressWarnings("unchecked")
            DefaultPropertyValueReceiver<InputStream> receiver = (DefaultPropertyValueReceiver<InputStream>)getPropertyValueReceivers()
                    .get(0);
            List<Image> values = New.list(receiver.getValues().size());
            if (values.size() > 1)
            {
                LOGGER.warn("Multiple images (" + values.size() + ") returned for tile key.");
            }
            for (InputStream inputStream : receiver.getValues())
            {
                try
                {
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    try
                    {
                        values.add((Image)ois.readObject());
                    }
                    catch (ClosedByInterruptException e)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug("Request for image was interrupted: " + e, e);
                        }
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("Failed to read stream for image: " + e, e);
                    }
                    catch (ClassNotFoundException e)
                    {
                        LOGGER.error("Failed to deserialize image: " + e, e);
                    }
                    finally
                    {
                        try
                        {
                            ois.close();
                        }
                        catch (IOException e)
                        {
                            LOGGER.error("Failed to close stream: " + e, e);
                        }
                    }
                }
                catch (StreamCorruptedException e)
                {
                    LOGGER.error("Stop at " + getDataModelCategory().getCategory() + ": " + e);
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to deserialize image: " + e, e);
                }
            }
            return values;
        }
    }
}
