package io.opensphere.csvcommon.detect.controller;

import io.opensphere.core.model.IntegerRange;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;
import io.opensphere.csvcommon.detect.lob.model.LobColumnResults;
import io.opensphere.csvcommon.detect.location.model.LocationResults;
import io.opensphere.importer.config.SpecialColumn;

/**
 * Parameters for loading a CSV file, detected by reading a sample of the file.
 */
public class DetectedParameters
{
    /** Altitude parameter. */
    private ValuesWithConfidence<SpecialColumn> myAltitudeParameter;

    /** Column format parameter. */
    private ValuesWithConfidence<? extends ColumnFormatParameters> myColumnFormatParameter;

    /** Comment parameter. */
    private ValuesWithConfidence<Character> myCommentParameter;

    /** Data lines parameter. */
    private ValuesWithConfidence<IntegerRange> myDataLinesParameter;

    /** Date column parameter. */
    private ValuesWithConfidence<DateColumnResults> myDateColumnParameter;

    /** Header line parameter. */
    private ValuesWithConfidence<Integer> myHeaderLineParameter;

    /** The Location parameter. */
    private ValuesWithConfidence<LocationResults> myLocationParameter;

    /** The LOB parameter. */
    private ValuesWithConfidence<LobColumnResults> myLOBParameter;

    /**
     * Gets the altitudeParameter.
     *
     * @return the altitudeParameter
     */
    public ValuesWithConfidence<SpecialColumn> getAltitudeParameter()
    {
        return myAltitudeParameter;
    }

    /**
     * Sets the altitudeParameter.
     *
     * @param altitudeParameter the altitudeParameter
     */
    public void setAltitudeParameter(ValuesWithConfidence<SpecialColumn> altitudeParameter)
    {
        myAltitudeParameter = altitudeParameter;
    }

    /**
     * Get the column format parameter.
     *
     * @return the column format parameter
     */
    public ValuesWithConfidence<? extends ColumnFormatParameters> getColumnFormatParameter()
    {
        return myColumnFormatParameter;
    }

    /**
     * Get the comment parameter.
     *
     * @return The comment parameter.
     */
    public ValuesWithConfidence<Character> getCommentParameter()
    {
        return myCommentParameter;
    }

    /**
     * Get the data lines parameter.
     *
     * @return The data lines parameter.
     */
    public ValuesWithConfidence<IntegerRange> getDataLinesParameter()
    {
        return myDataLinesParameter;
    }

    /**
     * Get the date column parameter.
     *
     * @return The date column parameter.
     */
    public ValuesWithConfidence<DateColumnResults> getDateColumnParameter()
    {
        return myDateColumnParameter;
    }

    /**
     * Get the header line parameter.
     *
     * @return The header line parameter.
     */
    public ValuesWithConfidence<Integer> getHeaderLineParameter()
    {
        return myHeaderLineParameter;
    }

    /**
     * Gets the location parameter.
     *
     * @return the location parameter
     */
    public ValuesWithConfidence<LocationResults> getLocationParameter()
    {
        return myLocationParameter;
    }

    /**
     * Gets the line of bearing parameter.
     *
     * @return the line of bearing parameter
     */
    public ValuesWithConfidence<LobColumnResults> getLOBParameter()
    {
        return myLOBParameter;
    }

    /**
     * Sets the location parameter.
     *
     * @param parameter the new location parameter
     */
    public void setLocationParameter(ValuesWithConfidence<LocationResults> parameter)
    {
        myLocationParameter = parameter;
    }

    /**
     * Sets the line of bearing parameter.
     *
     * @param parameter the new line of bearing parameter
     */
    public void setLOBParameter(ValuesWithConfidence<LobColumnResults> parameter)
    {
        myLOBParameter = parameter;
    }

    /**
     * Set the column format parameter.
     *
     * @param columnFormatParameter The column format parameter.
     */
    public void setColumnFormatParameter(ValuesWithConfidence<? extends ColumnFormatParameters> columnFormatParameter)
    {
        myColumnFormatParameter = columnFormatParameter;
    }

    /**
     * Set the comment parameter.
     *
     * @param commentParameter The comment parameter.
     */
    public void setCommentParameter(ValuesWithConfidence<Character> commentParameter)
    {
        myCommentParameter = commentParameter;
    }

    /**
     * Set the data lines parameter.
     *
     * @param dataLinesParameter The data lines parameter.
     */
    public void setDataLinesParameter(ValuesWithConfidence<IntegerRange> dataLinesParameter)
    {
        myDataLinesParameter = dataLinesParameter;
    }

    /**
     * Sets the date column parameter.
     *
     * @param dateColumnParameter The date column parameter.
     */
    public void setDateColumnParameter(ValuesWithConfidence<DateColumnResults> dateColumnParameter)
    {
        myDateColumnParameter = dateColumnParameter;
    }

    /**
     * Set the header line parameter.
     *
     * @param headerLineParameter The header line parameter.
     */
    public void setHeaderLineParameter(ValuesWithConfidence<Integer> headerLineParameter)
    {
        myHeaderLineParameter = headerLineParameter;
    }
}
