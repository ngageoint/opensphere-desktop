package io.opensphere.server.services;

import java.awt.Color;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.ChangeSupport.Callback;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.server.services.ServerDataTypeSync.ServerSyncChangeEvent.SyncChangeType;

/**
 * The Class ServerDataTypeSync.
 */
public class ServerDataTypeSync
{
    /** The change support for Server synchronization listeners. */
    private final ChangeSupport<ServerSyncChangeListener> myChangeSupport = new WeakChangeSupport<>();

    /** The data types' default color. */
    private volatile Color myDefaultColor = AbstractServerDataTypeInfo.DEFAULT_TYPE_COLOR;

    /** Flag indicating whether there is a type that handles data. */
    private volatile boolean myHasData;

    /** Flag indicating whether there is a type that handles map tiles. */
    private volatile boolean myHasMapTiles;

    /** The data types' time extents. */
    private volatile TimeExtents myTimeExtents;

    /**
     * Adds a change listener.
     *
     * @param listener the listener to add
     */
    public void addListener(ServerSyncChangeListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Gets the default color.
     *
     * @return the default color
     */
    public Color getDefaultColor()
    {
        return myDefaultColor;
    }

    /**
     * Gets the time extents.
     *
     * @return the time extents
     */
    public TimeExtents getTimeExtents()
    {
        return myTimeExtents;
    }

    /**
     * Checks if there is a data type that handles data.
     *
     * @return true, if there is a data type that handles data
     */
    public boolean isHasData()
    {
        return myHasData;
    }

    /**
     * Checks if there is a data type that handles map tiles.
     *
     * @return true, if there is a data type that handles map tiles
     */
    public boolean isHasMapTiles()
    {
        return myHasMapTiles;
    }

    /**
     * Removes a change listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ServerSyncChangeListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Sets the default color.
     *
     * @param color the new default color
     * @param source the source of the change
     */
    public void setDefaultColor(Color color, Object source)
    {
        myDefaultColor = color;
        sendChangeEvent(SyncChangeType.SYNC_COLOR, source);
    }

    /**
     * Sets the flag indicating there is a data type that handles data.
     *
     * @param hasData the flag indicating there is a data type that handles
     *            data.
     * @param source the source of the change
     */
    public void setHasData(boolean hasData, Object source)
    {
        myHasData = hasData;
        sendChangeEvent(SyncChangeType.SYNC_HAS_DATA, source);
    }

    /**
     * Sets the flag indicating there is a data type that handles tiles.
     *
     * @param hasMapTiles the flag indicating there is a data type that handles
     *            tiles.
     * @param source the source of the change
     */
    public void setHasMapTiles(boolean hasMapTiles, Object source)
    {
        myHasMapTiles = hasMapTiles;
        sendChangeEvent(SyncChangeType.SYNC_HAS_TILES, source);
    }

    /**
     * Sets the time extents.
     *
     * @param extents the new extents
     * @param source the source of the change
     */
    public void setTimeExtents(TimeExtents extents, Object source)
    {
        myTimeExtents = extents;
        sendChangeEvent(SyncChangeType.SYNC_TIME_EXTENT, source);
    }

    /**
     * Send a change event to the synchronization listeners.
     *
     * @param type the type of synchronization change
     * @param source the source of the change
     */
    private void sendChangeEvent(final SyncChangeType type, final Object source)
    {
        myChangeSupport.notifyListeners(new Callback<ServerSyncChangeListener>()
        {
            @Override
            public void notify(ServerSyncChangeListener listener)
            {
                listener.handleSyncChangeEvent(type, source);
            }
        });
    }

    /**
     * ServerSyncChangeEvent class that prompts listeners when a synchronizable
     * change occurs.
     */
    public static class ServerSyncChangeEvent
    {
        /** The change type. */
        private final SyncChangeType myChangeType;

        /** The object that initiated the change. */
        private final Object mySource;

        /**
         * Instantiates a new server synchronization change event.
         *
         * @param type the type of change that occurred
         * @param source the source of the change
         */
        public ServerSyncChangeEvent(SyncChangeType type, Object source)
        {
            myChangeType = type;
            mySource = source;
        }

        /**
         * Gets the change type.
         *
         * @return the enumerated change type
         */
        public SyncChangeType getChangeType()
        {
            return myChangeType;
        }

        /**
         * Gets the source of the change.
         *
         * @return the source object
         */
        public Object getSource()
        {
            return mySource;
        }

        /** Change type enumeration. */
        public enum SyncChangeType
        {
            /** Data type default color change. */
            SYNC_COLOR,

            /** Data type that provides data was added. */
            SYNC_HAS_DATA,

            /** Data type that provides map tiles was added. */
            SYNC_HAS_TILES,

            /** Data type time extent change. */
            SYNC_TIME_EXTENT,
        }
    }

    /**
     * Listener interface for server synchronization changes.
     */
    @FunctionalInterface
    public interface ServerSyncChangeListener
    {
        /**
         * Handle Data Type Synchronization event.
         *
         * @param type the type of synchronization change
         * @param source the source of the change
         */
        void handleSyncChangeEvent(SyncChangeType type, Object source);
    }
}
