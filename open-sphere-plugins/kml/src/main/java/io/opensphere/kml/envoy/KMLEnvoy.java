package io.opensphere.kml.envoy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import io.opensphere.core.Notify;
import io.opensphere.core.Notify.Method;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcherUtilities;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.UncloseableInputStream;
import io.opensphere.core.util.lang.ExceptionUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadControl;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.kml.common.model.KMLContentType;
import io.opensphere.kml.common.model.KMLDataEvent;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSource.FailureReason;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLToolboxUtils;

/**
 * The KML envoy.
 */
@SuppressWarnings("PMD.GodClass")
public class KMLEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLEnvoy.class);

    /** The data source. */
    private KMLDataSource myDataSource;

    /** The KML data model category. */
    private DataModelCategory myKmlCategory;

    /** The icon data model category. */
    private DataModelCategory myIconCategory;

    /** Cache deposit receiver. */
    private final CacheDepositReceiver myCacheDepositReceiver = new CacheDepositReceiver()
    {
        @Override
        public <T> long[] receive(CacheDeposit<T> deposit)
        {
            return getDataRegistry().addModels(deposit);
        }
    };

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param dataSource The KML data source
     */
    public KMLEnvoy(Toolbox toolbox, KMLDataSource dataSource)
    {
        super(toolbox);
        setDataSource(dataSource);
    }

    @Override
    public void close()
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("close");
        }

        // Remove the data source from the style cache
        KMLToolboxUtils.getKmlToolbox().getStyleCache().removeData(myDataSource);

        super.close();

        myDataSource.disassociateHandler(this);
    }

    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory dataModelCategory,
            Collection<? extends IntervalPropertyValueSet> intervalSets)
    {
        return SingleSatisfaction.generateSatisfactions(intervalSets);
    }

    @Override
    public String getThreadPoolName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public void open()
    {
        myDataSource.associateEnvoy(this);
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return category.matches(myKmlCategory) || category.matches(myIconCategory);
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
                throws InterruptedException, QueryException
    {
        if (CollectionUtilities.hasContent(satisfactions))
        {
            throw new IllegalArgumentException("Satisfactions are not supported.");
        }
        final String url = getUrl(parameters);

        if (!url.startsWith(myDataSource.getPath()))
        {
            return;
        }

        String displayName = getDisplayName(parameters, url);

        @SuppressWarnings("PMD.PrematureDeclaration")
        long t0 = System.nanoTime();

        TaskActivity taskActivity = activateTaskActivity(
                myDataSource.getCreatingFeature() != null ? myDataSource.getCreatingFeature().getName() : myDataSource.getName());

        KMLFeature rootFeature = null;

        myDataSource.setFailureReason(null);
        myDataSource.setErrorMessage(null);
        KMLDataSource dataSourceToLoad = getDataSourceToLoad(url, displayName);

        try
        {
            startLoad(dataSourceToLoad);

            Queue<KMLDataSource> dataSourcesToLoad = New.queue();
            dataSourcesToLoad.add(dataSourceToLoad);

            while (!dataSourcesToLoad.isEmpty() && myDataSource.isActive() && !isClosed())
            {
                ThreadControl.check();

                KMLDataSource dataSource = dataSourcesToLoad.poll();

                // Process a normal KML data source
                if (KMLDataRegistryHelper.KML_CATEGORY_FAMILY.equals(category.getFamily()) || category.getFamily() == null)
                {
                    KMLFeature result = process(dataSource, dataSourcesToLoad);

                    // Save off the main result in order to fire success
                    // at the end
                    if (result != null)
                    {
                        rootFeature = result;
                    }
                }
                else
                {
                    loadImage(dataSource, url, queryReceiver);
                }
            }
        }
        finally
        {
            dataSourceToLoad.setBusy(false, this);
            dataSourceToLoad.setLoadError(dataSourceToLoad.getFailureReason() != null, this);

            // Complete the task activity
            taskActivity.setComplete(true);
        }

        if (dataSourceToLoad.getFailureReason() == null || !myDataSource.isActive())
        {
            if (rootFeature != null)
            {
                handleLoadSuccess(queryReceiver, url, displayName, rootFeature, dataSourceToLoad);
            }
        }
        else
        {
            ThreadControl.check();
            dataSourceToLoad.getOutcomeTracker().failure();
            LOGGER.warn("Unable to load " + dataSourceToLoad.getActualPath());
            throw new QueryException(myDataSource.getErrorMessage());
        }

        logDataSourceLoadTiming(t0, dataSourceToLoad);
    }

    /**
     * Setter for dataSource.
     *
     * @param dataSource the dataSource
     */
    public final void setDataSource(KMLDataSource dataSource)
    {
        myDataSource = dataSource;
        myKmlCategory = KMLDataRegistryHelper.getKmlCategory(myDataSource, null);
        myIconCategory = KMLDataRegistryHelper.getIconCategory(myDataSource, null);
    }

    @Override
    public String toString()
    {
        return KMLEnvoy.class.getSimpleName() + "[" + myDataSource + "]";
    }

    /**
     * Activate that task activity that notifies the user that KML is being
     * loaded.
     *
     * @param url The url.
     * @return The task activity.
     */
    private TaskActivity activateTaskActivity(final String url)
    {
        TaskActivity taskActivity = new TaskActivity();
        if (!isClosed())
        {
            taskActivity.setLabelValue("Loading KML - " + StringUtilities.removeHTML(url));
            taskActivity.setActive(true);
            getToolbox().getUIRegistry().getMenuBarRegistry().addTaskActivity(taskActivity);
        }
        return taskActivity;
    }

    /**
     * Retrieve the image from an input stream, create a cache deposit with it,
     * and send it to the given receiver. This does <b>not</b> close the given
     * input stream.
     *
     * @param imageStream The image stream.
     * @param contentLength The content length of the image, or -1 if unknown.
     * @param name The name for the image, used to retrieve it from the data
     *            registry later.
     * @param cacheDepositReceiver The receiver for the cache deposit.
     * @throws IOException If there is an error reading from the stream.
     * @throws InterruptedException If the request is interrupted.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    private void depositImageToReceiver(InputStream imageStream, long contentLength, String name,
            CacheDepositReceiver cacheDepositReceiver) throws IOException, InterruptedException
    {
//        try
//        {
//            ImageMetrics metrics = LOGGER.isDebugEnabled() ? new ImageMetrics() : null;
//            Image image = Image.read(imageStream, (int)contentLength, Nulls.STRING, false, true, metrics);
//            try
//            {
//                if (metrics != null)
//                {
//                    LOGGER.debug(StringUtilities.formatTimingMessage("Time to read and decode image stream: ",
//                            metrics.getDecodeTimeNanoseconds()));
//                }
        InputStream streamForDeposit = imageStream;
        // Serialization.serializeToStream(image);
        try
        {
            // TODO: it'd be nice to be able to collect these and do a single insert
            CacheDeposit<InputStream> deposit = KMLDataRegistryHelper.createCacheDeposit(KMLEnvoy.class.getSimpleName(),
                    myDataSource, name, streamForDeposit);
            cacheDepositReceiver.receive(deposit);
        }
        catch (CacheException e)
        {
            ThreadControl.check();
            if (ExceptionUtilities.hasCause(e, InterruptedIOException.class))
            {
                throw new InterruptedException(e.getMessage());
            }
            else
            {
                LOGGER.warn("Failed to deposit KML data: " + e, e);
            }
        }
//                finally
//                {
//                    closeStream(streamForDeposit);
//                }
//            }
//            finally
//            {
//                image.dispose();
//            }
//        }
//        catch (ImageFormatUnknownException e)
//        {
//            LOGGER.debug("Image format is unknown for KML entry: " + name, e);
//        }
    }

    /**
     * Get the data source that should be loaded for a particular URL. If the
     * URL matches the path of my data source, then my data source should be
     * loaded. If the URL doesn't match, my data source is cloned and its path
     * is assigned to the input URL, and then that data source is loaded.
     *
     * @param url The URL.
     * @param displayName The name of the data source if one is created.
     * @return The data source to be loaded.
     */
    private KMLDataSource getDataSourceToLoad(final String url, String displayName)
    {
        KMLDataSource dataSourceToLoad;
        if (url.equals(myDataSource.getPath()))
        {
            dataSourceToLoad = myDataSource;
        }
        else
        {
            dataSourceToLoad = myDataSource.clone();
            dataSourceToLoad.setPath(url);
            dataSourceToLoad.setName(displayName);
        }
        return dataSourceToLoad;
    }

    /**
     * Get the display name from some property matchers. If no display name is
     * found in the parameters, create a default one.
     *
     * @param parameters The property matchers.
     * @param url The url.
     * @return The display name, or {@code null} if none was found.
     */
    private String getDisplayName(List<? extends PropertyMatcher<?>> parameters, String url)
    {
        String displayName = PropertyMatcherUtilities.getOperand(parameters,
                KMLDataRegistryHelper.DISPLAY_NAME_PROPERTY_DESCRIPTOR);
        if (displayName == null)
        {
            displayName = myDataSource.getName() + " - " + url;
        }
        return displayName;
    }

    /**
     * Get the URL from some property matchers.
     *
     * @param parameters The property matchers.
     * @return The URL.
     * @throws IllegalArgumentException If no URL parameter could be found.
     */
    private String getUrl(List<? extends PropertyMatcher<?>> parameters) throws IllegalArgumentException
    {
        String url = PropertyMatcherUtilities.getOperand(parameters, KMLDataRegistryHelper.URL_PROPERTY_DESCRIPTOR);
        if (url == null)
        {
            throw new IllegalArgumentException("No URL parameter found in parameters: " + parameters);
        }
        return url;
    }

    /**
     * Handle the successful load of a KML root feature.
     *
     * @param queryReceiver The receiver for the cache deposit.
     * @param url The URL that the root feature was loaded from.
     * @param displayName The display name for the feature.
     * @param rootFeature The root feature.
     * @param dataSourceToLoad The data source.
     */
    private void handleLoadSuccess(CacheDepositReceiver queryReceiver, final String url, String displayName,
            KMLFeature rootFeature, KMLDataSource dataSourceToLoad)
    {
        dataSourceToLoad.setIsLoaded(myDataSource.isActive());
        dataSourceToLoad.getOutcomeTracker().success();

        KMLDataEvent event = new KMLDataEvent(dataSourceToLoad, rootFeature);
        CacheDeposit<KMLDataEvent> deposit = KMLDataRegistryHelper.createCacheDeposit(KMLEnvoy.class.getSimpleName(),
                myDataSource, url, displayName, event);
        try
        {
            queryReceiver.receive(deposit);
        }
        catch (CacheException e)
        {
            LOGGER.warn("Failed to deposit KML data: " + e, e);
        }
    }

    /**
     * Load an image and deposit it into the cache deposit receiver.
     *
     * @param dataSource The image data source.
     * @param url The url for the image.
     * @param queryReceiver The query receiver.
     * @throws InterruptedException If the query is interrupted.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    private void loadImage(KMLDataSource dataSource, String url, CacheDepositReceiver queryReceiver) throws InterruptedException
    {
        long t0 = System.nanoTime();

        KMLDataLoader kmlDataLoader = new KMLDataLoader(dataSource, getToolbox());
        try (InputStream stream = kmlDataLoader.load())
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Time to connect to image stream: ", System.nanoTime() - t0));
            }

            depositImageToReceiver(stream, kmlDataLoader.getContentLength(), url, queryReceiver);
        }
        catch (IOException e)
        {
            LOGGER.warn("Failed to decode image: " + e, e);
            return;
        }
    }

    /**
     * Log how long it took to load a data source.
     *
     * @param t0 The start time from {@link System#nanoTime()}.
     * @param dataSourceToLoad The data source.
     */
    private void logDataSourceLoadTiming(long t0, KMLDataSource dataSourceToLoad)
    {
        // Debug
        if (LOGGER.isDebugEnabled())
        {
            long t1 = System.nanoTime();
            String msg = StringUtilities.concat("Loaded ", dataSourceToLoad.getActualPath(), " in ");
            LOGGER.debug(StringUtilities.formatTimingMessage(msg, t1 - t0));
        }
    }

    /**
     * Process an input stream.
     *
     * @param is The input stream.
     * @param contentType The expected content type.
     * @param contentLengthBytes The expected content length, or -1 if unknown.
     * @param processor The KML processor.
     * @param dataSourcesToLoad Return collection of additional data sources to
     *            load.
     * @throws IOException If there is an error.
     */
    private void process(InputStream is, KMLContentType contentType, long contentLengthBytes, KMLProcessor processor,
            Collection<? super KMLDataSource> dataSourcesToLoad) throws IOException
    {
        if (contentType == KMLContentType.KMZ)
        {
            processKMZ(is, contentLengthBytes, processor, dataSourcesToLoad);
        }
        else
        {
            processor.process(is, dataSourcesToLoad);
        }
    }

    /**
     * Attempt to process an input stream as either a KML or KMZ. The input
     * stream may be closed by this operation.
     *
     * @param dataSource The KML data source.
     * @param dataSourcesToLoad Return collection of data sources to load.
     * @return The result of the processing.
     */
    private KMLFeature process(KMLDataSource dataSource, Collection<? super KMLDataSource> dataSourcesToLoad)
    {
        KMLFeature feature = null;

        KMLDataLoader kmlDataLoader = new KMLDataLoader(dataSource, getToolbox());
        try (InputStream inputStream = kmlDataLoader.load())
        {
            if (inputStream != null)
            {
                KMLProcessor processor = new KMLProcessor(dataSource);

                // Determine the content type
                KMLContentType contentType = KMLContentType.KML;
                try
                {
                    contentType = StreamUtilities.isZipInputStream(inputStream) ? KMLContentType.KMZ : KMLContentType.KML;
                }
                catch (IOException e)
                {
                    Notify.error("The following exception occurred while processing: " + dataSource.getDataGroupKey() + "\n"
                            + e.getMessage(), Method.ALERT_HIDDEN);
                    LOGGER.error(e.getMessage(), e);
                }
                dataSource.setContentType(contentType);

                try
                {
                    LOGGER.info(StringUtilities.concat("Loading ", contentType, ": ", dataSource.getActualPath()));

                    // Try the expected content type
                    process(inputStream, contentType, kmlDataLoader.getContentLength(), processor, dataSourcesToLoad);
                }
                catch (IOException e)
                {
                    Notify.error("The following exception occurred while processing: " + dataSource.getDataGroupKey() + "\n"
                            + e.getMessage(), Method.ALERT_HIDDEN);
                    LOGGER.error(e.getMessage(), e);
                }

                feature = processor.getResult();
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to close input stream");
        }

        return feature;
    }

    /**
     * Attempts to unzip the given input stream as a KMZ. The first KML zip
     * entry that is found will be sent to the given KML processor. Non-KML zip
     * entries are sent to the data registry.
     *
     * @param inputStream The input stream to be processed.
     * @param contentLength The length of the stream in bytes, or -1 if unknown.
     * @param processor The KML processor that will receive the KML document.
     * @param dataSourcesToLoad Return collection of additional data sources to
     *            load.
     * @throws IOException If the KMZ data could not be unzipped.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    private void processKMZ(InputStream inputStream, long contentLength, KMLProcessor processor,
            Collection<? super KMLDataSource> dataSourcesToLoad) throws IOException
    {
        ZipInputStream zipStream = new ZipInputStream(inputStream);

        // Remove any previously cached icons for this data source.
        KMLDataRegistryHelper.clearIconData(getDataRegistry(), myDataSource);

        boolean foundKML = false;
        for (ZipEntry entry; (entry = zipStream.getNextEntry()) != null;)
        {
            // Call the processor on the first KML file
            if (KMLContentType.getKMLContentTypeForFilename(entry.getName()) == KMLContentType.KML)
            {
                if (!foundKML)
                {
                    // Use an un-closeable input stream to make sure the zip
                    // stream stays open.
                    processor.process(new UncloseableInputStream(zipStream), dataSourcesToLoad);
                    foundKML = true;
                }
            }
            // Add anything else to the registry
            else if (!entry.isDirectory())
            {
                try
                {
                    depositImageToReceiver(zipStream, entry.getSize(), entry.getName(), myCacheDepositReceiver);
                }
                catch (InterruptedException e)
                {
                    myDataSource.setFailureReason(FailureReason.OTHER);
                    myDataSource.setErrorMessage("Load was interrupted for: " + myDataSource.getPath());
                    throw new IOException(myDataSource.getErrorMessage());
                }
            }
        }

        if (!foundKML)
        {
            myDataSource.setFailureReason(FailureReason.OTHER);
            myDataSource.setErrorMessage("No KML file found in KMZ file: " + myDataSource.getPath());
            throw new IOException(myDataSource.getErrorMessage());
        }
    }

    /**
     * Indicate that a data source is starting to load.
     *
     * @param dataSource The data source.
     */
    private void startLoad(KMLDataSource dataSource)
    {
        if (!dataSource.isBusy())
        {
            dataSource.setBusy(true, this);
        }
        if (dataSource.loadError())
        {
            dataSource.setLoadError(false, this);
        }
    }
}
