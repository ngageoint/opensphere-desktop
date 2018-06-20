package io.opensphere.infinity.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.SimpleEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.server.ContentType;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.ValueWithCount;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.infinity.json.Aggs;
import io.opensphere.infinity.json.Any;
import io.opensphere.infinity.json.Bool;
import io.opensphere.infinity.json.BoundingBox;
import io.opensphere.infinity.json.ElasticGeometry;
import io.opensphere.infinity.json.GeometryFilter;
import io.opensphere.infinity.json.SearchRequest;
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.infinity.json.Shape;
import io.opensphere.infinity.json.TimeRange;
import io.opensphere.mantle.infinity.InfinityUtilities;
import io.opensphere.mantle.infinity.QueryParameters;
import io.opensphere.mantle.infinity.QueryParameters.GeometryType;
import io.opensphere.mantle.infinity.QueryResults;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.server.util.JsonUtils;

/** Infinity envoy. */
public class InfinityEnvoy extends SimpleEnvoy<QueryResults>
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(InfinityEnvoy.class);

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
        return QueryResults.FAMILY.equals(category.getFamily());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        QueryParameters queryParameters = (QueryParameters)parameters.stream()
                .filter(p -> p.getPropertyDescriptor() == QueryParameters.PROPERTY_DESCRIPTOR).map(p -> p.getOperand()).findAny()
                .orElse(null);
        try
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
                        queryParameters.setGeometry(geometry);
                        queryParameters.setTimeSpan(timeSpan);
                        query(category, queryReceiver, queryParameters);
                    }
                }
            }
        }
        catch (IOException | CacheException e)
        {
            throw new QueryException(e);
        }
    }

    @Override
    protected URL getUrl(DataModelCategory category) throws MalformedURLException
    {
        return new URL(category.getCategory());
    }

    @Override
    protected Collection<QueryResults> parseDepositItems(CancellableInputStream inputStream) throws IOException
    {
        SearchResponse response = JsonUtils.createMapper().readValue(inputStream, SearchResponse.class);
        return List.of(toQueryResults(response));
    }

    @Override
    protected CacheDeposit<QueryResults> createDeposit(DataModelCategory category, Collection<? extends QueryResults> items)
    {
        // Overridden so as to be unused
        return null;
    }

    /**
     * Performs a query and deposits the results in the query receiver.
     *
     * @param category the data model category
     * @param queryReceiver the query receiver
     * @param parameters the query parameters
     * @throws IOException if something went wrong with the query or parsing result
     * @throws CacheException if something went wrong with the deposit
     */
    private void query(DataModelCategory category, CacheDepositReceiver queryReceiver, QueryParameters parameters)
        throws IOException, CacheException
    {
        URL url = getUrl(category);
        LOGGER.info("Request: " + url + " " + parameters.getTimeSpan() + " " + parameters.getGeometry());

        InputStream postData = createRequestStream(parameters);
        ResponseValues response = new ResponseValues();
        ServerProvider<HttpServer> provider = getServerProviderRegistry().getProvider(HttpServer.class);

        try (CancellableInputStream inputStream = HttpUtilities.sendPost(url, postData, response, ContentType.JSON, provider))
        {
            Collection<QueryResults> results = parseDepositItems(inputStream);
            if (!results.isEmpty())
            {
                CacheDeposit<QueryResults> deposit = createDeposit(category, results, parameters);
                queryReceiver.receive(deposit);
            }
        }
    }

    /**
     * Creates a JSON request stream (the post body).
     *
     * @param parameters the query parameters
     * @return the request stream
     * @throws IOException if something goes wrong
     */
    private InputStream createRequestStream(QueryParameters parameters) throws IOException
    {
        SearchRequest request = createSearchRequest(parameters);

        ByteArrayOutputStream out = new ByteArrayOutputStream(384);
        ObjectMapper mapper = JsonUtils.createMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.writeValue(out, request);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(new String(out.toByteArray()));
        }

        InputStream postData = new ByteArrayInputStream(out.toByteArray());
        return postData;
    }

    /**
     * Creates the search request bean.
     *
     * @param parameters the query parameters
     * @return the search request bean
     */
    private SearchRequest createSearchRequest(QueryParameters parameters)
    {
        SearchRequest request = new SearchRequest();
        request.setSize(0);
        request.setTimeout("30s");
        Object[] must = new Object[2];
        if (parameters.getEndTimeField() != null)
        {
            Bool bool = new Bool();
            Object[] innerMust = new Object[2];
            innerMust[0] = new Any("range",
                    new Any(parameters.getTimeField(), new TimeRange(null, Long.valueOf(parameters.getTimeSpan().getEnd()))));
            innerMust[1] = new Any("range", new Any(parameters.getEndTimeField(),
                    new TimeRange(Long.valueOf(parameters.getTimeSpan().getStart()), null)));
            bool.setMust(innerMust);
            must[0] = new Any("bool", bool);
        }
        else
        {
            must[0] = new Any("range", new Any(parameters.getTimeField(), new TimeRange(parameters.getTimeSpan())));
        }
        if (parameters.getGeometryType() == GeometryType.POINT)
        {
            must[1] = new Any("geo_bounding_box",
                    new ElasticGeometry(parameters.getGeomField(), new BoundingBox(parameters.getGeometry())));
        }
        else
        {
            must[1] = new Any("geo_shape", new ElasticGeometry(parameters.getGeomField(),
                    new GeometryFilter(new Shape("envelope", parameters.getGeometry()), "intersects")));
        }
        request.getQuery().getBool().setMust(must);
        if (parameters.getBinField() != null)
        {
            String field = parameters.getBinField().toLowerCase();
            if (parameters.getBinFieldType() == String.class
                    || DynamicEnumerationKey.class.isAssignableFrom(parameters.getBinFieldType()))
            {
                field += ".keyword";
                request.setAggs(new Aggs(field, 10000, InfinityUtilities.MISSING_VALUE));
            }
            else if(Number.class.isAssignableFrom(parameters.getBinFieldType()))
            {
                request.setAggs(new Aggs(field, parameters.getBinWidth(), InfinityUtilities.MISSING_VALUE, parameters.getBinOffset()));
            }
            else
            {
                LOGGER.warn("Unhandled bin field type during infinity search request: " + parameters.getBinFieldType());
                request.setAggs(new Aggs(field, 10000, InfinityUtilities.MISSING_VALUE));
            }
        }
        return request;
    }

    /**
     * Creates a deposit for the data.
     *
     * @param category the data model category
     * @param items the items to deposit
     * @param parameters the query parameters
     * @return the deposit
     */
    private CacheDeposit<QueryResults> createDeposit(DataModelCategory category, Collection<? extends QueryResults> items,
            QueryParameters parameters)
    {
        List<PropertyAccessor<QueryResults, ?>> accessors = New.list();
        accessors.add(UnserializableAccessor.getHomogeneousAccessor(QueryResults.PROPERTY_DESCRIPTOR));
        accessors.add(new ResultTimeSpanAccessor(TimeSpan.TIMELESS, parameters.getTimeSpan()));
        return new DefaultCacheDeposit<>(category.withSource(getClass().getName()), accessors, items, true,
                TimeInstant.get().plus(Minutes.ONE).toDate(), false);
    }

    /**
     * Converts the SearchResponse to a QueryResults.
     *
     * @param response the SearchResponse
     * @return the QueryResults
     */
    private QueryResults toQueryResults(SearchResponse response)
    {
        QueryResults results = new QueryResults(response.getHits().getTotal());
        if (response.getAggregations() != null)
        {
            List<ValueWithCount<String>> bins = Arrays.stream(response.getAggregations().getBins().getBuckets())
                    .map(b -> new ValueWithCount<>(b.getKey(), (int)b.getDoc_count())).collect(Collectors.toList());
            results.setBins(bins);
        }
        return results;
    }

    /** TimeSpanAccessor. */
    private static class ResultTimeSpanAccessor extends TimeSpanAccessor<QueryResults>
    {
        /** The individual time span. */
        private final TimeSpan myTimeSpan;

        /**
         * Constructor.
         *
         * @param extent The total time span extent
         * @param timeSpan The individual time span
         */
        public ResultTimeSpanAccessor(TimeSpan extent, TimeSpan timeSpan)
        {
            super(extent);
            myTimeSpan = timeSpan;
        }

        @Override
        public TimeSpan access(QueryResults input)
        {
            return myTimeSpan;
        }
    }
}
