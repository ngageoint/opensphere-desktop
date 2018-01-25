package io.opensphere.core.common.dds;

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* This code is a pure Java implementation of the DXT compression algorithm
 * described in the paper "Real-Time DXT Compression" dated May 20th 2006 by
 * J.M.P. van Waveren (c) Id Software, Inc.
 *
 * The following copy notice is included in their paper, and likely applies to
 * some parts:
 *
 * "This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details."
 *
 * Note that due to the fact that Java's strong type protection and its
 * insistence upon signed primitives makes much of the below code completely
 * indistinguishable from the original paper. */

public class DDSEncoder
{
    // RGBA
    int first = 0;

    int second = 1;

    int third = 2;

    int fourth = 3;

    private boolean myRGBA = true;

    public DDSEncoder()
    {
        // TODO Auto-generated constructor stub
    }

    public static final int DDS_HEADER_SIZE = 128;

    public enum CompressionType
    {
        DXT1, DXT1_Transparent, DXT5, UNCOMPRESSED
    };

    // DANGER, LITTLE ENDIAN ASSUMED!
    private byte[] makeBytes(short source)
    {
        return new byte[] { (byte)source, (byte)(source >> 8) };
    }

    private byte[] makeBytes(int source)
    {
        return new byte[] { (byte)source, (byte)(source >> 8), (byte)(source >> 16), (byte)(source >> 24) };
    }

    public void setABGR()
    {
        setColorOrder(3, 2, 1, 0);
    }

    public void setBGR()
    {
        setColorOrder(2, 1, 0);
    }

    public void setColorOrder(int red, int green, int blue)
    {
        setColorOrder(red, green, blue, 3);
        // Alpha intentionally left alone, do not want to screw up
        // the extractBlock_noAlpha function.
    }

    public void setColorOrder(int red, int green, int blue, int alpha)
    {
        first = red;
        second = green;
        third = blue;
        fourth = alpha;
        myRGBA = first == 0 && second == 1 && third == 2 && fourth == 3;
    }

    /**
     * This routine sacrifices performance for a bit of safety. Recommend using
     * when you cannot guarantee that the image is in the RGB(a) colorspace
     *
     * @param image The image to be encoded
     * @param buildHeader true = return the full DDS file false = return just
     *            the DXTn encoded pixel data
     * @return a bytebuffer with either the full DDS file or the DXTn endcoded
     *         bytes
     */
    public ByteBuffer encodeDDS_safe(BufferedImage image, boolean buildHeader)
    {
        ByteBuffer returnBuffer = null;
        CompressionType type = null;
        // In ARGB speak
        int imageSize = image.getHeight() * image.getWidth() * 4;
        if (image.getTransparency() == Transparency.TRANSLUCENT)
        {
            type = CompressionType.DXT5;
            returnBuffer = ByteBuffer.allocate((imageSize >> 2) + (buildHeader ? DDS_HEADER_SIZE : 0));
        }
        else if (image.getTransparency() == Transparency.BITMASK)
        {
            type = CompressionType.DXT1_Transparent;
            returnBuffer = ByteBuffer.allocate((imageSize >> 3) + (buildHeader ? DDS_HEADER_SIZE : 0));
        }
        else if (image.getTransparency() == Transparency.OPAQUE)
        {
            type = CompressionType.DXT1;
            returnBuffer = ByteBuffer.allocate((imageSize >> 3) + (buildHeader ? DDS_HEADER_SIZE : 0));
        }
        else
        {
            return null;
        }

        returnBuffer.order(ByteOrder.LITTLE_ENDIAN);
        if (buildHeader)
        {
            if (type == CompressionType.DXT1 || type == CompressionType.DXT1_Transparent)
            {
                buildHeaderDxt1(image.getWidth(), image.getHeight(), returnBuffer);
            }
            else
            {
                buildHeaderDxt5(image.getWidth(), image.getHeight(), returnBuffer);
            }
        }

        compressImageDXTn_safe(image, type, returnBuffer);
        returnBuffer.flip();

        return returnBuffer;
    }

