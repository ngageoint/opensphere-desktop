
package io.opensphere.core.common.convolve;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * This class encapsulates a suite of test routines for LeastSqLineLocator and
 * LeastSqLineSupport.  See below for details on how to use it.
 */
public class LeastSqLineTestSuite
{
    /** Minimum value of x. */
    private double minX = -10.0;

    /** Maximum value of x. */
    private double maxX = 10.0;

    /** Minimum value of y. */
    private double minY = -10.0;

    /** Maximum value of y. */
    private double maxY = 10.0;

    /** Display background color. */
    private Color bgColor = Color.BLACK;

    /** Display color of x- and y-axes. */
    private Color axisColor = Color.BLUE;

    /** Display color of lines other than coordinate axes. */
    private Color lineColor = Color.WHITE;

    /** Display color of the points. */
    private Color ptColor = Color.RED;

    /** Display color of ellipses. */
    private Color ellipseColor = Color.GREEN;

    /** The displayable x-axis. */
    private LineG xAxis = new LineG(minX, 0.0, maxX, 0.0);

    /** The displayable y-axis. */
    private LineG yAxis = new LineG(0.0, minY, 0.0, maxY);

    /** The list of lines to be displayed. */
    private LinkedList<LineG> lines = new LinkedList<>();

    /** The list of points to be displayed. */
    private LinkedList<PointG> points = new LinkedList<>();

    /** The list of ellipses to be dsiplayed. */
    private LinkedList<EllipseG> ellipses = new LinkedList<>();

    /** A class for representing a line segment in model space. */
    private static class LineG
    {
        /** The first endpoint. */
        public PointG p0;

        /** The second endpoint. */
        public PointG p1;

        /**
         * Construct from endpoint coordinates.
         *
         * @param x0 first x-coordinate
         * @param y0 first y-coordinate
         * @param x1 second x-coordinate
         * @param y1 second y-coordinate
         */
        public LineG(double x0, double y0, double x1, double y1)
        {
            p0 = new PointG(x0, y0);
            p1 = new PointG(x1, y1);
        }
    }

    /** A class for representing a point in model space. */
    private static class PointG
    {
        /** X-coordinate. */
        public double x;

        /** Y-coordinate. */
        public double y;

        /**
         * Construct from coordinate values.
         *
         * @param x0 x-coordinate
         * @param y0 y-coordinate
         */
        public PointG(double x0, double y0)
        {
            x = x0;
            y = y0;
        }

        /**
         * Construct a unit vector parallel to this one.
         *
         * @return unit vector
         */
        public PointG unit()
        {
            double mag = Math.sqrt(sq(x) + sq(y));
            if (mag == 0.0)
                return null;
            return new PointG(x / mag, y / mag);
        }
    }

    /** A class for representing an ellipse in model space. */
    private static class EllipseG
    {
        /** The points of the ellipse. */
        public List<PointG> points = new LinkedList<>();

        /**
         * Generate points for an ellipse and store them in its resident list.
         *
         * @param cov a matrix describing the shape of the ellipse
         * @param x the model coordinates of the center of the ellipse
         * @param confR scale factor of the ellipse
         * @return the generated ellipse
         */
        public static EllipseG create (double[][] cov, double[] x, double confR) {
            int n = 32;
            EllipseG ell = new EllipseG();
            for (int i = 0; i < n; i++) {
                double theta = i * 2.0 * Math.PI / n;
                double[] u = new double[] {Math.cos(theta), Math.sin(theta)};
                LeastSqLineSupport.scalarMult(confR, u);
                double[] v = LeastSqLineSupport.multVec(cov, u);
                LeastSqLineSupport.add(v, x);
                ell.points.add(new PointG(v[0], v[1]));
            }
            return ell;
        }
    }

    /** A random number generator. */
    private static Random rand = new Random();

    /**
     * Generate a bell-like random number with mean zero.
     *
     * @return a random number
     */
    private static double bell()
    {
        return (rand.nextDouble() + rand.nextDouble() - rand.nextDouble() - rand.nextDouble()) / 4.0;
    }

    /**
     * Generate a bell-like random number with the specified mean.
     *
     * @param mean the desired mean
     * @return a random number
     */
    private static double bell(double mean)
    {
        return mean + bell();
    }

    /**
     * Generate a bell-like random number with a specified mean and width.
     *
     * @param mean the mean
     * @param var the width
     *
     * @return a random number
     */
    private static double bell(double mean, double var)
    {
        return mean + var * bell();
    }

