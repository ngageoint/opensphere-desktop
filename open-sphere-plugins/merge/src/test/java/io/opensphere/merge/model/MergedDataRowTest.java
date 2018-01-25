package io.opensphere.merge.model;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;

/**
 * Unit test for {@link MergedDataRow}.
 */
public class MergedDataRowTest
{
    /**
     * test.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        Map<String, Serializable> data = New.map();
        MapGeometrySupport geometry = support.createMock(MapGeometrySupport.class);
        TimeSpan span = TimeSpan.get();

        support.replayAll();

        MergedDataRow row = new MergedDataRow(data, geometry, span);
        assertEquals(data, row.getData());
        assertEquals(geometry, row.getGeometry());
        assertEquals(span, row.getTimespan());

        support.verifyAll();
    }

    /**
     * Tests serializing the data.
     *
     * @throws IOException if the test fails.
     * @throws ClassNotFoundException if the test fails.
     */
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        Map<String, Serializable> data = New.map();
        data.put("column1", "value1");
        MapGeometrySupport geometry = new SimpleMapPointGeometrySupport(LatLonAlt.createFromDegrees(10, 11));
        TimeSpan span = TimeSpan.get();

        MergedDataRow row = new MergedDataRow(data, geometry, span);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        objectOut.writeObject(row);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(in);
        MergedDataRow serialized = (MergedDataRow)objectIn.readObject();

        assertEquals(data, serialized.getData());
        assertEquals(geometry, serialized.getGeometry());
        assertEquals(span, serialized.getTimespan());
    }
}
