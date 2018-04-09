package io.opensphere.core.cache.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator.ConnectionUser;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * A garbage collector that checks the database for expired records and removes
 * them a little at a time to avoid locking the database for long periods of
 * time.
 */
public class GarbageCollector implements Runnable
{
    /** Minimum time between executions. */
    private static final int DELAY_MILLISECONDS = Integer.getInteger("opensphere.db.gcDelayMilliseconds", 400).intValue();

    /**
     * How long to allow a group to exist after its expiration time. A query may
     * still be running that is using the group when the group expires, so this
     * buffer allows some time for queries to finish.
     */
    private static final int EXPIRATION_BUFFER_MS = Integer.getInteger("opensphere.db.expirationBufferMilliseconds", 60000)
            .intValue();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GarbageCollector.class);

    /** The cache utilities. */
    private final CacheUtilities myCacheUtilities;

    /** The database connection appropriator. */
    private final ConnectionAppropriator myConnectionAppropriator;

    /** The earliest time another collection can run. */
    private long myEarliestNextCollectionTimeMillis;

    /** An executor to use for follow-on tasking. */
    private final ScheduledExecutorService myExecutor;

    /** The queue of groups to delete. */
    private final Queue<Integer> myGroupIdQueue = new LinkedList<>();

    /** The SQL generator. */
    private final SQLGenerator mySQLGenerator;

    /** The database task factory. */
    private final DatabaseTaskFactory myTaskFactory;

    /** Time budget for a garbage collection. */
    private final long myTimeBudgetNanoseconds;

    /**
     * Constructor.
     *
     * @param timeBudgetNanoseconds The time budget for each collection, in
     *            nanoseconds.
     * @param taskFactory The database task factory.
     * @param cacheUtilities The cache utilities instance.
     * @param sqlGenerator An SQL generator.
     * @param connectionAppropriator A connection appropriator.
     * @param executor An executor to use for follow-on tasking.
     */
    public GarbageCollector(long timeBudgetNanoseconds, DatabaseTaskFactory taskFactory, CacheUtilities cacheUtilities,
            SQLGenerator sqlGenerator, ConnectionAppropriator connectionAppropriator, ScheduledExecutorService executor)
    {
        myTimeBudgetNanoseconds = timeBudgetNanoseconds;
        myTaskFactory = taskFactory;
        myCacheUtilities = cacheUtilities;
        mySQLGenerator = sqlGenerator;
        myConnectionAppropriator = connectionAppropriator;
        myExecutor = executor;
    }

    /**
     * Reset the garbage collector so it will re-query for expired groups. This
     * is necessary if tables are dropped external to the garbage collector.
     */
    public synchronized void reset()
    {
        myEarliestNextCollectionTimeMillis = System.currentTimeMillis() + DELAY_MILLISECONDS;
        myGroupIdQueue.clear();
    }

    @Override
    public synchronized void run()
    {
        if (System.currentTimeMillis() < myEarliestNextCollectionTimeMillis)
        {
            return;
        }
        try
        {
            final long deadline = System.nanoTime() + myTimeBudgetNanoseconds / 2;
            do
            {
                final Integer groupId = myGroupIdQueue.poll();
                if (groupId == null)
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Checking for expired data groups.");
                    }
                    for (final int id : getExpiredGroups())
                    {
                        myGroupIdQueue.add(Integer.valueOf(id));
                    }

                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Expired groups to remove: " + myGroupIdQueue);
                    }
                }
                else
                {
                    final long t0 = System.nanoTime();
                    myConnectionAppropriator
                            .appropriateStatement(myTaskFactory.getPurgeGroupsTask(new int[] { groupId.intValue() }));
                    final long et = System.nanoTime() - t0;

                    if (LOGGER.isDebugEnabled())
                    {
                        LOGGER.debug(StringUtilities.formatTimingMessage(
                                "Time to perform garbage collection for group " + groupId.intValue() + ": ", et));
                    }
                }
            }
            while (System.nanoTime() < deadline && !myGroupIdQueue.isEmpty());

            myEarliestNextCollectionTimeMillis = System.currentTimeMillis() + DELAY_MILLISECONDS;

            // Do some higher-frequency cleanup as long as the group id queue
            // is not empty.
            if (!myGroupIdQueue.isEmpty() && myExecutor != null)
            {
                try
                {
                    myExecutor.schedule(this, DELAY_MILLISECONDS, TimeUnit.MILLISECONDS);
                }
                catch (final RejectedExecutionException e)
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(e);
                    }
                }
            }
        }
        catch (final CacheException e)
        {
            LOGGER.warn("Failed to perform garbage collection on database: " + e, e);
        }
    }

    /**
     * Get the ids of any expired groups in the database.
     *
     * @return The array of ids.
     * @throws CacheException If there is a database error.
     */
    protected int[] getExpiredGroups() throws CacheException
    {
        final ConnectionUser<int[]> user = new ConnectionUser<int[]>()
        {
            @Override
            public int[] run(Connection conn) throws CacheException
            {
                return new StatementAppropriator(conn).appropriateStatement(new StatementUser<int[]>()
                {
                    @Override
                    public int[] run(Connection unused, Statement stmt) throws CacheException
                    {
                        final long timeThreshold = System.currentTimeMillis() - EXPIRATION_BUFFER_MS;
                        final String sql = mySQLGenerator.generateGetExpiredGroups(timeThreshold);
                        return myCacheUtilities.executeIntArrayQuery(stmt, sql);
                    }
                });
            }
        };

        return myConnectionAppropriator.appropriateConnection(user, false);
    }
}
