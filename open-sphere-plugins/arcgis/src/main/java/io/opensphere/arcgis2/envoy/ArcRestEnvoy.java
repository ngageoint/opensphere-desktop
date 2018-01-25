package io.opensphere.arcgis2.envoy;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.arcgis2.esri.ObjectIdsResponse;
import io.opensphere.arcgis2.esri.Response;
import io.opensphere.arcgis2.util.ArcGISRegistryUtils;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
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
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.server.util.JsonUtils;

/**
 * Envoy that queries and retrieves data from an ArcGIS rest server.
 */
public class ArcRestEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /** The server provider. */
    private final ServerProvider<HttpServer> myServerProvider;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public ArcRestEnvoy(Toolbox toolbox)
    {
        super(toolbox);
        myServerProvider = toolbox != null ? toolbox.getServerProviderRegistry().getProvider(HttpServer.class) : null;
    }

    @Override
    public void open()
    {
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
        return ArcRestEnvoy.class.getSimpleName();
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return ArcGISRegistryUtils.FEATURE_FAMILY.equals(category.getFamily());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
                throws QueryException
    {
        DataFilter filter = null;
        @SuppressWarnings("unchecked")
        PropertyMatcher<DataFilter> filterMatcher = (PropertyMatcher<DataFilter>)parameters.stream()
                .filter(p -> p.getPropertyDescriptor() == ArcGISRegistryUtils.DATA_FILTER_PROPERTY_DESCRIPTOR).findAny()
                .orElse(null);
        if (filterMatcher != null)
        {
            filter = filterMatcher.getOperand();
        }

        for (Satisfaction sat : satisfactions)
        {
            IntervalPropertyValueSet valueSet = sat.getIntervalPropertyValueSet();
            Collection<? extends Geometry> geometries = valueSet.getValues(GeometryAccessor.PROPERTY_DESCRIPTOR);
            Collection<? extends TimeSpan> timeSpans = valueSet.getValues(TimeSpanAccessor.PROPERTY_DESCRIPTOR);
            for (Geometry geometry : geometries)
            {
                if (CollectionUtilities.hasContent(timeSpans))
                {
                    for (TimeSpan timeSpan : timeSpans)
                    {
                        query(category, geometry, timeSpan, filter, queryReceiver);
                    }
                }
                else
                {
                    query(category, geometry, TimeSpan.TIMELESS, filter, queryReceiver);
                }
            }
        }
    }

    /**
     * Performs the query.
     *
     * @param category The data model category.
     * @param geometry The geometry to query.
     * @param timeSpan The time span to query.
     * @param filter The optional filter to query.
     * @param queryReceiver An object that will receive {@link Query} objects
     *            produced by this data provider.
     * @throws QueryException If there is a problem with the query.
     */
    void query(DataModelCategory category, Geometry geometry, TimeSpan timeSpan, DataFilter filter,
            CacheDepositReceiver queryReceiver) throws QueryException
    {
        String baseUrl = ArcGISRegistryUtils.getLayerUrl(category);
        final int batchSize = 200;

        try
        {
            long[] objectIds = queryIds(baseUrl, geometry, timeSpan, filter);

            if (objectIds != null && objectIds.length > 0)
            {
                Collection<Response> results = New.list();
                for (int startId = 0; startId < objectIds.length; startId += batchSize)
                {
                    Response response = queryFeatures(baseUrl, objectIds, startId, batchSize);
                    results.add(response);
                }

                Collection<PropertyAccessor<Response, ?>> accessors = New.list(2);
                accessors.add(UnserializableAccessor.getHomogeneousAccessor(ArcGISRegistryUtils.FEATURE_DESCRIPTOR));
                accessors.add(new TimeSpanAccessor<Response>(timeSpan)
                {
                    @Override
                    public TimeSpan access(Response input)
                    {
                        return timeSpan;
                    }
                });
                queryReceiver.receive(new DefaultCacheDeposit<>(category, accessors, results, true, new Date(), false));
            }
        }
        catch (IOException | CacheException e)
        {
            throw new QueryException(e);
        }
    }

    /**
     * Queries the object IDs for the given items.
     *
     * @param baseUrl The base URL to query.
     * @param geometry The geometry to query.
     * @param timeSpan The time span to query.
     * @param filter The optional filter to query.
     * @return the object IDs, or null
     * @throws IOException if something went wrong
     */
    private long[] queryIds(String baseUrl, Geometry geometry, TimeSpan timeSpan, DataFilter filter) throws IOException
    {
        URL idsUrl = ArcRestEnvoyUtils.buildIdsUrl(baseUrl, geometry, timeSpan, filter);
        ObjectIdsResponse idsResponse;
        try (CancellableInputStream idsStream = performRequest(idsUrl, "Querying ArcGIS feature IDs"))
        {
            idsResponse = JsonUtils.createMapper().readValue(idsStream, ObjectIdsResponse.class);
        }
        return idsResponse.getObjectIds();
    }

    /**
     * Queries features for the given IDs.
     *
     * @param baseUrl The base URL to query.
     * @param objectIds The complete array of IDs.
     * @param startId The start ID.
     * @param batchSize The batch size.
     * @return the response containing the features
     * @throws IOException if something went wrong
     */
    private Response queryFeatures(String baseUrl, long[] objectIds, int startId, int batchSize) throws IOException
    {
        int endId = Math.min(startId + batchSize, objectIds.length);
        long[] subsetIds = Arrays.copyOfRange(objectIds, startId, endId);
        URL requestUrl = ArcRestEnvoyUtils.buildRequestUrl(baseUrl, subsetIds);
        String message = new StringBuilder("Querying ArcGIS features ").append(startId + 1).append(" to ").append(endId)
                .toString();
        Response response;
        try (CancellableInputStream responseStream = performRequest(requestUrl, message))
        {
            response = JsonUtils.createMapper().readValue(responseStream, Response.class);
        }
        return response;
    }

    /**
     * Performs the request.
     *
     * @param url the URL
     * @param message the message for the task activity
     * @return the response stream
     * @throws IOException If something went wrong
     */
    protected CancellableInputStream performRequest(URL url, String message) throws IOException
    {
        try (TaskActivity ta = TaskActivity.createActive(message))
        {
            getToolbox().getUIRegistry().getMenuBarRegistry().addTaskActivity(ta);

            return HttpUtilities.sendGet(url, myServerProvider);
        }
    }
}
