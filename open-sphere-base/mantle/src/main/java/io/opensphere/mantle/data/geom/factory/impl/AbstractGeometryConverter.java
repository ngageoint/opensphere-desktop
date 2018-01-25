package io.opensphere.mantle.data.geom.factory.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportToGeometryConverter;

/**
 * The Class AbstractGeometryConverter.
 */
public abstract class AbstractGeometryConverter implements MapGeometrySupportToGeometryConverter
{
    /** The Toolbox. */
    private final Toolbox myToolbox;

    /**
     * Creates the time constraints.
     *
     * @param tb the {@link Toolbox}
     * @param dti the {@link DataTypeInfo}
     * @param ts the {@link TimeSpan}
     * @return the constraints
     */
    protected static Constraints createTimeConstraints(Toolbox tb, DataTypeInfo dti, TimeSpan ts)
    {
        DataGroupInfo dgi = getDataGroupInfoFromDti(tb, dti);
        return dgi == null ? Constraints.createTimeOnlyConstraint(ts) : Constraints.createTimeOnlyConstraint(dgi.getId(), ts);
    }

    /**
     * Gets the data group info associated with a data type info.
     *
     * @param tb the {@link Toolbox}
     * @param dti the {@link DataTypeInfo}.
     * @return the {@link DataTypeInfo} or null if not found
     */
    protected static DataGroupInfo getDataGroupInfoFromDti(Toolbox tb, DataTypeInfo dti)
    {
        return dti.getParent();
    }

    /**
     * Instantiates a new abstract geometry converter.
     *
     * @param tb the {@link Toolbox}
     */
    public AbstractGeometryConverter(Toolbox tb)
    {
        myToolbox = tb;
    }

    @Override
    public Toolbox getToolbox()
    {
        return myToolbox;
    }
}
