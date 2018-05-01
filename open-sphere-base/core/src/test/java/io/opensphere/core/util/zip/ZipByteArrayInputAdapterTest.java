package io.opensphere.core.util.zip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.util.Utilities;

/**
 * Test class used to exercise the functionality of the
 * {@link ZipByteArrayInputAdapter} class.
 */
public class ZipByteArrayInputAdapterTest
{
    /**
     * The object on which tests are performed.
     */
    private ZipByteArrayInputAdapter myTestObject;

    /**
     * The Byte array used in test operations.
     */
    private byte[] myByteArray;

    /**
     * The Location array used in test operations.
     */
    private String myLocation;

    /**
     * The Name array used in test operations.
     */
    private String myName;

    /**
     * The method used in test operations.
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
        myByteArray = RandomStringUtils.randomAlphabetic(500).getBytes();
        myLocation = RandomStringUtils.randomAlphabetic(85);
        myName = RandomStringUtils.randomAlphabetic(25);
        myMethod = RandomUtils.nextInt();
        myTestObject = new ZipByteArrayInputAdapter(myName, myLocation, myByteArray, myMethod);
    }

    /**
     * Test method for {@link ZipByteArrayInputAdapter#closeInputStream()}.
     *
     * @throws IOException if the test fails.
     */
    @Test
    public void testCloseInputStream() throws IOException
    {
        // get the input stream first, to make sure it's not null:
        try (InputStream in = myTestObject.getInputStream())
        {
            myTestObject.closeInputStream();
        }
        // call it twice to make sure no exceptions are thrown:
        myTestObject.closeInputStream();
    }

    /**
     * Test method for {@link ZipByteArrayInputAdapter#getInputStream()}.
     *
     * @throws IOException if the test fails.
     */
    @Test
    public void testGetInputStream() throws IOException
    {
        byte[] bytes = new byte[myByteArray.length];
        int bytesRead = 0;
        try (InputStream in = myTestObject.getInputStream(); InputStream in2 = myTestObject.getInputStream())
        {
            bytesRead = in.read(bytes);
            assertTrue(Utilities.sameInstance(in, in2));
        }
        assertEquals(bytesRead, myByteArray.length);
        String expectedContent = new String(myByteArray);
        String actualContent = new String(bytes);

        assertEquals(expectedContent, actualContent);
    }

    /**
     * Test method for {@link ZipByteArrayInputAdapter#getLocation()}.
     */
    @Test
    public void testGetLocation()
    {
        assertEquals(myLocation, myTestObject.getLocation());
    }

    /**
     * Test method for {@link ZipByteArrayInputAdapter#getName()}.
     */
    @Test
    public void testGetName()
    {
        assertEquals(myName, myTestObject.getName());
    }

    /**
     * Test method for
     * {@link ZipByteArrayInputAdapter#ZipByteArrayInputAdapter(String, String, byte[], int)}
     * .
     */
    @Test
    public void testZipByteArrayInputAdapter()
    {
        assertNotNull(myTestObject);
        assertEquals(myName, myTestObject.getName());
        assertEquals(myMethod, myTestObject.getMethod());
        assertEquals(myLocation, myTestObject.getLocation());
    }
}
