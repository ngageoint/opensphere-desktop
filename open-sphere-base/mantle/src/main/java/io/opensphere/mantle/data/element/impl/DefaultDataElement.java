package io.opensphere.mantle.data.element.impl;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;

/**
 * A default implementation of the {@link DataElement} interface.
 */
public class DefaultDataElement implements DataElement
{
    /**
     * The id of this element within the cache.
     */
    private long myCacheId;

    /** The DataTypeInfo. */
    private final DataTypeInfo myDataTypeInfo;

    /** the ID for the DataElement. */
    private final long myId;

    /** The MetaDataProvider. */
    private MetaDataProvider myMetaDataProvider;

    /** The TimeSpan. */
    private TimeSpan myTimeSpan = TimeSpan.TIMELESS;

    /** The visualization state. */
    private final VisualizationState myVisualizationState;

    /**
     * CTOR with id only. Note: Displayable is true by default.
     *
     * @param id the ID
     */
    public DefaultDataElement(long id)
    {
        this(id, TimeSpan.TIMELESS);
    }

    /**
     * CTOR with id and {@link TimeSpan}. Note: Displayable is true by default.
     *
     * @param id the ID
     * @param ts the TimeSpan, note that if null will be set to {@link TimeSpan}
     *            .TIMELESS.
     */
    public DefaultDataElement(long id, TimeSpan ts)
    {
        this(id, ts, null);
    }

    /**
     * CTOR with id, {@link TimeSpan}, and {@link DataTypeInfo}. Note:
     * Displayable is true by default.
     *
     * @param id the ID
     * @param ts the TimeSpan, note that if null will be set to {@link TimeSpan}
     *            .TIMELESS.
     * @param dti the {@link DataTypeInfo}, can be null if not linked to a type
     */
    public DefaultDataElement(long id, TimeSpan ts, DataTypeInfo dti)
    {
        this(id, ts, dti, null);
    }

    /**
     * CTOR with all parameters. Note: Displayable is true by default.
     *
     * @param id the ID
     * @param ts the TimeSpan, note that if null will be set to {@link TimeSpan}
     *            .TIMELESS.
     * @param dti the {@link DataTypeInfo}, can be null if not linked to a type
     * @param mdp the {@link MetaDataProvider}, can be null if there is no
     *            provider.
     */
    public DefaultDataElement(long id, TimeSpan ts, DataTypeInfo dti, MetaDataProvider mdp)
    {
        this(id, ts, dti, mdp, false);
    }

    /**
     * CTOR with all parameters. Note: Displayable is true by default.
     *
     * @param id the ID
     * @param ts the TimeSpan, note that if null will be set to
     * @param dti the {@link DataTypeInfo}, can be null if not linked to a type
     * @param mdp the {@link MetaDataProvider}, can be null if there is no
     *            provider.
     * @param isMapDataElement the is map data element {@link TimeSpan}
     *            .TIMELESS.
     */
    protected DefaultDataElement(long id, TimeSpan ts, DataTypeInfo dti, MetaDataProvider mdp, boolean isMapDataElement)
    {
        myId = id;
        myTimeSpan = ts;
        if (myTimeSpan == null)
        {
            myTimeSpan = TimeSpan.TIMELESS;
        }
        myDataTypeInfo = dti;
        myMetaDataProvider = mdp;
        myVisualizationState = new VisualizationState(isMapDataElement);
    }

    @Override
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    @Override
    public long getId()
    {
        return myId;
    }

    @Override
    public long getIdInCache()
    {
        return myCacheId;
    }

    @Override
    public MetaDataProvider getMetaData()
    {
        return myMetaDataProvider;
    }

    @Override
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    @Override
    public VisualizationState getVisualizationState()
    {
        return myVisualizationState;
    }

    @Override
    public boolean isDisplayable()
    {
        return true;
    }

    @Override
    public boolean isMappable()
    {
        return false;
    }

    @Override
    public void setDisplayable(boolean displayable, Object source)
    {
        throw new UnsupportedOperationException("Use visibility flag in VisualizationState");
    }

    @Override
    public void setIdInCache(long cacheId)
    {
        myCacheId = cacheId;
    }

    /**
     * Sets the MetaDataProvider.
     *
     * @param mdp the MetaDataProvider to set.
     */
    public void setMetaDataProvider(MetaDataProvider mdp)
    {
        myMetaDataProvider = mdp;
    }

    /**
     * Sets the time span for this DataElement. Note: if ts is null, time span
     * will be set to TimeSpan.TIMELESS.
     *
     * @param ts the time span to set.
     */
    public void setTimeSpan(TimeSpan ts)
    {
        myTimeSpan = ts;
        if (myTimeSpan == null)
        {
            myTimeSpan = TimeSpan.TIMELESS;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append(":\nTimeSpan: ").append(myTimeSpan.toDisplayString())
                .append("\nDataTypeInfo: Name: ").append(myDataTypeInfo == null ? "NULL" : myDataTypeInfo.getDisplayName())
                .append(" Key[").append(myDataTypeInfo == null ? "NULL" : myDataTypeInfo.getTypeKey()).append('\n')
                .append(myVisualizationState.toString()).append("\n" + "MetaDataProvider:")
                .append(myMetaDataProvider == null ? "NULL" : "").append('\n');
        if (myMetaDataProvider != null)
        {
            sb.append(myMetaDataProvider.toString());
        }
        return sb.toString();
    }
}
