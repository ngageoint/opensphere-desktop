package io.opensphere.arcgis2.envoy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import io.opensphere.arcgis2.esri.EsriFullLayer;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.SimpleEnvoy;
import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.QueryException;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.server.util.JsonUtils;

/** Envoy that gets ArcGIS layer information. */
public class ArcGISDescribeLayerEnvoy extends SimpleEnvoy<EsriFullLayer>
{
    /** The {@link PropertyDescriptor} for the describe layer results. */
    private static final PropertyDescriptor<EsriFullLayer> DESCRIBE_LAYER_DESCRIPTOR = new PropertyDescriptor<>("DescribeLayer",
            EsriFullLayer.class);

    /** The describe layer data model category family. */
    private static final String DESCRIBE_LAYER_FAMILY = "ArcGIS.DescribeLayer";

    /**
     * Helper method for a client to query this envoy.
     *
     * @param dataRegistry the data registry
     * @param layerUrl the layer URL to query
     * @return the layer info
     * @throws QueryException if something goes wrong with the query
     */
    public static EsriFullLayer query(DataRegistry dataRegistry, String layerUrl) throws QueryException
    {
        DataModelCategory category = new DataModelCategory(null, DESCRIBE_LAYER_FAMILY, layerUrl);
        SimpleQuery<EsriFullLayer> query = new SimpleQuery<>(category, DESCRIBE_LAYER_DESCRIPTOR);
        QueryTracker tracker = dataRegistry.performQuery(query);
        if (tracker.getQueryStatus() == QueryTracker.QueryStatus.SUCCESS)
        {
            EsriFullLayer layer = query.getResults().iterator().next();
            return layer;
        }
        else
        {
            throw new QueryException(tracker.getException().getMessage(), tracker.getException());
        }
    }

    /**
     * Constructor.
     *
     * @param toolbox The toolbox.
     */
    public ArcGISDescribeLayerEnvoy(Toolbox toolbox)
    {
        super(toolbox);
    }

    @Override
    public boolean providesDataFor(DataModelCategory category)
    {
        return DESCRIBE_LAYER_FAMILY.equals(category.getFamily());
    }

    @Override
    protected URL getUrl(DataModelCategory category) throws MalformedURLException
    {
        return new URL(category.getCategory() + "?f=json");
    }

    @Override
    protected Collection<EsriFullLayer> parseDepositItems(CancellableInputStream inputStream) throws IOException
    {
        EsriFullLayer fullLayer = JsonUtils.createMapper().readValue(inputStream, EsriFullLayer.class);
        return Collections.singleton(fullLayer);
    }

    @Override
    protected CacheDeposit<EsriFullLayer> createDeposit(DataModelCategory category, Collection<? extends EsriFullLayer> items)
    {
        return new DefaultCacheDeposit<>(category.withSource(getClass().getName()),
                Collections.singleton(SerializableAccessor.getHomogeneousAccessor(DESCRIBE_LAYER_DESCRIPTOR)), items, true,
                TimeInstant.get().plus(new Days(7)).toDate(), true);
    }
}
