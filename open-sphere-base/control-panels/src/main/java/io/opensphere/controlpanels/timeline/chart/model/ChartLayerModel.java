package io.opensphere.controlpanels.timeline.chart.model;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import io.opensphere.controlpanels.timeline.ResolutionBasedSnapFunction;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.model.time.ExtentAccumulator;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.timeline.StyledTimelineDatum;
import io.opensphere.core.timeline.TimelineDatum;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.MemoizingSupplier;
import io.opensphere.core.util.collections.New;

/**
 * Chart layer model.
 */
public class ChartLayerModel
{
    /** The bins. */
    private final List<ChartBin> myBins = New.list();

    /** Whether the bins are dirty. */
    private boolean myBinsDirty;

    /**
     * Whether the data have changed since the last time this was set to false.
     */
    private boolean myChanged;

    /** The color. */
    private Color myColor;

    /** The data. */
    private final List<TimelineDatum> myData = New.list();
    // private final CompactTimeSpanList myData = new CompactTimeSpanList();

    /** The maximum bin count. */
    private int myMaxCount;

    /** Whether the max count is dirty. */
    private boolean myMaxCountDirty;

    /** The name of the layer. */
    private final String myName;

    /** The order of the data relative to other data. */
    private int myOrder;

    /** The selected color. */
    private Color mySelectedColor;

    /** The timeline UI model. */
    private final TimelineUIModel myUIModel;

    /** Whether the layer is visible. */
    private boolean myVisible = true;

    /** The time extents supplier. */
    private final MemoizingSupplier<TimeSpan> myExtentsSupplier;

    /**
     * Gets the selected color for the given color.
     *
     * @param color the normal color
     * @return the selected color
     */
    private static Color getSelectedColor(Color color)
    {
        Color selectedColor = color;
        if (color != null)
        {
            if (ColorUtilities.getBrightness(color) >= 125)
            {
                selectedColor = color.darker();
            }
            else
            {
                int shift = 64;
                int red = Math.min(color.getRed() + shift, 255);
                int green = Math.min(color.getGreen() + shift, 255);
                int blue = Math.min(color.getBlue() + shift, 255);
                selectedColor = new Color(red, green, blue);
            }
        }
        return selectedColor;
    }

    /**
     * Constructor.
     *
     * @param name The name of the layer
     * @param color The color
     * @param uiModel The timeline UI
     */
    public ChartLayerModel(String name, Color color, TimelineUIModel uiModel)
    {
        myName = name;
        setColor(color);
        myUIModel = uiModel;
        myExtentsSupplier = new MemoizingSupplier<>(this::calculateExtents);
    }

    /**
     * Gets the time spans.
     *
     * @return the time spans
     */
    public Iterable<TimelineDatum> getTimeSpans()
    {
        return myData;
    }

