package io.opensphere.core.util.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** Tests for {@link ByteBufferInputStream}. */
@SuppressFBWarnings("OS_OPEN_STREAM")
public class ByteBufferInputStreamTest
{
    /** The capacity to use for the buffer. */
    private static final int CAPACITY = 20;

    /** The limit to use for the buffer. */
    private static final int LIMIT = 15;

    /** The offset to use for the buffer. */
    private static final int OFFSET = 5;

    /** Random number generator. */
    private static final Random RANDOM = new Random();

    /** Test for {@link ByteBufferInputStream#available()}. */
    @Test
    public void testAvailable()
    {
        ByteBuffer buf = createBuffer();

        ByteBufferInputStream bbis = new ByteBufferInputStream(buf);
        Assert.assertEquals(10, bbis.available());
        bbis.read();
        Assert.assertEquals(9, bbis.available());
        buf.get();
        Assert.assertEquals(8, bbis.available());
    }

    /**
     * Test for {@link ByteBufferInputStream#mark(int)} and
     * {@link ByteBufferInputStream#reset()}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testMark() throws IOException
    {
        ByteBuffer buf = createBuffer();

        ByteBufferInputStream bbis = new ByteBufferInputStream(buf);
        Assert.assertTrue(bbis.markSupported());
        Assert.assertEquals(5, bbis.skip(5));
        Assert.assertEquals(OFFSET + 5, buf.position());
        bbis.mark(0);
        Assert.assertEquals(5, bbis.skip(5));
        Assert.assertEquals(OFFSET + 10, buf.position());
        bbis.reset();
        Assert.assertEquals(OFFSET + 5, buf.position());
    }

    /** Test for {@link ByteBufferInputStream#read()}. */
    @Test
    public void testRead()
    {
        ByteBuffer buf = createBuffer();

        ByteBufferInputStream bbis = new ByteBufferInputStream(buf);
        for (int index = OFFSET; index < LIMIT; ++index)
        {
            Assert.assertEquals(buf.get(index) & 0xFF, bbis.read());
        }
    }

    /**
     * Test for {@link ByteBufferInputStream#read(byte[])}.
     */
    @Test
    public void testReadByteArray()
    {
        ByteBuffer buf = createBuffer();

        ByteBufferInputStream bbis = new ByteBufferInputStream(buf);
        byte[] arr = new byte[5];
        Assert.assertEquals(arr.length, bbis.read(arr));
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(buf.get(index + OFFSET), arr[index]);
        }
    }

    /** Test for {@link ByteBufferInputStream#read(byte[], int, int)}. */
    @Test
    public void testReadByteArrayIntInt()
    {
        ByteBuffer buf = createBuffer();

        ByteBufferInputStream bbis = new ByteBufferInputStream(buf);
        byte[] arr = new byte[7];
        Assert.assertEquals(5, bbis.read(arr, 2, 5));
        for (int index = 0; index < arr.length - 2; ++index)
        {
            Assert.assertEquals(buf.get(index + OFFSET), arr[index + 2]);
        }
    }

    /** Test for {@link ByteBufferInputStream#skip(long)}. */
    @Test
    public void testSkip()
    {
        ByteBuffer buf = createBuffer();

        ByteBufferInputStream bbis = new ByteBufferInputStream(buf);
        Assert.assertEquals(5, bbis.skip(5));
        Assert.assertEquals(LIMIT - OFFSET - 5, bbis.available());

        byte[] arr = new byte[bbis.available()];
        bbis.read(arr);
        for (int index = 0; index < arr.length; ++index)
        {
            Assert.assertEquals(buf.get(index + OFFSET + 5), arr[index]);
        }
    }

    /**
     * Create a buffer for testing.
     *
     * @return The buffer.
     */
    private ByteBuffer createBuffer()
    {
        byte[] arr = new byte[CAPACITY];
        RANDOM.nextBytes(arr);
        ByteBuffer buf = ByteBuffer.wrap(arr, OFFSET, LIMIT - OFFSET);
        return buf.asReadOnlyBuffer();
    }
}
