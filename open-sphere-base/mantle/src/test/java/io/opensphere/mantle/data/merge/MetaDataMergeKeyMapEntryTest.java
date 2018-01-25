package io.opensphere.mantle.data.merge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test code used to exercise the functionality of the
 * {@link MetaDataMergeKeyMapEntry} class.
 */
public class MetaDataMergeKeyMapEntryTest
{
    /**
     * The object on which tests are performed.
     */
    private MetaDataMergeKeyMapEntry myTestObject;

    /**
     * Creates the resources needed to execute the tests.
     *
     * @throws Exception if the resources cannot be initialized.
     */
    @Before
    public void setUp() throws Exception
    {
        myTestObject = new MetaDataMergeKeyMapEntry();
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#hashCode()}
     * .
     */
    @Test
    public void testHashCode()
    {
        String testMergeKey = RandomStringUtils.randomAlphabetic(25);
        String testSourceKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject = new MetaDataMergeKeyMapEntry(testMergeKey, testSourceKey);

        int expectedResult = 1;
        expectedResult = 31 * expectedResult + testSourceKey.hashCode();
        expectedResult = 31 * expectedResult + testMergeKey.hashCode();

        assertEquals(expectedResult, myTestObject.hashCode());

        myTestObject = new MetaDataMergeKeyMapEntry();
        assertEquals(961, myTestObject.hashCode());
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#MetaDataMergeKeyMapEntry()}
     * .
     */
    @Test
    public void testMetaDataMergeKeyMapEntry()
    {
        assertNotNull(myTestObject);
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#MetaDataMergeKeyMapEntry(MetaDataMergeKeyMapEntry)}
     * .
     */
    @Test
    public void testMetaDataMergeKeyMapEntryMetaDataMergeKeyMapEntry()
    {
        String testMergeKey = RandomStringUtils.randomAlphabetic(25);
        String testSourceKey = RandomStringUtils.randomAlphabetic(25);
        MetaDataMergeKeyMapEntry testObject2 = new MetaDataMergeKeyMapEntry(testMergeKey, testSourceKey);

        myTestObject = new MetaDataMergeKeyMapEntry(testObject2);

        assertEquals(testMergeKey, myTestObject.getMergeKeyName());
        assertEquals(testSourceKey, myTestObject.getSourceKeyName());
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#MetaDataMergeKeyMapEntry(String, String)}
     * .
     */
    @Test
    public void testMetaDataMergeKeyMapEntryStringString()
    {
        String testMergeKey = RandomStringUtils.randomAlphabetic(25);
        String testSourceKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject = new MetaDataMergeKeyMapEntry(testMergeKey, testSourceKey);

        assertEquals(testMergeKey, myTestObject.getMergeKeyName());
        assertEquals(testSourceKey, myTestObject.getSourceKeyName());
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#encode(ObjectOutputStream)}
     * .
     *
     * @throws IOException if the test resources cannot be configured.
     */
    @Test
    public void testEncodeDecode() throws IOException
    {
        String testSourceKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject.setSourceKeyName(testSourceKey);
        String testMergeKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject.setMergeKeyName(testMergeKey);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        int bytesWritten = myTestObject.encode(objectOut);

        assertEquals(56, bytesWritten);

        objectOut.flush();
        byte[] data = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream objectIn = new ObjectInputStream(in);

        // use a new object for the decode to ensure that no data is
        // accedentially preserved between operations.
        myTestObject = new MetaDataMergeKeyMapEntry();
        myTestObject.decode(objectIn);

        assertEquals(testSourceKey, myTestObject.getSourceKeyName());
        assertEquals(testMergeKey, myTestObject.getMergeKeyName());
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#encode(ObjectOutputStream)}
     * .
     *
     * @throws IOException if the test resources cannot be configured.
     */
    @Test
    public void testEncodeDecodeWithNull() throws IOException
    {
        String testSourceKey = null;
        myTestObject.setSourceKeyName(testSourceKey);
        String testMergeKey = null;
        myTestObject.setMergeKeyName(testMergeKey);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);

        int bytesWritten = myTestObject.encode(objectOut);

        assertEquals(2, bytesWritten);

        objectOut.flush();
        byte[] data = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream objectIn = new ObjectInputStream(in);

        // use a new object for the decode to ensure that no data is
        // accidentally preserved between operations.
        myTestObject = new MetaDataMergeKeyMapEntry();
        myTestObject.decode(objectIn);

        assertEquals(testSourceKey, myTestObject.getSourceKeyName());
        assertEquals(testMergeKey, myTestObject.getMergeKeyName());
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#equals(Object)}
     * .
     */
    @Test
    public void testEqualsObjectSame()
    {
        assertEquals(myTestObject, myTestObject);
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#equals(Object)}
     * .
     */
    @Test
    public void testEqualsObjectEqual()
    {
        String testSourceKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject.setSourceKeyName(testSourceKey);
        String testMergeKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject.setMergeKeyName(testMergeKey);

        MetaDataMergeKeyMapEntry testObject2 = new MetaDataMergeKeyMapEntry(testMergeKey, testSourceKey);

        assertEquals(myTestObject, testObject2);
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#equals(Object)}
     * .
     */
    @Test
    public void testEqualsObjectUnequal()
    {
        myTestObject.setSourceKeyName(RandomStringUtils.randomAlphabetic(25));
        myTestObject.setMergeKeyName(RandomStringUtils.randomAlphabetic(25));

        MetaDataMergeKeyMapEntry testObject2 = new MetaDataMergeKeyMapEntry(RandomStringUtils.randomAlphabetic(25),
                RandomStringUtils.randomAlphabetic(25));

        assertFalse(myTestObject.equals(testObject2));
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#equals(Object)}
     * .
     */
    @Test
    public void testEqualsObjectUnequalMergeKeys()
    {
        myTestObject.setSourceKeyName(RandomStringUtils.randomAlphabetic(25));
        myTestObject.setMergeKeyName(RandomStringUtils.randomAlphabetic(25));

        MetaDataMergeKeyMapEntry testObject2 = new MetaDataMergeKeyMapEntry(RandomStringUtils.randomAlphabetic(25),
                myTestObject.getSourceKeyName());

        assertFalse(myTestObject.equals(testObject2));
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#equals(Object)}
     * .
     */
    @Test
    public void testEqualsObjectNull()
    {
        assertNotNull(myTestObject);
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#equals(Object)}
     * .
     */
    @Test
    public void testEqualsObjectDifferentClasses()
    {
        assertFalse(myTestObject.equals(RandomStringUtils.randomAlphabetic(25)));
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#setMergeKeyName(String)}
     * .
     */
    @Test
    public void testSetMergeKeyName()
    {
        String testMergeKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject.setMergeKeyName(testMergeKey);

        assertEquals(testMergeKey, myTestObject.getMergeKeyName());
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#setSourceKeyName(String)}
     * .
     */
    @Test
    public void testSetSourceKeyName()
    {
        String testSourceKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject.setSourceKeyName(testSourceKey);

        assertEquals(testSourceKey, myTestObject.getSourceKeyName());
    }

    /**
     * Test method for
     * {@link io.opensphere.mantle.data.merge.MetaDataMergeKeyMapEntry#toString()}
     * .
     */
    @Test
    public void testToString()
    {
        String testMergeKey = RandomStringUtils.randomAlphabetic(25);
        String testSourceKey = RandomStringUtils.randomAlphabetic(25);
        myTestObject = new MetaDataMergeKeyMapEntry(testMergeKey, testSourceKey);

        assertEquals(MetaDataMergeKeyMapEntry.class.getSimpleName() + " MergeKeyName[" + testMergeKey + "] SourceKeyName["
                + testSourceKey + "]", myTestObject.toString());
    }
}
