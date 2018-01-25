package io.opensphere.core.util.function;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link AppendFunction}. */
public class AppendFunctionTest
{
    /** Test for {@link AppendFunction}. */
    @Test
    public void testApply()
    {
        Assert.assertEquals("Knock knock Who's there?", new AppendFunction("Who's there?").apply("Knock knock "));
    }
}
