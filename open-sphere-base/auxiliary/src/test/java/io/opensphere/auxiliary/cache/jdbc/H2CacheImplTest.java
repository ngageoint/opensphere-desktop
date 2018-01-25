package io.opensphere.auxiliary.cache.jdbc;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.cache.CacheDeposit;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.CacheModificationReport;
import io.opensphere.core.cache.DefaultCacheDeposit;
import io.opensphere.core.cache.DefaultCacheModificationListener;
import io.opensphere.core.cache.PropertyValueMap;
import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyAccessor;
import io.opensphere.core.cache.accessor.PropertyArrayAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.matcher.GeometryMatcher;
import io.opensphere.core.cache.matcher.MultiPropertyMatcher;
import io.opensphere.core.cache.matcher.PropertyMatcher;
import io.opensphere.core.cache.matcher.StringPropertyMatcher;
import io.opensphere.core.cache.util.PropertyArrayDescriptor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.DefaultOrderSpecifier;
import io.opensphere.core.data.util.OrderSpecifier;
import io.opensphere.core.data.util.OrderSpecifier.Order;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Nulls;
import org.junit.Assert;

/**
 * Test for {@link H2CacheImpl}.
 */
@SuppressWarnings("PMD.GodClass")
public class H2CacheImplTest
{
    /** Test category name. */
    private static final String CATEGORY1 = "category1";

    /** A second test category. */
    private static final String CATEGORY2 = "category2";

    /** Column name. */
    private static final String COLUMN_PREFIX = "column";

    /** The URL for the in-memory database. */
    private static final String DB_URL = "mem:test";

    /** Test family name. */
    private static final String FAMILY1 = "family1";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(H2CacheImplTest.class);

    /** Property array property name. */
    private static final String PROPERTY_ARRAY_PROPERTY = "propertyArray";

    /** Test source name. */
    private static final String SOURCE1 = "source1";

    /**
     * Test that two inserts can be completed concurrently.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is a database error.
     * @throws NotSerializableException If an object cannot be serialized.
     * @throws InterruptedException If interrupted.
     * @throws ExecutionException If another exception was encountered.
     */
    @Test
    public void testConcurrentInsert()
        throws ClassNotFoundException, CacheException, NotSerializableException, InterruptedException, ExecutionException
    {
        final H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        final Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        final Object obj1 = new Object();
        final Object obj2 = new Object();

        final AtomicBoolean gate1 = new AtomicBoolean();
        final AtomicBoolean gate2 = new AtomicBoolean();

        PropertyDescriptor<Serializable> desc = new PropertyDescriptor<>("prop", Serializable.class);
        final SerializableAccessor<Object, Serializable> acc = new SerializableAccessor<Object, Serializable>(desc)
        {
            @Override
            public Serializable access(Object input)
            {
                if (Utilities.sameInstance(input, obj1))
                {
                    synchronized (H2CacheImplTest.this)
                    {
                        gate1.set(true);
                        H2CacheImplTest.this.notifyAll();
                    }
                    synchronized (H2CacheImplTest.this)
                    {
                        try
                        {
                            while (!gate2.get())
                            {
                                H2CacheImplTest.this.wait(1000L);
                            }
                        }
                        catch (InterruptedException e)
                        {
                            Assert.fail("Test was interrupted.");
                        }
                    }
                }
                return "";
            }
        };

        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        Future<long[]> future1 = threadPool.submit(new Callable<long[]>()
        {
            @Override
            public long[] call() throws CacheException, NotSerializableException
            {
                Collection<Object> objects1 = Collections.singleton(obj1);
                DataModelCategory category = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY1);
                CacheDeposit<Object> insert1 = new DefaultCacheDeposit<Object>(category, Collections.singleton(acc), objects1,
                        true, expiration, true);
                return cache.put(insert1, (CacheModificationListener)null);
            }
        });

        // Wait for the first insert to get started.
        synchronized (this)
        {
            while (!gate1.get())
            {
                wait();
            }
        }

