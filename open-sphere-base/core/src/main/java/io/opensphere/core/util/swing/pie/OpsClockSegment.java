package io.opensphere.core.util.swing.pie;

import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * The Class OpsClockSegment. Creates the path that consists of each portion, 1
 * slice in 1 ring or level of the ops clock. Each path consists of 2 arcs and 2
 * points. The 3rd point(p3) is added as a point of reference.
 */
public class OpsClockSegment extends Path2D.Float
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The Outer arc. */
    private final Arc2D.Float myOuterArc;

    /** The Inner arc. */
    private final Arc2D.Float myInnerArc;

    /**
     * Instantiates a new segment.
     *
     * @param x the x location of the cell
     * @param y the y location of the cell
     * @param ringThickness the ring thickness
     * @param startAngle the start angle
     * @param extent the arc length of this segment in degrees
     * @param innerCircleRadius the radius of the inner (center) circle
     */
    public OpsClockSegment(int x, int y, float ringThickness, float startAngle, float extent, float innerCircleRadius)
    {
        float doubleThickness = 2 * ringThickness;
        float diameter = doubleThickness * x + 2 * innerCircleRadius;
        myOuterArc = new Arc2D.Float(0, 0, diameter, diameter, startAngle, extent, Arc2D.OPEN);
        myInnerArc = new Arc2D.Float(ringThickness, ringThickness, diameter - doubleThickness, diameter - doubleThickness,
                startAngle + extent, -extent, Arc2D.OPEN);

        append(myOuterArc, true);
        float angle = startAngle + extent;
        Point2D p1 = getPointOnEdge(angle, diameter);
        Point2D p2 = getPointOnEdge(angle, diameter - doubleThickness);

        p2.setLocation(p2.getX() + ringThickness, p2.getY() + ringThickness);
        lineTo(p2.getX(), p2.getY());

        append(myInnerArc, true);

        angle = startAngle;
        p1 = getPointOnEdge(angle, diameter);
        p2 = getPointOnEdge(angle, diameter - doubleThickness);
        p2.setLocation(p2.getX() + ringThickness, p2.getY() + ringThickness);
        lineTo(p1.getX(), p1.getY());

        closePath();
    }

    /**
     * Gets the inner arc.
     *
     * @return the inner arc
     */
    public Arc2D.Float getInnerArc()
    {
        return myInnerArc;
    }

    /**
     * Gets the outer arc.
     *
     * @return the outer arc
     */
    public Arc2D.Float getOuterArc()
    {
        return myOuterArc;
    }

    /**
     * Gets the point on edge.
     *
     * @param circleAngle the circle angle
     * @param diameter the diameter
     * @return the point on edge
     */
    private Point2D getPointOnEdge(float circleAngle, float diameter)
    {
        float radius = diameter / 2f;
        float x = radius;
        float y = radius;

        double rads = Math.toRadians(circleAngle);

        // Find the outer point on the radial line.
        float xPosy = (float)(x + Math.cos(rads) * radius);
        float yPosy = (float)(y - Math.sin(rads) * radius);

        return new Point2D.Float(xPosy, yPosy);
    }
}
