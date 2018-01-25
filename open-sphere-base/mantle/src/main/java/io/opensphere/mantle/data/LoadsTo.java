package io.opensphere.mantle.data;

/**
 * LoadsTo enum.
 */
public enum LoadsTo
{
    /** Loads in a non-interactive mode ( display only). */
    BASE,

    /** Loads in a non-interactive mode, but included in the timeline. */
    BASE_WITH_TIMELINE,

    /**
     * Loads in an interactive always-visible mode ( i.e. not time line based).
     */
    STATIC,

    /** Loads for use only in association with the time line. */
    TIMELINE;

    /**
     * Checks if is pickable.
     *
     * @return true, if is pickable
     */
    public boolean isPickable()
    {
        return isAnalysisEnabled();
    }

    /**
     * Checks if analysis is enabled.
     *
     * @return true, if analysis is enabled
     */
    public boolean isAnalysisEnabled()
    {
        return equals(STATIC) || equals(TIMELINE);
    }

    /**
     * Checks if timeline is enabled.
     *
     * @return true, if timeline is enabled
     */
    public boolean isTimelineEnabled()
    {
        return equals(BASE_WITH_TIMELINE) || equals(TIMELINE);
    }

    /**
     * Returns the enum value including timeline being enabled.
     *
     * @return the new value
     */
    public LoadsTo withTimeline()
    {
        LoadsTo result = this;
        if (this == BASE)
        {
            result = BASE_WITH_TIMELINE;
        }
        else if (this == STATIC)
        {
            result = TIMELINE;
        }
        return result;
    }

    /**
     * Returns the enum value including analysis being enabled.
     *
     * @return the new value
     */
    public LoadsTo withAnalysis()
    {
        LoadsTo result = this;
        if (this == BASE)
        {
            result = STATIC;
        }
        else if (this == BASE_WITH_TIMELINE)
        {
            result = TIMELINE;
        }
        return result;
    }

    /**
     * Returns the enum value including timeline being disabled.
     *
     * @return the new value
     */
    public LoadsTo withoutTimeline()
    {
        LoadsTo result = this;
        if (this == BASE_WITH_TIMELINE)
        {
            result = BASE;
        }
        else if (this == TIMELINE)
        {
            result = STATIC;
        }
        return result;
    }

    /**
     * Returns the enum value including analysis being disabled.
     *
     * @return the new value
     */
    public LoadsTo withoutAnalysis()
    {
        LoadsTo result = this;
        if (this == STATIC)
        {
            result = BASE;
        }
        else if (this == TIMELINE)
        {
            result = BASE_WITH_TIMELINE;
        }
        return result;
    }
}
