package io.opensphere.csvcommon.detect.location.model;

import java.util.Collections;
import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.importer.config.ColumnType;

/**
 * The LocationResults contain all potential location columns identified in a
 * file header including either single latitude and longitude columns that have
 * been paired or individual columns that have been identified as location
 * columns.
 */
public class LocationResults
{
    /** The Lat lon results. */
    private final List<LatLonColumnResults> myLatLonResults;

    /** The Location columns. */
    private final List<PotentialLocationColumn> myLocationColumns;

    /**
     * Instantiates a new location results.
     */
    public LocationResults()
    {
        myLatLonResults = New.list();
        myLocationColumns = New.list();
    }

    /**
     * Adds a result to the lat/lon pair list.
     *
     * @param latLonResults the lat lon results
     */
    public void addResult(LatLonColumnResults latLonResults)
    {
        myLatLonResults.add(latLonResults);
    }

    /**
     * Adds a result to the location column list.
     *
     * @param locationResult the location result
     */
    public void addResult(PotentialLocationColumn locationResult)
    {
        myLocationColumns.add(locationResult);
    }

    /**
     * Gets the most likely lat lon column pair.
     *
     * @return the most likely lat lon column pair
     */
    public LatLonColumnResults getMostLikelyLatLonColumnPair()
    {
        float confidence = 0;
        LatLonColumnResults results = null;
        if (myLatLonResults != null && !myLatLonResults.isEmpty())
        {
            for (LatLonColumnResults res : myLatLonResults)
            {
                if (res.getConfidence() > confidence)
                {
                    confidence = res.getConfidence();
                    results = res;
                }
            }
        }
        return results;
    }

    /**
     * Gets the most likely location column.
     *
     * @param type the type
     * @return the most likely location column
     */
    public PotentialLocationColumn getMostLikelyLocationColumn(ColumnType type)
    {
        PotentialLocationColumn result = null;
        float conf = 0;
        if (myLocationColumns != null && !myLocationColumns.isEmpty())
        {
            for (PotentialLocationColumn res : myLocationColumns)
            {
                if (res.getType().equals(type) && res.getConfidence() > conf)
                {
                    conf = res.getConfidence();
                    result = res;
                }
            }
        }
        return result;
    }

    /**
     * Gets the most likely location column.
     *
     * @return the most likely location column
     */
    public PotentialLocationColumn getMostLikelyLocationColumn()
    {
        float confidence = 0;
        PotentialLocationColumn result = null;
        if (myLocationColumns != null && !myLocationColumns.isEmpty())
        {
            for (PotentialLocationColumn res : myLocationColumns)
            {
                if (res.getConfidence() > confidence)
                {
                    confidence = res.getConfidence();
                    result = res;
                }
            }
        }
        return result;
    }

    /**
     * Gets the greater of the 2 potential confidence values.
     *
     * @return the confidence
     */
    public float getConfidence()
    {
        float latLonConfidence = getMostLikelyLatLonColumnPair() == null ? 0f : getMostLikelyLatLonColumnPair().getConfidence();
        float locationConfidence = getMostLikelyLocationColumn() == null ? 0f : getMostLikelyLocationColumn().getConfidence();
        return latLonConfidence > locationConfidence ? latLonConfidence : locationConfidence;
    }

    /**
     * Gets the lat/lon results.
     *
     * @return the lat lon results
     */
    public List<LatLonColumnResults> getLatLonResults()
    {
        return Collections.unmodifiableList(myLatLonResults);
    }

    /**
     * Removes a LatLonColumnResults result.
     *
     * @param toRemove the result to remove
     */
    public void removeLatLonResult(LatLonColumnResults toRemove)
    {
        myLatLonResults.remove(toRemove);
    }

    /**
     * Gets the location results.
     *
     * @return the location results
     */
    public List<PotentialLocationColumn> getLocationResults()
    {
        return Collections.unmodifiableList(myLocationColumns);
    }

    /**
     * Removes a location column.
     *
     * @param toRemove the to remove
     */
    public void removeLocationColumn(PotentialLocationColumn toRemove)
    {
        myLocationColumns.remove(toRemove);
    }
}
