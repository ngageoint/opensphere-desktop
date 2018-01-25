package io.opensphere.core.util.lang;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test {@link PatternStringTokenizer}.
 */
public class PatternStringTokenizerTest
{
    /**
     * Test {@link PatternStringTokenizer#tokenize(String)} with a tokenizer
     * constructed with token widths, with a string that does not match the
     * given widths.
     */
    @Test
    public void testFixWidthDivisions()
    {
        PatternStringTokenizer tokenizer = PatternStringTokenizer.createFromDivisions(new int[] { 5, 7 });
        List<String> actual = tokenizer.tokenize("a   abbccccccc");
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("a   a", actual.get(0));
        Assert.assertEquals("bb", actual.get(1));
        Assert.assertEquals("ccccccc", actual.get(2));
    }

    /**
     * Test {@link PatternStringTokenizer#tokenize(String)} with a string that
     * matches the pattern.
     */
    @Test
    public void testMatch()
    {
        PatternStringTokenizer tokenizer = new PatternStringTokenizer("(.{5})(\\S+)\\s*(.{7}).*");
        List<String> actual = tokenizer.tokenize("a   abb    cccccccxxxx");
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("a   a", actual.get(0));
        Assert.assertEquals("bb", actual.get(1));
        Assert.assertEquals("ccccccc", actual.get(2));
    }

    /**
     * Test {@link PatternStringTokenizer#tokenize(String)} with a string that
     * doesn't match the pattern.
     */
    @Test
    public void testNoMatch()
    {
        PatternStringTokenizer tokenizer = new PatternStringTokenizer("(.{5})(\\S+)\\s*(.{7}).*");
        List<String> actual = tokenizer.tokenize("a   accccccc");
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("", actual.get(0));
        Assert.assertEquals("", actual.get(1));
        Assert.assertEquals("", actual.get(2));
    }

    /**
     * Test {@link PatternStringTokenizer#tokenize(String)} with a tokenizer
     * constructed with token widths.
     */
    @Test
    public void testWidthsMatch()
    {
        PatternStringTokenizer tokenizer = PatternStringTokenizer.createFromWidths(new int[] { 5, 2, 7 });
        List<String> actual = tokenizer.tokenize("a   abbccccccc");
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("a   a", actual.get(0));
        Assert.assertEquals("bb", actual.get(1));
        Assert.assertEquals("ccccccc", actual.get(2));
    }

    /**
     * Test {@link PatternStringTokenizer#tokenize(String)} with a tokenizer
     * constructed with token widths, with a string that does not match the
     * given widths.
     */
    @Test
    public void testWidthsNoMatch()
    {
        PatternStringTokenizer tokenizer = PatternStringTokenizer.createFromWidths(new int[] { 5, 2, 7 });
        List<String> actual = tokenizer.tokenize("a   abbccccccc ");
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("", actual.get(0));
        Assert.assertEquals("", actual.get(1));
        Assert.assertEquals("", actual.get(2));
    }
}
