package io.opensphere.core.util;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link ColorUtilities}. */
public class ColorUtilitiesTest
{
    /** Test for {@link ColorUtilities#blendColors(Color, Color)}. */
    @Test
    public void testBlendColors()
    {
        Assert.assertEquals(new Color(51, 0, 204), ColorUtilities.blendColors(new Color(255, 0, 0, 51), Color.BLUE));
    }

    /**
     * Test for {@link ColorUtilities#brighten(Color, double)}.
     */
    @Test
    public void testBrighten()
    {
        // Result with a factor of .7 should be the same as Color.
        Color color1 = new Color(40, 40, 40);
        Assert.assertEquals(color1.brighter(), ColorUtilities.brighten(color1, .7));

        // Opacity should be retained.
        Color color2 = new Color(40, 40, 40, 10);
        Assert.assertEquals(ColorUtilities.opacitizeColor(color2.brighter(), 10), ColorUtilities.brighten(color2, .7));
    }

    /**
     * Test for {@link ColorUtilities#convertFromColorString(String)}.
     */
    @Test
    public void testConvertFromColorString()
    {
        Assert.assertEquals(Color.RED, ColorUtilities.convertFromColorString("red"));
        Assert.assertEquals(Color.RED, ColorUtilities.convertFromColorString("RED"));
        Assert.assertEquals(new Color(250, 0, 0, 255), ColorUtilities.convertFromColorString("250-0-0-255"));
        Assert.assertEquals(new Color(250, 0, 0, 254), ColorUtilities.convertFromColorString("250-0-0-254"));
        Assert.assertEquals(new Color(0, 250, 0, 255), ColorUtilities.convertFromColorString("0-250-0-255"));
        Assert.assertEquals(new Color(0, 250, 0, 254), ColorUtilities.convertFromColorString("0-250-0-254"));
        Assert.assertEquals(new Color(0, 0, 250, 255), ColorUtilities.convertFromColorString("0-0-250-255"));
        Assert.assertEquals(new Color(0, 0, 250, 254), ColorUtilities.convertFromColorString("0-0-250-254"));
        Assert.assertNull(ColorUtilities.convertFromColorString(""));
        Assert.assertNull(ColorUtilities.convertFromColorString("255"));
    }

    /**
     * Test for
     * {@link ColorUtilities#convertFromHexString(String, int, int, int, int)}.
     */
    @Test
    public void testConvertFromHexString()
    {
        Assert.assertEquals(new Color(0x12, 0x12, 0x12, 0x12), ColorUtilities.convertFromHexString("12345678", 0, 0, 0, 0));
        Assert.assertEquals(new Color(0x12, 0x34, 0x56, 0x78), ColorUtilities.convertFromHexString("12345678", 0, 1, 2, 3));
        Assert.assertEquals(new Color(0x78, 0x56, 0x34, 0x12), ColorUtilities.convertFromHexString("12345678", 3, 2, 1, 0));
        Assert.assertEquals(new Color(0xff, 0xff, 0xff, 0xff), ColorUtilities.convertFromHexString("ffffffff", 3, 2, 1, 0));
    }

    /**
     * Test for
     * {@link ColorUtilities#convertFromHexString(String, int, int, int, int)}.
     */
    @Test(expected = NumberFormatException.class)
    public void testConvertFromHexStringBadInput1()
    {
        ColorUtilities.convertFromHexString("", 0, 0, 0, 0);
    }

    /**
     * Test for
     * {@link ColorUtilities#convertFromHexString(String, int, int, int, int)}.
     */
    @Test(expected = NumberFormatException.class)
    public void testConvertFromHexStringBadInput2()
    {
        ColorUtilities.convertFromHexString(" 12345678", 0, 0, 0, 0);
    }

    /**
     * Test for
     * {@link ColorUtilities#convertFromHexString(String, int, int, int, int)}.
     */
    @Test(expected = NumberFormatException.class)
    public void testConvertFromHexStringBadInput3()
    {
        ColorUtilities.convertFromHexString("ffffffff0", 3, 2, 1, 0);
    }

