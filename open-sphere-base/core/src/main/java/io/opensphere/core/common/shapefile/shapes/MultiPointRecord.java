/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.List;

public class MultiPointRecord extends ShapeRecord
{
    /* Same definition as a MultiPoint. Intentionally using an independent class
     * for consistency and in case something needs to be handled differently.
     *
     * Byte Position Field Value Type Order Byte 0 Shape Type 8 Integer Little
     * Byte 4 Box Box Double[4] Little Byte 36 NumPoints NumPoints Integer
     * Little Byte 40 Points Points Point[NumPoints] Little */
    protected double[] box;

    protected int numPoints;

    protected Point2D.Double[] points;

    public MultiPointRecord()
    {
        shapeType = 5;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 22 + 10 * (points == null ? 0 : points.length);
    }

    public MultiPointRecord(List<Point2D.Double> partPointList)
    {
        setPoints(partPointList);
    }

    public void setPoints(List<Point2D.Double> partPointList)
    {
        if (partPointList == null)
        {
            throw new NullPointerException();
        }

        // If we don't already have a bounding box
        // the create one and prep it for new bounds.
        box = new double[4];
        // Xmin
        box[0] = Double.MAX_VALUE;
        // Ymin
        box[1] = Double.MAX_VALUE;
        // Xmax
        box[2] = Double.MIN_VALUE;
        // Ymax
        box[3] = Double.MIN_VALUE;

        numPoints += partPointList.size();

        // Take care of the points array, rebuilding it if necessary.

        points = new Point2D.Double[partPointList.size()];
        for (int i = 0; i < partPointList.size(); i++)
        {
            points[i] = partPointList.get(i);

            if (points[i].getX() < box[0])
            {
                box[0] = points[i].getX();
            }

            if (points[i].getY() < box[1])
            {
                box[1] = points[i].getY();
            }

            if (points[i].getX() > box[2])
            {
                box[2] = points[i].getX();
            }

            if (points[i].getY() > box[3])
            {
                box[3] = points[i].getY();
            }
        }
    }

    /**
     * Returns the array of min x, y and max x, y.
     *
     * @return the box
     */
    @Override
    public double[] getBox()
    {
        return box;
    }

    /**
     * @return the numPoints
     */
    public int getNumPoints()
    {
        return numPoints;
    }

    /**
     * @return the points
     */
    public Point2D.Double[] getPoints()
    {
        return points;
    }

    @Override
    public boolean parseRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        if (!parseRecordType(buffer))
        {
            // Log
            returnValue = false;
        }
        else if (getShapeType() != ShapeType.POLYGON.getValue())
        {
            // Log
            returnValue = false;
        }
        else
        {
            box = new double[4];
            for (int i = 0; i < 4; i++)
            {
                box[i] = buffer.getDouble();
            }
            numPoints = buffer.getInt();
            points = new Point2D.Double[numPoints];
            for (int i = 0; i < numPoints; i++)
            {
                points[i] = new Point2D.Double();
                points[i].x = buffer.getDouble();
                points[i].y = buffer.getDouble();
            }
        }
        return returnValue;
    }

    @Override
    public boolean writeRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        if (!writeRecordType(buffer))
        {
            // Log
            returnValue = false;
        }
        else if (getShapeType() != ShapeType.POLYGON.getValue())
        {
            // Log
            returnValue = false;
        }
        else
        {
            for (int i = 0; i < 4; i++)
            {
                buffer.putDouble(box[i]);
            }

            buffer.putInt(numPoints);
            for (int i = 0; i < numPoints; i++)
            {
                buffer.putDouble(points[i].x);
                buffer.putDouble(points[i].y);
            }
        }
        return returnValue;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(super.toString() + "\n");
        sb.append("Bounding box: " + "\n" + new Point2D.Double(box[0], box[1]).toString() + "\n"
                + new Point2D.Double(box[2], box[3]).toString() + "\n");
        sb.append("NumPoints: " + numPoints);
        for (Point2D.Double point : points)
        {
            sb.append("\n" + point.toString());
        }
        return sb.toString();
    }
}
