package io.opensphere.core.util.swing.pie;

import java.awt.Image;
import java.util.Collection;

/**
 * The model for icons around the outer ring of the pie chart.
 */
public interface IconModel
{
    /**
     * Adds an arc.
     *
     * @param arc the arc
     */
    void addArc(Arc arc);

    /**
     * Adds the change listener.
     *
     * @param listener the listener
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Adds an icon at the given angle.
     *
     * @param icon the icon
     * @param angle the angle, where 0 &lt;= angle &lt; 360, and 0 means up
     * @param userObject the user object
     */
    void addIcon(Image icon, float angle, Object userObject);

    /**
     * Removes all arcs.
     */
    void clearArcs();

    /**
     * Removes all icons.
     */
    void clearIcons();

    /**
     * Gets the arcs.
     *
     * @return the arcs
     */
    Collection<Arc> getArcs();

    /**
     * Gets the icons and their angles.
     *
     * @return a Collection of Pairs where the first item is the icon and the
     *         second item is the angle.
     */
    Collection<IconInfo> getIcons();

    /**
     * Gets the mouse-over icon info.
     *
     * @return the mouse-over icon info, or null
     */
    IconInfo getMouseOverIcon();

    /**
     * Removes the change listener.
     *
     * @param listener the listener
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Sets the mouse-over icon info.
     *
     * @param iconInfo the icon info
     * @param source the source of the change
     */
    void setMouseOverIcon(IconInfo iconInfo, Object source);

    /**
     * Icon info helper class.
     */
    class IconInfo
    {
        /** The icon. */
        private final Image myIcon;

        /** The angle. */
        private final float myAngle;

        /** The user object. */
        private final Object myUserObject;

        /**
         * Constructor.
         *
         * @param icon the icon
         * @param angle the angle
         * @param userObject the user object
         */
        public IconInfo(Image icon, float angle, Object userObject)
        {
            super();
            myIcon = icon;
            myAngle = angle;
            myUserObject = userObject;
        }

        /**
         * Gets the angle.
         *
         * @return the angle
         */
        public float getAngle()
        {
            return myAngle;
        }

        /**
         * Gets the icon.
         *
         * @return the icon
         */
        public Image getIcon()
        {
            return myIcon;
        }

        /**
         * Gets the user object.
         *
         * @return the user object
         */
        public Object getUserObject()
        {
            return myUserObject;
        }
    }
}