    /**
     * Gets the bins.
     *
     * @return the bins
     */
    public List<ChartBin> getBins()
    {
        if (myBinsDirty)
        {
            calculateBins();
            myBinsDirty = false;
        }
        return myBins;
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Gets the extents of the list.
     *
     * @return the time extents
     */
    public TimeSpan getExtents()
    {
        return myExtentsSupplier.get();
    }

    /**
     * Gets the maximum count in the data.
     *
     * @return the maximum count
     */
    public int getMaxCount()
    {
        if (myMaxCountDirty)
        {
            calculateMaxCount();
            myMaxCountDirty = false;
        }
        return myMaxCount;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public int getOrder()
    {
        return myOrder;
    }

    /**
     * Gets the selected color.
     *
     * @return the selected color
     */
    public Color getSelectedColor()
    {
        return mySelectedColor;
    }

    /**
     * Gets whether the data have changed since the last time setChanged() was
     * set to false.
     *
     * @return whether the data have changed
     */
    public boolean isChanged()
    {
        return myChanged;
    }

    /**
     * Gets whether the layer is visible.
     *
     * @return whether the layer is visible
     */
    public boolean isVisible()
    {
        return myVisible;
    }

    /**
     * Marks the data as having changed or not as far as the caller is
     * concerned.
     *
     * @param changed whether the data have changed
     */
    public void setChanged(boolean changed)
    {
        myChanged = changed;
    }

    /**
     * Sets the color.
     *
     * @param color the color
     */
    public final void setColor(Color color)
    {
        myColor = color;
        mySelectedColor = getSelectedColor(color);
    }

    /**
     * Sets the time spans of the data.
     *
     * @param timeSpans the time spans of the data
     */
    public void setData(Collection<? extends TimelineDatum> timeSpans)
    {
        myData.clear();
        myData.addAll(timeSpans);
        myExtentsSupplier.invalidate();
        myChanged = true;
        myBinsDirty = true;
        myMaxCountDirty = true;
    }

    /**
     * Sets the order.
     *
     * @param order the order
     */
    public void setOrder(int order)
    {
        myOrder = order;
    }

    /**
     * Sets whether the layer is visible.
     *
     * @param visible whether the layer is visible
     */
    public void setVisible(boolean visible)
    {
        myVisible = visible;
    }

    /**
     * Calculates the bins.
     */
    private void calculateBins()
    {
        ChartBin[] binArray = createBins();

        // Calculate the count for each bin
        // Note: This code is optimized because it needs to be performant
        int bestIndex = 0;
        int binCount = binArray.length;
        int index;
        ChartBin bin;
        for (TimelineDatum datum : myData)
        {
            boolean isBinnable = true;
            if (datum instanceof StyledTimelineDatum)
            {
                isBinnable = false;
            }

            TimeSpan dataSpan = datum.getTimeSpan();
            if (isBinnable)
            {
                for (int i = 0; i < binCount; ++i)
                {
                    index = bestIndex + i;
                    if (index >= binCount)
                    {
                        index -= binCount;
                    }

                    bin = binArray[index];
                    if (bin.getSpan().contains(dataSpan))
                    {
                        bin.increment();
                        bestIndex = index;
                        break;
                    }
                    else if (isBinnable && bin.getSpan().overlaps(dataSpan))
                    {
                        bin.increment();
                    }
                }
            }
        }
    }

    /**
     * Indicates if this layer can be binned or should always be shown in raw
     * mode.
     *
     * @return True if the layer can be binned, false if this layer should
     *         always be drawn directly.
     */
    public boolean isBinnable()
    {
        boolean isBinnable = true;

        for (TimelineDatum datum : myData)
        {
            if (datum instanceof StyledTimelineDatum)
            {
                isBinnable = false;
                break;
            }
        }

        return isBinnable;
    }

    /**
     * Creates bins.
     *
     * @return the bins
     */
    private ChartBin[] createBins()
    {
        ChartBin[] binArray;

        long modulus;
        double millisPerPixel = myUIModel.getMillisPerPixel().get().doubleValue();
        if (millisPerPixel > 6 * Constants.MILLIS_PER_HOUR)
        {
            modulus = 4 * (long)millisPerPixel;
        }
        else
        {
            modulus = ResolutionBasedSnapFunction.getModulus(millisPerPixel);
        }
        long durationMs = modulus << 2;
        Duration dur = new Milliseconds(durationMs);
        long start = MathUtil.roundDownTo(myUIModel.getUISpan().get().getStart(), durationMs) - (durationMs >> 1);
        long end = myUIModel.getUISpan().get().getEnd() + durationMs;

        myBins.clear();
        for (TimeSpan span = TimeSpan.get(start, dur); span.getStart() < end; span = span.plus(dur))
        {
            myBins.add(new ChartBin(span));
        }
        binArray = myBins.toArray(new ChartBin[myBins.size()]);

        return binArray;
    }

    /**
     * Calculates the max count.
     */
    private void calculateMaxCount()
    {
        myMaxCount = 0;
        for (ChartBin bin : getBins())
        {
            if (bin.getCount() > myMaxCount)
            {
                myMaxCount = bin.getCount();
            }
        }
    }

    /**
     * Calculates the extents of the list.
     *
     * @return the time extents
     */
    private TimeSpan calculateExtents()
    {
        ExtentAccumulator accumulator = new ExtentAccumulator();
        for (TimelineDatum data : myData)
        {
            accumulator.add(data.getTimeSpan());
        }
        return accumulator.getExtent();
    }
}
