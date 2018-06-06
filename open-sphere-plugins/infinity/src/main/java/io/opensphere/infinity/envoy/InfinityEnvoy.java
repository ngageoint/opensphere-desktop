package io.opensphere.infinity.envoy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.SimpleEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.GeneralPropertyMatcher;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
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
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.infinity.json.Aggs;
import io.opensphere.infinity.json.Any;
import io.opensphere.infinity.json.BoundingBox;
import io.opensphere.infinity.json.GeoBoundingBox;
import io.opensphere.infinity.json.SearchRequest;
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.infinity.json.TimeRange;
import io.opensphere.infinity.util.InfinityUtilities;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.server.util.JsonUtils;

/** Infinity envoy. */
public class InfinityEnvoy extends SimpleEnvoy<SearchResponse>
{
    /** The data model category family. */
    private static final String FAMILY = "Infinity.Search";

    /** The {@link PropertyDescriptor} for the results. */
    private static final PropertyDescriptor<SearchResponse> RESULTS_DESCRIPTOR = new PropertyDescriptor<>("SearchResponse",
            SearchResponse.class);

    /** The {@link PropertyDescriptor} for the geometry field. */
    private static final PropertyDescriptor<String> GEOM_FIELD_DESCRIPTOR = new PropertyDescriptor<>("GeometryField",
            String.class);

    /** The {@link PropertyDescriptor} for the time field. */
    private static final PropertyDescriptor<String> TIME_FIELD_DESCRIPTOR = new PropertyDescriptor<>("TimeField", String.class);

    /** The {@link PropertyDescriptor} for the bin field. */
    private static final PropertyDescriptor<String> BIN_FIELD_DESCRIPTOR = new PropertyDescriptor<>("BinField", String.class);

