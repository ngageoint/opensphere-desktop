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
    private DataTypeInfo myDataTypeInfo;

    /** the ID for the DataElement. */
    private long myId;

    /** The MetaDataProvider. */
    private MetaDataProvider myMetaDataProvider;

    /** The TimeSpan. */
    private TimeSpan myTimeSpan = TimeSpan.TIMELESS;

    /** The visualization state. */
    private VisualizationState myVisualizationState;

    /**
     * Copy constructor.
     *
     * @param source the object from which to copy data.
     */
    protected DefaultDataElement(DefaultDataElement source)
    {
        myCacheId = source.myCacheId;
        myDataTypeInfo = source.myDataTypeInfo;
        myId = source.myId;
        myMetaDataProvider = source.myMetaDataProvider;
        myTimeSpan = source.myTimeSpan;

        myVisualizationState = new VisualizationState(source.myVisualizationState.isMapDataElement());
        myVisualizationState.setColor(source.getVisualizationState().getColor());
        myVisualizationState.setAltitudeAdjust(source.getVisualizationState().getAltitudeAdjust());
        myVisualizationState.setHasAlternateGeometrySupport(getVisualizationState().hasAlternateGeometrySupport());
        myVisualizationState.setLobVisible(getVisualizationState().isLobVisible());
        myVisualizationState.setSelected(getVisualizationState().isSelected());
        myVisualizationState.setVisible(getVisualizationState().isVisible());
    }

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

    /**
     * Sets the value of the {@link #myVisualizationState} field.
     *
     * @param visualizationState the value to store in the
     *            {@link #myVisualizationState} field.
     */
    protected void setVisualizationState(VisualizationState visualizationState)
    {
        myVisualizationState = visualizationState;
    }

    /**
     * Sets the value of the {@link #myId} field.
     *
     * @param id the value to store in the {@link #myId} field.
     */
    protected void setId(long id)
    {
        myId = id;
    }

    /**
     * Sets the value of the {@link #myDataTypeInfo} field.
     *
     * @param dataTypeInfo the value to store in the {@link #myDataTypeInfo}
     *            field.
     */
    protected void setDataTypeInfo(DataTypeInfo dataTypeInfo)
    {
        myDataTypeInfo = dataTypeInfo;
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

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.DataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo)
     */
    @Override
    public DataElement cloneForDatatype(DataTypeInfo datatype)
    {
        DefaultDataElement clone = new DefaultDataElement(this);
        clone.setDataTypeInfo(datatype);

//        DefaultDataElement clone = new DefaultDataElement(myId * 10, myTimeSpan, datatype, myMetaDataProvider,
//                myVisualizationState.isMapDataElement());
//
//        VisualizationState visualizationState = clone.getVisualizationState();
//        visualizationState.setColor(getVisualizationState().getColor());
//        visualizationState.setAltitudeAdjust(getVisualizationState().getAltitudeAdjust());
//        visualizationState.setHasAlternateGeometrySupport(getVisualizationState().hasAlternateGeometrySupport());
//        visualizationState.setLobVisible(getVisualizationState().isLobVisible());
//        visualizationState.setSelected(getVisualizationState().isSelected());
//        visualizationState.setVisible(getVisualizationState().isVisible());

        return clone;
    }
}
