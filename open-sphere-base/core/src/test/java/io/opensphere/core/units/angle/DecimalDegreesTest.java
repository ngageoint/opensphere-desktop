package io.opensphere.core.units.angle;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link DecimalDegrees}.
 */
public class DecimalDegreesTest
{
    /**
     * Test {@link DecimalDegrees#clone()}.
     */
    @Test
    public void testClone()
    {
        Angle ang = new DecimalDegrees(34.54512);
        Assert.assertEquals(ang.getMagnitude(), ang.clone().getMagnitude(), 0.);
    }

    /**
     * Test {@link Angle#compareTo(Angle)}.
     */
    @Test
    public void testCompareTo()
    {
        Angle ang = new DecimalDegrees(34.54512);
        Assert.assertEquals(0, ang.compareTo(new DecimalDegrees(34.54512)));
        Assert.assertTrue(ang.compareTo(new DecimalDegrees(34.54513)) < 0);
        Assert.assertTrue(ang.compareTo(new DecimalDegrees(34.54511)) > 0);
    }

    /**
     * Test {@link #equals(Object)}.
     */
    public void testEquals()
    {
        Assert.assertTrue(new DecimalDegrees(34.54512).equals(new DecimalDegrees(34.54512)));
        Assert.assertFalse(new DecimalDegrees(34.54512).equals(new DecimalDegrees(34.545125)));
    }

    /**
     * Test {@link #hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        Angle ang = new DecimalDegrees(34.54512);
        Assert.assertEquals(ang.hashCode(), new DecimalDegrees(34.54512).hashCode());
        Assert.assertFalse(ang.hashCode() == new DecimalDegrees(34.545125).hashCode());
    }

    /**
     * Test the label and toString methods.
     */
    @Test
    public void testLabels()
    {
        Angle ang = new DecimalDegrees(34.54512);
        Assert.assertTrue(ang.getLongLabel().length() > 0);
        Assert.assertTrue(ang.getShortLabel().length() > 0);
        Assert.assertTrue(ang.toString().length() > 0);
    }

    /**
     * Test {@link Angle#toShortLabelString()}.
     */
    @Test
    public void testToShortLabelString()
    {
        Assert.assertEquals(" 34.545120\u00B0", new DecimalDegrees(34.54512).toShortLabelString());
        Assert.assertEquals(" 134.545120\u00B0", new DecimalDegrees(134.54512).toShortLabelString());
        Assert.assertEquals(" 134.545125\u00B0", new DecimalDegrees(134.545125).toShortLabelString());
        Assert.assertEquals(" 134.545126\u00B0", new DecimalDegrees(134.5451255).toShortLabelString());
        Assert.assertEquals("-34.545120\u00B0", new DecimalDegrees(-34.54512).toShortLabelString());
        Assert.assertEquals("-134.545120\u00B0", new DecimalDegrees(-134.54512).toShortLabelString());
        Assert.assertEquals(" -0.000000\u00B0", new DecimalDegrees(-0.0000004).toShortLabelString());
    }

    /**
     * Test {@link Angle#toShortLabelString(char, char)}.
     */
    @Test
    public void testToShortLabelStringCharChar()
    {
        Assert.assertEquals(" 34.545120\u00B0N", new DecimalDegrees(34.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134.545120\u00B0N", new DecimalDegrees(134.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134.545125\u00B0N", new DecimalDegrees(134.545125).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134.545126\u00B0N", new DecimalDegrees(134.5451255).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 34.545120\u00B0S", new DecimalDegrees(-34.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134.545120\u00B0S", new DecimalDegrees(-134.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 0.000000\u00B0S", new DecimalDegrees(-0.).toShortLabelString('N', 'S'));
    }
}
