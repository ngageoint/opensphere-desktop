package io.opensphere.imagery.histogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import io.opensphere.core.util.Utilities;

/**
 * The Class HistoManipulationDisplayPanel.
 */
@SuppressWarnings("PMD.GodClass")
public class HistoManipulationDisplayPanel extends JPanel
{
    /** The Constant DELETE_MODE. */
    public static final int DELETE_MODE = 4;

    /** The Constant INSERT_MODE. */
    public static final int INSERT_MODE = 2;

    /** The Constant MODIFY_MODE. */
    public static final int MODIFY_MODE = 1;

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Active color breakpoint. */
    private Point myActiveColorBreakpoint;

    /** The Chart. */
    private JFreeChart myChart;

    /** The Chart panel. */
    private ChartPanel myChartPanel;

//    private AutoTiledImageLayer myAssociatedLayer;

    /** The Color breakpoint being dragged. */
    private Point myColorBreakpointBeingDragged;

    /** The Data to intensity points. */
    private final List<Point> myDataToIntensityPoints = new LinkedList<>();

    /** The Input hist values. */
    private final int[] myInputHistValues;

    /** The Mouse action mode. */
    private int myMouseActionMode = MODIFY_MODE;

    /** The Output values. */
    private int[] myOutputValues;

    /** The Rendering hints. */
    private final Map<RenderingHints.Key, Object> myRenderingHints = new HashMap<>();

    /**
     * Instantiates a new histo manipulation display panel.
     *
     * @param color the color
     */
    public HistoManipulationDisplayPanel(Color color)
    {
        myRenderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        myDataToIntensityPoints.add(new Point(60, 0));
        myDataToIntensityPoints.add(new Point(90, 50));
        myDataToIntensityPoints.add(new Point(200, 210));
        myDataToIntensityPoints.add(new Point(237, 255));
        // myDataToIntensityPoints.add(new Point(256,
        // myInputHistValues.length-1));

        int[] hugeHisto = populateDummyGaussianData(65535, 10000);
        myInputHistValues = downsampleHisto(hugeHisto, 256);
        // myInputHistValues = populateDummyGaussianData(256, 3000);
        setLayout(new BorderLayout());
        setupChart(color);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.addRenderingHints(myRenderingHints);
        final Rectangle2D screenDataArea = myChartPanel.getScreenDataArea();
        final int lftX = (int)screenDataArea.getMinX();
        final int rgtX = (int)screenDataArea.getMaxX();
        final int btmY = (int)screenDataArea.getMaxY();
        final int topY = (int)screenDataArea.getMinY();
        final int boxSide = 6;
        final int width = rgtX - lftX;
        final int height = btmY - topY;
        final double widthRatio = (double)width / myInputHistValues.length;
        final double heightRatio = (double)height / 256;

        // A side effect of this for loop is that afterwards linePoint will be
        // left
        // with the last point in the myDataToIntensityPoints list.
        Point linePoint = null;
        for (Point nextPoint : myDataToIntensityPoints)
        {
            // if this is the first point in the line, draw a line from the
            // y-axis to
            // this point at this point's y.
            if (linePoint == null)
            {
                g2.draw(new Line2D.Double(lftX, btmY - (int)(nextPoint.y * heightRatio), lftX + (int)(nextPoint.x * widthRatio),
                        btmY - (int)(nextPoint.y * heightRatio)));
                linePoint = nextPoint;
                continue;
            }
            g2.draw(new Line2D.Double(lftX + (int)(linePoint.x * widthRatio), btmY - (int)(linePoint.y * heightRatio),
                    lftX + (int)(nextPoint.x * widthRatio), btmY - (int)(nextPoint.y * heightRatio)));

            final Color savedColor = g2.getColor();
            // Draw the red outline if this point is active
            if (Utilities.sameInstance(myActiveColorBreakpoint, linePoint))
            {
                g2.setColor(Color.RED);
                g2.draw(new Rectangle2D.Double(lftX + (int)(linePoint.x * widthRatio) - 4,
                        btmY - (int)(linePoint.y * heightRatio) - 4, boxSide + 2, boxSide + 2));
                g2.setColor(savedColor);
            }

            // Draw the regular box around the point
            g2.draw(new Rectangle2D.Double(lftX + (int)(linePoint.x * widthRatio) - 3,
                    btmY - (int)(linePoint.y * heightRatio) - 3, boxSide, boxSide));

            linePoint = nextPoint;
        }
        // Draw the box around the last point, and also continue the line to the
        // right
        // side of the histogram horizontally.
        if (linePoint != null)
        {
            // draw the line to the right side of the histogram.
            g2.draw(new Line2D.Double(lftX + (int)(linePoint.x * widthRatio), btmY - (int)(linePoint.y * heightRatio),
                    lftX + (int)(myInputHistValues.length * widthRatio), btmY - (int)(linePoint.y * heightRatio)));

            final Color savedColor = g2.getColor();
            // Draw the red outline if this point is active
            if (Utilities.sameInstance(myActiveColorBreakpoint, linePoint))
            {
                g2.setColor(Color.RED);
                g2.draw(new Rectangle2D.Double(lftX + (int)(linePoint.x * widthRatio) - 4,
                        btmY - (int)(linePoint.y * heightRatio) - 4, boxSide + 2, boxSide + 2));
                g2.setColor(savedColor);
            }
            // Draw the regular box around the point
            g2.draw(new Rectangle2D.Double(lftX + (int)(linePoint.x * widthRatio) - 3,
                    btmY - (int)(linePoint.y * heightRatio) - 3, boxSide, boxSide));
        }
    }

