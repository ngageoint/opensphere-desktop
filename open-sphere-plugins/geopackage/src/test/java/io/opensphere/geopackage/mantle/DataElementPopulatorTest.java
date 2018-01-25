package io.opensphere.geopackage.mantle;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.geopackage.model.GeoPackageColumns;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;

/**
 * Tests the {@link DataElementPopulator} class.
 */
public class DataElementPopulatorTest
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
     * Another test column for time values.
     */
    private static final String ourColumn4 = "column4";

    /**
     * Tests creating a {@link DataElement} for a test geopackage row.
     */
    @Test
    public void testPopulateDataElement()
    {
        EasyMockSupport support = new EasyMockSupport();

        Map<String, Serializable> row = New.map();

        row.put(ourColumn1, "value2");
        row.put(ourColumn2, 10d);
        row.put(ourColumn3, null);
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11, 12)));

        DataTypeInfo dataTypeInfo = createDataTypeInfo(support, null);

        support.replayAll();

        DataElementPopulator populator = new DataElementPopulator();

        DataElement element = populator.populateDataElement(row, dataTypeInfo);

        MetaDataProvider provider = element.getMetaData();

        assertEquals(row.get(ourColumn1), provider.getValue(ourColumn1));
        assertEquals(row.get(ourColumn2), provider.getValue(ourColumn2));
        assertEquals(row.get(ourColumn3), provider.getValue(ourColumn3));
        assertEquals(row.get(GeoPackageColumns.GEOMETRY_COLUMN), provider.getValue(GeoPackageColumns.GEOMETRY_COLUMN));

        assertEquals(Color.red, element.getVisualizationState().getColor());
        assertEquals(TimeSpan.TIMELESS, element.getTimeSpan());
        assertEquals(dataTypeInfo, element.getDataTypeInfo());

        SimpleMapPointGeometrySupport simpleSupport = (SimpleMapPointGeometrySupport)((MapDataElement)element)
                .getMapGeometrySupport();

        assertEquals(Color.red, simpleSupport.getColor());
        assertEquals(11, simpleSupport.getLocation().getLatD(), 0d);
        assertEquals(10, simpleSupport.getLocation().getLonD(), 0d);
        assertEquals(12, simpleSupport.getLocation().getAltitude().getMeters(), 0d);

        support.verifyAll();
    }

    /**
     * Tests populating a data element with time.
     */
    @Test
    public void testPopulateDataElementWithTime()
    {
        EasyMockSupport support = new EasyMockSupport();

        Map<String, Serializable> row = New.map();

        Date time = new Date();

        row.put(ourColumn1, "value2");
        row.put(ourColumn2, 10d);
        row.put(ourColumn3, null);
        row.put(ourColumn4, DateTimeUtilities.generateISO8601DateString(time));
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11, 12)));

        DataTypeInfo dataTypeInfo = createDataTypeInfo(support, ourColumn4);

        support.replayAll();

        DataElementPopulator populator = new DataElementPopulator();

        DataElement element = populator.populateDataElement(row, dataTypeInfo);

        MetaDataProvider provider = element.getMetaData();

        assertEquals(row.get(ourColumn1), provider.getValue(ourColumn1));
        assertEquals(row.get(ourColumn2), provider.getValue(ourColumn2));
        assertEquals(row.get(ourColumn3), provider.getValue(ourColumn3));
        assertEquals(row.get(GeoPackageColumns.GEOMETRY_COLUMN), provider.getValue(GeoPackageColumns.GEOMETRY_COLUMN));
        assertEquals(row.get(ourColumn4), provider.getValue(ourColumn4));

        assertEquals(TimeSpan.get(time), element.getTimeSpan());

        assertEquals(Color.red, element.getVisualizationState().getColor());
        assertEquals(dataTypeInfo, element.getDataTypeInfo());

        SimpleMapPointGeometrySupport simpleSupport = (SimpleMapPointGeometrySupport)((MapDataElement)element)
                .getMapGeometrySupport();

        assertEquals(Color.red, simpleSupport.getColor());
        assertEquals(11, simpleSupport.getLocation().getLatD(), 0d);
        assertEquals(10, simpleSupport.getLocation().getLonD(), 0d);
        assertEquals(12, simpleSupport.getLocation().getAltitude().getMeters(), 0d);

        support.verifyAll();
    }

    /**
     * Tests populating a data element with an invalid time value.
     */
    @Test
    public void testPopulateDataElementWithTimeButNA()
    {
        EasyMockSupport support = new EasyMockSupport();

        Map<String, Serializable> row = New.map();

        row.put(ourColumn1, "value2");
        row.put(ourColumn2, 10d);
        row.put(ourColumn3, null);
        row.put(ourColumn4, "N_A");
        row.put(GeoPackageColumns.GEOMETRY_COLUMN, new GeometryFactory().createPoint(new Coordinate(10, 11, 12)));

        DataTypeInfo dataTypeInfo = createDataTypeInfo(support, ourColumn4);

        support.replayAll();

        DataElementPopulator populator = new DataElementPopulator();

        DataElement element = populator.populateDataElement(row, dataTypeInfo);

        MetaDataProvider provider = element.getMetaData();

        assertEquals(row.get(ourColumn1), provider.getValue(ourColumn1));
        assertEquals(row.get(ourColumn2), provider.getValue(ourColumn2));
        assertEquals(row.get(ourColumn3), provider.getValue(ourColumn3));
        assertEquals(row.get(GeoPackageColumns.GEOMETRY_COLUMN), provider.getValue(GeoPackageColumns.GEOMETRY_COLUMN));
        assertEquals(row.get(ourColumn4), provider.getValue(ourColumn4));

        assertEquals(TimeSpan.TIMELESS, element.getTimeSpan());

        assertEquals(Color.red, element.getVisualizationState().getColor());
        assertEquals(dataTypeInfo, element.getDataTypeInfo());

        SimpleMapPointGeometrySupport simpleSupport = (SimpleMapPointGeometrySupport)((MapDataElement)element)
                .getMapGeometrySupport();

        assertEquals(Color.red, simpleSupport.getColor());
        assertEquals(11, simpleSupport.getLocation().getLatD(), 0d);
        assertEquals(10, simpleSupport.getLocation().getLonD(), 0d);
        assertEquals(12, simpleSupport.getLocation().getAltitude().getMeters(), 0d);

        support.verifyAll();
    }

    /**
     * Tests creating a {@link DataElement} for a test geopackage row.
     */
    @Test
    public void testPopulateDataElementNullGeometry()
    {
        EasyMockSupport support = new EasyMockSupport();

        Map<String, Serializable> row = New.map();

        row.put(ourColumn1, "value2");
        row.put(ourColumn2, 10d);
        row.put(ourColumn3, null);

        DataTypeInfo dataTypeInfo = createDataTypeInfo(support, null);

        support.replayAll();

        DataElementPopulator populator = new DataElementPopulator();

        DataElement element = populator.populateDataElement(row, dataTypeInfo);

        MetaDataProvider provider = element.getMetaData();

        assertEquals(row.get(ourColumn1), provider.getValue(ourColumn1));
        assertEquals(row.get(ourColumn2), provider.getValue(ourColumn2));
        assertEquals(row.get(ourColumn3), provider.getValue(ourColumn3));

        assertEquals(Color.red, element.getVisualizationState().getColor());
        assertEquals(TimeSpan.TIMELESS, element.getTimeSpan());
        assertEquals(dataTypeInfo, element.getDataTypeInfo());

        support.verifyAll();
    }

    /**
     * Mocks a {@link DataTypeInfo}.
     *
     * @param support Used to create the mock.
     * @param timeKey The time column, or null if there isn't one.
     * @return The mocked {@link DataTypeInfo}.
     */
    private DataTypeInfo createDataTypeInfo(EasyMockSupport support, String timeKey)
    {
        DefaultMetaDataInfo metaInfo = new DefaultMetaDataInfo();
        metaInfo.addKey(ourColumn1, String.class, this);
        metaInfo.addKey(ourColumn2, Integer.class, this);
        metaInfo.addKey(ourColumn3, Serializable.class, this);
        metaInfo.addKey(GeoPackageColumns.GEOMETRY_COLUMN, Geometry.class, this);
        if (timeKey != null)
        {
            metaInfo.addKey(timeKey, String.class, this);
            metaInfo.setSpecialKey(timeKey, TimeKey.DEFAULT, this);
        }

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metaInfo);

        BasicVisualizationInfo visInfo = support.createMock(BasicVisualizationInfo.class);
        EasyMock.expect(visInfo.getTypeColor()).andReturn(Color.red);

        EasyMock.expect(dataType.getBasicVisualizationInfo()).andReturn(visInfo);

        return dataType;
    }
}
