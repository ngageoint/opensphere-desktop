package io.opensphere.controlpanels.layers.activedata.controller;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.util.swing.input.model.ColorModel;

/** Tests the {@link OpacityColorAdapter}. */
public class OpacityColorAdapterTest
{
    /** Tests {@link OpacityColorAdapter}. */
    @Test
    public void testToOpacityPercent()
    {
        ColorModel colorModel = new ColorModel();
        colorModel.set(Color.BLACK);

        try (OpacityColorAdapter opacityModel = new OpacityColorAdapter(colorModel))
        {
            Assert.assertEquals(100, opacityModel.get().intValue());

            opacityModel.open();

            colorModel.set(Color.WHITE);
            Assert.assertEquals(100, opacityModel.get().intValue());

            colorModel.set(new Color(255, 255, 255, 0));
            Assert.assertEquals(0, opacityModel.get().intValue());

            colorModel.set(new Color(255, 255, 255, 127));
            Assert.assertEquals(50, opacityModel.get().intValue());

            opacityModel.set(Integer.valueOf(75));
            Assert.assertEquals(191, colorModel.get().getAlpha());
        }
    }
}
