package io.opensphere.geopackage.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * Test class to exercise the functionality of the {@link LayerType} enum.
 */
public class LayerTypeTest
{
    /**
     * Test method for the {@link LayerType} class declaration.
     */
    @Test
    public void testClassDeclaration()
    {
        Class<LayerType> classDeclaration = LayerType.class;

        assertTrue(Modifier.isPublic(classDeclaration.getModifiers()));
        assertTrue(Modifier.isFinal(classDeclaration.getModifiers()));
        assertFalse(Modifier.isAbstract(classDeclaration.getModifiers()));
    }

    /**
     * Test method for {@link LayerType}.
     */
    @Test
    public void testEnum()
    {
        for (LayerType enumValue : LayerType.values())
        {
            assertNotNull(enumValue);
            assertTrue(enumValue.name().matches("[A-Z_]+"));
            assertEquals(enumValue, LayerType.valueOf(enumValue.name()));
        }
    }
}
