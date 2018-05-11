package io.opensphere.myplaces.constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An enumeration over the set of headers used in import / export operations for
 * my places. The headers are format agnostic, and intended to be used with all
 * import / export operations.
 */
public enum ImportExportHeader
{
    /**
     * The name of the flag used to indicate the display state of the dot within
     * the my place.
     */
    DOT_FLAG(Constants.IS_FEATURE_ON_ID),

    /**
     * The name of the flag used to indicate the hidden state of the my place's
     * balloon pop-up.
     */
    BALLOON_FLAG(Constants.IS_ANNOHIDE_ID),

    /**
     * The name of the flag used to indicate the display state of the my place
     * within the timeline.
     */
    TIMELINE_FLAG(Constants.IS_SHOW_IN_TIMELINE),

    /**
     * The name of the flag used to indicate the configuration state of the
     * animation setting of the my place.
     */
    ANIMATE_FLAG(Constants.IS_ANIMATE),

    /**
     * The name of the flag used to indicate the display state of the heading
     * field within the balloon.
     */
    HEADING_FLAG(Constants.IS_HEADING_ID),

    /**
     * The name of the flag used to indicate the display state of the velocity
     * field within the balloon.
     */
    VELOCITY_FLAG(Constants.IS_VELOCITY_ID),

    /**
     * The name of the flag used to indicate the display state of the distance
     * field within the balloon.
     */
    DISTANCE_FLAG(Constants.IS_DISTANCE_ID),

    /**
     * The name of the flag used to indicate the display state of the altitude
     * field within the balloon.
     */
    ALTITUDE_FLAG(Constants.IS_ALTITUDE_ID),

    /**
     * The name of the flag used to indicate the display state of the title
     * field within the balloon.
     */
    TITLE_FLAG(Constants.IS_TITLE),

    /**
     * The name of the flag used to indicate the display state of the field
     * titles within the balloon.
     */
    FIELD_TITLE_FLAG(Constants.IS_FIELD_TITLE),

    /**
     * The name of the flag used to indicate the display state of the
     * description field within the balloon.
     */
    DESCRIPTION_FLAG(Constants.IS_DESC_ID),

    /**
     * The name of the flag used to indicate the display state of the
     * description field within the balloon.
     */
    DMS_LAT_LON_FLAG(Constants.IS_DMS_ID),

    /**
     * The name of the flag used to indicate the display state of the
     * description field within the balloon.
     */
    // this may not be right... this may be the location type:
    DEC_LAT_LON_FLAG("isLatLon"),

    /**
     * The name of the flag used to indicate the display state of the
     * description field within the balloon.
     */
    MGRS_FLAG(Constants.IS_MGRS),

    /**
     * The name of the flag used to indicate the display state of the
     * description field within the balloon.
     */
    BALLOON_FILLED_FLAG(Constants.IS_BUBBLE_FILLED_ID),

    // Data Fields:
    /**
     * The name of the field in which the title of the my place is stored.
     */
    TITLE("name"),

    /**
     * The name of the field in which the description of the my place is stored.
     */
    DESCRIPTION("description"),

    /**
     * The name of the field in which the X Offset of the my place balloon is
     * stored.
     */
    X_OFFSET(Constants.X_OFFSET_ID),

    /**
     * The name of the field in which the Y Offset of the my place balloon is
     * stored.
     */
    Y_OFFSET(Constants.Y_OFFSET_ID),

    /**
     * The name of the field in which the map visualization type of the my place
     * is stored.
     */
    MAP_VISUALIZATION_TYPE(Constants.MAP_VISUALIZATION_ID),

    /**
     * The name of the field in which the font style of the my place's balloon
     * text is stored.
     */
    FONT_STYLE(Constants.FONT_STYLE_ID),

    /**
     * The name of the field in which the font name of the my place's balloon
     * text is stored.
     */
    FONT_NAME(Constants.FONT_NAME_ID),

    /**
     * The name of the field in which the font name of the my place's balloon
     * text is stored.
     */
    FONT_SIZE(Constants.FONT_SIZE_ID),

    /**
     * The name of the field in which the font color of the my place's balloon
     * text is stored.
     */
    FONT_COLOR("fontColor"),

    /**
     * The name of the field in which the color of the my place's balloon is
     * stored.
     */
    BALLOON_COLOR("balloonColor"),

    /**
     * The name of the field in which the name of the my place's associated look
     * angle is stored.
     */
    ASSOCIATED_VIEW(Constants.ASSOCIATED_VIEW_ID),

    /**
     * The name of the field in which the value of the start time is stored.
     */
    START_TIME("startDateTime"),

    /**
     * The name of the field in which the value of the end time is stored.
     */
    END_TIME("endDateTime"),

    /** The name of the field in which the latitude value is stored. */
    LATITUDE(Constants.LATITUDE_KEY),

    /** The name of the field in which the longitude value is stored. */
    LONGITUDE(Constants.LONGITUDE_KEY),

    /** The name of the field in which the altitude value is stored. */
    ALTITUDE("altitude");

    /**
     * The name of the field, used for encoding / decoding in save and load
     * operations. This name is not intended to be human readable for end users
     * (but the value should be obvious to developers).
     */
    private String myTitle;

    /**
     * Creates a new import export header instance, populated with the supplied
     * title.
     *
     * @param pTitle The name of the field, used for encoding / decoding in save
     *            and load operations.
     */
    private ImportExportHeader(String pTitle)
    {
        myTitle = pTitle;
    }

    /**
     * Gets the title of the enum instance.
     *
     * @return the title of the enum instance.
     */
    public String getTitle()
    {
        return myTitle;
    }

    /** Enum values indexed by the title field. */
    private static Map<String, ImportExportHeader> titleMap = new LinkedHashMap<>();

    static
    {
        for (ImportExportHeader h : values())
        {
            titleMap.put(h.getTitle(), h);
        }
    }

    /**
     * Gets the enum instance corresponding to the supplied title.
     *
     * @param t the title for which to get the enum value.
     * @return the enum instance corresponding to the supplied title, or null if
     *         none could be found.
     */
    public static ImportExportHeader getByTitle(String t)
    {
        if (t == null)
        {
            return null;
        }
        return titleMap.get(t);
    }

    /**
     * Get the enum instance with the specified partial title (which may have
     * been truncated during export).
     * 
     * @param t the title or some prefix thereof
     * @return the matching ImportExportHeader, if any, or null
     */
    public static ImportExportHeader getByPrefix(String t)
    {
        for (ImportExportHeader h : values())
        {
            if (h.getTitle().startsWith(t))
            {
                return h;
            }
        }
        return null;
    }
}
