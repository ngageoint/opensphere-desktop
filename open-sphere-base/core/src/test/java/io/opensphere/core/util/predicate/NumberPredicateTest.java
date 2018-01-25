package io.opensphere.core.util.predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link NumberPredicate}. */
public class NumberPredicateTest
{
    /** Test {@link NumberPredicate}. */
    @Test
    public void test()
    {
        NumberPredicate predicate = new NumberPredicate();

        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertFalse(predicate.test(" "));
        Assert.assertFalse(predicate.test("a"));
        Assert.assertFalse(predicate.test("5d"));
        Assert.assertFalse(predicate.test("5f"));
        Assert.assertTrue(predicate.test("0"));
        Assert.assertTrue(predicate.test("01"));
        Assert.assertTrue(predicate.test("01."));
        Assert.assertTrue(predicate.test("01.0"));
        Assert.assertTrue(predicate.test("-4e3"));
    }
}
