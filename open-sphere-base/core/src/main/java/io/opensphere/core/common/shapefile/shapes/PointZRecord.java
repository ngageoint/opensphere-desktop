/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.nio.ByteBuffer;

public class PointZRecord extends PointMRecord implements ZMinMax
{
    /* Byte Position Field Value Type Order Byte 0 Shape Type 11 Integer Little
     * Byte 4 X X Double Little Byte 12 Y Y Double Little Byte 20 Z Z Double
     * Little Byte 38 M M Double Little */

    private double z;

    public PointZRecord()
    {
        shapeType = 11;
    }

    public PointZRecord(double x, double y, double z, double m)
    {
        shapeType = 11;
        this.x = x;
        this.y = y;
        this.z = z;
        this.m = m;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 18;
    }

    public void setPointZ(double x, double y, double z, double m)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.m = m;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double y)
    {
        this.y = y;
    }

    @Override
    public boolean parseRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        if (!parseRecordType(buffer))
        {
            // Log something?
            returnValue = false;
        }
        else if (getShapeType() != ShapeType.POINTZ.getValue())
        {
            // Log something
            returnValue = false;
        }
        else
        {
            x = buffer.getDouble();
            y = buffer.getDouble();
            z = buffer.getDouble();
            m = buffer.getDouble();
        }
        return returnValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.shapefile.shapes.PointMRecord#writeRecord(java.nio.ByteBuffer)
     */
    @Override
    public boolean writeRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        if (!writeRecordType(buffer))
        {
            // Log something
            returnValue = false;
        }
        else if (getShapeType() != ShapeType.POINTZ.getValue())
        {
            // Log something
            returnValue = false;
        }
        else
        {
            buffer.putDouble(x);
            buffer.putDouble(y);
            buffer.putDouble(z);
            buffer.putDouble(m);
        }
        return returnValue;
    }

    @Override
    public String toString()
    {
        return super.toString() + "\n X: " + x + " Y: " + y + " Z: " + z + " M: " + m;
    }

    @Override
    public double getZMax()
    {
        return z;
    }

    @Override
    public double getZMin()
    {
        return z;
    }
}
