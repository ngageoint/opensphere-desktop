package io.opensphere.wms.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.nio.channels.ClosedByInterruptException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

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
import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageFormatUnknownException;
import io.opensphere.core.image.StreamingImage;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Cancellable;
import io.opensphere.core.util.lang.ExceptionUtilities;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;
import io.opensphere.wms.config.v1.WMSLayerConfig;
import io.opensphere.wms.config.v1.WMSLayerConfig.LayerType;
import io.opensphere.wms.event.DefaultWmsConnectionParams;
import io.opensphere.wms.layer.TileImageKey;
import io.opensphere.wms.layer.WMSLayer;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.layer.WMSLayerValueProviderImpl;
import io.opensphere.wms.toolbox.WMSToolbox;
import io.opensphere.wms.util.WMSEnvoyHelper;
import io.opensphere.wms.util.WMSQueryTracker;

/**
 * Envoy that retrieves WMS layers from an OGC server.
 */
@SuppressWarnings("PMD.GodClass")
public class WMSGetMapEnvoy extends AbstractEnvoy implements DataRegistryDataProvider, WMSLayerEnvoy
{
    /** Property descriptor for images used in the data registry. */
    public static final PropertyDescriptor<InputStream> IMAGE_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("value",
            InputStream.class, 262435L);

    /**
     * The accessor for getting the input stream for the raw image bytes. The
     * raw bytes for the image may be used directly from this stream.
     */
    private static final InputStreamAccessor<InputStream> IMAGE_STREAM_ACCESSOR = InputStreamAccessor
            .getHomogeneousAccessor(IMAGE_PROPERTY_DESCRIPTOR);

    /** Property descriptor for keys used in the data registry. */
    private static final PropertyDescriptor<String> KEY_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("key", String.class);

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(WMSGetMapEnvoy.class);

    /**
     * The warning for incorrect BIL sizes should only be issued once. When this
     * is true, we have already issued the warning.
     */
    private static volatile boolean ourBILSizeWarningIssued;

    /** Used to ensure that the BIL size warning is only issued once. */
    private static Object ourBILWarningMonitor = new Object();

    /**
     * Indicates if this envoy has reported the error before.
     */
    private boolean myHasReportedError;

    /** The Metrics tracker. */
    private final WMSQueryTracker myMetricsTracker;

    /** The server connection configuration. */
    private final transient ServerConnectionParams myServerConnConfig;

    /** The WMS layer. */
    private final WMSLayer myWMSLayer;

    /**
     * The WMS version used in requests made by this envoy (either "1.1.1" or
     * "1.3.0").
     */
    private final String myWMSVersion;

    /**
     * Construct the envoy.
     *
     * @param layer The layer this envoy is for.
     * @param toolbox the toolbox used to retrieve dataRegistry, eventManager
     * @param metricsTracker the metrics tracker
     * @param servConf The server configuration.
     * @param wmsVersion The WMS version to be used in requests made by this
     *            envoy (either "1.1.1" or "1.3.0").
     */
    public WMSGetMapEnvoy(WMSLayer layer, Toolbox toolbox, WMSQueryTracker metricsTracker, ServerConnectionParams servConf,
            String wmsVersion)
    {
        super(toolbox);
        myHasReportedError = false;
        myWMSLayer = layer;
        myMetricsTracker = metricsTracker;
        myWMSVersion = wmsVersion;

        // Create a local copy of the connection parameters.
        myServerConnConfig = new DefaultWmsConnectionParams(servConf);
    }

    @Override
    public WMSLayerValueProvider getLayer()
    {
        return new WMSLayerValueProviderImpl(myWMSLayer);
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        Collection<Satisfaction> results = New.collection(intervalSets.size());
        for (final IntervalPropertyValueSet set : intervalSets)
        {
            results.add(new SingleSatisfaction(set));
        }
        return results;
    }

    @Override
    public String getThreadPoolName()
    {
        return "WMS: " + myServerConnConfig.getWmsUrl();
    }

    /**
     * Get the WMS layer associated with this envoy.
     *
     * @return The layer.
     */
    public WMSLayer getWMSLayer()
    {
        return myWMSLayer;
    }

