package io.opensphere.mantle.data;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;

/**
 * Events from changes to DataTypeInfo.
 */
public abstract class AbstractDataTypeInfoChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The DataTypeInfo. */
    private final DataTypeInfo myDataTypeInfo;

    /** The source. */
    private final Object mySource;

    /** The type. */
    private final Type myType;

    /** A value if appropriate. */
    private final Object myValue;

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param aType - the {@link Type} of event.
     * @param source - the source of the event.
     */
    public AbstractDataTypeInfoChangeEvent(DataTypeInfo dti, Type aType, Object source)
    {
        this(dti, aType, null, source);
    }

    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param aType - the {@link Type} of event.
     * @param value - the value that changed if appropriate.
     * @param source - the source of the event.
     */
    public AbstractDataTypeInfoChangeEvent(DataTypeInfo dti, Type aType, Object value, Object source)
    {
        myDataTypeInfo = dti;
        myType = aType;
        mySource = source;
        myValue = value;
    }

    /**
     * Gets the {@link DataTypeInfo} dispatching the event.
     *
     * @return the {@link DataTypeInfo}
     */
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    /**
     * Gets the DataType key.
     *
     * @return the data type key.
     */
    public String getDataTypeKey()
    {
        return myDataTypeInfo.getTypeKey();
    }

    @Override
    public String getDescription()
    {
        return "Changes to a DataTypeInfo";
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType()
    {
        return myType;
    }

    /**
     * Gets the value for the change if provided for this type.
     *
     * @return the value or null if none.
     */
    public Object getValue()
    {
        return myValue;
    }

    /**
     * The Type of event.
     */
    public enum Type
    {
        /** Lift for the data type. */
        LIFT_CHANGED,

        /** loads to data type have changed. */
        LOADS_TO_CHANGED,

        /** Meta-data key was added to set. */
        METADATA_KEY_ADDED,

        /** Meta-data key was removed from set. */
        METADATA_KEY_REMOVED,

        /** A special key designator has changed for the metadata. */
        METADATA_SPECIAL_KEY_CHANGED,

        /** The Rebuild Geometry Request. */
        REBUILD_GEOMETRY_REQUEST,

        /** Indicates that the DataTypeInfo is in use. */
        SOURCE_IN_USE,

        /** Indicates that the DataTypeInfo is no longer in use. */
        SOURCE_NO_LONGER_IN_USE,

        /** Tags for this data type have changed. */
        TAGS_CHANGED,

        /** The time extens changed. */
        TIME_EXTENTS_CHANGED,

        /** Indicates that the Type Color has changed. */
        TYPE_COLOR_CHANGED,

        /** Type Visibility Changed. */
        VISIBILITY_CHANGED,

        /** Indicates that the Z-Order of the type has changed. */
        Z_ORDER_CHANGED;
    }
}
