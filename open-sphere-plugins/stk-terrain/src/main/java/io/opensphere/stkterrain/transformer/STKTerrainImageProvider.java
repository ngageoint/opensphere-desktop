package io.opensphere.stkterrain.transformer;

import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.ZYXKeyPropertyMatcher;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.QueryTracker;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.util.Constants;

/**
 * The image provider used to get the quantized mesh for a specific tile.
 */
public class STKTerrainImageProvider implements ImageProvider<ZYXImageKey>
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(STKTerrainImageProvider.class);

    /**
     * Used to query for the quantized mesh.
     */
    private final DataRegistry myDataRegistry;

    /**
     * The url to the terrain server.
     */
    private final String myServerUrl;

    /**
     * The name of the tile set the tile belongs to.
     */
    private final String myTileSetName;

    /**
     * Constructs a new STK terrain image provider that provides
     * {@link QuantizedMesh} for specified tiles.
     *
     * @param dataRegistry Used to query for the quantized mesh.
     * @param serverUrl The url to the terrain server.
     * @param tileSetName The name of the tile set the tile belongs to.
     */
    public STKTerrainImageProvider(DataRegistry dataRegistry, String serverUrl, String tileSetName)
    {
        myDataRegistry = dataRegistry;
        myServerUrl = serverUrl;
        myTileSetName = tileSetName;
    }

    @Override
    public Image getImage(ZYXImageKey key)
    {
        DataModelCategory category = new DataModelCategory(myServerUrl, QuantizedMesh.class.getName(), myTileSetName);
        List<PropertyMatcher<?>> matchers = New.list();
        ZYXKeyPropertyMatcher keyMatcher = new ZYXKeyPropertyMatcher(Constants.KEY_PROPERTY_DESCRIPTOR, key);
        matchers.add(keyMatcher);

        SimpleQuery<QuantizedMesh> query = new SimpleQuery<>(category, Constants.QUANTIZED_MESH_PROPERTY_DESCRIPTOR, matchers);

        QueryTracker tracker = myDataRegistry.performQuery(query);

        QuantizedMesh mesh = null;
        if (query.getResults() != null && !query.getResults().isEmpty())
        {
            mesh = query.getResults().get(0);
        }
        else if (tracker.getException() != null)
        {
            LOGGER.error(tracker.getException(), tracker.getException());
        }

        return mesh;
    }
}
