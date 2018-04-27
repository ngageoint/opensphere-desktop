package io.opensphere.core.util.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class used to exercise the functionality of the {@link ZipInputAdapter}
 * class.
 */
public class ZipInputAdapterTest
{
    /**
     * The object on which tests are performed.
     */
    private ZipInputAdapter myTestObject;

    /**
     * Test data used during instantiation.
     */
    private int myMethod;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws java.lang.Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        myMethod = RandomUtils.nextInt();
        myTestObject = new ZipInputAdapter(myMethod)
        {
            @Override
            public String getName()
            {
                return null;
            }

            @Override
            public String getLocation()
            {
                return null;
            }

            @Override
            public InputStream getInputStream() throws IOException
            {
                return null;
            }
        };
    }

    /**
     * Test method to verify that there are no private methods in the
     * {@link ZipInputAdapter} class.
     */
    @Test
    public void testNonPrivateMethods()
    {
        Method[] declaredMethods = ZipInputAdapter.class.getDeclaredMethods();

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
     * Test method for
     * {@link io.opensphere.core.util.zip.ZipInputAdapter#closeInputStream()}.
     *
     * @throws IOException if the test fails.
     */
    @Test
    public void testCloseInputStream() throws IOException
    {
        myTestObject.closeInputStream();
    }

    /**
     * Test method for
     * {@link io.opensphere.core.util.zip.ZipInputAdapter#getMethod()}.
     */
    @Test
    public void testGetMethod()
    {
        assertEquals(myMethod, myTestObject.getMethod());
    }

    /**
     * Test method for
     * {@link io.opensphere.core.util.zip.ZipInputAdapter#getSize()}.
     */
    @Test
    public void testGetSize()
    {
        assertEquals(0, myTestObject.getSize());
    }
}
