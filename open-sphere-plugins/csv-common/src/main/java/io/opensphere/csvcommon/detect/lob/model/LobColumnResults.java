package io.opensphere.csvcommon.detect.lob.model;

/**
 * The Class LobColumnResults. Contains the column details for line of bearing
 * data.
 */
public class LobColumnResults
{
    /** The Column. */
    private LineOfBearingColumn myColumn;

    /** Degree of confidence that the results have the required column. */
    private float myConfidence;

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
     * Gets the line of bearing column.
     *
     * @return the line of bearing column
     */
    public LineOfBearingColumn getLineOfBearingColumn()
    {
        return myColumn;
    }

    /**
     * Sets the line of bearing column.
     *
     * @param column the new line of bearing column
     */
    public void setLineOfBearingColumn(LineOfBearingColumn column)
    {
        myColumn = column;
        myConfidence = 1.0f;
    }
}
