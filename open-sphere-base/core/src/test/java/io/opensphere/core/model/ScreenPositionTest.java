package io.opensphere.core.model;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link ScreenPosition}. */
public class ScreenPositionTest
{
    /**
     * Test for {@link ScreenPosition#toSimpleString()} and
     * {@link ScreenPosition#fromSimpleString(String)}.
     */
    @Test
    public void testSimpleString()
    {
        ScreenPosition posIn = new ScreenPosition(1, 2);
        ScreenPosition posOut = ScreenPosition.fromSimpleString(posIn.toSimpleString());
        Assert.assertEquals(posIn.getX(), posOut.getX(), 0.0001);
        Assert.assertEquals(posIn.getY(), posOut.getY(), 0.0001);
    }
}
