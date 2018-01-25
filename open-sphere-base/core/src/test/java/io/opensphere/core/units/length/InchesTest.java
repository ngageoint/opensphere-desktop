package io.opensphere.core.units.length;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link Inches}.
 */
public class InchesTest
{
    /**
     * Test {@link Inches#clone()}.
     */
    @Test
    public void testClone()
    {
        Length dist = new Inches(63360.);
        Assert.assertEquals(dist.getMagnitude(), dist.clone().getMagnitude(), 0.);
    }

    /**
     * Test {@link Length#compareTo(Length)}.
     */
    @Test
    public void testCompareTo()
    {
        Length dist = new Inches(63360.);
        Assert.assertEquals(0, dist.compareTo(new Feet(5280.)));
        Assert.assertEquals(0, dist.compareTo(new Inches(63360.)));
        Assert.assertEquals(0, dist.compareTo(new NauticalMiles(0.86897624190064794816414686825054)));
        Assert.assertEquals(0, dist.compareTo(new Meters(1609.344)));
        Assert.assertEquals(0, dist.compareTo(new Kilometers(1.609344)));
        Assert.assertEquals(0, dist.compareTo(new StatuteMiles(1.)));
        Assert.assertEquals(0, dist.compareTo(new Yards(1760.)));
        Assert.assertTrue(dist.compareTo(new Feet(5281)) < 0);
        Assert.assertTrue(dist.compareTo(new Feet(5279)) > 0);
    }

    /**
     * Test {@link #equals(Object)}.
     */
    public void testEquals()
    {
        Assert.assertTrue(new Inches(63360.).equals(new Inches(63360.)));
        Assert.assertFalse(new Inches(63360.).equals(new Inches(63361.)));
        Assert.assertFalse(new Inches(63360.).equals(new StatuteMiles(1.)));
    }

    /**
     * Test {@link #hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        Length dist = new Inches(63360.);
        Assert.assertEquals(dist.hashCode(), new Feet(5280.).hashCode());
        Assert.assertEquals(dist.hashCode(), new Inches(63360.).hashCode());
        Assert.assertEquals(dist.hashCode(), new NauticalMiles(0.86897624190064794816414686825054).hashCode());
        Assert.assertEquals(dist.hashCode(), new Meters(1609.344).hashCode());
        Assert.assertEquals(dist.hashCode(), new Kilometers(1.609344).hashCode());
        Assert.assertEquals(dist.hashCode(), new StatuteMiles(1.).hashCode());
        Assert.assertEquals(dist.hashCode(), new Yards(1760.).hashCode());
        Assert.assertFalse(dist.hashCode() == new Meters(1.).hashCode());
    }

    /**
     * Test the label and toString methods.
     */
    @Test
    public void testLabels()
    {
        Length dist = new Inches(63360.);
        Assert.assertTrue(dist.getLongLabel(true).length() > 0);
        Assert.assertTrue(dist.getLongLabel(false).length() > 0);
        Assert.assertTrue(dist.getShortLabel(true).length() > 0);
        Assert.assertTrue(dist.getShortLabel(false).length() > 0);
        Assert.assertTrue(dist.toString().length() > 0);
    }

    /**
     * Test {@link Length#toLongLabelString()}.
     */
    @Test
    public void testToLongLabelString()
    {
        Assert.assertEquals("2.0 inches", new Inches(2.).toLongLabelString());
        Assert.assertEquals("1.0 inch", new Inches(1.).toLongLabelString());
        Assert.assertEquals("0.1 inches", new Inches(.1).toLongLabelString());
    }

    /**
     * Test {@link Length#toShortLabelString()}.
     */
    @Test
    public void testToShortLabelString()
    {
        Assert.assertEquals("2.0 in", new Inches(2.).toShortLabelString());
        Assert.assertEquals("1.0 in", new Inches(1.).toShortLabelString());
        Assert.assertEquals("0.1 in", new Inches(.1).toShortLabelString());
    }

    /**
     * Test unit conversion.
     */
    @Test
    public void testUnits()
    {
        Length dist = new Inches(63360.);
        Assert.assertEquals(5280., dist.inFeet(), 0.);
        Assert.assertEquals(1609.344, dist.inMeters(), 0.);
        Assert.assertEquals(5280., new Feet(dist).getMagnitude(), 0.);
        Assert.assertEquals(63360., new Inches(dist).getMagnitude(), 0.);
        Assert.assertEquals(0.86897624190064794816414686825054, new NauticalMiles(dist).getMagnitude(), 0.);
        Assert.assertEquals(1609.344, new Meters(dist).getMagnitude(), 0.);
        Assert.assertEquals(1.609344, new Kilometers(dist).getMagnitude(), 0.);
        Assert.assertEquals(1., new StatuteMiles(dist).getMagnitude(), 0.);
        Assert.assertEquals(1760., new Yards(dist).getMagnitude(), 0.);
    }
}
