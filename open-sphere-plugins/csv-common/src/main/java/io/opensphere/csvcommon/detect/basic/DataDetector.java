package io.opensphere.csvcommon.detect.basic;

import java.util.List;

import io.opensphere.core.model.IntegerRange;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.predicate.NonEmptyPredicate;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;

/**
 * Detector for the range of data lines from the sample lines.
 *
 * The range returned contains 0-based indexes of the first and last data line
 * in the sample data. These indexes may be different from what's actually in
 * the file.
 */
public class DataDetector implements CellDetector<IntegerRange>
{
    /**
     * Get the expected cell count.
     *
     * @param sampler the sampler
     * @return the expected cell count
     */
    private static int getExpectedCount(CellSampler sampler)
    {
        int expectedCount;
        int headerSize = sampler.getHeaderCells() == null ? 0 : sampler.getHeaderCells().size();
        if (headerSize > 0)
        {
            expectedCount = headerSize;
        }
        else
        {
            int[] counts = new int[sampler.getBeginningSampleCells().size()];
            int i = 0;
            for (List<? extends String> row : sampler.getBeginningSampleCells())
            {
                counts[i++] = row.size();
            }
            expectedCount = MathUtil.mode(counts, 0).getValue().intValue();
        }
        return expectedCount;
    }

    @Override
    public ValuesWithConfidence<IntegerRange> detect(CellSampler sampler)
    {
        int expectedCellCount = getExpectedCount(sampler);

        NonEmptyPredicate nonEmptyPredicate = new NonEmptyPredicate();

        // Find first data rows
        int firstDataRow = -1;
        for (int r = 0, n = sampler.getBeginningSampleCells().size(); r < n; r++)
        {
            List<? extends String> row = sampler.getBeginningSampleCells().get(r);
            boolean isDataRow = row.size() == expectedCellCount && row.stream().anyMatch(nonEmptyPredicate);
            if (isDataRow)
            {
                firstDataRow = r;
                break;
            }
        }

        // Find last data rows
        int lastDataRow = -1;
        for (int r = sampler.getEndingSampleCells().size() - 1; r >= 0; --r)
        {
            List<? extends String> row = sampler.getEndingSampleCells().get(r);
            boolean isDataRow = row.size() == expectedCellCount && row.stream().anyMatch(nonEmptyPredicate);
            if (isDataRow)
            {
                lastDataRow = r + sampler.getEndingSampleLinesIndexOffset();
                break;
            }
        }

        float confidence = firstDataRow == -1 ? 0f : 1f;
        return new ValuesWithConfidence<IntegerRange>(new IntegerRange(firstDataRow, lastDataRow), confidence);
    }
}
