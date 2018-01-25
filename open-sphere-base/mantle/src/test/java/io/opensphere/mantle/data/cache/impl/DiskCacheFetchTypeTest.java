package io.opensphere.mantle.data.cache.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * A test class used to exercise the functionality of the
 * {@link DiskCacheFetchType} enumeration.
 */
public class DiskCacheFetchTypeTest
{
    /**
     * Test method for the {@link DiskCacheFetchType} class declaration.
     */
    @Test
    public void testClassDeclaration()
    {
        Class<DiskCacheFetchType> classDeclaration = DiskCacheFetchType.class;

        assertTrue(Modifier.isPublic(classDeclaration.getModifiers()));
        assertTrue(Modifier.isFinal(classDeclaration.getModifiers()));
        assertFalse(Modifier.isAbstract(classDeclaration.getModifiers()));
    }

    /**
     * Test method for {@link DiskCacheFetchType}.
     */
    @Test
    public void testEnum()
    {
        for (DiskCacheFetchType enumValue : DiskCacheFetchType.values())
        {
            assertNotNull(enumValue);
            assertTrue(enumValue.name().matches("[A-Z_]+"));
            assertEquals(enumValue, DiskCacheFetchType.valueOf(enumValue.name()));
        }
    }
}
