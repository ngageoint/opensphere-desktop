package io.opensphere.core.util.predicate;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link AndPredicate}. */
public class AndPredicateTest
{
    /** A predicate that is always {@code true}. */
    private static final Predicate<Object> TRUE_PREDICATE = new Predicate<Object>()
    {
        @Override
        public boolean test(Object input)
        {
            return true;
        }
    };

    /** A predicate that is always {@code false}. */
    private static final Predicate<Object> FALSE_PREDICATE = new Predicate<Object>()
    {
        @Override
        public boolean test(Object input)
        {
            return false;
        }
    };

    /** Test {@link AndPredicate}. */
    @Test
    public void test()
    {
        Assert.assertTrue(new AndPredicate<Void>(Collections.<Predicate<Object>>emptyList()).test(null));

        Assert.assertTrue(new AndPredicate<Void>(Arrays.asList(TRUE_PREDICATE)).test(null));
        Assert.assertFalse(new AndPredicate<Void>(Arrays.asList(FALSE_PREDICATE)).test(null));

        Assert.assertTrue(new AndPredicate<Void>(Arrays.asList(TRUE_PREDICATE, TRUE_PREDICATE)).test(null));
        Assert.assertFalse(new AndPredicate<Void>(Arrays.asList(FALSE_PREDICATE, TRUE_PREDICATE)).test(null));
        Assert.assertFalse(new AndPredicate<Void>(Arrays.asList(TRUE_PREDICATE, FALSE_PREDICATE)).test(null));

        Assert.assertTrue(new AndPredicate<Void>(Arrays.asList(TRUE_PREDICATE, TRUE_PREDICATE, TRUE_PREDICATE)).test(null));
        Assert.assertFalse(new AndPredicate<Void>(Arrays.asList(TRUE_PREDICATE, TRUE_PREDICATE, FALSE_PREDICATE)).test(null));
        Assert.assertFalse(new AndPredicate<Void>(Arrays.asList(FALSE_PREDICATE, TRUE_PREDICATE, TRUE_PREDICATE)).test(null));
        Assert.assertFalse(new AndPredicate<Void>(Arrays.asList(TRUE_PREDICATE, FALSE_PREDICATE, TRUE_PREDICATE)).test(null));
    }
}
