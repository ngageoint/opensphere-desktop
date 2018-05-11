package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import io.opensphere.core.util.collections.New;

/**
 * Unit tests the {@link GeoPackageFeatureLayer} class.
 */
@SuppressWarnings("boxing")
public class GeoPackageFeatureLayerTest
{
    /**
     * Tests serializing and deserializing the class.
     *
     * @throws IOException Bad IO.
     * @throws ClassNotFoundException Bad class.
     */
    @Test
    public void test() throws IOException, ClassNotFoundException
    {
        String packageName = "I am package";
        String packageFile = "c:\\somepackage.gpkg";
        String name = "MyName";

        List<Map<String, Serializable>> rows = createTestData();

        GeoPackageFeatureLayer layer = new GeoPackageFeatureLayer(packageName, packageFile, name, rows.size());
        layer.getData().addAll(rows);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        objectOut.writeObject(layer);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);

        GeoPackageFeatureLayer actual = (GeoPackageFeatureLayer)objectIn.readObject();

        assertEquals(packageName, actual.getPackageName());
        assertEquals(packageFile, actual.getPackageFile());
        assertEquals(name, actual.getName());
        assertEquals(LayerType.FEATURE, actual.getLayerType());
        assertEquals(rows.size(), actual.getRecordCount());
        assertEquals(rows, actual.getData());
    }

    /**
     * Create data to test with.
     *
     * @return The test data.
     */
    private List<Map<String, Serializable>> createTestData()
    {
        List<Map<String, Serializable>> rows = New.list();
        for (int i = 0; i < 3; i++)
        {
            Map<String, Serializable> row = New.map();
            row.put("column1", "value" + i);
            row.put("column2", 10 + i);
            row.put("column3", 54.3 + i);

            Coordinate coord = new Coordinate(5 + i, 6 + i);
            Point location = new GeometryFactory().createPoint(coord);
            row.put(GeoPackageColumns.GEOMETRY_COLUMN, location);

            rows.add(row);
        }

        return rows;
    }
}
