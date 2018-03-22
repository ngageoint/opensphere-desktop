package io.opensphere.mantle.controller.event.impl;

import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.rangeset.DefaultRangedLongSet;
import io.opensphere.core.util.rangeset.ImmutableRangedLongSet;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.controller.event.AbstractDataTypeControllerEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Class CurrentDataTypeChangedEvent.
 */
public class DataElementsRemovedEvent extends AbstractDataTypeControllerEvent
{
    /** The are mappable. */
    private final boolean myAreMappable;

    /** The data element id set. */
    private final ImmutableRangedLongSet myRemovedDataElementIdSet;

    /** The data type info. */
    private final DataTypeInfo myType;

    /** Whether this event is for a full clear of the layer. */
    private boolean myFullClear;

    /**
     * Instantiates a new DataElementsRemovedEvent.
     *
     * @param dti the new current {@link DataTypeInfo}.
     * @param removedIds the ids that are removed.
     * @param areMappable the are mappable.
     * @param source the source of the change.
     */
    public DataElementsRemovedEvent(DataTypeInfo dti, List<Long> removedIds, boolean areMappable, Object source)
    {
        this(dti, new ImmutableRangedLongSet(new DefaultRangedLongSet(removedIds)), areMappable, source);
    }

    /**
     * Instantiates a new DataElementsRemovedEvent.
     *
     * @param dti the new current {@link DataTypeInfo}.
     * @param removedIds the ids that are removed.
     * @param areMappable the are mappable.
     * @param source the source of the change.
     */
    public DataElementsRemovedEvent(DataTypeInfo dti, long[] removedIds, boolean areMappable, Object source)
    {
        this(dti, new ImmutableRangedLongSet(new DefaultRangedLongSet(removedIds)), areMappable, source);
    }

    /**
     * Instantiates a new DataElementsRemovedEvent.
     *
     * @param dti the new current {@link DataTypeInfo}.
     * @param removedIds the ids that are removed.
     * @param areMappable the are mappable.
     * @param source the source of the change.
     */
    public DataElementsRemovedEvent(DataTypeInfo dti, RangedLongSet removedIds, boolean areMappable, Object source)
    {
        this(dti, new ImmutableRangedLongSet(new DefaultRangedLongSet(removedIds)), areMappable, source);
    }

    /**
     * Instantiates a new DataElementsRemovedEvent.
     *
     * @param dti the new current {@link DataTypeInfo}.
     * @param removedIds the ids that are removed.
     * @param areMappable the are mappable.
     * @param source the source of the change.
     */
    private DataElementsRemovedEvent(DataTypeInfo dti, ImmutableRangedLongSet removedIds, boolean areMappable, Object source)
    {
        super(source);
        Utilities.checkNull(dti, "dti");
        Utilities.checkNull(removedIds, "ids");
        myType = dti;
        myRemovedDataElementIdSet = removedIds;
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

    @Override
    public String getDescription()
    {
        return (myAreMappable ? "Map" : "") + " Data Elements Removed From Type:  "
                + (myType == null ? "NONE" : myType.getDisplayName()) + " by"
                + (getSource() == null ? "UNKNOWN" : getSource().getClass().getSimpleName()) + " Count: "
                + myRemovedDataElementIdSet.valueCount();
    }

    /**
     * Gets the data element ids.
     *
     * @return the ids
     */
    public ImmutableRangedLongSet getRemovedDataElementIds()
    {
        return myRemovedDataElementIdSet;
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

    /**
     * Gets whether this event is for a full clear of the layer.
     *
     * @return whether it's a full clear
     */
    public boolean isFullClear()
    {
        return myFullClear;
    }

    /**
     * Sets whether this event is for a full clear of the layer.
     *
     * @param fullClear whether it's a full clear
     */
    public void setFullClear(boolean fullClear)
    {
        myFullClear = fullClear;
    }
}
