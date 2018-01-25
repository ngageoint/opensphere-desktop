package io.opensphere.csvcommon.detect.columnformat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.ListLineSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatDetector;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.FixedWidthColumnFormatParameters;
import io.opensphere.csvcommon.util.CsvTestUtils;

import org.junit.Assert;

/**
 * Test for {@link ColumnFormatDetector}.
 */
public class ColumnFormatDetectorTest
{
    /**
     * Test for comma/space-delimited.
     */
    @Test
    public void testCommaAndSpaceDelimited()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector
                .detect(new ListLineSampler(CsvTestUtils.createBasicDelimitedData(", ")));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf(','), ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
    }

    /**
     * Test for comma-delimited.
     */
    @Test
    public void testCommaDelimited()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector
                .detect(new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf(','), ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
    }

    /**
     * Test for crap at the top of the file.
     */
    @Test
    public void testCrap()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        List<String> data = New.list();
        data.addAll(CsvTestUtils.createCrap());
        data.addAll(CsvTestUtils.createBasicDelimitedData(","));
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector.detect(new ListLineSampler(data));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf(','), ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
    }

    /**
     * Test guessing the delimiters when there are an excessive amount of quoted
     * columns.
     */
    @Test
    public void testExcessiveQuote()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        List<String> data = CsvTestUtils.createBasicDelimitedData(",", Character.valueOf('"'), false);
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector.detect(new ListLineSampler(data));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf(','), ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
        Assert.assertEquals(Character.valueOf('"'), ((DelimitedColumnFormatParameters)result.getBestValue()).getTextDelimiter());
    }

    /**
     * Test for fixed-width.
     */
    @Test
    public void testFixedWidth()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector
                .detect(new ListLineSampler(CsvTestUtils.createBasicFixedWidthData()));
        Assert.assertTrue(result.getBestValue() instanceof FixedWidthColumnFormatParameters);
        Assert.assertTrue(Arrays.equals(new int[] { 21, 41, 61, 68 },
                ((FixedWidthColumnFormatParameters)result.getBestValue()).getColumnDivisions()));
    }

    /**
     * Test for hard case. This is currently disabled because it's too hard.
     */
    public void testHard()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector
                .detect(new ListLineSampler(CsvTestUtils.createHardData()));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf(','), ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
    }

    /**
     * Test for space-delimited.
     */
    @Test
    public void testSpaceDelimited()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector
                .detect(new ListLineSampler(CsvTestUtils.createBasicDelimitedData(" ")));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf(' '), ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
    }

    /**
     * Test guessing the delimiters when there are an excessive amount of quoted
     * columns.
     */
    @Test
    public void testSparseQuote()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        List<String> data = CsvTestUtils.createBasicDelimitedData(",", Character.valueOf('"'), true);
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector.detect(new ListLineSampler(data));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf(','), ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
        Assert.assertEquals(Character.valueOf('"'), ((DelimitedColumnFormatParameters)result.getBestValue()).getTextDelimiter());
    }

    /**
     * Test for tab-delimited.
     */
    @Test
    public void testTabDelimited()
    {
        ColumnFormatDetector detector = new ColumnFormatDetector();
        ValuesWithConfidence<? extends ColumnFormatParameters> result = detector
                .detect(new ListLineSampler(CsvTestUtils.createBasicDelimitedData("\t")));
        Assert.assertTrue(result.getBestValue() instanceof DelimitedColumnFormatParameters);
        Assert.assertEquals(Character.valueOf('\t'),
                ((DelimitedColumnFormatParameters)result.getBestValue()).getTokenDelimiter());
    }
}
