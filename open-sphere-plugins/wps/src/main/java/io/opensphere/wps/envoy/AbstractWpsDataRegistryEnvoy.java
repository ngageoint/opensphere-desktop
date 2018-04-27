package io.opensphere.wps.envoy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
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
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.server.services.ServerConnectionParams;
import io.opensphere.server.source.OGCServerSource;

/**
 * An abstract base implementation of an envoy, designed to be used to perform a
 * single WPS request type against a single server, and populating the results
 * into the {@link DataRegistry}.
 *
 * @param <RESPONSE_TYPE> the WPS type returned by the envoy.
 */
@SuppressWarnings("PMD.GenericsNaming")
public abstract class AbstractWpsDataRegistryEnvoy<RESPONSE_TYPE> extends AbstractWpsEnvoy<RESPONSE_TYPE>
        implements DataRegistryDataProvider
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(AbstractWpsDataRegistryEnvoy.class);

    /**
     * The descriptor with which data is stored to the data registry.
     */
    private final PropertyDescriptor<RESPONSE_TYPE> myPropertyDescriptor;

    /**
     * Creates a new envoy through which a WPS request is executed for a single
     * server.
     *
     * @param pToolbox the toolbox through which application interaction occurs.
     * @param pServer the configuration of the server to which the query will be
     *            made.
     * @param pRequestType The request type for which this envoy is configured.
     * @param pPropertyDescriptor The descriptor with which data is stored to
     *            the data registry.
     */
    public AbstractWpsDataRegistryEnvoy(Toolbox pToolbox, ServerConnectionParams pServer, WpsRequestType pRequestType,
            PropertyDescriptor<RESPONSE_TYPE> pPropertyDescriptor)
    {
        super(pToolbox, pServer, pRequestType);

        myPropertyDescriptor = pPropertyDescriptor;
    }

    /**
     * Gets the value of the {@link #myPropertyDescriptor} field.
     *
     * @return the value stored in the {@link #myPropertyDescriptor} field.
     */
    public PropertyDescriptor<RESPONSE_TYPE> getPropertyDescriptor()
    {
        return myPropertyDescriptor;
    }

    /**
     * Removes all elements associated with the category defined for the envoy.
     */
    public void clearCache()
    {
        DataModelCategory category = new DataModelCategory(getServer().getWpsUrl(), OGCServerSource.WPS_SERVICE,
                getRequestType().getValue());
        getToolbox().getDataRegistry().removeModels(category, false);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryDataProvider#providesDataFor(io.opensphere.core.data.util.DataModelCategory)
     */
    @Override
    public boolean providesDataFor(DataModelCategory pCategory)
    {
        return StringUtils.equals(getServer().getWpsUrl(), pCategory.getSource())
                && StringUtils.equals(OGCServerSource.WPS_SERVICE, pCategory.getFamily())
                && StringUtils.equals(getRequestType().getValue(), pCategory.getCategory());
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryDataProvider#getSatisfaction(io.opensphere.core.data.util.DataModelCategory,
     *      java.util.Collection)
     */
    @Override
    public Collection<? extends Satisfaction> getSatisfaction(DataModelCategory pDataModelCategory,
            Collection<? extends IntervalPropertyValueSet> pIntervalSets)
    {
        return SingleSatisfaction.generateSatisfactions(pIntervalSets);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryDataProvider#getThreadPoolName()
     */
    @Override
    public String getThreadPoolName()
    {
        return getClass().getSimpleName() + ":" + getServer().getWpsUrl();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.data.DataRegistryDataProvider#query(io.opensphere.core.data.util.DataModelCategory,
     *      java.util.Collection, java.util.List, java.util.List, int,
     *      java.util.Collection, io.opensphere.core.data.CacheDepositReceiver)
     */
    @SuppressWarnings("unchecked")
    @Override
    public synchronized void query(DataModelCategory pCategory, Collection<? extends Satisfaction> pSatisfactions,
            List<? extends PropertyMatcher<?>> pParameters, List<? extends OrderSpecifier> pOrderSpecifiers, int pLimit,
            Collection<? extends PropertyDescriptor<?>> pPropertyDescriptors, CacheDepositReceiver pQueryReceiver)
        throws InterruptedException, QueryException
    {
        if (pPropertyDescriptors.size() != 1)
        {
            throw new QueryException("Unable to handle more than one descriptor.");
        }

        PropertyDescriptor<RESPONSE_TYPE> propertyDescriptor = (PropertyDescriptor<RESPONSE_TYPE>)pPropertyDescriptors.iterator()
                .next();

        depositToDataRegistry(pCategory, pQueryReceiver, propertyDescriptor, executeQuery(getParameterMap(pParameters)));
    }

    /**
     * Stores the supplied response object into the Data Registry, by way of the
     * supplied {@link CacheDepositReceiver}.
     *
     * @param pCategory the category with which the data should be stored.
     * @param pQueryReceiver the query receiver for the Data Registry that will
     *            receive the supplied results.
     * @param pPropertyDescriptor the descriptor used to describe the format of
     *            the data to be placed into the data registry.
     * @param pData the response object to store in the data registry.
     * @throws QueryException if the data cannot be stored into the cache.
     */
    protected synchronized void depositToDataRegistry(DataModelCategory pCategory, CacheDepositReceiver pQueryReceiver,
            PropertyDescriptor<RESPONSE_TYPE> pPropertyDescriptor, RESPONSE_TYPE pData)
        throws QueryException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Depositing data to data registry from " + this.getClass().getName());
        }
        DataModelCategory category = new DataModelCategory(getServer().getWpsUrl(), pCategory.getFamily(),
                pCategory.getCategory());
        SimpleSessionOnlyCacheDeposit<RESPONSE_TYPE> deposit = new SimpleSessionOnlyCacheDeposit<>(category, pPropertyDescriptor,
                Collections.singleton(pData));
        try
        {
            long[] values = pQueryReceiver.receive(deposit);
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Recieved IDs from registry: " + Arrays.toString(values) + "(" + this.getClass().getName() + ")");
            }
        }
        catch (CacheException e)
        {
            throw new QueryException("Unable to store WPS " + getRequestType() + " Response from server '"
                    + getServer().getWpsUrl() + "' into cache", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.AbstractEnvoy#open()
     */
    @Override
    public void open()
    {
        String errorMessage = null;

        try
        {
            DataModelCategory category = new DataModelCategory(getServer().getWpsUrl(), OGCServerSource.WPS_SERVICE,
                    getRequestType().getValue());
            query(category, null, null, null, 1, Collections.singleton(getPropertyDescriptor()),
                    new WpsCacheDepositReceiver(getDataRegistry()));
        }
        catch (InterruptedException e)
        {
            LOG.error("Interrupted while executing query.", e);
            errorMessage = e.getMessage();
        }
        catch (QueryException e)
        {
            LOG.error("Exception encountered while executing query.", e);
            errorMessage = e.getMessage();
        }

        fireStateEvent(errorMessage);

        // Let listeners know about the envoy executor
        getToolbox().getEventManager().publishEvent(new WpsEnvoyExecutorEvent(getExecutor()));
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.AbstractEnvoy#close()
     */
    @Override
    public void close()
    {
        clearCache();

        super.close();
    }

    /**
     * Creates a parameter map unique to the implemented request type.
     *
     * @param pParameters the set of parameters from which to extract data.
     *
     * @return a Map in which required parameters are defined (may be null if
     *         none are needed).
     */
    protected abstract Map<String, String> getParameterMap(List<? extends PropertyMatcher<?>> pParameters);

    /**
     * Creates a parameter map unique to the implemented request type.
     *
     * @return a Map in which the configured parameters are defined (may be null
     *         if none are needed).
     */
    @Override
    protected Map<String, String> getParameterMap()
    {
        return null;
    }
}
