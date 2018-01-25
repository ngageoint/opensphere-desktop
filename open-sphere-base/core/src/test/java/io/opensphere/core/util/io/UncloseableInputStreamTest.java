package io.opensphere.core.util.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link UncloseableInputStream}. */
public class UncloseableInputStreamTest
{
    /**
     * Test for {@link UncloseableInputStream#available()}.
     *
     * @throws IOException If the test fails.
     */
    @SuppressWarnings("resource")
    @Test
    public void testAvailable() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public synchronized int available()
            {
                called[0] = true;
                return super.available();
            }
        };
        new UncloseableInputStream(wrapped).available();
        Assert.assertTrue(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#close()}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testClose() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public void close() throws IOException
            {
                called[0] = true;
                super.close();
            }
        };
        new UncloseableInputStream(wrapped).close();
        Assert.assertFalse(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#getWrappedStream()}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testGetWrappedStream() throws IOException
    {
        InputStream wrapped = new ByteArrayInputStream(new byte[0]);
        Assert.assertSame(wrapped, new UncloseableInputStream(wrapped).getWrappedStream());
    }

    /**
     * Test for {@link UncloseableInputStream#mark(int)}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testMark() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public void mark(int readAheadLimit)
            {
                called[0] = true;
                super.mark(readAheadLimit);
            }
        };
        new UncloseableInputStream(wrapped).mark(0);
        Assert.assertTrue(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#markSupported()}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testMarkSupported() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public boolean markSupported()
            {
                called[0] = true;
                return super.markSupported();
            }
        };
        new UncloseableInputStream(wrapped).markSupported();
        Assert.assertTrue(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#read()}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testRead() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public synchronized int read()
            {
                called[0] = true;
                return super.read();
            }
        };
        new UncloseableInputStream(wrapped).read();
        Assert.assertTrue(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#read(byte[])}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testReadByteArray() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public int read(byte[] b) throws IOException
            {
                called[0] = true;
                return super.read(b);
            }
        };
        new UncloseableInputStream(wrapped).read(new byte[0]);
        Assert.assertTrue(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#read(byte[], int, int)}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testReadByteArrayIntInt() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public synchronized int read(byte[] b, int off, int len)
            {
                called[0] = true;
                return super.read(b, off, len);
            }
        };
        new UncloseableInputStream(wrapped).read(new byte[0], 0, 0);
        Assert.assertTrue(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#reset()}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testReset() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public synchronized void reset()
            {
                called[0] = true;
                super.reset();
            }
        };
        new UncloseableInputStream(wrapped).reset();
        Assert.assertTrue(called[0]);
    }

    /**
     * Test for {@link UncloseableInputStream#skip(long)}.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void testSkip() throws IOException
    {
        final boolean[] called = new boolean[1];
        InputStream wrapped = new ByteArrayInputStream(new byte[0])
        {
            @Override
            public synchronized long skip(long n)
            {
                called[0] = true;
                return super.skip(n);
            }
        };
        new UncloseableInputStream(wrapped).skip(0);
        Assert.assertTrue(called[0]);
    }
}