    /**
     * Test for
     * {@link ColorUtilities#convertFromHexString(String, int, int, int, int)}.
     */
    @Test(expected = NumberFormatException.class)
    public void testConvertFromHexStringNullInput()
    {
        ColorUtilities.convertFromHexString(null, 0, 0, 0, 0);
    }

    /**
     * Test for {@link ColorUtilities#convertToHexString(Color, int, int, int)}.
     */
    @Test
    public void testConvertToHexStringColorIntIntInt()
    {
        Assert.assertEquals("123456", ColorUtilities.convertToHexString(new Color(0x12, 0x34, 0x56, 0x78), 0, 1, 2));
        Assert.assertEquals("345678", ColorUtilities.convertToHexString(new Color(0x78, 0x56, 0x34, 0x12), 2, 1, 0));
        Assert.assertEquals("ffffff", ColorUtilities.convertToHexString(new Color(0xff, 0xff, 0xff, 0xff), 2, 1, 0));
    }

    /**
     * Test for
     * {@link ColorUtilities#convertToHexString(Color, int, int, int, int)}.
     */
    @Test
    public void testConvertToHexStringColorIntIntIntInt()
    {
        Assert.assertEquals("12345678", ColorUtilities.convertToHexString(new Color(0x12, 0x34, 0x56, 0x78), 0, 1, 2, 3));
        Assert.assertEquals("12345678", ColorUtilities.convertToHexString(new Color(0x78, 0x56, 0x34, 0x12), 3, 2, 1, 0));
        Assert.assertEquals("ffffffff", ColorUtilities.convertToHexString(new Color(0xff, 0xff, 0xff, 0xff), 3, 2, 1, 0));
    }

    /**
     * Test for {@link ColorUtilities#convertToRGBAColorString(Color)}.
     */
    @Test
    public void testConvertToRGBAColorString()
    {
        Assert.assertEquals("255-0-0-255", ColorUtilities.convertToRGBAColorString(new Color(255, 0, 0, 255)));
        Assert.assertEquals("255-0-0-0", ColorUtilities.convertToRGBAColorString(new Color(255, 0, 0, 0)));
        Assert.assertEquals("0-255-0-255", ColorUtilities.convertToRGBAColorString(new Color(0, 255, 0, 255)));
        Assert.assertEquals("0-255-0-0", ColorUtilities.convertToRGBAColorString(new Color(0, 255, 0, 0)));
        Assert.assertEquals("0-0-255-255", ColorUtilities.convertToRGBAColorString(new Color(0, 0, 255, 255)));
        Assert.assertEquals("0-0-255-0", ColorUtilities.convertToRGBAColorString(new Color(0, 0, 255, 0)));
    }

    /**
     * Test for {@link ColorUtilities#darken(Color, double)}.
     */
    @Test
    public void testDarken()
    {
        // Result with a factor of .7 should be the same as Color.
        Color color1 = new Color(40, 40, 40);
        Assert.assertEquals(color1.darker(), ColorUtilities.darken(color1, .7));

        // Opacity should be retained.
        Color color2 = new Color(40, 40, 40, 10);
        Assert.assertEquals(ColorUtilities.opacitizeColor(color2.darker(), 10), ColorUtilities.darken(color2, .7));
    }

    /**
     * Test for {@link ColorUtilities#getBrightness(Color)}.
     */
    @Test
    public void testGetBrightness()
    {
        Assert.assertEquals(0, ColorUtilities.getBrightness(Color.BLACK));
        Assert.assertEquals(122, ColorUtilities.getBrightness(new Color(223, 53, 111)));
        Assert.assertEquals(255, ColorUtilities.getBrightness(Color.WHITE));

        for (int index = 0; index < 256; ++index)
        {
            Assert.assertEquals(index, ColorUtilities.getBrightness(new Color(index, index, index)));
        }
    }

