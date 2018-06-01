package io.opensphere.infinity.envoy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.SimpleEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.UnserializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.server.util.JsonUtils;

/** Infinity envoy. */
public class InfinityEnvoy extends SimpleEnvoy<SearchResponse>
{
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
