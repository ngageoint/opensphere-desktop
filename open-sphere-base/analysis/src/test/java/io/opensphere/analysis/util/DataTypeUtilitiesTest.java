package io.opensphere.analysis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Test;

/** Tests for {@link DataTypeUtilities}. */
public class DataTypeUtilitiesTest
{
    /** Test for {@link DataTypeUtilities#toDouble(Object)}. */
    @Test
    public void testToDouble()
    {
        assertEquals(1.2, DataTypeUtilities.toDouble(Double.valueOf(1.2)), 0.0000001);
        assertEquals(100, DataTypeUtilities.toDouble(new Date(100)), 0.0000001);
        assertEquals(0, DataTypeUtilities.toDouble("0"), 0.0000001);
        assertEquals(1.2, DataTypeUtilities.toDouble("1.2"), 0.0000001);
        try
        {
            DataTypeUtilities.toDouble("a");
            fail("Should not be able to parse 'a'");
        }
        catch (Exception e)
        {
        }
    }

    /** Test for {@link DataTypeUtilities#fromDouble(double, Class)}. */
    @Test
    public void testFromDouble()
    {
        assertEquals(Double.valueOf(1.2), DataTypeUtilities.fromDouble(1.2, Double.class));
        assertEquals(new Date(100), DataTypeUtilities.fromDouble(100, Date.class));
        assertEquals("0.0", DataTypeUtilities.fromDouble(0, String.class));
        assertEquals("1.2", DataTypeUtilities.fromDouble(1.2, String.class));
    }
}
