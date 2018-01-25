package io.opensphere.mantle.data.element.factory;

import static org.junit.Assert.assertEquals;

import org.apache.avro.generic.GenericRecord;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.EndTimeKey;

/**
 * Unit test for {@link AvroTimeHelper}.
 */
public class AvroTimeHelperTest
{
    /**
     * Tests to make sure start and end times get set for a specific column
     * names.
     */
    @Test
    public void test()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataTypeInfo type = createDataType(support);

        long startTime = System.currentTimeMillis() - 1000;
        long endTime = System.currentTimeMillis();
        GenericRecord record = createRecord(support, startTime, endTime);

        support.replayAll();

        AvroTimeHelper helper = new AvroTimeHelper(type);

        TimeSpan span = helper.span(record);

        assertEquals(startTime, span.getStart());
        assertEquals(endTime, span.getEnd());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked data type.
     *
     * @param support Used to create the mock.
     * @return The mocked type.
     */
    private DataTypeInfo createDataType(EasyMockSupport support)
    {
        MetaDataInfo metadataInfo = support.createMock(MetaDataInfo.class);
        EasyMock.expect(metadataInfo.getKeyNames()).andReturn(New.list("UP_DATE_TIME", "DOWN_DATE_TIME"));
        EasyMock.expect(metadataInfo.getTimeKey()).andReturn("UP_DATE_TIME");
        EasyMock.expect(metadataInfo.getKeyForSpecialType(EasyMock.eq(EndTimeKey.DEFAULT))).andReturn("DOWN_DATE_TIME");

        DataTypeInfo info = support.createMock(DataTypeInfo.class);
        EasyMock.expect(info.getMetaDataInfo()).andReturn(metadataInfo).anyTimes();

        return info;
    }

    /**
     * Creates an easy mocked {@link GenericRecord}.
     *
     * @param support Used to create the mock.
     * @param startTime The start time.
     * @param endTime The end time.
     * @return The mocked {@link GenericRecord}.
     */
    private GenericRecord createRecord(EasyMockSupport support, long startTime, long endTime)
    {
        GenericRecord record = support.createMock(GenericRecord.class);

        EasyMock.expect(record.get("UP_DATE_TIME")).andReturn(new DateTime(startTime));
        EasyMock.expect(record.get("DOWN_DATE_TIME")).andReturn(new DateTime(endTime));

        return record;
    }
}
