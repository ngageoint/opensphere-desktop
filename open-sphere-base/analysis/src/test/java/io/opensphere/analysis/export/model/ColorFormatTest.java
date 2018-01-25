package io.opensphere.analysis.export.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * A test class used to exercise the functionality of the {@link ColorFormat}
 * enum.
 */
public class ColorFormatTest
{
    /**
     * Test method for the {@link ColorFormat} class declaration.
     */
    @Test
    public void testClassDeclaration()
    {
        Class<ColorFormat> classDeclaration = ColorFormat.class;

        assertTrue(Modifier.isPublic(classDeclaration.getModifiers()));
        assertTrue(Modifier.isFinal(classDeclaration.getModifiers()));
        assertFalse(Modifier.isAbstract(classDeclaration.getModifiers()));
    }

    /**
     * Test method for {@link ColorFormat}.
     */
    @Test
    public void testEnum()
    {
        for (ColorFormat enumValue : ColorFormat.values())
        {
            assertNotNull(enumValue);
            assertTrue(enumValue.name().matches("[A-Z_]+"));
            assertEquals(enumValue, ColorFormat.valueOf(enumValue.name()));
        }
    }
}
