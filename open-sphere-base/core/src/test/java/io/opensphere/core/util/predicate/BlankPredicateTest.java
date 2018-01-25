package io.opensphere.core.util.predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link BlankPredicate}. */
public class BlankPredicateTest
{
    /** Test {@link BlankPredicate}. */
    @Test
    public void test()
    {
        BlankPredicate predicate = new BlankPredicate();

        Assert.assertTrue(predicate.test(null));
        Assert.assertTrue(predicate.test(""));
        Assert.assertTrue(predicate.test(" "));
        Assert.assertTrue(predicate.test(" \n\t"));
        Assert.assertFalse(predicate.test("bob"));
        Assert.assertFalse(predicate.test("  bob  "));
    }
}
