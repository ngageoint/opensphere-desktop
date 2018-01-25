package io.opensphere.core.util.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.predicate.BlankPredicate;

/** Test {@link EndTrimmingTokenizer}. */
public class EndTrimmingTokenizerTest
{
    /** Test {@link EndTrimmingTokenizer}. */
    @Test
    public void test()
    {
        List<String> result = new ArrayList<>(Arrays.asList("", "one", "two", "", "three", "", ""));
        String input = "input";
        StringTokenizer tokenizer = EasyMock.createMock(StringTokenizer.class);
        EasyMock.expect(tokenizer.tokenize(input)).andReturn(result);
        EasyMock.replay(tokenizer);

        List<String> actual = new EndTrimmingTokenizer(new BlankPredicate(), tokenizer).tokenize(input);

        Assert.assertEquals(result.subList(0, 5), actual);
    }
}