    /**
     * Main.  Change the scenario number to select the desired test.
     *
     * @param argv ignored
     */
    public static void main(String[] argv)
    {
        new LeastSqLineTestSuite().scenario5();
    }

    /**
     * Test the 2x2 matrix inversion function by multiplying a matrix by its
     * inverse on the left and right.
     */
    protected void scenario7()
    {
        double[][] m = new double[][] {{2.0, 1.0}, {0.5, 3.0}};
        double[][] mInv = LeastSqLineSupport.inverse2x2(m);
        double[][] id1 = LeastSqLineSupport.multMatrix(m, mInv);
        System.out.println("id1:");
        System.out.println("  " + id1[0][0] + "  " + id1[0][1]);
        System.out.println("  " + id1[1][0] + "  " + id1[1][1]);
        double[][] id2 = LeastSqLineSupport.multMatrix(mInv, m);
        System.out.println("id2:");
        System.out.println("  " + id2[0][0] + "  " + id2[0][1]);
        System.out.println("  " + id2[1][0] + "  " + id2[1][1]);
    }

    /**
     * Test the eigenvalue and eigenvector functions for 2x2 matrices.  Each
     * eigenvector is tested by multiplying by the matrix and dividing by the
     * eigenvalue, which should leave the vector nearly unchanged.
     */
    protected void scenario6()
    {
        System.out.println("CONF_95_RADIUS = " + PlaneUtils.CONF_95_RADIUS);
        double[][] m = new double[][] {{2.0, 1.0}, {1.0, 3.0}};
        double[] lambda = LeastSqLineSupport.eigenVal2x2(m);
        System.out.println("eigenvals:");
        for (int i = 0; i < lambda.length; i++)
            System.out.println(" -> " + lambda[i]);
        double[][] basis = LeastSqLineSupport.eigenVec2x2(m, lambda);
        System.out.println("basis[0]:");
        for (int i = 0; i < basis[0].length; i++)
            System.out.println(" -> " + basis[0][i]);
        double[] dopple0 = LeastSqLineSupport.multVec(m, basis[0]);
        LeastSqLineSupport.scalarMult(1.0 / lambda[0], dopple0);
        System.out.println("dopple0:");
        for (int i = 0; i < dopple0.length; i++)
            System.out.println(" -> " + dopple0[i]);
        System.out.println("basis[1]:");
        for (int i = 0; i < basis[1].length; i++)
            System.out.println(" -> " + basis[1][i]);
        double[] dopple1 = LeastSqLineSupport.multVec(m, basis[1]);
        LeastSqLineSupport.scalarMult(1.0 / lambda[1], dopple1);
        System.out.println("dopple1:");
        for (int i = 0; i < dopple1.length; i++)
            System.out.println(" -> " + dopple1[i]);
    }

    /**
     * Run a localization with randomness less likely to produce "stupid"
     * outputs.
     */
    protected void scenario5()
    {
        List<LineG> lineList = new LinkedList<>();
        for (int i = 0; i < 3; i++)
        {
            double y = bell(0.0, 10.0);
            lineList.add(lineOfBearing(-5.0, y, 0.5 + bell(4.0, 2.0), bell(-y * 1.5, 5.0)));
        }
        convolveForScenario(lineList);
        show();
    }

    /** Run a localization with considerable randomness. */
    protected void scenario4()
    {
        List<LineG> lineList = new LinkedList<>();
        for (int i = 0; i < 4; i++)
            lineList.add(lineOfBearing(bell(0.0, 10.0), bell(0.0, 10.0), 0.5 + bell(0.0, 1.0), bell(0.0, 1.0)));
        convolveForScenario(lineList);
        show();
    }

    /** Run a localization with a somewhat random set of inputs. */
    protected void scenario3()
    {
        List<LineG> lineList = new LinkedList<>();
        lineList.add(lineOfBearing(-1.0, 5.0, bell(1.0), bell(-3.0)));
        lineList.add(lineOfBearing(1.0, 4.0, bell(0.0), bell(-1.0)));
        lineList.add(lineOfBearing(2.0, 0.0, bell(-4.0), bell(-1.0)));
        lineList.add(lineOfBearing(0.0, -1.0, bell(6.0), bell(5.0)));
        lineList.add(lineOfBearing(2.0, 4.0, bell(-2.0), bell(-9.0)));
        convolveForScenario(lineList);

        show();
    }

