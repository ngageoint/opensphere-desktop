package io.opensphere.core.util;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.function.ConstantFunction;

/** Test {@link NoEffectPredicate}. */
public class NoEffectPredicateTest
{
    /** Test {@link NoEffectPredicate}. */
    @Test
    public void test()
    {
        Assert.assertTrue(
                new NoEffectPredicate<Boolean>(new ConstantFunction<Boolean, Boolean>(Boolean.TRUE)).test(Boolean.TRUE));
        Assert.assertFalse(
                new NoEffectPredicate<Boolean>(new ConstantFunction<Boolean, Boolean>(Boolean.TRUE)).test(Boolean.FALSE));
        Assert.assertFalse(new NoEffectPredicate<Boolean>(new ConstantFunction<Boolean, Boolean>(Boolean.TRUE)).test(null));
        Assert.assertFalse(new NoEffectPredicate<Boolean>(new ConstantFunction<Boolean, Boolean>(null)).test(Boolean.TRUE));
    }
}
