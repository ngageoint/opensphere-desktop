package io.opensphere.core.datafilter.columns;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.state.v2.ColumnMappingType;
import io.opensphere.state.v2.ColumnMappings;
import io.opensphere.state.v2.ColumnType;

/** Unit tests for {@link ColumnMappingUtils}. */
public class ColumnMappingUtilsTest
{
    /**
     * Tests {@link ColumnMappingUtils#copy(ColumnMappings)}.
     */
    @Test
    public void testCopy()
    {
        ColumnMappings orig = ColumnMappingUtilsTest.newColumnMappings();
        ColumnMappings copy = ColumnMappingUtils.copy(orig);
        assertEquals(orig, copy);
    }

    /**
     * Creates a test object.
     *
     * @return the test object
     */
    public static ColumnMappings newColumnMappings()
    {
        ColumnMappings columnMappings = new ColumnMappings();
        ColumnMappingType columnMapping = new ColumnMappingType();
        columnMapping.setName("Altitude");
        columnMapping.setType("decimal");
        columnMapping.setDescription("Altitude yo!");
        ColumnType columnType = new ColumnType();
        columnType.setLayer("server!!layer1");
        columnType.setValue("alt");
        columnMapping.getColumn().add(columnType);
        columnMappings.getColumnMapping().add(columnMapping);
        return columnMappings;
    }

    /**
     * Asserts that two ColumnMappings objects are equal.
     *
     * @param input the first object
     * @param result the second object
     */
    public static void assertEquals(ColumnMappings input, ColumnMappings result)
    {
        Assert.assertEquals(input.getColumnMapping().size(), result.getColumnMapping().size());
        for (int i = 0; i < result.getColumnMapping().size(); i++)
        {
            ColumnMappingType inputMapping = input.getColumnMapping().get(i);
            ColumnMappingType resultMapping = result.getColumnMapping().get(i);
            Assert.assertEquals(inputMapping.getName(), resultMapping.getName());
            Assert.assertEquals(inputMapping.getType(), resultMapping.getType());
            Assert.assertEquals(inputMapping.getDescription(), resultMapping.getDescription());
            Assert.assertEquals(inputMapping.getColumn().size(), resultMapping.getColumn().size());
            for (int j = 0; j < resultMapping.getColumn().size(); j++)
            {
                ColumnType inputColumn = inputMapping.getColumn().get(j);
                ColumnType resultColumn = resultMapping.getColumn().get(j);
                Assert.assertEquals(inputColumn.getLayer(), resultColumn.getLayer());
                Assert.assertEquals(inputColumn.getValue(), resultColumn.getValue());
            }
        }
    }
}
