package io.opensphere.core.model;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.LatLonAlt.CoordFormat;

/** Test for {@link LatLonAltParser}. */
public class LatLonAltParserTest
{
    /** Four zeros. */
    private static final String ZZZZ = "0000";

    /**
     * Tests for {@link LatLonAltParser#parseLat(String)}.
     */
    @Test
    public void testParseLat()
    {
        // Test null
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat(null)));

        // Test empty string
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("")));
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat(" ")));

        // Test alpha
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("N")));

        // Test longitudes
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("5E")));
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("5W")));
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("91")));
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("-91")));

        // Test various representations for +5
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5."), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5.0"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5.0°"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("05°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("005°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("0005°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00'00"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00'00\""), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5.N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5.0N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5.0°N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°0'N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00'N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00'00N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00'00\"N"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("5°00'00\" N"), 0.);
        Assert.assertEquals(5.0, LatLonAltParser.parseLat("5°00.0N"), 0.0);

        // Test various representations for -5
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5."), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5.0"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5.0°"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5°0'"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5°00"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5°00'"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5°00'00"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("-5°00'00\""), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5.S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5.0S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5.0°S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5°0'S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5°00S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5°00'S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5°00'00S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5°00'00\"S"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLat("5°00'00\" S"), 0.);
        Assert.assertEquals(-5.0, LatLonAltParser.parseLat("5°00.0S"), 0.0);

        // Test decimal values over 100
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("123.4")));

        // Test different number of digits.
        Assert.assertEquals(+50., LatLonAltParser.parseLat("50"), 0.);
        Assert.assertEquals(+50., LatLonAltParser.parseLat("050"), 0.);
        Assert.assertEquals(+50., LatLonAltParser.parseLat("050", CoordFormat.DMS), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("500"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("0500"), 0.);
        Assert.assertEquals(+50., LatLonAltParser.parseLat("5000"), 0.);
        Assert.assertEquals(+50., LatLonAltParser.parseLat("05000"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("50000"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLat("050000"), 0.);
        Assert.assertEquals(36., LatLonAltParser.parseLat("360000"), 0.);
        Assert.assertEquals(36., LatLonAltParser.parseLat("0360000"), 0.);
        Assert.assertEquals(.6, LatLonAltParser.parseLat("003600"), 0.);
        Assert.assertEquals(.6, LatLonAltParser.parseLat("0003600"), 0.);
        Assert.assertEquals(.01, LatLonAltParser.parseLat("000036"), 0.);
        Assert.assertEquals(.01, LatLonAltParser.parseLat("0000036"), 0.);
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLat("0036", CoordFormat.DECIMAL)));
        Assert.assertEquals(+.6, LatLonAltParser.parseLat("0036", CoordFormat.DMS), 0.);

        for (int deg = 0; deg < 90; ++deg)
        {
            Assert.assertEquals(deg, LatLonAltParser.parseLat(Integer.toString(deg)), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("0" + deg), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat(deg + "00"), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("0" + deg + "00"), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat(deg + ZZZZ), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("0" + deg + ZZZZ), 0.);

            Assert.assertEquals(deg, LatLonAltParser.parseLat("+" + deg), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("+0" + deg), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("+" + deg + "00"), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("+0" + deg + "00"), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("+" + deg + ZZZZ), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLat("+0" + deg + ZZZZ), 0.);

            Assert.assertEquals(-deg, LatLonAltParser.parseLat("-" + deg), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("-0" + deg), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("-" + deg + "00"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("-0" + deg + "00"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("-" + deg + ZZZZ), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("-0" + deg + ZZZZ), 0.);

            Assert.assertEquals(-deg, LatLonAltParser.parseLat(deg + "S"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("0" + deg + "S"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat(deg + "00S"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("0" + deg + "00S"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat(deg + "0000S"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLat("0" + deg + "0000S"), 0.);
        }

        // Test some common cases.
        Assert.assertEquals(LatLonAltParser.parseLat("13 21 30N"), LatLonAltParser.parseLat("13 21.5N"), 0.0);
        Assert.assertEquals(LatLonAltParser.parseLat("13 21 30N"), LatLonAltParser.parseLat("132150N", CoordFormat.DDM), 0.0);

        Assert.assertEquals(25.1, LatLonAltParser.parseLat("25°6'"), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLat("25°06'"), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLat("25°6'0\""), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLat("25°6'00\""), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLat("25°06'00\""), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLat("25 6 0"), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLat("25 6"), 0.);
        Assert.assertEquals(25.36, LatLonAltParser.parseLat("25°21'36.0\""), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLat("25 21 36.0"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLat("25:21:36.0"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLat("25,21,36.0"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLat("25'21'36.0'"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLat("25\"21\"36.0\""), 1e-14);
        Assert.assertEquals(89 + 3599.999999 / 3600, LatLonAltParser.parseLat("89°59'59.999999\"N"), 1e-12);

        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("25°6'S"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("25°06'S"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("25°6'0\"S"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("25°6'00\"S"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("25°06'00\"S"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("25 6 0S"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("25 6S"), 0.);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("25°21'36.0\"S"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("25 21 36.0S"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("25:21:36.0S"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("25,21,36.0S"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("25'21'36.0'S"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("25\"21\"36.0\"S"), 1e-14);
        Assert.assertEquals(-89 - 3599.999999 / 3600, LatLonAltParser.parseLat("89°59'59.999999\"S"), 1e-12);
        Assert.assertEquals(37.0361, LatLonAltParser.parseLat("37.02.10N"), 1.2e-4);
        Assert.assertEquals(37.03614527, LatLonAltParser.parseLat("37.02.10.123N"), 7.8e-8);

        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("-25°6'"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("-25°06'"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("-25°6'0\""), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("-25°6'00\""), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("-25°06'00\""), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("-25 6 0"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLat("-25 6"), 0.);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("-25°21'36.0\""), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("-25 21 36.0"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("-25:21:36.0"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("-25,21,36.0"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("-25'21'36.0'"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLat("-25\"21\"36.0\""), 1e-14);
        Assert.assertEquals(-89 - 3599.999999 / 3600, LatLonAltParser.parseLat("-89°59'59.999999\""), 1e-12);
    }

    /**
     * Tests for {@link LatLonAltParser#parseLatLon(String)}.
     */
    @Test
    public void testParseLatLon()
    {
        // Test null
        Assert.assertNull(LatLonAltParser.parseLatLon(null));

        // Test empty string
        Assert.assertNull(LatLonAltParser.parseLatLon(""));
        Assert.assertNull(LatLonAltParser.parseLatLon(" "));

        // Test single digit
        Assert.assertNull(LatLonAltParser.parseLatLon("5"));

        // Test two digits
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5 6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5,6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5, 6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5 ,6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5 , 6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon(" 5  6 "));

        // Test two digits with hemisphere indicators
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5N 6E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5.N 6.E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5.N, 6.E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5.N ,6.E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("5.N , 6.E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("N5 E6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("N5. E6."));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("N5., E6."));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("N5. ,E6."));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5., 6.), LatLonAltParser.parseLatLon("N5. , E6."));

        // Test two digits with hemisphere indicators, negative
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("5S 6W"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("5.S 6.W"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("5.S, 6.W"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("5.S ,6.W"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("5.S , 6.W"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("S5 W6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("S5. W6."));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("S5., W6."));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("S5. ,W6."));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-5., -6.), LatLonAltParser.parseLatLon("S5. , W6."));

        // Test two digits with hemisphere indicators, reverse order
        Assert.assertEquals(LatLonAlt.createFromDegrees(6., 5.), LatLonAltParser.parseLatLon("5E 6N"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6., 5.), LatLonAltParser.parseLatLon("5.E 6.N"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6., 5.), LatLonAltParser.parseLatLon("E5 N6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6., 5.), LatLonAltParser.parseLatLon("E5. N6."));

        // Test two digits with hemisphere indicators, negative, reverse order
        Assert.assertEquals(LatLonAlt.createFromDegrees(-6., -5.), LatLonAltParser.parseLatLon("5W 6S"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-6., -5.), LatLonAltParser.parseLatLon("5.W 6.S"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-6., -5.), LatLonAltParser.parseLatLon("W5 S6"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-6., -5.), LatLonAltParser.parseLatLon("W5. S6."));

        // Test fractional degrees.
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("5.5 6.5"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("5.5N 6.5E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 5.5), LatLonAltParser.parseLatLon("5.5E 6.5N"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("N5.5 E6.5"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 5.5), LatLonAltParser.parseLatLon("E5.5 N6.5"));

        // Test with degree symbol.
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("5.5° 6.5°"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("5°30' 6°30'"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("5°30'00\" 6°30'00\""));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("5.5°N 6.5°E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 5.5), LatLonAltParser.parseLatLon("5.5°E 6.5°N"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("N5.5° E6.5°"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 5.5), LatLonAltParser.parseLatLon("E5.5° N6.5°"));

        // Test a series of digits with no symbols.
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("53000 63000"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(5.5, 6.5), LatLonAltParser.parseLatLon("053000 063000"));

        // Test longitude first.
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 5.5), LatLonAltParser.parseLatLon("053000E 063000"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 5.5), LatLonAltParser.parseLatLon("E053000 063000"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 5.5), LatLonAltParser.parseLatLon("053000 063000N"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(6.5, 91), LatLonAltParser.parseLatLon("910000 063000"));

        // Test high-precision values.
        Assert.assertEquals(179 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("89°59'59.999999\"N 179°59'59.999999\"E").getLonD(), 1e-12);
        Assert.assertEquals(89 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("89°59'59.999999\"N 179°59'59.999999\"E").getLatD(), 1e-12);
        Assert.assertEquals(179 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("89°59'59.999999\"N 179°59'59.999999\"E").getLonD(), 1e-12);
        Assert.assertEquals(89 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("89°59'59.999999\"N 179°59'59.999999\"E").getLatD(), 1e-12);
        Assert.assertEquals(179 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("N89°59'59.999999\" E179°59'59.999999\"").getLonD(), 1e-12);
        Assert.assertEquals(89 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("N89°59'59.999999\" E179°59'59.999999\"").getLatD(), 1e-12);
        Assert.assertEquals(179 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("N89°59'59.999999\" E179°59'59.999999\"").getLonD(), 1e-12);
        Assert.assertEquals(89 + 3599.999999 / 3600,
                LatLonAltParser.parseLatLon("N89°59'59.999999\" E179°59'59.999999\"").getLatD(), 1e-12);

        Assert.assertEquals(LatLonAlt.createFromDegrees(10.30251, 20.51002),
                LatLonAltParser.parseLatLon("101809036n 0203036072e"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(10.30251, 20.51002),
                LatLonAltParser.parseLatLon("101809036N 0203036072E"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(10.30251, 20.51002), LatLonAltParser.parseLatLon("101809036 0203036072"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-10.30251, -20.51002),
                LatLonAltParser.parseLatLon("101809036s 0203036072w"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(-10.30251, -20.51002),
                LatLonAltParser.parseLatLon("-101809036 -0203036072"));

        Assert.assertEquals(LatLonAlt.createFromDegrees(10.30251, 20.51002),
                LatLonAltParser.parseLatLon("101809036n0203036072e"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(10.30251, 20.51002),
                LatLonAltParser.parseLatLon("n101809036e0203036072"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(49.60361111111111, -0.021388888888888888),
                LatLonAltParser.parseLatLon("N493613 W0000117"));

        Assert.assertEquals(LatLonAlt.createFromDegrees(33, 169.0909), LatLonAltParser.parseLatLon("33 169.0909"));
        Assert.assertEquals(LatLonAlt.createFromDegrees(33, -169.0909), LatLonAltParser.parseLatLon("33 -169.0909"));

        StringBuilder sb = new StringBuilder();
        for (int lat = -90; lat <= 90; ++lat)
        {
            for (int lon = -180; lon <= 180; ++lon)
            {
                LatLonAlt lla = LatLonAlt.createFromDegrees(lat, lon);
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lat).append(' ').append(lon).toString()));
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append('.').toString()));
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lat).append(". ").append(lon).toString()));
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append('.').toString()));
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lat).append(".0 ").append(lon).toString()));
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(".0").toString()));
                sb.setLength(0);
                Assert.assertEquals(lla,
                        LatLonAltParser.parseLatLon(sb.append(lat).append(' ').append(lon).append(".0").toString()));
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lat).append('/').append(lon).toString()));
            }
        }

        // Test lon/lat for 3-digit longitudes.
        for (int lat = -90; lat <= 90; ++lat)
        {
            for (int lon = -180; lon <= 180; ++lon)
            {
                LatLonAlt lla = LatLonAlt.createFromDegrees(lat, lon);
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lon).append(' ').append(lat).toString()));
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append('.').toString()));
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lon).append(". ").append(lat).toString()));
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append('.').toString()));
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lon).append(".0 ").append(lat).toString()));
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(".0").toString()));
                sb.setLength(0);
                Assert.assertEquals(lla,
                        LatLonAltParser.parseLatLon(sb.append(lon).append(' ').append(lat).append(".0").toString()));
                sb.setLength(0);
                Assert.assertEquals(lla, LatLonAltParser.parseLatLon(sb.append(lat).append('/').append(lon).toString()));

                if (lon == -100)
                {
                    lon = 99;
                }
            }
        }
    }

    /**
     * Tests for {@link LatLonAltParser#parseLon(String)}.
     */
    @Test
    public void testParseLon()
    {
        // Test null
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLon(null)));

        // Test empty string
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLon("")));
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLon(" ")));

        // Test alpha
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLon("N")));

        // Test latitudes
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLon("5N")));
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLon("5S")));

        // Test various representations for +5
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5."), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5.0"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5.0°"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("05°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("005°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("0005°0'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00'"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00'00"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00'00\""), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5.E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5.0E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5.0°E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°0'E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00'E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00'00E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00'00\"E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5°00'00\" E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5.00.00\" E"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("5.00.00.00\" E"), 0.);

        // Test various representations for -5
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5."), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5.0"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5.0°"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5°0'"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5°00"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5°00'"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5°00'00"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("-5°00'00\""), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5.W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5.0W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5.0°W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5°0'W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5°00W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5°00'W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5°00'00W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5°00'00\"W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5°00'00\" W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5.00.00\" W"), 0.);
        Assert.assertEquals(-5., LatLonAltParser.parseLon("5.00.00.00\" W"), 0.);

        // Test decimal values over 100
        Assert.assertEquals(+123.4, LatLonAltParser.parseLon("123.4"), 0.);

        // Test different number of digits.
        Assert.assertEquals(+50., LatLonAltParser.parseLon("50"), 0.);
        Assert.assertEquals(+50., LatLonAltParser.parseLon("050"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("500"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("0500"), 0.);
        Assert.assertEquals(+50., LatLonAltParser.parseLon("5000"), 0.);
        Assert.assertEquals(+50., LatLonAltParser.parseLon("05000"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("50000"), 0.);
        Assert.assertEquals(+5., LatLonAltParser.parseLon("050000"), 0.);
        Assert.assertEquals(36., LatLonAltParser.parseLon("36"), 0.);
        Assert.assertEquals(36., LatLonAltParser.parseLon("3600"), 0.);
        Assert.assertEquals(36., LatLonAltParser.parseLon("360000"), 0.);
        Assert.assertEquals(36., LatLonAltParser.parseLon("0360000"), 0.);
        Assert.assertEquals(.6, LatLonAltParser.parseLon("00036"), 0.);
        Assert.assertEquals(.6, LatLonAltParser.parseLon("0003600"), 0.);
        Assert.assertEquals(.01, LatLonAltParser.parseLon("0000036"), 0.);
        Assert.assertTrue(Double.isNaN(LatLonAltParser.parseLon("00036", CoordFormat.DECIMAL)));
        Assert.assertEquals(+.6, LatLonAltParser.parseLon("00036", CoordFormat.DMS), 0.);

        for (int deg = 0; deg < 180; ++deg)
        {
            Assert.assertEquals(deg, LatLonAltParser.parseLon(Integer.toString(deg)), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLon("0" + deg), 0.);

            Assert.assertEquals(deg, LatLonAltParser.parseLon("+" + deg), 0.);
            Assert.assertEquals(deg, LatLonAltParser.parseLon("+0" + deg), 0.);

            Assert.assertEquals(-deg, LatLonAltParser.parseLon("-" + deg), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLon("-0" + deg), 0.);

            Assert.assertEquals(-deg, LatLonAltParser.parseLon(deg + "W"), 0.);
            Assert.assertEquals(-deg, LatLonAltParser.parseLon("0" + deg + "W"), 0.);

            // Only do these tests above 3 to avoid the confusion between cases
            // like "100" where it could be 1°00 or 100°.
            if (deg > 3)
            {
                Assert.assertEquals(deg, LatLonAltParser.parseLon(deg + "00"), 0.);
                Assert.assertEquals(deg, LatLonAltParser.parseLon("0" + deg + "00"), 0.);
                Assert.assertEquals(deg, LatLonAltParser.parseLon(deg + ZZZZ), 0.);
                Assert.assertEquals(deg, LatLonAltParser.parseLon("0" + deg + ZZZZ), 0.);

                Assert.assertEquals(deg, LatLonAltParser.parseLon("+" + deg + "00"), 0.);
                Assert.assertEquals(deg, LatLonAltParser.parseLon("+0" + deg + "00"), 0.);
                Assert.assertEquals(deg, LatLonAltParser.parseLon("+" + deg + ZZZZ), 0.);
                Assert.assertEquals(deg, LatLonAltParser.parseLon("+0" + deg + ZZZZ), 0.);

                Assert.assertEquals(-deg, LatLonAltParser.parseLon("-" + deg + "00"), 0.);
                Assert.assertEquals(-deg, LatLonAltParser.parseLon("-0" + deg + "00"), 0.);
                Assert.assertEquals(-deg, LatLonAltParser.parseLon("-" + deg + ZZZZ), 0.);
                Assert.assertEquals(-deg, LatLonAltParser.parseLon("-0" + deg + ZZZZ), 0.);

                Assert.assertEquals(-deg, LatLonAltParser.parseLon(deg + "00W"), 0.);
                Assert.assertEquals(-deg, LatLonAltParser.parseLon("0" + deg + "00W"), 0.);
                Assert.assertEquals(-deg, LatLonAltParser.parseLon(deg + "0000W"), 0.);
                Assert.assertEquals(-deg, LatLonAltParser.parseLon("0" + deg + "0000W"), 0.);
            }
        }

        // Test some common cases.
        Assert.assertEquals(LatLonAltParser.parseLon("113 21 30N"), LatLonAltParser.parseLon("113 21.5N"), 0.0);
        Assert.assertEquals(LatLonAltParser.parseLon("13 21 30N"), LatLonAltParser.parseLon("13 21.5N"), 0.0);
        Assert.assertEquals(LatLonAltParser.parseLon("13 21 30N"), LatLonAltParser.parseLon("0132150N", CoordFormat.DDM), 0.0);

        Assert.assertEquals(25.1, LatLonAltParser.parseLon("25°6'"), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLon("25°06'"), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLon("25°6'0\""), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLon("25°6'00\""), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLon("25°06'00\""), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLon("25 6 0"), 0.);
        Assert.assertEquals(25.1, LatLonAltParser.parseLon("25 6"), 0.);
        Assert.assertEquals(25.36, LatLonAltParser.parseLon("25°21'36.0\""), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLon("25 21 36.0"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLon("25:21:36.0"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLon("25,21,36.0"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLon("25'21'36.0'"), 1e-14);
        Assert.assertEquals(25.36, LatLonAltParser.parseLon("25\"21\"36.0\""), 1e-14);
        Assert.assertEquals(179 + 3599.999999 / 3600, LatLonAltParser.parseLon("179°59'59.999999\"E"), 1e-12);

        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("25°6'W"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("25°06'W"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("25°6'0\"W"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("25°6'00\"W"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("25°06'00\"W"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("25 6 0W"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("25 6W"), 0.);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("25°21'36.0\"W"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("25 21 36.0W"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("25:21:36.0W"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("25,21,36.0W"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("25'21'36.0'W"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("25\"21\"36.0\"W"), 1e-14);
        Assert.assertEquals(-179 - 3599.999999 / 3600, LatLonAltParser.parseLon("179°59'59.999999\"W"), 1e-12);

        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("-25°6'"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("-25°06'"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("-25°6'0\""), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("-25°6'00\""), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("-25°06'00\""), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("-25 6 0"), 0.);
        Assert.assertEquals(-25.1, LatLonAltParser.parseLon("-25 6"), 0.);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("-25°21'36.0\""), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("-25 21 36.0"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("-25:21:36.0"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("-25,21,36.0"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("-25'21'36.0'"), 1e-14);
        Assert.assertEquals(-25.36, LatLonAltParser.parseLon("-25\"21\"36.0\""), 1e-14);
        Assert.assertEquals(-179 - 3599.999999 / 3600, LatLonAltParser.parseLon("-179°59'59.999999\""), 1e-12);
    }
}
