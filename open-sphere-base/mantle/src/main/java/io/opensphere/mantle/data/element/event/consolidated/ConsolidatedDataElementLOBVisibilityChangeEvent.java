package io.opensphere.mantle.data.element.event.consolidated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Class ConsolidatedDataElementLOBVisibilityChangeEvent.
 */
public class ConsolidatedDataElementLOBVisibilityChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /** The invisible set. */
    private final List<Long> myLobInvisible;

    /** The visible set. */
    private final List<Long> myLobVisible;

    /**
     * Instantiates a new LOB visibility change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param visibleIds the ids that are becoming visible
     * @param invisibleIds the ids that are becoming invisible
     * @param source the source
     */
    public ConsolidatedDataElementLOBVisibilityChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, List<Long> visibleIds,
            List<Long> invisibleIds, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myLobVisible = Collections
                .unmodifiableList(visibleIds instanceof LinkedList<?> ? new ArrayList<Long>(visibleIds) : visibleIds);
        myLobInvisible = Collections
                .unmodifiableList(invisibleIds instanceof LinkedList<?> ? new ArrayList<Long>(invisibleIds) : invisibleIds);
    }

    /**
     * Instantiates a new LOB visibility change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param visibleIds the ids that are becoming visible
     * @param invisibleIds the ids that are becoming invisible
     * @param source the source
     */
    public ConsolidatedDataElementLOBVisibilityChangeEvent(long regIds, String dataTypeKey, List<Long> visibleIds,
            List<Long> invisibleIds, Object source)
    {
        super(regIds, dataTypeKey, source);
        myLobVisible = Collections
                .unmodifiableList(visibleIds instanceof LinkedList<?> ? new ArrayList<Long>(visibleIds) : visibleIds);
        myLobInvisible = Collections
                .unmodifiableList(invisibleIds instanceof LinkedList<?> ? new ArrayList<Long>(invisibleIds) : invisibleIds);
    }

    /**
     * Instantiates a new LOB visibility change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param visibleIds the ids that are becoming visible
     * @param invisibleIds the ids that are becoming invisible
     * @param source the source
     */
    public ConsolidatedDataElementLOBVisibilityChangeEvent(long[] regIds, Set<String> dataTypeKeys, List<Long> visibleIds,
            List<Long> invisibleIds, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myLobVisible = Collections
                .unmodifiableList(visibleIds instanceof LinkedList<?> ? new ArrayList<Long>(visibleIds) : visibleIds);
        myLobInvisible = Collections
                .unmodifiableList(invisibleIds instanceof LinkedList<?> ? new ArrayList<Long>(invisibleIds) : invisibleIds);
    }

    /**
     * Gets the invisible set.
     *
     * @return the invisible set
     */
    public List<Long> getInvisibleIdSet()
    {
        return myLobInvisible;
    }

    /**
     * Gets the visible set.
     *
     * @return the visible set
     */
    public List<Long> getVisibleIdSet()
    {
        return myLobVisible;
    }
}
