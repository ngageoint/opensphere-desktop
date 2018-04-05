package io.opensphere.core.common.util;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

import com.github.jaiimageio.impl.common.PaletteBuilder;

/**
 * The PaletteBuilder this class extends implements the octree quantization
 * method as it is described in the "Graphics Gems" (ISBN 0-12-286166-3, Chapter
 * 4, pages 297-293)
 */
public class PaletteBuilderSizable extends PaletteBuilder
{
    /**
     * Create the builder for the given image and palette size.
     *
     * @param src The source image for the builder.
     * @param paletteSize The maximum number of colors for the image.
     */
    public PaletteBuilderSizable(RenderedImage src, int paletteSize)
    {
        super(src, paletteSize);
        buildPalette();
    }

    /**
     * Get the color table and the pixel index into the table for the image with
     * which this builder was initialized.
     *
     * @param colorTab The array to fill with the color table entries.
     * @param indexedPixels The array to fill with the indexed values of the
     *            pixels into the color table.
     */
    public void getColorTableAndIndexedPixels(byte[] colorTab, byte[] indexedPixels)
    {
        IndexColorModel icm = getIndexColorModel();
        int[] rgbs = new int[currSize];
        icm.getRGBs(rgbs);
        for (int i = 0; i < rgbs.length; ++i)
        {
            Color col = new Color(rgbs[i], false);
            colorTab[i * 3] = (byte)col.getRed();
            colorTab[i * 3 + 1] = (byte)col.getGreen();
            colorTab[i * 3 + 2] = (byte)col.getBlue();
        }

        for (int x = 0; x < src.getWidth(); ++x)
        {
            for (int y = 0; y < src.getHeight(); ++y)
            {
                Color aColor = getSrcColor(x, y);
                indexedPixels[y * src.getWidth() + x] = (byte)findColorIndex(root, aColor);
            }
        }
    }

    @Override
    public RenderedImage getIndexedImage()
    {
        return super.getIndexedImage();
    }

    /**
     * Get the color for the given location in the source image.
     *
     * @param x The x coordinate location in the source image.
     * @param y The y coordinate location in the source image.
     * @return The color at the given location in the source image.
     */
    protected Color getSrcColor(int x, int y)
    {
        int argb = srcColorModel.getRGB(srcRaster.getDataElements(x, y, null));
        return new Color(argb, transparency != Transparency.OPAQUE);
    }
}
