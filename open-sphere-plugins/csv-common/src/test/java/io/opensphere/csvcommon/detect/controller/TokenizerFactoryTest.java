package io.opensphere.csvcommon.detect.controller;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.csvcommon.config.v2.CSVColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVDelimitedColumnFormat;
import io.opensphere.csvcommon.config.v2.CSVFixedWidthColumnFormat;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.columnformat.FixedWidthColumnFormatParameters;
import io.opensphere.csvcommon.detect.controller.TokenizerFactoryImpl;

/** Test for {@link TokenizerFactoryImpl}. */
public class TokenizerFactoryTest
{
    /**
     * Test for
     * {@link TokenizerFactoryImpl#getTokenizer(io.opensphere.csv.config.v2.CSVColumnFormat, io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters)}
     * with a delimited column format.
     */
    @Test
    public void testGetTokenizerDelimited()
    {
        Assert.assertNull(new TokenizerFactoryImpl().getTokenizer(null, null));

        CSVColumnFormat truth;
        ColumnFormatParameters detected;
        StringTokenizer tokenizer;

        truth = new CSVDelimitedColumnFormat(",", "\"", 3);
        detected = new DelimitedColumnFormatParameters(Character.valueOf('"'), null, 1);

        // No detected.
        tokenizer = new TokenizerFactoryImpl().getTokenizer(truth, null);
        Assert.assertNotNull(tokenizer);
        Assert.assertEquals(Arrays.asList("aaa", "b", "cc,cc"), tokenizer.tokenize("aaa,b,\"cc,cc\""));

        // Truth and detected.
        tokenizer = new TokenizerFactoryImpl().getTokenizer(truth, detected);
        Assert.assertNotNull(tokenizer);
        Assert.assertEquals(Arrays.asList("aaa", "b", "cc,cc"), tokenizer.tokenize("aaa,b,\"cc,cc\""));

        // No truth.
        tokenizer = new TokenizerFactoryImpl().getTokenizer(null, detected);
        Assert.assertNotNull(tokenizer);
        Assert.assertEquals(Arrays.asList("aaa,b,", "cc,cc", ""), tokenizer.tokenize("aaa,b,\"cc,cc\""));
    }

    /**
     * Test for
     * {@link TokenizerFactoryImpl#getTokenizer(io.opensphere.csv.config.v2.CSVColumnFormat, io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters)}
     * with a fixed column format.
     */
    @Test
    public void testGetTokenizerFixed()
    {
        Assert.assertNull(new TokenizerFactoryImpl().getTokenizer(null, null));

        CSVColumnFormat truth;
        ColumnFormatParameters detected;
        StringTokenizer tokenizer;

        truth = new CSVFixedWidthColumnFormat(new int[] { 3, 4 });
        detected = new FixedWidthColumnFormatParameters(new int[] { 1, 3 });

        // No detected.
        tokenizer = new TokenizerFactoryImpl().getTokenizer(truth, null);
        Assert.assertNotNull(tokenizer);
        Assert.assertEquals(Arrays.asList("aaa", "b", "cc"), tokenizer.tokenize("aaabcc"));

        // Truth and detected.
        tokenizer = new TokenizerFactoryImpl().getTokenizer(truth, detected);
        Assert.assertNotNull(tokenizer);
        Assert.assertEquals(Arrays.asList("aaa", "b", "cc"), tokenizer.tokenize("aaabcc"));

        // No truth.
        tokenizer = new TokenizerFactoryImpl().getTokenizer(null, detected);
        Assert.assertNotNull(tokenizer);
        Assert.assertEquals(Arrays.asList("a", "aa", "bcc"), tokenizer.tokenize("aaabcc"));
    }
}
