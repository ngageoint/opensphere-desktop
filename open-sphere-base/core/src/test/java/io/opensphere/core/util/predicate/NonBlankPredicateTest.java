package io.opensphere.core.util.predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link NonBlankPredicate}. */
public class NonBlankPredicateTest
{
    /** Test {@link NonBlankPredicate}. */
    @Test
    public void test()
    {
        NonBlankPredicate predicate = new NonBlankPredicate();

        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertFalse(predicate.test(" "));
        Assert.assertFalse(predicate.test(" \n\t"));
        Assert.assertTrue(predicate.test("bob"));
        Assert.assertTrue(predicate.test("  bob  "));
    }
}
