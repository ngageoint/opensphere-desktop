package io.opensphere.core.control;

import java.awt.Point;

import io.opensphere.core.geometry.Geometry;

/**
 * Interface for listening for pick events.
 */
public interface PickListener
{
    /**
     * Handle an event when the picked geometry has changed. This is guaranteed
     * to happen on the AWT event thread.
     *
     * @param evt The event which has occurred related to picking.
     */
    void handlePickEvent(PickEvent evt);

    /**
     * Event generated for pick related notifications.
     */
    class PickEvent
    {
        /** Position of the cursor at the time of the event. */
        private final Point myLocation;

        /** The geometry which has been picked. */
        private final Geometry myPickedGeometry;

        /**
         * Construct me.
         *
         * @param picked Picked geometry.
         * @param location Position of the cursor at the time of the event.
         */
        public PickEvent(Geometry picked, Point location)
        {
            myPickedGeometry = picked;
            myLocation = location;
        }

        /**
         * Get the location.
         *
         * @return the location
         */
        public Point getLocation()
        {
            return myLocation;
        }

        /**
         * Get the pickedGeometry.
         *
         * @return the pickedGeometry
         */
        public Geometry getPickedGeometry()
        {
            return myPickedGeometry;
        }
    }
}
