package io.opensphere.core.model;

import java.util.Formatter;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link LatLonAltParser}. */
public class LatLonAltParserTestFunctional
{
    /**
     * Tests for {@link LatLonAltParser#parseLatLon(String)}.
     */
    @Test
    public void testParseLatLon()
    {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        LatLonAlt actual;
        double ang;
        LatLonAlt expected;
        int skip = 1;
        for (int deg = 0; deg < 180; deg += skip)
        {
            for (int min = 0; min < 60; min += skip)
            {
                for (int sec = 0; sec < 60; sec += skip)
                {
                    sb.setLength(0);
                    formatter.format("%02d%02d%02d %03d%02d%02d", Integer.valueOf(deg % 90), Integer.valueOf(min),
                            Integer.valueOf(sec), Integer.valueOf(deg), Integer.valueOf(min), Integer.valueOf(sec));
                    actual = LatLonAltParser.parseLatLon(sb.toString());
                    ang = deg + (min + sec / 60.) / 60.;
                    expected = LatLonAlt.createFromDegrees(ang % 90, ang);
                    Assert.assertEquals(expected, actual);

                    sb.setLength(0);
                    formatter.format("-%02d%02d%02d -%03d%02d%02d", Integer.valueOf(deg % 90), Integer.valueOf(min),
                            Integer.valueOf(sec), Integer.valueOf(deg), Integer.valueOf(min), Integer.valueOf(sec));
                    actual = LatLonAltParser.parseLatLon(sb.toString());
                    expected = LatLonAlt.createFromDegrees(-ang % 90, -ang);
                    Assert.assertEquals(expected, actual);

                    sb.setLength(0);
                    formatter.format("S%02d%02d%02d W%03d%02d%02d", Integer.valueOf(deg % 90), Integer.valueOf(min),
                            Integer.valueOf(sec), Integer.valueOf(deg), Integer.valueOf(min), Integer.valueOf(sec));
                    actual = LatLonAltParser.parseLatLon(sb.toString());
                    expected = LatLonAlt.createFromDegrees(-ang % 90, -ang);
                    Assert.assertEquals(expected, actual);
                }
            }
        }
        formatter.close();
    }
}
