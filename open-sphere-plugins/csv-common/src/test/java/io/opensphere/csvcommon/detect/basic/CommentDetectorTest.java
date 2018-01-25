package io.opensphere.csvcommon.detect.basic;

import java.util.List;

import org.junit.Test;

import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.csvcommon.detect.ListLineSampler;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.basic.CommentDetector;
import io.opensphere.csvcommon.util.CsvTestUtils;

import org.junit.Assert;

/** Test for {@link CommentDetector}. */
public class CommentDetectorTest
{
    /**
     * Test basic case.
     */
    @Test
    public void testBasic()
    {
        ValuesWithConfidence<Character> result = new CommentDetector()
                .detect(new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",")));
        Assert.assertNull(result.getBestValue());
    }

    /**
     * Test basic case with empty line.
     */
    @Test
    public void testBasicWithEmptyLine()
    {
        List<String> data = CsvTestUtils.createBasicDelimitedData(",", Character.valueOf('"'), false);
        data.add(StringUtilities.join(",", new String[] { "", "", "", "", "" }));
        ValuesWithConfidence<Character> result = new CommentDetector().detect(new ListLineSampler(data));
        Assert.assertNull(result.getBestValue());
    }

    /**
     * Test basic case with quotes.
     */
    @Test
    public void testBasicWithQuotes()
    {
        ValuesWithConfidence<Character> result = new CommentDetector()
                .detect(new ListLineSampler(CsvTestUtils.createBasicDelimitedData(",", Character.valueOf('"'), false)));
        Assert.assertNull(result.getBestValue());
    }

    /**
     * Test hard case.
     */
    @Test
    public void testHard()
    {
        ValuesWithConfidence<Character> result = new CommentDetector().detect(new ListLineSampler(CsvTestUtils.createHardData()));
        Assert.assertEquals(Character.valueOf('='), result.getBestValue());
    }

    /**
     * Test basic case with hash delimiter and empty line.
     */
    @Test
    public void testHashDelimiter()
    {
        List<String> data = CsvTestUtils.createBasicDelimitedData("#");
        data.add(StringUtilities.join("#", new String[] { "", "", "", "", "" }));
        ValuesWithConfidence<Character> result = new CommentDetector(Character.valueOf('#'), null)
                .detect(new ListLineSampler(data));
        Assert.assertNull(result.getBestValue());
    }
}