    /** Run a localization with a fixed set of inputs. */
    protected void scenario2()
    {
        List<LineG> lineList = new LinkedList<>();
        lineList.add(lineOfBearing(-1.0, -2.0, 1.0, 1.0));
        lineList.add(lineOfBearing(1.0, -3.0, 1.0, 6.0));
        lineList.add(lineOfBearing(-3.0, 1.0, 5.0, 1.0));
        lineList.add(lineOfBearing(-4.0, 3.0, 5.0, -2.0));
        convolveForScenario(lineList);

        show();
    }

    /** Display some lines and points. */
    protected void scenario1()
    {
        points.add(new PointG(2.0, 3.0));
        points.add(new PointG(-1.0, 0.5));

        lines.add(new LineG(-3.0, -2.0, 2.0, -1.0));
        lines.add(new LineG(0.0, 5.0, 1.0, -6.0));

        show();
    }

    /**
     * Run the localization algorithm and convert the outputs for display.
     *
     * @param lineList a list of lines to use for localization
     */
    private void convolveForScenario(List<LineG> lineList)
    {
        lines.addAll(lineList);
        LeastSqLineLocator lsqLoc = convolve();
        double[] x = lsqLoc.getLocation();
        points.add(new PointG(x[0], x[1]));
        ellipses.add(EllipseG.create(lsqLoc.getInverseStd(), x, PlaneUtils.CONF_95_RADIUS));
    }

    /**
     * Setup and run the least-squares localization algorithm using the
     * resident lines.
     *
     * @return the locator containing a solution
     */
    private LeastSqLineLocator convolve()
    {
        LeastSqLineLocator cw = new LeastSqLineLocator();
        populateWorker(cw, lines);
        cw.localize();
        return cw;
    }

    /**
     * Setup the locator in preparation to find a solution.
     *
     * @param cw the locator
     * @param lines a list of lines
     */
    private static void populateWorker(LeastSqLineLocator cw, List<LineG> lines)
    {
        cw.setNumLines(lines.size());
        int i = 0;
        for (LineG ln :  lines)
            cw.addLine(ln.p0.x, ln.p0.y, ln.p1.x - ln.p0.x, ln.p1.y - ln.p0.y, i++);
    }

    /**
     * Create a line of bearing from primitive values.
     *
     * @param x x-coordinate of a point on the line
     * @param y y-coordinate of a point on the line
     * @param dx x-coordinate of the direction vector
     * @param dy y-coordinate of the direction vector
     * @return
     */
    private static LineG lineOfBearing(double x, double y, double dx, double dy)
    {
        return lineOfBearing(new PointG(x, y), new PointG(dx, dy));
    }

    /**
     * Create a renderable line of bearing.
     *
     * @param pt one point on the line
     * @param dir the line's direction vector
     * @return the line
     */
    private static LineG lineOfBearing(PointG pt, PointG dir)
    {
        if (dir.x == 0.0 && dir.y == 0.0)
            return null;
        PointG dirUnit = dir.unit();
        double txNeg = -100;
        double txPos = 100;
        double tyNeg = -100;
        double tyPos = 100;

        if (dirUnit.x != 0.0)
        {
            txNeg = -(pt.x + 10.0) / dirUnit.x;
            txPos = (10.0 - pt.x) / dirUnit.x;
        }
        if (dirUnit.y != 0.0)
        {
            tyNeg = -(pt.y + 10.0) / dirUnit.y;
            tyPos = (10.0 - pt.y) / dirUnit.y;
        }

        double tNeg = Math.min(0.0,
                maxNeg(maxNeg(txNeg, tyNeg), maxNeg(txPos, tyPos)));
        double tPos = Math.max(0.0,
                minPos(minPos(txPos, tyPos), minPos(txNeg, tyNeg)));

        return new LineG(pt.x + tNeg * dirUnit.x, pt.y + tNeg * dirUnit.y,
                pt.x + tPos * dirUnit.x, pt.y + tPos * dirUnit.y);
    }

    /**
     * Return the smaller of one or two nonnegative values, if possible.
     * Otherwise, it returns one of the two (it matters not which).
     *
     * @param x one value
     * @param y another value
     * @return see above
     */
    private static double minPos(double x, double y)
    {
        if (x < 0.0)
            return y;
        if (y < 0.0)
            return x;
        return Math.min(x, y);
    }

    /**
     * Return the larger of one or two nonpositive values, if possible.
     * Otherwise, it returns one of the two (it matters not which).
     *
     * @param x one value
     * @param y another value
     * @return see above
     */
    private static double maxNeg(double x, double y)
    {
        if (x > 0.0)
            return y;
        if (y > 0.0)
            return x;
        return Math.max(x, y);
    }

