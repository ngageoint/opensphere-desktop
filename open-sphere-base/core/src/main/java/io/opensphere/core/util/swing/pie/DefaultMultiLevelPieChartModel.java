package io.opensphere.core.util.swing.pie;

import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * Model for MultiLevelPieChart.
 *
 * @param <T> The type of the data
 */
public class DefaultMultiLevelPieChartModel<T> implements MultiLevelPieChartModel<T>
{
    /** The values. */
    private TwoDimensionArrayList<T> myValues;

    /** The ring names. */
    private List<? extends String> myRingNames;

    /** The slice names. */
    private List<? extends String> mySliceNames;

    /**
     * Constructor.
     *
     * @param values the values
     * @param ringNames the ring names
     * @param sliceNames the slice names
     */
    public DefaultMultiLevelPieChartModel(TwoDimensionArrayList<T> values, List<? extends String> ringNames,
            List<? extends String> sliceNames)
    {
        checkValues(values, ringNames, sliceNames);
        myValues = values;
        myRingNames = ringNames;
        mySliceNames = sliceNames;
    }

    @Override
    public int getRingCount()
    {
        return myRingNames.size();
    }

    @Override
    public String getRingName(int ringIndex)
    {
        return myRingNames.get(ringIndex);
    }

    @Override
    public int getSliceCount()
    {
        return mySliceNames.size();
    }

    @Override
    public String getSliceName(int sliceIndex)
    {
        return mySliceNames.get(sliceIndex);
    }

    @Override
    public T getValueAt(int sliceIndex, int ringIndex)
    {
        return myValues.get(sliceIndex, ringIndex);
    }

    @Override
    public void setRingNames(List<? extends String> ringNames)
    {
        myRingNames = New.list(ringNames);
    }

    @Override
    public void setSlilceNames(List<? extends String> sliceNames)
    {
        mySliceNames = New.list(sliceNames);
    }

    @Override
    public void setValues(TwoDimensionArrayList<T> values)
    {
        checkValues(values, myRingNames, mySliceNames);
        myValues = values;
    }

    /**
     * Check values.
     *
     * @param values the values
     * @param ringNames the ring names
     * @param sliceNames the slice names
     */
    private void checkValues(TwoDimensionArrayList<T> values, List<? extends String> ringNames, List<? extends String> sliceNames)
    {
        if (values.getXCount() != sliceNames.size())
        {
            throw new IllegalArgumentException(
                    "X count of " + values.getXCount() + " doesn't match slice count of " + sliceNames.size());
        }
        if (values.getYCount() != ringNames.size())
        {
            throw new IllegalArgumentException(
                    "Y count of " + values.getYCount() + " doesn't match ring count of " + ringNames.size());
        }
    }
}
