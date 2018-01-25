/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.List;

public class MultiPointMRecord extends MultiPointRecord implements MeasureMinMax
{
    /* Byte Position Field Value Type Order Byte 0 Shape Type 8 Integer Little
     * Byte 4 Box Box Double[4] Little Byte 36 NumPoints NumPoints Integer
     * Little Byte 40 Points Points Point[NumPoints] Little Byte X* Mmin Mmin
     * Double Little Byte X+8* Mmax Mmax Double Little Byte X+16* Marray Marray
     * Double[Numpoints] Little */

    protected double measurementMin;

    protected double measurementMax;

    protected double[] measurements;

    public MultiPointMRecord()
    {
        shapeType = 28;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 22 + 10 * (points == null ? 0 : points.length) + 8 + 4 * (measurements == null ? 0 : measurements.length);
    }

    public MultiPointMRecord(List<Point2D.Double> pointList, List<Double> measurementList)
    {
        this();

        if (pointList == null || measurementList == null)
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

        setPoints(pointList);
        addMeasurements(measurementList);
    }

    public void addMeasurements(List<Double> partMeasurementList)
    {
        if (partMeasurementList == null)
        {
            throw new NullPointerException();
        }

        int numMeasurements = measurements == null ? 0 : measurements.length;
        numMeasurements += partMeasurementList.size();

        if (numMeasurements != points.length)
        {
            throw new IllegalArgumentException("Point and Measurment List Must be the same size!");
        }

        // If we don't already have a measurement set
        // prep the min/max
        if (measurements == null)
        {
            measurementMin = Double.MAX_VALUE;
            measurementMax = Double.MIN_VALUE;
        }

        // Take care of the measurements array, rebuild if necessary
        if (measurements == null)
        {
            measurements = new double[partMeasurementList.size()];
            for (int i = 0; i < partMeasurementList.size(); i++)
            {
                measurements[i] = partMeasurementList.get(i);

                if (measurements[i] < measurementMin)
                {
                    measurementMin = measurements[i];
                }

                if (measurements[i] > measurementMax)
                {
                    measurementMax = measurements[i];
                }
            }
        }
        else
        {
            double[] meas2 = new double[measurements.length + 1];
            System.arraycopy(measurements, 0, meas2, 0, measurements.length);

            for (int i = 0; i < partMeasurementList.size(); i++)
            {
                meas2[i + measurements.length] = partMeasurementList.get(i);

                // Also amend the bounding range if necessary
                if (measurements[i + measurements.length] < measurementMin)
                {
                    measurementMin = measurements[i + measurements.length];
                }

                if (measurements[i + measurements.length] > measurementMax)
                {
                    measurementMax = measurements[i + measurements.length];
                }
            }
            measurements = meas2;
        }
    }

    @Override
    public double getMeasurementMin()
    {
        return measurementMin;
    }

    @Override
    public double getMeasurementMax()
    {
        return measurementMax;
    }

    public double[] getMeasurements()
    {
        return measurements;
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
        else if (getShapeType() != ShapeType.MULTIPOINTM.getValue())
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
        else if (getShapeType() != ShapeType.MULTIPOINTM.getValue())
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
        sb.append("Meas Min: " + measurementMin + " Max: " + measurementMax);
        sb.append("NumPoints: " + numPoints);
        for (int i = 0; i < (points == null ? 0 : points.length); i++)
        {
            sb.append("\n" + points[i].toString() + " Meas: " + measurements[i]);
        }
        return sb.toString();
    }
}
