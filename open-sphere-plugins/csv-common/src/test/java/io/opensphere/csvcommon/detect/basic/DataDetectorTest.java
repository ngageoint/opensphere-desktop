package io.opensphere.csvcommon.detect.basic;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.IntegerRange;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.ListCellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.basic.DataDetector;
import io.opensphere.csvcommon.util.CsvTestUtils;

/** Test for {@link DataDetector}. */
public class DataDetectorTest
{
    /**
     * Test with no header.
     */
    @Test
    public void testNoHeader()
    {
        List<String> rows = CsvTestUtils.createSparseRowData();

        CellSampler sampler = new ListCellSampler(rows, rows, Character.valueOf(','), null, true);
        ValuesWithConfidence<IntegerRange> result = new DataDetector().detect(sampler);

        Assert.assertEquals(1f, result.getBestConfidence(), 0f);
        Assert.assertEquals(0, result.getBestValue().getMin().intValue());
        Assert.assertEquals(23, result.getBestValue().getMax().intValue());
    }

    /**
     * Test with a header.
     */
    @Test
    public void testWithHeader()
    {
        List<String> rows = CsvTestUtils.createSparseRowData();

        CellSampler sampler = new ListCellSampler(rows, rows, Character.valueOf(','), null);
        ValuesWithConfidence<IntegerRange> result = new DataDetector().detect(sampler);

        Assert.assertEquals(1f, result.getBestConfidence(), 0f);
        Assert.assertEquals(2, result.getBestValue().getMin().intValue());
        Assert.assertEquals(22, result.getBestValue().getMax().intValue());
    }
}
