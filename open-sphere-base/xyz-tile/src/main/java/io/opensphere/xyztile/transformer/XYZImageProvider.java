package io.opensphere.xyztile.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import io.opensphere.auxiliary.cache.jdbc.CustomObjectInputStream;
import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.image.StreamingDDSImage;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap.Factory;
import io.opensphere.core.util.collections.MappedObjectPool;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.xyztile.model.XYZTileLayerInfo;
import io.opensphere.xyztile.util.XYZTileUtils;

/**
 * An {@link ImageProvider} that gets tile images for xyz tiles.
 */
public class XYZImageProvider implements ImageProvider<ZYXImageKey>
{
    /** Procrastinating executor used to clean up the byte buffer pool. */
    private static final Executor CLEANUP_EXECUTOR = CommonTimer.createProcrastinatingExecutor(10000, 30000);

    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(XYZImageProvider.class);

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
     * The layer we are providing images for.
     */
    private final XYZTileLayerInfo myLayer;

    /**
     * Used to get the image.
     */
    private final DataRegistry myRegistry;

    /** The optional class provider. */
    private final ClassProvider myClassProvider;

    /**
     * Constructs a new Mapbox image provider.
     *
     * @param registry Used to get the image.
     * @param layer The layer we are providing images for.
     */
    public XYZImageProvider(DataRegistry registry, XYZTileLayerInfo layer)
    {
        myRegistry = registry;
        myLayer = layer;
        myClassProvider = CollectionUtilities.getItemOrNull(ServiceLoader.load(ClassProvider.class), 0);
    }

    @Override
    public Image getImage(ZYXImageKey key)
    {
        DataModelCategory imageCategory = new DataModelCategory(myLayer.getServerUrl(), XYZTileUtils.TILES_FAMILY,
                myLayer.getName());
        List<PropertyMatcher<?>> matchers = New.list();
        ZYXKeyPropertyMatcher keyMatcher = new ZYXKeyPropertyMatcher(XYZTileUtils.KEY_PROPERTY_DESCRIPTOR, key);
        matchers.add(keyMatcher);
        SimpleQuery<InputStream> query = new SimpleQuery<>(imageCategory, XYZTileUtils.IMAGE_PROPERTY_DESCRIPTOR, matchers);

        Image image = null;

        QueryTracker tracker = myRegistry.performQuery(query);

        if (query.getResults() != null && !query.getResults().isEmpty())
        {
            try
            {
                InputStream stream = query.getResults().get(0);

                StreamingDDSImage.setThreadByteBufferPool(ourByteBufferPool);
                try (ObjectInputStream ois = new CustomObjectInputStream(myClassProvider, stream))
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

        return image;
    }
}
