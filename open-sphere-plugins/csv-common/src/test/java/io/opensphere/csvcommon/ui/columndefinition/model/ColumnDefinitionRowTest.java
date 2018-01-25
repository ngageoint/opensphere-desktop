package io.opensphere.csvcommon.ui.columndefinition.model;

import static org.junit.Assert.assertEquals;

import java.util.Observer;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;

/**
 * Tests the ColumnDefinitionRow class.
 *
 */
public class ColumnDefinitionRowTest
{
    /**
     * Tests setting the data type.
     */
    @Test
    public void testSetDataType()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionRow.DATA_TYPE_PROPERTY);

        support.replayAll();

        ColumnDefinitionRow row = new ColumnDefinitionRow();
        row.addObserver(observer);

        String dataType = "Date";

        row.setDataType(dataType);

        assertEquals(dataType, row.getDataType());

        support.verifyAll();
    }

    /**
     * Tests setting the format.
     */
    @Test
    public void testSetFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        Observer observer = ModelTestUtil.createObserver(support, ColumnDefinitionRow.FORMAT_PROPERTY);

        support.replayAll();

        ColumnDefinitionRow row = new ColumnDefinitionRow();
        row.addObserver(observer);

        String format = "yyyymmdd";

        row.setFormat(format);

        assertEquals(format, row.getFormat());

        support.verifyAll();
    }
}
