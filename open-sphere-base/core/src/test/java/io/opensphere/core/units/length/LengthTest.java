package io.opensphere.core.units.length;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.units.InvalidUnitsException;

/** Tests for {@link Length}. */
public class LengthTest
{
    /**
     * Test for {@link Length#parse(String, String)}.
     *
     * @throws ParseException parse exception
     * @throws InvalidUnitsException units exception
     */
    @Test
    public void testParse() throws InvalidUnitsException, ParseException
    {
        Kilometers length = new Kilometers(5);
        Assert.assertEquals(length, Length.parse(length.getClass().getName(), length.getMagnitudeString()));
    }
}
