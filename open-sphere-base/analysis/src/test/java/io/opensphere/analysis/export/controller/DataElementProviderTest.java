package io.opensphere.analysis.export.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JTable;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.BeforeClass;
import org.junit.Test;

import io.opensphere.analysis.export.model.ColorFormat;
import io.opensphere.analysis.export.model.ExportOptionsModel;
import io.opensphere.analysis.export.model.LatLonFormat;
import io.opensphere.analysis.table.model.MetaColumn;
import io.opensphere.analysis.table.model.MetaColumnsTableModel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.SimpleMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;

/**
 * Unit tests the {@link DataElementProvider} class.
 */
public class DataElementProviderTest
{
    /**
     * The test columns.
     */
    private static final String[] ourColumns = new String[] { "DATE_TIME", "ROOM", "MESSAGE", "LAT", "LON" };

    /**
     * Sets up the timezone for all tests.
     */
    @BeforeClass
    public static void setupTimezone()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Tests providing data elements when user activates all export options.
     */
    @Test
    public void testAllOptionsSelected()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataElement> elements = createTestData(support);
        MetaColumnsTableModel tableModel = createTableModel(support, elements);
        DataElementCache cache = createElementCache(support, elements.get(0).getDataTypeInfo());

        support.replayAll();

        JTable table = new JTable();
        table.setModel(tableModel);

        ExportOptionsModel exportModel = new ExportOptionsModel();
        exportModel.setAddWkt(true);
        exportModel.setIncludeMetaColumns(true);
        exportModel.setSelectedColorFormat(ColorFormat.RGB_CODED);
        exportModel.setSelectedLatLonFormat(LatLonFormat.DMS);
        exportModel.setSelectedRowsOnly(true);
        exportModel.setSeparateDateTimeColumns(true);

        DataElementProvider provider = new DataElementProvider(exportModel, cache);

        List<DataElement> actuals = provider.provideElements(tableModel, table, 2);

        assertEquals(2, actuals.size());

        for (DataElement actual : actuals)
        {
            assertTrue(actual instanceof MapDataElement);
            assertDataElement(actual);
        }

