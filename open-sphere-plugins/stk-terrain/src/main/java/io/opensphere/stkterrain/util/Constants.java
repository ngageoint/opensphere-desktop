package io.opensphere.stkterrain.util;

import java.util.Map;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.units.duration.Weeks;
import io.opensphere.core.util.collections.New;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;

/**
 * Constains constant values used by the STK terrain classes.
 */
public final class Constants
{
    /**
     * The string value to prepend to any STK terrain envoy's thread pool name.
     */
    public static final String ENVOY_THREAD_POOL_NAME = "STKTerrain:";

    /**
     * The image format the transformers use for their tiles.
     */
    public static final String IMAGE_FORMAT = "quantizedmesh";

    /** Property descriptor for keys used in the data registry. */
    public static final PropertyDescriptor<String> KEY_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("key", String.class);

    /**
     * The provider type of the data type.
     */
    public static final String PROVIDER_TYPE = "STK Terrain";

    /**
     * The accept header to use for quantized mesh requests.
     */
    public static final Map<String, String> QUANTIZED_MESH_ACCEPT_HEADER = New.map();

    /** The descriptor for the tile sets property. */
    public static final PropertyDescriptor<QuantizedMesh> QUANTIZED_MESH_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(
            "quantizedMesh", QuantizedMesh.class);

    /**
     * The expiration time of the terrain tiles within the data registry.
     */
    public static final Weeks TILE_EXPIRATION = new Weeks(4);

    /**
     * The url to get all the layers from an STK terrain server.
     */
    public static final String TILE_SETS_URL = "";

    /**
     * The url to the tiles service.
     */
    public static final String TILES_URL = "";

    /**
     * The url to append in order to query for a TileSets metadata.
     */
    public static final String TILE_METADATA_URL = TILES_URL + "/layer.json";

    /** The descriptor for the tile sets property. */
    public static final PropertyDescriptor<TileSetMetadata> TILESET_METADATA_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(
            "tileSetMetadata", TileSetMetadata.class);

    /** The descriptor for the tile sets property. */
    public static final PropertyDescriptor<TileSet> TILESET_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>("tileSet",
            TileSet.class);

    /**
     * Static initializer.
     */
    static
    {
        QUANTIZED_MESH_ACCEPT_HEADER.put("Accept", "application/vnd.quantized-mesh,application/octet-stream;q=0.9");
    }

    /**
     * Not constructible.
     */
    private Constants()
    {
    }
}