        Collection<Object> objects = Collections.singleton(obj2);
        DataModelCategory category = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY2);
        CacheDeposit<Object> insert = new DefaultCacheDeposit<>(category, Collections.singleton(acc), objects, true, expiration,
                true);
        cache.put(insert, (CacheModificationListener)null);

        synchronized (this)
        {
            gate2.set(true);
            notifyAll();
        }

        future1.get();

        threadPool.shutdown();
        cache.close();
    }

    /**
     * Test retrieval of data model categories.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is another database error.
     * @throws NotSerializableException If an object cannot be serialized.
     */
    @Test
    public void testGetDataModelCategory() throws ClassNotFoundException, CacheException, NotSerializableException
    {
        H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        int objectCount = 5000;
        List<TestObject> objects1 = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; ++i)
        {
            objects1.add(new TestObject(Integer.toString(i), null, null));
        }
        DataModelCategory category1 = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY1);

        CacheDeposit<TestObject> insert1 = new DefaultCacheDeposit<TestObject>(category1,
                Nulls.<PropertyAccessor<TestObject, ?>>collection(), objects1, true, expiration, true);
        long[] ids1 = cache.put(insert1, (CacheModificationListener)null);

        List<TestObject> objects2 = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; ++i)
        {
            objects2.add(new TestObject(Integer.toString(i), null, null));
        }
        DataModelCategory category2 = new DataModelCategory("source2", FAMILY1, CATEGORY1);

        CacheDeposit<TestObject> insert2 = new DefaultCacheDeposit<TestObject>(category2,
                Nulls.<PropertyAccessor<TestObject, ?>>collection(), objects2, true, expiration, true);
        long[] ids2 = cache.put(insert2, (CacheModificationListener)null);

        List<TestObject> objects3 = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; ++i)
        {
            objects3.add(new TestObject(Integer.toString(i), null, null));
        }
        DataModelCategory category3 = new DataModelCategory("source2", FAMILY1, CATEGORY2);

        CacheDeposit<TestObject> insert3 = new DefaultCacheDeposit<TestObject>(category3,
                Nulls.<PropertyAccessor<TestObject, ?>>collection(), objects3, true, expiration, true);
        long[] ids3 = cache.put(insert3, (CacheModificationListener)null);

        long[] mixed = new long[ids1.length + ids2.length + ids3.length];
        for (int i = 0; i < ids1.length; i++)
        {
            mixed[i * 3] = ids1[i];
            mixed[i * 3 + 1] = ids2[i];
            mixed[i * 3 + 2] = ids3[i];
        }

        List<DataModelCategory> cats1 = cache.getDataModelCategoriesByModelId(Arrays.copyOf(mixed, 400), true, true, true);
        Assert.assertEquals(3, cats1.size());
        Assert.assertTrue(cats1.contains(category1));
        Assert.assertTrue(cats1.contains(category2));
        Assert.assertTrue(cats1.contains(category3));

        List<DataModelCategory> cats2 = cache.getDataModelCategoriesByModelId(mixed, false, true, true);
        Assert.assertEquals(2, cats2.size());
        Assert.assertTrue(cats2.contains(new DataModelCategory(null, FAMILY1, CATEGORY1)));
        Assert.assertTrue(cats2.contains(new DataModelCategory(null, FAMILY1, CATEGORY2)));

        List<DataModelCategory> cats3 = cache.getDataModelCategoriesByModelId(mixed, false, true, false);
        Assert.assertEquals(1, cats3.size());
        Assert.assertTrue(cats3.contains(new DataModelCategory(null, FAMILY1, null)));

        DataModelCategory[] cats4 = cache.getDataModelCategories(mixed);
        Assert.assertEquals(mixed.length, cats4.length);
        for (int i = 0; i < cats4.length;)
        {
            Assert.assertEquals(category1, cats4[i++]);
            Assert.assertEquals(category2, cats4[i++]);
            Assert.assertEquals(category3, cats4[i++]);
        }

        cache.close();
    }

    /**
     * Test inserting and retrieving string keys.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is another database error.
     * @throws NotSerializableException If an object cannot be serialized.
     */
    @Test
    public void testKeyRetrieval() throws ClassNotFoundException, CacheException, NotSerializableException
    {
        H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        PropertyDescriptor<String> propertyDescriptor = new PropertyDescriptor<>("key", String.class);

        int categoryCount = 10;
        int objectCount = 50000;
        List<List<String>> objectListList = new ArrayList<>(categoryCount);
        List<DataModelCategory> categoryList = new ArrayList<>(categoryCount);
        for (int j = 0; j < categoryCount; j++)
        {
            DataModelCategory category = new DataModelCategory("source" + j, FAMILY1, CATEGORY1);
            categoryList.add(category);

            List<String> objects = new ArrayList<>(objectCount);
            objectListList.add(objects);
            for (int i = 0; i < objectCount; ++i)
            {
                objects.add("A" + Integer.toString(i));
            }

            Collection<? extends PropertyAccessor<? super String, ?>> accessors = Collections
                    .singleton(SerializableAccessor.getHomogeneousAccessor(propertyDescriptor));
            CacheDeposit<String> insert = new DefaultCacheDeposit<>(category, accessors, objects, true, expiration, true);
            long t0 = System.nanoTime();
            long[] ids = cache.put(insert, (CacheModificationListener)null);
            long t1 = System.nanoTime();
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace("Time to put " + objectCount + " objects into cache: " + (t1 - t0) / 1e9 + "s");
            }

            Assert.assertEquals(objectCount, ids.length);
        }

        Random rand = new Random();
        int numberToRetrieve = 100;
        long getIdTime = 0L;
        long getValueTime = 0L;
        PropertyValueMap cacheResultMap = new PropertyValueMap();
        ArrayList<String> results = new ArrayList<>(1);
        cacheResultMap.addResultList(propertyDescriptor, results);
        for (int i = 0; i < numberToRetrieve; ++i)
        {
            int selectedCategoryIndex = rand.nextInt(categoryCount);
            int selectedObjectIndex = rand.nextInt(objectCount);
            String key = objectListList.get(selectedCategoryIndex).get(selectedObjectIndex);
            List<? extends PropertyMatcher<?>> parameters = Collections
                    .singletonList(new StringPropertyMatcher(propertyDescriptor.getPropertyName(), key));
            getIdTime -= System.nanoTime();
            long[] ids2 = cache.getIds(categoryList.get(selectedCategoryIndex), parameters, Nulls.<OrderSpecifier>list(), 0,
                    Integer.MAX_VALUE);
            getIdTime += System.nanoTime();
            Assert.assertEquals(1, ids2.length);

            results.clear();
            getValueTime -= System.nanoTime();
            cache.getValues(ids2, cacheResultMap, null);
            getValueTime += System.nanoTime();
            Assert.assertEquals(1, results.size());
            Assert.assertEquals(key, results.get(0));
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Time to get " + numberToRetrieve + " ids from cache: " + getIdTime / 1e9 + "s");
        }
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Time to get " + numberToRetrieve + " values from cache: " + getValueTime / 1e9 + "s");
        }

        cache.close();
    }

    /**
     * Test operations with a property array.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is another database error.
     * @throws NotSerializableException If an object cannot be serialized.
     */
    @Test
    public void testPropertyArray() throws ClassNotFoundException, CacheException, NotSerializableException
    {
        H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        DataModelCategory category = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY1);
        Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        int objectCount = 5001;
        int stringCount = 10;
        List<TestObject> objects = createTestObjects(objectCount, stringCount);

        Collection<PropertyAccessor<TestObject, ?>> accessors = new ArrayList<>();
        List<String> columnNames = new ArrayList<>(stringCount);
        for (int i = 0; i < stringCount; ++i)
        {
            columnNames.add(COLUMN_PREFIX + i);
        }
        Class<?>[] columnTypes = new Class<?>[stringCount];
        Arrays.fill(columnTypes, String.class);
        final int[] activeColumns = { 0, 2, 4, 6, 8 };
        int orderByColumn = 2;
        PropertyArrayDescriptor propertyArrayDescriptor = new PropertyArrayDescriptor(PROPERTY_ARRAY_PROPERTY, columnTypes,
                activeColumns, orderByColumn);
        accessors.add(new PropertyArrayAccessor<TestObject>(propertyArrayDescriptor)
        {
            @Override
            public Object[] access(TestObject input)
            {
                Object[] results = new Object[activeColumns.length];
                Object[] array = input.getStrings().toArray();
                for (int i = 0; i < results.length; i++)
                {
                    results[i] = array[activeColumns[i]];
                }
                return results;
            }
        });
        CacheDeposit<TestObject> insert = new DefaultCacheDeposit<>(category, accessors, objects, true, expiration, true);
        long t0 = System.nanoTime();
        DefaultCacheModificationListener listener = new DefaultCacheModificationListener();
        long[] ids = cache.put(insert, listener);
        long t1 = System.nanoTime();
        Assert.assertEquals(1, listener.getReports().size());
        Assert.assertEquals(objectCount, ids.length);
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Time to put " + objectCount + " objects into cache: " + (t1 - t0) / 1e9 + "s");
        }

        List<? extends OrderSpecifier> orderSpecifiers = Collections
                .singletonList(new DefaultOrderSpecifier(Order.ASCENDING, propertyArrayDescriptor));
        long[] resultIds = cache.getIds(category, null, orderSpecifiers, 0, Integer.MAX_VALUE);
        Assert.assertEquals(ids.length, resultIds.length);

        PropertyValueMap cacheResultMap = new PropertyValueMap();
        List<Object[]> arrayResults = new ArrayList<>(ids.length);
        cacheResultMap.addResultList(propertyArrayDescriptor, arrayResults);
        cache.getValues(resultIds, cacheResultMap, null);

        Assert.assertEquals(resultIds.length, arrayResults.size());
        String lastOrderByColumnValue = null;
        for (Object[] arr : arrayResults)
        {
            Assert.assertEquals(activeColumns.length, arr.length);
            for (int i = 0; i < arr.length; ++i)
            {
                Assert.assertTrue(arr[i].toString().startsWith(Integer.toString(i * 2)));
            }
            String orderByColumnValue = arr[orderByColumn / 2].toString();
            if (lastOrderByColumnValue != null)
            {
                Assert.assertTrue("Values not in ascending order: " + lastOrderByColumnValue + " and " + orderByColumnValue,
                        lastOrderByColumnValue.compareTo(orderByColumnValue) <= 0);
            }
            lastOrderByColumnValue = orderByColumnValue;
        }

        orderSpecifiers = Collections.singletonList(new DefaultOrderSpecifier(Order.DESCENDING, propertyArrayDescriptor));
        resultIds = cache.getIds(category, null, orderSpecifiers, 0, Integer.MAX_VALUE);
        Assert.assertEquals(ids.length, resultIds.length);

        cacheResultMap = new PropertyValueMap();
        arrayResults = new ArrayList<>(ids.length);
        cacheResultMap.addResultList(propertyArrayDescriptor, arrayResults);
        cache.getValues(resultIds, cacheResultMap, null);

        Assert.assertEquals(resultIds.length, arrayResults.size());
        lastOrderByColumnValue = null;
        for (Object[] arr : arrayResults)
        {
            Assert.assertEquals(activeColumns.length, arr.length);
            for (int i = 0; i < arr.length; ++i)
            {
                Assert.assertTrue(arr[i].toString().startsWith(Integer.toString(i * 2)));
            }
            String orderByColumnValue = arr[orderByColumn / 2].toString();
            if (lastOrderByColumnValue != null)
            {
                Assert.assertTrue("Values not in descending order: " + lastOrderByColumnValue + " and " + orderByColumnValue,
                        lastOrderByColumnValue.compareTo(orderByColumnValue) >= 0);
            }
            lastOrderByColumnValue = orderByColumnValue;
        }

        cache.clear(ids);
        cache.close();
    }

    /**
     * Test updating a property array.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is another database error.
     * @throws NotSerializableException If an object cannot be serialized.
     */
    @Test
    public void testPropertyArrayUpdate() throws ClassNotFoundException, CacheException, NotSerializableException
    {
        H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        DataModelCategory category = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY1);
        Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        int objectCount = 5000;
        int stringCount = 10;
        List<TestObject> objects = createTestObjects(objectCount, stringCount);

        Collection<PropertyAccessor<TestObject, ?>> accessors = new ArrayList<>();
        List<String> columnNames = new ArrayList<>(stringCount);
        for (int i = 0; i < stringCount; ++i)
        {
            columnNames.add(COLUMN_PREFIX + i);
        }
        Class<?>[] columnTypes = new Class<?>[stringCount];
        Arrays.fill(columnTypes, String.class);
        final int[] activeColumns = { 0, 2, 4, 6, 8 };
        PropertyArrayDescriptor propertyArrayDescriptor = new PropertyArrayDescriptor(PROPERTY_ARRAY_PROPERTY, columnTypes,
                activeColumns);
        accessors.add(new PropertyArrayAccessor<TestObject>(propertyArrayDescriptor)
        {
            @Override
            public Object[] access(TestObject input)
            {
                Object[] results = new Object[activeColumns.length];
                Object[] array = input.getStrings().toArray();
                for (int i = 0; i < results.length; i++)
                {
                    results[i] = array[activeColumns[i]];
                }
                return results;
            }
        });
        CacheDeposit<TestObject> insert = new DefaultCacheDeposit<>(category, accessors, objects, true, expiration, true);
        long[] ids = cache.put(insert, (CacheModificationListener)null);
        Assert.assertEquals(objectCount, ids.length);

        // Add two new columns.
        Collection<PropertyAccessor<TestObject, ?>> accessors2 = new ArrayList<>();
        columnNames.add(COLUMN_PREFIX + stringCount++);
        columnNames.add(COLUMN_PREFIX + stringCount++);

        columnTypes = Arrays.copyOf(columnTypes, stringCount);
        columnTypes[stringCount - 2] = Integer.class;
        columnTypes[stringCount - 1] = Double.class;
        int[] activeColumns2 = { stringCount - 2, stringCount - 1 };
        final PropertyArrayDescriptor propertyArrayDescriptor2 = new PropertyArrayDescriptor(PROPERTY_ARRAY_PROPERTY, columnTypes,
                activeColumns2);
        accessors2.add(new PropertyArrayAccessor<TestObject>(propertyArrayDescriptor2)
        {
            @Override
            public Object[] access(TestObject input)
            {
                Object[] result = new Object[2];
                result[0] = Integer.valueOf(3);
                result[1] = Double.valueOf(4.5);
                return result;
            }
        });
        insert = new DefaultCacheDeposit<>(category, accessors2, objects, false, expiration, true);
        ids = cache.put(insert, (CacheModificationListener)null);
        Assert.assertEquals(objectCount, ids.length);

        // Now query all the columns.
        int[] activeColumns3 = New.sequentialIntArray(0, stringCount);
        PropertyArrayDescriptor propertyArrayDescriptor3 = new PropertyArrayDescriptor(PROPERTY_ARRAY_PROPERTY, columnTypes,
                activeColumns3);

        PropertyValueMap cacheResultMap = new PropertyValueMap();
        List<Object[]> arrayResults = new ArrayList<>(ids.length);
        cacheResultMap.addResultList(propertyArrayDescriptor3, arrayResults);
        cache.getValues(ids, cacheResultMap, null);

        for (Object[] arr : arrayResults)
        {
            Assert.assertEquals(activeColumns3.length, arr.length);
            for (int i = 0; i < arr.length; ++i)
            {
                if (i < arr.length - 2)
                {
                    if (i % 2 == 0)
                    {
                        Assert.assertTrue(arr[i] instanceof String);
                        Assert.assertTrue(((String)arr[i]).startsWith(Integer.toString(i)));
                    }
                    else
                    {
                        Assert.assertNull(arr[i]);
                    }
                }
                else if (i == arr.length - 2)
                {
                    Assert.assertTrue(arr[i] instanceof Integer);
                    Assert.assertEquals(3, ((Integer)arr[i]).intValue());
                }
                else if (i == arr.length - 1)
                {
                    Assert.assertTrue(arr[i] instanceof Double);
                    Assert.assertEquals(4.5, ((Double)arr[i]).doubleValue(), 0.);
                }
            }
        }

        cache.clear(ids);
        cache.close();
    }

    /**
     * Test performing a spatial query.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is another database error.
     * @throws NotSerializableException If an object cannot be serialized.
     */
    @Test
    public void testSpatialQuery() throws ClassNotFoundException, CacheException, NotSerializableException
    {
        H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        DataModelCategory category = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY1);
        Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        // Create a bunch of objects at random locations around the world.
        int objectCount = 5000;
        int stringCount = 10;
        List<TestObject> objects = new ArrayList<>(objectCount);
        final GeometryFactory factory = new GeometryFactory();
        Random random = new Random();
        for (int i = 0; i < objectCount; ++i)
        {
            ArrayList<String> strings = new ArrayList<>(stringCount);
            for (int j = 0; j < stringCount; j++)
            {
                strings.add(Double.toString(random.nextInt()));
            }
            Geometry geom = factory
                    .createPoint(new Coordinate(random.nextDouble() * 360. - 180., random.nextDouble() * 180. - 90.));
            objects.add(new TestObject(Integer.toString(i), geom, strings));
        }

        long t0 = System.nanoTime();
        Collection<PropertyAccessor<TestObject, ?>> accessors = new ArrayList<>();
        accessors.add(new GeometryAccessor<H2CacheImplTest.TestObject>(factory.toGeometry(new Envelope(-180., 180., -90., 90.)))
        {
            @Override
            public Geometry access(TestObject input)
            {
                return input.getGeometry();
            }
        });
        List<String> columnNames = new ArrayList<>(stringCount);
        for (int i = 0; i < stringCount; ++i)
        {
            columnNames.add(COLUMN_PREFIX + i);
        }
        Class<?>[] columnTypes = new Class<?>[stringCount];
        Arrays.fill(columnTypes, String.class);
        int[] activeColumns = { 0, 1, 2, 3, 4 };
        PropertyArrayDescriptor propertyArrayDescriptor = new PropertyArrayDescriptor(PROPERTY_ARRAY_PROPERTY, columnTypes,
                activeColumns);
        accessors.add(new PropertyArrayAccessor<TestObject>(propertyArrayDescriptor)
        {
            @Override
            public Object[] access(TestObject input)
            {
                return Arrays.copyOf(input.getStrings().toArray(), 5);
            }
        });
        CacheDeposit<TestObject> insert = new DefaultCacheDeposit<>(category, accessors, objects, true, expiration, true);
        long[] ids = cache.put(insert, (CacheModificationListener)null);
        long t1 = System.nanoTime();
        Assert.assertEquals(objectCount, ids.length);
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Time to put " + objectCount + " objects into cache: " + (t1 - t0) / 1e9 + "s");
        }

        Geometry bounds = factory.toGeometry(new Envelope(-20., 20., -20., 20.));
        long t2 = System.nanoTime();
        List<GeometryMatcher> parameters = Collections.singletonList(
                new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME, GeometryMatcher.OperatorType.INTERSECTS, bounds));
        long[] resultIds = cache.getIds(category, parameters, Nulls.<OrderSpecifier>list(), 0, Integer.MAX_VALUE);
        long t3 = System.nanoTime();
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Time to get " + resultIds.length + " ids from cache: " + (t3 - t2) / 1e9 + "s");
        }

        PropertyValueMap cacheResultMap = new PropertyValueMap();
        ArrayList<Geometry> geometryResults = new ArrayList<>(ids.length);
        cacheResultMap.addResultList(new PropertyDescriptor<Geometry>(GeometryAccessor.GEOMETRY_PROPERTY_NAME, Geometry.class),
                geometryResults);
        List<Object[]> arrayResults = new ArrayList<>(ids.length);
        cacheResultMap.addResultList(propertyArrayDescriptor, arrayResults);
        cache.getValues(resultIds, cacheResultMap, null);

        Assert.assertEquals(resultIds.length, geometryResults.size());
        for (Geometry result : geometryResults)
        {
            Assert.assertTrue("Bounds do not contain result geometry: " + result, bounds.contains(result));
        }
        Assert.assertEquals(resultIds.length, arrayResults.size());
        for (Object[] arr : arrayResults)
        {
            Assert.assertEquals(activeColumns.length, arr.length);
        }

        // Test the equals operator.
        TestObject firstObject = objects.get(0);
        List<GeometryMatcher> parameters2 = Collections.singletonList(new GeometryMatcher(GeometryAccessor.GEOMETRY_PROPERTY_NAME,
                GeometryMatcher.OperatorType.EQUALS, firstObject.getGeometry()));
        long[] resultIds2 = cache.getIds(category, parameters2, Nulls.<OrderSpecifier>list(), 0, Integer.MAX_VALUE);
        Assert.assertTrue(resultIds2.length > 0);
        for (long id : resultIds2)
        {
            int ix = Arrays.binarySearch(ids, id);
            Assert.assertEquals(objects.get(ix).getGeometry(), firstObject.getGeometry());
        }

        // Test the not-equals operator.
