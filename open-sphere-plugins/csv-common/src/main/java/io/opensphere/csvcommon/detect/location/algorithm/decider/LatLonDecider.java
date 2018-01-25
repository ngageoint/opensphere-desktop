package io.opensphere.csvcommon.detect.location.algorithm.decider;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.detect.location.algorithm.decider.LocationDecider;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;

/**
 * The LatLonDecider will determine if there exists 1 or more pairs of columns
 * that qualify as a 'LATITUDE' and a 'LONGITUDE' column and assign a rating as
 * to which pair is most likely to be the primary lat/lon pair.
 */
public class LatLonDecider implements LocationDecider
{
    /** The Long names. */
    private final List<String> myLongNames;

    /** The Constant ourLongLatLonNamesKey. */
    public static final String ourLongLatLonNamesKey = "longLatLonNames";

    /** The Constant ourShortLatLonNamesKey. */
    public static final String ourShortLatLonNamesKey = "shortLatLonNames";

    /** The Short names. */
    private final List<String> myShortNames;

    /** The Column map. */
    private final Map<ColumnType, List<String>> myColumnMap = New.map();

    /** The Header cells. */
    private List<? extends String> myHeaderCells;

    /** The Potential columns. */
    private final List<PotentialLocationColumn> myPotentialColumns;

    /** The Constant ourConf1. */
    private static final float ourConf1 = .85f;

    /** The Constant ourConf2. */
    private static final float ourConf2 = .92f;

    /** The Constant ourDeltaConf. */
    private static final float ourDeltaConf = .2f;

    /**
     * Instantiates a new lat lon decider.
     *
     * @param prefsRegistry the preferences registry
     */
    public LatLonDecider(PreferencesRegistry prefsRegistry)
    {
        myLongNames = CSVColumnPrefsUtil.getCustomKeys(prefsRegistry, ourLongLatLonNamesKey);
        myShortNames = CSVColumnPrefsUtil.getCustomKeys(prefsRegistry, ourShortLatLonNamesKey);
        myColumnMap.put(ColumnType.LAT, New.list(CSVColumnPrefsUtil.getSpecialKeys(prefsRegistry, ColumnType.LAT)));
        myColumnMap.put(ColumnType.LON, New.list(CSVColumnPrefsUtil.getSpecialKeys(prefsRegistry, ColumnType.LON)));
        myPotentialColumns = New.list();
    }

    @Override
    public LocationResults determineLocationColumns(CellSampler sampler)
    {
        LocationResults locResults = null;
        if (sampler.getHeaderCells() != null)
        {
            locResults = new LocationResults();
            myHeaderCells = sampler.getHeaderCells();

            List<String> cells = New.list(sampler.getHeaderCells());
            myPotentialColumns.clear();
            detectPotentialLatitudeColumns(cells, myLongNames);
            detectPotentialLatitudeColumns(cells, myShortNames);

            if (!myPotentialColumns.isEmpty())
            {
                findColumnPairs(locResults);
            }
        }

        return locResults;
    }

