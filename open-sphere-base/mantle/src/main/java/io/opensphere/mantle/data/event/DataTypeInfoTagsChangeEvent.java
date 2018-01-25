package io.opensphere.mantle.data.event;

import java.util.Collections;
import java.util.Set;

import io.opensphere.mantle.data.AbstractDataTypeInfoChangeEvent;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Events from changes to DataTypeInfo tags.
 */
public class DataTypeInfoTagsChangeEvent extends AbstractDataTypeInfoChangeEvent
{
    /**
     * Event CTOR.
     *
     * @param dti - the {@link DataTypeInfo}
     * @param tagSet the tag set that changed
     * @param source - the source of the event.
     */
    public DataTypeInfoTagsChangeEvent(DataTypeInfo dti, Set<String> tagSet, Object source)
    {
        super(dti, Type.TAGS_CHANGED, tagSet == null ? Collections.<String>emptySet() : tagSet, source);
    }

    /**
     * Gets the changed tag set.
     *
     * @return the tags.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getTags()
    {
        return (Set<String>)getValue();
    }
}
