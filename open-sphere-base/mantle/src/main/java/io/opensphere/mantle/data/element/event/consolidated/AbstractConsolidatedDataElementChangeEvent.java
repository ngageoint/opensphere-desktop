package io.opensphere.mantle.data.element.event.consolidated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.lang.ToStringHelper;

/**
 * The Class AbstractConsolidatedDataElementChangeEvent.
 */
public abstract class AbstractConsolidatedDataElementChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The data type key set. */
    private final Set<String> myDataTypeKeySet;

    /** The data element id set. */
    private final List<Long> myIdSet;

    /** The source. */
    private final Object mySource;

    /**
     * Instantiates a new abstract consolidated data element change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param source the source
     */
    public AbstractConsolidatedDataElementChangeEvent(List<Long> regIds, Set<String> dataTypeKeys, Object source)
    {
        mySource = source;
        myIdSet = Collections.unmodifiableList(regIds instanceof LinkedList<?> ? new ArrayList<>(regIds) : regIds);
        myDataTypeKeySet = Collections.unmodifiableSet(dataTypeKeys == null ? new HashSet<String>() : dataTypeKeys);
    }

    /**
     * Instantiates a new abstract consolidated data element change event.
     *
     * @param regId the registry id for the data element
     * @param dataTypeKey the data type key
     * @param source the source
     */
    public AbstractConsolidatedDataElementChangeEvent(long regId, String dataTypeKey, Object source)
    {
        this(Collections.singletonList(Long.valueOf(regId)), Collections.singleton(dataTypeKey), source);
    }

    /**
     * Instantiates a new abstract consolidated data element change event.
     *
     * @param regIds the registry ids for the data elements
     * @param dataTypeKeys the data type keys
     * @param source the source
     */
    public AbstractConsolidatedDataElementChangeEvent(long[] regIds, Set<String> dataTypeKeys, Object source)
    {
        this(CollectionUtilities.listView(regIds), dataTypeKeys, source);
    }

    /**
     * Gets the data type keys. Note: This is an unmodifiable set.
     *
     * See: {@link Collections}.unmodifiableSet for more details.
     *
     * @return the data type keys
     */
    public Set<String> getDataTypeKeys()
    {
        return myDataTypeKeySet;
    }

    @Override
    public String getDescription()
    {
        return getClass().getName();
    }

    /**
     * Gets the registry ids.
     *
     * @return the registry ids
     */
    public List<Long> getRegistryIds()
    {
        return myIdSet;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("from", getSource() == null ? "NULL" : getSource().getClass().getSimpleName());
        helper.add("idCount", myIdSet.size());
        return helper.toString();
    }
}