    /**
     * Sets the mouse mode.
     *
     * @param mode the new mouse mode
     */
    public void setMouseMode(int mode)
    {
        if (mode != MODIFY_MODE && mode != INSERT_MODE && mode != DELETE_MODE)
        {
            throw new IllegalArgumentException("Argument must be one of: MODIFY_MODE, INSERT_MODE, or DELETE_MODE");
        }
        myMouseActionMode = mode;
    }

    /**
     * This function makes a new list containing the list passed as an argument,
     * but it may also contain "fake" points at the beginning or end of the list
     * if the endpoints of the passed list don't cover the whole domain.
     *
     * In other words, if the argument's first point has an X value which isn't
     * 0, a new point is added in the resulting list at the front of the list
     * with the 0 value. Similarly, if the last point doesn't have a large
     * enough X value to cover the right of the domain, a new Point is added in
     * that last position that does cover the domain. The Y values of the new
     * points are copies of the Y values of the first and last points. This
     * enables people to make histograms with declining, instead of increasing,
     * values.
     *
     * @param dataToIntensityPoints a List of intensity points. Empty is ok,
     *            though the resulting list won't be very interesting.
     *
     * @return A new list as described above. If the parameter
     *         dataToIntensityPoints is an empty list, the returned list will
     *         have only two points, one at the x,y origin and one at x,y max.
     */
    private List<Point> addEndPointsIfNecessary(List<Point> dataToIntensityPoints)
    {
        List<Point> toReturn = new LinkedList<>();

        Point firstValue = dataToIntensityPoints.get(0);
        if (firstValue == null)
        {
            toReturn.add(new Point(0, 0));
        }
        else if (firstValue.x != 0)
        {
            toReturn.add(new Point(0, firstValue.y));
        }

        toReturn.addAll(dataToIntensityPoints);

        Point lastValue = dataToIntensityPoints.get(dataToIntensityPoints.size() - 1);
        if (lastValue == null)
        {
            toReturn.add(new Point(myInputHistValues.length - 1, 255));
        }
        else if (lastValue.x != myInputHistValues.length - 1)
        {
            toReturn.add(new Point(myInputHistValues.length - 1, lastValue.y));
        }

        return toReturn;
    }

    /**
     * Chart point to java2 d.
     *
     * @param chartPoint the chart point
     * @return the point
     */
    private Point chartPointToJava2D(Point chartPoint)
    {
        final Rectangle2D dataArea = myChartPanel.getScreenDataArea();

        final double xScaling = myInputHistValues.length / (dataArea.getMaxX() - dataArea.getMinX());
        int xposition = (int)Math.round(chartPoint.getX() / xScaling + dataArea.getMinX());

        final CategoryPlot plot = myChart.getCategoryPlot();
        final NumberAxis rangeAxisy = (NumberAxis)plot.getRangeAxis();
        final double rangeLength = rangeAxisy.getRange().getLength();
        final double heightRatio = rangeLength / 256.0;

        final int rangeValue = (int)Math
                .round(rangeAxisy.valueToJava2D(chartPoint.getY() * heightRatio, dataArea, plot.getRangeAxisEdge()));

        return new Point(xposition, rangeValue);
    }

    /**
     * Convert to output dataset.
     *
     * @return the category dataset
     */
    private CategoryDataset convertToOutputDataset()
    {
        if (myOutputValues == null)
        {
            myOutputValues = new int[myInputHistValues.length];
        }

        updateOutputValues();

        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final int size = myOutputValues.length;
        for (int i = 0; i < size; i++)
        {
            dataset.addValue(myOutputValues[i], "", Integer.toString(i));
        }
        return dataset;
    }

