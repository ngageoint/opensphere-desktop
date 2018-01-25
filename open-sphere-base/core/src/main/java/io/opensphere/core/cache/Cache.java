package io.opensphere.core.cache;

import java.io.NotSerializableException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import gnu.trove.list.TIntList;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.matcher.IntervalPropertyMatcher;
import io.opensphere.core.cache.matcher.MultiPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.Satisfaction;

/**
 * Interface for a facility that allows persistent caching of arbitrary model
 * properties.
 */
public interface Cache
{
    /**
     * Get if the implementation supports a certain property descriptor.
     *
     * @param desc The descriptor.
     * @return {@code true} if the descriptor is supported.
     */
    boolean acceptsPropertyDescriptor(PropertyDescriptor<?> desc);

    /**
     * Remove all cached items.
     */
    void clear();

    /**
     * Clear cache entries by their data model categories.
     *
     * @param dmc The data model category, which may contain wildcards.
     * @param returnIds Flag indicating if the model ids should be returned. If
     *            {@code listener} is not {@code null} and this is {@code false}
     *            , the id array sent to the listener will be {@code null}.
     * @param listener Optional listener that will receive the removed values.
     *            The cache implementation may not send all values to the
     *            listener.
     * @throws CacheException If there's a problem accessing the cache.
     */
    void clear(DataModelCategory dmc, boolean returnIds, CacheRemovalListener listener) throws CacheException;

    /**
     * Clear cache entries by their primary keys. There is no error if any of
     * the cache entries do not exist.
     *
     * @param ids The object ids.
     * @throws CacheException If there's a problem accessing the cache.
     */
    void clear(long[] ids) throws CacheException;

    /**
     * Clear cache entries by their primary keys. There is no error if any of
     * the cache entries do not exist.
     *
     * @param ids The object ids.
     * @param listener Optional listener that will receive the removed values.
     *            The cache implementation may not send all values to the
     *            listener. The cache implementation may not populate the
     *            {@link DataModelCategory} in the listener call.
     * @throws CacheException If there's a problem accessing the cache.
     */
    void clear(long[] ids, CacheRemovalListener listener) throws CacheException;

    /**
     * Clear cache entries by their group ids. There is no error if any of the
     * cache entries do not exist.
     *
     * @param groupIds The group ids.
     * @throws CacheException If there's a problem accessing the cache.
     */
    void clearGroups(int[] groupIds) throws CacheException;

    /**
     * Close the cache, freeing any runtime resources.
     */
    void close();

    /**
     * Get the data model category for each of a set of models.
     *
     * @param ids The numeric ids of the models.
     * @return The data model categories.
     * @throws CacheException If there's a problem accessing the cache.
     */
    DataModelCategory[] getDataModelCategories(long[] ids) throws CacheException;

    /**
     * Get the data model categories for some groups. If {@code distinct} is
     * {@code true}, only distinct categories are returned, in indeterminate
     * order. If {@code distinct} if {@code false}, one category is returned for
     * each group, in the same order as the input array.
     *
     * @param groupIds The group ids.
     * @param source Flag indicating that the source of the data model
     *            categories needs to be populated.
     * @param family Flag indicating that the family of the data model
     *            categories needs to be populated.
     * @param category Flag indicating that the category of the data model
     *            categories needs to be populated.
     * @param distinct Flag indicating if only distinct categories should be
     *            returned.
     * @return The data model categories.
     * @throws CacheException If there's a problem accessing the cache.
     */
    List<DataModelCategory> getDataModelCategoriesByGroupId(int[] groupIds, boolean source, boolean family, boolean category,
            boolean distinct) throws CacheException;

    /**
     * Get the distinct data model categories for some models.
     *
     * @param ids The numeric ids of the models.
     * @param source Flag indicating that the source of the data model
     *            categories needs to be populated.
     * @param family Flag indicating that the family of the data model
     *            categories needs to be populated.
     * @param category Flag indicating that the category of the data model
     *            categories needs to be populated.
     * @return The data model categories.
     * @throws CacheException If there's a problem accessing the cache.
     */
    List<DataModelCategory> getDataModelCategoriesByModelId(long[] ids, boolean source, boolean family, boolean category)
        throws CacheException;

