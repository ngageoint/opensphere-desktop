package io.opensphere.core.util.predicate;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link EqualsIgnoreCasePredicate}. */
public class EqualsIgnoreCasePredicateTest
{
    /** Test {@link EqualsIgnoreCasePredicate#test(String)}. */
    @Test
    public void test()
    {
        EqualsIgnoreCasePredicate predicate = new EqualsIgnoreCasePredicate(Arrays.asList("ONE", "TWO", "THREE"));
        Assert.assertFalse(predicate.test(null));
        Assert.assertFalse(predicate.test(""));
        Assert.assertTrue(predicate.test("One"));
        Assert.assertTrue(predicate.test("tWo"));
        Assert.assertTrue(predicate.test("thRee"));
        Assert.assertFalse(predicate.test("aOne"));
        Assert.assertFalse(predicate.test("atWo"));
        Assert.assertFalse(predicate.test("athRee"));
        Assert.assertFalse(predicate.test("Onea"));
        Assert.assertFalse(predicate.test("tWoa"));
        Assert.assertFalse(predicate.test("thReea"));
    }
}
