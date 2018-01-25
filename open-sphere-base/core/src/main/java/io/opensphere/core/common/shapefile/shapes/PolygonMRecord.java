/**
 *
 */
package io.opensphere.core.common.shapefile.shapes;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The first in the "m" series that we have implemented. Brat uses this, but
 * we're not yet sure why.
 *
 * From ESRI's docs: "Measured Shape Types in X,Y Space: Shapes of this type
 * have an additional coordinate¾M. Note that "no data" value can be specified
 * as a value for M.
 */
public class PolygonMRecord extends PolygonRecord implements MeasureMinMax
{
    /* From PolygonM Line: Byte Position Field Value Type Order Byte 0 Shape
     * Type 3 Integer Little Byte 4 Box Box Double[4] Little Byte 36 NumParts
     * NumParts Integer Little Byte 40 NumPoints NumPoints Integer Little Byte
     * 44 Parts Parts Integer[NumParts] Little Byte XX Points Points
     * Point[NumPoints] Little
     *
     * Where Point = Double, Double (x,y)
     *
     * The "M" adds" Byte Y Mmin Mmin Double Little Byte Y+8 Mmax Mmax Double
     * Little Byte Y+16 Marray Marray Double[NumPoints] Little */

    protected double measurementMin;

    protected double measurementMax;

    protected double[] measurements;

    /**
     *
     */
    public PolygonMRecord()
    {
        shapeType = 25;
    }

    @Override
    public int getContentLengthInWords()
    {
        return 30 + 2 * (parts == null ? 0 : parts.length) + 10 * (points == null ? 0 : points.length) + 8
                + 4 * (measurements == null ? 0 : measurements.length);
    }

    public PolygonMRecord(List<Point2D.Double> partPointList, List<Double> partMeasurementList)
    {
        this();

        if (partPointList == null || partMeasurementList == null)
        {
            throw new NullPointerException();
        }

        if (partPointList.size() == 0)
        {
            throw new IllegalArgumentException("List must contain data!");
        }

        if (partPointList.size() != partMeasurementList.size())
        {
            throw new IllegalArgumentException("Point and Measurment List Must be the same size!");
        }

        addPart(partPointList, partMeasurementList);
    }

    @Override
    public void addPart(List<Point2D.Double> partPointList)
    {
        ArrayList<Double> measList = new ArrayList<>(partPointList.size());
        for (int i = 0; i < partPointList.size(); i++)
        {
            measList.add(0.0);
        }

        addPart(partPointList, measList);
    }

    public void addPart(List<Point2D.Double> partPointList, List<Double> partMeasurementList)
    {
        if (partPointList == null || partMeasurementList == null)
        {
            throw new NullPointerException();
        }

        if (partPointList.size() != partMeasurementList.size())
        {
            throw new IllegalArgumentException("Point and Measurment List Must be the same size!");
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
            box[2] = Double.MIN_VALUE;
            // Ymax
            box[3] = Double.MIN_VALUE;
        }

        // If we don't already have a measurement set
        // prep the min/max
        if (measurements == null)
        {
            measurementMin = Double.MAX_VALUE;
            measurementMax = Double.MIN_VALUE;
        }

        numParts++;
        numPoints += partPointList.size();

        // Take care of the points array, rebuilding it if necessary.
        int nextPartIndex = 0;
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

            parts2[parts2.length - 1] = nextPartIndex;
            parts = parts2;
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
        else if (getShapeType() != ShapeType.POLYGONM.getValue())
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
        else if (getShapeType() != ShapeType.POLYGONM.getValue())
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
        sb.append("MeasurementMin: " + measurementMin + "\n");
        sb.append("MeasurementMax: " + measurementMax);
        for (double meas : measurements)
        {
            sb.append("\n" + meas);
        }
        return sb.toString();
    }
}
