package io.opensphere.core.util;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/** Test {@link QuotingBufferedReader}. */
public class QuotingBufferedReaderTest
{
    /**
     * Test {@link QuotingBufferedReader#close()}.
     *
     * @throws IOException Impossible.
     */
    public void testClose() throws IOException
    {
        final boolean[] closed = new boolean[1];
        Reader in = new Reader()
        {
            @Override
            public void close() throws IOException
            {
                closed[0] = true;
            }

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException
            {
                return -1;
            }
        };
        QuotingBufferedReader reader = new QuotingBufferedReader(in, null, null);
        reader.close();

        Assert.assertTrue(closed[0]);
    }

    /**
     * Test {@link QuotingBufferedReader} with escapes.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    public void testEscapes() throws IOException
    {
        List<String> expected = New.list(5);
        expected.add("one\\\n\r\\ one");
        expected.add("two\\\n two\\\\");
        expected.add("three three");

        StringBuilder sb = new StringBuilder();
        for (String line : expected)
        {
            sb.append(line).append(StringUtilities.LINE_SEP);
        }

        Reader in = new CharArrayReader(sb.toString().toCharArray());

        for (int sz = 1; sz < sb.length() + 1; ++sz)
        {
            in.reset();
            QuotingBufferedReader reader = new QuotingBufferedReader(in, sz, null, new char[] { '\\' });

            List<String> actual = New.list(5);
            for (String line; (line = reader.readLine()) != null;)
            {
                actual.add(line);
            }
            Assert.assertEquals("Lines do not match when buffer size is " + sz, expected, actual);
        }
    }

    /**
     * Test {@link QuotingBufferedReader} when multiple blank lines occur
     * consecutively.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    public void testEmptyLines() throws IOException
    {
        String dataLine = "First,Line,With,Data";

        StringBuilder sb = new StringBuilder();
        sb.append("\n" + "\n\r" + "\n\r" + "\r" + "\r\n");
        sb.append(dataLine).append(StringUtilities.LINE_SEP);

        Reader in = new CharArrayReader(sb.toString().toCharArray());
        QuotingBufferedReader reader = new QuotingBufferedReader(in, null, null);

        for (int i = 0; i < 5; ++i)
        {
            String line = reader.readLine();
            Assert.assertEquals("Line should be empty for line " + i, "", line);
        }

        String line = reader.readLine();
        Assert.assertEquals(dataLine, line);
    }

    /**
     * Test {@link QuotingBufferedReader} with no input.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    public void testNoInput() throws IOException
    {
        Reader in = new CharArrayReader("".toCharArray());

        QuotingBufferedReader reader = new QuotingBufferedReader(in, new char[] { '"', '\'' }, new char[] { '\\' });

        Assert.assertEquals(null, reader.readLine());
    }

    /**
     * Test {@link QuotingBufferedReader} with no escapes or quotes.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    public void testNoQuotesOrEscapes() throws IOException
    {
        List<String> expected = New.list(5);
        expected.add("one one");
        expected.add("two\\ two\\\\");
        expected.add("three three");

        StringBuilder sb = new StringBuilder();
        for (String line : expected)
        {
            sb.append(line).append(StringUtilities.LINE_SEP);
        }

        Reader in = new CharArrayReader(sb.toString().toCharArray());

        for (int sz = 1; sz < sb.length() + 1; ++sz)
        {
            in.reset();
            QuotingBufferedReader reader = new QuotingBufferedReader(in, sz, null, null);

            List<String> actual = New.list(5);
            for (String line; (line = reader.readLine()) != null;)
            {
                actual.add(line);
            }
            Assert.assertEquals("Lines do not match when buffer size is " + sz, expected, actual);
        }
    }

    /**
     * Test {@link QuotingBufferedReader} with quotes.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    public void testQuotes() throws IOException
    {
        List<String> expected = New.list(5);
        expected.add("one one");
        expected.add("two\\ two\\\\");
        expected.add("\\\"three\n thr'ee\"");
        expected.add("four'\n\r\"\n' four");
        expected.add("five five");

        StringBuilder sb = new StringBuilder();
        for (String line : expected)
        {
            sb.append(line).append(StringUtilities.LINE_SEP);
        }

        Reader in = new CharArrayReader(sb.toString().toCharArray());

        for (int sz = 1; sz < sb.length() + 1; ++sz)
        {
            in.reset();
            QuotingBufferedReader reader = new QuotingBufferedReader(in, sz, new char[] { '"', '\'' }, null);

            List<String> actual = New.list(5);
            for (String line; (line = reader.readLine()) != null;)
            {
                actual.add(line);
            }
            Assert.assertEquals("Lines do not match when buffer size is " + sz, expected, actual);
        }
    }

    /**
     * Test {@link QuotingBufferedReader} with quotes and escapes.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Test
    public void testQuotesAndEscapes() throws IOException
    {
        List<String> expected = New.list(5);
        expected.add("one\\\n\r\\ one");
        expected.add("two\\\n two\\\\");
        expected.add("\"three\n thr'ee\"");
        expected.add("four'\n\r\"\n' four");
        expected.add("five five");

        StringBuilder sb = new StringBuilder();
        for (String line : expected)
        {
            sb.append(line).append(StringUtilities.LINE_SEP);
        }

        Reader in = new CharArrayReader(sb.toString().toCharArray());

        for (int sz = 1; sz < sb.length() + 1; ++sz)
        {
            in.reset();
            QuotingBufferedReader reader = new QuotingBufferedReader(in, sz, new char[] { '"', '\'' }, new char[] { '\\' });

            List<String> actual = New.list(5);
            for (String line; (line = reader.readLine()) != null;)
            {
                actual.add(line);
            }
            Assert.assertEquals("Lines do not match when buffer size is " + sz, expected, actual);
        }
    }
}
