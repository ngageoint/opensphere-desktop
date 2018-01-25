package io.opensphere.geopackage.model;

import java.io.InputStream;

import io.opensphere.core.cache.util.PropertyDescriptor;

/**
 * Contains geopackage property descriptors.
 */
public final class GeoPackagePropertyDescriptors
{
    /** The descriptor for the GeoPackageLayer property. */
    public static final PropertyDescriptor<GeoPackageLayer> GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(
            "geopackageLayer", GeoPackageLayer.class);

    /** The descriptor for the GeoPackageLayer property. */
    public static final PropertyDescriptor<GeoPackageTile> GEOPACKAGE_TILE_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(
            "geopackageTile", GeoPackageTile.class);

    /** Property descriptor for images used in the data registry. */
    public static final PropertyDescriptor<InputStream> IMAGE_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("value",
            InputStream.class, 132044L);

    /** Property descriptor for keys used in the data registry. */
    public static final PropertyDescriptor<String> KEY_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("key", String.class);

    /** Property descriptor for zoom level value of geo tiles. */
    public static final PropertyDescriptor<Long> ZOOM_LEVEL_PROPERTY_DESCRIPTOR = PropertyDescriptor.create("zoomLevel",
            Long.class);

    /**
     * Not constructible.
     */
    private GeoPackagePropertyDescriptors()
    {
    }
}