//        List<GeometryMatcher> parameters3 = Collections.singletonList(new GeometryMatcher(
//                GeometryAccessor.GEOMETRY_PROPERTY_NAME, GeometryMatcher.OperatorType.NE, firstObject.getGeometry()));
//        long[] resultIds3 = cache.getIds(category, parameters3, Nulls.<OrderSpecifier>list(), 0, Integer.MAX_VALUE);
//        Assert.assertTrue(resultIds3.length > 0);
//        for (long id : resultIds3)
//        {
//            int ix = Arrays.binarySearch(ids, id);
//            Assert.assertFalse(objects.get(ix).getGeometry().equals(firstObject.getGeometry()));
//        }

        cache.clear(ids);
        long[] resultIds4 = cache.getIds(category, Nulls.<PropertyMatcher<?>>list(), Nulls.<OrderSpecifier>list(), 0,
                Integer.MAX_VALUE);
        Assert.assertEquals(0, resultIds4.length);
        cache.close();
    }

    /**
     * Test updating values in the cache.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is another database error.
     * @throws NotSerializableException If an object cannot be serialized.
     */
    @Test
    public void testUpdate() throws ClassNotFoundException, CacheException, NotSerializableException
    {
        H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        DataModelCategory category = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY1);
        Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        int objectCount = 10;
        List<TestObject> objects = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; ++i)
        {
            objects.add(new TestObject(Integer.toString(i), null, null));
        }

        Collection<PropertyAccessor<TestObject, ?>> accessors = new ArrayList<>();
        PropertyDescriptor<Boolean> activeDescriptor = new PropertyDescriptor<>("active", Boolean.class);
        accessors.add(new SerializableAccessor<TestObject, Boolean>(activeDescriptor)
        {
            /** A boolean to flip back and forth. */
            private boolean myAlternator;

            @Override
            public Boolean access(TestObject input)
            {
                myAlternator ^= true;
                return Boolean.valueOf(myAlternator);
            }
        });
        accessors.add(new SerializableAccessor<H2CacheImplTest.TestObject, String>("key", String.class)
        {
            @Override
            public String access(TestObject input)
            {
                return input.getKey();
            }
        });
        CacheDeposit<TestObject> insert = new DefaultCacheDeposit<>(category, accessors, objects, true, expiration, true);
        DefaultCacheModificationListener listener = new DefaultCacheModificationListener();
        long[] ids = cache.put(insert, listener);
        Assert.assertEquals(1, listener.getReports().size());
        CacheModificationReport report = listener.getReports().iterator().next();
        Assert.assertEquals(category, report.getDataModelCategory());
        final long[] batch1Ids = report.getIds();
        Assert.assertEquals(objectCount, batch1Ids.length);
        assertArraysEqual(ids, batch1Ids);

        PropertyValueMap cacheResultMap = new PropertyValueMap();
        ArrayList<Boolean> results = new ArrayList<>(batch1Ids.length);
        cacheResultMap.addResultList(activeDescriptor, results);
        cache.getValues(batch1Ids, cacheResultMap, null);

        for (int i = 0; i < results.size();)
        {
            Assert.assertTrue(results.get(i++).booleanValue());
            Assert.assertFalse(results.get(i++).booleanValue());
        }

        DataModelCategory category2 = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY2);
        long[] batch2Ids = cache.put(
                new DefaultCacheDeposit<TestObject>(category2, accessors, objects, true, CacheDeposit.SESSION_END, true),
                listener);

        Collection<PersistentPropertyAccessor<TestObject, ?>> accessors2 = new ArrayList<>();
        accessors2.add(new SerializableAccessor<TestObject, Boolean>(activeDescriptor)
        {
            /** A boolean to flip back and forth. */
            private boolean myAlternator = true;

            @Override
            public Boolean access(TestObject input)
            {
                myAlternator ^= true;
                return Boolean.valueOf(myAlternator);
            }
        });
        List<TestObject> objects2 = new ArrayList<>(objects);
        Collections.shuffle(objects2);

        List<String> keys = new ArrayList<>(objects2.size());
        for (TestObject obj : objects2)
        {
            keys.add(obj.getKey());
        }
        PropertyDescriptor<String> desc = new PropertyDescriptor<>("key", String.class);
        List<? extends PropertyMatcher<?>> parameters = Collections.singletonList(new MultiPropertyMatcher<String>(desc, keys));
        long[] ids2 = cache.getIds(category, parameters, Nulls.<OrderSpecifier>list(), 0, Integer.MAX_VALUE);
        Assert.assertEquals(batch1Ids.length, ids2.length);

        // Verify the order.
        for (int i = 0; i < ids2.length; i++)
        {
            long id = ids2[i];
            int index = Arrays.binarySearch(batch1Ids, id);
            TestObject found = objects.get(index);
            TestObject expected = objects2.get(i);
            Assert.assertEquals(expected, found);
        }

        listener.getReports().clear();
        cache.updateValues(ids2, objects2, accessors2, null, listener);
        Assert.assertEquals(1, listener.getReports().size());
        report = listener.getReports().iterator().next();
        Assert.assertEquals(category, report.getDataModelCategory());
        long[] ids3 = report.getIds();
        assertArraysEqual(ids2, ids3);

        results.clear();
        cache.getValues(ids2, cacheResultMap, null);

        for (int i = 0; i < results.size();)
        {
            Assert.assertFalse(results.get(i++).booleanValue());
            Assert.assertTrue(results.get(i++).booleanValue());
        }

        // Update all ids with a TRUE value.
        Collection<SerializableAccessor<Boolean, ?>> accessors3 = new ArrayList<>();
        accessors3.add(SerializableAccessor.getHomogeneousAccessor(activeDescriptor));
        PropertyDescriptor<Boolean> junkDescriptor = new PropertyDescriptor<>("junk", Boolean.class);
        accessors3.add(SerializableAccessor.getHomogeneousAccessor(junkDescriptor));

        listener.getReports().clear();
        cache.put(new DefaultCacheDeposit<Boolean>(new DataModelCategory(Nulls.STRING, Nulls.STRING, Nulls.STRING), accessors3,
                Collections.singleton(Boolean.TRUE), false, CacheDeposit.SESSION_END, true), listener);
        Assert.assertEquals(2, listener.getReports().size());

        // Check the first report.
        Iterator<? extends CacheModificationReport> reportsIter = listener.getReports().iterator();
        report = reportsIter.next();
        ids2 = report.getIds();
        long[] batchIds = getBatchIds(report, category, category2, batch1Ids, batch2Ids);
        Assert.assertNotNull(batchIds);
        assertArraysEqual(ids2, batchIds);
        Collection<? extends PropertyDescriptor<?>> propertyDescriptors = report.getPropertyDescriptors();
        Assert.assertEquals(2, propertyDescriptors.size());
        Assert.assertTrue(propertyDescriptors.contains(activeDescriptor));
        Assert.assertTrue(propertyDescriptors.contains(junkDescriptor));

        // Check the second report.
        report = reportsIter.next();
        ids2 = report.getIds();
        batchIds = getBatchIds(report, category, category2, batch1Ids, batch2Ids);
        Assert.assertNotNull(batchIds);
        assertArraysEqual(ids2, batchIds);
        propertyDescriptors = report.getPropertyDescriptors();
        Assert.assertEquals(2, propertyDescriptors.size());
        Assert.assertTrue(propertyDescriptors.contains(activeDescriptor));
        Assert.assertTrue(propertyDescriptors.contains(junkDescriptor));

        results.clear();
        cache.getValues(batch1Ids, cacheResultMap, null);

        for (int i = 0; i < results.size();)
        {
            Assert.assertTrue(results.get(i++).booleanValue());
        }

        cache.clear();
        ids2 = cache.getIds(category, Nulls.<PropertyMatcher<?>>list(), Nulls.<OrderSpecifier>list(), 0, Integer.MAX_VALUE);
        Assert.assertEquals(0, ids2.length);

        cache.close();
    }

    /**
     * Test updating interval values in the cache.
     *
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If there is another database error.
     * @throws NotSerializableException If an object cannot be serialized.
     */
    @Test
    public void testUpdateInterval() throws ClassNotFoundException, CacheException, NotSerializableException
    {
        H2CacheImpl cache = new H2CacheImpl(DB_URL, -1, null);
        cache.initialize(-1L);

        DataModelCategory category = new DataModelCategory(SOURCE1, FAMILY1, CATEGORY1);
        Date expiration = new Date(System.currentTimeMillis() + 3600000L);

        int objectCount = 10;
        List<TestObject> objects = new ArrayList<>(objectCount);
        for (int i = 0; i < objectCount; ++i)
        {
            objects.add(new TestObject(Integer.toString(i), null, null));
        }

        CacheDeposit<TestObject> insert = new DefaultCacheDeposit<TestObject>(category,
                Nulls.<PropertyAccessor<TestObject, ?>>collection(), objects, true, expiration, true);
        long[] ids = cache.put(insert, (CacheModificationListener)null);
        Assert.assertEquals(objectCount, ids.length);

        Collection<PersistentPropertyAccessor<TestObject, ?>> accessors = new ArrayList<>();
        final TimeSpan extent = TimeSpan.get(100L, 200L);
        accessors.add(new TimeSpanAccessor<TestObject>(extent)
        {
            @Override
            public TimeSpan access(TestObject input)
            {
                return extent;
            }
        });

        cache.updateValues(ids, objects, accessors, null, null);

        PropertyValueMap cacheResultMap = new PropertyValueMap();
        ArrayList<TimeSpan> results = new ArrayList<>(ids.length);
        cacheResultMap.addResultList(new PropertyDescriptor<TimeSpan>(TimeSpanAccessor.TIME_PROPERTY_NAME, TimeSpan.class),
                results);
        cache.getValues(ids, cacheResultMap, null);

        for (int i = 0; i < results.size(); ++i)
        {
            Assert.assertEquals(extent, results.get(i));
        }

        try
        {
            cache.updateValues(ids, objects, accessors, null, null);
            Assert.fail("Duplicate update should have failed.");
        }
        catch (CacheException e)
        {
            // This exception indicates success for this test since duplicates
            // are not allowed.
            cache.close();
        }
    }

    /**
     * Assert that two arrays of longs are equal.
     *
     * @param arr1 The first array.
     * @param arr2 The second array.
     */
    private void assertArraysEqual(long[] arr1, final long[] arr2)
    {
        Assert.assertTrue("Arrays are not equal.", Arrays.equals(arr1, arr2));
    }

    /**
     * Create some test objects.
     *
     * @param objectCount The number of objects.
     * @param stringCount The number of strings in the object arrays.
     * @return The objects.
     */
    private List<TestObject> createTestObjects(int objectCount, int stringCount)
    {
        List<TestObject> objects = new ArrayList<>(objectCount);
        Random rand = new Random();
        for (int i = 0; i < objectCount; ++i)
        {
            ArrayList<String> strings = new ArrayList<>(stringCount);
            for (int j = 0; j < stringCount; j++)
            {
                strings.add(j + "_" + rand.nextInt());
            }
            objects.add(new TestObject(Integer.toString(i), (Geometry)null, strings));
        }
        return objects;
    }

    /**
     * Helper to get the batch ids for the given report.
     *
     * @param report The report
     * @param category The category of the first report
     * @param category2 The category of the second report
     * @param batch1Ids The batch ids of the first report
     * @param batch2Ids The batch ids of the second report
     * @return The batch ids
     */
    private long[] getBatchIds(CacheModificationReport report, DataModelCategory category, DataModelCategory category2,
            long[] batch1Ids, long[] batch2Ids)
    {
        return report.getDataModelCategory().equals(category) ? batch1Ids
                : report.getDataModelCategory().equals(category2) ? batch2Ids : null;
    }

    /**
     * An object to put into the cache.
     */
    private static class TestObject implements Serializable
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        /** The location for this object. */
        private final Geometry myGeometry;

        /** The key for this object. */
        private final String myKey;

        /** The strings. */
        private final List<String> myStrings;

        /**
         * Constructor.
         *
         * @param key The key for this object.
         * @param geom The location of the object.
         * @param strings The strings.
         */
        public TestObject(String key, Geometry geom, List<String> strings)
        {
            myKey = key;
            myGeometry = geom;
            myStrings = strings;
        }

        /**
         * Accessor for the geometry.
         *
         * @return The geometry.
         */
        public Geometry getGeometry()
        {
            return myGeometry;
        }

        /**
         * Accessor for the key.
         *
         * @return The key.
         */
        public String getKey()
        {
            return myKey;
        }

        /**
         * Get my strings.
         *
         * @return The strings.
         */
        public List<String> getStrings()
        {
            return myStrings;
        }
    }
}
