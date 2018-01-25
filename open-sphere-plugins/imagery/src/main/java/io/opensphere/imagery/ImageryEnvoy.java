package io.opensphere.imagery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.InputStreamAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultPropertyValueReceiver;
import io.opensphere.core.data.util.DefaultQuery;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.data.util.SimpleIdQuery;
import io.opensphere.core.image.DDSEncodableImage;
import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Serialization;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.imagery.util.ImageryTileLoadTracker;

/**
 * The Class ImageryEnvoy.
 */
@SuppressWarnings("PMD.GodClass")
public class ImageryEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /** Property descriptor for images used in the data registry. */
    private static final PropertyDescriptor<InputStream> IMAGE_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("value",
            InputStream.class, 262435L);

    /** Property descriptor for keys used in the data registry. */
    private static final PropertyDescriptor<String> KEY_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("key", String.class);

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ImageryEnvoy.class);

    /**
     * The amount of time in nanoseconds before a warning is issued to the
     * logger.
     */
    private static final long WARNING_TIME_NANOS = Integer.getInteger("opensphere.wms.warningTimeMilliseconds", 5000).longValue()
            * Constants.NANO_PER_MILLI;

    /** The Imagery source group. */
    private final ImagerySourceGroup myImagerySourceGroup;

    /** A map of layer names to layers. */
    private final Map<String, ImageryLayer> myLayerMap = New.map();

    /** The Layer map lock. */
    private final ReentrantLock myLayerMapLock = new ReentrantLock();

    /** The Metrics tracker. */
    private final ImageryTileLoadTracker myMetricsTracker;

    /**
     * Instantiates a new imagery envoy.
     *
     * @param toolbox the toolbox
     * @param metricsTracker the metrics tracker
     * @param sourceGroup the source group
     */
    public ImageryEnvoy(Toolbox toolbox, ImageryTileLoadTracker metricsTracker, ImagerySourceGroup sourceGroup)
    {
        super(toolbox);
        myImagerySourceGroup = sourceGroup;
        myMetricsTracker = metricsTracker;
    }

    /**
     * Clear image cache.
     */
    public void clearImageCache()
    {
        final Set<String> layerMapKeySet = getLayerKeySet();
        clearImageCache(layerMapKeySet);
    }

    /**
     * Clear image cache.
     *
     * @param layerKeys the layer keys
     */
    public void clearImageCache(Collection<String> layerKeys)
    {
        if (!CollectionUtilities.hasContent(layerKeys))
        {
            return;
        }

        myLayerMapLock.lock();
        try
        {
            for (final String key : layerKeys)
            {
                final ImageryLayer layer = deactivateLayer(key);
                if (layer != null)
                {
                    final DataModelCategory dmc = new DataModelCategory(null, Image.class.getName(), key);
                    final long[] modelsRemoved = getDataRegistry().removeModels(dmc, false);
                    if (modelsRemoved != null && LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Cleaned " + modelsRemoved.length + " items for layer " + key);
                    }
                    activateLayer(layer.getTypeInfo());
                }
            }
        }
        finally
        {
            myLayerMapLock.unlock();
        }
    }

    @Override
    public void close()
    {
        final Set<String> layerKeys = getLayerKeySet();
        for (final String key : layerKeys)
        {
            deactivateLayer(key);
        }

        super.close();
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        final Collection<Satisfaction> results = New.collection(intervalSets.size());
        for (final IntervalPropertyValueSet set : intervalSets)
        {
            results.add(new SingleSatisfaction(set));
        }
        return results;
    }

    @Override
    public String getThreadPoolName()
    {
        return getClass().getSimpleName();
    }

    /**
     * Load source.
     *
     * @throws InterruptedException If the thread is interrupted.
     */
    public void loadSource() throws InterruptedException
    {
        for (final ImageryFileSource src : myImagerySourceGroup.getImageSources())
        {
            ThreadControl.check();
            if (src.isEnabled())
            {
                activateLayer((ImageryDataTypeInfo)src.getDataTypeInfo());
            }
        }
    }

    @Override
    public void open()
    {
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        final ImageryLayerKey key = new ImageryLayerKey(category.getCategory());
        return (category.getSource() == null || category.getSource().startsWith(ImageryLayerImageProvider.class.getSimpleName()))
                && category.getFamily().equals(Image.class.getName()) && key.getGroupName() != null
                && key.getGroupName().equals(myImagerySourceGroup.getName()) && hasLayerKeyInLayerMap(key.getLayerName());
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
                throws InterruptedException, QueryException
    {
        final ImageryLayerKey key = new ImageryLayerKey(category.getCategory());
        final ImageryLayer layer = getLayerWithLayerKey(key.getLayerName());

        if (layer == null)
        {
            LOGGER.warn("Layer not found in layer map when performing query : " + category.getCategory());
            return;
        }
        if (CollectionUtilities.hasContent(satisfactions))
        {
            throw new IllegalArgumentException("Satisfactions are not supported.");
        }
        if (parameters.size() != 1 || !(parameters.get(0) instanceof ImageryImageKeyPropertyMatcher))
        {
            throw new IllegalArgumentException(
                    ImageryImageKeyPropertyMatcher.class.getSimpleName() + " was not found in parameters.");
        }
        final ImageryImageKeyPropertyMatcher param = (ImageryImageKeyPropertyMatcher)parameters.get(0);

        boolean error = true;
        try
        {
            myMetricsTracker.queryStarted();
            DDSEncodableImage imageFromServer = null;
            imageFromServer = ImageryEnvoyHelper.getImageFromSource(layer.getTypeInfo(), param.getImageKey());

            if (imageFromServer != null)
            {
                final Image image = convertImageToDDS(imageFromServer);

                try
                {
                    final Collection<PropertyAccessor<Image, ?>> accessors = New.collection();
                    accessors.add(new SerializableAccessor<Image, String>(KEY_PROPERTY_DESCRIPTOR)
                    {
                        @Override
                        public String access(Image input)
                        {
                            return param.getOperand();
                        }
                    });
                    accessors.add(new InputStreamAccessor<Image>(IMAGE_PROPERTY_DESCRIPTOR)
                    {
                        @Override
                        public InputStream access(Image input)
                        {
                            try
                            {
                                return new ByteArrayInputStream(Serialization.serialize(input));
                            }
                            catch (final IOException e)
                            {
                                LOGGER.error("Failed to serialize image: " + e, e);
                                return null;
                            }
                        }
                    });
                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug("Read image and depositing sending to query receiver: Category: " + category);
                    }
                    final Date expiration = new Date(System.currentTimeMillis() + Constants.MILLIS_PER_WEEK);
                    final CacheDeposit<Image> deposit = new DefaultCacheDeposit<>(category, accessors,
                            Collections.singleton(image), true, expiration, false);
                    queryReceiver.receive(deposit);
                    error = false;
                }
                catch (final CacheException e)
                {
                    LOGGER.warn("Failed to deposit image: " + e, e);
                }
            }
        }
        catch (final IOException e)
        {
            LOGGER.error("Failed to convert image for imageKey [" + param.getImageKey() + "] to DDS:" + e, e);
        }
        finally
        {
            if (error)
            {
                myMetricsTracker.queryError();
            }
            myMetricsTracker.queryEnded();
        }
    }

    @Override
    public void setFilter(Object filter)
    {
    }

    /**
     * Send the WMS layers to the data registry.
     *
     * @param layers The WMS layers.
     */
    protected void sendLayersToDataRegistry(Collection<? extends ImageryLayer> layers)
    {
        final String source = toString();
        final String family = ImageryLayer.class.getName();
        final Date expiration = CacheDeposit.SESSION_END;

        final Collection<PropertyAccessor<ImageryLayer, ?>> accessors = New.collection();
        accessors.add(UnserializableAccessor.getHomogeneousAccessor(ImageryLayer.PROPERTY_DESCRIPTOR));
        accessors.add(new SerializableAccessor<ImageryLayer, String>(KEY_PROPERTY_DESCRIPTOR)
        {
            @Override
            public String access(ImageryLayer input)
            {
                return input.getTypeInfo().getDisplayName();
            }
        });
        for (final ImageryLayer layer : layers)
        {
            final String category = layer.getTypeInfo().getDisplayName();
            final CacheDeposit<ImageryLayer> deposit = new DefaultCacheDeposit<>(
                    new DataModelCategory(source, family, category), accessors, Collections.singleton(layer), true, expiration,
                    true);
            getDataRegistry().addModels(deposit);
        }
    }

    /**
     * Active a layer based on the type info.
     *
     * @param typeInfo The type info of the layer being activated.
     */
    private void activateLayer(ImageryDataTypeInfo typeInfo)
    {
        final ImageryLayer layer = createLayer(typeInfo);
        myLayerMapLock.lock();
        try
        {
            myLayerMap.put(typeInfo.getTypeKey(), layer);
        }
        finally
        {
            myLayerMapLock.unlock();
        }
        sendLayersToDataRegistry(Collections.singleton(layer));
    }

    /**
     * Convert an image to DDS.
     *
     * @param image The input image.
     * @return The DDS image.
     * @throws IOException If the image cannot be converted.
     */
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    private Image convertImageToDDS(DDSEncodableImage image) throws IOException
    {
        final long t0 = System.nanoTime();
        final Image dds = image.asDDSImage();
        final long t1 = System.nanoTime();
        if (t1 - t0 > WARNING_TIME_NANOS)
        {
            LOGGER.warn(StringUtilities.formatTimingMessage("Transcoding image to DDS took ", t1 - t0));
        }
        else
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Transcoding image to DDS took ", t1 - t0));
            }
        }
        return dds;
    }

    /**
     * Create the layer model.
     *
     * @param info The DataTypeInfo for the WMS layer to be created.
     * @return A layer model.
     */
    private ImageryLayer createLayer(ImageryDataTypeInfo info)
    {
        final int resolveLevels = info.getImageryFileSource().getLevels();
        final ImageryLayer.Builder builder = new ImageryLayer.Builder(info);
        final GeographicBoundingBox gbb = info.getImageryFileSource().getBoundingBox();
        final double levelWidth = gbb.getDeltaLonD();
        builder.setLayerCellDimensions(ImageryLayer.createQuadSplitLayerCellDimensions(resolveLevels, levelWidth));
        builder.setImageProvider(new ImageryLayerImageProvider(info.getImageryFileSource(), getToolbox()));
        final ImageryLayer layer = new ImageryLayer(builder, getToolbox());
        return layer;
    }

    /**
     * Remove a layer model.
     *
     * @param layerTypeKey the layer name
     * @return the imagery layer which was deactivated.
     */
    private ImageryLayer deactivateLayer(String layerTypeKey)
    {
        // remove the layer from my list
        ImageryLayer layer = null;
        myLayerMapLock.lock();
        try
        {
            layer = myLayerMap.remove(layerTypeKey);
            if (layer != null)
            {
                // remove the layer from the data registry
                layer.deactivate();
                final String source = toString();
                final String key = layer.getTitle();
                final DataModelCategory dmc = new DataModelCategory(source, ImageryLayer.class.getName(), null);
                final Query query = new SimpleIdQuery(dmc, key, KEY_PROPERTY_DESCRIPTOR);
                final long[] ids = getDataRegistry().performLocalQuery(query);
                getDataRegistry().removeModels(ids);
            }
        }
        finally
        {
            myLayerMapLock.unlock();
        }

        return layer;
    }

    /**
     * Gets the layer key set.
     *
     * @return the layer key set
     */
    private Set<String> getLayerKeySet()
    {
        Set<String> layerMapKeySet = null;
        myLayerMapLock.lock();
        try
        {
            layerMapKeySet = New.set(myLayerMap.keySet());
        }
        finally
        {
            myLayerMapLock.unlock();
        }
        return layerMapKeySet;
    }

    /**
     * Gets the layer with layer key.
     *
     * @param layerKey the layer key
     * @return the layer with layer key
     */
    private ImageryLayer getLayerWithLayerKey(String layerKey)
    {
        ImageryLayer layer = null;
        myLayerMapLock.lock();
        try
        {
            layer = myLayerMap.get(layerKey);
        }
        finally
        {
            myLayerMapLock.unlock();
        }
        return layer;
    }

    /**
     * Checks for layer key in layer map.
     *
     * @param layerKey the layer key
     * @return true, if successful
     */
    private boolean hasLayerKeyInLayerMap(String layerKey)
    {
        boolean hasIt = false;
        myLayerMapLock.lock();
        try
        {
            hasIt = myLayerMap.containsKey(layerKey);
        }
        finally
        {
            myLayerMapLock.unlock();
        }
        return hasIt;
    }

    /**
     * Customized key-value query for WMS tiles.
     */
    protected static class TileQuery extends DefaultQuery
    {
        /**
         * Build the data model category for the query.
         *
         * @param source The source of the query.
         * @param layerKey the layer key
         * @return The data model category.
         */
        private static DataModelCategory buildCategory(String source, String layerKey)
        {
            return new DataModelCategory(source, Image.class.getName(), layerKey);
        }

        /**
         * Build the parameters for the query.
         *
         * @param imageKey The key for the image.
         * @return The list of query parameters.
         */
        private static List<PropertyMatcher<String>> buildParameters(ImageryImageKey imageKey)
        {
            return Collections.<PropertyMatcher<String>>singletonList(new ImageryImageKeyPropertyMatcher(imageKey));
        }

        /**
         * Constructor.
         *
         * @param source The source of the query.
         * @param layerKey the layer key
         * @param imageKey The key for the image.
         */
        public TileQuery(String source, String layerKey, ImageryImageKey imageKey)
        {
            super(buildCategory(source, layerKey),
                    Collections.singletonList(new DefaultPropertyValueReceiver<>(IMAGE_PROPERTY_DESCRIPTOR)),
                    buildParameters(imageKey), Collections.<OrderSpecifier>emptyList());
        }

        /**
         * Get the results of the query.
         *
         * @return The results.
         */
        public List<Image> getResults()
        {
            @SuppressWarnings("unchecked")
            final
            DefaultPropertyValueReceiver<InputStream> receiver = (DefaultPropertyValueReceiver<InputStream>)getPropertyValueReceivers()
                    .get(0);
            final List<Image> values = New.list(receiver.getValues().size());
            for (final InputStream inputStream : receiver.getValues())
            {
                ObjectInputStream ois;
                try
                {
                    ois = new ObjectInputStream(inputStream);
                    try
                    {
                        values.add((Image)ois.readObject());
                    }
                    catch (IOException | ClassNotFoundException e)
                    {
                        LOGGER.error("Failed to deserialize image: " + e, e);
                    }
                    finally
                    {
                        try
                        {
                            ois.close();
                        }
                        catch (final IOException e)
                        {
                            LOGGER.error("Failed to close stream: " + e, e);
                        }
                    }
                }
                catch (final IOException e)
                {
                    LOGGER.error("Failed to deserialize image: " + e, e);
                }
            }
            return values;
        }
    }

    /**
     * A property matcher that matches a {@link ImageryImageKey}.
     */
    private static class ImageryImageKeyPropertyMatcher extends GeneralPropertyMatcher<String>
    {
        /** The key for the image. */
        private final ImageryImageKey myImageKey;

        /**
         * Constructor.
         *
         * @param imageKey The key for the image.
         */
        public ImageryImageKeyPropertyMatcher(ImageryImageKey imageKey)
        {
            super(KEY_PROPERTY_DESCRIPTOR, imageKey.toString());
            myImageKey = imageKey;
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public boolean equals(Object obj)
        {
            return super.equals(obj);
        }

        /**
         * Accessor for the image key.
         *
         * @return The image key.
         */
        public ImageryImageKey getImageKey()
        {
            return myImageKey;
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public int hashCode()
        {
            return super.hashCode();
        }
    }
}
