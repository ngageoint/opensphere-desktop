package io.opensphere.csvcommon.common;

import java.util.List;

/**
 * An object that supplies a sampling of cells from a document.
 */
public interface CellSampler extends LineSampler
{
    /**
     * Get the cells available for sampling from the head of the document.
     *
     * @return A list of lists; each item in the outer list corresponds to one
     *         line from the file, each item in the inner list corresponds to
     *         one cell in the line.
     */
    List<? extends List<? extends String>> getBeginningSampleCells();

    /**
     * Get the cells available for sampling from the tail of the document.
     *
     * @return A list of lists; each item in the outer list corresponds to one
     *         line from the file, each item in the inner list corresponds to
     *         one cell in the line.
     */
    List<? extends List<? extends String>> getEndingSampleCells();

    /**
     * Get the header cells, if available.
     *
     * @return The header cells, or {@code null} if no header was detected.
     */
    List<? extends String> getHeaderCells();

    /**
     * Translate a sample line number to an absolute line number.
     *
     * @param line The sample line number.
     * @return The absolute line number.
     */
    int sampleLineToAbsoluteLine(int line);

    /**
     * Translate an absolute line to a sample line number. The inverse of
     * sampleLineToAbsoluteLine.
     *
     * @param line the line
     * @return the int
     */
    int absoluteLineToSampleLine(int line);
}
