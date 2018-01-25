package io.opensphere.core.util.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * A test class used to exercise the functionality of {@link DateTimeChangeType}
 * .
 */
public class DateTimeChangeTypeTest
{
    /**
     * Test method for the {@link DateTimeChangeType} class declaration.
     */
    @Test
    public void testClassDeclaration()
    {
        Class<DateTimeChangeType> classDeclaration = DateTimeChangeType.class;

        assertTrue(Modifier.isPublic(classDeclaration.getModifiers()));
        assertTrue(Modifier.isFinal(classDeclaration.getModifiers()));
        assertFalse(Modifier.isAbstract(classDeclaration.getModifiers()));
    }

    /**
     * Test method for {@link DateTimeChangeType}.
     */
    @Test
    public void testEnum()
    {
        for (DateTimeChangeType enumValue : DateTimeChangeType.values())
        {
            assertNotNull(enumValue);
            assertTrue(enumValue.name().matches("[A-Z_]+"));
            assertEquals(enumValue, DateTimeChangeType.valueOf(enumValue.name()));
            assertTrue(StringUtils.isNotBlank(enumValue.toString()));
        }
    }
}
