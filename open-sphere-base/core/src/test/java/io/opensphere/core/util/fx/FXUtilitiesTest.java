package io.opensphere.core.util.fx;

import javafx.scene.paint.Color;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link FXUtilities}. */
public class FXUtilitiesTest
{
    /** Test for color methods. */
    @Test
    public void testColors()
    {
        Assert.assertEquals(Color.WHITE, FXUtilities.fromAwtColor(java.awt.Color.WHITE));
        Assert.assertEquals(Color.RED, FXUtilities.fromAwtColor(java.awt.Color.RED));
        Assert.assertEquals(Color.LIME, FXUtilities.fromAwtColor(java.awt.Color.GREEN));
        Assert.assertEquals(Color.BLUE, FXUtilities.fromAwtColor(java.awt.Color.BLUE));
        Assert.assertEquals(Color.BLACK, FXUtilities.fromAwtColor(java.awt.Color.BLACK));

        Assert.assertEquals(java.awt.Color.WHITE, FXUtilities.toAwtColor(Color.WHITE));
        Assert.assertEquals(java.awt.Color.RED, FXUtilities.toAwtColor(Color.RED));
        Assert.assertEquals(java.awt.Color.GREEN, FXUtilities.toAwtColor(Color.LIME));
        Assert.assertEquals(java.awt.Color.BLUE, FXUtilities.toAwtColor(Color.BLUE));
        Assert.assertEquals(java.awt.Color.BLACK, FXUtilities.toAwtColor(Color.BLACK));
    }
}
