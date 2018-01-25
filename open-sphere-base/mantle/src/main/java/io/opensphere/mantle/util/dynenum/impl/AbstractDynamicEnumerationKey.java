package io.opensphere.mantle.util.dynenum.impl;

import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;

/**
 * The Class AbstractDynamicEnumerationKey.
 */
public abstract class AbstractDynamicEnumerationKey implements DynamicEnumerationKey
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new abstract dynamic enumeration key.
     */
    public AbstractDynamicEnumerationKey()
    {
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(getClass().getSimpleName()).append(" TypeId[").append(getTypeId()).append("] KeyId[").append(getMetaDataKeyId())
                .append("] ValId[").append(getValueId()).append(']');
        return sb.toString();
    }
}
