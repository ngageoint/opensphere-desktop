package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the property descriptors.
 */
public class GeoPackagePropertyDescriptorsTest
{
    /**
     * Tests the property descriptors.
     */
    @Test
    public void test()
    {
        assertEquals("geopackageLayer", GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR.getPropertyName());
        assertEquals(GeoPackageLayer.class, GeoPackagePropertyDescriptors.GEOPACKAGE_LAYER_PROPERTY_DESCRIPTOR.getType());
    }
}
