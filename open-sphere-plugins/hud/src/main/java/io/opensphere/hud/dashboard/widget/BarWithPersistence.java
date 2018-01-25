package io.opensphere.hud.dashboard.widget;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import io.opensphere.core.util.Utilities;

/**
 * A plotter that draws a filled rectangle in a box at a relative level that
 * corresponds to a given integer measurement. Also a line indicates a high
 * water mark of the measurements in the measurement history. Draws onto a
 * <code>BufferedImage</code> that can be used to write onto any
 * <code>JComponent</code>
 *
 * @version 1.0 August 1, 2004
 */
@SuppressWarnings("PMD.GodClass")
public class BarWithPersistence
{
    /** The our clear composite. */
    private static AlphaComposite ourClearComposite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f);

    /** The our src over composite. */
    private static AlphaComposite ourSrcOverComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

    /**
     * A holder for an identifying integer value, for record external record
     * keeping purposes only.
     */
    private int myId;

    /** The width of the buffered image. */
    private int myWidth;

    /** The height of the buffered image. */
    private int myHeight;

    /** The maximum measurement value that can currently be displayed. */
    private int myMaxBarUnits;

    /** The buffered image to draw on. */
    private BufferedImage myImage;

    /** Indicates whether the persistence / high water mark should be drawn. */
    private boolean myPersistance;

    /**
     * The current plot orientation {@link Orientation#HORIZONTAL} or
     * {@link Orientation#VERTICAL}.
     */
    private Orientation myOrientation;

    /** The Graphics2D context to use. */
    private Graphics2D myG2D;

    /** The plot background color. */
    private Color myBackgroundColor;

    /** The bar or line color for the plot. */
    private Color myBarColor;

    /** the persistence / high water line mark color. */
    private Color myPersistColor;

    /**
     * The current scaling being applied so that all measurements can be shown
     * in the plot.
     */
    private double myScale;

    /** The line segment specified with float coordinates. */
    private final Line2D.Float myAl2df;

    /** The measurement history. */
    private int[] myMeas;

    /** The current measurement. */
    private int myCurrMeas;

    /** The Border on. */
    private boolean myBorderOn;

    /**
     * Builds a TimeHistory plot.
     *
     * @param dim the {@link Dimension} of the plot.
     * @param persistance true = use persistence / high water mark.
     * @param maxMeasures the maximum measurements to use when determining the
     *            high water mark
     * @param orientation the {@link Orientation} of the bar
     * @param bar the color for the line or bar plot
     * @param persist the color for the high water mark line
     * @param maxBarUnits the maximum measurements to store in the history
     */
    public BarWithPersistence(Dimension dim, boolean persistance, int maxMeasures, Orientation orientation, Color bar,
            Color persist, int maxBarUnits)
    {
        Utilities.checkNull(dim, "dim");
        myId = 0;
        setMaxPersistMeasures(maxMeasures);
        myCurrMeas = 0;
        myAl2df = new Line2D.Float();
        myPersistance = persistance;
        myOrientation = orientation;
        setBackgroundColor(Color.black);
        setBarColor(bar);
        setPersistColor(persist);
        setMaxBarUnits(maxBarUnits);
        setDim((int)dim.getWidth(), (int)dim.getHeight());
        addMeas(0);
    }

    /**
     * Adds a measurement to the history, and the plot.
     *
     * @param meas the integer measurement
     */
    public final void addMeas(int meas)
    {
        myCurrMeas++;
        if (myCurrMeas >= myMeas.length)
        {
            myCurrMeas = 0;
        }
        myMeas[myCurrMeas] = meas;
    }

    /**
     * Clears the buffered image by repainting with the background color.
     *
     */
    public void clear()
    {
        myG2D.setComposite(ourClearComposite);
        for (int i = 0; i < myHeight; i++)
        {
            myG2D.setPaint(myBackgroundColor);
            myAl2df.setLine(0, i, myWidth - 1, i);
            myG2D.draw(myAl2df);
        }
        myG2D.setComposite(ourSrcOverComposite);
    }

    /**
     * Gets the plot's current background color.
     *
     * @return c the color
     */
    public Color getBackgroundColor()
    {
        return myBackgroundColor;
    }

    /**
     * Gets the plot's current bar color.
     *
     * @return c the color
     */
    public Color getBarColor()
    {
        return myBarColor;
    }

    /**
     * Gets the current measurement value being displayed.
     *
     * @return the value
     */
    public int getCurrentValue()
    {
        return myMeas[myCurrMeas];
    }

    /**
     * Gets the height of the plot ( BufferedImage ).
     *
     * @return the height of the plot
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Gets the plot's id.
     *
     * @return id, the id
     */
    public int getId()
    {
        return myId;
    }

    /**
     * Gets the buffered image on which the plot is drawn.
     *
     * @return BufferedImage the image
     */
    public BufferedImage getImage()
    {
        reDraw();
        return myImage;
    }

    /**
     * Gets the plot's maximum number of measures that can be stored.
     *
     * @return the number of measures
     */
    public int getMaxBarUnits()
    {
        return myMaxBarUnits;
    }

    /**
     * Gets the plot {@link Orientation} .
     *
     * @return the orientation
     */
    public Orientation getOrientation()
    {
        return myOrientation;
    }

    /**
     * Gets the plot's persistence line color.
     *
     * @return c the color
     */
    public Color getPersistColor()
    {
        return myPersistColor;
    }

    /**
     * Gets the measurement for the value that is being used to draw the high
     * water mark.
     *
     * @return the value
     */
    public int getPersistValue()
    {
        int maxVal = 0;
        if (myPersistance)
        {
            for (int i = 0; i < myMeas.length; i++)
            {
                if (myMeas[i] > maxVal)
                {
                    maxVal = myMeas[i];
                }
            }
        }
        return maxVal;
    }

    /**
     * Gets the width of the plot ( BufferedImage ).
     *
     * @return the width of the plot
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Checks if is border on.
     *
     * @return true, if is border on
     */
    public boolean isBorderOn()
    {
        return myBorderOn;
    }

    /**
     * The amount of memory ( in bytes ) used to store the current number of
     * measurements.
     *
     * @return the number of bytes
     */
    public int memUsedInHist()
    {
        return myMeas.length * 4;
    }

    /**
     * Redraws the bar onto the BufferedImage.
     *
     */
    public void reDraw()
    {
        int curMeas = getCurrentValue();
        int perMeas = getPersistValue();
        int barHeight = (int)(myScale * curMeas);
        int perHeight = (int)(myScale * perMeas);

        clear();

        if (myOrientation == Orientation.VERTICAL)
        {
            for (int i = 0; i < myHeight; i++)
            {
                // Draw the bar while it's less than myHeight
                if (barHeight >= i && barHeight > 0)
                {
                    myG2D.setPaint(myBarColor);
                    myAl2df.setLine(1, myHeight - 2 - i, myWidth - 2, myHeight - 2 - i);
                    myG2D.draw(myAl2df);
                }
                if (perHeight > 0 && perHeight == i)
                {
                    myG2D.setPaint(myPersistColor);
                    myAl2df.setLine(1, myHeight - 2 - i, myWidth - 2, myHeight - 2 - i);
                    myG2D.draw(myAl2df);
                }
            }
        }
        else
        {
            for (int i = 0; i < myWidth; i++)
            {
                if (barHeight >= i && barHeight > 0)
                {
                    myG2D.setPaint(myBarColor);
                    myAl2df.setLine(i, 1, i, myHeight - 2);
                    myG2D.draw(myAl2df);
                }
                if (perHeight > 0 && perHeight == i)
                {
                    myG2D.setPaint(myPersistColor);
                    myAl2df.setLine(i, 1, i, myHeight - 2);
                    myG2D.draw(myAl2df);
                }
            }
        }

        // Draw the border
        if (myBorderOn)
        {
            myG2D.setPaint(Color.black);
            myAl2df.setLine(0, 0, myWidth - 1, 0);
            myG2D.draw(myAl2df);
            myAl2df.setLine(0, 0, 0, myHeight - 1);
            myG2D.draw(myAl2df);
            myAl2df.setLine(myWidth - 1, 0, myWidth - 1, myHeight - 1);
            myG2D.draw(myAl2df);
            myAl2df.setLine(0, myHeight - 1, myWidth - 1, myHeight - 1);
            myG2D.draw(myAl2df);
        }
    }

    /**
     * Resets the stored measurement history.
     *
     */
    public void reset()
    {
        for (int i = 0; i < myMeas.length; i++)
        {
            myMeas[i] = 0;
        }
        myCurrMeas = 0;
    }

    /**
     * Sets the plot's background color.
     *
     * @param c the color
     */
    public final void setBackgroundColor(Color c)
    {
        if (c == null)
        {
            myBackgroundColor = Color.black;
        }
        else
        {
            myBackgroundColor = c;
        }
        resetImageContext();
    }

    /**
     * Sets the plot's bar color.
     *
     * @param c the color
     */
    public final void setBarColor(Color c)
    {
        if (c != null)
        {
            myBarColor = c;
        }
        else
        {
            myBarColor = Color.green;
        }
    }

    /**
     * Sets the border on.
     *
     * @param borderOn the new border on
     */
    public void setBorderOn(boolean borderOn)
    {
        myBorderOn = borderOn;
    }

    /**
     * Sets the dimensions of the plot ( BufferedImage ).
     *
     * @param width Width of the plot
     * @param height Height of the plot
     */
    public final void setDim(int width, int height)
    {
        if (!(width == myWidth && height == myHeight))
        {
            if (width < 1)
            {
                myWidth = 1;
            }
            else
            {
                myWidth = width;
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
     * Sets the plot's id.
     *
     * @param id , the id
     */
    public void setId(int id)
    {
        myId = id;
    }

    /**
     * Sets the plot's maximum number of measures that can be stored.
     *
     * @param units the number of measures
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
     * Sets the max persist measures.
     *
     * @param numMeas the new max persist measures
     */
    public final void setMaxPersistMeasures(int numMeas)
    {
        if (myMeas == null)
        {
            if (numMeas > 0)
            {
                myMeas = new int[numMeas];
            }
            else
            {
                myMeas = new int[10];
            }
            myCurrMeas = 0;
        }
        else
        {
            if (numMeas > 0)
            {
                if (myMeas.length > numMeas)
                {
                    int[] measSet = new int[numMeas];

                    int copied = 0;
                    int current = myCurrMeas;
                    while (copied < measSet.length)
                    {
                        measSet[copied] = myMeas[current];

                        current++;
                        if (current >= myMeas.length)
                        {
                            current = 0;
                        }

                        copied++;
                    }
                    myCurrMeas = 0;
                    myMeas = measSet;
                }
                else if (numMeas > myMeas.length)
                {
                    int[] measSet = new int[numMeas];
                    System.arraycopy(myMeas, myCurrMeas, measSet, 0, myMeas.length - myCurrMeas);
                    System.arraycopy(myMeas, 0, measSet, myMeas.length - myCurrMeas - 1, myCurrMeas);
                    myCurrMeas = 0;
                    myMeas = measSet;
                }
            }
        }
    }

    /**
     * Sets the plot {@link Orientation}. .
     *
     * @param orient the orientation.
     */
    public void setOrientation(Orientation orient)
    {
        myOrientation = orient;
    }

    /**
     * Sets the plot's persistence line color.
     *
     * @param c the color.
     */
    public final void setPersistColor(Color c)
    {
        if (c != null)
        {
            myPersistColor = c;
        }
        else
        {
            myPersistColor = Color.white;
        }
    }

    /**
     * Toggles on or off whether to draw the persistance / high water mark.
     *
     */
    public void togglePersistance()
    {
        myPersistance = !myPersistance;
    }

    /**
     * Creates a new image context ( BufferedImage ) to use for rendering. This
     * is necessary if the plot is resized.
     *
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
     * multiplied by this value before being drawn So measurements greater than
     * the height of the image can be displayed. This is automatically done as
     * new measurements are added so that all data in the displayable history
     * can be plotted on the display.
     *
     */
    private void setScale()
    {
        if (myOrientation == Orientation.VERTICAL)
        {
            myScale = (double)(myHeight - 1) / (double)myMaxBarUnits;
        }
        else
        {
            myScale = (double)(myWidth - 1) / (double)myMaxBarUnits;
        }
    }

    /**
     * The Enum Orientation.
     */
    public enum Orientation
    {
        /** The HORIZONTAL. */
        HORIZONTAL,

        /** The VERTICAL. */
        VERTICAL
    }
}
