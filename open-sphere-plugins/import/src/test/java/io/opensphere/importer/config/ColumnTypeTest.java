package io.opensphere.importer.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat.Type;

/**
 * Class designed to exercise the functionality of the {@link ColumnType} class.
 */
public class ColumnTypeTest
{
    /**
     * Test method for the {@link ColumnType} class declaration.
     */
    @Test
    public void testClassDeclaration()
    {
        Class<ColumnType> classDeclaration = ColumnType.class;

        assertTrue(Modifier.isPublic(classDeclaration.getModifiers()));
        assertTrue(Modifier.isFinal(classDeclaration.getModifiers()));
        assertFalse(Modifier.isAbstract(classDeclaration.getModifiers()));
    }

    /**
     * Test method for {@link ColumnType}.
     */
    @Test
    public void testEnum()
    {
        for (ColumnType enumValue : ColumnType.values())
        {
            assertNotNull(enumValue);
            assertTrue(enumValue.name().matches("[A-Z_]+"));
            assertEquals(enumValue, ColumnType.valueOf(enumValue.name()));
        }
    }

    /**
     * Test method for {@link ColumnType#fromString(String)}.
     */
    @Test
    public void testFromString()
    {
        assertEquals(ColumnType.DATE, ColumnType.fromString(ColumnType.DATE.toString()));
    }

    /**
     * Test method for
     * {@link ColumnType#fromDateFormatType(io.opensphere.core.common.configuration.date.DateFormat.Type, boolean)}
     * .
     */
    @Test
    public void testFromDateFormatType()
    {
        assertEquals(ColumnType.DATE, ColumnType.fromDateFormatType(Type.DATE, false));
        assertEquals(ColumnType.DOWN_DATE, ColumnType.fromDateFormatType(Type.DATE, true));
        assertEquals(ColumnType.TIME, ColumnType.fromDateFormatType(Type.TIME, false));
        assertEquals(ColumnType.DOWN_TIME, ColumnType.fromDateFormatType(Type.TIME, true));
        assertEquals(ColumnType.TIMESTAMP, ColumnType.fromDateFormatType(Type.TIMESTAMP, false));
        assertEquals(ColumnType.DOWN_TIMESTAMP, ColumnType.fromDateFormatType(Type.TIMESTAMP, true));
    }
}
