package io.opensphere.geopackage.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.image.ImageReader;
import io.opensphere.core.image.StreamingDDSImage;
import io.opensphere.core.image.StreamingImage;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.LazyMap.Factory;
import io.opensphere.core.util.collections.MappedObjectPool;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.geopackage.model.GeoPackagePropertyDescriptors;
import io.opensphere.geopackage.model.GeoPackageTile;
import io.opensphere.geopackage.model.GeoPackageTileLayer;
import io.opensphere.geopackage.util.Constants;

/**
 * The {@link ImageProvider} that can get an image for a {@link GeoPackageTile}.
 */
public class GeoPackageImageProvider implements ImageProvider<ZYXImageKey>
{
    /** Procrastinating executor used to clean up the byte buffer pool. */
    private static final Executor CLEANUP_EXECUTOR = CommonTimer.createProcrastinatingExecutor(10000, 30000);

    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(GeoPackageImageProvider.class);

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

    /**
     * If the geopackage layer has a terrain extension applied, this reader will
     * know how to read the terrain image.
     */
    private ImageReader myImageReader;

    /**
     * The layer we are providing images for.
     */
    private final GeoPackageTileLayer myLayer;

    /**
     * Used to get the image.
     */
    private final DataRegistry myRegistry;

    /**
     * Used to notify the user of the ongoing queries.
     */
    private final GeoPackageQueryTracker myTracker;

    /**
     * Constructs a new geopackage image provider.
     *
     * @param registry Used to get the image.
     * @param layer The layer we are providing images for.
     * @param queryTracker Used to notify the user of the ongoing queries.
     */
    public GeoPackageImageProvider(DataRegistry registry, GeoPackageTileLayer layer, GeoPackageQueryTracker queryTracker)
    {
        myRegistry = registry;
        myLayer = layer;
        myTracker = queryTracker;

        if (myLayer.getExtensions().containsKey(Constants.TERRAIN_EXTENSION))
        {
            String imageFormat = myLayer.getExtensions().get(Constants.TERRAIN_EXTENSION);
            ServiceLoader<ImageReader> loader = ServiceLoader.load(ImageReader.class);
            for (ImageReader reader : loader)
            {
                if (imageFormat.equals(reader.getImageFormat()))
                {
                    myImageReader = reader;
                    break;
                }
            }
        }
    }

    @Override
    public Image getImage(ZYXImageKey key)
    {
        DataModelCategory imageCategory = new DataModelCategory(myLayer.getPackageFile(), myLayer.getName(),
                Image.class.getName());
        List<PropertyMatcher<?>> matchers = New.list();
        ZYXKeyPropertyMatcher keyMatcher = new ZYXKeyPropertyMatcher(GeoPackagePropertyDescriptors.KEY_PROPERTY_DESCRIPTOR, key);
        matchers.add(keyMatcher);
        SimpleQuery<InputStream> query = new SimpleQuery<>(imageCategory, GeoPackagePropertyDescriptors.IMAGE_PROPERTY_DESCRIPTOR,
                matchers);

        Image image = null;
        myTracker.upCounter();

        try
        {
            QueryTracker tracker = myRegistry.performQuery(query);

            if (query.getResults() != null && !query.getResults().isEmpty())
            {
                try
                {
                    if (myImageReader == null)
                    {
                        InputStream stream = query.getResults().get(0);

                        if (StringUtils.isNotEmpty(myLayer.getExtensions().get(Constants.TERRAIN_EXTENSION)))
                        {
                            StreamReader reader = new StreamReader(stream);
                            ByteBuffer buffer = reader.readStreamIntoBuffer();
                            image = new StreamingImage<>(new GeoPackageStreamingImageProvider(buffer), key, 512, 512);
                        }
                        else
                        {
                            StreamingDDSImage.setThreadByteBufferPool(ourByteBufferPool);
                            try (ObjectInputStream ois = new ObjectInputStream(stream))
                            {
                                image = (Image)ois.readObject();
                            }
                            catch (ClassNotFoundException e)
                            {
                                LOGGER.error(e, e);
                            }
                            finally
                            {
                                StreamingDDSImage.setThreadByteBufferPool(null);
                            }
                        }
                    }
                    else
                    {
                        StreamReader reader = new StreamReader(query.getResults().get(0));
                        image = myImageReader.readImage(reader.readStreamIntoBuffer());
                    }
                }
                catch (ClosedByInterruptException e)
                {
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(e, e);
                    }
                }
                catch (IOException e)
                {
                    LOGGER.error(e, e);
                }
            }
            else if (tracker.getException() != null)
            {
                LOGGER.error(tracker.getException(), tracker.getException());
            }
        }
        finally
        {
            myTracker.subtractCounter();
        }

        return image;
    }
}
