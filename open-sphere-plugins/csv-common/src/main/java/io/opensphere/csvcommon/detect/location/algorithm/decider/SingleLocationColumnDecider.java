package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.location.algorithm.decider.LocationDecider;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;

/**
 * The SingleLocationColumnDecider attempts to identify a location type column
 * in a set of well known columns names applicable to the given type. If columns
 * are found that match, they are added to a set of location results.
 */
public abstract class SingleLocationColumnDecider implements LocationDecider
{
    /** The Known names. */
    private final List<String> myKnownNames;

    /** The Type. */
    private final ColumnType myType;

    /** The Header cells. */
    private List<? extends String> myHeaderCells;

    /** The Scale factor. */
    private static final float ourScaleFactor = 1.4f;

    /**
     * Instantiates a new single location column decider.
     *
     * @param type the column type
     * @param prefsRegistry the prefs registry
     */
    public SingleLocationColumnDecider(ColumnType type, PreferencesRegistry prefsRegistry)
    {
        myKnownNames = CSVColumnPrefsUtil.getSpecialKeys(prefsRegistry, type);
        myType = type;
    }

    @Override
    public LocationResults determineLocationColumns(CellSampler sampler)
    {
        return determineLocationColumn(sampler);
    }

    /**
     * Determine location column.
     *
     * @param sampler the sampler
     * @return the location results
     */
    public LocationResults determineLocationColumn(CellSampler sampler)
    {
        LocationResults results = null;
        if (sampler.getHeaderCells() != null)
        {
            results = new LocationResults();
            myHeaderCells = sampler.getHeaderCells();
            List<String> cells = New.list(sampler.getHeaderCells());

            for (int i = cells.size() - 1; i >= 0; i--)
            {
                String colName = cells.get(i);
                for (String knownName : myKnownNames)
                {
                    CompareType cType = null;
                    if (colName.equalsIgnoreCase(knownName))
                    {
                        cType = CompareType.EQUALS;
                    }
                    else if (colName.toLowerCase().startsWith(knownName.toLowerCase()))
                    {
                        cType = CompareType.STARTS_WITH;
                    }
                    else if (colName.toLowerCase().endsWith(knownName.toLowerCase()))
                    {
                        cType = CompareType.ENDS_WITH;
                    }
                    else if (colName.toLowerCase().contains(knownName.toLowerCase()))
                    {
                        cType = CompareType.CONTAINS;
                    }

                    if (cType != null)
                    {
                        PotentialLocationColumn column = createPotentialColumn(cType, cells, colName, knownName);
                        if (column != null && column.getConfidence() > 0)
                        {
                            results.addResult(column);
                            break;
                        }
                    }
                }
            }
        }
        return results;
    }

    @Override
    public PotentialLocationColumn createPotentialColumn(CompareType cType, List<String> cells, String colName, String knownName)
    {
        cells.remove(colName);
        String[] tok = null;
        String prefix = null;
        String suffix = null;
        PotentialLocationColumn col = null;

        switch (cType)
        {
            case EQUALS:
                col = new PotentialLocationColumn(colName, myType, "", "", isLongName(knownName), indexForColumn(colName));
                col.setConfidence(1.0f);
                break;

            case STARTS_WITH:
                tok = colName.split(knownName);
                suffix = tok[tok.length - 1];
                col = new PotentialLocationColumn(colName, myType, "", suffix, isLongName(knownName), indexForColumn(colName));
                col.setConfidence(1.0f / 2 * checkLongName(knownName));
                break;

            case ENDS_WITH:
                tok = colName.split(knownName);
                prefix = tok[0];
                col = new PotentialLocationColumn(colName, myType, prefix, "", isLongName(knownName), indexForColumn(colName));
                col.setConfidence(1.0f / 3 * checkLongName(knownName));
                break;

            case CONTAINS:
                tok = colName.split(knownName);
                prefix = tok[0];
                suffix = tok[tok.length - 1];
                col = new PotentialLocationColumn(colName, myType, prefix, suffix, isLongName(knownName),
                        indexForColumn(colName));
                col.setConfidence(1.0f / 6 * checkLongName(knownName));
                break;

            default:
                break;
        }
        return col;
    }

    /**
     * Check to see if this is a valid long name and weight the confidence
     * slightly higher.
     *
     * @param name the name
     * @return the confidence scale factor
     */
    private float checkLongName(String name)
    {
        return isLongName(name) ? ourScaleFactor : 1.0f;
    }

    /**
     * Gets an index from the set of header columns for a given header column
     * name.
     *
     * @param columnName the column name
     * @return the index of the column
     */
    private int indexForColumn(String columnName)
    {
        for (String col : myHeaderCells)
        {
            if (col.equalsIgnoreCase(columnName))
            {
                return myHeaderCells.indexOf(col);
            }
        }
        return -1;
    }

    /**
     * Checks if this name is identified as a non abbreviated known name.
     *
     * @param name the name
     * @return true, if is long name
     */
    public abstract boolean isLongName(String name);
}
