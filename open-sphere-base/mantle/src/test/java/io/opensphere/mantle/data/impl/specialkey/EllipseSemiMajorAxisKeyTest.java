package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;

/** Tests for {@link EllipseSemiMajorAxisKey}. */
public class EllipseSemiMajorAxisKeyTest
{
    /** Test for {@link EllipseSemiMajorAxisKey#detectUnit(String)}. */
    @Test
    public void testDetectUnit()
    {
        Assert.assertEquals(Kilometers.class, EllipseSemiMajorAxisKey.detectUnit("blah (km)"));
        Assert.assertEquals(Kilometers.class, EllipseSemiMajorAxisKey.detectUnit("blah (kilometers)"));
        Assert.assertEquals(Meters.class, EllipseSemiMajorAxisKey.detectUnit("blah (m)"));
        Assert.assertEquals(Meters.class, EllipseSemiMajorAxisKey.detectUnit("blah (meters)"));
        Assert.assertEquals(StatuteMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (mi)"));
        Assert.assertEquals(StatuteMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (miles)"));
        Assert.assertEquals(NauticalMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (nm)"));
        Assert.assertEquals(NauticalMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (nmi)"));
        Assert.assertEquals(NauticalMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (nautical miles)"));
    }
}