        support.verifyAll();
    }

    /**
     * Tests providing data elements when user excepts all default export
     * options.
     */
    @Test
    public void testDefaults()
    {
        EasyMockSupport support = new EasyMockSupport();

        List<DataElement> elements = createTestData(support);
        MetaColumnsTableModel tableModel = createTableModel(support, elements);
        DataElementCache cache = createElementCache(support, elements.get(0).getDataTypeInfo());

        support.replayAll();

        JTable table = new JTable();
        table.setModel(tableModel);

        ExportOptionsModel exportModel = new ExportOptionsModel();
        DataElementProvider provider = new DataElementProvider(exportModel, cache);

        List<DataElement> actuals = provider.provideElements(tableModel, table, 2);

        int index = 0;
        for (DataElement expected : elements)
        {
            DataElement actual = actuals.get(index);
            assertTrue(actual instanceof MapDataElement);
            assertEquals(expected.getMetaData().getValues().size(), actual.getMetaData().getValues().size());
            assertTrue(expected.getMetaData().getValues().containsAll(actual.getMetaData().getValues()));
            assertEquals(((MapDataElement)expected).getMapGeometrySupport(), ((MapDataElement)actual).getMapGeometrySupport());
            index++;
        }

        support.verifyAll();
    }

    /**
     * Asserts the element.
     *
     * @param element The element to assert.
     */
    private void assertDataElement(DataElement element)
    {
        long id = element.getId();

        MetaDataProvider metadataProvider = element.getMetaData();

        String wkt = metadataProvider.getValue("WKT Geometry").toString();

        if (id == 0)
        {
            assertEquals("POINT (0 0)", wkt);
        }
        else
        {
            assertEquals("POINT (22.2 20.2)", wkt);
        }

        assertEquals(Integer.valueOf((int)id), metadataProvider.getValue("Index"));
        assertEquals("color[r=255,g=200,b=0,a=255]", metadataProvider.getValue("Color"));
        if (id == 0)
        {
            assertEquals("0°0'0\"N", metadataProvider.getValue("LAT (DMS)"));
            assertEquals("0°0'0\"E", metadataProvider.getValue("LON (DMS)"));
        }
        else
        {
            assertEquals("20°12'0\"N", metadataProvider.getValue("LAT (DMS)"));
            assertEquals("22°12'0\"E", metadataProvider.getValue("LON (DMS)"));
        }
        assertTrue(element.getVisualizationState().isSelected());
        assertEquals(TimeSpan.get(1000 + 1000 * id), metadataProvider.getValue("DATE_TIME"));
        assertEquals("1970-01-01", metadataProvider.getValue("DATE"));

        if (id == 0)
        {
            assertEquals("00:00:01.00", metadataProvider.getValue("TIME OF DAY"));
        }
        else
        {
            assertEquals("00:00:03.00", metadataProvider.getValue("TIME OF DAY"));
        }
    }

    /**
     * Creates an easy mocked {@link DataElementCache}.
     *
     * @param support Used to create the mock.
     * @param expectedDataType The expected {@link DataTypeInfo} to be passed to
     *            the {@link DataElementCache}.
     * @return The mocked {@link DataElementCache}.
     */
    private DataElementCache createElementCache(EasyMockSupport support, DataTypeInfo expectedDataType)
    {
        DirectAccessRetriever retriever = support.createMock(DirectAccessRetriever.class);
        EasyMock.expect(retriever.getMapGeometrySupport(EasyMock.anyLong())).andAnswer(this::getMapGeometrySupportAnswer)
                .anyTimes();

        DataElementCache cache = support.createMock(DataElementCache.class);

        EasyMock.expect(cache.getDirectAccessRetriever(EasyMock.eq(expectedDataType))).andReturn(retriever).anyTimes();

        return cache;
    }

    /**
     * Creates a mocked {@link MetaColumnsTableModel}.
     *
     * @param support Used to create the mock.
     * @param data The test data.
     * @return The mocked {@link MetaColumnsTableModel}.
     */
    private MetaColumnsTableModel createTableModel(EasyMockSupport support, List<DataElement> data)
    {
        List<MetaColumn<?>> metacolumns = New.list(new MockMetaColumn("Index"), new MockMetaColumn("Color"));
        MetaColumnsTableModel model = support.createMock(MetaColumnsTableModel.class);

        EasyMock.expect(Integer.valueOf(model.getRowCount())).andReturn(Integer.valueOf(data.size())).anyTimes();
        EasyMock.expect(model.getDataAt(EasyMock.anyInt())).andAnswer(() -> getDataAtAnswer(data)).anyTimes();
        EasyMock.expect(model.getMetaColumns()).andReturn(metacolumns).anyTimes();
        EasyMock.expect(Integer.valueOf(model.findColumn(EasyMock.cmpEq("Index")))).andReturn(Integer.valueOf(0)).anyTimes();
        EasyMock.expect(Integer.valueOf(model.findColumn(EasyMock.cmpEq("Color")))).andReturn(Integer.valueOf(1)).anyTimes();
        EasyMock.expect(model.getValueAt(EasyMock.anyInt(), EasyMock.eq(0))).andAnswer(this::getValueAtAnswer).anyTimes();
        EasyMock.expect(model.getValueAt(EasyMock.anyInt(), EasyMock.eq(1))).andReturn(Color.orange).anyTimes();
        model.addTableModelListener(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(Integer.valueOf(model.getColumnCount()))
                .andReturn(Integer.valueOf(ourColumns.length + metacolumns.size())).anyTimes();
        EasyMock.expect(model.getColumnName(EasyMock.anyInt())).andAnswer(() ->
        {
            int index = ((Integer)EasyMock.getCurrentArguments()[0]).intValue();
            String columnName = null;
            if (index < 2)
            {
                columnName = metacolumns.get(index).getColumnIdentifier();
            }
            else
            {
                columnName = ourColumns[index - 2];
            }
            return columnName;
        }).anyTimes();

        return model;
    }

    /**
     * Creates the test data to work with.
     *
     * @param support Used to mock the {@link DataTypeInfo} contained in the
     *            {@link DataElement}.
     * @return The test data to work with.
     */
    private List<DataElement> createTestData(EasyMockSupport support)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadataInfo.getLatitudeKey()).andReturn("LAT").anyTimes();
        EasyMock.expect(metadataInfo.getLongitudeKey()).andReturn("LON").anyTimes();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getMetaDataInfo()).andReturn(metadataInfo).anyTimes();

        // DATE_TIME, ROOM, MESSAGE, LAT, LON,
        List<DataElement> elements = New.list();

        for (int i = 0; i < 4; i++)
        {
            Map<String, Serializable> row = New.map();
            TimeSpan timeSpan = TimeSpan.get(1000 + 1000 * i);
            row.put(ourColumns[0], timeSpan);
            row.put(ourColumns[1], "room" + i);
            row.put(ourColumns[2], "message" + i);
            row.put(ourColumns[3], Double.valueOf(10.1 * i));
            row.put(ourColumns[4], Double.valueOf(11.1 * i));

            DefaultMapPointGeometrySupport point = new DefaultMapPointGeometrySupport(
                    LatLonAlt.createFromDegrees(10.1 * i, 11.1 * i));
            SimpleMetaDataProvider provider = new SimpleMetaDataProvider(row);
            DataElement element = new DefaultMapDataElement(i, timeSpan, dataType, provider, point);
            element.getVisualizationState().setSelected(i % 2 == 0);
            elements.add(element);
        }

        return elements;
    }

    /**
     * The answer for the getDataAt call on {@link MetaColumnsTableModel}.
     *
     * @param data The test data.
     * @return The element at the passed in index.
     */
    private DataElement getDataAtAnswer(List<DataElement> data)
    {
        int rowIndex = ((Integer)EasyMock.getCurrentArguments()[0]).intValue();

        return data.get(rowIndex);
    }

    /**
     * The answer to getMapGeometrySupport.
     *
     * @return The {@link MapGeometrySupport}.
     */
    private MapGeometrySupport getMapGeometrySupportAnswer()
    {
        int id = ((Long)EasyMock.getCurrentArguments()[0]).intValue();

        return new DefaultMapPointGeometrySupport(LatLonAlt.createFromDegrees(10.1 * id, 11.1 * id));
    }

    /**
     * The getValueAt answer call on {@link MetaColumnsTableModel} only when the
     * column is the index meta column.
     *
     * @return The row index.
     */
    private int getValueAtAnswer()
    {
        return ((Integer)EasyMock.getCurrentArguments()[0]).intValue();
    }
}
