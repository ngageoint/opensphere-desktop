package io.opensphere.importer.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.importer.config.ColumnType.Category;

/**
 * Class designed to exercise the functionality of the
 * {@link ImportParseParameters} class.
 */
public class ImportParseParametersTest
{
    /**
     * The date format used in the test.
     */
    private static final String DATE_FORMAT = "MM-DD-YYYY";

    /**
     * The object on which tests are performed.
     */
    private ImportParseParameters myTestObject;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws java.lang.Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        myTestObject = new ImportParseParameters();
    }

    /**
     * Test method to verify that there are no private methods in the
     * {@link ImportParseParameters} class.
     */
    @Test
    public void testNonPrivateMethods()
    {
        Method[] declaredMethods = ImportParseParameters.class.getDeclaredMethods();

        for (Method method : declaredMethods)
        {
            if (!method.getName().startsWith("$") && !method.getName().startsWith("lambda$"))
            {
                assertFalse(method.getName() + " is private. No private methods are permitted.",
                        Modifier.isPrivate(method.getModifiers()));
            }
        }
    }

    /**
     * Test method for {@link ImportParseParameters#hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        SpecialColumn specialColumn = new SpecialColumn(0, ColumnType.DATE, DATE_FORMAT);
        myTestObject.getSpecialColumns().add(specialColumn);

        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myTestObject.getColumnNames());
        result = prime * result + HashCodeHelper.getHashCode(myTestObject.getSpecialColumns());
        result = prime * result + HashCodeHelper.getHashCode(myTestObject.getColumnsToIgnore());

        assertEquals(result, myTestObject.hashCode());
    }

    /**
     * Test method for {@link ImportParseParameters#ImportParseParameters()}.
     */
    @Test
    public void testImportParseParameters()
    {
        assertNotNull(myTestObject);
    }

    /**
     * Test method for {@link ImportParseParameters#getColumnNames()} and
     * {@link ImportParseParameters#setColumnNames(List)}.
     */
    @Test
    public void testColumnNames()
    {
        assertNotNull(myTestObject.getColumnNames());
        assertTrue(myTestObject.getColumnNames().isEmpty());

        List<String> columnNames = Arrays.asList("ONE", "TWO", "Three");
        myTestObject.setColumnNames(columnNames);

        assertEquals(columnNames, myTestObject.getColumnNames());
    }

    /**
     * Test method for {@link ImportParseParameters#getSpecialColumns()}.
     */
    @Test
    public void testGetSpecialColumns()
    {
        assertNotNull(myTestObject.getSpecialColumns());
        assertTrue(myTestObject.getSpecialColumns().isEmpty());
    }

    /**
     * Test method for {@link ImportParseParameters#getColumnsToIgnore()} and
     * {@link ImportParseParameters#setColumnsToIgnore(java.util.List)}.
     */
    @Test
    public void testColumnsToIgnore()
    {
        assertNotNull(myTestObject.getColumnsToIgnore());
        assertTrue(myTestObject.getColumnsToIgnore().isEmpty());

        List<Integer> columns = Arrays.asList(1, 2, 3);
        myTestObject.setColumnsToIgnore(columns);

        assertEquals(columns, myTestObject.getColumnsToIgnore());
    }

    /**
     * Test method for
     * {@link ImportParseParameters#hasCategory(ColumnType.Category)} .
     */
    @Test
    public void testHasCategory()
    {
        SpecialColumn specialColumn = new SpecialColumn(0, ColumnType.DATE, DATE_FORMAT);
        myTestObject.getSpecialColumns().add(specialColumn);

        assertTrue(myTestObject.hasCategory(Category.TEMPORAL));
        assertFalse(myTestObject.hasCategory(Category.SPATIAL));
    }

    /**
     * Test method for {@link ImportParseParameters#hasType(ColumnType)}.
     */
    @Test
    public void testHasTypeColumnType()
    {
        SpecialColumn specialColumn = new SpecialColumn(0, ColumnType.DATE, DATE_FORMAT);
        myTestObject.getSpecialColumns().add(specialColumn);

        assertTrue(myTestObject.hasType(ColumnType.DATE));
        assertFalse(myTestObject.hasType(ColumnType.LOB));
    }

    /**
     * Test method for {@link ImportParseParameters#hasType(ColumnType[])}.
     */
    @Test
    public void testHasTypeColumnTypeArray()
    {
        SpecialColumn specialColumn = new SpecialColumn(0, ColumnType.DATE, DATE_FORMAT);
        myTestObject.getSpecialColumns().add(specialColumn);

        assertTrue(myTestObject.hasType(ColumnType.values()));
        assertFalse(myTestObject.hasType(ColumnType.LAT, ColumnType.LON));

        myTestObject.getColumnsToIgnore().add(0);
        assertFalse(myTestObject.hasType(ColumnType.values()));
    }

    /**
     * Test method for
     * {@link ImportParseParameters#isColumnIncluded(SpecialColumn, ColumnType[])}
     * .
     */
    @Test
    public void testIsColumnIncluded()
    {
        SpecialColumn specialColumn = new SpecialColumn(0, ColumnType.DATE, DATE_FORMAT);

        assertTrue(myTestObject.isColumnIncluded(specialColumn, ColumnType.values()));
        assertFalse(myTestObject.isColumnIncluded(specialColumn));
        assertFalse(myTestObject.isColumnIncluded(specialColumn, ColumnType.LAT, ColumnType.LON));

        myTestObject.getColumnsToIgnore().add(0);
        assertFalse(myTestObject.isColumnIncluded(specialColumn, ColumnType.values()));
    }

    /**
     * Test method for
     * {@link ImportParseParameters#getSpecialColumn(ColumnType)}.
     */
    @Test
    public void testGetSpecialColumn()
    {
        SpecialColumn specialColumn = new SpecialColumn(0, ColumnType.DATE, DATE_FORMAT);
        myTestObject.getSpecialColumns().add(specialColumn);

        assertEquals(specialColumn, myTestObject.getSpecialColumn(ColumnType.DATE));
        assertNull(myTestObject.getSpecialColumn(ColumnType.LAT));
    }

    /**
     * Test method for {@link ImportParseParameters#equals(java.lang.Object)}.
     */
    @Test
    @SuppressWarnings({ "PMD.EqualsNull", "PMD.UseAssertEqualsInsteadOfAssertTrue" })
    public void testEqualsObject()
    {
        assertTrue(myTestObject.equals(myTestObject));
        assertFalse(myTestObject.equals(null));
        assertFalse(myTestObject.equals(new Object()));

        ImportParseParameters other = new ImportParseParameters();
        assertTrue(myTestObject.equals(other));

        other.setColumnNames(Arrays.asList("FOO", "BAR"));
        assertFalse(myTestObject.equals(other));

        myTestObject.setColumnNames(Arrays.asList("FOO", "BAR"));
        SpecialColumn specialColumn = new SpecialColumn(0, ColumnType.DATE, DATE_FORMAT);
        other.getSpecialColumns().add(specialColumn);
        assertFalse(myTestObject.equals(other));
        myTestObject.getSpecialColumns().add(specialColumn);

        myTestObject.getColumnsToIgnore().add(0);
        assertFalse(myTestObject.equals(other));
    }

    /**
     * Test method for {@link ImportParseParameters#toString()}.
     */
    @Test
    public void testToString()
    {
        assertEquals("ImportParseParameters [columnNames=[], specialColumns=[], columnsToIgnore=[]]", myTestObject.toString());
    }

    /**
     * Test method for {@link ImportParseParameters#clone()}.
     */
    @Test
    public void testClone()
    {
        assertEquals(myTestObject, myTestObject.clone());
    }
}
