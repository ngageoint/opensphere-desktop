package io.opensphere.core.units.angle;

import org.junit.Test;

import org.junit.Assert;

/**
 * Test for {@link DegreesMinutesSeconds}.
 */
public class DegreesMinutesSecondsTest
{
    /**
     * Test {@link DegreesMinutesSeconds#clone()}.
     */
    @Test
    public void testClone()
    {
        Coordinates ang = new DegreesMinutesSeconds(34.54512);
        Assert.assertEquals(ang.getMagnitude(), ang.clone().getMagnitude(), 0.);
    }

    /**
     * Test {@link Coordinates#compareTo(Coordinates)}.
     */
    @Test
    public void testCompareTo()
    {
        Coordinates ang = new DegreesMinutesSeconds(34.54512);
        Assert.assertEquals(0, ang.compareTo(new DegreesMinutesSeconds(34.54512)));
        Assert.assertTrue(ang.compareTo(new DegreesMinutesSeconds(34.54513)) < 0);
        Assert.assertTrue(ang.compareTo(new DegreesMinutesSeconds(34.54511)) > 0);
    }

    /**
     * Test {@link #equals(Object)}.
     */
    public void testEquals()
    {
        Assert.assertTrue(new DegreesMinutesSeconds(34.54512).equals(new DegreesMinutesSeconds(34.54512)));
        Assert.assertFalse(new DegreesMinutesSeconds(34.54512).equals(new DegreesMinutesSeconds(34.545125)));
    }

    /**
     * Test {@link #hashCode()}.
     */
    @Test
    public void testHashCode()
    {
        Coordinates ang = new DegreesMinutesSeconds(34.54512);
        Assert.assertEquals(ang.hashCode(), new DegreesMinutesSeconds(34.54512).hashCode());
        Assert.assertFalse(ang.hashCode() == new DegreesMinutesSeconds(34.545125).hashCode());
    }

    /**
     * Test the label and toString methods.
     */
    @Test
    public void testLabels()
    {
        Coordinates ang = new DegreesMinutesSeconds(34.54512);
        Assert.assertTrue(ang.getLongLabel().length() > 0);
        Assert.assertTrue(ang.getShortLabel().length() > 0);
        Assert.assertTrue(ang.toString().length() > 0);
    }

