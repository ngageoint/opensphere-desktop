package io.opensphere.core.util.predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link NonEmptyPredicate}. */
public class NonEmptyPredicateTest
{
    /** Test {@link NonEmptyPredicate}. */
    @Test
    public void test()
    {
        NonEmptyPredicate predicate = new NonEmptyPredicate();

        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertTrue(predicate.test(" "));
        Assert.assertTrue(predicate.test("bob"));
        Assert.assertTrue(predicate.test("  bob  "));
    }
}
