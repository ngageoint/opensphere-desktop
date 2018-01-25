package io.opensphere.core.util.predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link ValidURLPredicate}. */
public class ValidURLPredicateTest
{
    /** Test {@link ValidURLPredicate}. */
    @Test
    public void test()
    {
        ValidURLPredicate predicate = new ValidURLPredicate();

        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertFalse(predicate.test(" "));
        Assert.assertFalse(predicate.test("a"));
        Assert.assertFalse(predicate.test("http://"));
        Assert.assertFalse(predicate.test("http://?"));
        Assert.assertFalse(predicate.test("invalid.com"));
        Assert.assertTrue(predicate.test("http://good.com"));
        Assert.assertTrue(predicate.test("http://good.com?"));
        Assert.assertTrue(predicate.test("http://good.com?param=true"));
    }
}
