package io.opensphere.core.util.awt;

import java.awt.Font;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link AWTUtilities}. */
public class AWTUtilitiesTest
{
    /** Test {@link AWTUtilities#encode(java.awt.Font)}. */
    @Test
    public void testEncode()
    {
        Font font = new Font("Arial", Font.BOLD + Font.ITALIC, 14);
        Assert.assertEquals(font, Font.decode(AWTUtilities.encode(font)));
    }
}
