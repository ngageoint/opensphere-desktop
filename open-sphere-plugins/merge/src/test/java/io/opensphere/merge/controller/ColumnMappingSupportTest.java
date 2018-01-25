package io.opensphere.merge.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.datafilter.columns.ColumnMappingController;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.merge.algorithm.Col;

/**
 * Unit test for {@link ColumnMappingSupport}.
 */
public class ColumnMappingSupportTest
{
    /**
     * The layer 1 columns.
     */
    private static final List<String> ourType1Columns = New.list("USER_NAME", "PLACE_COUNTRY", "TEXT");

    /**
     * The layer 2 columns.
     */
    private static final List<String> ourType2Columns = New.list("TRUNCATED", "EMBERSGEOCODE_COUNTRY", "TEXT");

    /**
     * The test layer key.
     */
    private static final String ourTypeKey1 = "twitter";

    /**
     * Another test layer key.
     */
    private static final String ourTypeKey2 = "embers";

    /**
     * Tests the column match function.
     */
    @Test
    public void testColumnMatch()
    {
        EasyMockSupport support = new EasyMockSupport();

        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);
        ColumnMappingController mapper = createMapper(support);
        List<Col> layer1Columns = createColumns(support, ourTypeKey1, ourType1Columns);
        List<Col> layer2Columns = createColumns(support, ourTypeKey2, ourType2Columns);

        support.replayAll();

        ColumnMappingSupport mappingSupport = new ColumnMappingSupport(lookup, mapper);

        int i = 0;
        for (Col layer1Column : layer1Columns)
        {
            int j = 0;
            for (Col layer2Column : layer2Columns)
            {
                String joinedName = mappingSupport.columnMatch(layer1Column, layer2Column);
                if (i == 1 && j == 1 || i == 2 && j == 2)
                {
                    assertNotNull(joinedName);
                }
                else
                {
                    assertNull(joinedName);
                }
                j++;
            }
            i++;
        }

        support.verifyAll();
    }

    /**
     * Tests get records.
     */
    @Test
    public void testGetRecords()
    {
        EasyMockSupport support = new EasyMockSupport();

        ColumnMappingController mapper = support.createMock(ColumnMappingController.class);
        DataTypeInfo type = support.createMock(DataTypeInfo.class);
        List<DataElement> elements = createElements(support);
        DataElementLookupUtils lookup = createLookup(support, type, elements);

        support.replayAll();

        ColumnMappingSupport mappingSupport = new ColumnMappingSupport(lookup, mapper);
        assertEquals(elements, mappingSupport.getRecords(type));

        support.verifyAll();
    }

    /**
     * Creates the columns for test.
     *
     * @param support Used to create a mock.
     * @param typeKey The layer.
     * @param columnNames The column names.
     * @return The columns.
     */
    private List<Col> createColumns(EasyMockSupport support, String typeKey, List<String> columnNames)
    {
        DataTypeInfo dataType = support.createMock(DataTypeInfo.class);
        EasyMock.expect(dataType.getTypeKey()).andReturn(typeKey).atLeastOnce();

        List<Col> columns = New.list();

        for (String columnName : columnNames)
        {
            Col column = new Col();
            column.name = columnName;
            column.owner = dataType;

            columns.add(column);
        }

        return columns;
    }

    /**
     * Creates mocked data elements.
     *
     * @param support Used to create the mock.
     * @return The data elements.
     */
    private List<DataElement> createElements(EasyMockSupport support)
    {
        DataElement element = support.createMock(DataElement.class);

        return New.list(element);
    }

    /**
     * Creates a mocked {@link DataElementLookupUtils}.
     *
     * @param support Used to create the mock.
     * @param type The expected type.
     * @param elements The elements to return.
     * @return The mocked class.
     */
    private DataElementLookupUtils createLookup(EasyMockSupport support, DataTypeInfo type, List<DataElement> elements)
    {
        DataElementLookupUtils lookup = support.createMock(DataElementLookupUtils.class);

        EasyMock.expect(lookup.getDataElements(EasyMock.eq(type))).andReturn(elements);

        return lookup;
    }

    /**
     * Creates an easy mocked mapper.
     *
     * @param support Used to create the mock.
     * @return The mocked mapper.
     */
    private ColumnMappingController createMapper(EasyMockSupport support)
    {
        ColumnMappingController mapper = support.createMock(ColumnMappingController.class);

        EasyMock.expect(mapper.getDefinedColumn(EasyMock.cmpEq(ourTypeKey1), EasyMock.isA(String.class)))
                .andAnswer(this::getDefinedColumnAnswer).atLeastOnce();
        EasyMock.expect(mapper.getDefinedColumn(EasyMock.cmpEq(ourTypeKey2), EasyMock.isA(String.class)))
                .andAnswer(this::getDefinedColumnAnswer).atLeastOnce();

        return mapper;
    }

    /**
     * The answer to the getDefinedColumn mocked call.
     *
     * @return The mapped column name, or null if not mapped.
     */
    private String getDefinedColumnAnswer()
    {
        String definedColumn = null;

        String typeKey = EasyMock.getCurrentArguments()[0].toString();
        String column = EasyMock.getCurrentArguments()[1].toString();
        if (ourTypeKey1.equals(typeKey))
        {
            assertTrue(ourType1Columns.contains(column));
        }
        else
        {
            assertTrue(ourType2Columns.contains(column));
        }

        if (column.endsWith("_COUNTRY"))
        {
            definedColumn = "COUNTRY";
        }

        return definedColumn;
    }
}
