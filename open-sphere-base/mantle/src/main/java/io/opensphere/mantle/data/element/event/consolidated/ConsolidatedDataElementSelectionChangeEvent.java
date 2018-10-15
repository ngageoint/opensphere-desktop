package io.opensphere.mantle.data.element.event.consolidated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Class ConsolidatedDataElementSelectionChangeEvent.
 */
public class ConsolidatedDataElementSelectionChangeEvent extends AbstractConsolidatedDataElementChangeEvent
{
    /** The de-select set. */
    private final List<Long> myDeselects;

    /** The select set. */
    private final List<Long> mySelects;

    /**
     * Instantiates a new selection change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param selects the set of selected ids
     * @param deselects the set of deselected ids
     * @param source the source
     */
    public ConsolidatedDataElementSelectionChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, List<Long> selects,
            List<Long> deselects, Object source)
    {
        super(regIds, dataTypeKeys, source);
        mySelects = Collections.unmodifiableList(selects instanceof LinkedList<?> ? new ArrayList<>(selects) : selects);
        myDeselects = Collections
                .unmodifiableList(deselects instanceof LinkedList<?> ? new ArrayList<>(deselects) : deselects);
    }

    /**
     * Instantiates a new selection change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKey the data type key
     * @param selects the set of selected ids
     * @param deselects the set of deselected ids
     * @param source the source
     */
    public ConsolidatedDataElementSelectionChangeEvent(long regIds, String dataTypeKey, List<Long> selects, List<Long> deselects,
            Object source)
    {
        super(regIds, dataTypeKey, source);
        mySelects = Collections.unmodifiableList(selects instanceof LinkedList<?> ? new ArrayList<>(selects) : selects);
        myDeselects = Collections
                .unmodifiableList(deselects instanceof LinkedList<?> ? new ArrayList<>(deselects) : deselects);
    }

    /**
     * Instantiates a new selection change consolidated data element change
     * event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param selects the set of selected ids
     * @param deselects the set of deselected ids
     * @param source the source
     */
    public ConsolidatedDataElementSelectionChangeEvent(long[] regIds, Set<String> dataTypeKeys, List<Long> selects,
            List<Long> deselects, Object source)
    {
        super(regIds, dataTypeKeys, source);
        mySelects = Collections.unmodifiableList(selects instanceof LinkedList<?> ? new ArrayList<>(selects) : selects);
        myDeselects = Collections
                .unmodifiableList(deselects instanceof LinkedList<?> ? new ArrayList<>(deselects) : deselects);
    }

    /**
     * Gets the deselect set.
     *
     * @return the deselect set
     */
    public List<Long> getDeselectIdSet()
    {
        return myDeselects;
    }

    /**
     * Gets the select set.
     *
     * @return the select set
     */
    public List<Long> getSelectIdSet()
    {
        return mySelects;
    }
}
