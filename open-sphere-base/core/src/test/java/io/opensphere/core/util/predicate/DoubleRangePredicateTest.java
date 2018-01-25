package io.opensphere.core.util.predicate;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link DoubleRangePredicate}.
 */
public class DoubleRangePredicateTest
{
    /** Test for {@link DoubleRangePredicate}. */
    @Test
    public void test()
    {
        DoubleRangePredicate predicate = new DoubleRangePredicate(5., 7.);

        Assert.assertFalse(predicate.test(Double.valueOf(4)));
        Assert.assertTrue(predicate.test(Double.valueOf(5)));
        Assert.assertTrue(predicate.test(Double.valueOf(6)));
        Assert.assertTrue(predicate.test(Double.valueOf(7)));
        Assert.assertFalse(predicate.test(Double.valueOf(8)));
    }
}
