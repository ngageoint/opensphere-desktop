package io.opensphere.core.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * A test designed to exercise the functionality of {@link KeyBindingChangeType}
 * enum.
 */
public class KeyBindingChangeTypeTest
{
    /**
     * Test method for the {@link KeyBindingChangeType} class declaration.
     */
    @Test
    public void testClassDeclaration()
    {
        Class<KeyBindingChangeType> classDeclaration = KeyBindingChangeType.class;

        assertTrue(Modifier.isPublic(classDeclaration.getModifiers()));
        assertTrue(Modifier.isFinal(classDeclaration.getModifiers()));
        assertFalse(Modifier.isAbstract(classDeclaration.getModifiers()));
    }

    /**
     * Test method for {@link KeyBindingChangeType}.
     */
    @Test
    public void testEnum()
    {
        for (KeyBindingChangeType enumValue : KeyBindingChangeType.values())
        {
            assertNotNull(enumValue);
            assertTrue(enumValue.name().matches("[A-Z_]+"));
            assertEquals(enumValue, KeyBindingChangeType.valueOf(enumValue.name()));
        }
    }
}
