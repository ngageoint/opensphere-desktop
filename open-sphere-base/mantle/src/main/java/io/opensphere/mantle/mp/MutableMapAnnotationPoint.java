package io.opensphere.mantle.mp;

import java.awt.Color;
import java.awt.Font;
import java.util.Comparator;

/**
 * The Interface MapAnnotationPoint.
 */
public interface MutableMapAnnotationPoint extends MapAnnotationPoint
{
    /**
     * Comparator that orders {@link MutableMapAnnotationPoint}s by their
     * display names.
     */
    Comparator<MutableMapAnnotationPoint> TITLE_COMPARATOR = new Comparator<MutableMapAnnotationPoint>()
    {
        @Override
        public int compare(MutableMapAnnotationPoint o1, MutableMapAnnotationPoint o2)
        {
            int result = 0;
            try
            {
                result = o1.getTitle() == null ? o2.getTitle() == null ? 0 : "".compareTo(o2.getTitle())
                        : o1.getTitle().compareTo(o2.getTitle());
            }
            catch (RuntimeException e)
            {
                result = 0;
            }
            return result;
        }
    };

    /**
     * Fires off a {@link MapAnnotationPointChangeEvent} to any listeners.
     *
     * @param e - the event to fire.
     */
    void fireChangeEvent(MapAnnotationPointChangeEvent e);

    /**
     * Gets the group.
     *
     * @return the group
     */
    MutableMapAnnotationPointGroup getGroup();

    /**
     * Gets the unique ID for this mutable map annotation point.
     *
     * Note that this id is set internally and cannot be altered.
     *
     * @return the unique id.
     */
    long getId();

    /**
     * Sets the associated view name.
     *
     * @param name the new associated view name
     * @param source the source
     */
    void setAssociatedViewName(String name, Object source);

    /**
     * Sets the background color.
     *
     * @param color the new background color
     * @param source the source of the change
     */
    void setBackgroundColor(Color color, Object source);

    /**
     * Sets the color.
     *
     * @param shapeColor the new color
     * @param source the source of the change
     */
    void setColor(Color shapeColor, Object source);

    /**
     * Sets the description.
     *
     * @param desc the new description
     * @param source the source of the change
     */
    void setDescription(String desc, Object source);

    /**
     * Sets this point equal to another point.
     *
     * @param other the other {@link MapAnnotationPoint} to set equal to.
     * @param source the source of the change
     */
    void setEqualTo(MapAnnotationPoint other, Object source);

    /**
     * Sets the filled.
     *
     * @param filled the new filled
     * @param source the source of the change
     */
    void setFilled(boolean filled, Object source);

    /**
     * Sets the font color.
     *
     * @param font the new font
     * @param source the source of the change
     */
    void setFont(Font font, Object source);

    /**
     * Sets the font color.
     *
     * @param color the new font color
     * @param source the source of the change
     */
    void setFontColor(Color color, Object source);

    /**
     * Sets the font size.
     *
     * @param fontSize the new font size
     * @param source the source of the change
     */
    void setFontSize(String fontSize, Object source);

    /**
     * Sets the group.
     *
     * @param group the new group
     */
    void setGroup(MutableMapAnnotationPointGroup group);

    /**
     * Sets the lat.
     *
     * @param lat the new lat
     * @param source the source of the change
     */
    void setLat(double lat, Object source);

    /**
     * Sets the lon.
     *
     * @param lon the new lon
     * @param source the source of the change
     */
    void setLon(double lon, Object source);

    /**
     * Sets the altitude.
     *
     * @param pAltitude the new altitude.
     * @param pEventSource the source of the change.
     */
    void setAltitude(double pAltitude, Object pEventSource);

    /**
     * Sets the value of the MGRS field.
     *
     * @param mgrs the new value to assign to the MGRS field.
     * @param source the source of the change.
     */
    void setMGRS(String mgrs, Object source);

    /**
     * Sets the value of the Time Enabled flag.
     *
     * @param timeEnabled the value to assign to the time enabled flag.
     * @param source the source of the change.
     */
    void setTimeEnabled(boolean timeEnabled, Object source);

    /**
     * Sets the title.
     *
     * @param title the new title
     * @param source the source of the change
     */
    void setTitle(String title, Object source);

    /**
     * Sets the visible.
     *
     * @param visible the new visible
     * @param source the source of the change
     */
    void setVisible(boolean visible, Object source);

    /**
     * Sets the x offset.
     *
     * @param xOffset the new x offset
     * @param source the source of the change
     */
    void setxOffset(int xOffset, Object source);

    /**
     * Sets the xy offset together.
     *
     * @param xOffset the x offset
     * @param yOffset the y offset
     * @param source the source of the change.
     */
    void setXYOffset(int xOffset, int yOffset, Object source);

    /**
     * Sets the y offset.
     *
     * @param yOffset the new y offset
     * @param source the source of the change
     */
    void setyOffset(int yOffset, Object source);
}
