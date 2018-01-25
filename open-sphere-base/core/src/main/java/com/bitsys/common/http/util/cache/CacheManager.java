package com.bitsys.common.http.util.cache;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class manages a cache.
 */
public class CacheManager
{
   /** The cache. */
   @SuppressWarnings("rawtypes")
   private final Map<Object, CachedValue> cache = Collections
      .synchronizedMap(new HashMap<Object, CachedValue>());

   /**
    * Indicates if the cache contains a value for the given key.
    *
    * @param key
    *           the key into the cache.
    * @return <code>true</code> if there is a value associated with the given
    *         key.
    */
   public boolean contains(final Object key)
   {
      return cache.containsKey(key);
   }

   /**
    * Inserts a new value into the cache. If the key was previously associated
    * with a value, it is returned.
    *
    * @param key
    *           the key into the cache.
    * @param value
    *           the value to cache.
    * @return the value previously associated with the given key in cache or
    *         <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public <T> T put(final Object key, final T value)
   {
      final CachedValue<T> previousValue = cache.put(key, new CachedValue<>(value));
      return previousValue == null ? null : previousValue.getValue();
   }

   /**
    * Fetches the value from cache. Returns <code>null</code> if the key is not
    * associated with a value in cache or the cached value is <code>null</code>.
    *
    * @param key
    *           the key into the cache.
    * @return the cached value or <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public <T> T get(final Object key)
   {
      final CachedValue<T> value = cache.get(key);
      return value == null ? null : value.getValue();
   }

   /**
    * Clears any cached items since the given date. Data before this date will
    * be preserved. If the parameter is <code>null</code> then the entire cache
    * should be cleared.
    *
    * @param since
    *           the starting date when the cache will be cleared.
    */
   @SuppressWarnings("rawtypes")
   public void clearCache(final Date since)
   {
      if (since == null)
      {
         cache.clear();
      }
      else
      {
         synchronized (cache)
         {
            final Iterator<CachedValue> iterator = cache.values().iterator();
            while (iterator.hasNext())
            {
               final CachedValue entry = iterator.next();
               if (!entry.getCacheDate().before(since))
               {
                  iterator.remove();
               }
            }
         }
      }
   }
}
