package io.opensphere.csvcommon.detect.controller;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.LineSampler;

/** A line sampler that reads from a {@link QuotingBufferedReader}. */
public class ReaderLineSampler implements LineSampler
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ReaderLineSampler.class);

    /** The samples at the beginning of the file. */
    private final List<String> myBeginSamples = New.list();

    /** The number of lines sampled prior to the ending sample lines. */
    private int myEndingSampleLinesIndexOffset;

    /** The samples at the end of the file. */
    private final List<String> myEndSamples = New.list();

    /** The reader. */
    private final QuotingBufferedReader myReader;

    /**
     * Constructor that takes a {@link QuotingBufferedReader}.
     *
     * @param reader The reader.
     * @param beginLines The number of lines to sample at the beginning.
     * @param endLines The number of lines to sample at the end.
     */
    public ReaderLineSampler(QuotingBufferedReader reader, int beginLines, int endLines)
    {
        myReader = reader;
        sampleLines(beginLines, endLines);
    }

    /**
     * Constructor that takes a {@link Reader}.
     *
     * @param reader The reader.
     * @param beginLines The number of lines to sample at the beginning.
     * @param endLines The number of lines to sample at the end.
     */
    public ReaderLineSampler(Reader reader, int beginLines, int endLines)
    {
        this(reader instanceof QuotingBufferedReader ? (QuotingBufferedReader)reader
                : new QuotingBufferedReader(reader, null, null), beginLines, endLines);
    }

    @Override
    public void close() throws IOException
    {
        myReader.close();
    }

    @Override
    public List<? extends String> getBeginningSampleLines()
    {
        return myBeginSamples;
    }

    @Override
    public List<? extends String> getEndingSampleLines()
    {
        return myEndSamples;
    }

    @Override
    public int getEndingSampleLinesIndexOffset()
    {
        return myEndingSampleLinesIndexOffset;
    }

    /**
     * Get the reader.
     *
     * @return the reader for this sampler.
     */
    public QuotingBufferedReader getReader()
    {
        return myReader;
    }

    /**
     * Read a line from the reader.
     *
     * @return The line, or {@code null} if EOF was reached.
     * @throws IOException If there is an error reading.
     */
    private String readLine() throws IOException
    {
        return myReader.readLine();
    }

    /**
     * Ensure that the given number of lines have been sampled, if possible.
     *
     * @param beginLines The desired number of lines to sample at the beginning
     *            of the document.
     * @param endLines The desired number of lines to sample at the end of the
     *            document.
     */
    private void sampleLines(int beginLines, int endLines)
    {
        try
        {
            while (myBeginSamples.size() < beginLines)
            {
                String line = readLine();
                if (line == null)
                {
                    break;
                }
                else
                {
                    myBeginSamples.add(line);
                }
            }

            myEndingSampleLinesIndexOffset = myBeginSamples.size();

            LinkedList<String> endSamples = new LinkedList<>();
            for (String line; (line = readLine()) != null;)
            {
                endSamples.add(line);

                if (endSamples.size() > endLines)
                {
                    ++myEndingSampleLinesIndexOffset;
                    endSamples.removeFirst();
                }
            }
            myEndSamples.addAll(endSamples);
        }
        catch (IOException e)
        {
            LOGGER.error("Failed to sample line: " + e, e);
        }
    }
}
