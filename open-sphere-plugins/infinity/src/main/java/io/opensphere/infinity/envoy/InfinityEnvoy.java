package io.opensphere.infinity.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.SimpleEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.server.ContentType;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.infinity.json.Aggs;
import io.opensphere.infinity.json.SearchRequest;
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.server.util.JsonUtils;

/** Infinity envoy. */
public class InfinityEnvoy extends SimpleEnvoy<SearchResponse>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(InfinityEnvoy.class);

    /** The data model category family. */
    private static final String FAMILY = "Infinity.Search";

    /** The {@link PropertyDescriptor} for the results. */
    private static final PropertyDescriptor<SearchResponse> PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("SearchResponse",
            SearchResponse.class);

    /**
     * Helper method for a client to query this envoy.
     *
     * @param dataRegistry the data registry
     * @param layerUrl the layer URL to query
     * @return the search response
     * @throws QueryException if something goes wrong with the query
     */
    public static SearchResponse query(DataRegistry dataRegistry, String layerUrl) throws QueryException
    {
        // TODO query similar to RecommendedLayersEnvoy
        DataModelCategory category = new DataModelCategory(null, FAMILY, layerUrl);
        SimpleQuery<SearchResponse> query = new SimpleQuery<>(category, PROPERTY_DESCRIPTOR);
        List<SearchResponse> results = performQuery(dataRegistry, query);
        return results.iterator().next();
    }

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public InfinityEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return FAMILY.equals(category.getFamily());
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
            String binField = "field1";
            if (url != null)
            {
                for (Satisfaction sat : satisfactions)
                {
                    IntervalPropertyValueSet valueSet = sat.getIntervalPropertyValueSet();
                    Collection<? extends Geometry> geometries = valueSet.getValues(GeometryAccessor.PROPERTY_DESCRIPTOR);
                    Collection<? extends TimeSpan> timeSpans = valueSet.getValues(TimeSpanAccessor.PROPERTY_DESCRIPTOR);
                    for (Geometry geometry : geometries)
                    {
                        for (TimeSpan timeSpan : timeSpans)
                        {
                            query(category, queryReceiver, url, geometry, timeSpan, binField);
                        }
                    }
                }
            }
            else
            {
                LOGGER.info(getClass().getName() + " envoy's getUrl method returned a null value, skipping query.");
            }
        }
        catch (IOException | CacheException e)
        {
            throw new QueryException(e);
        }
    }

    private void query(DataModelCategory category, CacheDepositReceiver queryReceiver, URL url, Geometry geometry,
            TimeSpan timeSpan, String binField)
        throws IOException, CacheException
    {
        System.out.println(timeSpan + " " + geometry);

        SearchRequest request = new SearchRequest();
        //  TODO populate with geom/time data
        if (binField != null)
        {
            request.setAggs(new Aggs(binField + ".keyword", 10000, 1000000000000000000L));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonUtils.createMapper().writeValue(out, request);

        InputStream postData = new ByteArrayInputStream(out.toByteArray());
        ResponseValues response = new ResponseValues();
        ServerProvider<HttpServer> provider = getServerProviderRegistry().getProvider(HttpServer.class);
        try (CancellableInputStream inputStream = HttpUtilities.sendPost(url, postData, response, ContentType.JSON, provider))
        {
            Collection<SearchResponse> items = parseDepositItems(inputStream);
            if (!items.isEmpty())
            {
                CacheDeposit<SearchResponse> deposit = createDeposit(category, items);
                queryReceiver.receive(deposit);
            }
        }
    }

    @Override
    protected URL getUrl(DataModelCategory category) throws MalformedURLException
    {
        return new URL(category.getCategory());
    }

    @Override
    protected Collection<SearchResponse> parseDepositItems(CancellableInputStream inputStream) throws IOException
    {
        return List.of(JsonUtils.createMapper().readValue(inputStream, SearchResponse.class));
    }

    @Override
    protected CacheDeposit<SearchResponse> createDeposit(DataModelCategory category, Collection<? extends SearchResponse> items)
    {
        return new DefaultCacheDeposit<>(category.withSource(getClass().getName()),
                List.of(UnserializableAccessor.getHomogeneousAccessor(PROPERTY_DESCRIPTOR)), items, true,
                TimeInstant.get().plus(Minutes.ONE).toDate(), false);
    }
}
