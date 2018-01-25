package io.opensphere.core.data;

import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.CacheModificationListener;
import io.opensphere.core.cache.CacheModificationReport;
import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.cache.DefaultCacheModificationListener;
import io.opensphere.core.cache.accessor.PersistentPropertyAccessor;
import io.opensphere.core.cache.accessor.SerializableAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import org.junit.Assert;

/**
 * Tests for {@link DataRegistryImpl}.
 */
public class DataRegistryImplTest
{
    /**
     * Test for
     * {@link DataRegistryImpl#updateModels(long[], java.util.Collection, java.util.Collection, Object, boolean, CacheModificationListener)}
     * .
     *
     * @throws CacheException If there is a cache error.
     * @throws NotSerializableException If a value is not serializable.
     * @throws InterruptedException If the test is interrupted.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateModelsLongArray() throws NotSerializableException, CacheException, InterruptedException
    {
        long[] ids = { 32523L };
        Collection<? extends String> input = Collections.singleton("test");
        PropertyDescriptor<String> propertyDescriptor = PropertyDescriptor.create("key", String.class);
        Collection<? extends PersistentPropertyAccessor<String, String>> accessors = new ArrayList<PersistentPropertyAccessor<String, String>>(
                Collections.singleton(SerializableAccessor.getHomogeneousAccessor(propertyDescriptor)));
        DataModelCategory dataModelCategory = new DataModelCategory("source", "family", "category");

        final CacheModificationReport report = new CacheModificationReport(dataModelCategory, ids,
                Collections.singleton(propertyDescriptor));

        Cache cache = EasyMock.createMock(Cache.class);
        CacheModificationListener cacheModListenerMock = EasyMock.createMock(CacheModificationListener.class);
        cache.updateValues(EasyMock.eq(ids), EasyMock.eq(input), EasyMock.eq(accessors), EasyMock.<Executor>eq(null),
                EasyMock.<CacheModificationListener>anyObject());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                CacheModificationListener listener = (CacheModificationListener)EasyMock.getCurrentArguments()[4];
                listener.cacheModified(report);
                return null;
            }
        });
        cache.setClassProvider(EasyMock.isA(ClassProvider.class));

        DataRegistryListener<String> listener = EasyMock.createNiceMock(DataRegistryListener.class);

        EasyMock.replay(cache, listener, cacheModListenerMock);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Submit a task to the executor to prevent it from running any tasking
        // from the registry until we're ready.
        final AtomicBoolean semaphore = new AtomicBoolean(false);
        Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (DataRegistryImplTest.this)
                {
                    semaphore.set(true);
                    DataRegistryImplTest.this.notifyAll();
                    while (semaphore.get())
                    {
                        try
                        {
                            DataRegistryImplTest.this.wait();
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }
                }
            }
        };
        executor.execute(runner);

        // Make sure the executor is paused.
        synchronized (this)
        {
            while (!semaphore.get())
            {
                wait();
            }
        }

        boolean returnEarly = false;
        DataRegistry dr = new DataRegistryImpl(executor, cache);
        dr.addChangeListener(listener, new DataModelCategory(null, null, null), propertyDescriptor);
        DefaultCacheModificationListener defCacheModListener = new DefaultCacheModificationListener();
        dr.updateModels(ids, input, accessors, this, returnEarly, defCacheModListener);

        Assert.assertEquals(1, defCacheModListener.getReports().size());
        Assert.assertEquals(report, defCacheModListener.getReports().iterator().next());

        // Allow the executor to run.
        synchronized (this)
        {
            semaphore.set(false);
            notifyAll();
        }

        executor.execute(runner);

        // Wait for the executor to pause again.
        synchronized (this)
        {
            while (!semaphore.get())
            {
                wait();
            }
        }

        EasyMock.verify(cache);
        EasyMock.reset(cache);
        EasyMock.replay(cache);

        returnEarly = true;
        dr.updateModels(ids, input, accessors, this, returnEarly, cacheModListenerMock);

        EasyMock.verify(cache, cacheModListenerMock);
        EasyMock.reset(cache, cacheModListenerMock);

        cache.updateValues(EasyMock.eq(ids), EasyMock.eq(input), EasyMock.eq(accessors), EasyMock.eq(executor),
                EasyMock.<CacheModificationListener>anyObject());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                ((CacheModificationListener)EasyMock.getCurrentArguments()[4]).cacheModified(report);
                return null;
            }
        });

        cacheModListenerMock.cacheModified(report);
        EasyMock.expectLastCall();

        EasyMock.replay(cache, cacheModListenerMock);

        // Allow the executor to run.
        synchronized (this)
        {
            semaphore.set(false);
            notifyAll();
        }

        // Now submit another runner so we know when the listener runner is
        // done.
        executor.execute(runner);

        // Wait for the executor to pause again, and then let it go.
        synchronized (this)
        {
            while (!semaphore.get())
            {
                wait();
            }
            semaphore.set(false);
            notifyAll();
        }

        EasyMock.verify(cache, cacheModListenerMock);
    }
}
