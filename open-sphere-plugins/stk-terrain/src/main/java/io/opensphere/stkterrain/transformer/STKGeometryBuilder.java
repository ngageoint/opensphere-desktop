package io.opensphere.stkterrain.transformer;

import java.util.List;

import io.opensphere.core.collada.jaxb.Geometry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractTileGeometry.Divider;
import io.opensphere.core.geometry.ImageManager;
import io.opensphere.core.geometry.TerrainTileGeometry;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.ZYXImageKey;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.model.TileSetMetadata;

/**
 * Builds the initial STK {@link Geometry}.
 */
public class STKGeometryBuilder
{
    /**
     * Passed down to the image providers.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Constructs a new geometry builder.
     *
     * @param dataRegistry Passed down to the geometry's {@link ImageProvider}.
     */
    public STKGeometryBuilder(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    /**
     * Generates the initial geometries.
     *
     * @param typeKey The {@link DataTypeInfo} key.
     * @param serverUrl The url to the STK terrain server.
     * @param tileSetName The name of the tile set and the name of the
     *            {@link DataTypeInfo}.
     * @param tileSetMetadata Metadata describing the {@link TileSet}.
     * @param divider The divider used to make smaller tiles.
     * @param props The {@link DataTypeInfo}'s render properties.
     * @return The initial terrain geometries at zoom level 0, top most view.
     */
    public List<TerrainTileGeometry> buildInitialGeometries(String typeKey, String serverUrl, String tileSetName,
            TileSetMetadata tileSetMetadata, Divider<GeographicPosition> divider, TileRenderProperties props)
    {
        List<TerrainTileGeometry> geometries = New.list();

        List<ZYXImageKey> keys = New.list(
                new ZYXImageKey(0, 0, 0,
                        new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, -180), LatLonAlt.createFromDegrees(90, 0))),
                new ZYXImageKey(0, 0, 1,
                        new GeographicBoundingBox(LatLonAlt.createFromDegrees(-90, 0), LatLonAlt.createFromDegrees(90, 180))));

        for (ZYXImageKey key : keys)
        {
            TerrainTileGeometry.Builder<GeographicPosition> builder = new TerrainTileGeometry.Builder<>();
            STKElevationImageReader meshReader = new STKElevationImageReader(key.getBounds(), tileSetMetadata.getProjection(),
                    typeKey);
            builder.setElevationReader(meshReader);
            builder.setBounds(key.getBounds());
            ImageManager imageManager = new ImageManager(key,
                    new STKTerrainImageProvider(myDataRegistry, serverUrl, tileSetName));
            builder.setImageManager(imageManager);
            builder.setMaximumDisplaySize(250);
            builder.setMinimumDisplaySize(50);
            builder.setDivider(divider);
            geometries.add(new TerrainTileGeometry(builder, props, typeKey));
        }

        return geometries;
    }
}
