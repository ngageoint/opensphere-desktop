package io.opensphere.core.util.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;

/**
 * Tests the {@link ListOfBytesInputStream} class.
 */
public class ListOfBytesInputStreamTest
{
    /**
     * Tests the mark reset and mark supported functionality.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testMark() throws IOException
    {
        List<byte[]> data = New.list();
        data.add(new byte[] { 0, 1, 2 });

        try (ListOfBytesInputStream in = new ListOfBytesInputStream(data, 3, 100000))
        {
            assertEquals(3, in.available());

            assertTrue(in.markSupported());

            assertEquals(0, in.read());
            assertEquals(2, in.available());

            in.mark(0);

            assertEquals(1, in.read());
            assertEquals(1, in.available());

            assertEquals(2, in.read());
            assertEquals(0, in.available());

            in.reset();

            assertEquals(2, in.available());
            assertEquals(1, in.read());
            assertEquals(1, in.available());

            assertEquals(2, in.read());
            assertEquals(0, in.available());
        }
    }

    /**
     * Tests reading a byte.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testRead() throws IOException
    {
        List<byte[]> data = New.list();
        data.add(new byte[] { 0, 1, 2 });

        try (ListOfBytesInputStream in = new ListOfBytesInputStream(data, 3, 100000))
        {
            assertEquals(3, in.available());

            assertEquals(0, in.read());
            assertEquals(2, in.available());

            assertEquals(1, in.read());
            assertEquals(1, in.available());

            assertEquals(2, in.read());
            assertEquals(0, in.available());
        }
    }

    /**
     * Tests reading a large byte array from the input stream.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testReadByteArrayIntInt() throws IOException
    {
        byte[] array = new byte[256000];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = (byte)(i % 10);
            byte adder = (byte)(i / 100000);
            array[i] += adder;
        }

        List<byte[]> data = New.list();
        try (ListOfBytesOutputStream out = new ListOfBytesOutputStream())
        {
            out.write(array);
            data.addAll(out.toArrays());

            try (ListOfBytesInputStream in = new ListOfBytesInputStream(data, out.size(), out.getChunkSize()))
            {
                byte[] actual = new byte[in.available()];
                int read = in.read(actual);

                assertEquals(0, in.available());

                assertEquals(array.length, read);
                assertEquals(array.length, actual.length);

                for (int i = 0; i < array.length; i++)
                {
                    assertEquals(array[i], actual[i]);
                }
            }
        }
    }

    /**
     * Tests skipping.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testSkip() throws IOException
    {
        List<byte[]> data = New.list();
        data.add(new byte[] { 0, 1, 2 });

        try (ListOfBytesInputStream in = new ListOfBytesInputStream(data, 3, 100000))
        {
            assertEquals(3, in.available());

            long skipped = in.skip(2);
            assertEquals(2, skipped);

            assertEquals(1, in.available());
            assertEquals(2, in.read());
            assertEquals(0, in.available());
        }
    }
}
