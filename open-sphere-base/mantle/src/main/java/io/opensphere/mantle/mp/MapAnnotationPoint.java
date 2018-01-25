package io.opensphere.mantle.mp;

import java.awt.Color;
import java.awt.Font;

import io.opensphere.core.model.time.TimeSpan;

/**
 * The Interface MapAnnotationPoint.
 */
public interface MapAnnotationPoint
{
    /** The default font. */
    Font DEFAULT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    /**
     * Gets the {@link MapAnnotationPointSettings} settings.
     *
     * @return the {@link MapAnnotationPointSettings} settings
     */
    MapAnnotationPointSettings getAnnoSettings();

    /**
     * Gets the associated view name.
     *
     * @return the associated view name
     */
    String getAssociatedViewName();

    /**
     * Gets the background color.
     *
     * @return the background color
     */
    Color getBackgroundColor();

    /**
     * Gets the color.
     *
     * @return the color
     */
    Color getColor();

    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the font.
     *
     * @return the font
     */
    Font getFont();

    /**
     * Gets the font color.
     *
     * @return the font color
     */
    Color getFontColor();

    /**
     * Gets the font size.
     *
     * @return the font size
     */
    String getFontSize();

    /**
     * Gets the lat.
     *
     * @return the lat
     */
    double getLat();

    /**
     * Gets the lon.
     *
     * @return the lon
     */
    double getLon();

    /**
     * Gets the value of the altitude field.
     *
     * @return the altitude of the point, in meters.
     */
    double getAltitude();

    /**
     * Indicates if this point has any altitude or not.
     *
     * @return True if this point has altitude, false if it should be clamped to
     *         ground.
     */
    boolean hasAltitude();

    /**
     * Gets the mGRS.
     *
     * @return the mGRS
     */
    String getMGRS();

    /**
     * Gets the title.
     *
     * @return the title
     */
    String getTitle();

    /**
     * Gets the x offset.
     *
     * @return the x offset
     */
    int getxOffset();

    /**
     * Gets the y offset.
     *
     * @return the y offset
     */
    int getyOffset();

    /**
     * Checks if is filled.
     *
     * @return true, if is filled
     */
    boolean isFilled();

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    boolean isVisible();

    /**
     * Checks if time is enabled.
     *
     * @return true if time is enabled, false otherwise.
     */
    boolean isTimeEnabled();

    /**
     * Gets the time of the point.
     *
     * @return The time of the point, or null if there isn't one.
     */
    TimeSpan getTime();
}
