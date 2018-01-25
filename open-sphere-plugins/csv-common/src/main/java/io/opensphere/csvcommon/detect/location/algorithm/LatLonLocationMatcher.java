package io.opensphere.csvcommon.detect.location.algorithm;

import java.util.List;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.detect.location.algorithm.LocationMatchMaker;
import io.opensphere.csvcommon.detect.location.model.LatLonColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.csvcommon.detect.location.model.PotentialLocationColumn;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class LatLonLocationMatcher provides methods that consolidate pairs of
 * PotentialLocationColumns into a single LatLonColumnResult object.
 */
public abstract class LatLonLocationMatcher extends LocationMatchMaker
{
    /** The Constant ourConf3. */
    private static final float ourConf3 = .2f;

    /** The Constant ourConf4. */
    private static final float ourConf4 = .3f;

    /** The Aggregate results. */
    private final LocationResults myAggregateResults = new LocationResults();

    /** The Max confidence for a given pair of columns. */
    private float myMaxConfidence;

    /** The Column pairs. */
    private List<int[]> myColumnPairs;

    /**
     * Consolidates the results by comparing column pairs. Pairs with lower
     * column indexes are weighted higher. Also, column pair id'ed as both
     * latitude columns are changed so that the column with the higher index in
     * the pair is assigned as a longitude column.
     *
     * @param results the results
     * @param numColumns the number of columns
     */
    protected void consolidateResults(LocationResults results, int numColumns)
    {
        if (!results.getLocationResults().isEmpty())
        {
            float matchPair = 0;
            int isLonLat = -1;
            myColumnPairs = New.list();
            for (int i = 0; i < results.getLocationResults().size(); i++)
            {
                PotentialLocationColumn col1 = results.getLocationResults().get(i);
                for (int j = 0; j < results.getLocationResults().size(); j++)
                {
                    PotentialLocationColumn col2 = results.getLocationResults().get(j);
                    if (col1.getConfidence() > .5f && col2.getConfidence() > .5f)
                    {
                        matchPair = .5f;
                        // Most likely, lat/lon columns will be next to each
                        // other
                        if (Math.abs(col1.getColumnIndex() - col2.getColumnIndex()) == 1)
                        {
                            matchPair += ourConf3;
                            float scale = 1.0f - (float)col2.getColumnIndex() / (float)numColumns;

                            if (col1.getLatFormat() != null && col2.getLonFormat() != null
                                    || col1.getLonFormat() != null && col2.getLatFormat() != null)
                            {
                                matchPair += ourConf4;
                                // Make sure the columns have a valid lat/lon or
                                // lon/lat
                                if (col1.getLonFormat() != null && col2.getLatFormat() != null)
                                {
                                    isLonLat = 1;
                                }
                                else if (col1.getLatFormat() != null && col2.getLonFormat() != null)
                                {
                                    isLonLat = 0;
                                }

                                if (isLonLat > -1 && isNewPair(col1.getColumnIndex(), col2.getColumnIndex()))
                                {
                                    addLatLonResult(col1, col2, scale * matchPair);
                                }
                            }
                            // If there are 2 columns identified as lat next to
                            // each other, one could be
                            // longitude. In this case, assume, lat/lon, change
                            // col2 and consider this
                            // a valid pair
                            else if (col1.getLatFormat() != null && col2.getLatFormat() != null)
                            {
                                matchPair += ourConf4;
                                col2.setType(ColumnType.LON);
                                col2.setColumnName(ColumnType.LON.toString());
                                col2.setLatFormat(null);
                                col2.setLonFormat(CoordFormat.DECIMAL);
                                if (isNewPair(col1.getColumnIndex(), col2.getColumnIndex()))
                                {
                                    addLatLonResult(col1, col2, scale * matchPair);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks if these two column indexes have been added as a potential pair or
     * not.
     *
     * @param index1 potential column 1 index
     * @param index2 potential column 2 index
     * @return true, if is new pair
     */
    private boolean isNewPair(int index1, int index2)
    {
        boolean isNew = true;
        for (int[] pair : myColumnPairs)
        {
            if (pair[0] == index1 && pair[1] == index2 || pair[0] == index2 && pair[1] == index1)
            {
                isNew = false;
            }
        }

        if (isNew)
        {
            myColumnPairs.add(new int[] { index1, index2 });
        }

        return isNew;
    }

    /**
     * Adds a LatLonColumnResults object to the results set.
     *
     * @param col1 the 1st column
     * @param col2 the 2nd column
     * @param conf the confidence
     */
    private void addLatLonResult(PotentialLocationColumn col1, PotentialLocationColumn col2, float conf)
    {
        if (myMaxConfidence == 0)
        {
            myMaxConfidence = conf;
        }
        else
        {
            if (conf > myMaxConfidence)
            {
                myMaxConfidence = conf;
            }
        }

        LatLonColumnResults llcr = new LatLonColumnResults(col1, col2);
        llcr.setConfidence(conf);
        myAggregateResults.addResult(llcr);
    }

    /**
     * Gets the aggregate results.
     *
     * @return the aggregate results
     */
    public LocationResults getAggregateResults()
    {
        return myAggregateResults;
    }

    /**
     * Gets the maximum confidence.
     *
     * @return the max confidence
     */
    public float getMaxConfidence()
    {
        return myMaxConfidence;
    }
}
