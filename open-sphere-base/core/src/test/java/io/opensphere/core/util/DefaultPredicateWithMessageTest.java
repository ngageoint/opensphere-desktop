package io.opensphere.core.util;

import java.util.function.Predicate;

import org.junit.Test;

import org.junit.Assert;

/** Test for {@link DefaultPredicateWithMessage}. */
public class DefaultPredicateWithMessageTest
{
    /** Test for {@link DefaultPredicateWithMessage}. */
    @Test
    public void test()
    {
        String trueMessage = "The test was true.";
        String falseMessage = "The test was false.";

        DefaultPredicateWithMessage<Void> predicate;

        // Test true.
        predicate = new DefaultPredicateWithMessage<Void>(new Predicate<Void>()
        {
            @Override
            public boolean test(Void input)
            {
                return true;
            }
        }, trueMessage, falseMessage);
        Assert.assertTrue(predicate.test(null));
        Assert.assertEquals(trueMessage, predicate.getMessage());

        // Test false.
        predicate = new DefaultPredicateWithMessage<Void>(new Predicate<Void>()
        {
            @Override
            public boolean test(Void input)
            {
                return false;
            }
        }, trueMessage, falseMessage);
        Assert.assertFalse(predicate.test(null));
        Assert.assertEquals(falseMessage, predicate.getMessage());
    }
}
