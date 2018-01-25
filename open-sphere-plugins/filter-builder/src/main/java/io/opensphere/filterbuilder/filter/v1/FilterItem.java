package io.opensphere.filterbuilder.filter.v1;

import java.util.Collection;

import io.opensphere.core.datafilter.DataFilterItem;
import io.opensphere.core.util.lang.ExpectedCloneableException;

/**
 * This interface is used as a type that can contain any filter related objects
 * such as {@link Group} and {@link Criteria} objects. It is for convenience
 * when using {@link Collection}s that may contain multiple types of filter
 * related classes.
 */
public abstract class FilterItem implements DataFilterItem, Cloneable
{
    /** The serial version ID. */
    private static final long serialVersionUID = 1L;

    @Override
    public FilterItem clone()
    {
        try
        {
            return (FilterItem)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }
    }
}
