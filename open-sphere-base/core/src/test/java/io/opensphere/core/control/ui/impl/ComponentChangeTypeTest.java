package io.opensphere.core.control.ui.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * A test used to exercise the functionality of the {@link ComponentChangeType}
 * enum.
 */
public class ComponentChangeTypeTest
{
    /**
     * Test method for the {@link ComponentChangeType} class declaration.
     */
    @Test
    public void testClassDeclaration()
    {
        Class<ComponentChangeType> classDeclaration = ComponentChangeType.class;

        assertTrue(Modifier.isPublic(classDeclaration.getModifiers()));
        assertTrue(Modifier.isFinal(classDeclaration.getModifiers()));
        assertFalse(Modifier.isAbstract(classDeclaration.getModifiers()));
    }

    /**
     * Test method for {@link ComponentChangeType}.
     */
    @Test
    public void testEnum()
    {
        for (ComponentChangeType enumValue : ComponentChangeType.values())
        {
            assertNotNull(enumValue);
            assertTrue(enumValue.name().matches("[A-Z_]+"));
            assertEquals(enumValue, ComponentChangeType.valueOf(enumValue.name()));
        }
    }
}
