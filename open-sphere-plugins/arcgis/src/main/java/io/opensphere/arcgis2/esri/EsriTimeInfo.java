package io.opensphere.arcgis2.esri;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The Class EsriTimeInfo.
 */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriTimeInfo implements Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My end time field. */
    @JsonProperty("endTimeField")
    private String myEndTimeField;

    /** My export options. */
    @JsonProperty("exportOptions")
    private transient EsriTimeExportOptions myExportOptions;

    /** My start time field. */
    @JsonProperty("startTimeField")
    private String myStartTimeField;

    /** My time extent. */
    @JsonProperty("timeExtent")
    private List<Long> myTimeExtent;

    /** My time interval. */
    @JsonProperty("timeInterval")
    private int myTimeInterval;

    /** My time interval units. */
    @JsonProperty("timeIntervalUnits")
    private transient EsriTimeUnits myTimeIntervalUnits;

    /** My time reference. */
    @JsonProperty("timeReference")
    private Object myTimeReference;

    /** My track id field. */
    @JsonProperty("trackIdField")
    private String myTrackIdField;

    /**
     * Gets the end time field.
     *
     * @return the end time field
     */
    public String getEndTimeField()
    {
        return myEndTimeField;
    }

    /**
     * Gets the export options.
     *
     * @return the arc GIS time export options
     */
    public EsriTimeExportOptions getExportOptions()
    {
        return myExportOptions;
    }

    /**
     * Gets the start time field.
     *
     * @return the start time field
     */
    public String getStartTimeField()
    {
        return myStartTimeField;
    }

    /**
     * Gets the time extent.
     *
     * @return the time extent
     */
    public List<Long> getTimeExtent()
    {
        return Collections.unmodifiableList(myTimeExtent);
    }

    /**
     * Gets the time interval.
     *
     * @return the time interval
     */
    public int getTimeInterval()
    {
        return myTimeInterval;
    }

    /**
     * Gets the time interval units.
     *
     * @return the esri time units
     */
    public EsriTimeUnits getTimeIntervalUnits()
    {
        return myTimeIntervalUnits;
    }

    /**
     * Gets the time reference.
     *
     * @return the time reference
     */
    public Object getTimeReference()
    {
        return myTimeReference;
    }

    /**
     * Gets the track id field.
     *
     * @return the track id field
     */
    public String getTrackIdField()
    {
        return myTrackIdField;
    }

    /**
     * Sets the end time field.
     *
     * @param endTimeField the new end time field
     */
    public void setEndTimeField(String endTimeField)
    {
        myEndTimeField = endTimeField;
    }

    /**
     * Sets the export options.
     *
     * @param exportOptions the new export options
     */
    public void setExportOptions(EsriTimeExportOptions exportOptions)
    {
        myExportOptions = exportOptions;
    }

    /**
     * Sets the start time field.
     *
     * @param startTimeField the new start time field
     */
    public void setStartTimeField(String startTimeField)
    {
        myStartTimeField = startTimeField;
    }

    /**
     * Sets the time extent.
     *
     * @param timeExtent the new time extent
     */
    public void setTimeExtent(List<Long> timeExtent)
    {
        myTimeExtent = timeExtent;
    }

    /**
     * Sets the time interval.
     *
     * @param timeInterval the new time interval
     */
    public void setTimeInterval(int timeInterval)
    {
        myTimeInterval = timeInterval;
    }

    /**
     * Sets the time interval units.
     *
     * @param timeIntervalUnits the new time interval units
     */
    public void setTimeIntervalUnits(EsriTimeUnits timeIntervalUnits)
    {
        myTimeIntervalUnits = timeIntervalUnits;
    }

    /**
     * Sets the time reference.
     *
     * @param timeReference the new time reference
     */
    public void setTimeReference(Object timeReference)
    {
        myTimeReference = timeReference;
    }

    /**
     * Sets the track id field.
     *
     * @param trackIdField the new track id field
     */
    public void setTrackIdField(String trackIdField)
    {
        myTrackIdField = trackIdField;
    }

    /** The Class EsriTimeExportOptions. */
    @JsonAutoDetect(JsonMethod.NONE)
    public static class EsriTimeExportOptions
    {
        /** The time data cumulative flag. */
        @JsonProperty("timeDataCumulative")
        private boolean myTimeDataCumulative;

        /** My time offset. */
        @JsonProperty("timeOffset")
        private Object myTimeOffset;

        /** My time offset units. */
        @JsonProperty("timeOffsetUnits")
        private EsriTimeUnits myTimeOffsetUnits;

        /** The use time flag. */
        @JsonProperty("useTime")
        private boolean myUseTime;

        /**
         * Gets the time offset.
         *
         * @return the time offset
         */
        public Object getTimeOffset()
        {
            return myTimeOffset;
        }

        /**
         * Gets the time offset units.
         *
         * @return the time offset units
         */
        public EsriTimeUnits getTimeOffsetUnits()
        {
            return myTimeOffsetUnits;
        }

        /**
         * Checks if is time data cumulative.
         *
         * @return true, if is time data cumulative
         */
        public boolean isTimeDataCumulative()
        {
            return myTimeDataCumulative;
        }

        /**
         * Checks if is use time.
         *
         * @return true, if is use time
         */
        public boolean isUseTime()
        {
            return myUseTime;
        }

        /**
         * Sets the time data cumulative.
         *
         * @param timeCumulative the new time data cumulative
         */
        public void setTimeDataCumulative(boolean timeCumulative)
        {
            myTimeDataCumulative = timeCumulative;
        }

        /**
         * Sets the time offset.
         *
         * @param timeOffset the new time offset
         */
        public void setTimeOffset(Object timeOffset)
        {
            myTimeOffset = timeOffset;
        }

        /**
         * Sets the time offset units.
         *
         * @param units the new time offset units
         */
        public void setTimeOffsetUnits(EsriTimeUnits units)
        {
            myTimeOffsetUnits = units;
        }

        /**
         * Sets the use time.
         *
         * @param useTime the new use time
         */
        public void setUseTime(boolean useTime)
        {
            myUseTime = useTime;
        }
    }

    /** The Enum for ESRI Time Units. */
    public enum EsriTimeUnits
    {
        /** The ESRI time representation for centuries. */
        esriTimeUnitsCenturies,

        /** The ESRI time representation for days. */
        esriTimeUnitsDays,

        /** The ESRI time representation for decades. */
        esriTimeUnitsDecades,

        /** The ESRI time representation for hours. */
        esriTimeUnitsHours,

        /** The ESRI time representation for milliseconds. */
        esriTimeUnitsMilliseconds,

        /** The ESRI time representation for minutes. */
        esriTimeUnitsMinutes,

        /** The ESRI time representation for months. */
        esriTimeUnitsMonths,

        /** The ESRI time representation for seconds. */
        esriTimeUnitsSeconds,

        /** The ESRI time representation for unknown time units. */
        esriTimeUnitsUnknown,

        /** The ESRI time representation for weeks. */
        esriTimeUnitsWeeks,

        /** The ESRI time representation for years. */
        esriTimeUnitsYears,
    }
}
