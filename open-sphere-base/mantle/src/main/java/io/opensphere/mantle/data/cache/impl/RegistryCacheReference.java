package io.opensphere.mantle.data.cache.impl;

import io.opensphere.mantle.data.cache.CacheStoreType;

/**
 * The Class RegistryCacheReference.
 */
public class RegistryCacheReference extends CacheReference
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The my registry id. */
    private long myRegistryId;

    /**
     * Instantiates a new registry cache reference.
     */
    public RegistryCacheReference()
    {
    }

    /**
     * Instantiates a new registry cache reference.
     *
     * @param id the id
     */
    public RegistryCacheReference(long id)
    {
        myRegistryId = id;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj) || getClass() != obj.getClass())
        {
            return false;
        }
        RegistryCacheReference other = (RegistryCacheReference)obj;
        return myRegistryId == other.myRegistryId;
    }

    /**
     * Gets the registry id.
     *
     * @return the registry id
     */
    public long getRegistryId()
    {
        return myRegistryId;
    }

    @Override
    public CacheStoreType getType()
    {
        return CacheStoreType.REGISTRY;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int)(myRegistryId ^ myRegistryId >>> 32);
        return result;
    }

    /**
     * Sets the registry id.
     *
     * @param id the new registry id
     */
    public void setRegistryId(long id)
    {
        myRegistryId = id;
    }
}
