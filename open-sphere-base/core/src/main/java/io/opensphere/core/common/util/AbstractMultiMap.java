package io.opensphere.core.common.util;

import java.io.Serializable;

/**
 * An abstract base classes for multi-value map implementations.
 *
 * @param <KEY_TYPE>
 * @param <VALUE_TYPE>
 */
public abstract class AbstractMultiMap<KEY_TYPE, VALUE_TYPE> implements MultiMap<KEY_TYPE, VALUE_TYPE>, Serializable, Cloneable
{
    /**
     * The unique identifier used for serialization operations.
     */
    private static final long serialVersionUID = -5911724821218237623L;

    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractMultiMap()
    {
        /* intentionally blank */
    }
}
