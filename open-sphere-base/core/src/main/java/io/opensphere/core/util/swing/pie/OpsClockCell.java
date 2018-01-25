package io.opensphere.core.util.swing.pie;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * A cell associates one segment of the multi level pie to an x, y mapping. Each
 * cell is 1 pie slice in 1 ring or level of the pie. The cells are used to
 * build the structure of the pie.
 */
public class OpsClockCell extends Path2D.Float
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The x,y table location of this cell. */
    private final Point myTableCell;

    /** The Inner point. */
    private List<Point> myInnerPoints;

    /** The Outer point. */
    private List<Point> myOuterPoints;

    /** The ring label location. */
    private Point myRingLabelLocation;

    /** The Start angle. */
    private final float myStartAngle;

    /**
     * Instantiates a new cell.
     *
     * @param segment the segment
     * @param translatedXPos the translated x pos
     * @param translatedYPos the translated y pos
     * @param startAngle the start angle
     * @param x the x
     * @param y the y
     */
    public OpsClockCell(OpsClockSegment segment, float translatedXPos, float translatedYPos, float startAngle, int x, int y)
    {
        super();
        myTableCell = new Point(x, y);
        myStartAngle = startAngle;
        calculatePoints(segment, translatedXPos, translatedYPos);
        setRingLabelLocation(segment, startAngle, translatedXPos, translatedYPos);
    }

    /**
     * Gets the ring label location.
     *
     * @return the ring label location
     */
    public Point getRingLabelLocation()
    {
        return myRingLabelLocation;
    }

    /**
     * Gets the start angle.
     *
     * @return the start angle
     */
    public float getStartAngle()
    {
        return myStartAngle;
    }

    /**
     * Gets the table cell.
     *
     * @return the table cell
     */
    public Point getTableCell()
    {
        return myTableCell;
    }

    /**
     * Calculates the inner and outer points.
     *
     * @param segment the segment
     * @param x the x
     * @param y the y
     */
    private void calculatePoints(OpsClockSegment segment, float x, float y)
    {
        FlatteningPathIterator innerIter = new FlatteningPathIterator(
                segment.getInnerArc().getPathIterator(AffineTransform.getTranslateInstance(x, y)), 1);
        myInnerPoints = New.list();
        float[] innerCoords = new float[6];

        while (!innerIter.isDone())
        {
            innerIter.currentSegment(innerCoords);
            int xPoint = (int)innerCoords[0];
            int yPoint = (int)innerCoords[1];
            myInnerPoints.add(new Point(xPoint, yPoint));
            innerIter.next();
        }

        FlatteningPathIterator outerIter = new FlatteningPathIterator(
                segment.getOuterArc().getPathIterator(AffineTransform.getTranslateInstance(x, y)), 1);
        myOuterPoints = New.list();
        float[] outerCoords = new float[6];

        while (!outerIter.isDone())
        {
            outerIter.currentSegment(outerCoords);
            int xPoint = (int)outerCoords[0];
            int yPoint = (int)outerCoords[1];
            myOuterPoints.add(new Point(xPoint, yPoint));
            outerIter.next();
        }
    }

    /**
     * Sets the ring label location by getting the first point of the outer
     * translated arc and the last point of the inner translated arc.
     *
     * @param segment the segment
     * @param startAngle the start angle
     * @param x the x
     * @param y the y
     */
    private void setRingLabelLocation(OpsClockSegment segment, float startAngle, float x, float y)
    {
        // Only set the ring label pos on the 0 degree north(90) leg.
        if (startAngle == 90 && !myOuterPoints.isEmpty())
        {
            Point outerPoint = myOuterPoints.get(0);
            Point innerPoint = myInnerPoints.get(myInnerPoints.size() - 1);
            myRingLabelLocation = new Point((int)((outerPoint.x + innerPoint.x) / 2.0),
                    (int)((outerPoint.y + innerPoint.y) / 2.0));
        }
    }
}