    /**
     * Helper method for a client to query this envoy.
     *
     * @param dataRegistry the data registry
     * @param dataType the data type to query
     * @param polygon the polygon to query
     * @param timeSpan the time span to query
     * @param binField the bin field
     * @return the search response
     * @throws QueryException if something goes wrong with the query
     */
    public static SearchResponse query(DataRegistry dataRegistry, DataTypeInfo dataType, Polygon polygon, TimeSpan timeSpan,
            String binField)
        throws QueryException
    {
        String url = InfinityUtilities.getUrl(dataType);
        String geomField = InfinityUtilities.getTagValue(".es-geopoint", dataType);
        String timeField = InfinityUtilities.getTagValue(".es-starttime", dataType);
        if (timeField == null)
        {
            timeField = InfinityUtilities.getTagValue(".es-datetime", dataType);
        }
        DataModelCategory category = new DataModelCategory(null, FAMILY, url);
        List<PropertyMatcher<?>> parameters = New.list(5);
        parameters.add(
                new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME, GeometryMatcher.OperatorType.INTERSECTS, polygon));
        parameters.add(new TimeSpanMatcher(TimeSpanAccessor.TIME_PROPERTY_NAME, timeSpan));
        parameters.add(new GeneralPropertyMatcher<>(GEOM_FIELD_DESCRIPTOR, geomField));
        parameters.add(new GeneralPropertyMatcher<>(TIME_FIELD_DESCRIPTOR, timeField));
        if (binField != null)
        {
            parameters.add(new GeneralPropertyMatcher<>(BIN_FIELD_DESCRIPTOR, binField));
        }
        SimpleQuery<SearchResponse> query = new SimpleQuery<>(category, RESULTS_DESCRIPTOR, parameters);
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
            for (Satisfaction sat : satisfactions)
            {
                IntervalPropertyValueSet valueSet = sat.getIntervalPropertyValueSet();
                Collection<? extends Geometry> geometries = valueSet.getValues(GeometryAccessor.PROPERTY_DESCRIPTOR);
                Collection<? extends TimeSpan> timeSpans = valueSet.getValues(TimeSpanAccessor.PROPERTY_DESCRIPTOR);
                String geomField = (String)parameters.stream().filter(p -> p.getPropertyDescriptor() == GEOM_FIELD_DESCRIPTOR)
                        .map(p -> p.getOperand()).findAny().orElse(null);
                String timeField = (String)parameters.stream().filter(p -> p.getPropertyDescriptor() == TIME_FIELD_DESCRIPTOR)
                        .map(p -> p.getOperand()).findAny().orElse(null);
                String binField = (String)parameters.stream().filter(p -> p.getPropertyDescriptor() == BIN_FIELD_DESCRIPTOR)
                        .map(p -> p.getOperand()).findAny().orElse(null);
                for (Geometry geometry : geometries)
                {
                    for (TimeSpan timeSpan : timeSpans)
                    {
                        query(category, queryReceiver, geometry, timeSpan, geomField, timeField, binField);
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
    protected Collection<SearchResponse> parseDepositItems(CancellableInputStream inputStream) throws IOException
    {
        return List.of(JsonUtils.createMapper().readValue(inputStream, SearchResponse.class));
    }

    @Override
    protected CacheDeposit<SearchResponse> createDeposit(DataModelCategory category, Collection<? extends SearchResponse> items)
    {
        // Overridden so as to be unused
        return null;
    }

    /**
     * Performs a query and deposits the results in the query receiver.
     *
     * @param category the data model category
     * @param queryReceiver the query receiver
     * @param geometry the geometry
     * @param timeSpan the time span
     * @param geomField the geometry field
     * @param timeField the time field
     * @param binField the bin field
     * @throws IOException if something went wrong with the query or parsing result
     * @throws CacheException if something went wrong with the deposit
     */
    private void query(DataModelCategory category, CacheDepositReceiver queryReceiver, Geometry geometry, TimeSpan timeSpan,
            String geomField, String timeField, String binField)
        throws IOException, CacheException
    {
        URL url = getUrl(category);
        System.out.println(url);
        InputStream postData = createRequestStream(geometry, timeSpan, geomField, timeField, binField);
        ResponseValues response = new ResponseValues();
        ServerProvider<HttpServer> provider = getServerProviderRegistry().getProvider(HttpServer.class);

        try (CancellableInputStream inputStream = HttpUtilities.sendPost(url, postData, response, ContentType.JSON, provider))
        {
            Collection<SearchResponse> items = parseDepositItems(inputStream);
            if (!items.isEmpty())
            {
                CacheDeposit<SearchResponse> deposit = createDeposit(category, items, geometry, timeSpan);
                queryReceiver.receive(deposit);
            }
        }
    }

    /**
     * Creates a JSON request stream (the post body).
     *
     * @param geometry the geometry
     * @param timeSpan the time span
     * @param geomField the geometry field
     * @param timeField the time field
     * @param binField the bin field
     * @return the request stream
     * @throws IOException if something goes wrong
     */
    private InputStream createRequestStream(Geometry geometry, TimeSpan timeSpan, String geomField, String timeField,
            String binField)
        throws IOException
    {
        SearchRequest request = createSearchRequest(geometry, timeSpan, geomField, timeField, binField);

        ByteArrayOutputStream out = new ByteArrayOutputStream(384);
        ObjectMapper mapper = JsonUtils.createMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.writeValue(out, request);

        InputStream postData = new ByteArrayInputStream(out.toByteArray());
        System.out.println(new String(out.toByteArray()));
        return postData;
    }

    /**
     * Creates the search request bean.
     *
     * @param geometry the geometry
     * @param timeSpan the time span
     * @param geomField the geometry field
     * @param timeField the time field
     * @param binField the bin field
     * @return the search request bean
     */
    private SearchRequest createSearchRequest(Geometry geometry, TimeSpan timeSpan, String geomField, String timeField,
            String binField)
    {
        SearchRequest request = new SearchRequest();
        request.setSize(0);
        request.setTimeout("30s");
        Object[] must = new Object[2];
        must[0] = new Any("range", new Any(timeField, new TimeRange(timeSpan)));
        must[1] = new Any("geo_bounding_box", new GeoBoundingBox(geomField, new BoundingBox(geometry)));
        request.getQuery().getBool().setMust(must);
        if (binField != null)
        {
            final long largeValue = 1000000000000000000L;
            request.setAggs(new Aggs(binField + ".keyword", 10000, largeValue));
        }
        return request;
    }

    /**
     * Creates a deposit for the data.
     *
     * @param category the data model category
     * @param items the items to deposit
     * @param geometry the geometry
     * @param timeSpan the time span
     * @return the deposit
     */
    private CacheDeposit<SearchResponse> createDeposit(DataModelCategory category, Collection<? extends SearchResponse> items,
            Geometry geometry, TimeSpan timeSpan)
    {
        List<PropertyAccessor<SearchResponse, ?>> accessors = New.list();
        accessors.add(UnserializableAccessor.getHomogeneousAccessor(RESULTS_DESCRIPTOR));
        accessors.add(new ResultTimeSpanAccessor(TimeSpan.TIMELESS, timeSpan));
        return new DefaultCacheDeposit<>(category.withSource(getClass().getName()), accessors, items, true,
                TimeInstant.get().plus(Minutes.ONE).toDate(), false);
    }

    /** TimeSpanAccessor. */
    private static class ResultTimeSpanAccessor extends TimeSpanAccessor<SearchResponse>
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
        public TimeSpan access(SearchResponse input)
        {
            return myTimeSpan;
        }
    }
}
