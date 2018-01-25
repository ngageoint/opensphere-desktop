package io.opensphere.csvcommon.detect.controller;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.detect.ListLineSampler;
import io.opensphere.csvcommon.detect.controller.CellSamplerImpl;

/**
 * Test {@link CellSamplerImpl}.
 */
public class CellSamplerImplTest
{
    /** Test {@link CellSamplerImpl} with no header line. */
    @Test
    public void testNoHeader()
    {
        List<String> beginLines = Arrays.asList("A1, B1, C1", "A2, B2, C2", "A3, B3, C3");
        List<String> endLines = Arrays.asList("A13, B13, C13", "A14, B14, C14", "A15, B15, C15");
        LineSampler lineSampler = new ListLineSampler(beginLines, endLines, 10);
        StringTokenizer tokenizer = new StringTokenizer()
        {
            @Override
            public List<String> tokenize(String input)
            {
                return Arrays.asList(input.split(", "));
            }
        };
        CellSamplerImpl sampler = new CellSamplerImpl(lineSampler, tokenizer, -1);
        Assert.assertNull(sampler.getHeaderCells());
        Assert.assertEquals(beginLines, sampler.getBeginningSampleLines());
        Assert.assertEquals(endLines, sampler.getEndingSampleLines());

        List<? extends List<? extends String>> beginCells = sampler.getBeginningSampleCells();
        Assert.assertEquals(3, beginCells.size());
        Assert.assertEquals(Arrays.asList("A1", "B1", "C1"), beginCells.get(0));
        Assert.assertEquals(Arrays.asList("A2", "B2", "C2"), beginCells.get(1));
        Assert.assertEquals(Arrays.asList("A3", "B3", "C3"), beginCells.get(2));

        // Test getting cells outside the tokenized range.
        Assert.assertEquals("", beginCells.get(0).get(4));

        List<? extends List<? extends String>> endCells = sampler.getEndingSampleCells();
        Assert.assertEquals(3, endCells.size());
        Assert.assertEquals(Arrays.asList("A13", "B13", "C13"), endCells.get(0));
        Assert.assertEquals(Arrays.asList("A14", "B14", "C14"), endCells.get(1));
        Assert.assertEquals(Arrays.asList("A15", "B15", "C15"), endCells.get(2));
        Assert.assertEquals(13, lineSampler.getEndingSampleLinesIndexOffset());

        Assert.assertEquals("", endCells.get(0).get(4));
    }

    /** Test {@link CellSamplerImpl} with a header line. */
    @Test
    public void testWithHeader()
    {
        List<String> beginLines = Arrays.asList("A1, B1, C1", "A2, B2, C2", "A3, B3, C3", "A4, B4, C4");
        List<String> endLines = Arrays.asList("A13, B13, C13", "A14, B14, C14", "A15, B15, C15");
        LineSampler lineSampler = new ListLineSampler(beginLines, endLines, 10);
        StringTokenizer tokenizer = new StringTokenizer()
        {
            @Override
            public List<String> tokenize(String input)
            {
                return Arrays.asList(input.split(", "));
            }
        };
        CellSamplerImpl sampler = new CellSamplerImpl(lineSampler, tokenizer, 1);
        Assert.assertEquals(Arrays.asList("A2", "B2", "C2"), sampler.getHeaderCells());
        Assert.assertEquals(beginLines.subList(2, 4), sampler.getBeginningSampleLines());
        Assert.assertEquals(endLines, sampler.getEndingSampleLines());

        List<? extends List<? extends String>> cells = sampler.getBeginningSampleCells();
        Assert.assertEquals(2, cells.size());
        Assert.assertEquals(Arrays.asList("A3", "B3", "C3"), cells.get(0));
        Assert.assertEquals(Arrays.asList("A4", "B4", "C4"), cells.get(1));

        Assert.assertEquals(2, sampler.sampleLineToAbsoluteLine(0));
        Assert.assertEquals(3, sampler.sampleLineToAbsoluteLine(1));

        List<? extends List<? extends String>> endCells = sampler.getEndingSampleCells();
        Assert.assertEquals(3, endCells.size());
        Assert.assertEquals(Arrays.asList("A13", "B13", "C13"), endCells.get(0));
        Assert.assertEquals(Arrays.asList("A14", "B14", "C14"), endCells.get(1));
        Assert.assertEquals(Arrays.asList("A15", "B15", "C15"), endCells.get(2));
        Assert.assertEquals(12, sampler.getEndingSampleLinesIndexOffset());
    }
}
