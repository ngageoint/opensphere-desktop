package io.opensphere.core.util.predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link IntegerRangePredicate}. */
public class IntegerRangePredicateTest
{
    /** Test {@link IntegerRangePredicate}. */
    @Test
    public void test()
    {
        IntegerRangePredicate predicate = new IntegerRangePredicate(5, 7);

        Assert.assertFalse(predicate.test(Integer.valueOf(4)));
        Assert.assertTrue(predicate.test(Integer.valueOf(5)));
        Assert.assertTrue(predicate.test(Integer.valueOf(6)));
        Assert.assertTrue(predicate.test(Integer.valueOf(7)));
        Assert.assertFalse(predicate.test(Integer.valueOf(8)));
    }
}