    /**
     * Test {@link Coordinates#toShortLabelString()}.
     */
    @Test
    public void testToShortLabelString()
    {
        Assert.assertEquals("  34°08'42.4320\"", new DegreesMinutesSeconds(34.14512).toShortLabelString());
        Assert.assertEquals("  34°32'42.4320\"", new DegreesMinutesSeconds(34.54512).toShortLabelString());
        Assert.assertEquals(" 134°32'42.4320\"", new DegreesMinutesSeconds(134.54512).toShortLabelString());
        Assert.assertEquals(" 134°32'42.4500\"", new DegreesMinutesSeconds(134.545125).toShortLabelString());
        Assert.assertEquals(" 134°32'42.4518\"", new DegreesMinutesSeconds(134.5451255).toShortLabelString());
        Assert.assertEquals(" 134°58'59.9999\"", new DegreesMinutesSeconds(134.9833333).toShortLabelString());
        Assert.assertEquals(" 134°59'00.0000\"", new DegreesMinutesSeconds(134.98333332).toShortLabelString());
        Assert.assertEquals(" 134°59'59.9999\"", new DegreesMinutesSeconds(134.9999999861).toShortLabelString());
        Assert.assertEquals(" 135°00'00.0000\"", new DegreesMinutesSeconds(134.9999999862).toShortLabelString());
        Assert.assertEquals("   0°58'59.9999\"", new DegreesMinutesSeconds(0.9833333).toShortLabelString());
        Assert.assertEquals("   0°59'00.0000\"", new DegreesMinutesSeconds(0.98333332).toShortLabelString());
        Assert.assertEquals("   0°59'59.9999\"", new DegreesMinutesSeconds(0.9999999861).toShortLabelString());
        Assert.assertEquals("   1°00'00.0000\"", new DegreesMinutesSeconds(0.9999999862).toShortLabelString());
        Assert.assertEquals(" -34°08'42.4320\"", new DegreesMinutesSeconds(-34.14512).toShortLabelString());
        Assert.assertEquals(" -34°32'42.4320\"", new DegreesMinutesSeconds(-34.54512).toShortLabelString());
        Assert.assertEquals("-134°32'42.4320\"", new DegreesMinutesSeconds(-134.54512).toShortLabelString());
        Assert.assertEquals("-134°32'42.4500\"", new DegreesMinutesSeconds(-134.545125).toShortLabelString());
        Assert.assertEquals("-134°32'42.4518\"", new DegreesMinutesSeconds(-134.5451255).toShortLabelString());
        Assert.assertEquals("-134°58'59.9999\"", new DegreesMinutesSeconds(-134.9833333).toShortLabelString());
        Assert.assertEquals("-134°59'00.0000\"", new DegreesMinutesSeconds(-134.98333332).toShortLabelString());
        Assert.assertEquals("-134°59'59.9999\"", new DegreesMinutesSeconds(-134.9999999861).toShortLabelString());
        Assert.assertEquals("-135°00'00.0000\"", new DegreesMinutesSeconds(-134.9999999862).toShortLabelString());
        Assert.assertEquals("  -0°58'59.9999\"", new DegreesMinutesSeconds(-0.9833333).toShortLabelString());
        Assert.assertEquals("  -0°59'00.0000\"", new DegreesMinutesSeconds(-0.98333332).toShortLabelString());
        Assert.assertEquals("  -0°59'59.9999\"", new DegreesMinutesSeconds(-0.9999999861).toShortLabelString());
        Assert.assertEquals("  -1°00'00.0000\"", new DegreesMinutesSeconds(-0.9999999862).toShortLabelString());
    }

