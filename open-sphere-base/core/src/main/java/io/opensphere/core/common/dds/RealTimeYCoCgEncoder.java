package io.opensphere.core.common.dds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* This code is a pure Java implementation of the DXT compression algorithm
 * described in the paper "Real-Time YCoCg-DXT Compression" dated September
 * 14th, 2007 by J.M.P. van Waveren, Id Software, Inc. and Ignacio Castano,
 * Nvidia Corp; paper (c) Id Software, Inc.
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

public class RealTimeYCoCgEncoder
{
    // To decode back into RGBA, this algorithm requires the following to
    // run in a shader:
    // scale = (color.z * ( 255.0/8.0 ) + 1.0
    // Co = ( color.x - ( 0.5 * 256.0/255.0 ) ) / scale;
    // Cg = ( color.y - ( 0.5 * 256.0/255.0 ) ) / scale;
    // Y = color.w;
    // R = Y + Co - Cg;
    // G = Y + Cg;
    // B = Y - Co - Cg;

    // RGBA
    int first = 0;

    int second = 1;

    int third = 2;

    int fourth = 3;

    // RGBA index values
    int r = 0;

    int g = 1;

    int b = 2;

    int a = 3;

    // CoCg_Y index values
    int Co = 0;

    int Cg = 1;

    int _a = 2;

    int Y = 3;

    public RealTimeYCoCgEncoder()
    {
        // TODO Auto-generated constructor stub
    }

    public static final int DXT_HEADER_SIZE = 128;

    public enum CompressionType
    {
        // Only DXT5 is supported via this algorithm
        DXT5_YCoCg
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
        first = 3;
        second = 2;
        third = 1;
        fourth = 0;
    }

    public void setBGR()
    {
        first = 2;
        second = 1;
        third = 0;
    }

