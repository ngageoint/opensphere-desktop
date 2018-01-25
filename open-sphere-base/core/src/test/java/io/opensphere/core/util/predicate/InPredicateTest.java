package io.opensphere.core.util.predicate;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link InPredicate}. */
public class InPredicateTest
{
    /** Test {@link InPredicate}. */
    @Test
    public void test()
    {
        InPredicate predicate = new InPredicate(Arrays.asList("one", "two", "three"));
        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertFalse(predicate.test("One"));
        Assert.assertTrue(predicate.test("one"));
        Assert.assertTrue(predicate.test("two"));
        Assert.assertTrue(predicate.test("three"));
    }
}
