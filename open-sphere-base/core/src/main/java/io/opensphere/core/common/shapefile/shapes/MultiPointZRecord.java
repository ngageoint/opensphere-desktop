/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.List;

public class MultiPointZRecord extends MultiPointMRecord implements ZMinMax
{
    /* Byte Position Field Value Type Order Byte 0 Shape Type 8 Integer Little
     * Byte 4 Box Box Double[4] Little Byte 36 NumPoints NumPoints Integer
     * Little Byte 40 Points Points Point[NumPoints] Little Byte X* Zmin Zmin
     * Double Little Byte X+8* Zmax Zmax Double Little Byte X+16* Zarray Zarray
     * Double[NumPoints] Little Byte Y* Mmin Mmin Double Little Byte Y+8* Mmax
     * Mmax Double Little Byte YX+16* Marray Marray Double[NumPoints] Little */

    protected double zMin;

    protected double zMax;

    protected double[] zValues;

    public MultiPointZRecord()
    {
        shapeType = 18;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 22 + 10 * (points == null ? 0 : points.length) + 8 + 4 * (measurements == null ? 0 : measurements.length) + 8
                + 4 * (zValues == null ? 0 : zValues.length);
    }

    public MultiPointZRecord(List<Point2D.Double> pointList, List<Double> zList, List<Double> measurementList)
    {
        this();

        if (pointList == null || measurementList == null || zList == null)
        {
            throw new NullPointerException();
        }

        if (pointList.size() == 0)
        {
            throw new IllegalArgumentException("List must contain data!");
        }

        if (pointList.size() != measurementList.size())
        {
            throw new IllegalArgumentException("Point and Measurment List Must be the same size!");
        }

        if (pointList.size() != zList.size())
        {
            throw new IllegalArgumentException("Point and Z List Must be the same size!");
        }

        setPoints(pointList);
        addMeasurements(zList);
        addZRecords(zList);
    }

    public void addZRecords(List<Double> zList)
    {
        if (zList == null)
        {
            throw new NullPointerException();
        }

        int numZRecords = zValues == null ? 0 : zValues.length;
        numZRecords += zList.size();

        if (numZRecords != points.length)
        {
            throw new IllegalArgumentException("Point and Z List Must be the same size!");
        }

        // If we don't already have a z record set
        // prep the min/max
        if (zValues == null)
        {
            zMin = Double.MAX_VALUE;
            zMax = Double.MIN_VALUE;
        }

        // Take care of the measurements array, rebuild if necessary
        if (zValues == null)
        {
            zValues = new double[zList.size()];
            for (int i = 0; i < zList.size(); i++)
            {
                zValues[i] = zList.get(i);

                if (zValues[i] < zMin)
                {
                    zMin = zValues[i];
                }

                if (zValues[i] > zMax)
                {
                    zMax = zValues[i];
                }
            }
        }
        else
        {
            double[] meas2 = new double[zValues.length + 1];
            System.arraycopy(zValues, 0, meas2, 0, zValues.length);

            for (int i = 0; i < zList.size(); i++)
            {
                meas2[i + zValues.length] = zList.get(i);

                // Also amend the bounding range if necessary
                if (zValues[i + zValues.length] < zMin)
                {
                    zMin = zValues[i + zValues.length];
                }

                if (zValues[i + zValues.length] > zMax)
                {
                    zMax = zValues[i + zValues.length];
                }
            }
            zValues = meas2;
        }
    }

    @Override
    public double getZMin()
    {
        return zMin;
    }

    @Override
    public double getZMax()
    {
        return zMax;
    }

    public double[] getZRecords()
    {
        return zValues;
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
        else if (getShapeType() != ShapeType.MULTIPOINTZ.getValue())
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

            zMin = buffer.getDouble();
            zMax = buffer.getDouble();
            zValues = new double[numPoints];
            for (int i = 0; i < numPoints; i++)
            {
                zValues[i] = buffer.getDouble();
            }

            measurementMin = buffer.getDouble();
            measurementMax = buffer.getDouble();
            measurements = new double[numPoints];
            for (int i = 0; i < numPoints; i++)
            {
                measurements[i] = buffer.getDouble();
            }
        }
        return returnValue;
    }

    @Override
    public boolean writeRecord(ByteBuffer buffer)
    {
        boolean returnValue = true;
        if (!super.parseRecord(buffer))
        {
            // Log
            returnValue = false;
        }
        else if (getShapeType() != ShapeType.MULTIPOINTZ.getValue())
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

            buffer.putDouble(zMin);
            buffer.putDouble(zMax);
            for (int i = 0; i < numPoints; i++)
            {
                buffer.putDouble(zValues[i]);
            }

            buffer.putDouble(measurementMin);
            buffer.putDouble(measurementMax);
            for (int i = 0; i < numPoints; i++)
            {
                buffer.putDouble(measurements[i]);
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
        sb.append("Z Min: " + zMin + " Max: " + zMax);
        sb.append("Meas Min: " + measurementMin + " Max: " + measurementMax);
        sb.append("NumPoints: " + numPoints);
        for (int i = 0; i < (points == null ? 0 : points.length); i++)
        {
            sb.append("\n" + points[i].toString() + " + Z: " + zValues[i] + " Meas: " + measurements[i]);
        }
        return sb.toString();
    }
}
