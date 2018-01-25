package io.opensphere.core.common.shapefile.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ShapeHeader
{
    /* Byte Position Field Value Type Order Byte 0 File Code 9994 Integer Big
     * Byte 4 Unused 0 Integer Big Byte 8 Unused 0 Integer Big Byte 12 Unused 0
     * Integer Big Byte 16 Unused 0 Integer Big Byte 20 Unused 0 Integer Big
     * Byte 24 File Length File Length Integer Big Byte 28 Version 1000 Integer
     * Little Byte 32 Shape Type Shape Type Integer Little Byte 36 Bounding Box
     * Xmin Double Little Byte 44 Bounding Box Ymin Double Little Byte 52
     * Bounding Box Xmax Double Little Byte 60 Bounding Box Ymax Double Little
     * Byte 68* Bounding Box Zmin Double Little Byte 76* Bounding Box Zmax
     * Double Little Byte 84* Bounding Box Mmin Double Little Byte 92* Bounding
     * Box Mmax Double Little
     *
     * *Unused, with value 0.0 if not measured or Z type
     *
     * File length is # of 16bit words */
    public static final int HEADER_SIZE = 100;

    /**
     * Copies of the data parsed from the buffer
     */
    public int fileLength = HEADER_SIZE; // converted to bytes

    public int version = 1000;

    public int shapeType = 0;

    public double[] bbox = new double[] { Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE,
        Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE };

    /**
     * Constructs an empty shape header
     */
    public ShapeHeader()
    {
    }

    /**
     * Constructs a shape header with a fileLength, shapeType and bbox
     *
     * @param fileLength the length of the file in bytes
     * @param shapeType the type of shape to be in the file
     * @param bbox must be a double[8] array.
     */
    /* public ShapeHeader( int fileLength, int shapeType, double[] bbox ) {
     * version = 1000; this.fileLength = fileLength; this.shapeType = shapeType;
     * this.bbox = bbox; } */

    /**
     * Parses the header out of the passed in FileChannel.
     *
     * @return boolean True/false based upon success
     *
     */
    public boolean parseHeader(ByteBuffer buffer)
    {
        boolean returnValue = true;

        buffer.order(ByteOrder.BIG_ENDIAN);
        if (buffer.getInt() != 9994)
        {
            // TODO Maybe throw an exception on these?
            // TODO Maybe ignore these?
            // TODO Maybe just log these?
            returnValue = false;
        }

        buffer.position(24);
        /* converted to bytes */
        fileLength = buffer.getInt() * 2;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        version = buffer.getInt();
        if (version != 1000)
        {
            returnValue = false;
        }

        shapeType = buffer.getInt();
        if (shapeType == 2)
        {
            shapeType = 3;
        }
        for (int i = 0; i < 8; i++)
        {
            bbox[i] = buffer.getDouble();
        }

        return returnValue;
    }

    /**
     * Writes the header into the passed in FileChannel.
     *
     * @return boolean True/false based upon success
     *
     */
    public boolean writeHeader(ByteBuffer buffer)
    {
        boolean returnValue = true;

        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(9994);

        buffer.position(24);
        /* converted to bytes */
        buffer.putInt(fileLength / 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(version);

        buffer.putInt(shapeType);

        for (int i = 0; i < 8; i++)
        {
            buffer.putDouble(bbox[i]);
        }

        return returnValue;
    }

}
