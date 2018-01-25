package com.bitsys.common.http.util.cache;

import java.util.Date;

/**
 * This class represents a cache entry. It contains the cached value as well
 * as the time at which the value was cached.
 *
 * @param <T>
 *           the type of value cached.
 */
public class CachedValue<T>
{
   /** The cached value. */
   private final T value;

   /** The date when the result was cached. */
   private final Date cacheDate;

   /**
    * Constructs a new {@linkplain CachedValue} using the given result. The
    * cache date will be initialized to right now.
    *
    * @param value
    *           the cached value.
    */
   public CachedValue(final T value)
   {
      this(value, new Date());
   }

   /**
    * Constructs a new {@linkplain CachedValue} using the given result.
    *
    * @param value
    *           the cached value.
    * @param cacheDate
    *           the date when the value was cached.
    */
   public CachedValue(final T value, final Date cacheDate)
   {
      if (cacheDate == null)
      {
         throw new IllegalArgumentException("The cache date is null");
      }
      this.value = value;
      this.cacheDate = cacheDate;
   }

   /**
    * Returns the cached value.
    *
    * @return the cached value.
    */
   public T getValue()
   {
      return value;
   }

   /**
    * Returns the date when the value was cached.
    *
    * @return the date when the value was cached.
    */
   public Date getCacheDate()
   {
      return cacheDate;
   }
}
