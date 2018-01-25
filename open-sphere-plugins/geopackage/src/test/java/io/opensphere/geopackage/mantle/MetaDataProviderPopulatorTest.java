package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;

/**
 * Tests the {@link MetaDataProviderPopulator} class.
 */
public class MetaDataProviderPopulatorTest
{
    /**
     * A test column name.
     */
    private static final String ourColumn1 = "column1";

    /**
     * A test column name.
     */
    private static final String ourColumn2 = "column2";

    /**
     * Another test column name.
     */
    private static final String ourColumn3 = "column3";

    /**
     * Tests creating a {@link MetaDataProvider}.
     */
    @Test
    public void testPopulateProvider()
    {
        DefaultMetaDataInfo metaInfo = new DefaultMetaDataInfo();
        metaInfo.addKey(ourColumn1, String.class, this);
        metaInfo.addKey(ourColumn2, Integer.class, this);
        metaInfo.addKey(ourColumn3, Serializable.class, this);
        metaInfo.addKey(GeoPackageColumns.GEOMETRY_COLUMN, Geometry.class, this);

        Map<String, Serializable> row = New.map();

        row.put(ourColumn1, "value2");
        row.put(ourColumn2, 10d);
        row.put(ourColumn3, null);
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11)));

        MetaDataProviderPopulator populator = new MetaDataProviderPopulator();

        MetaDataProvider provider = populator.populateProvider(row, metaInfo);

        assertEquals(row.get(ourColumn1), provider.getValue(ourColumn1));
        assertEquals(row.get(ourColumn2), provider.getValue(ourColumn2));
        assertEquals(row.get(ourColumn3), provider.getValue(ourColumn3));
        assertEquals(row.get(GeoPackageColumns.GEOMETRY_COLUMN), provider.getValue(GeoPackageColumns.GEOMETRY_COLUMN));
    }
}
