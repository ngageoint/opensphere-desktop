/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.List;

public class PolyLineRecord extends ShapeRecord
{
    /* Byte Position Field Value Type Order Byte 0 Shape Type 3 Integer Little
     * Byte 4 Box Box Double[4] Little Byte 36 NumParts NumParts Integer Little
     * Byte 40 NumPoints NumPoints Integer Little Byte 44 Parts Parts
     * Integer[NumParts] Little Byte XX Points Points Point[NumPoints] Little
     *
     * Where Point = Double, Double (x,y) */
    protected double[] box;

    protected int numParts;

    protected int numPoints;

    protected int[] parts;

    protected Point2D.Double[] points;

    public PolyLineRecord()
    {
        shapeType = 3;
    }

    public PolyLineRecord(List<Point2D.Double> partPointList)
    {
        this();
        addPart(partPointList);
    }

    @Override
    public int getContentLengthInWords()
    {
        return 22 + 2 * (parts == null ? 0 : parts.length) + 8 * (points == null ? 0 : points.length);
    }

    public void addPart(List<Point2D.Double> partPointList)
    {
        if (partPointList == null)
        {
            throw new NullPointerException();
        }

        // If we don't already have a bounding box
        // the create one and prep it for new bounds.
        if (box == null)
        {
            box = new double[4];
            // Xmin
            box[0] = Double.MAX_VALUE;
            // Ymin
            box[1] = Double.MAX_VALUE;
            // Xmax
            box[2] = -Double.MAX_VALUE;
            // Ymax
            box[3] = -Double.MAX_VALUE;
        }

        // Take care of the points array, rebuilding it if necessary.
        if (points == null)
        {
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
        else
        {

            Point2D.Double[] points2 = new Point2D.Double[points.length + partPointList.size()];
            System.arraycopy(points, 0, points2, 0, points.length);

            for (int i = 0; i < partPointList.size(); i++)
            {
                points2[i + points.length] = partPointList.get(i);

                // Also amend the bounding box if necessary
                if (points2[i + points.length].getX() < box[0])
                {
                    box[0] = points2[i + points.length].getX();
                }

                if (points2[i + points.length].getY() < box[1])
                {
                    box[1] = points2[i + points.length].getY();
                }

                if (points2[i + points.length].getX() > box[2])
                {
                    box[2] = points2[i + points.length].getX();
                }

                if (points2[i + points.length].getY() > box[3])
                {
                    box[3] = points2[i + points.length].getY();
                }
            }

            points = points2;
        }

        // Take care of the parts array, rebuilding it if necessary
        if (parts == null)
        {
            parts = new int[1];
            parts[0] = 0;
        }
        else
        {
            int[] parts2 = new int[parts.length + 1];
            System.arraycopy(parts, 0, parts2, 0, parts.length);

            parts2[parts2.length - 1] = numPoints;
            parts = parts2;
        }

        numParts++;
        numPoints += partPointList.size();
    }

    /**
     * @return the box
     */
    @Override
    public double[] getBox()
    {
        return box;
    }

    /**
     * @return the numParts
     */
    public int getNumParts()
    {
        return numParts;
    }

    /**
     * @return the numPoints
     */
    public int getNumPoints()
    {
        return numPoints;
    }

    /**
     * @return the parts
     */
    public int[] getParts()
    {
        return parts;
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
        else if (getShapeType() != ShapeType.POLYLINE.getValue())
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
            numParts = buffer.getInt();
            numPoints = buffer.getInt();
            parts = new int[numParts];
            for (int i = 0; i < numParts; i++)
            {
                parts[i] = buffer.getInt();
            }
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
        else if (getShapeType() != ShapeType.POLYLINE.getValue())
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
            buffer.putInt(numParts);
            buffer.putInt(numPoints);
            for (int i = 0; i < numParts; i++)
            {
                buffer.putInt(parts[i]);
            }
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
        // ignoring parts...
        // sb.append("NumParts: " + numParts + "\n");
        sb.append("NumPoints: " + numPoints);
        for (Point2D.Double point : points)
        {
            sb.append("\n" + point.toString());
        }
        return sb.toString();
    }
}