    @Override
    public void open()
    {
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        if (category.getSource() == null)
        {
            return false;
        }

        boolean validImage = category.getSource().startsWith(LayerImageProvider.class.getSimpleName())
                && category.getFamily().equals(Image.class.getName());
        boolean validImageStream = category.getSource().startsWith(LayerStreamingImageProvider.class.getSimpleName())
                && category.getFamily().equals(StreamingImage.class.getName());

        if (!validImage && !validImageStream)
        {
            return false;
        }

        WMSLayerKey key = new WMSLayerKey(category.getCategory());
        return key.getServerName() != null
                && key.getServerName().equals(myServerConnConfig.getServerId(OGCServerSource.WMS_SERVICE))
                && key.getLayerName().equals(myWMSLayer.getTypeInfo().getWmsConfig().getLayerConfig().getLayerName());
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        if (CollectionUtilities.hasContent(satisfactions))
        {
            throw new IllegalArgumentException("Satisfactions are not supported.");
        }
        if (parameters.size() != 1 || !(parameters.get(0) instanceof KeyPropertyMatcher))
        {
            throw new IllegalArgumentException(KeyPropertyMatcher.class.getSimpleName() + " was not found in parameters.");
        }
        final KeyPropertyMatcher param = (KeyPropertyMatcher)parameters.get(0);

        boolean error = true;
        try
        {
            handleQueryStarted();

            InputStream imageStream;
            try
            {
                imageStream = WMSEnvoyHelper.getImageStreamFromServer(
                        getToolbox().getPluginToolboxRegistry().getPluginToolbox(WMSToolbox.class).getSldRegistry(),
                        myServerConnConfig, myWMSLayer.getConfiguration(), param.getImageKey(), getToolbox(), myWMSVersion);
            }
            catch (InterruptedException e)
            {
                handleQueryCancelled();
                error = false;
                throw e;
            }
            catch (ClosedByInterruptException e)
            {
                handleQueryCancelled();
                // a cancel is not counted as an error
                error = false;
                throw new InterruptedException(e.getMessage());
            }
            catch (IOException e)
            {
                if (ThreadControl.isThreadCancelled())
                {
                    handleQueryCancelled();
                    // a cancel is not counted as an error
                    error = false;
                    throw new InterruptedException(e.getMessage());
                }
                else
                {
                    handleQueryException(e);
                    throw new QueryException("Error connecting to server: " + e, e);
                }
            }
            catch (GeneralSecurityException e)
            {
                handleQueryException(e);
                throw new QueryException("Error connecting to server: " + e, e);
            }
            catch (ImageFormatUnknownException e)
            {
                handleQueryException(e);
                throw new QueryException("Data returned from server has an unrecognized format: " + e, e);
            }
            catch (URISyntaxException e)
            {
                handleQueryException(e);
                throw new QueryException(e.getMessage(), e);
            }

            if (imageStream != null)
            {
                try
                {
                    error = handleImageStreamFromServer(category, queryReceiver, param, imageStream);
                }
                catch (CacheException e)
                {
                    if (ExceptionUtilities.hasCause(e, InterruptedIOException.class)
                            || imageStream instanceof Cancellable && ((Cancellable)imageStream).isCancelled())
                    {
                        handleQueryCancelled();
                        // a cancel is not counted as an error
                        error = false;
                        throw new InterruptedException(e.getMessage());
                    }
                    else
                    {
                        LOGGER.error("Failed to cache data: " + e, e);
                        error = true;
                    }
                }
                finally
                {
                    try
                    {
                        imageStream.close();
                    }
                    catch (IOException e)
                    {
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug(e, e);
                        }
                    }
                }
            }
        }
        finally
        {
            handleQueryEnded(error);
        }
    }

    @Override
    public String toString()
    {
        String serverTitle = myServerConnConfig.getServerTitle();
        String id = myServerConnConfig.getServerId(OGCServerSource.WMS_SERVICE);
        return new StringBuilder().append(WMSGetMapEnvoy.class.getSimpleName()).append(',').append(id).append(',')
                .append(serverTitle).append(',').append(myWMSLayer.getTitle()).toString();
    }

    /**
     * Handle when a query receives an image from the server.
     *
     * @param category The data model category.
     * @param queryReceiver An object that will receive {@link Query} objects
     *            produced by this data provider.
     * @param param the property matcher for the query.
     * @param imageStreamFromServer The image which was returned.
     * @return true when an error has occurred and false when no error has
     *         occurred.
     * @throws CacheException If there is a problem caching the image stream.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    private boolean handleImageStreamFromServer(DataModelCategory category, CacheDepositReceiver queryReceiver,
            final KeyPropertyMatcher param, InputStream imageStreamFromServer)
        throws CacheException
    {
        try
        {
            Collection<PropertyAccessor<InputStream, ?>> accessors = New.collection();
            accessors.add(
                    SerializableAccessor.<InputStream, String>getSingletonAccessor(KEY_PROPERTY_DESCRIPTOR, param.getOperand()));
            accessors.add(IMAGE_STREAM_ACCESSOR);

            CacheDeposit<InputStream> deposit = null;

            if (isBeforeLastDurationOrTimeless(Weeks.TWO, param))
            {
                deposit = new DefaultCacheDeposit<>(category, accessors, Collections.singleton(imageStreamFromServer), true,
                        myWMSLayer.getValidDuration().add(new Milliseconds(System.currentTimeMillis())).asDate(), false);
            }
            else
            {
                Date expiration = CacheDeposit.SESSION_END;
                if (myWMSLayer.getConfiguration().getDisplayConfig().getRefreshTime() > 0)
                {
                    expiration = new Date(
                            System.currentTimeMillis() + myWMSLayer.getConfiguration().getDisplayConfig().getRefreshTime());
                }
                deposit = new DefaultCacheDeposit<>(category, accessors, Collections.singleton(imageStreamFromServer), true,
                        expiration, false);
            }

            long[] ids = queryReceiver.receive(deposit);

            WMSLayerConfig config = myWMSLayer.getConfiguration();
            if (config.getLayerType() == LayerType.SRTM)
            {
                synchronized (ourBILWarningMonitor)
                {
                    if (!ourBILSizeWarningIssued)
                    {
                        int width = config.getGetMapConfig().getTextureWidth().intValue();
                        int height = config.getGetMapConfig().getTextureHeight().intValue();
                        long[] sizes = getToolbox().getDataRegistry().getPersistedSizes(ids, IMAGE_PROPERTY_DESCRIPTOR);
                        if (sizes != null && sizes.length == 1 && sizes[0] != height * width * 2)
                        {
                            ourBILSizeWarningIssued = true;
                            LOGGER.warn("Server returned an image size (" + sizes[0] + " which does not match the expected size ("
                                    + width * height * 2 + "). For request : " + param.getOperand());
                            JOptionPane.showMessageDialog(null, "<html>The server returned terrain data with unexpected size.<br>"
                                    + "Earth terrain may be incorrect.</html>");
                        }
                    }
                }
            }

            return false;
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Failed to cache data: " + e, e);
            return true;
        }
    }

    /**
     * Handle a cancelled query.
     */
    private void handleQueryCancelled()
    {
        if (myMetricsTracker != null)
        {
            myMetricsTracker.queryCancelled();
        }
    }

    /**
     * Handle the end of a query.
     *
     * @param error Flag indicating if there was an error.
     */
    private void handleQueryEnded(boolean error)
    {
        if (myMetricsTracker != null)
        {
            if (error)
            {
                myMetricsTracker.queryError();
            }
            myMetricsTracker.queryEnded();
        }
    }

    /**
     * Posts a user message if this is the first time an exception has occured.
     *
     * @param exception The exception to post the message for.
     */
    private void handleQueryException(Exception exception)
    {
        if (!myHasReportedError)
        {
            UserMessageEvent.error(getToolbox().getEventManager(), exception.getMessage(), false, true);
            myHasReportedError = true;
        }
    }

    /**
     * Handle a started query.
     */
    private void handleQueryStarted()
    {
        if (myMetricsTracker != null)
        {
            myMetricsTracker.queryStarted();
        }
    }

    /**
     * Checks if is within last specified duration.
     *
     * @param duration the duration
     * @param param the param
     * @return true, if is within last week
     */
    private boolean isBeforeLastDurationOrTimeless(final Duration duration, final KeyPropertyMatcher param)
    {
        return param.getImageKey().getTimeSpan().isTimeless()
                || param.getImageKey().getTimeSpan().isBefore(TimeSpan.get(duration, new Date()));
    }

    /**
     * A property matcher that matches a {@link TileImageKey}.
     */
    public static class KeyPropertyMatcher extends GeneralPropertyMatcher<String>
    {
        /** The key for the image. */
        private final TileImageKey myImageKey;

        /**
         * Constructor.
         *
         * @param imageKey The key for the image.
         */
        public KeyPropertyMatcher(TileImageKey imageKey)
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
        public TileImageKey getImageKey()
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
