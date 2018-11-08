package io.opensphere.mantle.data.element.impl;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Default implementation of the {@link MapDataElement} interface.
 */
public class DefaultMapDataElement extends DefaultDataElement implements MapDataElement
{
    /** The {@link MapGeometrySupport}. */
    private MapGeometrySupport myMapGeometrySupport;

    /**
     * @param source the source from which to copy data.
     */
    public DefaultMapDataElement(DefaultMapDataElement source)
    {
        super(source);

        myMapGeometrySupport = source.myMapGeometrySupport.createCopy();
    }

    /**
     * CTOR with id and {@link MapGeometrySupport}. Note: Displayable is true by
     * default.
     *
     * @param id - the ID
     * @param mgs - the {@link MapGeometrySupport}, can not be null
     */
    public DefaultMapDataElement(long id, MapGeometrySupport mgs)
    {
        this(id, TimeSpan.TIMELESS, mgs);
    }

    /**
     * CTOR with id, {@link TimeSpan}, {@link DataTypeInfo}, and and
     * {@link MapGeometrySupport}. Note: Displayable is true by default.
     *
     * @param id - the ID
     * @param ts - the TimeSpan, note that if null will be set to
     *            {@link TimeSpan}.TIMELESS.
     * @param dti - the {@link DataTypeInfo}, can be null if not linked to a
     *            type
     * @param mgs - the {@link MapGeometrySupport}, can not be null
     */
    public DefaultMapDataElement(long id, TimeSpan ts, DataTypeInfo dti, MapGeometrySupport mgs)
    {
        this(id, ts, dti, null, mgs);
    }

    /**
     * CTOR with all parameters. Note: Displayable is true by default.
     *
     * @param id - the ID
     * @param ts - the TimeSpan, note that if null will be set to
     *            {@link TimeSpan}.TIMELESS.
     * @param dti - the {@link DataTypeInfo}, can be null if not linked to a
     *            type
     * @param mdp - the {@link MetaDataProvider}, can be null if there is no
     *            provider.
     * @param mgs - the {@link MapGeometrySupport}, can not be null
     */
    public DefaultMapDataElement(long id, TimeSpan ts, DataTypeInfo dti, MetaDataProvider mdp, MapGeometrySupport mgs)
    {
        super(id, ts, dti, mdp, true);
        if (mgs == null)
        {
            throw new IllegalArgumentException("MapGeometrySupport can not be null");
        }
        myMapGeometrySupport = mgs;
        getVisualizationState().setColor(mgs.getColor());
    }

    /**
     * CTOR with id, {@link TimeSpan}, and {@link MapGeometrySupport}. Note:
     * Displayable is true by default.
     *
     * @param id - the ID
     * @param ts - the TimeSpan, note that if null will be set to
     *            {@link TimeSpan}.TIMELESS.
     * @param mgs - the {@link MapGeometrySupport}, can not be null
     */
    public DefaultMapDataElement(long id, TimeSpan ts, MapGeometrySupport mgs)
    {
        this(id, ts, null, mgs);
    }

    @Override
    public MapGeometrySupport getMapGeometrySupport()
    {
        return myMapGeometrySupport;
    }

    @Override
    public boolean isMappable()
    {
        return true;
    }

    @Override
    public void setMapGeometrySupport(MapGeometrySupport mgs)
    {
        if (mgs == null)
        {
            throw new IllegalArgumentException("MapGeometrySupport can not be null");
        }

        myMapGeometrySupport = mgs;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(super.toString());
        sb.append("MapGeometrySupport:").append(myMapGeometrySupport == null ? "NULL" : "").append('\n');
        if (myMapGeometrySupport != null)
        {
            sb.append(myMapGeometrySupport.toString());
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.element.impl.DefaultDataElement#cloneForDatatype(io.opensphere.mantle.data.DataTypeInfo, long)
     */
    @Override
    public DataElement cloneForDatatype(DataTypeInfo datatype, long newId)
    {
        DefaultMapDataElement clone = new DefaultMapDataElement(this);
        clone.setId(newId);
        clone.setDataTypeInfo(datatype);
        clone.setMetaDataProvider(getMetaData().createCopy(datatype));

        return clone;
    }
}
