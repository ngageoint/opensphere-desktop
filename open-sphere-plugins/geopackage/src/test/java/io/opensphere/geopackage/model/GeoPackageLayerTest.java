package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

/**
 * Tests the {@link GeoPackageLayer} class.
 */
public class GeoPackageLayerTest
{
    /**
     * Tests serializing and deserializing a {@link GeoPackageLayer}.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        GeoPackageLayer layer = new GeoPackageLayer("Test Package", "c:\test.gpkg", "aLayer", LayerType.TILE, 123);

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);

        out.writeObject(layer);

        byte[] bytes = byteOut.toByteArray();

        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(byteIn);

        GeoPackageLayer actual = (GeoPackageLayer)in.readObject();

        assertEquals("c:\test.gpkg", actual.getPackageFile());
        assertEquals("Test Package", actual.getPackageName());
        assertEquals("aLayer", actual.getName());
        assertEquals(LayerType.TILE, actual.getLayerType());
        assertEquals(123, actual.getRecordCount());
    }
}
