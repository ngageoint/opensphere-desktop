package io.opensphere.core.common.geom;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * The <code>PolygonHD</code> class encapsulates a description of a closed,
 * two-dimensional region within a coordinate space. This region is bounded by
 * an arbitrary number of line segments, each of which is one side of the
 * PolygonHD. Internally, a PolygonHD comprises of a list of {@code (x,y)}
 * coordinate pairs, where each pair defines a <i>vertex</i> of the PolygonHD,
 * and two successive pairs are the endpoints of a line that is a side of the
 * PolygonHD. The first and final pairs of {@code (x,y)} points are joined by a
 * line segment that closes the PolygonHD. This <code>PolygonHD</code> is
 * defined with an even-odd winding rule. See
 * {@link java.awt.geom.PathIterator#WIND_EVEN_ODD WIND_EVEN_ODD} for a
 * definition of the even-odd winding rule. This class's hit-testing methods,
 * which include the <code>contains</code>, <code>intersects</code> and
 * <code>inside</code> methods, use the <i>insideness</i> definition described
 * in the {@link Shape} class comments.
 */
public class PolygonHD implements Shape, java.io.Serializable
{

    /**
     * The total number of points. The value of <code>npoints</code> represents
     * the number of valid points in this <code>PolygonHD</code> and might be
     * less than the number of elements in {@link #xpoints xpoints} or
     * {@link #ypoints ypoints}. This value can be NULL.
     *
     * @serial
     * @see #addPoint(int, int)
     * @since 1.0
     */
    public int npoints;

    /**
     * The array of X coordinates. The number of elements in this array might be
     * more than the number of X coordinates in this <code>PolygonHD</code>. The
     * extra elements allow new points to be added to this
     * <code>PolygonHD</code> without re-creating this array. The value of
     * {@link #npoints npoints} is equal to the number of valid points in this
     * <code>PolygonHD</code>.
     *
     * @serial
     * @see #addPoint(int, int)
     * @since 1.0
     */
    public double[] xpoints;

    /**
     * The array of Y coordinates. The number of elements in this array might be
     * more than the number of Y coordinates in this <code>PolygonHD</code>. The
     * extra elements allow new points to be added to this
     * <code>PolygonHD</code> without re-creating this array. The value of
     * <code>npoints</code> is equal to the number of valid points in this
     * <code>PolygonHD</code>.
     *
     * @serial
     * @see #addPoint(int, int)
     * @since 1.0
     */
    public double[] ypoints;

    /**
     * The bounds of this {@code PolygonHD}. This value can be null.
     *
     * @serial
     * @see #getBoundingBox()
     * @see #getBounds()
     * @since 1.0
     */
    protected Rectangle2D.Double bounds;

    /* JDK 1.1 serialVersionUID */
    private static final long serialVersionUID = -6460061437900069969L;

    /* Default length for xpoints and ypoints. */
    private static final int MIN_LENGTH = 4;

    /**
     * Creates an empty PolygonHD.
     *
     * @since 1.0
     */
    public PolygonHD()
    {
        xpoints = new double[MIN_LENGTH];
        ypoints = new double[MIN_LENGTH];
    }

    /**
     * Constructs and initializes a <code>PolygonHD</code> from the specified
     * parameters.
     *
     * @param xpoints an array of X coordinates
     * @param ypoints an array of Y coordinates
     * @param npoints the total number of points in the <code>PolygonHD</code>
     * @exception NegativeArraySizeException if the value of
     *                <code>npoints</code> is negative.
     * @exception IndexOutOfBoundsException if <code>npoints</code> is greater
     *                than the length of <code>xpoints</code> or the length of
     *                <code>ypoints</code>.
     * @exception NullPointerException if <code>xpoints</code> or
     *                <code>ypoints</code> is <code>null</code>.
     * @since 1.0
     */
    public PolygonHD(double[] xpoints, double[] ypoints, int npoints)
    {
        // Fix 4489009: should throw IndexOutofBoundsException instead
        // of OutofMemoryException if npoints is huge and > {x,y}points.length
        if (npoints > xpoints.length || npoints > ypoints.length)
        {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || " + "npoints > ypoints.length");
        }
        // Fix 6191114: should throw NegativeArraySizeException with
        // negative npoints
        if (npoints < 0)
        {
            throw new NegativeArraySizeException("npoints < 0");
        }
        // Fix 6343431: Applet compatibility problems if arrays are not
        // exactly npoints in length
        this.npoints = npoints;
        this.xpoints = PolygonHD.copyDoubleArray(xpoints, npoints);
        this.ypoints = PolygonHD.copyDoubleArray(ypoints, npoints);
    }

    public PolygonHD(Point2D.Double[] points, int npoints)
    {
        if (npoints > points.length)
        {
            throw new IndexOutOfBoundsException("npoints > points.length || ");
        }
        if (npoints < 0)
        {
            throw new NegativeArraySizeException("npoints < 0");
        }
        this.npoints = npoints;
        xpoints = new double[npoints];
        ypoints = new double[npoints];
        for (int i = 0; i < npoints; i++)
        {
            xpoints[i] = points[i].x;
            ypoints[i] = points[i].y;
        }
    }

    /**
     * Resets this <code>PolygonHD</code> object to an empty PolygonHD. The
     * coordinate arrays and the data in them are left untouched but the number
     * of points is reset to zero to mark the old vertex data as invalid and to
     * start accumulating new vertex data at the beginning. All
     * internally-cached data relating to the old vertices are discarded. Note
     * that since the coordinate arrays from before the reset are reused,
     * creating a new empty <code>PolygonHD</code> might be more memory
     * efficient than resetting the current one if the number of vertices in the
     * new PolygonHD data is significantly smaller than the number of vertices
     * in the data from before the reset.
     *
     * @see java.awt.PolygonHD#invalidate
     * @since 1.4
     */
    public void reset()
    {
        npoints = 0;
        bounds = null;
    }

    /**
     * Invalidates or flushes any internally-cached data that depends on the
     * vertex coordinates of this <code>PolygonHD</code>. This method should be
     * called after any direct manipulation of the coordinates in the
     * <code>xpoints</code> or <code>ypoints</code> arrays to avoid inconsistent
     * results from methods such as <code>getBounds</code> or
     * <code>contains</code> that might cache data from earlier computations
     * relating to the vertex coordinates.
     *
     * @see java.awt.PolygonHD#getBounds
     * @since 1.4
     */
    public void invalidate()
    {
        bounds = null;
    }

    /**
     * Calculates the bounding box of the points passed to the constructor. Sets
     * <code>bounds</code> to the result.
     *
     * @param xpoints array of <i>x</i> coordinates
     * @param ypoints array of <i>y</i> coordinates
     * @param npoints the total number of points
     */
    void calculateBounds(double[] xpoints, double[] ypoints, int npoints)
    {
        double boundsMinX = Double.MAX_VALUE;
        double boundsMinY = Double.MAX_VALUE;
        double boundsMaxX = Double.MIN_VALUE;
        double boundsMaxY = Double.MIN_VALUE;

        for (int i = 0; i < npoints; i++)
        {
            double x = xpoints[i];
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);
            double y = ypoints[i];
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        bounds = new Rectangle2D.Double(boundsMinX, boundsMinY, boundsMaxX - boundsMinX, boundsMaxY - boundsMinY);
    }

    /* Resizes the bounding box to accomodate the specified coordinates.
     *
     * @param x,&nbsp;y the specified coordinates */
    void updateBounds(double x, double y)
    {
        if (x < bounds.x)
        {
            bounds.width = bounds.width + (bounds.x - x);
            bounds.x = x;
        }
        else
        {
            bounds.width = Math.max(bounds.width, x - bounds.x);
            // bounds.x = bounds.x;
        }

        if (y < bounds.y)
        {
            bounds.height = bounds.height + (bounds.y - y);
            bounds.y = y;
        }
        else
        {
            bounds.height = Math.max(bounds.height, y - bounds.y);
            // bounds.y = bounds.y;
        }
    }

    /**
     * Appends the specified coordinates to this <code>PolygonHD</code>.
     * <p>
     * If an operation that calculates the bounding box of this
     * <code>PolygonHD</code> has already been performed, such as
     * <code>getBounds</code> or <code>contains</code>, then this method updates
     * the bounding box.
     *
     * @param x the specified X coordinate
     * @param y the specified Y coordinate
     * @see java.awt.PolygonHD#getBounds
     * @see java.awt.PolygonHD#contains
     * @since 1.0
     */
    public void addPoint(double x, double y)
    {
        if (npoints >= xpoints.length || npoints >= ypoints.length)
        {
            int newLength = npoints * 2;
            // Make sure that newLength will be greater than MIN_LENGTH and
            // aligned to the power of 2
            if (newLength < MIN_LENGTH)
            {
                newLength = MIN_LENGTH;
            }
            else if ((newLength & newLength - 1) != 0)
            {
                newLength = Integer.highestOneBit(newLength);
            }

            xpoints = PolygonHD.copyDoubleArray(xpoints, newLength);
            ypoints = PolygonHD.copyDoubleArray(ypoints, newLength);
        }
        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;
        if (bounds != null)
        {
            updateBounds(x, y);
        }
    }

    /**
     * Gets the bounding box of this <code>PolygonHD</code>. The bounding box is
     * the smallest {@link Rectangle} whose sides are parallel to the x and y
     * axes of the coordinate space, and can completely contain the
     * <code>PolygonHD</code>.
     *
     * @return a <code>Rectangle</code> that defines the bounds of this
     *         <code>PolygonHD</code>.
     * @since 1.1
     */
    @Override
    public Rectangle getBounds()
    {
        return getBounds2D().getBounds();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    @Override
    public Rectangle2D getBounds2D()
    {
        if (npoints == 0)
        {
            return new Rectangle();
        }
        if (bounds == null)
        {
            calculateBounds(xpoints, ypoints, npoints);
        }
        return bounds;
    }

    /**
     * Determines whether the specified {@link Point} is inside this
     * <code>PolygonHD</code>.
     *
     * @param p the specified <code>Point</code> to be tested
     * @return <code>true</code> if the <code>PolygonHD</code> contains the
     *         <code>Point</code>; <code>false</code> otherwise.
     * @see #contains(double, double)
     * @since 1.0
     */
    public boolean contains(Point p)
    {
        return contains(p.x, p.y);
    }

    /**
     * Determines whether the specified coordinates are inside this
     * <code>PolygonHD</code>.
     * <p>
     *
     * @param x the specified X coordinate to be tested
     * @param y the specified Y coordinate to be tested
     * @return {@code true} if this {@code PolygonHD} contains the specified
     *         coordinates {@code (x,y)}; {@code false} otherwise.
     * @see #contains(double, double)
     * @since 1.1
     */
    public boolean contains(int x, int y)
    {
        return contains((double)x, (double)y);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    @Override
    public boolean contains(double x, double y)
    {
        if (npoints <= 2 || !getBounds2D().contains(x, y))
        {
            return false;
        }
        int hits = 0;

        double lastx = xpoints[npoints - 1];
        double lasty = ypoints[npoints - 1];
        double curx, cury;

        // Walk the edges of the PolygonHD
        for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++)
        {
            curx = xpoints[i];
            cury = ypoints[i];

            if (cury == lasty)
            {
                continue;
            }

            double leftx;
            if (curx < lastx)
            {
                if (x >= lastx)
                {
                    continue;
                }
                leftx = curx;
            }
            else
            {
                if (x >= curx)
                {
                    continue;
                }
                leftx = lastx;
            }

            double test1, test2;
            if (cury < lasty)
            {
                if (y < cury || y >= lasty)
                {
                    continue;
                }
                if (x < leftx)
                {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            }
            else
            {
                if (y < lasty || y >= cury)
                {
                    continue;
                }
                if (x < leftx)
                {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }

            if (test1 < test2 / (lasty - cury) * (lastx - curx))
            {
                hits++;
            }
        }

        return (hits & 1) != 0;
    }

    private Crossings getCrossings(double xlo, double ylo, double xhi, double yhi)
    {
        Crossings cross = new Crossings.EvenOdd(xlo, ylo, xhi, yhi);
        double lastx = xpoints[npoints - 1];
        double lasty = ypoints[npoints - 1];
        double curx, cury;

        // Walk the edges of the PolygonHD
        for (int i = 0; i < npoints; i++)
        {
            curx = xpoints[i];
            cury = ypoints[i];
            if (cross.accumulateLine(lastx, lasty, curx, cury))
            {
                return null;
            }
            lastx = curx;
            lasty = cury;
        }

        return cross;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    @Override
    public boolean contains(Point2D p)
    {
        return contains(p.getX(), p.getY());
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    @Override
    public boolean intersects(double x, double y, double w, double h)
    {
        if (npoints <= 0 || !getBounds2D().intersects(x, y, w, h))
        {
            return false;
        }

        Crossings cross = getCrossings(x, y, x + w, y + h);
        return cross == null || !cross.isEmpty();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    @Override
    public boolean intersects(Rectangle2D r)
    {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    @Override
    public boolean contains(double x, double y, double w, double h)
    {
        if (npoints <= 0 || !getBounds2D().intersects(x, y, w, h))
        {
            return false;
        }

        Crossings cross = getCrossings(x, y, x + w, y + h);
        return cross != null && cross.covers(y, y + h);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.2
     */
    @Override
    public boolean contains(Rectangle2D r)
    {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * Returns an iterator object that iterates along the boundary of this
     * <code>PolygonHD</code> and provides access to the geometry of the outline
     * of this <code>PolygonHD</code>. An optional {@link AffineTransform} can
     * be specified so that the coordinates returned in the iteration are
     * transformed accordingly.
     *
     * @param at an optional <code>AffineTransform</code> to be applied to the
     *            coordinates as they are returned in the iteration, or
     *            <code>null</code> if untransformed coordinates are desired
     * @return a {@link PathIterator} object that provides access to the
     *         geometry of this <code>PolygonHD</code>.
     * @since 1.2
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at)
    {
        return new PolygonHDPathIterator(this, at);
    }

    /**
     * Returns an iterator object that iterates along the boundary of the
     * <code>Shape</code> and provides access to the geometry of the outline of
     * the <code>Shape</code>. Only SEG_MOVETO, SEG_LINETO, and SEG_CLOSE point
     * types are returned by the iterator. Since PolygonHDs are already flat,
     * the <code>flatness</code> parameter is ignored. An optional
     * <code>AffineTransform</code> can be specified in which case the
     * coordinates returned in the iteration are transformed accordingly.
     *
     * @param at an optional <code>AffineTransform</code> to be applied to the
     *            coordinates as they are returned in the iteration, or
     *            <code>null</code> if untransformed coordinates are desired
     * @param flatness the maximum amount that the control points for a given
     *            curve can vary from colinear before a subdivided curve is
     *            replaced by a straight line connecting the endpoints. Since
     *            PolygonHDs are already flat the <code>flatness</code>
     *            parameter is ignored.
     * @return a <code>PathIterator</code> object that provides access to the
     *         <code>Shape</code> object's geometry.
     * @since 1.2
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness)
    {
        return getPathIterator(at);
    }

    class PolygonHDPathIterator implements PathIterator
    {
        PolygonHD poly;

        AffineTransform transform;

        int index;

        public PolygonHDPathIterator(PolygonHD pg, AffineTransform at)
        {
            poly = pg;
            transform = at;
            if (pg.npoints == 0)
            {
                // Prevent a spurious SEG_CLOSE segment
                index = 1;
            }
        }

        /**
         * Returns the winding rule for determining the interior of the path.
         *
         * @return an integer representing the current winding rule.
         * @see PathIterator#WIND_NON_ZERO
         */
        @Override
        public int getWindingRule()
        {
            return WIND_EVEN_ODD;
        }

        /**
         * Tests if there are more points to read.
         *
         * @return <code>true</code> if there are more points to read;
         *         <code>false</code> otherwise.
         */
        @Override
        public boolean isDone()
        {
            return index > poly.npoints;
        }

        /**
         * Moves the iterator forwards, along the primary direction of
         * traversal, to the next segment of the path when there are more points
         * in that direction.
         */
        @Override
        public void next()
        {
            index++;
        }

        /**
         * Returns the coordinates and type of the current path segment in the
         * iteration. The return value is the path segment type: SEG_MOVETO,
         * SEG_LINETO, or SEG_CLOSE. A <code>float</code> array of length 2 must
         * be passed in and can be used to store the coordinates of the
         * point(s). Each point is stored as a pair of <code>float</code>
         * x,&nbsp;y coordinates. SEG_MOVETO and SEG_LINETO types return one
         * point, and SEG_CLOSE does not return any points.
         *
         * @param coords a <code>float</code> array that specifies the
         *            coordinates of the point(s)
         * @return an integer representing the type and coordinates of the
         *         current path segment.
         * @see PathIterator#SEG_MOVETO
         * @see PathIterator#SEG_LINETO
         * @see PathIterator#SEG_CLOSE
         */
        @Override
        public int currentSegment(float[] coords)
        {
            if (index >= poly.npoints)
            {
                return SEG_CLOSE;
            }
            coords[0] = (float)poly.xpoints[index];
            coords[1] = (float)poly.ypoints[index];
            if (transform != null)
            {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return index == 0 ? SEG_MOVETO : SEG_LINETO;
        }

        /**
         * Returns the coordinates and type of the current path segment in the
         * iteration. The return value is the path segment type: SEG_MOVETO,
         * SEG_LINETO, or SEG_CLOSE. A <code>double</code> array of length 2
         * must be passed in and can be used to store the coordinates of the
         * point(s). Each point is stored as a pair of <code>double</code>
         * x,&nbsp;y coordinates. SEG_MOVETO and SEG_LINETO types return one
         * point, and SEG_CLOSE does not return any points.
         *
         * @param coords a <code>double</code> array that specifies the
         *            coordinates of the point(s)
         * @return an integer representing the type and coordinates of the
         *         current path segment.
         * @see PathIterator#SEG_MOVETO
         * @see PathIterator#SEG_LINETO
         * @see PathIterator#SEG_CLOSE
         */
        @Override
        public int currentSegment(double[] coords)
        {
            if (index >= poly.npoints)
            {
                return SEG_CLOSE;
            }
            coords[0] = poly.xpoints[index];
            coords[1] = poly.ypoints[index];
            if (transform != null)
            {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return index == 0 ? SEG_MOVETO : SEG_LINETO;
        }
    }

    public static double[] copyDoubleArray(double[] orig, int npoints)
    {
        if (orig == null)
        {
            return null;
        }

        double[] copy = new double[npoints];
        for (int i = 0; i < orig.length; i++)
        {
            copy[i] = orig[i];
        }

        return copy;
    }
}