    /**
     * Creates the dummy dataset.
     *
     * @return the category dataset
     */
    private CategoryDataset createDummyDataset()
    {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final int size = myInputHistValues.length;
        for (int i = 0; i < size; i++)
        {
            dataset.addValue(myInputHistValues[i], "", Integer.toString(i));
        }
        return dataset;
    }

//    /**
//     * Gets the max count.
//     *
//     * @param histValues the hist values
//     * @return the max count
//     */
//    private int getMaxCount(int[] histValues)
//    {
//        final int size = histValues.length;
//        int maxCount = 0;
//        for (int i = 0; i < size; i++)
//        {
//            final int count = histValues[i];
//            if (count > maxCount)
//            {
//                maxCount = count;
//            }
//        }
//
//        return maxCount;
//    }

    /**
     * Downsample histo.
     *
     * @param bigSample the big sample
     * @param targetTotalBins the target total bins
     * @return the int[]
     */
    private int[] downsampleHisto(int[] bigSample, int targetTotalBins)
    {
        final int downSampleSize = targetTotalBins;
        final int[] toReturn = new int[downSampleSize];
        final int samplesPerBin = (int)Math.ceil(bigSample.length / (double)downSampleSize);

        for (int i = 0; i < downSampleSize; i++)
        {
            final int startOfBinInclusive = i * samplesPerBin;
            final int ceilingOfBinExclusive = Math.min(startOfBinInclusive + samplesPerBin, bigSample.length);

            for (int j = startOfBinInclusive; j < ceilingOfBinExclusive; j++)
            {
                toReturn[i] += bigSample[j];
            }
        }

        return toReturn;
    }

    /**
     * Find color point in vicinity of mouse.
     *
     * @param mousePoint the mouse point
     * @return the point
     */
    private Point findColorPointInVicinityOfMouse(Point2D mousePoint)
    {
        Point toReturn = null;
        for (Point intensityPoint : myDataToIntensityPoints)
        {
            final Point colorPointInMouseCoords = chartPointToJava2D(intensityPoint);
            if (Math.abs(mousePoint.getX() - colorPointInMouseCoords.x) < 10
                    && Math.abs(mousePoint.getY() - colorPointInMouseCoords.y) < 10)
            {
                toReturn = intensityPoint;
            }
        }
        return toReturn;
    }

