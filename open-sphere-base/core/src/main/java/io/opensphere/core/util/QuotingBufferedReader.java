package io.opensphere.core.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * A buffered reader that accounts for quoted and escaped line separators.
 */
public class QuotingBufferedReader extends Reader
{
    /** The buffer of characters. */
    private char[] myBuffer;

    /** The number of characters read into the buffer. */
    private int myBufferLength;

    /** Possible escape characters. */
    private final char[] myEscapes;

    /** The current index into the buffer. */
    private int myIndex;

    /** Possible quote characters. */
    private final char[] myQuotes;

    /** The wrapped reader. */
    private final Reader myReader;

    /**
     * Constructor.
     *
     * @param in The wrapped reader.
     * @param quotes Possible quote characters.
     * @param escapes Possible escape characters.
     */
    public QuotingBufferedReader(Reader in, char[] quotes, char[] escapes)
    {
        this(in, 8192, quotes, escapes);
    }

    /**
     * Constructor.
     *
     * @param in The wrapped reader.
     * @param sz The buffer size.
     * @param quotes Possible quote characters.
     * @param escapes Possible escape characters.
     */
    public QuotingBufferedReader(Reader in, int sz, char[] quotes, char[] escapes)
    {
        if (sz <= 0)
        {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        myReader = Utilities.checkNull(in, "in");
        myBuffer = new char[sz];
        myQuotes = quotes == null ? new char[0] : quotes.clone();
        myEscapes = escapes == null ? new char[0] : escapes.clone();
    }

    /**
     * Sets the buffer size and resets {@link #myBufferLength} and
     * {@link #myIndex}.
     * <p>
     * This should be used instead of creating a new QuotingBufferedReader when
     * resetting the wrapped stream.
     *
     * @param size the new buffer size
     */
    public void setBuffer(int size)
    {
        myBuffer = new char[size];
        myBufferLength = 0;
        myIndex = 0;
    }

    @Override
    public void close() throws IOException
    {
        synchronized (lock)
        {
            myReader.close();
        }
    }

    /**
     * Get a copy of the possible quote characters.
     *
     * @return a copy of the possible quote characters.
     */
    public char[] getQuotes()
    {
        return Arrays.copyOf(myQuotes, myQuotes.length);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        synchronized (lock)
        {
            int start = myIndex;
            int count = 0;
            while (count < len)
            {
                if (myBufferLength < myIndex)
                {
                    myBufferLength = myReader.read(myBuffer);
                    if (myBufferLength == -1)
                    {
                        return count == 0 ? -1 : count;
                    }
                    myIndex = 0;
                }

                System.arraycopy(myBuffer, start, cbuf, off + count, myBufferLength - myIndex);
                count += myBufferLength - myIndex;
            }
            return count;
        }
    }

    /**
     * Read a line from the stream. A line is terminated by a line separator
     * that is not escaped and is not within quotes.
     *
     * @return The line, or {@code null} if the end of the stream has been
     *         reached.
     * @throws IOException If there is an I/O error.
     */
    public String readLine() throws IOException
    {
        synchronized (lock)
        {
            StringBuilder sb = null;
            String result = null;
            char quote = 0;
            boolean esc = false;
            int start = myIndex;
            boolean eol = false;
            boolean nl = false;
            boolean cr = false;
            while (true)
            {
                if (myBufferLength <= myIndex)
                {
                    if (!eol && start < myIndex)
                    {
                        if (sb == null)
                        {
                            result = new String(myBuffer, start, myIndex - start);
                        }
                        else
                        {
                            sb.append(myBuffer, start, myIndex - start);
                        }
                    }
                    start = 0;
                    myIndex = 0;
                    myBufferLength = myReader.read(myBuffer);
                    if (myBufferLength == -1)
                    {
                        return sb == null ? result : sb.toString();
                    }
                    else if (!eol)
                    {
                        if (result != null && sb == null)
                        {
                            sb = new StringBuilder(result.length() * 2);
                            sb.append(result);
                        }
                        result = null;
                    }
                }

                if (!esc && quote == 0 && (myBuffer[myIndex] == '\n' && !nl || myBuffer[myIndex] == '\r' && !cr))
                {
                    if (!eol)
                    {
                        result = sb == null ? new String(myBuffer, start, myIndex - start)
                                : sb.append(myBuffer, start, myIndex - start).toString();
                        start = myIndex;
                    }
                    nl |= myBuffer[myIndex] == '\n';
                    cr |= myBuffer[myIndex] == '\r';
                    eol = true;
                }
                else if (eol)
                {
                    return result;
                }
                else if (Utilities.indexOf(myEscapes, myBuffer[myIndex]) != -1)
                {
                    esc = !esc;
                }
                else if (!esc && myBuffer[myIndex] == quote)
                {
                    quote = 0;
                }
                else if (!esc && quote == 0 && Utilities.indexOf(myQuotes, myBuffer[myIndex]) != -1)
                {
                    quote = myBuffer[myIndex];
                }
                else if (esc && myBuffer[myIndex] != '\n' && myBuffer[myIndex] != '\r')
                {
                    esc = false;
                }

                ++myIndex;
            }
        }
    }

    @Override
    public boolean ready() throws IOException
    {
        synchronized (lock)
        {
            return myBufferLength > myIndex || myReader.ready();
        }
    }

    @Override
    public void reset() throws IOException
    {
        myReader.reset();
    }
}
