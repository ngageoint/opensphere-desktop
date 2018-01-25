package io.opensphere.csvcommon.detect;

import java.util.Collections;
import java.util.List;

import io.opensphere.csvcommon.common.LineSampler;

/**
 * A simple line sampler that gets its lines from a list of strings.
 */
public class ListLineSampler implements LineSampler
{
    /** The list of strings to use for the beginning lines. */
    private final List<String> myBeginLines;

    /** The list of strings to use for the end lines. */
    private final List<String> myEndLines;

    /** The number of lines between the beginning and the end. */
    private final int myMiddleLineCount;

    /**
     * Constructor.
     *
     * @param beginLines The list containing the beginning lines.
     */
    public ListLineSampler(List<String> beginLines)
    {
        this(beginLines, Collections.<String>emptyList(), 0);
    }

    /**
     * Constructor.
     *
     * @param beginLines The list containing the beginning lines.
     * @param endLines The list containing the end lines.
     * @param middleLineCount The number of imaginary lines in the middle.
     */
    public ListLineSampler(List<String> beginLines, List<String> endLines, int middleLineCount)
    {
        myBeginLines = Collections.unmodifiableList(beginLines);
        myMiddleLineCount = middleLineCount;
        myEndLines = Collections.unmodifiableList(endLines);
    }

    @Override
    public void close()
    {
    }

    @Override
    public List<? extends String> getBeginningSampleLines()
    {
        return myBeginLines;
    }

    @Override
    public int getEndingSampleLinesIndexOffset()
    {
        return myBeginLines.size() + myMiddleLineCount;
    }

    @Override
    public List<? extends String> getEndingSampleLines()
    {
        return myEndLines;
    }
}
