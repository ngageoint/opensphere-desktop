package io.opensphere.core.units.length;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link Meters}.
 */
public class MetersTest
{
    /**
     * Test {@link Meters#clone()}.
     */
    @Test
    public void testClone()
    {
        Length dist = new Meters(1852.);
        Assert.assertEquals(dist.getMagnitude(), dist.clone().getMagnitude(), 0.);
    }

    /**
     * Test {@link Length#compareTo(Length)}.
     */
    @Test
    public void testCompareTo()
    {
        Length dist = new Meters(1852.);
        Assert.assertEquals(0, dist.compareTo(new Feet(6076.1154855643044619422572178515)));
        Assert.assertEquals(0, dist.compareTo(new Inches(72913.385826771653543307086614219)));
        Assert.assertEquals(0, dist.compareTo(new NauticalMiles(1.)));
        Assert.assertEquals(0, dist.compareTo(new Meters(1852.)));
        Assert.assertEquals(0, dist.compareTo(new Kilometers(1.852)));
        Assert.assertEquals(0, dist.compareTo(new StatuteMiles(1.1507794480235425117314881094416)));
        Assert.assertEquals(0, dist.compareTo(new Yards(2025.3718285214348206474190726172)));
        Assert.assertTrue(dist.compareTo(new NauticalMiles(1.1)) < 0);
        Assert.assertTrue(dist.compareTo(new NauticalMiles(.9)) > 0);
    }

    /**
     * Test {@link #equals(Object)}.
     */
    public void testEquals()
    {
        Assert.assertTrue(new Meters(1852.).equals(new Meters(1852.)));
        Assert.assertFalse(new Meters(1852.).equals(new Meters(1853.)));
        Assert.assertFalse(new Meters(1852.).equals(new StatuteMiles(1.)));
    }

    /**
     * Test {@link #hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        Length dist = new Meters(1852.);
        Assert.assertEquals(dist.hashCode(), new Feet(6076.1154855643044619422572178515).hashCode());
        Assert.assertEquals(dist.hashCode(), new Inches(72913.385826771653543307086614219).hashCode());
        Assert.assertEquals(dist.hashCode(), new NauticalMiles(1.).hashCode());
        Assert.assertEquals(dist.hashCode(), new Meters(1852.).hashCode());
        Assert.assertEquals(dist.hashCode(), new Kilometers(1.852).hashCode());
        Assert.assertEquals(dist.hashCode(), new StatuteMiles(1.1507794480235425117314881094416).hashCode());
        Assert.assertEquals(dist.hashCode(), new Yards(2025.3718285214348206474190726172).hashCode());
        Assert.assertFalse(dist.hashCode() == new Meters(1.).hashCode());
    }

    /**
     * Test the label and toString methods.
     */
    @Test
    public void testLabels()
    {
        Length dist = new Meters(1852.);
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
        Assert.assertEquals("2.0 meters", new Meters(2.).toLongLabelString());
        Assert.assertEquals("1.0 meter", new Meters(1.).toLongLabelString());
        Assert.assertEquals("0.1 meters", new Meters(.1).toLongLabelString());
    }

    /**
     * Test {@link Length#toShortLabelString()}.
     */
    @Test
    public void testToShortLabelString()
    {
        Assert.assertEquals("2.0 m", new Meters(2.).toShortLabelString());
        Assert.assertEquals("1.0 m", new Meters(1.).toShortLabelString());
        Assert.assertEquals("0.1 m", new Meters(.1).toShortLabelString());
    }

    /**
     * Test unit conversion.
     */
    @Test
    public void testUnits()
    {
        Length dist = new Meters(1852.);
        Assert.assertEquals(6076.1154855643044619422572178515, dist.inFeet(), 0.);
        Assert.assertEquals(1852., dist.inMeters(), 0.);
        Assert.assertEquals(6076.1154855643044619422572178515, new Feet(dist).getMagnitude(), 0.);
        Assert.assertEquals(72913.385826771653543307086614219, new Inches(dist).getMagnitude(), 0.);
        Assert.assertEquals(1., new NauticalMiles(dist).getMagnitude(), 0.);
        Assert.assertEquals(1852., new Meters(dist).getMagnitude(), 0.);
        Assert.assertEquals(1.852, new Kilometers(dist).getMagnitude(), 0.);
        Assert.assertEquals(1.1507794480235425117314881094416, new StatuteMiles(dist).getMagnitude(), 0.);
        Assert.assertEquals(2025.3718285214348206474190726172, new Yards(dist).getMagnitude(), 0.);
    }
}
