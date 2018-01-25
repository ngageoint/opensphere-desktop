package io.opensphere.osh.results.video;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link ByteUtilities}. */
public class ByteUtilitiesTest
{
    /**
     * Tests
     * {@link ByteUtilities#readNBytes(java.io.InputStream, java.io.OutputStream, int)}
     * .
     *
     * @throws IOException if a problem occurs reading the stream
     */
    @Test
    public void testReadNBytes() throws IOException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int bytesRead = ByteUtilities.readNBytes(in, out, 5);
        Assert.assertEquals(5, bytesRead);
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4, 5 }, out.toByteArray()));
        Assert.assertEquals(6, in.read());

        out.reset();
        bytesRead = ByteUtilities.readNBytes(in, out, 5);
        Assert.assertEquals(2, bytesRead);
        Assert.assertTrue(Arrays.equals(new byte[] { 7, 8 }, out.toByteArray()));
        Assert.assertEquals(-1, in.read());
    }

    /**
     * Tests
     * {@link ByteUtilities#readUntilInclusive(PushbackInputStream, java.io.OutputStream, byte[])}
     * .
     *
     * @throws IOException if a problem occurs reading the stream
     */
    @Test
    public void testReadUntilInclusive() throws IOException
    {
        PushbackInputStream in = new PushbackInputStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }), 10);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteUtilities.readUntilInclusive(in, out, new byte[] { 4, 5, 6 });
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4, 5, 6 }, out.toByteArray()));
        Assert.assertEquals(7, in.read());
        Assert.assertEquals(8, in.read());
        Assert.assertEquals(-1, in.read());
    }

    /** Tests {@link ByteUtilities#indexOf(byte[], int, byte[])}. */
    @Test
    public void testIndexOf()
    {
        Assert.assertEquals(2, ByteUtilities.indexOf(new byte[] { 1, 2, 3, 4, 5 }, 5, new byte[] { 3, 4 }));
        Assert.assertEquals(3, ByteUtilities.indexOf(new byte[] { 1, 2, 3, 4, 5 }, 5, new byte[] { 4, 5 }));
        Assert.assertEquals(4, ByteUtilities.indexOf(new byte[] { 1, 2, 3, 4, 5 }, 5, new byte[] { 5 }));
        Assert.assertEquals(-1, ByteUtilities.indexOf(new byte[] { 1, 2, 3, 4, 5 }, 5, new byte[] { 5, 6 }));
    }

    /** Tests {@link ByteUtilities#shift(byte[], byte[], int)}. */
    @Test
    public void testShift()
    {
        byte[] bytes = new byte[] { 0, 0, 0 };
        ByteUtilities.shift(bytes, new byte[] { 1, 2, 3, 4, 5 }, 5);
        Assert.assertTrue(Arrays.equals(new byte[] { 3, 4, 5 }, bytes));

        ByteUtilities.shift(bytes, new byte[] { 1, 2, 3 }, 3);
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, bytes));

        bytes = new byte[] { 0, 1, 2, 3, 4, 5 };
        ByteUtilities.shift(bytes, new byte[] { 6, 7 }, 2);
        Assert.assertTrue(Arrays.equals(new byte[] { 2, 3, 4, 5, 6, 7 }, bytes));

        ByteUtilities.shift(bytes, new byte[] { 8, 9, 10 }, 2);
        Assert.assertTrue(Arrays.equals(new byte[] { 4, 5, 6, 7, 8, 9 }, bytes));
    }

    /** Tests {@link ByteUtilities#concat(byte[], byte[], int)}. */
    @Test
    public void testConcat()
    {
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3, 4, 5, 6 },
                ByteUtilities.concat(new byte[] { 1, 2 }, new byte[] { 3, 4, 5, 6 }, 4)));
    }
}
