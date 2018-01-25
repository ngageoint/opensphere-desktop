package io.opensphere.merge.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.Serializable;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.impl.DefaultDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Unit test for {@link MergeDataElementProvider}.
 */
public class MergeDataElementProviderTest
{
    /**
     * The test column.
     */
    private static final String ourTestColumn = "column1";

    /**
     * Tests creating a data element.
     */
    @Test
    public void testCreateDataElement()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        Map<String, Serializable> data = New.map();
        data.put(ourTestColumn, "testvalue");
        MergedDataRow row1 = new MergedDataRow(data, null, TimeSpan.get());

        data = New.map();
        data.put(ourTestColumn, "testvalue2");
        MergedDataRow row2 = new MergedDataRow(data, null, TimeSpan.get());

        support.replayAll();

        MergeDataElementProvider provider = new MergeDataElementProvider(dataType, New.list(row1, row2));
        DataElement element1 = provider.next();
        DataElement element2 = provider.next();

        assertTrue(element1.getId() != element2.getId());

        assertTrue(element1 instanceof DefaultDataElement);
        assertEquals(row1.getTimespan(), element1.getTimeSpan());
        assertEquals(dataType, element1.getDataTypeInfo());
        assertEquals("testvalue", element1.getMetaData().getValue(ourTestColumn));

        assertTrue(element2 instanceof DefaultDataElement);
        assertEquals(row2.getTimespan(), element2.getTimeSpan());
        assertEquals(dataType, element2.getDataTypeInfo());
        assertEquals("testvalue2", element2.getMetaData().getValue(ourTestColumn));

        support.verifyAll();
    }

    /**
     * Tests creating a data element.
     */
    @Test
    public void testCreateDataElementMap()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);

        MapGeometrySupport geom1 = support.createMock(MapGeometrySupport.class);
        EasyMock.expect(geom1.getColor()).andReturn(Color.red);
        Map<String, Serializable> data = New.map();
        data.put(ourTestColumn, "testvalue");
        MergedDataRow row1 = new MergedDataRow(data, geom1, TimeSpan.get());

        MapGeometrySupport geom2 = support.createMock(MapGeometrySupport.class);
        EasyMock.expect(geom2.getColor()).andReturn(Color.red);
        data = New.map();
        data.put(ourTestColumn, "testvalue2");
        MergedDataRow row2 = new MergedDataRow(data, geom2, TimeSpan.get());

        support.replayAll();

        MergeDataElementProvider provider = new MergeDataElementProvider(dataType, New.list(row1, row2));
        DataElement element1 = provider.next();
        DataElement element2 = provider.next();

        assertTrue(element1.getId() != element2.getId());

        assertTrue(element1 instanceof DefaultMapDataElement);
        assertEquals(geom1, ((MapDataElement)element1).getMapGeometrySupport());
        assertEquals(row1.getTimespan(), element1.getTimeSpan());
        assertEquals(dataType, element1.getDataTypeInfo());
        assertEquals("testvalue", element1.getMetaData().getValue(ourTestColumn));

        assertTrue(element2 instanceof DefaultMapDataElement);
        assertEquals(geom2, ((MapDataElement)element2).getMapGeometrySupport());
        assertEquals(row2.getTimespan(), element2.getTimeSpan());
        assertEquals(dataType, element2.getDataTypeInfo());
        assertEquals("testvalue2", element2.getMetaData().getValue(ourTestColumn));

        support.verifyAll();
    }
}
