package io.opensphere.core.util.function;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link ToLowerCaseFunction}. */
public class ToLowerCaseFunctionTest
{
    /** Test {@link ToLowerCaseFunction}. */
    @Test
    public void test()
    {
        Assert.assertEquals("one", new ToLowerCaseFunction().apply("one"));
        Assert.assertEquals("one", new ToLowerCaseFunction().apply("One"));
        Assert.assertEquals("one", new ToLowerCaseFunction().apply("ONE"));
        Assert.assertEquals("", new ToLowerCaseFunction().apply(""));
        Assert.assertNull(new ToLowerCaseFunction().apply(null));
    }
}
