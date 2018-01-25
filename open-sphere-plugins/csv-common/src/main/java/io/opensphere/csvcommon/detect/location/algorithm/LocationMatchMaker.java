package io.opensphere.csvcommon.detect.location.algorithm;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.CellDetector;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class LocationMatchMaker. Attempts to match location data in a set of
 * sample data.
 */
public abstract class LocationMatchMaker implements CellDetector<LocationResults>
{
    /** The Potential columns. */
    private final Map<PotentialLocationColumn, Integer> myPotentialColumns = New.map();

    @Override
    public ValuesWithConfidence<LocationResults> detect(CellSampler sampler)
    {
        LocationResults results = new LocationResults();
        for (List<? extends String> row : sampler.getBeginningSampleCells())
        {
            for (int i = 0; i < row.size(); i++)
            {
                validateValue(i, row.get(i), results);
            }
        }

        for (Entry<PotentialLocationColumn, Integer> entry : myPotentialColumns.entrySet())
        {
            float scale = (float)entry.getValue().intValue() / (float)sampler.getBeginningSampleCells().size();
            entry.getKey().setConfidence(scale * entry.getKey().getConfidence());
        }

        return new ValuesWithConfidence<LocationResults>(results, Math.max(0f, results.getConfidence()));
    }

    /**
     * Validates a value.
     *
     * @param index the index
     * @param str the value to validate
     * @param results the results
     */
    protected abstract void validateValue(int index, String str, LocationResults results);

    /**
     * Creates the potential column or updates the count for potentials that
     * have already been created for an index.
     *
     * @param index the column index
     * @param results the results
     * @param confidence the confidence
     * @param type the type of column to create
     * @return the potential location column if one was created
     */
    public PotentialLocationColumn createPotentialColumn(int index, LocationResults results, float confidence, ColumnType type)
    {
        PotentialLocationColumn plc = null;
        if (confidence > 0)
        {
            if (myPotentialColumns.isEmpty())
            {
                plc = addPotentialLocationColumn(index, results, confidence, type);
            }
            else
            {
                boolean indexFound = false;
                for (Entry<PotentialLocationColumn, Integer> entry : myPotentialColumns.entrySet())
                {
                    if (entry.getKey().getColumnIndex() == index)
                    {
                        entry.setValue(Integer.valueOf(entry.getValue().intValue() + 1));
                        indexFound = true;

                        break;
                    }
                }
                if (!indexFound)
                {
                    plc = addPotentialLocationColumn(index, results, confidence, type);
                }
            }
        }
        return plc;
    }

    /**
     * Adds the potential location column.
     *
     * @param index the index
     * @param results the results
     * @param confidence the confidence
     * @param type the type
     * @return the potential location column
     */
    private PotentialLocationColumn addPotentialLocationColumn(int index, LocationResults results, float confidence,
            ColumnType type)
    {
        PotentialLocationColumn plc = new PotentialLocationColumn(type.toString(), type, "", "", true, index);
        plc.setConfidence(confidence);
        results.addResult(plc);
        myPotentialColumns.put(plc, Integer.valueOf(1));
        return plc;
    }

    /**
     * Gets the potential columns.
     *
     * @return the potential columns
     */
    public Map<PotentialLocationColumn, Integer> getPotentialColumns()
    {
        return myPotentialColumns;
    }
}
