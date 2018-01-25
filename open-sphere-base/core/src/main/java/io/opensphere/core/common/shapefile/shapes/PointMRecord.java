/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.nio.ByteBuffer;

public class PointMRecord extends PointRecord implements MeasureMinMax
{
    /* Byte Position Field Value Type Order Byte 0 Shape Type 21 Integer Little
     * Byte 4 X X Double Little Byte 12 Y Y Double Little Byte 20 M M Double
     * Little */

    protected double m;

    public PointMRecord()
    {
        shapeType = 21;
    }

    public PointMRecord(double x, double y, double m)
    {
        shapeType = 21;
        this.x = x;
        this.y = y;
        this.m = m;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 14;
    }

    public double getM()
    {
        return m;
    }

    public void setM(double m)
    {
        this.m = m;
    }

    @Override
    public double getMeasurementMax()
    {
        return m;
    }

    @Override
    public double getMeasurementMin()
    {
        return m;
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
        else if (getShapeType() != ShapeType.POINTM.getValue())
        {
            // Log something
            returnValue = false;
        }
        else
        {
            x = buffer.getDouble();
            y = buffer.getDouble();
            m = buffer.getDouble();
        }
        return returnValue;
    }

    @Override
    public boolean writeRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        if (!writeRecordType(buffer))
        {
            // Log something
            returnValue = false;
        }
        else if (getShapeType() != ShapeType.POINTM.getValue())
        {
            // Log something
            returnValue = false;
        }
        else
        {
            buffer.putDouble(x);
            buffer.putDouble(y);
            buffer.putDouble(m);
        }
        return returnValue;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.common.shapefile.shapes.PointRecord#toString()
     */
    @Override
    public String toString()
    {
        return super.toString() + "\n X: " + x + " Y: " + y + " M: " + m;
    }
}
