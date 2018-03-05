package io.opensphere.analysis.heatmap;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/** Composite that adds color. */
public class AddComposite implements Composite
{
    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints)
    {
        return new AddCompositeContext();
    }

    /** CompositeContext that adds color. */
    static class AddCompositeContext implements CompositeContext
    {
        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
        {
            int width = Math.min(src.getWidth(), dstIn.getWidth());
            int height = Math.min(src.getHeight(), dstIn.getHeight());

            int[] srcPx = null;
            int[] dstPx = null;
            int srcInt;
            int dstInt;

            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    srcPx = src.getPixel(x, y, srcPx);
                    dstPx = dstIn.getPixel(x, y, dstPx);

                    srcInt = srcPx[0] << 16 | srcPx[1] << 8 | srcPx[2];
                    dstInt = dstPx[0] << 16 | dstPx[1] << 8 | dstPx[2];

                    dstInt = Math.min(srcInt + dstInt, 0x00FFFFFF);

                    dstOut.setPixel(x, y, new int[] { dstInt >> 16 & 0xFF, dstInt >> 8 & 0xFF, dstInt & 0xFF, 0 });
                }
            }
        }

        @Override
        public void dispose()
        {
        }
    }
}
