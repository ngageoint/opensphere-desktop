package io.opensphere.mantle.data.element.event.consolidated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Class ConsolidatedDataElementHighlightChangeEvent.
 */
public class ConsolidatedDataElementHighlightChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /** The visible set. */
    private final List<Long> myHighlighted;

    /** The invisible set. */
    private final List<Long> myUnhighlighted;

    /**
     * Instantiates a new highlight change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param highlightedIds the ids that are becoming highlighted
     * @param unhighlightedIds the ids that are becoming unhighlighted
     * @param source the source
     */
    public ConsolidatedDataElementHighlightChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, List<Long> highlightedIds,
            List<Long> unhighlightedIds, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myHighlighted = Collections
                .unmodifiableList(highlightedIds instanceof LinkedList<?> ? new ArrayList<>(highlightedIds) : highlightedIds);
        myUnhighlighted = Collections.unmodifiableList(
                unhighlightedIds instanceof LinkedList<?> ? new ArrayList<>(unhighlightedIds) : unhighlightedIds);
    }

    /**
     * Instantiates a new highlight change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param highlightedIds the ids that are becoming highlighted
     * @param unhighlightedIds the ids that are becoming unhighlighted
     * @param source the source
     */
    public ConsolidatedDataElementHighlightChangeEvent(long regIds, String dataTypeKey, List<Long> highlightedIds,
            List<Long> unhighlightedIds, Object source)
    {
        super(regIds, dataTypeKey, source);
        myHighlighted = Collections
                .unmodifiableList(highlightedIds instanceof LinkedList<?> ? new ArrayList<>(highlightedIds) : highlightedIds);
        myUnhighlighted = Collections.unmodifiableList(
                unhighlightedIds instanceof LinkedList<?> ? new ArrayList<>(unhighlightedIds) : unhighlightedIds);
    }

    /**
     * Instantiates a new highlight change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param highlightedIds the ids that are becoming highlighted
     * @param unhighlightedIds the ids that are becoming unhighlighted
     * @param source the source
     */
    public ConsolidatedDataElementHighlightChangeEvent(long[] regIds, Set<String> dataTypeKeys, List<Long> highlightedIds,
            List<Long> unhighlightedIds, Object source)
    {
        super(regIds, dataTypeKeys, source);
        myHighlighted = Collections
                .unmodifiableList(highlightedIds instanceof LinkedList<?> ? new ArrayList<>(highlightedIds) : highlightedIds);
        myUnhighlighted = Collections.unmodifiableList(
                unhighlightedIds instanceof LinkedList<?> ? new ArrayList<>(unhighlightedIds) : unhighlightedIds);
    }

    /**
     * Gets the highlighted set.
     *
     * @return the highlighted set
     */
    public List<Long> getHighlightedIdSet()
    {
        return myHighlighted;
    }

    /**
     * Gets the unhighlighted set.
     *
     * @return the unhighlighted set
     */
    public List<Long> getUnHighlightedIdSet()
    {
        return myUnhighlighted;
    }
}
