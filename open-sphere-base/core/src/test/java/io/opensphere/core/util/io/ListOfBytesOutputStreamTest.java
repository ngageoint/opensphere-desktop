package io.opensphere.core.util.io;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

/**
 * Tests the {@link ListOfBytesOutputStream} class.
 */
public class ListOfBytesOutputStreamTest
{
    /**
     * Tests writing a single byte.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testWriteInt() throws IOException
    {
        try (ListOfBytesOutputStream out = new ListOfBytesOutputStream())
        {
            out.write(0);
            out.write(1);
            out.write(2);

            List<byte[]> bytes = out.toArrays();

            assertEquals(1, bytes.size());

            byte[] actual = bytes.get(0);

            assertEquals(3, actual.length);

            assertEquals(0, actual[0]);
            assertEquals(1, actual[1]);
            assertEquals(2, actual[2]);
        }
    }

    /**
     * Tests writing an array of bytes.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testWriteByteArray() throws IOException
    {
        byte[] array = new byte[256000];
        for (int i = 0; i < array.length; i++)
        {
            array[i] = (byte)(i % 10);
            byte adder = (byte)(i / 100000);
            array[i] += adder;
        }

        try (ListOfBytesOutputStream out = new ListOfBytesOutputStream())
        {
            out.write(array);

            assertEquals(256000, out.size());
            List<byte[]> byteList = out.toArrays();
            assertEquals(256000 / out.getChunkSize() + 1, byteList.size());

            for (int i = 0; i < byteList.size() - 1; i++)
            {
                byte[] bytes = byteList.get(i);
                assertEquals(out.getChunkSize(), bytes.length);
                for (int j = 0; j < bytes.length; j++)
                {
                    assertEquals((byte)(j % 10) + i, bytes[j]);
                }
            }

            byte[] lastBytes = byteList.get(byteList.size() - 1);
            assertEquals(256000 % out.getChunkSize(), lastBytes.length);

            for (int i = 0; i < lastBytes.length; i++)
            {
                assertEquals((byte)(i % 10) + byteList.size() - 1, lastBytes[i]);
            }
        }
    }

    /**
     * Tests the reset functionality.
     *
     * @throws IOException Bad IO.
     */
    @Test
    public void testReset() throws IOException
    {
        try (ListOfBytesOutputStream out = new ListOfBytesOutputStream())
        {
            out.write(0);
            out.write(1);
            out.write(2);

            List<byte[]> bytes = out.toArrays();

            assertEquals(1, bytes.size());

            byte[] actual = bytes.get(0);

            assertEquals(3, out.size());
            assertEquals(3, actual.length);

            assertEquals(0, actual[0]);
            assertEquals(1, actual[1]);
            assertEquals(2, actual[2]);

            out.reset();

            out.write(3);
            out.write(4);
            out.write(5);

            bytes = out.toArrays();

            assertEquals(1, bytes.size());

            actual = bytes.get(0);
            assertEquals(3, out.size());
            assertEquals(3, actual.length);

            assertEquals(3, actual[0]);
            assertEquals(4, actual[1]);
            assertEquals(5, actual[2]);
        }
    }
}
