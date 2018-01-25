package io.opensphere.core.util.lang;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link BooleanUtilities}. */
public class BooleanUtilitiesTest
{
    /** Test {@link BooleanUtilities}. */
    @Test
    public void test()
    {
        Assert.assertNull(BooleanUtilities.fromByte((byte)0));
        Assert.assertEquals(Boolean.FALSE, BooleanUtilities.fromByte((byte)1));
        Assert.assertEquals(Boolean.TRUE, BooleanUtilities.fromByte((byte)2));
        Assert.assertEquals(Boolean.TRUE, BooleanUtilities.fromByte((byte)-1));

        Assert.assertEquals(0, BooleanUtilities.toByte(null));
        Assert.assertEquals(1, BooleanUtilities.toByte(Boolean.FALSE));
        Assert.assertEquals(2, BooleanUtilities.toByte(Boolean.TRUE));

        Assert.assertEquals(null, BooleanUtilities.fromByte(BooleanUtilities.toByte(null)));
        Assert.assertEquals(Boolean.FALSE, BooleanUtilities.fromByte(BooleanUtilities.toByte(Boolean.FALSE)));
        Assert.assertEquals(Boolean.TRUE, BooleanUtilities.fromByte(BooleanUtilities.toByte(Boolean.TRUE)));
    }
}
