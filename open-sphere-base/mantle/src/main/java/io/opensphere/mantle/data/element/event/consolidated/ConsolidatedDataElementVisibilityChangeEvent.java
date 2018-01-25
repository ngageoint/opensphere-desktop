package io.opensphere.mantle.data.element.event.consolidated;

import java.util.List;
import java.util.Set;

import gnu.trove.set.hash.TLongHashSet;

/**
 * The Class ConsolidatedDataElementVisibilityChangeEvent.
 */
public class ConsolidatedDataElementVisibilityChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /** The de-select set. */
    private final TLongHashSet myInvisible;

    /** The select set. */
    private final TLongHashSet myVisible;

    /**
     * Instantiates a new visibility change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param visibleIds the ids that are becoming visible
     * @param invisibleIds the ids that are becoming invisible
     * @param source the source
     */
    public ConsolidatedDataElementVisibilityChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, TLongHashSet visibleIds,
            TLongHashSet invisibleIds, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myVisible = visibleIds == null ? new TLongHashSet() : visibleIds;
        myInvisible = invisibleIds == null ? new TLongHashSet() : invisibleIds;
    }

    /**
     * Instantiates a new visibility change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param visibleIds the ids that are becoming visible
     * @param invisibleIds the ids that are becoming invisible
     * @param source the source
     */
    public ConsolidatedDataElementVisibilityChangeEvent(long regIds, String dataTypeKey, TLongHashSet visibleIds,
            TLongHashSet invisibleIds, Object source)
    {
        super(regIds, dataTypeKey, source);
        myVisible = visibleIds == null ? new TLongHashSet() : visibleIds;
        myInvisible = invisibleIds == null ? new TLongHashSet() : invisibleIds;
    }

    /**
     * Instantiates a new visibility change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param visibleIds the ids that are becoming visible
     * @param invisibleIds the ids that are becoming invisible
     * @param source the source
     */
    public ConsolidatedDataElementVisibilityChangeEvent(long[] regIds, Set<String> dataTypeKeys, TLongHashSet visibleIds,
            TLongHashSet invisibleIds, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myVisible = visibleIds == null ? new TLongHashSet() : visibleIds;
        myInvisible = invisibleIds == null ? new TLongHashSet() : invisibleIds;
    }

    /**
     * Gets the invisible set.
     *
     * @return the invisible set
     */
    public TLongHashSet getInvisibleIdSet()
    {
        return myInvisible;
    }

    /**
     * Gets the visible set.
     *
     * @return the visible set
     */
    public TLongHashSet getVisibleIdSet()
    {
        return myVisible;
    }
}