    /**
     * Test for {@link ColorUtilities#getBrightness(Color)} with a {@code null}
     * argument.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetBrightnessNullArg()
    {
        ColorUtilities.getBrightness(null);
    }

    /** Test for {@link ColorUtilities#isEqual(Color, Color)}. */
    @Test
    public void testIsEqual()
    {
        Assert.assertTrue(ColorUtilities.isEqual(Color.RED, Color.RED));
        Assert.assertTrue(ColorUtilities.isEqual(Color.RED, new Color(255, 0, 0, 255)));
        Assert.assertFalse(ColorUtilities.isEqual(Color.RED, new Color(255, 0, 0, 254)));
        Assert.assertTrue(ColorUtilities.isEqual(null, null));
        Assert.assertFalse(ColorUtilities.isEqual(Color.RED, null));
        Assert.assertFalse(ColorUtilities.isEqual(null, Color.RED));
    }

    /** Test for {@link ColorUtilities#isEqualIgnoreAlpha(Color, Color)}. */
    @Test
    public void testIsEqualIgnoreAlpha()
    {
        Assert.assertTrue(ColorUtilities.isEqualIgnoreAlpha(Color.RED, Color.RED));
        Assert.assertTrue(ColorUtilities.isEqualIgnoreAlpha(Color.RED, new Color(255, 0, 0, 255)));
        Assert.assertTrue(ColorUtilities.isEqualIgnoreAlpha(Color.RED, new Color(255, 0, 0, 254)));
        Assert.assertTrue(ColorUtilities.isEqualIgnoreAlpha(null, null));
        Assert.assertFalse(ColorUtilities.isEqualIgnoreAlpha(Color.RED, null));
        Assert.assertFalse(ColorUtilities.isEqualIgnoreAlpha(null, Color.RED));
    }

    /** Test for {@link ColorUtilities#opacitizeColor(Color, float)}. */
    @Test
    public void testOpacitizeColorFloat()
    {
        Assert.assertNull(ColorUtilities.opacitizeColor(null, 1f));
        Assert.assertEquals(new Color(.1f, .2f, .3f, .5f), ColorUtilities.opacitizeColor(new Color(.1f, .2f, .3f, .4f), .5f));
        Assert.assertEquals(new Color(0f, 1f, 0f, 0f), ColorUtilities.opacitizeColor(new Color(0f, 1f, 0f, 1f), 0f));
        Assert.assertEquals(new Color(0f, 1f, 0f, 1f), ColorUtilities.opacitizeColor(new Color(0f, 1f, 0f, 1f), 1f));
    }

    /**
     * Test for {@link ColorUtilities#opacitizeColor(Color, float)} with
     * negative input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOpacitizeColorFloatBadInput1()
    {
        ColorUtilities.opacitizeColor(Color.RED, -Float.MIN_VALUE);
    }

    /**
     * Test for {@link ColorUtilities#opacitizeColor(Color, float)} with >1
     * input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOpacitizeColorFloatBadInput12()
    {
        ColorUtilities.opacitizeColor(Color.RED, 1.0000001f);
    }

    /** Test for {@link ColorUtilities#opacitizeColor(Color, int)}. */
    @Test
    public void testOpacitizeColorInt()
    {
        Assert.assertNull(ColorUtilities.opacitizeColor(null, 1f));
        Assert.assertEquals(new Color(1, 2, 3, 5), ColorUtilities.opacitizeColor(new Color(1, 2, 3, 4), 5));
        Assert.assertEquals(new Color(0, 1, 0, 0), ColorUtilities.opacitizeColor(new Color(0, 1, 0, 1), 0));
        Assert.assertEquals(new Color(0, 1, 0, 255), ColorUtilities.opacitizeColor(new Color(0, 1, 0, 255), 255));
    }

    /**
     * Test for {@link ColorUtilities#opacitizeColor(Color, int)} with negative
     * input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOpacitizeColorIntBadInput1()
    {
        ColorUtilities.opacitizeColor(Color.RED, -1);
    }

    /**
     * Test for {@link ColorUtilities#opacitizeColor(Color, int)} with >1 input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOpacitizeColorIntBadInput12()
    {
        ColorUtilities.opacitizeColor(Color.RED, 256);
    }
}
