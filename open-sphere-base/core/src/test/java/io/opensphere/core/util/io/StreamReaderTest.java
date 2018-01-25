package io.opensphere.core.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * Test for {@link StreamReader}.
 */
public class StreamReaderTest
{
    /** The array to read. */
    private static final byte[] ARRAY = new byte[10000];

    /** Input stream. */
    private static final ByteArrayInputStream BAIS = new ByteArrayInputStream(ARRAY);

    static
    {
        for (int i = 0; i < ARRAY.length; ++i)
        {
            ARRAY[i] = (byte)(i % 0x100);
        }
    }

    /**
     * Reset the input stream before each test.
     */
    @Before
    public void beforeTest()
    {
        BAIS.reset();
    }

    /**
     * Test a contentLength that is smaller than ARRAY.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testContentLength1() throws IOException
    {
        int contentLength = 9999;
        StreamReader sr = new StreamReader(BAIS, contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer();
        Assert.assertEquals(contentLength, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[contentLength];
        result.get(arr);

        for (int i = 0; i < contentLength; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test a contentLength that is smaller than ARRAY, passing in the result
     * buffer.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testContentLength1WithBuffer() throws IOException
    {
        int contentLength = 9999;
        StreamReader sr = new StreamReader(BAIS, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertSame(buf, result);
        Assert.assertEquals(contentLength, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[contentLength];
        result.get(arr);

        for (int i = 0; i < contentLength; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test a contentLength that is smaller than ARRAY, passing in a result
     * buffer that is larger than the content length.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testContentLength1WithLargerBuffer() throws IOException
    {
        int contentLength = 9999;
        StreamReader sr = new StreamReader(BAIS, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(contentLength + 10);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertSame(buf, result);
        Assert.assertEquals(contentLength, result.limit());
        byte[] arr = new byte[contentLength];
        result.get(arr);

        for (int i = 0; i < contentLength; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test a contentLength that is smaller than ARRAY, passing in a result
     * buffer that is smaller than the content length.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testContentLength1WithSmallerBuffer() throws IOException
    {
        int contentLength = 9999;
        StreamReader sr = new StreamReader(BAIS, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(101);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertNotSame(buf, result);
        Assert.assertEquals(contentLength, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[contentLength];
        result.get(arr);

        for (int i = 0; i < contentLength; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test a content length that is larger than ARRAY.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testContentLength2() throws IOException
    {
        int contentLength = 10001;
        StreamReader sr = new StreamReader(BAIS, contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer();
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test a content length that is larger than ARRAY, passing in the result
     * buffer.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testContentLength2WithBuffer() throws IOException
    {
        int contentLength = 10001;
        StreamReader sr = new StreamReader(BAIS, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertSame(buf, result);
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test {@link StreamReader#copyStream(java.io.OutputStream)}.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testCopyStream() throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        InputStream result = new StreamReader(BAIS).copyStream(outStream);
        ByteBuffer resultBuffer = new StreamReader(result).readStreamIntoBuffer();
        byte[] resultArr = new byte[resultBuffer.limit()];
        resultBuffer.get(resultArr, 0, resultBuffer.limit());

        Assert.assertTrue(Arrays.equals(resultArr, ARRAY));
        Assert.assertTrue(Arrays.equals(outStream.toByteArray(), ARRAY));
    }

    /**
     * Test content length and estimated size smaller than ARRAY.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize1() throws IOException
    {
        int estimatedSize = 9999;
        int contentLength = 9999;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer();
        Assert.assertEquals(contentLength, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[contentLength];
        result.get(arr);

        for (int i = 0; i < contentLength; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test content length and estimated size smaller than ARRAY, passing in the
     * result buffer.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize1WithBuffer() throws IOException
    {
        int estimatedSize = 9999;
        int contentLength = 9999;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertSame(buf, result);
        Assert.assertEquals(contentLength, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[contentLength];
        result.get(arr);

        for (int i = 0; i < contentLength; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test content length and estimated size larger than ARRAY.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize2() throws IOException
    {
        int estimatedSize = 10001;
        int contentLength = 10001;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer();
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test content length and estimated size larger than ARRAY, passing in the
     * result buffer.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize2WithBuffer() throws IOException
    {
        int estimatedSize = 10001;
        int contentLength = 10001;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertSame(buf, result);
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test an estimated size that is too small and a contentLength that is too
     * big.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize3() throws IOException
    {
        int estimatedSize = 500;
        int contentLength = 10001;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer();
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test an estimated size that is too small and a contentLength that is too
     * big, passing in the result buffer.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize3WithBuffer() throws IOException
    {
        int estimatedSize = 500;
        int contentLength = 10001;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertSame(buf, result);
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test an estimated size that is correct with a contentLength that is
     * large.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize4() throws IOException
    {
        int estimatedSize = 10000;
        int contentLength = 20000;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer();
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(estimatedSize + 1, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test an estimated size that is correct with a contentLength that is
     * large, passing in the result buffer.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testEstimatedSize4WithBuffer() throws IOException
    {
        int estimatedSize = 10000;
        int contentLength = 20000;
        StreamReader sr = new StreamReader(BAIS, estimatedSize, contentLength);
        ByteBuffer buf = ByteBuffer.allocate(contentLength);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertSame(buf, result);
        Assert.assertEquals(ARRAY.length, result.limit());
        Assert.assertEquals(contentLength, result.capacity());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        for (int i = 0; i < ARRAY.length; ++i)
        {
            Assert.assertEquals(ARRAY[i], arr[i]);
        }
    }

    /**
     * Test a {@link StreamReader} with no estimated size or content length.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testNoLimit() throws IOException
    {
        StreamReader sr = new StreamReader(BAIS);
        ByteBuffer result = sr.readStreamIntoBuffer();
        Assert.assertEquals(ARRAY.length, result.limit());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        Assert.assertTrue(Arrays.equals(arr, ARRAY));
    }

    /**
     * Test a {@link StreamReader} with no estimated size or content length,
     * passing in the result buffer.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testNoLimitWithBuffer() throws IOException
    {
        StreamReader sr = new StreamReader(BAIS);
        ByteBuffer buf = ByteBuffer.allocate(ARRAY.length - 1);
        ByteBuffer result = sr.readStreamIntoBuffer(buf);
        Assert.assertNotSame(buf, result);
        Assert.assertEquals(ARRAY.length, result.limit());
        byte[] arr = new byte[ARRAY.length];
        result.get(arr);

        Assert.assertTrue(Arrays.equals(arr, ARRAY));
    }

    /**
     * Test {@link StreamReader#readStreamIntoString(java.nio.charset.Charset)}.
     *
     * @throws IOException If there is an error.
     */
    @Test
    public void testReadStreamIntoString() throws IOException
    {
        String testString = "I know what I am and I'm glad I'm a man, and so is Lola.";
        final byte[] bytes = testString.getBytes(StringUtilities.DEFAULT_CHARSET);
        InputStream is = new InputStream()
        {
            /** The position. */
            private int myPosition;

            @Override
            public int read() throws IOException
            {
                if (myPosition == bytes.length)
                {
                    return -1;
                }
                return bytes[myPosition++];
            }

            @Override
            public synchronized void reset() throws IOException
            {
                myPosition = 0;
            }
        };

        Assert.assertEquals(testString, new StreamReader(is).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
        is.reset();
        Assert.assertEquals(testString, new StreamReader(is, 1, -1).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
        is.reset();
        Assert.assertEquals(testString,
                new StreamReader(is, testString.length() - 1, -1).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
        is.reset();
        Assert.assertEquals(testString,
                new StreamReader(is, testString.length(), -1).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
        is.reset();
        Assert.assertEquals(testString,
                new StreamReader(is, testString.length() + 1, -1).readStreamIntoString(StringUtilities.DEFAULT_CHARSET));
    }
}
