package io.opensphere.core.util;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link BufferUtilities}.
 */
public class BufferUtilitiesTest
{
    /** Random. */
    private static final Random RANDOM = new Random();

    /** Test for {@link BufferUtilities#clone(java.nio.ByteBuffer)}. */
    @Test
    public void testCloneByteBuffer()
    {
        byte[] arr = new byte[20];
        RANDOM.nextBytes(arr);
        ByteBuffer buf;
        ByteBuffer clone;

        buf = ByteBuffer.wrap(arr, 5, 10);
        clone = BufferUtilities.clone(buf);
        Assert.assertNotSame(arr, clone.array());
        Assert.assertEquals(buf.position(), clone.position());
        Assert.assertEquals(buf.limit(), clone.limit());
        Assert.assertEquals(buf.capacity(), clone.capacity());
        Assert.assertTrue(buf.isDirect() == clone.isDirect());
        buf.position(0).limit(buf.capacity());
        clone.position(0).limit(buf.capacity());
        Assert.assertTrue(buf.equals(clone));

        // Change a value to verify that the buffers use different storage.
        clone.put(5, (byte)(clone.get(5) ^ 1));
        Assert.assertFalse(buf.equals(clone));

        buf = buf.asReadOnlyBuffer();
        clone = BufferUtilities.clone(buf);
        Assert.assertNotSame(arr, clone.array());
        Assert.assertEquals(buf.position(), clone.position());
        Assert.assertEquals(buf.limit(), clone.limit());
        Assert.assertEquals(buf.capacity(), clone.capacity());
        Assert.assertTrue(buf.isDirect() == clone.isDirect());
        buf.position(0).limit(buf.capacity());
        clone.position(0).limit(buf.capacity());
        Assert.assertTrue(buf.equals(clone));

        // Change a value to verify that the buffers use different storage.
        clone.put(5, (byte)(clone.get(5) ^ 1));
        Assert.assertFalse(buf.equals(clone));

        // Test direct.
        buf = ByteBuffer.allocateDirect(arr.length);
        buf.put(arr);
        clone = BufferUtilities.clone(buf);
        Assert.assertEquals(buf.position(), clone.position());
        Assert.assertEquals(buf.limit(), clone.limit());
        Assert.assertEquals(buf.capacity(), clone.capacity());
        buf.position(0).limit(buf.capacity());
        clone.position(0).limit(buf.capacity());
        Assert.assertTrue(buf.equals(clone));

        // Change a value to verify that the buffers use different storage.
        clone.put(5, (byte)(clone.get(5) ^ 1));
        Assert.assertFalse(buf.equals(clone));
    }
}
