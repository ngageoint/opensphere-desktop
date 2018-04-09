package io.opensphere.core.geometry;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/** Test that <code>Geometry</code>s comply with their design contract. */
public class GeometryTest
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeometryTest.class);

    /**
     * Test that all geometry classes are immutable.
     *
     * @throws ClassNotFoundException If a   class cannot be loaded.
     */
    @Test
    public void testImmutableGeometries() throws ClassNotFoundException
    {
        String packageName = GeometryTest.class.getPackage().getName();
        String packagePath = packageName.replaceAll("\\.", "/");
        File classesDir = new File(System.getProperty("classes.dir") + File.separator + packagePath);
        FilenameFilter filter = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith("Geometry.class") && !name.endsWith("RenderToTextureGeometry.class");
            }
        };
        final String[] classes = classesDir.list(filter);
        for (String filename : classes)
        {
            String typename = filename.substring(0, filename.length() - 6);
            Class<?> geometryClass = Class.forName(packageName + '.' + typename);
            for (Method method : geometryClass.getDeclaredMethods())
            {
                if ((method.getModifiers() & Modifier.PUBLIC) != 0 && method.getName().startsWith("set"))
                {
                    String fullName = geometryClass.getName() + '.' + method.getName();
                    LOGGER.warn("Geometry has a public mutator: " + fullName);
                    Assert.fail("Geometry has a public mutator: " + fullName);
                }
            }
        }
    }
}
