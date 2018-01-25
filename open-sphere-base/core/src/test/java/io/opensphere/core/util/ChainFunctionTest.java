package io.opensphere.core.util;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link ChainFunction}. */
public class ChainFunctionTest
{
    /** Test for {@link ChainFunction}. */
    @Test
    public void test()
    {
        Function<? super Integer, ? extends Integer> plusFive = new Function<Integer, Integer>()
        {
            @Override
            public Integer apply(Integer t)
            {
                return Integer.valueOf(t.intValue() + 5);
            }
        };
        Function<? super Integer, ? extends Integer> timesThree = new Function<Integer, Integer>()
        {
            @Override
            public Integer apply(Integer t)
            {
                return Integer.valueOf(t.intValue() * 3);
            }
        };

        Assert.assertEquals(Integer.valueOf(36), new ChainFunction<Integer>(plusFive, timesThree).apply(Integer.valueOf(7)));
        Assert.assertEquals(Integer.valueOf(26), new ChainFunction<Integer>(timesThree, plusFive).apply(Integer.valueOf(7)));
        Assert.assertEquals(Integer.valueOf(31),
                new ChainFunction<Integer>(
                        Arrays.<Function<? super Integer, ? extends Integer>>asList(timesThree, plusFive, plusFive))
                                .apply(Integer.valueOf(7)));
    }
}
