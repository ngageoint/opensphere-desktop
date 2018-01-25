package io.opensphere.csvcommon.common;

import java.io.IOException;
import java.util.List;

/**
 * An object that supplies a sampling of rows from a document.
 */
public interface LineSampler
{
    /**
     * Close the file.
     *
     * @throws IOException If there is an error closing the file.
     */
    void close() throws IOException;

    /**
     * Get the lines available for sampling at the beginning of the document.
     *
     * @return The lines.
     */
    List<? extends String> getBeginningSampleLines();

    /**
     * Get the lines available for sampling at the end of the document.
     *
     * @return The lines.
     */
    List<? extends String> getEndingSampleLines();

    /**
     * Get the line index offset that applies to the ending sample lines
     * returned by {@link #getEndingSampleLines()}. For example, if this method
     * returns 499, then <code>getEndSampleLines().get(0)</code> will return the
     * 500th (index 499) line of the sampled document. Calling
     * <code>getEndingSampleLines().size() + getEndingSampleLineIndexOffset()</code>
     * should give you the number of lines in the document.
     *
     * @return The line index offset.
     */
    int getEndingSampleLinesIndexOffset();
}
