package io.opensphere.xyztile.util;

import java.io.InputStream;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.units.duration.Hours;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/** XYZ Tile Utilities. */
public final class XYZTileUtils
{
    /** Property descriptor for images used in the data registry. */
    public static final PropertyDescriptor<InputStream> IMAGE_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("value",
            InputStream.class, 132044L);

    /** Property descriptor for keys used in the data registry. */
    public static final PropertyDescriptor<String> KEY_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("key", String.class);

    /**
     * The {@link PropertyDescriptor} for the {@link XYZTileLayerInfo} class.
     */
    public static final PropertyDescriptor<XYZTileLayerInfo> LAYERS_DESCRIPTOR = new PropertyDescriptor<>("layers",
            XYZTileLayerInfo.class);

    /** The layers data model category family. */
    public static final String LAYERS_FAMILY = "XYZ.Layers";

    /**
     * The expiration time of the XYZ tiles within the data registry.
     */
    public static final Hours TILE_EXPIRATION = new Hours(168);

    /** The tiles data model category family. */
    public static final String TILES_FAMILY = "XYZ.Tiles";

    /** The provider type. */
    public static final String XYZ_PROVIDER = "XYZ.Provider";

    /**
     * Creates a layers data model category.
     *
     * @param url the URL
     * @param provider The provider we want to produce the layer data.
     * @return the data model category
     */
    public static DataModelCategory newLayersCategory(String url, String provider)
    {
        return new DataModelCategory(url, XYZTileUtils.LAYERS_FAMILY, provider);
    }

    /** Disallow instantiation. */
    private XYZTileUtils()
    {
    }
}