    /**
     * Find index of next higher color point.
     *
     * @param chartPoint the chart point
     * @return the int
     */
    private int findIndexOfNextHigherColorPoint(Point chartPoint)
    {
        int insertIdx = Collections.binarySearch(myDataToIntensityPoints, chartPoint, new Comparator<Point>()
        {
            @Override
            public int compare(Point o1, Point o2)
            {
                if (o1.x < o2.x)
                {
                    return -1;
                }
                else if (o1.x == o2.x)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
        });
        // The binarySearch(...) call returns the index as (-(insertion point) -
        // 1)
        // if the item isn't found in the list. The intended use of the
        // function here is for inserting, so the item will not be on the list.
        // Change it back
        // to a positive insertion point so we can use it in calls to
        // add(index,object).
        // @see Collections.binarySearch(List<? extends T> list, T key,
        // Comparator <? super T> c)
        return Math.abs(insertIdx) - 1;
    }

    /**
     * This method takes the java2d point returned by
     * ChartPanel.translateScreenToJava2D
     * (chartMouseEvent.getTrigger().getPoint()); back to a point containing
     * (domain, range) for the chart. The origin of the incoming point has (by
     * default) its origin in the upper left corner. This is expected by this
     * method.
     *
     * @param java2DPoint - the java2d point returned by
     *            ChartPanel.translateScreenToJava2D
     *            (chartMouseEvent.getTrigger().getPoint());
     *
     * @return a Point containing the (domain, range) of the mouse point.
     */
    private Point java2dToChartPoint(Point2D java2DPoint)
    {
        final Rectangle2D dataArea = myChartPanel.getScreenDataArea();
        final double xScaling = myInputHistValues.length / (dataArea.getMaxX() - dataArea.getMinX());
        int xposition = (int)Math.round(xScaling * (java2DPoint.getX() - dataArea.getMinX()));
        xposition = xposition > myInputHistValues.length - 1 ? myInputHistValues.length - 1 : xposition;

        final CategoryPlot plot = myChart.getCategoryPlot();
        final NumberAxis rangeAxisy = (NumberAxis)plot.getRangeAxis();
        final double rangeLength = rangeAxisy.getRange().getLength();
        final double heightRatio = 256 / rangeLength;
        final int rangeValue = (int)Math
                .round(heightRatio * rangeAxisy.java2DToValue(java2DPoint.getY(), dataArea, plot.getRangeAxisEdge()));

        return new Point(xposition, rangeValue);
    }

    /**
     * Populate dummy gaussian data.
     *
     * @param desiredSizeOfArray the desired size of array
     * @param samples the samples
     * @return the int[]
     */
    private int[] populateDummyGaussianData(int desiredSizeOfArray, int samples)
    {
        final int[] toReturn = new int[desiredSizeOfArray];
        final int totalSamples = samples;
        Arrays.fill(toReturn, 0);
        final int size = toReturn.length;
        Random randomGenerator = new Random();

        for (int i = 0; i < totalSamples; i++)
        {
            // the nextGaussian() values are distributed with a mean of 0, and
            // 1 standard deviation is mapped to -1 and 1.
            int bin = (int)((randomGenerator.nextGaussian() + 5) / 10.0 * size);
            bin = bin < 0 ? 0 : bin;
            bin = bin > size - 1 ? size - 1 : bin;
            toReturn[bin] = toReturn[bin] + 1;
        }

        return toReturn;
    }

    /**
     * Sets the up chart.
     *
     * @param color the new up chart
     */
    private void setupChart(Color color)
    {
        CategoryDataset dataset = createDummyDataset();

        myChart = ChartFactory.createBarChart(null, null, "Intensity", dataset, PlotOrientation.VERTICAL, false, true, false);
        myChart.setAntiAlias(true);
        myChartPanel = new ChartPanel(myChart);
        add(myChartPanel, BorderLayout.CENTER);
        myChartPanel.setPreferredSize(new Dimension(650, 200));
        myChartPanel.setDomainZoomable(false);
        myChartPanel.setRangeZoomable(false);

        myChartPanel.addChartMouseListener(new HistChartMouseListener());
        HistMouseListener mouseListener = new HistMouseListener();
        myChartPanel.addMouseListener(mouseListener);
        myChartPanel.addMouseMotionListener(mouseListener);

        final CategoryPlot plot = myChart.getCategoryPlot();
        myChart.setBackgroundPaint(getBackground());
        plot.setBackgroundPaint(Color.black);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.lightGray);

        final NumberAxis rangeAxisy = (NumberAxis)plot.getRangeAxis();
        rangeAxisy.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxisy.setTickLabelPaint(Color.LIGHT_GRAY);
        rangeAxisy.setLabelPaint(Color.LIGHT_GRAY);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelsVisible(false);
        domainAxis.setTickMarksVisible(false);
        // 0 is 0%, 1 is 100%
        domainAxis.setLowerMargin(0);
        domainAxis.setUpperMargin(0);
        domainAxis.setTickLabelPaint(Color.LIGHT_GRAY);
        domainAxis.setLabelPaint(Color.LIGHT_GRAY);

        // --- Need this or else highlighting causes drawing artifacts when the
        // bars are bunched up
        // (which they will always be in a histogram)
        // final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        final BarRenderer renderer = new BarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        // ----

        renderer.setSeriesPaint(0, Color.gray);
        renderer.setShadowVisible(false);
        plot.setRenderer(1, renderer);
        plot.setDataset(1, dataset);

        CategoryDataset dataset2 = convertToOutputDataset();

        final BarRenderer renderer2 = new BarRenderer();
        renderer2.setBarPainter(new StandardBarPainter());
        renderer2.setDrawBarOutline(false);
        renderer2.setItemMargin(0);

        renderer2.setSeriesPaint(0, color);
        renderer2.setShadowVisible(false);
        plot.setRenderer(0, renderer2);
        plot.setDataset(0, dataset2);
    }

    /**
     * Update output.
     */
    private void updateOutput()
    {
        updateOutputValues();
        final CategoryPlot plot = myChart.getCategoryPlot();
        final DefaultCategoryDataset dataset = (DefaultCategoryDataset)plot.getDataset(0);
        final int size = myOutputValues.length;
        for (int i = 0; i < size; i++)
        {
            dataset.setValue(myOutputValues[i], "", Integer.toString(i));
        }
    }

