package io.opensphere.hud.dashboard.widget;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/**
 * The Class TimeHistoryPlot.
 */
@SuppressWarnings("PMD.GodClass")
public class TimeHistoryPlot
{
    /** The our clear composite. */
    private static AlphaComposite ourClearComposite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f);

    /** The our src over composite. */
    private static AlphaComposite ourSrcOverComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

    /** The width of the buffered image. */
    private int myWidth;

    /** The height of the buffered image. */
    private int myHeight;

    /** The maximum measurement value that can currently be displayed. */
    private int myMaxBarUnits;

    /** The buffered image to draw on. */
    private BufferedImage myImage;

    /** The Graphics2D context to use. */
    private Graphics2D myG2D;

    /** The plot background color. */
    private Color myBackgroundColor;

    /** The bar or line color for the plot. */
    private Color myBarColor;

    /**
     * The current scaling being applied so that all measurements can be shown
     * in the plot.
     */
    private double myScale;

    /** The current plot type {@link PlotType#LINE} or {@link PlotType#BAR}. */
    private PlotType myPlotType = PlotType.LINE;

    /**
     * A member line2D to conserve memory through reuse instead of making a new
     * line for every render operation.
     */
    private final Line2D.Float myLine2Df;

    /** The Measurements. */
    private int[] myMeasurements;

    /** The current measurement. */
    private int myCurrMeas;

    /** The Draw border. */
    private boolean myDrawBorder;

    /**
     * Builds a TimeHistory Plot.
     *
     * @param height the height of the image
     * @param width the width of the image
     * @param backgnd the background color for the plot
     * @param bar the color for the line or bar plot
     * @param maxBarUnits the maximum bar units to keep track of in the history
     * @param plotType the type of plot
     */
    public TimeHistoryPlot(int height, int width, Color backgnd, Color bar, int maxBarUnits, PlotType plotType)
    {
        myLine2Df = new Line2D.Float();
        myPlotType = plotType;
        myBackgroundColor = backgnd;
        myBarColor = bar;
        setMaxBarUnits(maxBarUnits);
        setDim(width, height);
        addMeas(0);
    }

    /**
     * Adds a measurement to the history and the plot.
     *
     * @param meas , the integer measurement
     */
    public final void addMeas(int meas)
    {
        myCurrMeas++;
        if (myCurrMeas >= myMeasurements.length)
        {
            myCurrMeas = 0;
        }

        myMeasurements[myCurrMeas] = meas;
    }

    /**
     * Gets the plot's current background color.
     *
     * @return the background color
     */
    public Color getBackgroundColor()
    {
        return myBackgroundColor;
    }

    /**
     * Get's the current bar or line color.
     *
     * @return the bar color
     */
    public Color getBarColor()
    {
        return myBarColor;
    }

    /**
     * Gets the height of the plot (Buffered Image ).
     *
     * @return the height of the plot
     *
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Gets the buffered image on which the plot is drawn.
     *
     * @return BufferedImage
     */
    public BufferedImage getImage()
    {
        reDraw();
        return myImage;
    }

    /**
     * Returns the max number of measurement sthat are stored by the plot
     * whether displayed or not.
     *
     * @return max measurements
     */
    public int getMaxBarUnits()
    {
        return myMaxBarUnits;
    }

    /**
     * Gets the maximum measurement stored in the history.
     *
     * @return int the maximum
     *
     */
    public int getMaxMeas()
    {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < myMeasurements.length; i++)
        {
            if (myMeasurements[i] > max)
            {
                max = myMeasurements[i];
            }
        }
        return max;
    }

    /**
     * Gets the maximum visible measurement stored in the history.
     *
     * @return the maximum
     *
     */
    public int getMaxVisMeas()
    {
        return getMaxMeas();
    }

    /**
     * Gets the plot type either {@link PlotType#BAR} or {@link PlotType#LINE}.
     *
     * @return the plot type
     */
    public PlotType getPlotType()
    {
        return myPlotType;
    }

    /**
     * Gets the width of the plot (Buffered Image ).
     *
     * @return the width of the plot
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Checks if is draw border.
     *
     * @return true, if is draw border
     */
    public boolean isDrawBorder()
    {
        return myDrawBorder;
    }

    /**
     * The ammount of memory (in bytes) used to store the current number of
     * measurements.
     *
     * @return int number of bytes
     *
     */
    public int memUsedInHist()
    {
        return myMeasurements.length * 4;
    }

    /**
     * Resets the stored measurement history.
     */
    public void reset()
    {
        for (int i = 0; i < myMeasurements.length; i++)
        {
            myMeasurements[i] = 0;
        }
    }

    /**
     * Sets the plot's current background color.
     *
     * @param c , the color
     *
     */
    public void setBackgroundColor(Color c)
    {
        myBackgroundColor = c;
    }

    /**
     * Sets the current bar or line color.
     *
     * @param c , the color
     *
     */
    public void setBarColor(Color c)
    {
        myBarColor = c;
    }

    /**
     * Sets the dimensions of the plot(BufferedImage).
     *
     * @param width width of the plot
     * @param height Heigh tof the plot
     *
     */
    public final void setDim(int width, int height)
    {
        if (!(width == myWidth && height == myHeight))
        {
            if (width != myWidth)
            {
                if (width < 4)
                {
                    myWidth = 4;
                }
                else
                {
                    myWidth = width;
                }

                if (myMeasurements == null)
                {
                    myMeasurements = new int[600];
                    myCurrMeas = 0;
                }
                else
                {
                    if (myWidth - 1 > myMeasurements.length)
                    {
                        int[] measSet = new int[myWidth - 1];
                        int cntTotal = 0;
                        int cntrAry1 = myCurrMeas;
                        int cntrAry2 = measSet.length - 1;

                        while (cntTotal < myMeasurements.length)
                        {
                            measSet[cntrAry2] = myMeasurements[cntrAry1];
                            cntrAry2--;
                            if (cntrAry2 < 0)
                            {
                                cntrAry2 = measSet.length - 1;
                            }

                            cntrAry1--;
                            if (cntrAry1 < 0)
                            {
                                cntrAry1 = myMeasurements.length - 1;
                            }

                            cntTotal++;
                        }

                        myCurrMeas = measSet.length - 1;
                        myMeasurements = measSet;
                    }
                }
            }

            if (height < 1)
            {
                myHeight = 1;
            }
            else
            {
                myHeight = height;
            }

            setScale();
            resetImageContext();
        }
    }

    /**
     * Sets the draw border.
     *
     * @param drawBorder the new draw border
     */
    public void setDrawBorder(boolean drawBorder)
    {
        myDrawBorder = drawBorder;
    }

    /**
     * Sets the max measurement value that can be dipslayed by the plot, whether
     * displayed or not.
     *
     * @param units the number of measurements
     */
    public final void setMaxBarUnits(int units)
    {
        if (units < 1)
        {
            myMaxBarUnits = 1;
        }
        else
        {
            myMaxBarUnits = units;
        }

        setScale();
    }

    /**
     * Sets the plot type to either {@link PlotType#BAR} or
     * {@link PlotType#LINE}.
     *
     * @param type the new plot type
     */
    public void setPlotType(PlotType type)
    {
        myPlotType = type;
    }

    /**
     * Clears the buffered image by repainting with the background color.
     *
     */
    protected void clear()
    {
        myG2D.setComposite(ourClearComposite);
        for (int i = 0; i < myHeight; i++)
        {
            myG2D.setPaint(new Color(0, 0, 0));
            myLine2Df.setLine(0, i, myWidth - 1, i);
            myG2D.draw(myLine2Df);
        }
        myG2D.setComposite(ourSrcOverComposite);
    }

    /**
     * Redraws the measurements onto the buffered image.
     */
    protected void reDraw()
    {
        clear();
        if (myDrawBorder)
        {
            myG2D.setPaint(Color.white);
            myLine2Df.setLine(0, 0, myWidth - 1, 0);
            myG2D.draw(myLine2Df);
            myLine2Df.setLine(0, 0, 0, myHeight - 1);
            myG2D.draw(myLine2Df);
            myLine2Df.setLine(myWidth - 1, 0, myWidth - 1, myHeight - 1);
            myG2D.draw(myLine2Df);
            myLine2Df.setLine(0, myHeight - 1, myWidth - 1, myHeight - 1);
            myG2D.draw(myLine2Df);
        }

        if (myPlotType == PlotType.BAR)
        {
            int curMeas = myCurrMeas;
            int barHeight = 0;
            int lineX = myWidth - 2;

            while (lineX > 0)
            {
                barHeight = (int)(myScale * myMeasurements[curMeas]);
                if (barHeight > myHeight)
                {
                    barHeight = myHeight;
                }

                if (barHeight > 0)
                {
                    myG2D.setPaint(myBarColor);
                    myLine2Df.setLine(lineX, myHeight - 2, lineX, myHeight - 2 - barHeight);
                    myG2D.draw(myLine2Df);
                }

                lineX--;
                curMeas--;
                if (curMeas <= 0)
                {
                    curMeas = myMeasurements.length - 1;
                }
            }
        }
        else
        {
            int lastBarHeight = 0;
            int curMeas = myCurrMeas;
            int lastVal = myMeasurements[curMeas];
            int barHeight = 0;
            int lineX = myWidth - 2;

            while (lineX > 0)
            {
                lastBarHeight = (int)(myScale * lastVal);
                barHeight = (int)(myScale * myMeasurements[curMeas]);

                if (barHeight > myHeight)
                {
                    barHeight = myHeight;
                }
                if (lastBarHeight > myHeight)
                {
                    lastBarHeight = myHeight;
                }
                myG2D.setPaint(myBarColor);
                myLine2Df.setLine(lineX, myHeight - 2 - lastBarHeight, lineX, myHeight - 2 - barHeight);
                myG2D.draw(myLine2Df);

                lastVal = myMeasurements[curMeas];

                lineX--;
                curMeas--;

                if (curMeas <= 0)
                {
                    curMeas = myMeasurements.length - 1;
                }
            }
        }
    }

    /**
     * Creates a new image context(BufferedImage) to use for rendering. This is
     * necessary if the plot is resized.
     */
    private void resetImageContext()
    {
        if (myWidth > 0 && myHeight > 0 && myBackgroundColor != null)
        {
            myImage = new BufferedImage(myWidth, myHeight, BufferedImage.TYPE_INT_ARGB);
            myG2D = myImage.createGraphics();
            myG2D.setBackground(myBackgroundColor);
        }
    }

    /**
     * Sets the scale of the buffered image so that all measurements are
     * multipled by this value before being drawn So measurements gereater than
     * the height of the image ca be displayed. This is automatically done as
     * new measurements are added so that all data in the displayable history
     * can be plotted on the display.
     *
     */
    private void setScale()
    {
        myScale = ((double)myHeight - 2) / myMaxBarUnits;
    }

    /**
     * The Enum PlotType.
     */
    public enum PlotType
    {
        /** The LINE. */
        LINE,

        /** The BAR. */
        BAR
    }
}
