package io.opensphere.stkterrain.envoy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

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
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.server.util.JsonUtils;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * The envoy that gets metadata for a specified terrain TileSet.
 */
public class TileSetMetadataEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /**
     * The url to the STK Terrain Server.
     */
    private final String myServerUrl;

    /**
     * Constructs a new metadata envoy.
     *
     * @param toolbox The system toolbox.
     * @param serverUrl The url to the STK Terrain Server.
     */
    public TileSetMetadataEnvoy(Toolbox toolbox, String serverUrl)
    {
        super(toolbox);
        myServerUrl = serverUrl;
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

    @Override
    public void open()
    {
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        boolean isServer = category.getSource() == null || myServerUrl.equals(category.getSource());
        return isServer && TileSetMetadata.class.getName().equals(category.getFamily())
                && !StringUtils.isBlank(category.getCategory());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
                throws InterruptedException, QueryException
    {
        try
        {
            URL url = new URL(myServerUrl + Constants.TILE_SETS_URL + "/" + category.getCategory() + Constants.TILE_METADATA_URL);
            HttpServer server = getToolbox().getServerProviderRegistry().getProvider(HttpServer.class).getServer(url);
            ResponseValues responseValues = new ResponseValues();
            CancellableInputStream stream = server.sendGet(url, responseValues);
            if (responseValues.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                ObjectMapper mapper = JsonUtils.createMapper();
                TileSetMetadata tileSetMetadata = mapper.readValue(stream, TileSetMetadata.class);

                Collection<PropertyAccessor<TileSetMetadata, ?>> accessors = New.collection();
                accessors.add(SerializableAccessor.<TileSetMetadata, TileSetMetadata>getSingletonAccessor(Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR,
                        tileSetMetadata));

                CacheDeposit<TileSetMetadata> deposit = new DefaultCacheDeposit<TileSetMetadata>(
                        new DataModelCategory(myServerUrl, category.getFamily(), category.getCategory()), accessors,
                        New.list(tileSetMetadata), true, DefaultCacheDeposit.SESSION_END, false);
                queryReceiver.receive(deposit);
            }
            else
            {
                StreamReader errorMessageReader = new StreamReader(stream);
                throw new QueryException(url.toString() + " returned code " + responseValues.getResponseCode() + " "
                        + responseValues.getResponseMessage() + " message "
                        + errorMessageReader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
            }
        }
        catch (IOException | URISyntaxException | CacheException e)
        {
            throw new QueryException(e.getMessage(), e);
        }
    }
}