    /**
     * Detects match cases for the set of header columns and creates a potential
     * column in the case of a valid match case.
     *
     * @param cells the list of header names
     * @param names the list of known lat/lon column names
     */
    private void detectPotentialLatitudeColumns(List<String> cells, List<String> names)
    {
        for (int i = cells.size() - 1; i >= 0; i--)
        {
            String colName = cells.get(i);
            for (String knownName : names)
            {
                CompareType compareType = null;
                if (colName.equalsIgnoreCase(knownName))
                {
                    compareType = CompareType.EQUALS;
                }
                else if (colName.toLowerCase().startsWith(knownName.toLowerCase()))
                {
                    compareType = CompareType.STARTS_WITH;
                }
                else if (colName.toLowerCase().endsWith(knownName.toLowerCase()))
                {
                    compareType = CompareType.ENDS_WITH;
                }
                else if (colName.toLowerCase().contains(knownName.toLowerCase()))
                {
                    compareType = CompareType.CONTAINS;
                }

                if (compareType != null)
                {
                    PotentialLocationColumn column = createPotentialColumn(compareType, cells, colName, knownName);
                    if (column != null)
                    {
                        myPotentialColumns.add(column);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public PotentialLocationColumn createPotentialColumn(CompareType cType, List<String> cells, String colName, String knownName)
    {
        ColumnType type = getColumnType(knownName);
        PotentialLocationColumn col = null;
        if (type.equals(ColumnType.LAT) || type.equals(ColumnType.LON))
        {
            cells.remove(colName);
            String[] tok = null;
            String prefix = null;
            String suffix = null;

            boolean longName = myLongNames.contains(colName);

            switch (cType)
            {
                case EQUALS:
                    col = new PotentialLocationColumn(colName, type, "", "", longName, indexForColumn(colName));
                    col.setConfidence(1.0f);
                    break;

                case STARTS_WITH:
                    tok = colName.toLowerCase().split(knownName.toLowerCase());
                    suffix = tok[tok.length - 1];
                    col = new PotentialLocationColumn(colName, type, "", suffix, longName, indexForColumn(colName));
                    col.setConfidence(ourConf2);
                    break;

                case ENDS_WITH:
                    tok = colName.toLowerCase().split(knownName.toLowerCase());
                    prefix = tok[0];
                    col = new PotentialLocationColumn(colName, type, prefix, "", longName, indexForColumn(colName));
                    col.setConfidence(ourConf2);
                    break;

                case CONTAINS:
                    tok = colName.toLowerCase().split(knownName.toLowerCase());
                    prefix = tok[0];
                    suffix = tok[tok.length - 1];
                    col = new PotentialLocationColumn(colName, type, prefix, suffix, longName, indexForColumn(colName));
                    col.setConfidence(ourConf1);
                    break;

                default:
                    break;
            }
        }
        return col;
    }

    /**
     * Attempts to match column pairs based on the set of potential columns that
     * have been created. The set of potential columns are compared using known
     * parameters and are matched accordingly. If a match is determined, a
     * LatLonColumnResult is created and added to the set of results.
     *
     * @param locResults the location results
     */
    private void findColumnPairs(LocationResults locResults)
    {
        for (PotentialLocationColumn toCheck : myPotentialColumns)
        {
            if (toCheck.getType() != ColumnType.LAT)
            {
                continue;
            }
            for (PotentialLocationColumn potCol : myPotentialColumns)
            {
                if (potCol.getType() != ColumnType.LON)
                {
                    continue;
                }
                // Pair columns with similar confidences and remove
                // duplicates since the list is traversed 2X.
                if (Math.abs(toCheck.getConfidence() - potCol.getConfidence()) < ourDeltaConf
                        && !hasLatLonResult(locResults.getLatLonResults(), toCheck, potCol))
                {
                    float toCheckConf = toCheck.getConfidence();
                    float potColConf = potCol.getConfidence();
                    LatLonColumnResults llc = new LatLonColumnResults(toCheck, potCol);

                    float combinedConf = (toCheck.getConfidence() + potCol.getConfidence()) / 2.0f;
                    llc.setConfidence((int)(combinedConf * 100));
                    if (llc.getConfidence() > 0)
                    {
                        locResults.addResult(llc);
                    }
                    else
                    {
                        // This wasn't a matched pair so put back the
                        // column's original confidence values.
                        toCheck.setConfidence(toCheckConf);
                        potCol.setConfidence(potColConf);
                    }
                }
            }
        }
    }

    /**
     * Checks for an existing lat lon result.
     *
     * @param results the results to check
     * @param col1 the first column
     * @param col2 the second column
     * @return true, if successful
     */
    private boolean hasLatLonResult(List<LatLonColumnResults> results, PotentialLocationColumn col1, PotentialLocationColumn col2)
    {
        boolean foundPair = false;
        for (LatLonColumnResults aRes : results)
        {
            if (aRes.getLatColumn().getColumnName().equals(col1.getColumnName())
                    && aRes.getLonColumn().getColumnName().equals(col2.getColumnName()))
            {
                foundPair = true;
            }
        }
        return foundPair;
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
     * Gets the column type for a given column name. The column names must be in
     * the column map in order for a valid type to be created.
     *
     * @param columnName the column name
     * @return the column type
     */
    private ColumnType getColumnType(String columnName)
    {
        for (Entry<ColumnType, List<String>> entry : myColumnMap.entrySet())
        {
            for (String name : entry.getValue())
            {
                if (name.equalsIgnoreCase(columnName))
                {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
