package io.opensphere.core.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link ColorArrayList}.
 */
public class ColorArrayListTest
{
    /**
     * Test the collection.
     */
    @Test
    public void test()
    {
        Color color1 = new Color(.7f, .2f, .4f, .9f);
        Color color2 = new Color(.1f, .2f, .5f, 1f);
        Color color3 = new Color(.4f, .7f, .8f, .2f);
        Color color4 = new Color(.8f, .1f, .2f, 0f);

        Collection<Color> col = new ArrayList<>();
        col.add(color1);
        col.add(color2);
        col.add(color3);
        col.add(color4);

        ColorArrayList cal = ColorArrayList.getColorArrayList(col);

        Assert.assertEquals(color1, cal.get(0));
        Assert.assertEquals(color2, cal.get(1));
        Assert.assertEquals(color3, cal.get(2));
        Assert.assertEquals(color4, cal.get(3));

        Assert.assertEquals(color1.getRGB(), cal.getARGB(0));
        Assert.assertEquals(color2.getRGB(), cal.getARGB(1));
        Assert.assertEquals(color3.getRGB(), cal.getARGB(2));
        Assert.assertEquals(color4.getRGB(), cal.getARGB(3));

        byte[] bytes = cal.getRGBABytes();

        Assert.assertEquals(16, bytes.length);
        Assert.assertEquals((byte)179, bytes[0]);
        Assert.assertEquals((byte)51, bytes[1]);
        Assert.assertEquals((byte)102, bytes[2]);
        Assert.assertEquals((byte)230, bytes[3]);
        Assert.assertEquals((byte)26, bytes[4]);
        Assert.assertEquals((byte)51, bytes[5]);
        Assert.assertEquals((byte)128, bytes[6]);
        Assert.assertEquals((byte)255, bytes[7]);
        Assert.assertEquals((byte)102, bytes[8]);
        Assert.assertEquals((byte)179, bytes[9]);
        Assert.assertEquals((byte)204, bytes[10]);
        Assert.assertEquals((byte)51, bytes[11]);
        Assert.assertEquals((byte)204, bytes[12]);
        Assert.assertEquals((byte)26, bytes[13]);
        Assert.assertEquals((byte)51, bytes[14]);
        Assert.assertEquals((byte)0, bytes[15]);
    }
}