    /** Create and make visible a JFrame to display visible artifacts. */
    private void show()
    {
        JFrame f = new JFrame("Stuff");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new Graph(), BorderLayout.CENTER);
        f.setSize(800, 800);
        f.setVisible(true);
    }

    /**
     * Swing framework plug-in for drawing artifacts in the containing class.
     * The paint method delegates to draw (q.v.).
     */
    private class Graph extends JPanel
    {
        @Override
        public void paint(Graphics g)
        {
            Dimension d = getSize();
            draw(g, d.width, d.height);
        }
    }

    /**
     * Draw all of the resident artifacts, using standardized colors.
     *
     * @param g Swing graphics context
     * @param w pixel width of the display
     * @param h pixel height of the display
     */
    private void draw(Graphics g, int w, int h)
    {
        if (w <= 0 || h <= 0)
            return;
        double x0 = (w - 1) / 2.0;
        double y0 = (h - 1) / 2.0;
        double c = Math.min(w / (maxX - minX), h / (maxY - minY));

        g.setColor(bgColor);
        g.fillRect(0, 0, w, h);
        g.setColor(axisColor);
        drawLine(xAxis, g, x0, y0, c);
        drawLine(yAxis, g, x0, y0, c);
        g.setColor(lineColor);
        for (LineG ln :  lines)
            drawLine(ln, g, x0, y0, c);
        g.setColor(ellipseColor);
        for (EllipseG el :  ellipses)
            drawEllipse(el, g, x0, y0, c);
        g.setColor(ptColor);
        for (PointG pt :  points)
            drawPoint(pt, g, x0, y0, c);
    }

    /**
     * Draw a point (as a 3x3 pixel square).
     *
     * @param p the point
     * @param g Swing graphics context
     * @param x0 pixel position corresponding to model x = 0
     * @param y0 pixel position corresponding to model y = 0
     * @param c scale factor relating model scale to pixel scale
     */
    private static void drawPoint(PointG p, Graphics g, double x0, double y0, double c)
    {
        g.fillRect(rint(x0 + c * p.x) - 1, rint(y0 - c * p.y) - 1, 3, 3);
    }

    /**
     * Draw a line connecting two points.
     *
     * @param p0 one endpoint of the line
     * @param p1 the other endpoint of the line
     * @param g Swing graphics context
     * @param x0 pixel position corresponding to model x = 0
     * @param y0 pixel position corresponding to model y = 0
     * @param c scale factor relating model scale to pixel scale
     */
    private static void drawLine(PointG p0, PointG p1, Graphics g, double x0, double y0, double c)
    {
        g.drawLine(rint(x0 + c * p0.x), rint(y0 - c * p0.y),
                rint(x0 + c * p1.x), rint(y0 - c * p1.y));
    }

    /**
     * Draw a line.
     *
     * @param ln the line
     * @param g Swing graphics context
     * @param x0 pixel position corresponding to model x = 0
     * @param y0 pixel position corresponding to model y = 0
     * @param c scale factor relating model scale to pixel scale
     */
    private static void drawLine(LineG ln, Graphics g, double x0, double y0, double c)
    {
        drawLine(ln.p0, ln.p1, g, x0, y0, c);
    }

    /**
     * Draw an ellipse.
     *
     * @param e the ellipse
     * @param g Swing graphics context
     * @param x0 pixel position corresponding to model x = 0
     * @param y0 pixel position corresponding to model y = 0
     * @param c scale factor relating model scale to pixel scale
     */
    private static void drawEllipse(EllipseG e, Graphics g, double x0, double y0, double c)
    {
        PointG p0 = null;
        PointG p1 = null;
        for (PointG p2 :  e.points) {
            if (p1 != null)
                drawLine(p1, p2, g, x0, y0, c);
            p1 = p2;
            if (p0 == null)
                p0 = p1;
        }
        if (p1 != p0 && p1 != null)
            drawLine(p1, p0, g, x0, y0, c);
    }

    /**
     * Round a double to the nearest int.
     *
     * @param t a double value
     * @return the nearest int value
     */
    private static int rint(double t)
    {
        return (int)Math.round(t);
    }

    /**
     * Square function.
     *
     * @param t a value
     * @return <i>t</i> squared
     */
    private static double sq(double t)
    {
        return t * t;
    }
}
