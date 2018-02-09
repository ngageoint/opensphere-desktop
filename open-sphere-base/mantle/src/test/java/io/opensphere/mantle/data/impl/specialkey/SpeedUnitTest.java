package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link SpeedUnit}. */
public class SpeedUnitTest
{
    /** Test for {@link SpeedUnit#detectUnit(String)}. */
    @Test
    public void test()
    {
        Assert.assertEquals(null, SpeedUnit.detectUnit("blah"));
        Assert.assertEquals(null, SpeedUnit.detectUnit("(blah/blah)"));
        Assert.assertEquals(SpeedUnit.METERS_PER_SECOND, SpeedUnit.detectUnit(" (meters/sec)"));
        Assert.assertEquals(SpeedUnit.METERS_PER_SECOND, SpeedUnit.detectUnit("(METERS/SEC)"));
        Assert.assertEquals(SpeedUnit.METERS_PER_SECOND, SpeedUnit.detectUnit("(meters/second)"));
        Assert.assertEquals(SpeedUnit.METERS_PER_SECOND, SpeedUnit.detectUnit("(m/s)"));
        Assert.assertEquals(SpeedUnit.METERS_PER_SECOND, SpeedUnit.detectUnit(" ( m / s ) "));
        Assert.assertEquals(SpeedUnit.METERS_PER_SECOND, SpeedUnit.detectUnit("r (+/-m/s)"));
        Assert.assertEquals(SpeedUnit.KILOMETERS_PER_HOUR, SpeedUnit.detectUnit("(kilometers/hour)"));
        Assert.assertEquals(SpeedUnit.KILOMETERS_PER_HOUR, SpeedUnit.detectUnit("(km/h)"));
        Assert.assertEquals(SpeedUnit.KILOMETERS_PER_HOUR, SpeedUnit.detectUnit("(km/hr)"));
        Assert.assertEquals(SpeedUnit.KILOMETERS_PER_HOUR, SpeedUnit.detectUnit("(kph)"));
        Assert.assertEquals(null, SpeedUnit.detectUnit("(m/hr)"));
        Assert.assertEquals(SpeedUnit.MILES_PER_HOUR, SpeedUnit.detectUnit("(miles/hour)"));
        Assert.assertEquals(SpeedUnit.MILES_PER_HOUR, SpeedUnit.detectUnit("(mi/h)"));
        Assert.assertEquals(SpeedUnit.MILES_PER_HOUR, SpeedUnit.detectUnit("(mph)"));
        Assert.assertEquals(SpeedUnit.KNOTS, SpeedUnit.detectUnit("(knots)"));
        Assert.assertEquals(SpeedUnit.KNOTS, SpeedUnit.detectUnit("(kn)"));
    }
}