    public void encodeDDS(int width, int height, CompressionType type, byte[] inRaster, ByteArrayOutputStream outFile)
        throws IOException
    {
        if (type == CompressionType.DXT5_YCoCg)
        {
            // Not worried about the overhead of copying the 128 byte header,
            // just the quarter-MB image data.
            ByteBuffer buffer = ByteBuffer.allocate(DXT_HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buildHeaderDxt5(width, height, buffer);
            outFile.write(buffer.array());
            if (inRaster.length == width * height * 4)
            {
                compressImageYCoCgDXT5(width, height, inRaster, outFile);
            }
            else if (inRaster.length == width * height * 3)
            {
                // No alpha channel
                compressImageYCoCgDXT5_noAlpha(width, height, inRaster, outFile);
            }
        }
    }

    public void encodeDDS(int width, int height, CompressionType type, byte[] inRaster, ByteBuffer outFile)
    {
        if (type == CompressionType.DXT5_YCoCg)
        {
            buildHeaderDxt5(width, height, outFile);
            if (inRaster.length == width * height * 4)
            {
                compressImageYCoCgDXT5(width, height, inRaster, outFile);
            }
            else if (inRaster.length == width * height * 3)
            {
                // No alpha channel
                compressImageYCoCgDXT5_noAlpha(width, height, inRaster, outFile);
            }
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
            if (type == CompressionType.DXT5_YCoCg)
            {
                if (inRaster.length == width * height * 4)
                {
                    compressImageYCoCgDXT5(width, height, inRaster, outFile);
                }
                else if (inRaster.length == width * height * 3)
                {
                    // No alpha channel
                    compressImageYCoCgDXT5_noAlpha(width, height, inRaster, outFile);
                }
            }
        }
    }

    private void extractBlock(byte[] inBuffer, int[][] colorBlock, int position, int width)
    {
        // Same code as RealTimeEncoder, only difference is the swap to CoCg_Y
        int[] temp = new int[4];

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
                // the first, second, etc. nomenclature allows for
                // the incoming buffer to be agbr (as in Java 6u18+)
                // after this, the temp should be RGBA as expected
                temp[first] = 0xFF & inBuffer[position++];
                // need to mask with 0xFF as simple assignment will move
                // the sign bit.
                temp[second] = 0xFF & inBuffer[position++];
                temp[third] = 0xFF & inBuffer[position++];
                temp[fourth] = 0xFF & inBuffer[position++];

                // Convert to CoCg_Y
                int val = ((temp[r] << 1) - (temp[b] << 1) + 2 >> 2) + 128;
                colorBlock[i * 4 + j][Co] = val < 0 ? 0 : val > 255 ? 255 : val;
                val = ((temp[g] << 1) - temp[r] - temp[b] + 2 >> 2) + 128;
                colorBlock[i * 4 + j][Cg] = val < 0 ? 0 : val > 255 ? 255 : val;
                colorBlock[i * 4 + j][_a] = temp[a];
                val = temp[r] + (temp[g] << 1) + temp[b] + 2 >> 2;
                colorBlock[i * 4 + j][Y] = val < 0 ? 0 : val > 255 ? 255 : val;
            }
            // Push buffer to right "row", each is "width" apart
            position += width * 4 - 16;
        }
    }

    private void extractBlock_noAlpha(byte[] inBuffer, int[][] colorBlock, int position, int width)
    {
        // Same code as RealTimeEncoder, only difference is the swap to CoCg_Y
        // This function fills in the missing alpha to be fully opaque (255)
        int[] temp = new int[4];

        for (int i = 0; i < 4; i++) // 4 rows
        {
            for (int j = 0; j < 4; j++) // 4 Cols
            {
                // copy the 3 bytes of rgb into their own ints
                temp[first] = 0xFF & inBuffer[position++];
                temp[second] = 0xFF & inBuffer[position++];
                temp[third] = 0xFF & inBuffer[position++];
                temp[fourth] = 255;

                // Convert to CoCg_Y
                int val = ((temp[r] << 1) - (temp[b] << 1) + 2 >> 2) + 128;
                colorBlock[i * 4 + j][Co] = val < 0 ? 0 : val > 255 ? 255 : val;
                val = ((temp[g] << 1) - temp[r] - temp[b] + 2 >> 2) + 128;
                colorBlock[i * 4 + j][Cg] = val < 0 ? 0 : val > 255 ? 255 : val;
                colorBlock[i * 4 + j][_a] = temp[a];
                val = temp[r] + (temp[g] << 1) + temp[b] + 2 >> 2;
                colorBlock[i * 4 + j][Y] = val < 0 ? 0 : val > 255 ? 255 : val;
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

    private void getMinMaxColors(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
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
    }

    private static final int s0 = 128 / 2 - 1;

    private static final int s1 = 128 / 4 - 1;

    private void scaleYCoCg(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        int m0 = Math.abs(minColor[0] - 128);
        int m1 = Math.abs(minColor[1] - 128);
        int m2 = Math.abs(maxColor[0] - 128);
        int m3 = Math.abs(maxColor[1] - 128);

        if (m1 > m0)
        {
            m0 = m1;
        }
        if (m3 > m2)
        {
            m2 = m3;
        }
        if (m2 > m0)
        {
            m0 = m2;
        }

        int mask0 = m0 <= s0 ? -1 : -0;
        int mask1 = m0 <= s1 ? -1 : -0;
        int scale = 1 + (1 & mask0) + (2 & mask1);

        minColor[0] = (minColor[0] - 128) * scale + 128;
        minColor[1] = (minColor[1] - 128) * scale + 128;
        minColor[2] = scale - 1 << 3;

        maxColor[0] = (maxColor[0] - 128) * scale + 128;
        maxColor[1] = (maxColor[1] - 128) * scale + 128;
        maxColor[2] = scale - 1 << 3;

        for (int i = 0; i < 16; i++)
        {
            colorBlock[i][0] = (colorBlock[i][0] - 128) * scale + 128;
            colorBlock[i][1] = (colorBlock[i][1] - 128) * scale + 128;
        }

    }

    private static final int INSET_COLOR_SHIFT = 4;

    private static final int INSET_ALPHA_SHIFT = 5;

    private static final int C565_5_MASK = 0xF8;

    private static final int C565_6_MASK = 0xFC;

    private void insetYCoCgBBox(int[] minColor, int[] maxColor)
    {
        // This block gets called hundreds of thousands of times per image
        // so use stack-space primitives rather than heap space array.
        int inset0, inset1, inset3;
        int mini0, mini1, mini3;
        int maxi0, maxi1, maxi3;

        inset0 = maxColor[0] - minColor[0] - ((1 << INSET_COLOR_SHIFT - 1) - 1);
        inset1 = maxColor[1] - minColor[1] - ((1 << INSET_COLOR_SHIFT - 1) - 1);
        inset3 = maxColor[3] - minColor[3] - ((1 << INSET_ALPHA_SHIFT - 1) - 1);

        mini0 = (minColor[0] << INSET_COLOR_SHIFT) + inset0 >> INSET_COLOR_SHIFT;
        mini1 = (minColor[1] << INSET_COLOR_SHIFT) + inset1 >> INSET_COLOR_SHIFT;
        mini3 = (minColor[3] << INSET_ALPHA_SHIFT) + inset3 >> INSET_ALPHA_SHIFT;

        maxi0 = (maxColor[0] << INSET_COLOR_SHIFT) - inset0 >> INSET_COLOR_SHIFT;
        maxi1 = (maxColor[1] << INSET_COLOR_SHIFT) - inset1 >> INSET_COLOR_SHIFT;
        maxi3 = (maxColor[3] << INSET_ALPHA_SHIFT) - inset3 >> INSET_ALPHA_SHIFT;

        mini0 = mini0 >= 0 ? mini0 : 0;
        mini1 = mini1 >= 0 ? mini1 : 0;
        mini3 = mini3 >= 0 ? mini3 : 0;

        maxi0 = maxi0 <= 255 ? maxi0 : 255;
        maxi1 = maxi1 <= 255 ? maxi1 : 255;
        maxi3 = maxi3 <= 255 ? maxi3 : 255;

        minColor[0] = mini0 & C565_5_MASK | mini0 >> 5;
        minColor[1] = mini1 & C565_6_MASK | mini1 >> 6;
        minColor[3] = mini3;
        maxColor[0] = maxi0 & C565_5_MASK | maxi0 >> 5;
        maxColor[1] = maxi1 & C565_6_MASK | maxi1 >> 6;
        maxColor[3] = maxi3;
    }

    private void selectYCoCgDiagonal(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        int mid0 = minColor[0] + maxColor[0] + 1 >> 1;
        int mid1 = minColor[1] + maxColor[1] + 1 >> 1;

        int side = 0;
        for (int i = 0; i < 16; i++)
        {
            int b0 = colorBlock[i][0] >= mid0 ? 1 : 0;
            int b1 = colorBlock[i][1] >= mid1 ? 1 : 0;
            side += b0 ^ b1;
        }

        int mask = side > 8 ? 0xFF & -1 : 0xFF & -0;

        int c0 = minColor[1];
        int c1 = maxColor[1];

        // Paper calls for"
        // c0 ^= c1 ^= mask &= c0 ^= c1;
        // But java doesn't like that. Splitting out the code makes it work.
        c0 ^= c1;
        mask &= c0;
        c1 ^= mask;
        c0 ^= c1;

        minColor[1] = c0;
        maxColor[1] = c1;
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
            int b1 = a <= ab1 ? 1 : 0;
            int b2 = a <= ab2 ? 1 : 0;
            int b3 = a <= ab3 ? 1 : 0;
            int b4 = a <= ab4 ? 1 : 0;
            int b5 = a <= ab5 ? 1 : 0;
            int b6 = a <= ab6 ? 1 : 0;
            int b7 = a <= ab7 ? 1 : 0;
            int index = b1 + b2 + b3 + b4 + b5 + b6 + b7 + 1 & 7;
            indices[i] = index ^ (2 > index ? 1 : 0);
        }
    }

    int getColorIndices(int[][] colorBlock, int[] minColor, int[] maxColor)
    {
        int result = 0;
        int colors0Co = maxColor[0] & C565_5_MASK | maxColor[0] >> 5;
        int colors0Cg = maxColor[1] & C565_6_MASK | maxColor[1] >> 6;
        // int colors0_a = maxColor[2] & C565_5_MASK | maxColor[2] >> 5;
        // int colors0Y = 0;
        int colors1Co = minColor[0] & C565_5_MASK | minColor[0] >> 5;
        int colors1Cg = minColor[1] & C565_6_MASK | minColor[1] >> 6;
        // int colors1_a = minColor[2] & C565_5_MASK | minColor[2] >> 5;
        // int colors1Y = 0;
        int colors2Co = (2 * colors0Co + 1 * colors1Co) / 3;
        int colors2Cg = (2 * colors0Cg + 1 * colors1Cg) / 3;
        // int colors2_a = ( 2 * colors0_a + 1 * colors1_a ) / 3;
        // int colors2Y = 0;
        int colors3Co = (1 * colors0Co + 2 * colors1Co) / 3;
        int colors3Cg = (1 * colors0Cg + 2 * colors1Cg) / 3;
        // int colors3_a = ( 1 * colors0_a + 2 * colors1_a ) / 3;
        // int colors3Y = 0;
        for (int i = 15; i >= 0; --i)
        {
            int c0 = colorBlock[i][0];
            int c1 = colorBlock[i][1];

            int d0 = Math.abs(colors0Co - c0) + Math.abs(colors0Cg - c1);
            int d1 = Math.abs(colors1Co - c0) + Math.abs(colors1Cg - c1);
            int d2 = Math.abs(colors2Co - c0) + Math.abs(colors2Cg - c1);
            int d3 = Math.abs(colors3Co - c0) + Math.abs(colors3Cg - c1);

            int b0 = d0 > d3 ? 1 : 0;
            int b1 = d1 > d2 ? 1 : 0;
            int b2 = d0 > d2 ? 1 : 0;
            int b3 = d1 > d3 ? 1 : 0;
            int b4 = d2 > d3 ? 1 : 0;

            int x0 = b1 & b2;
            int x1 = b0 & b3;
            int x2 = b0 & b4;
            result |= (x2 | (x0 | x1) << 1) << (i << 1);
        }
        return result;
    }

    public void compressImageYCoCgDXT5(int width, int height, byte[] rgba, byte[] dxt5Array)
    {
        ByteBuffer dxt5 = ByteBuffer.wrap(dxt5Array);
        dxt5.order(ByteOrder.LITTLE_ENDIAN);
        compressImageYCoCgDXT5(width, height, rgba, dxt5);
    }

    public void compressImageYCoCgDXT5(int width, int height, byte[] rgba, ByteBuffer dxt5)
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        int firstByteInRow = 0;
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int[] indices = new int[16];
        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                extractBlock(rgba, block, firstByteInRow + i * 4, width);
                getMinMaxColors(block, minColor, maxColor);
                scaleYCoCg(block, minColor, maxColor);
                insetYCoCgBBox(minColor, maxColor);
                selectYCoCgDiagonal(block, minColor, maxColor);

                // alpha channels
                dxt5.put((byte)maxColor[3]);
                dxt5.put((byte)minColor[3]);

                // Little ugly here, sorry! Unlike how it is written in the
                // source paper, I like keeping
                // all of the byte buffer accesses in the same loop in case we
                // get out of whack.
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

    public void compressImageYCoCgDXT5(int width, int height, byte[] rgba, ByteArrayOutputStream dxt5) throws IOException
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        int firstByteInRow = 0;
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int[] indices = new int[16];
        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                extractBlock(rgba, block, firstByteInRow + i * 4, width);
                getMinMaxColors(block, minColor, maxColor);
                scaleYCoCg(block, minColor, maxColor);
                insetYCoCgBBox(minColor, maxColor);
                selectYCoCgDiagonal(block, minColor, maxColor);

                // alpha channels
                dxt5.write((byte)maxColor[3]);
                dxt5.write((byte)minColor[3]);

                // Little ugly here, sorry! Unlike how it is written in the
                // source paper, I like keeping
                // all of the byte buffer accesses in the same loop in case we
                // get out of whack.
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

    public void compressImageYCoCgDXT5_noAlpha(int width, int height, byte[] rgba, byte[] dxt5Array)
    {
        ByteBuffer dxt5 = ByteBuffer.wrap(dxt5Array);
        dxt5.order(ByteOrder.LITTLE_ENDIAN);
        compressImageYCoCgDXT5_noAlpha(width, height, rgba, dxt5);
    }

    public void compressImageYCoCgDXT5_noAlpha(int width, int height, byte[] rgb, ByteBuffer dxt5)
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        int firstByteInRow = 0;
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int[] indices = new int[16];
        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                extractBlock_noAlpha(rgb, block, firstByteInRow + i * 3, width);
                getMinMaxColors(block, minColor, maxColor);
                scaleYCoCg(block, minColor, maxColor);
                insetYCoCgBBox(minColor, maxColor);
                selectYCoCgDiagonal(block, minColor, maxColor);

                // alpha channels
                dxt5.put((byte)maxColor[3]);
                dxt5.put((byte)minColor[3]);

                // Little ugly here, sorry! Unlike how it is written in the
                // source paper, I like keeping
                // all of the byte buffer accesses in the same loop in case we
                // get out of whack.
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
            firstByteInRow += width * 3 * 4;
        }
    }

    public void compressImageYCoCgDXT5_noAlpha(int width, int height, byte[] rgb, ByteArrayOutputStream dxt5) throws IOException
    {
        // Again, expressing bytes as ints to get around the signed byte issue
        int[] minColor = new int[4];
        int[] maxColor = new int[4];
        int firstByteInRow = 0;
        // (row x column) x rgba
        int[][] block = new int[16][4];
        int[] indices = new int[16];
        for (int j = 0; j < height; j += 4)
        {
            for (int i = 0; i < width; i += 4)
            {
                extractBlock_noAlpha(rgb, block, firstByteInRow + i * 3, width);
                getMinMaxColors(block, minColor, maxColor);
                scaleYCoCg(block, minColor, maxColor);
                insetYCoCgBBox(minColor, maxColor);
                selectYCoCgDiagonal(block, minColor, maxColor);

                // alpha channels
                dxt5.write((byte)maxColor[3]);
                dxt5.write((byte)minColor[3]);

                // Little ugly here, sorry! Unlike how it is written in the
                // source paper, I like keeping
                // all of the byte buffer accesses in the same loop in case we
                // get out of whack.
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
            firstByteInRow += width * 3 * 4;
        }
    }

    private static final int DDSD_CAPS = 0x0001;

    private static final int DDSD_HEIGHT = 0x0002;

    private static final int DDSD_WIDTH = 0x0004;

    private static final int DDSD_PIXELFORMAT = 0x1000;

    private static final int DDSD_MIPMAPCOUNT = 0x20000;

    private static final int DDSD_LINEARSIZE = 0x80000;

    private static final int DDPF_FOURCC = 0x0004;

    private static final int DDSCAPS_TEXTURE = 0x1000;

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

        // 11 unused double-words, identify ourselves
        buffer.put((byte)'H');
        buffer.put((byte)'Q');
        buffer.put((byte)'R');
        buffer.put((byte)'T');
        buffer.put((byte)'J');
        buffer.put((byte)'A');
        buffer.put((byte)'V');
        buffer.put((byte)'A');
        // version 1
        buffer.putInt(1);
        // Use GIMP Plugin's identifier so that gimp will read
        buffer.put((byte)'Y');
        buffer.put((byte)'C');
        buffer.put((byte)'G');
        buffer.put((byte)'1');

        // 7 unused double-words

        buffer.position(buffer.position() + 28);
        // pixel format size
        buffer.putInt(32);
        buffer.putInt(DDPF_FOURCC);
        buffer.put((byte)'D');
        buffer.put((byte)'X');
        buffer.put((byte)'T');
        buffer.put((byte)'5');
        // bits per pixel for RGB (non-compressed) formats
        /* buffer.putInt(0); // rgb bit masks for RGB formats buffer.putInt(0);
         * // rgb bit masks for RGB formats buffer.putInt(0); // rgb bit masks
         * for RGB formats buffer.putInt(0); // alpha mask for RGB formats */
        buffer.putInt(0);
        // bits per pixel for RGB (non-compressed) formats
        buffer.putInt(32);
        // rgb bit masks for RGB formats
        buffer.putInt(0x00ff0000);
        // rgb bit masks for RGB formats
        buffer.putInt(0x0000ff00);
        // rgb bit masks for RGB formats
        buffer.putInt(0x000000ff);
        // alpha mask for RGB formats
        buffer.putInt(0xff000000);
        buffer.putInt(DDSCAPS_TEXTURE);
        // ddsCaps2
        buffer.putInt(0);
        // 3 unused double-words
        buffer.position(buffer.position() + 12);
    }

}
