package io.opensphere.stkterrain.envoy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.SingleSatisfaction;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.cache.util.IntervalPropertyValueSet;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.CacheDepositReceiver;
import io.opensphere.core.data.DataRegistryDataProvider;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.util.Constants;

/**
 * Envoy that goes out to an STK Terrain Server and gets a QuantizedMesh tile
 * for given TMS coordinates.
 */
public class QuantizedMeshEnvoy extends AbstractEnvoy implements DataRegistryDataProvider
{
    /**
     * The url to an STK Terrain Server.
     */
    private final String myServerUrl;

    /**
     * Constructs a new envoy.
     *
     * @param toolbox The system toolbox.
     * @param serverUrl The url to an STK Terrain Server.
     */
    public QuantizedMeshEnvoy(Toolbox toolbox, String serverUrl)
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
        return isServer && QuantizedMesh.class.getName().equals(category.getFamily())
                && !StringUtils.isBlank(category.getCategory());
    }

    @Override
    public void query(DataModelCategory category, Collection<? extends Satisfaction> satisfactions,
            List<? extends PropertyMatcher<?>> parameters, List<? extends OrderSpecifier> orderSpecifiers, int limit,
            Collection<? extends PropertyDescriptor<?>> propertyDescriptors, CacheDepositReceiver queryReceiver)
        throws InterruptedException, QueryException
    {
        if (parameters.size() != 1 || !(parameters.get(0) instanceof ZYXKeyPropertyMatcher))
        {
            throw new IllegalArgumentException(ZYXKeyPropertyMatcher.class.getSimpleName() + " was not found in parameters.");
        }

        ZYXKeyPropertyMatcher param = (ZYXKeyPropertyMatcher)parameters.get(0);
        ZYXImageKey key = param.getImageKey();
        TileSetMetadata metadata = getTileMetadata(category.getCategory());

        if (metadata != null)
        {
            String tmsString = metadata.getTiles().get(0);
            tmsString = tmsString.replace("{z}", String.valueOf(key.getZ()));
            tmsString = tmsString.replace("{y}", String.valueOf(key.getY()));
            tmsString = tmsString.replace("{x}", String.valueOf(key.getX()));
            tmsString = tmsString.replace("{version}", String.valueOf(metadata.getVersion()));

            try
            {
                URL url = new URL(myServerUrl + Constants.TILE_SETS_URL + "/" + category.getCategory() + Constants.TILES_URL + "/"
                        + tmsString);
                HttpServer server = getToolbox().getServerProviderRegistry().getProvider(HttpServer.class).getServer(url);
                ResponseValues response = new ResponseValues();
                try
                {
                    try (CancellableInputStream stream = server.sendGet(url, Constants.QUANTIZED_MESH_ACCEPT_HEADER, response))
                    {
                        if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                        {
                            StreamReader meshReader = new StreamReader(stream);
                            ByteBuffer buffer = meshReader.readStreamIntoBuffer();

                            QuantizedMesh mesh = new QuantizedMesh(buffer);
                            Collection<PropertyAccessor<QuantizedMesh, ?>> accessors = New.collection();
                            accessors.add(SerializableAccessor.<QuantizedMesh, String>getSingletonAccessor(
                                    Constants.KEY_PROPERTY_DESCRIPTOR, param.getOperand()));
                            accessors.add(SerializableAccessor.<QuantizedMesh, QuantizedMesh>getSingletonAccessor(
                                    Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR, mesh));

                            CacheDeposit<QuantizedMesh> deposit = new DefaultCacheDeposit<QuantizedMesh>(
                                    new DataModelCategory(myServerUrl, category.getFamily(), category.getCategory()), accessors,
                                    Collections.singleton(mesh), true, TimeInstant.get().plus(Constants.TILE_EXPIRATION).toDate(),
                                    false);
                            queryReceiver.receive(deposit);
                        }
                        else if (response.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND)
                        {
                            StreamReader errorMessageReader = new StreamReader(stream);
                            throw new QueryException(url.toString() + " returned code " + response.getResponseCode() + " "
                                    + response.getResponseMessage() + " message "
                                    + errorMessageReader.readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
                        }
                    }
                }
                catch (IOException | URISyntaxException | CacheException e)
                {
                    throw new QueryException(e.getMessage(), e);
                }
            }
            catch (MalformedURLException e)
            {
                throw new QueryException(e.getMessage(), e);
            }
        }
        else
        {
            throw new QueryException("Could not retrieve terrain for " + category.toString() + " " + key.toString());
        }
    }

    /**
     * Retrieves the {@link TileSetMetadata} for the given tile set name.
     *
     * @param tileSetName The name of the tile set to get metadata for.
     * @return The tile set metadata.
     */
    private TileSetMetadata getTileMetadata(String tileSetName)
    {
        TileSetMetadata metadata = null;

        SimpleQuery<TileSetMetadata> query = new SimpleQuery<>(
                new DataModelCategory(myServerUrl, TileSetMetadata.class.getName(), tileSetName),
                Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR);
        long[] ids = getDataRegistry().performLocalQuery(query);

        if (ids != null && ids.length > 0)
        {
            metadata = query.getResults().get(0);
        }

        return metadata;
    }
}