    /**
     * Update output values.
     */
    private void updateOutputValues()
    {
        Arrays.fill(myOutputValues, 0);
        Point linePoint = null;

        List<Point> intensityPoints = addEndPointsIfNecessary(myDataToIntensityPoints);
        for (Point nextPoint : intensityPoints)
        {
            if (linePoint == null)
            {
                linePoint = nextPoint;
                continue;
            }

            final int ychange = nextPoint.y - linePoint.y;
            final int xchange = nextPoint.x - linePoint.x;
            final double slope = ychange / (double)xchange;
            final double yIntercept = nextPoint.y - slope * nextPoint.x;
            final int lowerRange = linePoint.x;
            final int upperRange = nextPoint.x;

            for (int j = lowerRange; j < upperRange; j++)
            {
                // find in which bin the data should be, in the output
                final int newIndex = (int)(j * slope + yIntercept);
                // sum the contents of the new output bin with the hist values
                // from the input bin.
                myOutputValues[newIndex] = myOutputValues[newIndex] + myInputHistValues[j];
            }
            linePoint = nextPoint;
        }
    }

    /**
     * A listener for mouse movement on the histo chart.
     */
    private final class HistChartMouseListener implements ChartMouseListener
    {
        @Override
        public void chartMouseClicked(ChartMouseEvent chartMouseEvent)
        {
        }

        @Override
        public void chartMouseMoved(ChartMouseEvent chartMouseEvent)
        {
            // Can't use the CategoryItemEntity to discover the "x" value across
            // the domain, since
            // it only gives a non-null result when an actual bar is being
            // moused-over. We need
            // reasonable values the entire time the chart is being moused-over,
            // so we have to compute
            // it ourselves.
            Point2D mousePoint = myChartPanel.translateScreenToJava2D(chartMouseEvent.getTrigger().getPoint());
            Rectangle2D dataArea = myChartPanel.getScreenDataArea();

            // Is the pointer over the graph?
            if (mousePoint.getX() > dataArea.getMinX() && mousePoint.getX() < dataArea.getMaxX()
                    && mousePoint.getY() > dataArea.getMinY() && mousePoint.getY() < dataArea.getMaxY())
            {
                highlightBox(mousePoint);
                repaint();
            }
        }

        /**
         * Highlight box.
         *
         * @param mousePoint the mouse point
         */
        private void highlightBox(Point2D mousePoint)
        {
            myActiveColorBreakpoint = findColorPointInVicinityOfMouse(mousePoint);
        }
    }

    /**
     * A mouse listener for the histogram.
     */
    private final class HistMouseListener extends MouseAdapter
    {
        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);
            if (myMouseActionMode == MODIFY_MODE || myMouseActionMode == INSERT_MODE)
            {
                if (myColorBreakpointBeingDragged == null)
                {
                    return;
                }
                Point theNewPoint = newChartPointFromMousePosition(e);

                myColorBreakpointBeingDragged.x = theNewPoint.x;
                myColorBreakpointBeingDragged.y = theNewPoint.y;
            }
            if (myMouseActionMode == INSERT_MODE)
            {
                myActiveColorBreakpoint = findColorPointInVicinityOfMouse(e.getPoint());
            }
            updateOutput();
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            super.mousePressed(e);
            if (myMouseActionMode == MODIFY_MODE)
            {
                Point point = findColorPointInVicinityOfMouse(e.getPoint());
                if (point == null)
                {
                    return;
                }
                myColorBreakpointBeingDragged = point;
            }
            else if (myMouseActionMode == DELETE_MODE)
            {
                Point point = findColorPointInVicinityOfMouse(e.getPoint());
                if (point == null)
                {
                    return;
                }
                myDataToIntensityPoints.remove(point);
                updateOutput();
                repaint();
            }
            else if (myMouseActionMode == INSERT_MODE)
            {
                Point point = newChartPointFromMousePosition(e);
                int insertIndex = findIndexOfNextHigherColorPoint(point);
                myDataToIntensityPoints.add(insertIndex, point);
                myColorBreakpointBeingDragged = point;
                updateOutput();
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            super.mouseReleased(e);
            if (myMouseActionMode == MODIFY_MODE || myMouseActionMode == INSERT_MODE)
            {
                myColorBreakpointBeingDragged = null;
            }
            repaint();
        }

        /**
         * New chart point from mouse position.
         *
         * @param e the e
         * @return the point
         */
        private Point newChartPointFromMousePosition(MouseEvent e)
        {
            Point theNewPoint = java2dToChartPoint(e.getPoint());

            theNewPoint.x = theNewPoint.x > myInputHistValues.length - 1 ? myInputHistValues.length - 1 : theNewPoint.x;
            theNewPoint.x = theNewPoint.x < 0 ? 0 : theNewPoint.x;
            theNewPoint.y = theNewPoint.y > 255 ? 255 : theNewPoint.y;
            theNewPoint.y = theNewPoint.y < 0 ? 0 : theNewPoint.y;
            return theNewPoint;
        }
    }
}
