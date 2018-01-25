package io.opensphere.osh.envoy;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Query;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.osh.util.OSHRegistryUtils;

/** OpenSensorHub get result envoy. */
public class OSHGetResultEnvoy extends AbstractOSHEnvoy
{
    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public OSHGetResultEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return OSHRegistryUtils.GET_RESULT_FAMILY.equals(category.getFamily());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws QueryException
    {
        for (Satisfaction sat : satisfactions)
        {
            Collection<? extends TimeSpan> timeSpans = sat.getIntervalPropertyValueSet()
                    .getValues(TimeSpanAccessor.PROPERTY_DESCRIPTOR);
            for (TimeSpan timeSpan : timeSpans)
            {
                query(category, timeSpan, queryReceiver);
            }
        }
    }

    /**
     * Performs the query.
     *
     * @param category The data model category.
     * @param timeSpan The time span to query.
     * @param queryReceiver An object that will receive {@link Query} objects
     *            produced by this data provider.
     * @throws QueryException If there is a problem with the query.
     */
    void query(DataModelCategory category, TimeSpan timeSpan, CacheDepositReceiver queryReceiver) throws QueryException
    {
        URL baseUrl = buildUrl(category, timeSpan);

        try
        {
            CancellableInputStream responseStream = performRequest(baseUrl);
            Collection<CancellableInputStream> results = Collections.singleton(responseStream);

            Collection<PropertyAccessor<CancellableInputStream, ?>> accessors = New.list(2);
            accessors.add(UnserializableAccessor.getHomogeneousAccessor(OSHRegistryUtils.GET_RESULT_DESCRIPTOR));
            accessors.add(new TimeSpanAccessor<CancellableInputStream>(timeSpan)
            {
                @Override
                public TimeSpan access(CancellableInputStream input)
                {
                    return getExtent();
                }
            });
            DefaultCacheDeposit<CancellableInputStream> deposit = new DefaultCacheDeposit<>(category, accessors, results, true,
                    new Date(), false);
            queryReceiver.receive(deposit);
        }
        catch (IOException | CacheException e)
        {
            throw new QueryException(e);
        }
    }

    /**
     * Builds the URL for the category.
     *
     * @param category The data model category.
     * @param timeSpan The time span to query.
     * @return the URL
     */
    private URL buildUrl(DataModelCategory category, TimeSpan timeSpan)
    {
        StringBuilder urlString = new StringBuilder(300);
        urlString.append(OSHRegistryUtils.getUrl(category));
        urlString.append("?service=SOS&version=2.0&request=GetResult&offering=").append(OSHRegistryUtils.getOfferingId(category));
        urlString.append("&observedProperty=").append(OSHRegistryUtils.getProperty(category));
        urlString.append("&temporalFilter=phenomenonTime,").append(getTimeString(timeSpan));
        return UrlUtilities.toURL(urlString.toString());
    }

    /**
     * Gets the time query string for the span.
     *
     * @param timeSpan The time span to query.
     * @return the time query string
     */
    static String getTimeString(TimeSpan timeSpan)
    {
        String timeString;
        if (timeSpan.isZero() || timeSpan.isTimeless())
        {
            timeString = "now";
        }
        else if (timeSpan.isUnboundedStart())
        {
            timeString = "now/" + DateTimeUtilities.generateISO8601DateString(timeSpan.getEndDate());
        }
        else if (timeSpan.isUnboundedEnd())
        {
            timeString = DateTimeUtilities.generateISO8601DateString(timeSpan.getStartDate()) + "/now";
        }
        else if (timeSpan.isInstantaneous())
        {
            timeString = DateTimeUtilities.generateISO8601DateString(timeSpan.getStartDate());
        }
        else
        {
            timeString = timeSpan.toISO8601String();
        }
        return timeString;
    }
}
