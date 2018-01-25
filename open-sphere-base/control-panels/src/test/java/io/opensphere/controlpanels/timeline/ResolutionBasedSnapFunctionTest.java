package io.opensphere.controlpanels.timeline;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.Constants;

/** Test {@link ResolutionBasedSnapFunction}. */
public class ResolutionBasedSnapFunctionTest
{
    /** Test {@link ResolutionBasedSnapFunction}. */
    @Test
    public void testGetModulus()
    {
        // @formatter:off
        Assert.assertEquals(01 * Constants.MILLIS_PER_DAY,  ResolutionBasedSnapFunction.getModulus(02 * Constants.MILLIS_PER_HOUR + 1));
        Assert.assertEquals(06 * Constants.MILLIS_PER_HOUR, ResolutionBasedSnapFunction.getModulus(02 * Constants.MILLIS_PER_HOUR));
        Assert.assertEquals(06 * Constants.MILLIS_PER_HOUR, ResolutionBasedSnapFunction.getModulus(30 * Constants.MILLIS_PER_MINUTE + 1));
        Assert.assertEquals(03 * Constants.MILLIS_PER_HOUR, ResolutionBasedSnapFunction.getModulus(30 * Constants.MILLIS_PER_MINUTE));
        Assert.assertEquals(03 * Constants.MILLIS_PER_HOUR, ResolutionBasedSnapFunction.getModulus(15 * Constants.MILLIS_PER_MINUTE + 1));
        Assert.assertEquals(01 * Constants.MILLIS_PER_HOUR, ResolutionBasedSnapFunction.getModulus(15 * Constants.MILLIS_PER_MINUTE));
        Assert.assertEquals(01 * Constants.MILLIS_PER_HOUR, ResolutionBasedSnapFunction.getModulus(05 * Constants.MILLIS_PER_MINUTE + 1));
        Assert.assertEquals(30 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(05 * Constants.MILLIS_PER_MINUTE));
        Assert.assertEquals(30 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(2.5 * Constants.MILLIS_PER_MINUTE + 1));
        Assert.assertEquals(15 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(2.5 * Constants.MILLIS_PER_MINUTE));
        Assert.assertEquals(15 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(75 * Constants.MILLI_PER_UNIT + 1));
        Assert.assertEquals(05 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(75 * Constants.MILLI_PER_UNIT));
        Assert.assertEquals(05 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(25 * Constants.MILLI_PER_UNIT + 1));
        Assert.assertEquals(01 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(25 * Constants.MILLI_PER_UNIT));
        Assert.assertEquals(01 * Constants.MILLIS_PER_MINUTE, ResolutionBasedSnapFunction.getModulus(05 * Constants.MILLI_PER_UNIT + 1));
        Assert.assertEquals(30 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(5 * Constants.MILLI_PER_UNIT));
        Assert.assertEquals(30 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(2501));
        Assert.assertEquals(15 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(2500));
        Assert.assertEquals(15 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(1251));
        Assert.assertEquals(05 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(1250));
        Assert.assertEquals(05 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(417));
        Assert.assertEquals(01 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(416));
        Assert.assertEquals(01 * Constants.MILLI_PER_UNIT, ResolutionBasedSnapFunction.getModulus(84));
        Assert.assertEquals(500, ResolutionBasedSnapFunction.getModulus(83));
        Assert.assertEquals(500, ResolutionBasedSnapFunction.getModulus(42));
        Assert.assertEquals(100, ResolutionBasedSnapFunction.getModulus(41));
        Assert.assertEquals(100, ResolutionBasedSnapFunction.getModulus(9));
        Assert.assertEquals(50, ResolutionBasedSnapFunction.getModulus(8));
        Assert.assertEquals(50, ResolutionBasedSnapFunction.getModulus(5));
        Assert.assertEquals(10, ResolutionBasedSnapFunction.getModulus(4));
        Assert.assertEquals(10, ResolutionBasedSnapFunction.getModulus(1));
        Assert.assertEquals(1, ResolutionBasedSnapFunction.getModulus(.833));
        // @formatter:on
    }
}
