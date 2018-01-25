package io.opensphere.osh.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.matcher.TimeSpanMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.QueryTracker.QueryStatus;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.model.Offering;
import io.opensphere.osh.model.Output;

/** OpenSensorHub querier. */
public class OSHQuerier implements OSHImageQuerier
{
    /** The data registry. */
    private final DataRegistry myDataRegistry;

    /**
     * Constructor.
     *
     * @param dataRegistry The data registry
     */
    public OSHQuerier(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    /**
     * Queries the capabilities.
     *
     * @param baseUrl the base URL
     * @return the offerings
     * @throws QueryException if a problem occurred querying the data registry
     */
    public List<Offering> getCapabilities(String baseUrl) throws QueryException
    {
        List<Offering> offerings;

        DataModelCategory category = OSHRegistryUtils.newGetCapabilitiesCategory(baseUrl);
        SimpleQuery<Offering> query = new SimpleQuery<>(category, OSHRegistryUtils.GET_CAPABILITIES_DESCRIPTOR);
        QueryTracker tracker = myDataRegistry.performQuery(query);
        if (tracker.getQueryStatus() == QueryStatus.SUCCESS)
        {
            offerings = query.getResults();
        }
        else
        {
            throw new QueryException(tracker.getException());
        }

        return offerings;
    }

    /**
     * Queries describe sensor information.
     *
     * @param baseUrl the base URL
     * @param offering the offering
     * @return the outputs
     * @throws QueryException if a problem occurred querying the data registry
     */
    public List<Output> describeSensor(String baseUrl, Offering offering) throws QueryException
    {
        List<Output> outputs;

        DataModelCategory category = OSHRegistryUtils.newDescribeSensorCategory(baseUrl, offering.getProcedure());
        SimpleQuery<Output> query = new SimpleQuery<>(category, OSHRegistryUtils.DESCRIBE_SENSOR_DESCRIPTOR);
        QueryTracker tracker = myDataRegistry.performQuery(query);
        if (tracker.getQueryStatus() == QueryStatus.SUCCESS)
        {
            outputs = query.getResults();
        }
        else
        {
            throw new QueryException(tracker.getException());
        }

        return outputs;
    }

    /**
     * Queries result template information.
     *
     * @param baseUrl the base URL
     * @param offering the offering
     * @param property the property
     * @return the result stream
     * @throws QueryException if a problem occurred querying the data registry
     */
    public Output getResultTemplate(String baseUrl, Offering offering, String property) throws QueryException
    {
        Output output;

        DataModelCategory category = OSHRegistryUtils.newGetResultTemplateCategory(baseUrl, offering.getId(), property);
        SimpleQuery<Output> query = new SimpleQuery<>(category, OSHRegistryUtils.GET_RESULT_TEMPLATE_DESCRIPTOR);
        QueryTracker tracker = myDataRegistry.performQuery(query);
        if (tracker.getQueryStatus() == QueryStatus.SUCCESS)
        {
            output = query.getResults().get(0);
        }
        else
        {
            throw new QueryException(tracker.getException());
        }

        return output;
    }

    /**
     * Queries get results.
     *
     * @param baseUrl the base URL
     * @param offering the offering
     * @param property the property
     * @param timeSpan the time span to query
     * @return the result stream
     * @throws QueryException if a problem occurred querying the data registry
     */
    public CancellableInputStream getResults(String baseUrl, Offering offering, String property, TimeSpan timeSpan)
        throws QueryException
    {
        DataModelCategory category = OSHRegistryUtils.newGetResultCategory(baseUrl, offering.getId(), property);
        TimeSpanMatcher timeMatcher = new TimeSpanMatcher(TimeSpanAccessor.TIME_PROPERTY_NAME, timeSpan);
        SimpleQuery<CancellableInputStream> query = new SimpleQuery<>(category, OSHRegistryUtils.GET_RESULT_DESCRIPTOR,
                timeMatcher);
        QueryTracker tracker = myDataRegistry.performQuery(query);
        if (tracker.getQueryStatus() == QueryStatus.SUCCESS)
        {
            CancellableInputStream stream = query.getResults().iterator().next();
            return stream;
        }
        else
        {
            throw new QueryException(tracker.getException());
        }
    }

    @Override
    public byte[] queryImage(String typeKey, TimeSpan timeSpan) throws QueryException
    {
        DataModelCategory category = OSHRegistryUtils.newVideoResultCategory(typeKey);
        TimeSpanMatcher timeMatcher = new TimeSpanMatcher(TimeSpanAccessor.TIME_PROPERTY_NAME, timeSpan);
        SimpleQuery<byte[]> query = new SimpleQuery<>(category, OSHRegistryUtils.VIDEO_RESULT_DESCRIPTOR, timeMatcher);
        myDataRegistry.performLocalQuery(query);
        List<byte[]> results = query.getResults();
        return CollectionUtilities.getItemOrNull(results, 0);
    }

    /**
     * Deposits a video image in the data registry.
     *
     * @param dataType the data type
     * @param imageBytes the image bytes
     * @param timeSpan the time span of the image
     */
    public void depositImage(DataTypeInfo dataType, byte[] imageBytes, TimeSpan timeSpan)
    {
        DataModelCategory category = OSHRegistryUtils.newVideoResultCategory(dataType.getTypeKey());
        Collection<PropertyAccessor<byte[], ?>> accessors = New.list(2);
        accessors.add(SerializableAccessor.getHomogeneousAccessor(OSHRegistryUtils.VIDEO_RESULT_DESCRIPTOR));
        accessors.add(new TimeSpanAccessor<byte[]>(dataType.getTimeExtents().getExtent())
        {
            @Override
            public TimeSpan access(byte[] input)
            {
                return timeSpan;
            }
        });
        CacheDeposit<byte[]> deposit = new DefaultCacheDeposit<>(category, accessors, Collections.singleton(imageBytes), true,
                CacheDeposit.SESSION_END, false);
        myDataRegistry.addModels(deposit);
    }
}
