package io.opensphere.controlpanels.timeline.chart;

/**
 * An enumeration of chart types.
 */
public enum ChartType
{
    /** Overlapping bar. */
    BAR_OVERLAPPING("overlapping bar"),

    /** Stacked bar. */
    BAR_STACKED("stacked bar"),

    /** Overlapping line. */
    LINE_OVERLAPPING("overlapping line"),

    /** Stacked line. */
    LINE_STACKED("stacked line"),

    /** Direct. */
    DIRECT("raw data"),

    /** No chart. */
    NONE("none");

    /** The description. */
    private String myDescription;

    /**
     * Constructor.
     *
     * @param description The description
     */
    ChartType(String description)
    {
        myDescription = description;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the next chart type.
     *
     * @return the next chart type
     */
    public ChartType next()
    {
        ChartType next;
        switch (this)
        {
            case LINE_OVERLAPPING:
                next = LINE_STACKED;
                break;
            case LINE_STACKED:
                next = BAR_OVERLAPPING;
                break;
            case BAR_OVERLAPPING:
                next = BAR_STACKED;
                break;
            case BAR_STACKED:
                next = DIRECT;
                break;
            case DIRECT:
                next = NONE;
                break;
            case NONE:
                next = LINE_OVERLAPPING;
                break;
            default:
                next = null;
                break;
        }
        return next;
    }
}
