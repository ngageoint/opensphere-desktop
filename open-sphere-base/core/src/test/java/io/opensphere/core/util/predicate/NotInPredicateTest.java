package io.opensphere.core.util.predicate;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link NotInPredicate}. */
public class NotInPredicateTest
{
    /** Test {@link NotInPredicate}. */
    @Test
    public void test()
    {
        NotInPredicate predicate = new NotInPredicate(Arrays.asList("one", "two", "three"));
        Assert.assertTrue(predicate.test(null));
        Assert.assertTrue(predicate.test(""));
        Assert.assertTrue(predicate.test("One"));
        Assert.assertFalse(predicate.test("one"));
        Assert.assertFalse(predicate.test("two"));
        Assert.assertFalse(predicate.test("three"));
    }
}
