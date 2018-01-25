package io.opensphere.csvcommon.detect.basic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.basic.HeaderDetector;
import io.opensphere.csvcommon.util.CsvTestUtils;

/** Test for {@link HeaderDetector}. */
public class HeaderDetectorTest
{
    /**
     * Test the detector with a header in the first row.
     */
    @Test
    public void testBasic()
    {
        List<List<String>> cells = CsvTestUtils.createBasicData();
        CellSampler sampler = createMockCellSampler(cells);

        ValuesWithConfidence<Integer> result = new HeaderDetector(5).detect(sampler);

        Assert.assertEquals(1f, result.getBestConfidence(), 0f);
        Assert.assertEquals(0, result.getBestValue().intValue());
    }

    /**
     * Test the detector with some crap in the first few lines.
     */
    @Test
    public void testCrap()
    {
        List<List<String>> cells = New.list();
        List<String> rows = CsvTestUtils.createCrap();
        for (String row : rows)
        {
            cells.add(Collections.singletonList(row));
        }
        cells.addAll(CsvTestUtils.createBasicData());

        CellSampler sampler = createMockCellSampler(cells);

        ValuesWithConfidence<Integer> result = new HeaderDetector(5).detect(sampler);

        Assert.assertEquals(1f, result.getBestConfidence(), 0f);
        Assert.assertEquals(rows.size(), result.getBestValue().intValue());
    }

    /**
     * Test the detector with no header.
     */
    @Test
    public void testNoHeader()
    {
        List<List<String>> cells = New.list();

        List<String> rows = CsvTestUtils.createHardData();
        for (String row : rows)
        {
            cells.add(Arrays.asList(row.split(",")));
        }

        CellSampler sampler = createMockCellSampler(cells);

        ValuesWithConfidence<Integer> result = new HeaderDetector(3).detect(sampler);

        Assert.assertEquals(0f, result.getBestConfidence(), 0f);
    }

    /**
     * Test the detector with a header with spaces and underscores.
     */
    @Test
    public void testSpacesAndUnderscoresAndQuotes()
    {
        final List<List<String>> cells = New.list();
        cells.add(Arrays.asList("There", " is a 'house'", " in_\"New_Orleans\""));
        cells.add(Arrays.asList("5", "8", "12"));
        cells.add(Arrays.asList("a", "", ""));

        CellSampler sampler = createMockCellSampler(cells);

        ValuesWithConfidence<Integer> result = new HeaderDetector(3).detect(sampler);

        Assert.assertEquals(1f, result.getBestConfidence(), 0f);
        Assert.assertEquals(0, result.getBestValue().intValue());
    }

    /**
     * Create the mock cell sampler.
     *
     * @param cells The cells to provide.
     * @return The cell sampler.
     */
    private CellSampler createMockCellSampler(List<? extends List<? extends String>> cells)
    {
        CellSampler sampler = EasyMock.createMock(CellSampler.class);
        EasyMock.expect(sampler.getBeginningSampleCells());
        EasyMock.expectLastCall().andReturn(cells);
        EasyMock.replay(sampler);
        return sampler;
    }
}
