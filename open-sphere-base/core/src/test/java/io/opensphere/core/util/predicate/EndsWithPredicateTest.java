package io.opensphere.core.util.predicate;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link EndsWithPredicate}. */
public class EndsWithPredicateTest
{
    /** Test {@link InPredicate#test(Object)}. */
    @Test
    public void test()
    {
        EndsWithPredicate predicate = new EndsWithPredicate(Arrays.asList("one", "two", "three"), false);
        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertFalse(predicate.test("One"));
        Assert.assertTrue(predicate.test("one"));
        Assert.assertTrue(predicate.test("two"));
        Assert.assertTrue(predicate.test("three"));
        Assert.assertTrue(predicate.test("aone"));
        Assert.assertTrue(predicate.test("atwo"));
        Assert.assertTrue(predicate.test("athree"));
        Assert.assertFalse(predicate.test("onea"));
        Assert.assertFalse(predicate.test("twoa"));
        Assert.assertFalse(predicate.test("threea"));
    }

    /** Test {@link InPredicate#test(Object)}. */
    @Test
    public void testIgnoreCase()
    {
        EndsWithPredicate predicate = new EndsWithPredicate(Arrays.asList("ONE", "TWO", "THREE"), true);
        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertTrue(predicate.test("One"));
        Assert.assertTrue(predicate.test("tWo"));
        Assert.assertTrue(predicate.test("thRee"));
        Assert.assertTrue(predicate.test("aOne"));
        Assert.assertTrue(predicate.test("atWo"));
        Assert.assertTrue(predicate.test("athRee"));
        Assert.assertFalse(predicate.test("Onea"));
        Assert.assertFalse(predicate.test("tWoa"));
        Assert.assertFalse(predicate.test("thReea"));
    }
}
