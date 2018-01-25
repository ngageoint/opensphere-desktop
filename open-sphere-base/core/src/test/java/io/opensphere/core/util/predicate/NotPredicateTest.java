package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link NotPredicate}. */
public class NotPredicateTest
{
    /** Test {@link NotPredicate}. */
    @Test
    public void test()
    {
        Predicate<Void> truePredicate = new Predicate<Void>()
        {
            @Override
            public boolean test(Void input)
            {
                return true;
            }
        };
        Assert.assertFalse(new NotPredicate<>(truePredicate).test(null));

        Predicate<Void> falsePredicate = new Predicate<Void>()
        {
            @Override
            public boolean test(Void input)
            {
                return false;
            }
        };
        Assert.assertTrue(new NotPredicate<>(falsePredicate).test(null));
    }
}
