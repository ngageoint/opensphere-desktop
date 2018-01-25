package io.opensphere.mantle.data.cache.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import gnu.trove.list.TLongList;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.CacheQuery;
import io.opensphere.mantle.data.cache.DirectAccessRetriever;
import io.opensphere.mantle.data.dynmeta.impl.DynamicMetadataManagerImpl;

/**
 * The Interface CacheAssistant.
 */
public interface CacheAssistant
{
    /**
     * Cache element.
     *
     * @param source the source
     * @param category the category
     * @param id the id
     * @param type the type
     * @param ce the ce
     */
    void cacheElement(String source, String category, long id, DataTypeInfo type, CacheEntry ce);

    /**
     * Cache elements.
     *
     * @param source the source
     * @param category the category
     * @param ids the ids
     * @param type the type
     * @param ceList the ce list
     */
    @SuppressWarnings("PMD.LooseCoupling")
    void cacheElements(String source, String category, TLongList ids, DataTypeInfo type, LinkedList<CacheEntry> ceList);

    /**
     * Data type removed.
     *
     * @param dti the dti
     */
    void dataTypeRemoved(DataTypeInfo dti);

    /**
     * Gets the direct access retriever.
     *
     * @param dti the dti
     * @param cacheRefMap the cache ref map
     * @param dcm the dcm
     * @return the direct access retriever
     */
    DirectAccessRetriever getDirectAccessRetriever(DataTypeInfo dti, Map<Long, CacheEntry> cacheRefMap,
            DynamicMetadataManagerImpl dcm);

    /**
     * Gets the preferred insert block size for the assistant or -1 if no
     * preference.
     *
     * @return the preferred insert block size or -1 if no preference.
     */
    int getPreferredInsertBlockSize();

    /**
     * Removes the element.
     *
     * @param cacheId the cache id
     * @param ref the ref
     */
    void removeElement(long cacheId, CacheReference ref);

    /**
     * Removes the elements.
     *
     * @param cacheIds the cache ids
     * @param refs the refs
     */
    void removeElements(List<Long> cacheIds, List<CacheReference> refs);

    /**
     * Retrieve and update element cache entries.
     *
     * @param query the query
     * @param cacheIds the cache ids
     * @param entries the entries
     * @param updateEntries the update entries
     */
    void retrieveAndUpdateElementCacheEntries(CacheQuery query, List<Long> cacheIds, List<CacheEntry> entries,
            boolean updateEntries);
}
