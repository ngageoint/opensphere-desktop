package io.opensphere.core.datafilter;

import java.io.Serializable;
import java.util.Collection;

/**
 * This interface is used as a type that can contain any filter related objects
 * such as {@link DataFilterGroup} and {@link DataFilterCriteria} objects. It is
 * for convenience when using {@link Collection}s that may contain multiple
 * types of filter related classes.
 */
@FunctionalInterface
public interface DataFilterItem extends Serializable
{
    /**
     * The returned {@link String} will be an SQL-like string that can be used
     * to form a WFS request. The specifics of the format for this returned
     * value are left to the implementing class(es) to define.
     *
     * @return the string.
     */
    String getSqlLikeString();
}