    public void encodeDDS(int width, int height, CompressionType type, byte[] inRaster, OutputStream outFile) throws IOException
    {
        if (type == CompressionType.DXT1)
        {
            // Not worried about the overhead of copying the 128 byte header,
            // just the quarter-MB image data.
            ByteBuffer buffer = ByteBuffer.allocate(DDS_HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buildHeaderDxt1(width, height, buffer);
            outFile.write(buffer.array());
            compressImageDXT1(width, height, inRaster, outFile);
        }
        else if (type == CompressionType.DXT1_Transparent)
        {
            // Not worried about the overhead of copying the 128 byte header,
            // just the quarter-MB image data.
            ByteBuffer buffer = ByteBuffer.allocate(DDS_HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buildHeaderDxt1(width, height, buffer);
            outFile.write(buffer.array());
            compressImageDXT1_alpha(width, height, inRaster, outFile);
        }
        else if (type == CompressionType.DXT5)
        {
            // Not worried about the overhead of copying the 128 byte header,
            // just the quarter-MB image data.
            ByteBuffer buffer = ByteBuffer.allocate(DDS_HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buildHeaderDxt5(width, height, buffer);
            outFile.write(buffer.array());
            compressImageDXT5(width, height, inRaster, outFile);
        }
        else if (type == CompressionType.UNCOMPRESSED)
        {
            // Not worried about the overhead of copying the 128 byte header,
            // just the quarter-MB image data.
            ByteBuffer buffer = ByteBuffer.allocate(DDS_HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            boolean hasAlpha = inRaster.length / height / width * 8 == 32;
            buildHeaderUncompressed(width, height, hasAlpha, buffer);
            outFile.write(buffer.array());
            writeImageUncompressed(width, height, inRaster, outFile);
        }
    }

    public void encodeDDS(int width, int height, CompressionType type, byte[] inRaster, ByteBuffer outFile)
    {
        if (type == CompressionType.DXT1)
        {
            buildHeaderDxt1(width, height, outFile);
            compressImageDXT1(width, height, inRaster, outFile);
        }
        else if (type == CompressionType.DXT1_Transparent)
        {
            buildHeaderDxt1(width, height, outFile);
            compressImageDXT1_alpha(width, height, inRaster, outFile);
        }
        else if (type == CompressionType.DXT5)
        {
            buildHeaderDxt5(width, height, outFile);
            compressImageDXT5(width, height, inRaster, outFile);
        }
        else if (type == CompressionType.UNCOMPRESSED)
        {
            boolean hasAlpha = inRaster.length / height / width * 8 == 32;
            buildHeaderUncompressed(width, height, hasAlpha, outFile);
            writeImageUncompressed(width, height, inRaster, outFile);
        }
    }

    public void encodeDDS(int width, int height, CompressionType type, byte[] inRaster, ByteBuffer outFile, boolean buildHeader)
    {
        if (buildHeader)
        {
            encodeDDS(width, height, type, inRaster, outFile);
        }
        else
        {
            if (type == CompressionType.DXT1)
            {
                compressImageDXT1(width, height, inRaster, outFile);
            }
            else if (type == CompressionType.DXT1_Transparent)
            {
                compressImageDXT1_alpha(width, height, inRaster, outFile);
            }
            else if (type == CompressionType.DXT5)
            {
                compressImageDXT5(width, height, inRaster, outFile);
            }
            else if (type == CompressionType.UNCOMPRESSED)
            {
                writeImageUncompressed(width, height, inRaster, outFile);
            }
        }
    }

    private void extractBlock_safe(BufferedImage image, int[][] colorBlock, int[] originalPixels, int x, int y)
    {
        // A bufferedImage safe version of the two methods below
        image.getRGB(x, y, 4, 4, originalPixels, 0, 4);

        for (int i = 0; i < 16; i++)
        {
            // From JAD, the above call returns:
            // getAlpha(obj) << 24 | getRed(obj) << 16 | getGreen(obj) << 8 |
            // getBlue(obj) << 0;
            colorBlock[i][2] = 0xFF & (byte)originalPixels[i];
            colorBlock[i][1] = 0xFF & (byte)(originalPixels[i] >> 8);
            colorBlock[i][0] = 0xFF & (byte)(originalPixels[i] >> 16);
            colorBlock[i][3] = 0xFF & (byte)(originalPixels[i] >> 24);
        }

    }

    private void extractBlock(byte[] inBuffer, int[][] colorBlock, int position, int width)
    {
        // Original C++ source did a lot of looking at the byte
        // buffer in place. This is almost impossible to do
        // in Java without a copy. Since I'm copying, I'm
        // putting into an 'int' rather than 'byte' so that
        // I'm not simultaneously screwed by the whole
        // "signed byte" nonsense in java.
        //
        for (int i = 0; i < 4; i++) // 4 rows
        {
            for (int j = 0; j < 4; j++) // 4 Cols
            {
                // copy the 4 bytes of rgba into their own ints
                // need to do it this way as simple assignment will move the
                // sign bit.
                colorBlock[i * 4 + j][first] = 0xFF & inBuffer[position++];
                colorBlock[i * 4 + j][second] = 0xFF & inBuffer[position++];
                colorBlock[i * 4 + j][third] = 0xFF & inBuffer[position++];
                colorBlock[i * 4 + j][fourth] = 0xFF & inBuffer[position++];
                // columns are in sequence
            }
            // Push buffer to right "row", each is "width" apart
            position += width * 4 - 16;
        }
    }

    private void extractBlock_noAlpha(byte[] inBuffer, int[][] colorBlock, int position, int width)
    {
        // Same as extractBlock, copied in order to hope to avoid
        // a million+ "if" comparisons and the branch mispredictions that
        // would ensue.
        for (int i = 0; i < 4; i++) // 4 rows
        {
            for (int j = 0; j < 4; j++) // 4 Cols
            {
                // copy the 3 bytes of rgb into their own ints
                colorBlock[i * 4 + j][first] = 0xFF & inBuffer[position++];
                colorBlock[i * 4 + j][second] = 0xFF & inBuffer[position++];
                colorBlock[i * 4 + j][third] = 0xFF & inBuffer[position++];
                colorBlock[i * 4 + j][fourth] = 255;
                // columns are in sequence
            }
            // Push buffer to right "row", each is "width" apart
            position += width * 3 - 12;
        }
    }

    private short colorTo565(int[] color)
    {
        // Back out of the Java friendly int world, and into the uber packed dxt
        // world. 3 colors in 16bits!
        return (short)(color[0] >> 3 << 11 | color[1] >> 2 << 5 | color[2] >> 3);
    }

    private int colorTo565Int(int[] color)
    {
        // Just in case you need the 5:6:5 value as an unsigned value
        return color[0] >> 3 << 11 | color[1] >> 2 << 5 | color[2] >> 3;
    }

    /**
     * @deprecated The performance of Java's branch predictions makes the
     *             luminance function about the same speed. Since the luminance
     *             function works a ton better, consider this function
     *             deprecated.
     */
    @Deprecated
    void getMinMaxColors_dxt1(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        int inset0, inset1, inset2;
        // Maybe try 0xFF for safety
        minColor[0] = minColor[1] = minColor[2] = 255;
        maxColor[0] = maxColor[1] = maxColor[2] = 0;
        for (int i = 0; i < 16; i++)
        {
            if (colorBlock[i][0] < minColor[0])
            {
                minColor[0] = colorBlock[i][0];
            }
            if (colorBlock[i][1] < minColor[1])
            {
                minColor[1] = colorBlock[i][1];
            }
            if (colorBlock[i][2] < minColor[2])
            {
                minColor[2] = colorBlock[i][2];
            }
            if (colorBlock[i][0] > maxColor[0])
            {
                maxColor[0] = colorBlock[i][0];
            }
            if (colorBlock[i][1] > maxColor[1])
            {
                maxColor[1] = colorBlock[i][1];
            }
            if (colorBlock[i][2] > maxColor[2])
            {
                maxColor[2] = colorBlock[i][2];
            }
        }
        // INSET_SHIFT = 4
        inset0 = maxColor[0] - minColor[0] >> 4;
        inset1 = maxColor[1] - minColor[1] >> 4;
        inset2 = maxColor[2] - minColor[2] >> 4;

        minColor[0] = minColor[0] + inset0 <= 255 ? minColor[0] + inset0 : 255;
        minColor[1] = minColor[1] + inset1 <= 255 ? minColor[1] + inset1 : 255;
        minColor[2] = minColor[2] + inset2 <= 255 ? minColor[2] + inset2 : 255;
        maxColor[0] = maxColor[0] >= inset0 ? maxColor[0] - inset0 : 0;
        maxColor[1] = maxColor[1] >= inset1 ? maxColor[1] - inset1 : 0;
        maxColor[2] = maxColor[2] >= inset2 ? maxColor[2] - inset2 : 0;
    }

    // Yes, I know that we could use the same function for both Dxt1 and Dxt5...
    // Right now, I'm semi-paranoid about the extra couple thousand instructions
    // per
    // image.
    /**
     * @deprecated The performance of Java's branch predictions makes the
     *             luminance function about the same speed. Since the luminance
     *             function works a ton better, consider this function
     *             deprecated.
     */
    @Deprecated
    void getMinMaxColors_dxt5(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        int inset0, inset1, inset2;
        // Maybe try 0xFF for safety
        minColor[0] = minColor[1] = minColor[2] = minColor[3] = 255;
        maxColor[0] = maxColor[1] = maxColor[2] = maxColor[3] = 0;

        for (int i = 0; i < 16; ++i)
        {
            if (colorBlock[i][0] < minColor[0])
            {
                minColor[0] = colorBlock[i][0];
            }
            if (colorBlock[i][1] < minColor[1])
            {
                minColor[1] = colorBlock[i][1];
            }
            if (colorBlock[i][2] < minColor[2])
            {
                minColor[2] = colorBlock[i][2];
            }
            if (colorBlock[i][3] < minColor[3])
            {
                minColor[3] = colorBlock[i][3];
            }
            if (colorBlock[i][0] > maxColor[0])
            {
                maxColor[0] = colorBlock[i][0];
            }
            if (colorBlock[i][1] > maxColor[1])
            {
                maxColor[1] = colorBlock[i][1];
            }
            if (colorBlock[i][2] > maxColor[2])
            {
                maxColor[2] = colorBlock[i][2];
            }
            if (colorBlock[i][3] > maxColor[3])
            {
                maxColor[3] = colorBlock[i][3];
            }
        }

        // INSET_SHIFT = 4

        inset0 = maxColor[0] - minColor[0] >> 4;
        inset1 = maxColor[1] - minColor[1] >> 4;
        inset2 = maxColor[2] - minColor[2] >> 4;
        // inset[3] = (( maxColor[3] - minColor[3] ) >> 4);
        // PRD - Not inseting the alpha, because doing so is retarded.

        minColor[0] = minColor[0] + inset0 <= 255 ? minColor[0] + inset0 : 255;
        minColor[1] = minColor[1] + inset1 <= 255 ? minColor[1] + inset1 : 255;
        minColor[2] = minColor[2] + inset2 <= 255 ? minColor[2] + inset2 : 255;
        // minColor[3] = (( minColor[3] + inset[3] <= 255 ) ? minColor[3] +
        // inset[3] : 255);
        maxColor[0] = maxColor[0] >= inset0 ? maxColor[0] - inset0 : 0;
        maxColor[1] = maxColor[1] >= inset1 ? maxColor[1] - inset1 : 0;
        maxColor[2] = maxColor[2] >= inset2 ? maxColor[2] - inset2 : 0;
        // maxColor[3] = (( maxColor[3] >= inset[3] ) ? maxColor[3] - inset[3] :
        // 0);

    }

    void getMinMaxColors_luminance(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        float maxLuminance = Float.MIN_VALUE;
        float minLuminance = Float.MAX_VALUE;
        maxColor[0] = 0;
        maxColor[1] = 0;
        maxColor[2] = 0;
        maxColor[3] = 0;
        minColor[0] = 255;
        minColor[1] = 255;
        minColor[2] = 255;
        minColor[3] = 255;

        // While the luminance function in the paper performs significantly
        // better,
        // it doesn't deal with DXT5, so I'm freestylin' on the way alpha is
        // handled

        for (int i = 0; i < 16; ++i)
        {
            // This equation was copied from the paper. For anyone trying to
            // figure out why it
            // doesn't match the standard conversions, its taken from the YUV
            // conversion:
            // Y = .299r + .587g + .114b.
            // Since scale doesn't matter, calculations can be avoided by
            // approximating to
            // 1/4r + 1/2g + 1/8b and then scaled by 4 to r + g*2 + b/2
            // I'd like to figure out a way to reliably take alpha into account
            // here, as
            // geoserver has a habit of sending "tranparent white" and "mostly
            // white,
            // mostly transparent" pixels. The alpha check accounts for the
            // former,
            // but the latter needs to be handled here somehow.
            float luminance = colorBlock[i][0] + colorBlock[i][1] * 2 + colorBlock[i][2] / 2;

            if (colorBlock[i][3] > 0)
            {
                // Transparent pixels shouldn't affect the min/max colors

                // I hate that I'm branching two layers deep here, but I don't
                // really
                // have a good alternative plan (yet)
                if (luminance > maxLuminance)
                {
                    maxLuminance = luminance;
                    maxColor[0] = colorBlock[i][0];
                    maxColor[1] = colorBlock[i][1];
                    maxColor[2] = colorBlock[i][2];
                }
                if (luminance < minLuminance)
                {
                    minLuminance = luminance;
                    minColor[0] = colorBlock[i][0];
                    minColor[1] = colorBlock[i][1];
                    minColor[2] = colorBlock[i][2];
                }
            }
            if (colorBlock[i][3] > maxColor[3])
            {
                maxColor[3] = colorBlock[i][3];
            }
            if (colorBlock[i][3] < minColor[3])
            {
                minColor[3] = colorBlock[i][3];
            }
        }

        if (colorTo565Int(maxColor) < colorTo565Int(minColor))
        {
            int[] temp = minColor.clone();
            minColor[0] = maxColor[0];
            minColor[1] = maxColor[1];
            minColor[2] = maxColor[2];
            maxColor[0] = temp[0];
            maxColor[1] = temp[1];
            maxColor[2] = temp[2];
        }
    }

    private static final int C565_5_MASK = 0xF8;

    private static final int C565_6_MASK = 0xFC;

    int getColorIndices(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        int result = 0;
        int colors0r = maxColor[0] & C565_5_MASK | maxColor[0] >> 5;
        int colors0g = maxColor[1] & C565_6_MASK | maxColor[1] >> 6;
        int colors0b = maxColor[2] & C565_5_MASK | maxColor[2] >> 5;
        int colors1r = minColor[0] & C565_5_MASK | minColor[0] >> 5;
        int colors1g = minColor[1] & C565_6_MASK | minColor[1] >> 6;
        int colors1b = minColor[2] & C565_5_MASK | minColor[2] >> 5;
        int colors2r = (2 * colors0r + 1 * colors1r) / 3;
        int colors2g = (2 * colors0g + 1 * colors1g) / 3;
        int colors2b = (2 * colors0b + 1 * colors1b) / 3;
        int colors3r = (1 * colors0r + 2 * colors1r) / 3;
        int colors3g = (1 * colors0g + 2 * colors1g) / 3;
        int colors3b = (1 * colors0b + 2 * colors1b) / 3;
        for (int i = 15; i >= 0; --i)
        {
            int c0 = colorBlock[i][0];
            int c1 = colorBlock[i][1];
            int c2 = colorBlock[i][2];
            // Math.abs is slow, and introduces a branch on each call
            /* int d0 = Math.abs( colors0r - c0 ) + Math.abs( colors0g - c1 ) +
             * Math.abs( colors0b - c2 ); int d1 = Math.abs( colors1r - c0 ) +
             * Math.abs( colors1g - c1 ) + Math.abs( colors1b - c2 ); int d2 =
             * Math.abs( colors2r - c0 ) + Math.abs( colors2g - c1 ) + Math.abs(
             * colors2b - c2 ); int d3 = Math.abs( colors3r - c0 ) + Math.abs(
             * colors3g - c1 ) + Math.abs( colors3b - c2 ); */
            int d0r = colors0r - c0;
            int d0g = colors0g - c1;
            int d0b = colors0b - c2;
            d0r = (d0r ^ d0r >> 31) - (d0r >> 31);
            d0g = (d0g ^ d0g >> 31) - (d0g >> 31);
            d0b = (d0b ^ d0b >> 31) - (d0b >> 31);
            int d0 = d0r + d0g + d0b;
            int d1r = colors1r - c0;
            int d1g = colors1g - c1;
            int d1b = colors1b - c2;
            d1r = (d1r ^ d1r >> 31) - (d1r >> 31);
            d1g = (d1g ^ d1g >> 31) - (d1g >> 31);
            d1b = (d1b ^ d1b >> 31) - (d1b >> 31);
            int d1 = d1r + d1g + d1b;
            int d2r = colors2r - c0;
            int d2g = colors2g - c1;
            int d2b = colors2b - c2;
            d2r = (d2r ^ d2r >> 31) - (d2r >> 31);
            d2g = (d2g ^ d2g >> 31) - (d2g >> 31);
            d2b = (d2b ^ d2b >> 31) - (d2b >> 31);
            int d2 = d2r + d2g + d2b;
            int d3r = colors3r - c0;
            int d3g = colors3g - c1;
            int d3b = colors3b - c2;
            d3r = (d3r ^ d3r >> 31) - (d3r >> 31);
            d3g = (d3g ^ d3g >> 31) - (d3g >> 31);
            d3b = (d3b ^ d3b >> 31) - (d3b >> 31);
            int d3 = d3r + d3g + d3b;
            /* int b0 = d0 > d3 ? 1 : 0; int b1 = d1 > d2 ? 1 : 0; int b2 = d0 >
             * d2 ? 1 : 0; int b3 = d1 > d3 ? 1 : 0; int b4 = d2 > d3 ? 1 : 0; */
            // Java's insistence that a comparison operator returns a logical
            // boolean and not
            // an integer causes the above code, which has a ton of branches and
            // mis-prediction
            // opportunities. The below code, while difficult to read,
            // accomplishes the same
            // without branching
            int b0 = d3 - d0 >> 31 & 0x1;
            int b1 = d2 - d1 >> 31 & 0x1;
            int b2 = d2 - d0 >> 31 & 0x1;
            int b3 = d3 - d1 >> 31 & 0x1;
            int b4 = d3 - d2 >> 31 & 0x1;
            int x0 = b1 & b2;
            int x1 = b0 & b3;
            int x2 = b0 & b4;
            result |= (x2 | (x0 | x1) << 1) << (i << 1);
        }

        return result;
    }

    int getColorIndices_dxt1_alpha(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        int result = 0;

        // Basic difference between DXT1 alpha and DXTn is:
        // (a) the min and max are swapped in the block to trigger this behavior
        // so 00 is now max and 01 is now min. (10 is still in between)
        // (b) the topmost bit pair (11) is used as transparent

        // 5:5:5:1 model, not 5:6:5
        // Min and Max swapped. If you understand the algorithm, this helps
        // you keep your brain on straight. (well, at least mine)
        int colors0r = minColor[0] & C565_5_MASK | minColor[0] >> 5;
        int colors0g = minColor[1] & C565_5_MASK | minColor[1] >> 5;
        int colors0b = minColor[2] & C565_5_MASK | minColor[2] >> 5;
        int colors1r = maxColor[0] & C565_5_MASK | maxColor[0] >> 5;
        int colors1g = maxColor[1] & C565_5_MASK | maxColor[1] >> 5;
        int colors1b = maxColor[2] & C565_5_MASK | maxColor[2] >> 5;

        // Only 3 colors encoded, not 4
        // / 2;
        int colors2r = colors0r + colors1r >> 1;
        // / 2;
        int colors2g = colors0g + colors1g >> 1;
        // / 2;
        int colors2b = colors0b + colors1b >> 1;

        for (int i = 15; i >= 0; --i)
        {
            int c0 = colorBlock[i][0];
            int c1 = colorBlock[i][1];
            int c2 = colorBlock[i][2];
            // Math.abs is slow, and introduces a branch on each call
            /* int d0 = Math.abs( colors0r - c0 ) + Math.abs( colors0g - c1 ) +
             * Math.abs( colors0b - c2 ); int d1 = Math.abs( colors1r - c0 ) +
             * Math.abs( colors1g - c1 ) + Math.abs( colors1b - c2 ); int d2 =
             * Math.abs( colors2r - c0 ) + Math.abs( colors2g - c1 ) + Math.abs(
             * colors2b - c2 ); */
            int d0r = colors0r - c0;
            int d0g = colors0g - c1;
            int d0b = colors0b - c2;
            d0r = (d0r ^ d0r >> 31) - (d0r >> 31);
            d0g = (d0g ^ d0g >> 31) - (d0g >> 31);
            d0b = (d0b ^ d0b >> 31) - (d0b >> 31);
            int d0 = d0r + d0g + d0b;
            int d1r = colors1r - c1;
            int d1g = colors1g - c1;
            int d1b = colors1b - c2;
            d1r = (d1r ^ d1r >> 31) - (d1r >> 31);
            d1g = (d1g ^ d1g >> 31) - (d1g >> 31);
            d1b = (d1b ^ d1b >> 31) - (d1b >> 31);
            int d1 = d1r + d1g + d1b;
            int d2r = colors2r - c0;
            int d2g = colors2g - c1;
            int d2b = colors2b - c2;
            d2r = (d2r ^ d2r >> 31) - (d2r >> 31);
            d2g = (d2g ^ d2g >> 31) - (d2g >> 31);
            d2b = (d2b ^ d2b >> 31) - (d2b >> 31);
            int d2 = d2r + d2g + d2b;
            int d3 = c0 | c1 | c2;
            /* int b0 = d0 > d2 ? 1 : 0; int b1 = d1 > d2 ? 1 : 0; int b2 = d0 >
             * d1 ? 1 : 0; */
            // Java's insistence that a comparison operator returns a logical
            // boolean and not
            // an integer causes the above code, which has a ton of branches and
            // mis-prediction
            // opportunities. The below code, while difficult to read,
            // accomplishes the same
            // without branching
            int b0 = d2 - d0 >> 31 & 0x1;
            int b1 = d2 - d1 >> 31 & 0x1;
            int b2 = d1 - d0 >> 31 & 0x1;
            int x0 = b2 & (b1 ^ b2);
            int x1 = b0 & b1;
            // all three numbers are zero
            int x2 = d3 == 0 ? 1 : 0;
            result |= (x0 | x2 | (x1 | x2) << 1) << (i << 1);
        }
        return result;
    }

    void getAlphaIndices(int[][] colorBlock, int[] indices, int minAlpha, int maxAlpha)
    {
        int mid = (maxAlpha - minAlpha) / (2 * 7);

        int ab1 = minAlpha + mid;
        int ab2 = (6 * maxAlpha + 1 * minAlpha) / 7 + mid;
        int ab3 = (5 * maxAlpha + 2 * minAlpha) / 7 + mid;
        int ab4 = (4 * maxAlpha + 3 * minAlpha) / 7 + mid;
        int ab5 = (3 * maxAlpha + 4 * minAlpha) / 7 + mid;
        int ab6 = (2 * maxAlpha + 5 * minAlpha) / 7 + mid;
        int ab7 = (1 * maxAlpha + 6 * minAlpha) / 7 + mid;

        for (int i = 0; i < 16; i++)
        {
            int a = colorBlock[i][3];
            /* int b1 = a <= ab1 ? 1 : 0; int b2 = a <= ab2 ? 1 : 0; int b3 = a
             * <= ab3 ? 1 : 0; int b4 = a <= ab4 ? 1 : 0; int b5 = a <= ab5 ? 1
             * : 0; int b6 = a <= ab6 ? 1 : 0; int b7 = a <= ab7 ? 1 : 0; */
            // Java's insistence that a comparison operator returns a logical
            // boolean and not
            // an integer causes the above code, which has a ton of branches and
            // mis-prediction
            // opportunities. The below code, while difficult to read,
            // accomplishes the same
            // without branching
            int b1 = a - ab1 - 1 >> 31 & 0x1;
            int b2 = a - ab2 - 1 >> 31 & 0x1;
            int b3 = a - ab3 - 1 >> 31 & 0x1;
            int b4 = a - ab4 - 1 >> 31 & 0x1;
            int b5 = a - ab5 - 1 >> 31 & 0x1;
            int b6 = a - ab6 - 1 >> 31 & 0x1;
            int b7 = a - ab7 - 1 >> 31 & 0x1;
            int index = b1 + b2 + b3 + b4 + b5 + b6 + b7 + 1 & 7;
            indices[i] = index ^ (2 > index ? 1 : 0);
        }
    }

    public void compressImageDXTn_safe(BufferedImage image, CompressionType type, ByteBuffer returnBuffer)
    {
        int[] minColor = new int[4];
        int[] maxColor = new int[4];

        int width = image.getWidth();
        int height = image.getHeight();

        // (row x column) x rgba

        int[][] block = new int[16][4];

        // Performance HACK... Allocate this array up here to
        // avoid a million allocates/de-allocates
        int[] originalPixels = new int[16];
        // I suppose if I was really paranoid I could reuse the above...
        int[] indices = new int[16];

        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                extractBlock_safe(image, block, originalPixels, i, j);
                // Since the extract block is getting handed a 4 byte pixel in
                // ARGB space, we can use DXT5 method even if we decide later
                // that we don't need alpha
                getMinMaxColors_luminance(block, minColor, maxColor);

                if (type == CompressionType.DXT5)
                {
                    // alpha channels
                    returnBuffer.put((byte)maxColor[3]);
                    returnBuffer.put((byte)minColor[3]);

                    // Little ugly here, sorry! Unlike how it is written in the
                    // source paper, I like keeping
                    // all of the byte buffer accesses in the same loop in case
                    // we get out of whack.
                    getAlphaIndices(block, indices, minColor[3], maxColor[3]);
                    returnBuffer.put((byte)(indices[0] >> 0 | indices[1] << 3 | indices[2] << 6));
                    returnBuffer.put((byte)(indices[2] >> 2 | indices[3] << 1 | indices[4] << 4 | indices[5] << 7));
                    returnBuffer.put((byte)(indices[5] >> 1 | indices[6] << 2 | indices[7] << 5));
                    returnBuffer.put((byte)(indices[8] >> 0 | indices[9] << 3 | indices[10] << 6));
                    returnBuffer.put((byte)(indices[10] >> 2 | indices[11] << 1 | indices[12] << 4 | indices[13] << 7));
                    returnBuffer.put((byte)(indices[13] >> 1 | indices[14] << 2 | indices[15] << 5));

                }
                if (type == CompressionType.DXT1_Transparent)
                {
                    returnBuffer.putShort(colorTo565(minColor));
                    returnBuffer.putShort(colorTo565(maxColor));
                    returnBuffer.putInt(getColorIndices_dxt1_alpha(block, minColor, maxColor));
                }
                else
                {
                    returnBuffer.putShort(colorTo565(maxColor));
                    returnBuffer.putShort(colorTo565(minColor));
                    returnBuffer.putInt(getColorIndices(block, minColor, maxColor));
                }

            }
        }
    }

    public void compressImageDXT1(int width, int height, byte[] rgba, byte[] dxt1Array)
    {
        ByteBuffer dxt1 = ByteBuffer.wrap(dxt1Array);
        dxt1.order(ByteOrder.LITTLE_ENDIAN);
        compressImageDXT1(width, height, rgba, dxt1);
    }

    public void compressImageDXT1(int width, int height, byte[] rgba, ByteBuffer dxt1)
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];

        // protect against a missing alpha channel
        int pixelSize = rgba.length / (width * height);

        // (row x column) x rgba

        int[][] block = new int[16][4];

        // Setting up if OUTSIDE the looping to hope for best
        // branch predicting and best performance.
        if (pixelSize == 3)
        {
            int firstByteInRow = 0;
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock_noAlpha(rgba, block, firstByteInRow + i * pixelSize, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);
                    dxt1.putShort(colorTo565(maxColor));
                    dxt1.putShort(colorTo565(minColor));
                    dxt1.putInt(getColorIndices(block, minColor, maxColor));
                }
                firstByteInRow += width * pixelSize * 4;
            }
        }
        else
        {
            int firstByteInRow = 0;
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock(rgba, block, firstByteInRow + i * 4, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);
                    dxt1.putShort(colorTo565(maxColor));
                    dxt1.putShort(colorTo565(minColor));
                    dxt1.putInt(getColorIndices(block, minColor, maxColor));
                }
                firstByteInRow += width * 4 * 4;
            }
        }
    }

    public void compressImageDXT1(int width, int height, byte[] rgba, OutputStream dxt1) throws IOException
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];

        // protect against a missing alpha channel
        int pixelSize = rgba.length / (width * height);

        // (row x column) x rgba

        int[][] block = new int[16][4];

        // Setting up if OUTSIDE the looping to hope for best
        // branch predicting and best performance.
        if (pixelSize == 3)
        {
            int firstByteInRow = 0;
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock_noAlpha(rgba, block, firstByteInRow + i * pixelSize, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);
                    dxt1.write(makeBytes(colorTo565(maxColor)));
                    dxt1.write(makeBytes(colorTo565(minColor)));
                    dxt1.write(makeBytes(getColorIndices(block, minColor, maxColor)));
                }
                firstByteInRow += width * pixelSize * 4;
            }
        }
        else
        {
            int firstByteInRow = 0;
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock(rgba, block, firstByteInRow + i * 4, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);
                    dxt1.write(makeBytes(colorTo565(maxColor)));
                    dxt1.write(makeBytes(colorTo565(minColor)));
                    dxt1.write(makeBytes(getColorIndices(block, minColor, maxColor)));
                }
                firstByteInRow += width * 4 * 4;
            }
        }
    }

    public void compressImageDXT1_alpha(int width, int height, byte[] rgba, byte[] dxt1Array)
    {
        ByteBuffer dxt1 = ByteBuffer.wrap(dxt1Array);
        dxt1.order(ByteOrder.LITTLE_ENDIAN);
        compressImageDXT1_alpha(width, height, rgba, dxt1);
    }

    public void compressImageDXT1_alpha(int width, int height, byte[] rgba, ByteBuffer dxt1)
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int firstByteInRow = 0;
        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                extractBlock(rgba, block, firstByteInRow + i * 4, width);
                getMinMaxColors_luminance(block, minColor, maxColor);
                dxt1.putShort(colorTo565(minColor));
                dxt1.putShort(colorTo565(maxColor));
                dxt1.putInt(getColorIndices_dxt1_alpha(block, minColor, maxColor));
            }
            firstByteInRow += width * 4 * 4;
        }
    }

    public void compressImageDXT1_alpha(int width, int height, byte[] rgba, OutputStream dxt1) throws IOException
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int firstByteInRow = 0;
        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                extractBlock(rgba, block, firstByteInRow + i * 4, width);
                getMinMaxColors_luminance(block, minColor, maxColor);
                dxt1.write(makeBytes(colorTo565(minColor)));
                dxt1.write(makeBytes(colorTo565(maxColor)));
                dxt1.write(makeBytes(getColorIndices_dxt1_alpha(block, minColor, maxColor)));
            }
            firstByteInRow += width * 4 * 4;
        }
    }

    public void compressImageDXT5(int width, int height, byte[] rgba, byte[] dxt5Array)
    {
        ByteBuffer dxt5 = ByteBuffer.wrap(dxt5Array);
        dxt5.order(ByteOrder.LITTLE_ENDIAN);
        compressImageDXT5(width, height, rgba, dxt5);
    }

    public void compressImageDXT5(int width, int height, byte[] rgba, ByteBuffer dxt5)
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        int firstByteInRow = 0;
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int[] indices = new int[16];

        // protect against a missing alpha channel
        int pixelSize = rgba.length / (width * height);

        // Setting up if OUTSIDE the looping to hope for best
        // branch predicting and best performance.
        if (pixelSize == 3)
        {
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock_noAlpha(rgba, block, firstByteInRow + i * 4, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);

                    // alpha channels
                    dxt5.put((byte)maxColor[3]);
                    dxt5.put((byte)minColor[3]);

                    // Little ugly here, sorry! Unlike how it is written in the
                    // source paper, I like keeping
                    // all of the byte buffer accesses in the same loop in case
                    // we get out of whack.
                    getAlphaIndices(block, indices, minColor[3], maxColor[3]);
                    dxt5.put((byte)(indices[0] >> 0 | indices[1] << 3 | indices[2] << 6));
                    dxt5.put((byte)(indices[2] >> 2 | indices[3] << 1 | indices[4] << 4 | indices[5] << 7));
                    dxt5.put((byte)(indices[5] >> 1 | indices[6] << 2 | indices[7] << 5));
                    dxt5.put((byte)(indices[8] >> 0 | indices[9] << 3 | indices[10] << 6));
                    dxt5.put((byte)(indices[10] >> 2 | indices[11] << 1 | indices[12] << 4 | indices[13] << 7));
                    dxt5.put((byte)(indices[13] >> 1 | indices[14] << 2 | indices[15] << 5));

                    dxt5.putShort(colorTo565(maxColor));
                    dxt5.putShort(colorTo565(minColor));

                    dxt5.putInt(getColorIndices(block, minColor, maxColor));
                }
                firstByteInRow += width * 4 * 4;
            }
        }
        else
        {
            // Hope for the best!
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock(rgba, block, firstByteInRow + i * 4, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);

                    // alpha channels
                    dxt5.put((byte)maxColor[3]);
                    dxt5.put((byte)minColor[3]);

                    // Little ugly here, sorry! Unlike how it is written in the
                    // source paper, I like keeping
                    // all of the byte buffer accesses in the same loop in case
                    // we get out of whack.
                    getAlphaIndices(block, indices, minColor[3], maxColor[3]);
                    dxt5.put((byte)(indices[0] >> 0 | indices[1] << 3 | indices[2] << 6));
                    dxt5.put((byte)(indices[2] >> 2 | indices[3] << 1 | indices[4] << 4 | indices[5] << 7));
                    dxt5.put((byte)(indices[5] >> 1 | indices[6] << 2 | indices[7] << 5));
                    dxt5.put((byte)(indices[8] >> 0 | indices[9] << 3 | indices[10] << 6));
                    dxt5.put((byte)(indices[10] >> 2 | indices[11] << 1 | indices[12] << 4 | indices[13] << 7));
                    dxt5.put((byte)(indices[13] >> 1 | indices[14] << 2 | indices[15] << 5));

                    dxt5.putShort(colorTo565(maxColor));
                    dxt5.putShort(colorTo565(minColor));

                    dxt5.putInt(getColorIndices(block, minColor, maxColor));
                }
                firstByteInRow += width * 4 * 4;
            }
        }
    }

    public void compressImageDXT5(int width, int height, byte[] rgba, OutputStream dxt5) throws IOException
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        int firstByteInRow = 0;
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int[] indices = new int[16];

        // protect against a missing alpha channel
        int pixelSize = rgba.length / (width * height);

        // Setting up if OUTSIDE the looping to hope for best
        // branch predicting and best performance.
        if (pixelSize == 3)
        {
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock_noAlpha(rgba, block, firstByteInRow + i * 4, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);

                    // alpha channels
                    dxt5.write((byte)maxColor[3]);
                    dxt5.write((byte)minColor[3]);

                    // Little ugly here, sorry! Unlike how it is written in the
                    // source paper, I like keeping
                    // all of the byte buffer accesses in the same loop in case
                    // we get out of whack.
                    getAlphaIndices(block, indices, minColor[3], maxColor[3]);
                    dxt5.write((byte)(indices[0] >> 0 | indices[1] << 3 | indices[2] << 6));
                    dxt5.write((byte)(indices[2] >> 2 | indices[3] << 1 | indices[4] << 4 | indices[5] << 7));
                    dxt5.write((byte)(indices[5] >> 1 | indices[6] << 2 | indices[7] << 5));
                    dxt5.write((byte)(indices[8] >> 0 | indices[9] << 3 | indices[10] << 6));
                    dxt5.write((byte)(indices[10] >> 2 | indices[11] << 1 | indices[12] << 4 | indices[13] << 7));
                    dxt5.write((byte)(indices[13] >> 1 | indices[14] << 2 | indices[15] << 5));

                    dxt5.write(makeBytes(colorTo565(maxColor)));
                    dxt5.write(makeBytes(colorTo565(minColor)));

                    dxt5.write(makeBytes(getColorIndices(block, minColor, maxColor)));
                }
                firstByteInRow += width * 4 * 4;
            }
        }
        else
        {
            for (int j = 0; j < height; j += 4)
            {
                for (int i = 0; i < width; i += 4)
                {
                    extractBlock(rgba, block, firstByteInRow + i * 4, width);
                    getMinMaxColors_luminance(block, minColor, maxColor);

                    // alpha channels
                    dxt5.write((byte)maxColor[3]);
                    dxt5.write((byte)minColor[3]);

                    // Little ugly here, sorry! Unlike how it is written in the
                    // source paper, I like keeping
                    // all of the byte buffer accesses in the same loop in case
                    // we get out of whack.
                    getAlphaIndices(block, indices, minColor[3], maxColor[3]);
                    dxt5.write((byte)(indices[0] >> 0 | indices[1] << 3 | indices[2] << 6));
                    dxt5.write((byte)(indices[2] >> 2 | indices[3] << 1 | indices[4] << 4 | indices[5] << 7));
                    dxt5.write((byte)(indices[5] >> 1 | indices[6] << 2 | indices[7] << 5));
                    dxt5.write((byte)(indices[8] >> 0 | indices[9] << 3 | indices[10] << 6));
                    dxt5.write((byte)(indices[10] >> 2 | indices[11] << 1 | indices[12] << 4 | indices[13] << 7));
                    dxt5.write((byte)(indices[13] >> 1 | indices[14] << 2 | indices[15] << 5));

                    dxt5.write(makeBytes(colorTo565(maxColor)));
                    dxt5.write(makeBytes(colorTo565(minColor)));

                    dxt5.write(makeBytes(getColorIndices(block, minColor, maxColor)));
                }
                firstByteInRow += width * 4 * 4;
            }
        }

    }

    /**
     * Decode a DDS image to an RGB or RGBA byte array, depending on the type of
     * compression.
     *
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     * @param type The compression type of the input raster.
     * @param raster The input raster.
     * @return The bytes.
     */
    public byte[] decodeDDS(int width, int height, CompressionType type, ByteBuffer raster)
    {
        if (type == CompressionType.DXT1)
        {
            return decodeDXT1(width, height, raster);
        }
        else if (type == CompressionType.DXT5)
        {
            return decodeDXT5(width, height, raster);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported compression type: " + type);
        }
    }

    /**
     * Decode a DXT1 image to an RGB byte array.
     *
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     * @param data The image raster.
     * @return The RGB byte array.
     */
    public byte[] decodeDXT1(int width, int height, ByteBuffer data)
    {
        final int resultBytesPerPixel = 3;
        final float dxt1BytesPerPixel = .5f;
        if (width * height % 16 != 0)
        {
            throw new IllegalArgumentException("Width * height must be divisible by 16.");
        }

        if (data.remaining() != width * height * dxt1BytesPerPixel + DDS_HEADER_SIZE)
        {
            throw new IllegalArgumentException("Data array is wrong size: " + data.remaining() + " != "
                    + (width * height * dxt1BytesPerPixel + DDS_HEADER_SIZE));
        }

        byte[] result = new byte[width * height * resultBytesPerPixel];
        for (int blockY = 0; blockY < height; blockY += 4)
        {
            for (int blockX = 0; blockX < width; blockX += 4)
            {
                int blockIndex = (int)(dxt1BytesPerPixel * (blockX * 4 + blockY * width)) + DDS_HEADER_SIZE;

                int r0 = 248 & data.get(blockIndex + 1);
                int g0 = 252 & (data.get(blockIndex + 1) << 5 | 28 & data.get(blockIndex) >> 3);
                int b0 = 248 & data.get(blockIndex) << 3;

                int r1 = 248 & data.get(blockIndex + 3);
                int g1 = 252 & (data.get(blockIndex + 3) << 5 | 28 & data.get(blockIndex + 2) >> 3);
                int b1 = 248 & data.get(blockIndex + 2) << 3;

                int r2 = (r1 + (r0 << 1)) / 3;
                int g2 = (g1 + (g0 << 1)) / 3;
                int b2 = (b1 + (b0 << 1)) / 3;

                int r3 = ((r1 << 1) + r0) / 3;
                int g3 = ((g1 << 1) + g0) / 3;
                int b3 = ((b1 << 1) + b0) / 3;

                for (int y = 0; y < 4; ++y)
                {
                    byte val = data.get(blockIndex + 4 + y);
                    for (int x = 0; x < 4; ++x)
                    {
                        int resultIndex = (x + blockX + (blockY + y) * width) * resultBytesPerPixel;
                        int colorSelect = val >> (x << 1) & 3;
                        if (colorSelect == 2)
                        {
                            result[resultIndex] = (byte)r2;
                            result[resultIndex + 1] = (byte)g2;
                            result[resultIndex + 2] = (byte)b2;
                        }
                        else if (colorSelect == 3)
                        {
                            result[resultIndex] = (byte)r3;
                            result[resultIndex + 1] = (byte)g3;
                            result[resultIndex + 2] = (byte)b3;
                        }
                        else if (colorSelect == 1)
                        {
                            result[resultIndex] = (byte)r1;
                            result[resultIndex + 1] = (byte)g1;
                            result[resultIndex + 2] = (byte)b1;
                        }
                        else
                        {
                            result[resultIndex] = (byte)r0;
                            result[resultIndex + 1] = (byte)g0;
                            result[resultIndex + 2] = (byte)b0;
                        }
                    }
                }
            }
        }

        return result;

    }

    /**
     * Decode a DXT5 image to an RGBA byte array.
     *
     * @param width The width of the image in pixels.
     * @param height The height of the image in pixels.
     * @param data The image raster.
     * @return The RGB byte array.
     */
    public byte[] decodeDXT5(int width, int height, ByteBuffer data)
    {
        final int resultBytesPerPixel = 4;

        if (width * height % 16 != 0)
        {
            throw new IllegalArgumentException("Width * height must be divisible by 16.");
        }

        if (data.remaining() != width * height + DDS_HEADER_SIZE)
        {
            throw new IllegalArgumentException(
                    "data array is wrong size: " + data.remaining() + " != " + (width * height + DDS_HEADER_SIZE));
        }

        byte[] result = new byte[width * height * resultBytesPerPixel];
        byte[] alpha = new byte[8];
        for (int blockY = 0; blockY < height; blockY += 4)
        {
            for (int blockX = 0; blockX < width; blockX += 4)
            {
                int blockIndex = blockX * 4 + blockY * width + DDS_HEADER_SIZE;

                alpha[0] = data.get(blockIndex);
                alpha[1] = data.get(blockIndex + 1);
                int a0 = 255 & alpha[0];
                int a1 = 255 & alpha[1];
                alpha[2] = (byte)((6 * a0 + 1 * a1 + 3) / 7);
                alpha[3] = (byte)((5 * a0 + 2 * a1 + 3) / 7);
                alpha[4] = (byte)((4 * a0 + 3 * a1 + 3) / 7);
                alpha[5] = (byte)((3 * a0 + 4 * a1 + 3) / 7);
                alpha[6] = (byte)((2 * a0 + 5 * a1 + 3) / 7);
                alpha[7] = (byte)((1 * a0 + 6 * a1 + 3) / 7);

                byte alpha0 = data.get(blockIndex + 2);
                byte alpha1 = data.get(blockIndex + 3);
                byte alpha2 = data.get(blockIndex + 4);
                byte alpha3 = data.get(blockIndex + 5);
                byte alpha4 = data.get(blockIndex + 6);
                byte alpha5 = data.get(blockIndex + 7);

                int r0 = 248 & data.get(blockIndex + 9);
                int g0 = 252 & (data.get(blockIndex + 9) << 5 | 28 & data.get(blockIndex + 8) >> 3);
                int b0 = 248 & data.get(blockIndex + 8) << 3;

                int r1 = 248 & data.get(blockIndex + 11);
                int g1 = 252 & (data.get(blockIndex + 11) << 5 | 28 & data.get(blockIndex + 10) >> 3);
                int b1 = 248 & data.get(blockIndex + 10) << 3;

                int r2 = (r1 + (r0 << 1)) / 3;
                int g2 = (g1 + (g0 << 1)) / 3;
                int b2 = (b1 + (b0 << 1)) / 3;

                int r3 = ((r1 << 1) + r0) / 3;
                int g3 = ((g1 << 1) + g0) / 3;
                int b3 = ((b1 << 1) + b0) / 3;

                result[3 + (blockX + blockY * width) * resultBytesPerPixel] = alpha[alpha0 & 7];
                result[3 + (1 + blockX + blockY * width) * resultBytesPerPixel] = alpha[alpha0 >> 3 & 7];
                result[3 + (2 + blockX + blockY * width) * resultBytesPerPixel] = alpha[alpha1 << 2 & 4 | alpha0 >> 6 & 3];
                result[3 + (3 + blockX + blockY * width) * resultBytesPerPixel] = alpha[alpha1 >> 1 & 7];

                result[3 + (blockX + (blockY + 1) * width) * resultBytesPerPixel] = alpha[alpha1 >> 4 & 7];
                result[3 + (1 + blockX + (blockY + 1) * width) * resultBytesPerPixel] = alpha[alpha2 << 1 & 6 | alpha1 >> 7 & 1];
                result[3 + (2 + blockX + (blockY + 1) * width) * resultBytesPerPixel] = alpha[alpha2 >> 2 & 7];
                result[3 + (3 + blockX + (blockY + 1) * width) * resultBytesPerPixel] = alpha[alpha2 >> 5 & 7];

                result[3 + (blockX + (blockY + 2) * width) * resultBytesPerPixel] = alpha[alpha3 & 7];
                result[3 + (1 + blockX + (blockY + 2) * width) * resultBytesPerPixel] = alpha[alpha3 >> 3 & 7];
                result[3 + (2 + blockX + (blockY + 2) * width) * resultBytesPerPixel] = alpha[alpha4 << 2 & 4 | alpha3 >> 6 & 3];
                result[3 + (3 + blockX + (blockY + 2) * width) * resultBytesPerPixel] = alpha[alpha4 >> 1 & 7];

                result[3 + (blockX + (blockY + 3) * width) * resultBytesPerPixel] = alpha[alpha4 >> 4 & 7];
                result[3 + (1 + blockX + (blockY + 3) * width) * resultBytesPerPixel] = alpha[alpha5 << 1 & 6 | alpha4 >> 7 & 1];
                result[3 + (2 + blockX + (blockY + 3) * width) * resultBytesPerPixel] = alpha[alpha5 >> 2 & 7];
                result[3 + (3 + blockX + (blockY + 3) * width) * resultBytesPerPixel] = alpha[alpha5 >> 5 & 7];

                for (int y = 0; y < 4; ++y)
                {
                    byte val = data.get(blockIndex + 12 + y);
                    for (int x = 0; x < 4; ++x)
                    {
                        int resultIndex = (x + blockX + (blockY + y) * width) * resultBytesPerPixel;
                        int colorSelect = val >> (x << 1) & 3;
                        if (colorSelect == 2)
                        {
                            result[resultIndex] = (byte)r2;
                            result[resultIndex + 1] = (byte)g2;
                            result[resultIndex + 2] = (byte)b2;
                        }
                        else if (colorSelect == 3)
                        {
                            result[resultIndex] = (byte)r3;
                            result[resultIndex + 1] = (byte)g3;
                            result[resultIndex + 2] = (byte)b3;
                        }
                        else if (colorSelect == 1)
                        {
                            result[resultIndex] = (byte)r1;
                            result[resultIndex + 1] = (byte)g1;
                            result[resultIndex + 2] = (byte)b1;
                        }
                        else
                        {
                            result[resultIndex] = (byte)r0;
                            result[resultIndex + 1] = (byte)g0;
                            result[resultIndex + 2] = (byte)b0;
                        }
                    }
                }
            }
        }

        return result;

    }

    public void writeImageUncompressed(int width, int height, byte[] rgba, ByteBuffer dds)
    {
        // if possible use bulk copy.
        if (myRGBA)
        {
            dds.put(rgba);
        }
        else
        {
            int pixelSize = rgba.length / (width * height);
            switch (pixelSize)
            {
                case 3:
                    for (int i = 0; i < rgba.length; i += 3)
                    {
                        dds.put(rgba[i + 2]);
                        dds.put(rgba[i + 1]);
                        dds.put(rgba[i]);
                    }
                    break;
                case 4:
                    for (int i = 0; i < rgba.length; i += 4)
                    {
                        dds.put(rgba[i + first]);
                        dds.put(rgba[i + second]);
                        dds.put(rgba[i + third]);
                        dds.put(rgba[i + fourth]);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected pixel size: " + pixelSize);
            }
        }
    }

    private void writeImageUncompressed(int width, int height, byte[] rgba, OutputStream dds) throws IOException
    {
        // if possible use bulk copy.
        if (myRGBA)
        {
            dds.write(rgba);
        }
        else
        {
            for (int i = 0; i < rgba.length; i += 4)
            {
                dds.write(rgba[i + first]);
                dds.write(rgba[i + second]);
                dds.write(rgba[i + third]);
                dds.write(rgba[i + fourth]);
            }
        }
    }

    private static final int DDSD_CAPS = 0x0001;

    private static final int DDSD_HEIGHT = 0x0002;

    private static final int DDSD_WIDTH = 0x0004;

    private static final int DDSD_PITCH = 0x8;

    private static final int DDSD_PIXELFORMAT = 0x1000;

    private static final int DDSD_MIPMAPCOUNT = 0x20000;

    private static final int DDSD_LINEARSIZE = 0x80000;

    private static final int DDPF_FOURCC = 0x0004;

    private static final int DDSCAPS_TEXTURE = 0x1000;

    private static final int DDPF_RGB = 0x00000040;

    private static final int DDPF_ALPHAPIXELS = 0x00000001;

    private static final int RGBA_R_MASK = 0x00FF0000;

    private static final int RGBA_G_MASK = 0x0000FF00;

    private static final int RGBA_B_MASK = 0x000000FF;

    private static final int RGBA_A_MASK = 0xFF000000;

    protected static void buildHeaderDxt1(int width, int height, ByteBuffer buffer)
    {
        buffer.rewind();
        buffer.put((byte)'D');
        buffer.put((byte)'D');
        buffer.put((byte)'S');
        buffer.put((byte)' ');
        buffer.putInt(124);
        int flag = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT | DDSD_MIPMAPCOUNT | DDSD_LINEARSIZE;
        buffer.putInt(flag);
        buffer.putInt(height);
        buffer.putInt(width);
        buffer.putInt(width * height / 2);
        // depth
        buffer.putInt(0);
        // mipmap count
        buffer.putInt(0);
        // 11 unused double-words
        buffer.position(buffer.position() + 44);
        // pixel format size
        buffer.putInt(32);
        buffer.putInt(DDPF_FOURCC);
        buffer.put((byte)'D');
        buffer.put((byte)'X');
        buffer.put((byte)'T');
        buffer.put((byte)'1');
        // bits per pixel for RGB (non-compressed) formats
        buffer.putInt(0);
        // rgb bit masks for RGB formats
        buffer.putInt(0);
        // rgb bit masks for RGB formats
        buffer.putInt(0);
        // rgb bit masks for RGB formats
        buffer.putInt(0);
        // alpha mask for RGB formats
        buffer.putInt(0);
        buffer.putInt(DDSCAPS_TEXTURE);
        // ddsCaps2
        buffer.putInt(0);
        // 3 unused double-words
        buffer.position(buffer.position() + 12);
    }

    protected static void buildHeaderDxt5(int width, int height, ByteBuffer buffer)
    {
        buffer.rewind();
        buffer.put((byte)'D');
        buffer.put((byte)'D');
        buffer.put((byte)'S');
        buffer.put((byte)' ');
        buffer.putInt(124);
        int flag = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT | DDSD_MIPMAPCOUNT | DDSD_LINEARSIZE;
        buffer.putInt(flag);
        buffer.putInt(height);
        buffer.putInt(width);
        buffer.putInt(width * height);
        // depth
        buffer.putInt(0);
        // mipmap count
        buffer.putInt(0);
        // 11 unused double-words
        buffer.position(buffer.position() + 44);
        // pixel format size
        buffer.putInt(32);
        buffer.putInt(DDPF_FOURCC);
        buffer.put((byte)'D');
        buffer.put((byte)'X');
        buffer.put((byte)'T');
        buffer.put((byte)'5');
        // bits per pixel for RGB (non-compressed) formats
        buffer.putInt(0);
        // rgb bit masks for RGB formats
        buffer.putInt(0);
        // rgb bit masks for RGB formats
        buffer.putInt(0);
        // rgb bit masks for RGB formats
        buffer.putInt(0);
        // alpha mask for RGB formats
        buffer.putInt(0);
        buffer.putInt(DDSCAPS_TEXTURE);
        // ddsCaps2
        buffer.putInt(0);
        // 3 unused double-words
        buffer.position(buffer.position() + 12);
    }

    private void buildHeaderUncompressed(int width, int height, boolean hasAlpha, ByteBuffer buffer)
    {
        buffer.rewind();
        buffer.put((byte)'D');
        buffer.put((byte)'D');
        buffer.put((byte)'S');
        buffer.put((byte)' ');
        buffer.putInt(124);
        int flag = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PITCH | DDSD_PIXELFORMAT | DDSD_MIPMAPCOUNT;
        buffer.putInt(flag);
        buffer.putInt(height);
        buffer.putInt(width);
        int pixelSize = hasAlpha ? 32 : 24;
        buffer.putInt(width * pixelSize);
        // depth
        buffer.putInt(0);
        // mipmap count
        buffer.putInt(0);
        // 11 unused double-words
        buffer.position(buffer.position() + 44);
        // pixel format size
        buffer.putInt(32);
        buffer.putInt(hasAlpha ? DDPF_RGB | DDPF_ALPHAPIXELS : DDPF_RGB);
        if (hasAlpha)
        {
            // compression type
            buffer.put((byte)'A');
        }
        // compression type
        buffer.put((byte)'R');
        // compression type
        buffer.put((byte)'G');
        // compression type
        buffer.put((byte)'B');
        if (!hasAlpha)
        {
            buffer.put((byte)' ');
        }
        // bits per pixel for RGB (non-compressed) formats
        buffer.putInt(pixelSize);
        // bits per pixel for RGB (non-compressed) formats
        buffer.putInt(RGBA_R_MASK);
        // rgb bit masks for RGB formats
        buffer.putInt(RGBA_G_MASK);
        // rgb bit masks for RGB formats
        buffer.putInt(RGBA_B_MASK);
        // rgb bit masks for RGB formats
        buffer.putInt(RGBA_A_MASK);
        buffer.putInt(DDSCAPS_TEXTURE);
        // ddsCaps2
        buffer.putInt(0);
        // 3 unused double-words
        buffer.position(buffer.position() + 12);
    }
}
