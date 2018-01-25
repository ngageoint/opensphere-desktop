package io.opensphere.server.services;

import java.awt.Color;
import java.util.Collection;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.TimeExtents;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.server.services.ServerDataTypeSync.ServerSyncChangeEvent.SyncChangeType;
import io.opensphere.server.services.ServerDataTypeSync.ServerSyncChangeListener;

/**
 * The Class ServerDataTypeInfo.
 */
public abstract class AbstractServerDataTypeInfo extends DefaultDataTypeInfo implements ServerSyncChangeListener
{
    /**
     * The default color for feature data if base color is not specified or
     * cannot be determined.
     */
    public static final Color DEFAULT_TYPE_COLOR = Color.WHITE;

    /**
     * Object that handles synchronization between {@link DataTypeInfo}s with
     * the same parent group.
     */
    private ServerDataTypeSync myTypeSync;

    /**
     * Instantiates a new server data type info.
     *
     * @param tb the tb
     * @param serverName the server name
     * @param typeKey the type key
     * @param typeName the type name
     * @param displayName the display name
     * @param filtersData Flag indicating whether this type handles DataFilters
     *            from the DataFilterRegistry
     */
    public AbstractServerDataTypeInfo(Toolbox tb, String serverName, String typeKey, String typeName, String displayName,
            boolean filtersData)
    {
        super(tb, serverName, typeKey, typeName, displayName, filtersData);
    }

    /**
     * Get the names that should be used to match this layer to layers from
     * another OGC Service (e.g. to marry WMS layers with their WFS
     * counterparts).
     *
     * @return the names for comparison
     */
    public abstract Collection<String> getNamesForComparison();

    @Override
    public abstract void handleSyncChangeEvent(SyncChangeType type, Object source);

    /**
     * Sets the default type color.
     *
     * @param c the new default type color
     */
    public void setDefaultTypeColor(Color c)
    {
        if (getBasicVisualizationInfo() instanceof ServerBasicVisualizationInfo)
        {
            ((ServerBasicVisualizationInfo)getBasicVisualizationInfo()).setDefaultTypeColor(c);
        }
    }

    /**
     * Sets the TimeExtents for this type. Overrides the base class type to
     * optionally sync the extents with other Data types.
     *
     * @param timeExtents - the {@link TimeExtents}
     * @param syncTime - sync extents with other types
     */
    public void setTimeExtents(TimeExtents timeExtents, boolean syncTime)
    {
        super.setTimeExtents(timeExtents, this);
        if (syncTime && getTypeSync() != null)
        {
            getTypeSync().setTimeExtents(timeExtents, this);
        }
    }

    @Override
    public void setTimeExtents(TimeExtents timeExtents, Object source)
    {
        setTimeExtents(timeExtents, true);
    }

    /**
     * Sets the type synchronization object.
     *
     * @param typeSync the new type synchronization object
     */
    public void setTypeSync(ServerDataTypeSync typeSync)
    {
        myTypeSync = typeSync;
        typeSync.addListener(this);
    }

    /**
     * Get the type synchronization object.
     *
     * @return the server data type synchronization object
     */
    protected ServerDataTypeSync getTypeSync()
    {
        return myTypeSync;
    }
}
