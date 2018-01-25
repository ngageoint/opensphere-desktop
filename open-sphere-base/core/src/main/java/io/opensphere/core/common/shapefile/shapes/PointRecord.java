/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;

public class PointRecord extends ShapeRecord
{
    /* Byte Position Field Value Type Order Byte 0 Shape Type 1 Integer Little
     * Byte 4 X X Double Little Byte 12 Y Y Double Little */

    protected double x;

    protected double y;

    public PointRecord()
    {
        shapeType = 1;
    }

    public PointRecord(Point2D.Double point)
    {
        shapeType = 1;
        x = point.getX();
        y = point.getY();
    }

    public PointRecord(double x, double y)
    {
        shapeType = 1;
        this.x = x;
        this.y = y;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 10;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    @Override
    public double[] getBox()
    {
        double[] box = new double[4];
        box[0] = x;
        box[1] = y;
        box[2] = x;
        box[3] = y;
        return box;
    }

    /**
     * @return the point
     */
    public Point2D.Double getPoint()
    {
        return new Point2D.Double(x, y);
    }

    public void setPoint(Point2D.Double point)
    {
        x = point.getX();
        y = point.getY();
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
        else if (getShapeType() != ShapeType.POINT.getValue())
        {
            // Log something
            returnValue = false;
        }
        else
        {
            x = buffer.getDouble();
            y = buffer.getDouble();
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
        else if (getShapeType() != ShapeType.POINT.getValue())
        {
            // Log something
            returnValue = false;
        }
        else
        {
            buffer.putDouble(x);
            buffer.putDouble(y);
        }
        return returnValue;
    }

    @Override
    public String toString()
    {
        return super.toString() + "\n X: " + x + " Y: " + y;
    }

}
