package io.opensphere.csvcommon.detect.controller;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.QuotingBufferedReader;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.csvcommon.detect.controller.ReaderLineSampler;

/** Test {@link ReaderLineSampler}. */
public class ReaderLineSamplerTest
{
    /** Test {@link ReaderLineSampler}. */
    @Test
    public void test()
    {
        List<String> expectedBegin = New.list(5);
        expectedBegin.add("begin one");
        expectedBegin.add("begin two");
        expectedBegin.add("\"begin\n three\"");
        expectedBegin.add("begin four");
        expectedBegin.add("begin five");

        List<String> middle = New.list(2);
        middle.add("middle one");
        middle.add("middle two");

        List<String> expectedEnd = New.list(5);
        expectedEnd.add("end one");
        expectedEnd.add("end two");
        expectedEnd.add("end three");
        expectedEnd.add("end four");
        expectedEnd.add("end five");

        StringBuilder sb = new StringBuilder();
        for (String line : expectedBegin)
        {
            sb.append(line).append(StringUtilities.LINE_SEP);
        }

        for (String line : middle)
        {
            sb.append(line).append(StringUtilities.LINE_SEP);
        }

        for (String line : expectedEnd)
        {
            sb.append(line).append(StringUtilities.LINE_SEP);
        }

        Reader in = new CharArrayReader(sb.toString().toCharArray());

        ReaderLineSampler sampler = new ReaderLineSampler(new QuotingBufferedReader(in, new char[] { '"' }, null),
                expectedBegin.size(), expectedEnd.size());

        Assert.assertEquals(expectedBegin, sampler.getBeginningSampleLines());
        Assert.assertEquals(expectedEnd, sampler.getEndingSampleLines());
        Assert.assertEquals(expectedBegin.size() + middle.size(), sampler.getEndingSampleLinesIndexOffset());
    }
}
