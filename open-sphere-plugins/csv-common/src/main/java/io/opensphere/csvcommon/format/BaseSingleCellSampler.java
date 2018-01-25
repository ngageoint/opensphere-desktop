package io.opensphere.csvcommon.format;

import java.io.IOException;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;

/**
 * A base cell sampler used by the formatters to help find a format. The cell
 * sampler is an abstract class providing all the basic functionality that the
 * individual formatter samplers needs. The class only ever samples a single
 * column of data.
 *
 */
public abstract class BaseSingleCellSampler implements CellSampler
{
    /**
     * The data to provide in table format.
     */
    private final List<List<String>> myCellData;

    /**
     * The data to provide.
     */
    private final List<String> myData;

    /**
     * Constructs a new base single cell sampler.
     *
     * @param data The data to provide.
     * @param addExtraColumn True if a column of 0's should be added to the
     *            sample data, false otherwise.
     */
    public BaseSingleCellSampler(List<String> data, boolean addExtraColumn)
    {
        myData = data;

        myCellData = New.list();

        for (String cell : myData)
        {
            List<String> row = New.list(cell);

            if (addExtraColumn)
            {
                row.add("0");
            }

            myCellData.add(row);
        }
    }

    @Override
    public void close() throws IOException
    {
    }

    @Override
    public List<? extends List<? extends String>> getBeginningSampleCells()
    {
        return myCellData;
    }

    @Override
    public List<? extends String> getBeginningSampleLines()
    {
        return myData;
    }

    @Override
    public List<? extends List<? extends String>> getEndingSampleCells()
    {
        return New.list();
    }

    @Override
    public List<? extends String> getEndingSampleLines()
    {
        return New.list();
    }

    @Override
    public int getEndingSampleLinesIndexOffset()
    {
        return 0;
    }

    @Override
    public int sampleLineToAbsoluteLine(int line)
    {
        return 0;
    }

    @Override
    public int absoluteLineToSampleLine(int line)
    {
        return 0;
    }
}
