package io.opensphere.core.api.adapter;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryTrackerListener;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.MimeType;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.HttpUtilities;

/**
 * An abstract envoy that is tailored for simple cases.
 *
 * @param <T> The type of the deposit
 */
public abstract class SimpleEnvoy<T> extends AbstractEnvoy implements DataRegistryDataProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SimpleEnvoy.class);

    /** The server provider registry. */
    private final ServerProviderRegistry myServerProviderRegistry;

    /**
     * Helper method to perform a query.
     *
     * @param <T> the type of the results
     * @param dataRegistry the data registry
     * @param query the query
     * @return the results
     * @throws QueryException if something goes wrong with the query
     */
    public static <T> List<T> performQuery(DataRegistry dataRegistry, SimpleQuery<T> query) throws QueryException
    {
        QueryTracker tracker = dataRegistry.performQuery(query);
        if (tracker.getQueryStatus() == QueryTracker.QueryStatus.SUCCESS)
        {
            return query.getResults();
        }
        throw new QueryException(tracker.getException().getMessage(), tracker.getException());
    }

    /**
     * Helper method to submit a query.
     *
     * @param dataRegistry the data registry
     * @param query the query
     * @param listener the listener for results
     */
    public static void submitQuery(DataRegistry dataRegistry, SimpleQuery<?> query, QueryTrackerListener listener)
    {
        QueryTracker tracker = dataRegistry.submitQuery(query);
        tracker.addListener(listener);
    }

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public SimpleEnvoy(Toolbox toolbox)
    {
        super(toolbox);
        myServerProviderRegistry = toolbox != null ? toolbox.getServerProviderRegistry() : null;
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
        return AbstractEnvoy.class.getSimpleName();
    }

    @Override
    public void open()
    {
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        try
        {
            URL url = getUrl(category);
            query(url, category, queryReceiver);
        }
        catch (IOException | CacheException e)
        {
            throw new QueryException(e);
        }
    }

    /**
     * Gets the server provider registry.
     *
     * @return the server provider registry
     */
    protected ServerProviderRegistry getServerProviderRegistry()
    {
        return myServerProviderRegistry;
    }

    /**
     * Queries the URL, parses results, and deposits them in the data registry.
     *
     * @param url the URL
     * @param category the data model category
     * @param queryReceiver the query receiver
     * @throws IOException if something goes wrong
     * @throws CacheException if something goes wrong
     * @throws QueryException if something goes wrong
     */
    protected void query(URL url, DataModelCategory category, CacheDepositReceiver queryReceiver)
        throws IOException, CacheException, QueryException
    {
        if (url != null)
        {
            try (CancellableInputStream inputStream = sendGet(url))
            {
                Collection<T> items = parseDepositItems(inputStream);
                if (!items.isEmpty())
                {
                    CacheDeposit<T> deposit = createDeposit(category, items);
                    queryReceiver.receive(deposit);
                }
            }
        }
        else
        {
            LOGGER.info(getClass().getName() + " envoy's getUrl method returned a null value, skipping query.");
        }
    }

    /**
     * Gets the URL to query.
     *
     * @param category the data model category
     * @return the URL
     * @throws MalformedURLException if the URL is malformed
     */
    protected URL getUrl(DataModelCategory category) throws MalformedURLException
    {
        return null;
    }

    /**
     * Performs a GET request to the given URL and returns the response as a {@link CancellableInputStream}.
     *
     * @param url the URL
     * @return The input stream
     * @throws QueryException If something went wrong
     */
    protected CancellableInputStream sendGet(URL url) throws QueryException
    {
        return sendGet(url, Collections.emptyMap());
    }

    /**
     * Performs a GET request to the given URL and returns the response as a {@link CancellableInputStream}.
     *
     * @param url the URL
     * @param extraHeaderValues Any extra header information to add to the post request.
     * @return The input stream
     * @throws QueryException If something went wrong
     */
    protected CancellableInputStream sendGet(URL url, Map<String, String> extraHeaderValues) throws QueryException
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("GET " + url);
        }

        CancellableInputStream inputStream = null;
        ResponseValues response = new ResponseValues();
        try
        {
            inputStream = myServerProviderRegistry.getProvider(HttpServer.class).getServer(url).sendGet(url, extraHeaderValues,
                    response);

            String error = null;
            if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                error = HttpUtilities.formatResponse(url, response);
            }
            else if (response.getContentType().contains(MimeType.HTML.getMimeType()))
            {
                // They are sending us an html page instead of something like
                // XML or json.
                String html = new StreamReader(inputStream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
                error = StringUtilities.removeHTML(html);
            }

            if (error != null)
            {
                inputStream.close();
                throw new QueryException(error);
            }
        }
        catch (ClosedByInterruptException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(e, e);
            }
        }
        catch (ConnectException e)
        {
            throw new QueryException(e.getMessage() + " to " + url, e);
        }
        catch (URISyntaxException | IOException e)
        {
            throw new QueryException(e);
        }
        return inputStream;
    }

    /**
     * Parses the input stream into the items to be deposited to the registry.
     *
     * @param inputStream the input stream
     * @return the items to be deposited
     * @throws IOException if a problem occurred reading the stream
     */
    protected abstract Collection<T> parseDepositItems(CancellableInputStream inputStream) throws IOException;

    /**
     * Creates a cache deposit from the items.
     *
     * @param category the data model category
     * @param items the items to deposit
     * @return the cache deposit
     */
    protected abstract CacheDeposit<T> createDeposit(DataModelCategory category, Collection<? extends T> items);
}
