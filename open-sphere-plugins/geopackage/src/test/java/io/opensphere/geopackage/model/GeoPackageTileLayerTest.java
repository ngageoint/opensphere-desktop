package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.junit.Test;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/**
 * Unit test for the {@link GeoPackageTileLayer} class.
 */
public class GeoPackageTileLayerTest
{
    /**
     * Tests serializing and deserializing this class.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void test() throws IOException, ClassNotFoundException
    {
        GeoPackageTileLayer layer = new GeoPackageTileLayer("thePackage", "c:\\somefile.gpkg", "myName", 714);
        layer.setMinZoomLevel(8);
        layer.setMaxZoomLevel(11);
        Map<Long, TileMatrix> zoomToMatrix = New.map();
        zoomToMatrix.put(8L, new TileMatrix(20, 20));
        layer.getZoomLevelToMatrix().putAll(zoomToMatrix);
        GeographicBoundingBox boundingBox = new GeographicBoundingBox(LatLonAlt.createFromDegrees(10, 10),
                LatLonAlt.createFromDegrees(11, 11));
        layer.setBoundingBox(boundingBox);

        layer.getExtensions().put("imageFormat", "mesh");

        assertEquals("c:\\somefile.gpkgmyName", layer.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(layer);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        GeoPackageTileLayer actual = (GeoPackageTileLayer)objectIn.readObject();

        assertEquals("thePackage", actual.getPackageName());
        assertEquals("c:\\somefile.gpkg", actual.getPackageFile());
        assertEquals("myName", actual.getName());
        assertEquals(LayerType.TILE, actual.getLayerType());
        assertEquals(714, actual.getRecordCount());
        assertEquals(8, actual.getMinZoomLevel());
        assertEquals(11, actual.getMaxZoomLevel());
        assertEquals(layer.getId(), actual.getId());
        assertEquals(zoomToMatrix, actual.getZoomLevelToMatrix());
        assertEquals(boundingBox, actual.getBoundingBox());
        assertEquals("mesh", actual.getExtensions().get("imageFormat"));
    }
}
