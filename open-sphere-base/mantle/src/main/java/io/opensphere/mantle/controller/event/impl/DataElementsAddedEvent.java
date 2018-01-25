package io.opensphere.mantle.controller.event.impl;

import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.rangeset.DefaultRangedLongSet;
import io.opensphere.core.util.rangeset.ImmutableRangedLongSet;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class CurrentDataTypeChangedEvent.
 */
public class DataElementsAddedEvent extends AbstractDataTypeControllerEvent
{
    /** The data element id set. */
    private final ImmutableRangedLongSet myAddedDataElementIdSet;

    /** The are mappable. */
    private final boolean myAreMappable;

    /** The data type info. */
    private final DataTypeInfo myType;

    /**
     * Instantiates a new DataElementsAddedEvent.
     *
     * @param dti the new current {@link DataTypeInfo}.
     * @param addedDataElementIds the ids that were added
     * @param areMappable the are mappable
     * @param source the source of the change.
     */
    public DataElementsAddedEvent(DataTypeInfo dti, List<Long> addedDataElementIds, boolean areMappable, Object source)
    {
        super(source);
        Utilities.checkNull(dti, "dti");
        Utilities.checkNull(addedDataElementIds, "ids");
        myType = dti;
        myAddedDataElementIdSet = new ImmutableRangedLongSet(new DefaultRangedLongSet(addedDataElementIds));
        myAreMappable = areMappable;
    }

    /**
     * Instantiates a new DataElementsAddedEvent.
     *
     * @param dti the new current {@link DataTypeInfo}.
     * @param addedDataElementIds the ids that were added
     * @param areMappable the are mappable
     * @param source the source of the change.
     */
    public DataElementsAddedEvent(DataTypeInfo dti, long[] addedDataElementIds, boolean areMappable, Object source)
    {
        super(source);
        Utilities.checkNull(dti, "dti");
        Utilities.checkNull(addedDataElementIds, "ids");
        myType = dti;
        myAddedDataElementIdSet = new ImmutableRangedLongSet(new DefaultRangedLongSet(addedDataElementIds));
        myAreMappable = areMappable;
    }

    /**
     * Are mappable.
     *
     * @return true, if successful
     */
    public boolean areMappable()
    {
        return myAreMappable;
    }

    /**
     * Gets the data element ids.
     *
     * @return the ids
     */
    public ImmutableRangedLongSet getAddedDataElementIds()
    {
        return myAddedDataElementIdSet;
    }

    @Override
    public String getDescription()
    {
        return (myAreMappable ? "Map" : "") + " Data Elements Added To Type:  "
                + (myType == null ? "NONE" : myType.getDisplayName()) + " by"
                + (getSource() == null ? "UNKNOWN" : getSource().getClass().getSimpleName()) + " Count: "
                + myAddedDataElementIdSet.valueCount();
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public DataTypeInfo getType()
    {
        return myType;
    }
}