    /**
     * Get the group ids that match a data model category.
     *
     * @param category The data model category. Any <code>null</code> values are
     *            treated as wildcards..
     * @return The group ids.
     * @throws CacheException If there's a problem accessing the cache.
     */
    int[] getGroupIds(DataModelCategory category) throws CacheException;

    /**
     * Get the group ids for some element ids.
     *
     * @param ids The element ids.
     * @param distinct Indicates if only distinct group ids should be returned.
     * @return
     *         <ul>
     *         <li>If <code>distinct</code> is <code>false</code>, the group
     *         ids, one for each element. If there is no group, the value is set
     *         to <code>0</code>.</li>
     *         <li>If <code>distinct</code> is <code>true</code>, the distinct
     *         group ids.</li>
     *         </ul>
     * @throws CacheException If there's a problem accessing the cache.
     */
    int[] getGroupIds(long[] ids, boolean distinct) throws CacheException;

    /**
     * Get the primary keys that match the input parameters, using
     * {@link Satisfaction} objects returned from
     * {@link #getIntervalSatisfactions(DataModelCategory, Collection)}. Using
     * this method will likely be faster than using
     * {@link #getIds(Collection, Collection, List, int, int)}.
     *
     * @param satisfactions The satisfactions, as returned from
     *            {@link #getIntervalSatisfactions(DataModelCategory, Collection)}
     *            .
     * @param parameters An optional list of property matchers to limit the
     *            query.
     * @param orderSpecifiers Optional specifiers of how the ids should be
     *            ordered. Note that order specifiers will slow down the query.
     * @param startIndex The first row index to return, <code>0</code> being the
     *            first row.
     * @param limit A limit on the number of ids returned.
     * @return The array of ids.
     * @throws CacheException If there's a problem accessing the cache.
     * @throws NotSerializableException If one of the parameter values is not
     *             serializable.
     */
    long[] getIds(Collection<? extends Satisfaction> satisfactions, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
                throws CacheException, NotSerializableException;

    /**
     * Get the primary keys that match the input parameters.
     * <p>
     * Order is determined by the order specifiers if they are provided. If they
     * are not provided, order is determined by the first
     * {@link MultiPropertyMatcher} parameter if there is one. If there are no
     * order specifiers and no {@link MultiPropertyMatcher}s, order is
     * undefined.
     *
     * @param category The data model category. Any <code>null</code> values are
     *            treated as wildcards.
     * @param parameters An optional list of property matchers to limit the
     *            query.
     * @param orderSpecifiers Optional specifiers of how the ids should be
     *            ordered. Note that order specifiers will slow down the query.
     * @param startIndex The first row index to return, <code>0</code> being the
     *            first row.
     * @param limit A limit on the number of ids returned.
     * @return The array of ids.
     * @throws CacheException If there's a problem accessing the cache.
     * @throws NotSerializableException If one of the parameter values is not
     *             serializable.
     */
    long[] getIds(DataModelCategory category, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
                throws CacheException, NotSerializableException;

    /**
     * Get the primary keys that match the input parameters.
     * <p>
     * Order is determined by the order specifiers if they are provided. If they
     * are not provided, order is determined by the first
     * {@link MultiPropertyMatcher} parameter if there is one. If there are no
     * order specifiers and no {@link MultiPropertyMatcher}s, order is
     * undefined.
     *
     * @param groupIds The group ids.
     * @param parameters An optional list of property matchers to limit the
     *            query.
     * @param orderSpecifiers Optional specifiers of how the ids should be
     *            ordered. Note that order specifiers will slow down the query.
     * @param startIndex The first row index to return, <code>0</code> being the
     *            first row.
     * @param limit A limit on the number of ids returned.
     * @return The array of ids.
     * @throws CacheException If there's a problem accessing the cache.
     * @throws NotSerializableException If one of the parameter values is not
     *             serializable.
     */
    long[] getIds(int[] groupIds, Collection<? extends PropertyMatcher<?>> parameters,
            List<? extends OrderSpecifier> orderSpecifiers, int startIndex, int limit)
                throws NotSerializableException, CacheException;

    /**
     * Get the interval combinations that can be satisfied by this cache.
     * <p>
     * For example, if I want to get the satisfaction for the following interval
     * parameters:
     * <ul>
     * <li>Bounding box (0, 0, 10, 10)</li>
     * <li>Time (0:00, 1:00)</li>
     * </ul>
     * <br>
     * The results might look like the following:
     * <ul>
     * <li>Result 1
     * <ul>
     * <li>Bounding box (0, 0, 5, 5)</li>
     * <li>Time (0:00, 0:10)</li>
     * </ul>
     * </li>
     * <li>Result 2
     * <ul>
     * <li>Bounding box (5, 5, 10, 10)</li>
     * <li>Time (0:10, 0:20)</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param category The data model category.
     * @param parameters The parameters on the query.
     * @return A collection of satisfactions, one for each group.
     */
    Collection<? extends Satisfaction> getIntervalSatisfactions(DataModelCategory category,
            Collection<? extends IntervalPropertyMatcher<?>> parameters);

    /**
     * Extract property values from the cache using their models' primary keys.
     *
     * @param ids The object ids.
     * @param cacheResultMap A map of property descriptors to lists that are to
     *            contain the values for each property.
     * @param failedIndices Indices into the id array of elements that could not
     *            be retrieved. This may be <code>null</code>.
     * @throws CacheException If there's a problem accessing the cache.
     */
    void getValues(long[] ids, PropertyValueMap cacheResultMap, TIntList failedIndices) throws CacheException;

    /**
     * Get the sizes of some property values in the cache.
     *
     * @param ids The object ids.
     * @param desc The descriptor for the property.
     * @return The sizes, in bytes, in the same order as the input ids.
     * @throws CacheException If there's a problem accessing the cache.
     */
    long[] getValueSizes(long[] ids, PropertyDescriptor<?> desc) throws CacheException;

    /**
     * Initialize the cache. This may only be called once.
     *
     * @param millisecondsWait How many milliseconds to wait for the cache to
     *            initialize before returning. If this is 0, the method will
     *            return immediately. If this is negative, the method will block
     *            until the cache is initialized.
     * @throws CacheException If initialization fails.
     */
    void initialize(long millisecondsWait) throws CacheException;

    /**
     * Put model properties into the cache.
     *
     * @param <T> The type of the source objects in the deposit.
     * @param insert An object that provides the properties to be persisted in
     *            the cache. If this is a <i>new</i> insert, the data model
     *            category cannot contain <code>null</code>s.
     * @param listener Optional listener for cache modification reports.
     * @return The cache ids for the altered records. If the deposit contains
     *         more than one input object, the ids will be in the same order as
     *         the input objects.
     * @throws CacheException If there's a problem accessing the cache.
     * @throws NotSerializableException If a non-serializable property value is
     *             encountered.
     */
    <T> long[] put(final CacheDeposit<T> insert, CacheModificationListener listener)
        throws CacheException, NotSerializableException;

    /**
     * Set the class provider to be used for deserializing objects.
     *
     * @param provider A class provider to use when the system class loader
     *            fails.
     */
    void setClassProvider(ClassProvider provider);

    /**
     * Set the maximum size of the in-memory cache.
     *
     * @param bytes The number of bytes.
     * @throws CacheException If there's a problem setting the cache size.
     */
    void setInMemorySizeBytes(long bytes) throws CacheException;

    /**
     * Set the limit on the on-disk size of the database.
     *
     * @param bytes The number of bytes.
     */
    void setOnDiskSizeLimitBytes(long bytes);

    /**
     * Set model properties to new values.
     *
     * @param <T> The type of the input objects.
     * @param ids The ids for the elements being changed.
     * @param input The input objects. This must either be a singleton to
     *            indicate that all values are to be set to be the same, or it
     *            must be the same size as the array of ids, indicating that
     *            there is one value per id, using the iteration order of the
     *            collection.
     * @param accessors The property accessors.
     * @param executor Optional executor to use when updating values in the
     *            nested cache.
     * @param listener Optional listener for cache modification reports.
     * @throws CacheException If there's a problem accessing the cache.
     * @throws NotSerializableException If the property value cannot be
     *             serialized.
     */
    <T> void updateValues(long[] ids, Collection<? extends T> input,
            Collection<? extends PropertyAccessor<? super T, ?>> accessors, Executor executor, CacheModificationListener listener)
                throws CacheException, NotSerializableException;
}
