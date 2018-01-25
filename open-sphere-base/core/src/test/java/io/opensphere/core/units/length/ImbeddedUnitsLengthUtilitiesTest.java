package io.opensphere.core.units.length;

import org.junit.Test;

import org.junit.Assert;

/** Test for {@link ImbeddedUnitsLengthUtilities}. */
public class ImbeddedUnitsLengthUtilitiesTest
{
    /** Test for {@link ImbeddedUnitsLengthUtilities#getLength(String)}. */
    @Test
    public void testLength()
    {
        // Test to make sure that we correctly recognize and parse floating
        // point values.
        Length len = ImbeddedUnitsLengthUtilities.getLength("777.");
        Assert.assertEquals(len, new Meters(777.));

        len = ImbeddedUnitsLengthUtilities.getLength("777");
        Assert.assertEquals(len, new Meters(777.));

        len = ImbeddedUnitsLengthUtilities.getLength("777.333");
        Assert.assertEquals(len, new Meters(777.333));

        len = ImbeddedUnitsLengthUtilities.getLength("0.333");
        Assert.assertEquals(len, new Meters(.333));

        len = ImbeddedUnitsLengthUtilities.getLength(".333");
        Assert.assertEquals(len, new Meters(.333));

        len = ImbeddedUnitsLengthUtilities.getLength("-0.333");
        Assert.assertEquals(len, new Meters(-0.333));

        len = ImbeddedUnitsLengthUtilities.getLength("-.333");
        Assert.assertEquals(len, new Meters(-0.333));

        len = ImbeddedUnitsLengthUtilities.getLength("7.773e3");
        Assert.assertEquals(len, new Meters(7773.));

        len = ImbeddedUnitsLengthUtilities.getLength("+7.773e3");
        Assert.assertEquals(len, new Meters(7773.));

        len = ImbeddedUnitsLengthUtilities.getLength("777.3e3");
        Assert.assertEquals(len, new Meters(777300.));

        len = ImbeddedUnitsLengthUtilities.getLength("777.333.");
        Assert.assertEquals(len, new Meters(777.333));

        // Test parsing with imbedded units.
        len = ImbeddedUnitsLengthUtilities.getLength("7000 M");
        Assert.assertEquals(len, new Meters(7000.));

        len = ImbeddedUnitsLengthUtilities.getLength("77.33 KM");
        Assert.assertEquals(len, new Kilometers(77.33));

        len = ImbeddedUnitsLengthUtilities.getLength("77.ft");
        Assert.assertEquals(len, new Feet(77.));

        len = ImbeddedUnitsLengthUtilities.getLength("7.33e5mi");
        Assert.assertEquals(len, new StatuteMiles(733000));
    }
}
