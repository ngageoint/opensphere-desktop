package io.opensphere.csvcommon.detect.location.model;

import io.opensphere.importer.config.ColumnType;

/**
 * The LatLonColumnResults class stores a set of potential latitude and
 * longitude columns.
 */
public final class LatLonColumnResults
{
    /** The Potential column1. */
    private final PotentialLocationColumn myPotentialColumn1;

    /** The Potential column2. */
    private final PotentialLocationColumn myPotentialColumn2;

    /** The Confidence. */
    private float myConfidence;

    /** The Scale factor. */
    private static final float ourScaleFactor = 100.0f;

    /** The Confidence factor1. */
    private static final float ourConfidenceFactor1 = .8f;

    /** The Constant ourConfidenceFactor2. */
    private static final float ourConfidenceFactor2 = 1.2f;

    /** The Constant ourConfidenceFactor3. */
    private static final float ourConfidenceFactor3 = 1.3f;

    /** The Column type. */
    private ColumnType myColumnType;

    /**
     * Instantiates a new lat lon column results.
     *
     * @param col1 the col1
     * @param col2 the col2
     */
    public LatLonColumnResults(PotentialLocationColumn col1, PotentialLocationColumn col2)
    {
        myPotentialColumn1 = col1;
        myPotentialColumn2 = col2;
    }

    /**
     * Gets the latitude column.
     *
     * @return the latitude column
     */
    public PotentialLocationColumn getLatColumn()
    {
        if (myPotentialColumn1.getType().equals(ColumnType.LAT))
        {
            return myPotentialColumn1;
        }
        else if (myPotentialColumn2.getType().equals(ColumnType.LAT))
        {
            return myPotentialColumn2;
        }
        return null;
    }

    /**
     * Gets the longitude column.
     *
     * @return the longitude column
     */
    public PotentialLocationColumn getLonColumn()
    {
        if (myPotentialColumn1.getType().equals(ColumnType.LON))
        {
            return myPotentialColumn1;
        }
        else if (myPotentialColumn2.getType().equals(ColumnType.LON))
        {
            return myPotentialColumn2;
        }
        return null;
    }

    /**
     * Sets the confidence based on exact match or not.
     *
     * @param matchConfidence the degree of confidence that these columns are a
     *            match, from 1 to 100.
     */
    public void setConfidence(int matchConfidence)
    {
        if (getLatColumn().getPrefix().isEmpty() && getLonColumn().getPrefix().isEmpty() && getLatColumn().getSuffix().isEmpty()
                && getLonColumn().getSuffix().isEmpty())
        {
            myConfidence = matchConfidence / ourScaleFactor;
        }
        else if (getLatColumn().getPrefix().isEmpty() && getLonColumn().getPrefix().isEmpty()
                && getLatColumn().getSuffix().equals(getLonColumn().getSuffix()))
        {
            myConfidence = 1.0f / ourConfidenceFactor2 * (matchConfidence / ourScaleFactor);
        }
        else if (getLatColumn().getPrefix().equals(getLonColumn().getPrefix())
                && getLatColumn().getSuffix().equals(getLonColumn().getSuffix()))
        {
            myConfidence = 1.0f / ourConfidenceFactor3 * (matchConfidence / ourScaleFactor);
        }
        else if (!getLatColumn().getPrefix().equals(getLonColumn().getPrefix())
                && !getLatColumn().getSuffix().equals(getLonColumn().getSuffix()))
        {
            myConfidence = 0f;
        }
        // Prefixes don't match but there is a lat and lon.
        else if (getLatColumn().isLongName() == getLonColumn().isLongName())
        {
            myConfidence = ourConfidenceFactor1 * matchConfidence / ourScaleFactor;
        }

        getLatColumn().setConfidence(myConfidence);
        getLonColumn().setConfidence(myConfidence);
    }

    /**
     * Sets the confidence.
     *
     * @param conf the new confidence
     */
    public void setConfidence(float conf)
    {
        myConfidence = conf;
        getLatColumn().setConfidence(myConfidence);
        getLonColumn().setConfidence(myConfidence);
    }

    /**
     * Gets the confidence.
     *
     * @return the confidence
     */
    public float getConfidence()
    {
        return myConfidence;
    }

    /**
     * Gets the column type.
     *
     * @return the column type
     */
    public ColumnType getColumnType()
    {
        return myColumnType;
    }

    /**
     * Sets the column type.
     *
     * @param columnType the new column type
     */
    public void setColumnType(ColumnType columnType)
    {
        myColumnType = columnType;
    }
}
