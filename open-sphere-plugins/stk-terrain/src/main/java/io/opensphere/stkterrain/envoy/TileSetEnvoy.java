package io.opensphere.stkterrain.envoy;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.collections.New;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.model.TileSetDataSource;
import io.opensphere.stkterrain.util.Constants;

/**
 * The envoy that goes and gets all TileSets that an STK Terrain Server has.
 */
public class TileSetEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(TileSetEnvoy.class);

    /**
     * The url string to the STK Terrain server.
     */
    private final String myServerUrl;

    /**
     * Used to notify if this server is valid STK terrain server or not.
     */
    private final DefaultValidatorSupport myValidatorSupport;

    /**
     * Constructs a new {@link TileSetEnvoy}.
     *
     * @param toolbox The system toolbox.
     * @param validatorSupport Used to notify if this server is valid STK
     *            terrain server or not.
     * @param serverUrl The url to the STK Terrain Server.
     */
    public TileSetEnvoy(Toolbox toolbox, DefaultValidatorSupport validatorSupport, String serverUrl)
    {
        super(toolbox);
        myServerUrl = serverUrl;
        myValidatorSupport = validatorSupport;
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
        return Constants.ENVOY_THREAD_POOL_NAME + myServerUrl;
    }

    /**
     * Gets the validator support that is notified when the envoy is opened.
     *
     * @return The validator support.
     */
    public DefaultValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    @Override
    public void open()
    {
        try
        {
            query(new DataModelCategory(null, TileSet.class.getName(), null), New.collection(), New.list(), New.list(), -1,
                    New.collection(), new CacheDepositReceiver()
                    {
                        @Override
                        public <T> long[] receive(CacheDeposit<T> deposit)
                        {
                            myValidatorSupport.setValidationResult(ValidationStatus.VALID, null);
                            return getDataRegistry().addModels(deposit);
                        }
                    });
        }
        catch (InterruptedException | QueryException e)
        {
            myValidatorSupport.setValidationResult(ValidationStatus.ERROR, e.getMessage());
            LOGGER.error(e, e);
        }
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        boolean isServer = category.getSource() == null || myServerUrl.equals(category.getSource());
        return isServer && TileSet.class.getName().equals(category.getFamily());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
                throws InterruptedException, QueryException
    {
        try
        {
            TileSet[] tileSets = new TileSet[1];
            tileSets[0] = new TileSet();
            tileSets[0].getDataSources().add(new TileSetDataSource());
            tileSets[0].setName("world");
            tileSets[0].getDataSources().get(0).setName("world");

            Collection<PropertyAccessor<TileSet, ?>> accessors = New.collection();
            accessors.add(SerializableAccessor.getHomogeneousAccessor(Constants.TILESET_PROPERTY_DESCRIPTOR));

            CacheDeposit<TileSet> deposit = new DefaultCacheDeposit<TileSet>(
                    new DataModelCategory(myServerUrl, TileSet.class.getName(), TileSet.class.getName()), accessors,
                    New.list(tileSets), true, DefaultCacheDeposit.SESSION_END, false);
            queryReceiver.receive(deposit);

        }
        catch (CacheException e)
        {
            throw new QueryException(e.getMessage(), e);
        }
    }
}