    /**
     * Test {@link Coordinates#toShortLabelString(char, char)}.
     */
    @Test
    public void testToShortLabelStringCharChar()
    {
        Assert.assertEquals("  34°08'42.4320\"N", new DegreesMinutesSeconds(34.14512).toShortLabelString('N', 'S'));
        Assert.assertEquals("  34°32'42.4320\"N", new DegreesMinutesSeconds(34.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°32'42.4320\"N", new DegreesMinutesSeconds(134.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°32'42.4500\"N", new DegreesMinutesSeconds(134.545125).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°32'42.4518\"N", new DegreesMinutesSeconds(134.5451255).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°58'59.9999\"N", new DegreesMinutesSeconds(134.9833333).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°59'00.0000\"N", new DegreesMinutesSeconds(134.98333332).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°59'59.9999\"N", new DegreesMinutesSeconds(134.9999999861).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 135°00'00.0000\"N", new DegreesMinutesSeconds(134.9999999862).toShortLabelString('N', 'S'));
        Assert.assertEquals("   0°58'59.9999\"N", new DegreesMinutesSeconds(0.9833333).toShortLabelString('N', 'S'));
        Assert.assertEquals("   0°59'00.0000\"N", new DegreesMinutesSeconds(0.98333332).toShortLabelString('N', 'S'));
        Assert.assertEquals("   0°59'59.9999\"N", new DegreesMinutesSeconds(0.9999999861).toShortLabelString('N', 'S'));
        Assert.assertEquals("   1°00'00.0000\"N", new DegreesMinutesSeconds(0.9999999862).toShortLabelString('N', 'S'));
        Assert.assertEquals("  34°08'42.4320\"S", new DegreesMinutesSeconds(-34.14512).toShortLabelString('N', 'S'));
        Assert.assertEquals("  34°32'42.4320\"S", new DegreesMinutesSeconds(-34.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°32'42.4320\"S", new DegreesMinutesSeconds(-134.54512).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°32'42.4500\"S", new DegreesMinutesSeconds(-134.545125).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°32'42.4518\"S", new DegreesMinutesSeconds(-134.5451255).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°58'59.9999\"S", new DegreesMinutesSeconds(-134.9833333).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°59'00.0000\"S", new DegreesMinutesSeconds(-134.98333332).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 134°59'59.9999\"S", new DegreesMinutesSeconds(-134.9999999861).toShortLabelString('N', 'S'));
        Assert.assertEquals(" 135°00'00.0000\"S", new DegreesMinutesSeconds(-134.9999999862).toShortLabelString('N', 'S'));
        Assert.assertEquals("   0°58'59.9999\"S", new DegreesMinutesSeconds(-0.9833333).toShortLabelString('N', 'S'));
        Assert.assertEquals("   0°59'00.0000\"S", new DegreesMinutesSeconds(-0.98333332).toShortLabelString('N', 'S'));
        Assert.assertEquals("   0°59'59.9999\"S", new DegreesMinutesSeconds(-0.9999999861).toShortLabelString('N', 'S'));
        Assert.assertEquals("   1°00'00.0000\"S", new DegreesMinutesSeconds(-0.9999999862).toShortLabelString('N', 'S'));
    }

    /**
     * Test {@link Coordinates#toShortLabelString(int, int)}.
     */
    @Test
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    public void testToShortLabelStringIntInt()
    {
        String[] expected = { "34°", "34°", "34°", "34°", "34°", "34°", "34°", "34°", "34°", "34°", " 34°", " 34°", " 34°",
            " 34°", " 34°", " 34°", " 34°", " 34°", " 34°", " 34°", "  34°", "  34°", "  34°", "  34°", "  34°", "  34°", "  34°",
            "  34°", "  34°", "  34°", "   34°", "   34°", "   34°", "   34°", "   34°", "   34°", "   34°", "   34°", "   34°",
            "   34°", "    34°", "    34°", "    34°", "    34°", "    34°", "    34°", "    34°", "    34°", "    34°",
            "    34°", "  34°03'", "  34°03'", "  34°03'", "  34°03'", "  34°03'", "  34°03'", "  34°03'", "  34°03'", "  34°03'",
            "  34°03'", "   34°03'", "   34°03'", "   34°03'", "   34°03'", "   34°03'", "   34°03'", "   34°03'", "   34°03'",
            "   34°03'", "   34°03'", "    34°03'", "    34°03'", "    34°03'", "    34°03'", "    34°03'", "    34°03'",
            "    34°03'", "    34°03'", "    34°03'", "    34°03'", "  34°03'04\"", "  34°03'04\"", "  34°03'04\"",
            "  34°03'04\"", "  34°03'04\"", "  34°03'04\"", "  34°03'04\"", "  34°03'04\"", "  34°03'04\"", "  34°03'04\"",
            "   34°03'04\"", "   34°03'04\"", "   34°03'04\"", "   34°03'04\"", "   34°03'04\"", "   34°03'04\"", "   34°03'04\"",
            "   34°03'04\"", "   34°03'04\"", "   34°03'04\"", "    34°03'04\"", "  34°03'04.0\"", "  34°03'04.0\"",
            "  34°03'04.0\"", "  34°03'04.0\"", "  34°03'04.0\"", "  34°03'04.0\"", "  34°03'04.0\"", "  34°03'04.0\"",
            "  34°03'04.0\"", "     34°03'04\"", "   34°03'04.0\"", "  34°03'04.03\"", "  34°03'04.03\"", "  34°03'04.03\"",
            "  34°03'04.03\"", "  34°03'04.03\"", "  34°03'04.03\"", "  34°03'04.03\"", "  34°03'04.03\"", "      34°03'04\"",
            "    34°03'04.0\"", "   34°03'04.03\"", "  34°03'04.032\"", "  34°03'04.032\"", "  34°03'04.032\"",
            "  34°03'04.032\"", "  34°03'04.032\"", "  34°03'04.032\"", "  34°03'04.032\"", "       34°03'04\"",
            "     34°03'04.0\"", "    34°03'04.03\"", "   34°03'04.032\"", "  34°03'04.0320\"", "  34°03'04.0320\"",
            "  34°03'04.0320\"", "  34°03'04.0320\"", "  34°03'04.0320\"", "  34°03'04.0320\"", "        34°03'04\"",
            "      34°03'04.0\"", "     34°03'04.03\"", "    34°03'04.032\"", "   34°03'04.0320\"", "  34°03'04.03200\"",
            "  34°03'04.03200\"", "  34°03'04.03200\"", "  34°03'04.03200\"", "  34°03'04.03200\"", "         34°03'04\"",
            "       34°03'04.0\"", "      34°03'04.03\"", "     34°03'04.032\"", "    34°03'04.0320\"", "   34°03'04.03200\"",
            "  34°03'04.032000\"", "  34°03'04.032000\"", "  34°03'04.032000\"", "  34°03'04.032000\"", "          34°03'04\"",
            "        34°03'04.0\"", "       34°03'04.03\"", "      34°03'04.032\"", "     34°03'04.0320\"",
            "    34°03'04.03200\"", "   34°03'04.032000\"", "  34°03'04.0320000\"", "  34°03'04.0320000\"",
            "  34°03'04.0320000\"", };
        for (int width = 3; width < 20; ++width)
        {
            for (int precision = 0; precision < 10; ++precision)
            {
                String str = new DegreesMinutesSeconds(34.05112).toShortLabelString(width, precision);
                Assert.assertEquals(width, str.length());
                Assert.assertEquals(expected[(width - 3) * 10 + precision], str);
            }
        }

        // Tests for rounding.
        Assert.assertEquals("  35°20'59.994999599987864\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(28, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("  35°20'59.99499959998786\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(27, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("  35°20'59.9949995999879\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(26, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("  35°20'59.994999599988\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(25, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("  35°20'59.99499959999\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(24, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("  35°20'59.9949996000\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(23, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("  35°20'59.994999600\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(22, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("  35°20'59.99499960\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(21, Integer.MAX_VALUE, 'N', 'S'));
        Assert.assertEquals("   35°20'59.9949996\"N",
                new DegreesMinutesSeconds(35.349998611).toShortLabelString(21, 7, 'N', 'S'));
        Assert.assertEquals("  35°20'59.9949996\"N", new DegreesMinutesSeconds(35.349998611).toShortLabelString(20, 7, 'N', 'S'));
        Assert.assertEquals("   35°20'59.995000\"N", new DegreesMinutesSeconds(35.349998611).toShortLabelString(20, 6, 'N', 'S'));
        Assert.assertEquals("  35°20'59.995000\"N", new DegreesMinutesSeconds(35.349998611).toShortLabelString(19, 6, 'N', 'S'));
        Assert.assertEquals("  35°20'59.99500\"N", new DegreesMinutesSeconds(35.349998611).toShortLabelString(18, 6, 'N', 'S'));
        Assert.assertEquals("  35°20'59.9950\"N", new DegreesMinutesSeconds(35.349998611).toShortLabelString(17, 6, 'N', 'S'));
        Assert.assertEquals("  35°20'59.995\"N", new DegreesMinutesSeconds(35.349998611).toShortLabelString(16, 6, 'N', 'S'));
        Assert.assertEquals("  35°21'00.00\"N", new DegreesMinutesSeconds(35.34999861112).toShortLabelString(15, 6, 'N', 'S'));
        Assert.assertEquals("  35°21'N", new DegreesMinutesSeconds(35.349998611).toShortLabelString(9, 6, 'N', 'S'));
        Assert.assertEquals("36°N", new DegreesMinutesSeconds(35.5).toShortLabelString(4, 6, 'N', 'S'));
    }
}
