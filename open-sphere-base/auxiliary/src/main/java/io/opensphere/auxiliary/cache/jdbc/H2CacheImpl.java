package io.opensphere.auxiliary.cache.jdbc;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.h2.api.ErrorCode;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.DeleteDbFiles;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import io.opensphere.core.cache.Cache;
import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.ClassProvider;
import io.opensphere.core.cache.DatabaseAlreadyOpenException;
import io.opensphere.core.cache.jdbc.ConnectionAppropriator;
import io.opensphere.core.cache.jdbc.DatabaseTaskFactory;
import io.opensphere.core.cache.jdbc.JdbcCacheImpl;
import io.opensphere.core.cache.jdbc.SQLGenerator;
import io.opensphere.core.cache.jdbc.StatementAppropriator.StatementUser;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.filesystem.FileUtilities;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Implementation of {@link Cache} that uses the H2 database.
 *
 * TODO: Rewrite Hatbox to handle time as well.
 */
public class H2CacheImpl extends JdbcCacheImpl
{
    /** The catalog name (null is a wildcard). */
    protected static final String ALL_CATALOGS = Nulls.STRING;

    /** The schema name. */
    protected static final String SCHEMA_NAME = "PUBLIC";

    /**
     * Default size of the memory cache as a percentage of the JVM max memory
     * value.
     */
    private static final float DEFAULT_CACHE_SIZE_PERCENTAGE = 10f;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(H2CacheImpl.class);

    /** The SQL generator. */
    private static final HatboxSQLGeneratorImpl SQL_GENERATOR = new HatboxSQLGeneratorImpl();

    /** A connection appropriator with H2 extensions. */
    private final H2ConnectionAppropriator myConnectionAppropriator = new H2ConnectionAppropriator(getConnectionSource());

    /** The database connection pool. */
    private JdbcConnectionPool myConnectionPool;

    /** The data source for the connection pool. */
    private final JdbcDataSource myConnectionPoolDataSource;

    /** An in-memory cache of the database state. */
    private final H2DatabaseState myDatabaseState = new H2DatabaseState();

    /** The data trimmer. */
    private volatile DatabaseSizeDataTrimmer myDataTrimmer;

    /** Flag indicating that opening the database failed. */
    private final AtomicBoolean myFailed = new AtomicBoolean(false);

    /**
     * The path to the database file.
     */
    private final String myPath;

    /**
     * Lock used to block getting connections while the database is restarting.
     */
    private final ReadWriteLock myRestartLock = new ReentrantReadWriteLock();

    /** The type mapper responsible for mapping Java types to database types. */
    private final H2TypeMapper myTypeMapper = new H2TypeMapper();

    /**
     * Static method for use in H2 stored procedure that converts a JTS
     * well-known-binary to a string representation.
     *
     * @param bytes The well-known-binary.
     * @return The string.
     * @throws ParseException If there is a parse exception.
     */
    public static String wkbToString(byte[] bytes) throws ParseException
    {
        return new WKBReader().read(bytes).toString();
    }

    /**
     * Create the jdbc url for the db connection.
     *
     * @param path The path to the db file.
     * @return The url.
     */
    protected static String compileUrl(String path)
    {
        final boolean autoServer = !path.startsWith("mem:") && Boolean.getBoolean("opensphere.db.autoServer");
        return "jdbc:h2:" + path + "/db"
//+ ";AUTO_SERVER=true;TRACE_LEVEL_FILE=0;LOCK_TIMEOUT=60000;PAGE_SIZE=4096;MULTI_THREADED=0;UNDO_LOG=0;LOG=1;CIPHER=XTEA",
                + ";AUTO_SERVER=" + autoServer
                + ";TRACE_LEVEL_FILE=0;LOCK_TIMEOUT=60000;PAGE_SIZE=4096;MULTI_THREADED=0;UNDO_LOG=0;LOG=1;MAX_MEMORY_UNDO=100000"
                + ";MV_STORE=FALSE";
//"sa", "JBiuujg.ab.PA openspheredb"
    }

    /**
     * Construct the H2 cache implementation.
     *
     * @param path The path to the database file.
     * @param rowLimit The maximum number of rows in a table before trimming
     *            occurs. A negative number indicates no limit.
     * @param executor An executor for background database tasks.
     * @throws ClassNotFoundException If the database driver cannot be loaded.
     * @throws CacheException If the tables cannot be created.
     */
    public H2CacheImpl(String path, int rowLimit, ScheduledExecutorService executor) throws ClassNotFoundException, CacheException
    {
        super("org.h2.Driver", compileUrl(path), "sa", "", rowLimit, executor);
        final String url = getUrl();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Creating connection pool with URL [" + url + "]");
        }
        myConnectionPoolDataSource = new JdbcDataSource();
        myConnectionPoolDataSource.setURL(url);
        myConnectionPoolDataSource.setUser(getUsername());
        myConnectionPoolDataSource.setPassword(getPassword());
        myConnectionPool = createConnectionPool();
        myPath = path;
    }

    @Override
    public void initialize(long millisecondsWait) throws CacheException
    {
        deleteDatabaseIfNecessary();
        super.initialize(millisecondsWait);
    }

    @Override
    public synchronized void close()
    {
        if (isClosed())
        {
            return;
        }

        super.close();
        myConnectionPool.dispose();
    }

    @Override
    public void setClassProvider(ClassProvider provider)
    {
        myTypeMapper.setClassProvider(provider);
    }

    @Override
    public void setInMemorySizeBytes(final long bytes) throws CacheException
    {
        super.setInMemorySizeBytes(bytes);

        LOGGER.info("Setting H2 cache_size to " + bytes / Constants.BYTES_PER_KILOBYTE + "KB");
        getConnectionAppropriator().appropriateStatement((conn, stmt) ->
        {
            getCacheUtil().execute("set cache_size " + bytes / Constants.BYTES_PER_KILOBYTE, stmt);
            return null;
        });
    }

    @Override
    public void setOnDiskSizeLimitBytes(long bytes)
    {
        if (bytes > 0L)
        {
            DatabaseSizeDataTrimmer dataTrimmer = myDataTrimmer;
            if (dataTrimmer == null)
            {
                final DatabaseCompactor compactor = new DatabaseCompactor()
                {
                    @Override
                    public void compact(int[] groupIds)
                    {
                        compactDatabase(groupIds);
                    }
                };
                dataTrimmer = new DatabaseSizeDataTrimmer(Long.MAX_VALUE, getCacheUtil(), SQL_GENERATOR,
                        getConnectionAppropriator(), getLock().readLock(), compactor);
                myDataTrimmer = dataTrimmer;
                scheduleDataTrimmer();
            }

            dataTrimmer.setSizeLimitBytes(bytes);
        }
        else
        {
            myDataTrimmer = null;
        }
    }

    /**
     * Deletes the database if necessary.
     */
    protected void deleteDatabaseIfNecessary()
    {
        try
        {
            Path parent = Paths.get(myPath).getParent();
            if (parent != null)
            {
                Path prefsPath = Paths.get(parent.toString(), "prefs");
                boolean fromOldVersion = !Files
                        .exists(Paths.get(prefsPath.toString(), "io.opensphere.core.pipeline.Pipeline.xml"));
                if (fromOldVersion)
                {
                    Path dbPath = Paths.get(myPath, "db.h2.db");
                    LOGGER.info("Deleting/archiving " + dbPath);
                    FileUtilities.archive(dbPath);
                }
            }
        }
        catch (InvalidPathException e)
        {
            LOGGER.error(e);
        }
    }

    /**
     * Compact the database by dropping the tables for some groups and shutting
     * it down and restarting.
     *
     * @param groupIds The group ids.
     */
    protected void compactDatabase(final int[] groupIds)
    {
        if (isClosed())
        {
            return;
        }

        final int maxConnections = myConnectionPool.getMaxConnections();

        myRestartLock.writeLock().lock();
        try
        {
            final long t0 = System.nanoTime();

            // Set the max connections to 1 so that when we get a connection, we
            // know it's the only one.
            myConnectionPool.setMaxConnections(1);

            runTask((StatementUser<Void>)(conn2, stmt) ->
            {
                getDatabaseTaskFactory().getPurgeGroupsTask(groupIds).run(conn2, stmt);
                getCacheUtil().execute("SHUTDOWN COMPACT", stmt);
                return null;
            });

            myConnectionPool.dispose();
            myConnectionPool = createConnectionPool();

            LOGGER.info(StringUtilities.formatTimingMessage("Time to compact database: ", System.nanoTime() - t0));
        }
        catch (final CacheException e)
        {
            LOGGER.error("Failed to compact database: " + e, e);
        }
        finally
        {
            myRestartLock.writeLock().unlock();
            myConnectionPool.setMaxConnections(maxConnections);
        }
    }

    /**
     * Create the connection pool.
     *
     * @return The connection pool.
     */
    protected final JdbcConnectionPool createConnectionPool()
    {
        final JdbcConnectionPool connectionPool = JdbcConnectionPool.create(myConnectionPoolDataSource);
        connectionPool.setMaxConnections(100);
        return connectionPool;
    }

    @Override
    protected DatabaseTaskFactory createDatabaseTaskFactory()
    {
        return new HatboxDatabaseTaskFactory(getCacheUtil(), getDatabaseState(), getSQLGenerator(), getTypeMapper());
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    protected Connection getConnection() throws CacheException
    {
        final long t0 = System.nanoTime();
        myRestartLock.readLock().lock();
        try
        {
            return myConnectionPool.getConnection();
        }
        catch (final SQLException e)
        {
            if (myConnectionPool.getActiveConnections() == 0 && myFailed.compareAndSet(false, true)
                    && e.getErrorCode() != ErrorCode.DATABASE_CALLED_AT_SHUTDOWN)
            {
                if (e.getErrorCode() == ErrorCode.DATABASE_ALREADY_OPEN_1)
                {
                    throw new DatabaseAlreadyOpenException();
                }

                LOGGER.error("Opening database [" + getUrl() + "] failed. Deleting database.");
                final String dir = myPath;
                final String db = "db";
                DeleteDbFiles.execute(dir, db, false);

                @SuppressWarnings("PMD.CloseResource")
                final Connection connection = getConnection();
                myFailed.set(false);
                return connection;
            }
            LOGGER.warn("Time waiting for connection was: " + (double)(System.nanoTime() - t0) / Constants.NANO_PER_UNIT + "s");
            throw new CacheException("Failed to get connection from connection pool: " + e, e);
        }
        finally
        {
            myRestartLock.readLock().unlock();
        }
    }

    @Override
    protected ConnectionAppropriator getConnectionAppropriator()
    {
        return myConnectionAppropriator;
    }

    @Override
    protected H2DatabaseState getDatabaseState()
    {
        return myDatabaseState;
    }

    @Override
    protected SQLGenerator getSQLGenerator()
    {
        return SQL_GENERATOR;
    }

    @Override
    protected H2TypeMapper getTypeMapper()
    {
        return myTypeMapper;
    }

    /**
     * Create the function aliases in the database if they don't exist.
     *
     * @param stmt The statement to use.
     * @throws SQLException If there's a database error.
     */
    protected void initAliases(Statement stmt) throws SQLException
    {
        final Collection<String> existingAliases = new ArrayList<>();
        final ResultSet rs = stmt.getConnection().getMetaData().getProcedures(ALL_CATALOGS, SCHEMA_NAME, "%");
        try
        {
            while (rs.next())
            {
                existingAliases.add(rs.getString("ALIAS_NAME"));
            }
        }
        finally
        {
            rs.close();
        }

        final String[] h2Alias = { "HATBOX_BUILD_INDEX", "HATBOX_DE_SPATIALIZE", "HATBOX_INTERSECTS_WKB", "HATBOX_INTERSECTS_WKT",
            "HATBOX_MBR_INTERSECTS_ENV", "HATBOX_QUERY_WITH_PREDICATE_WKB", "HATBOX_QUERY_WITH_PREDICATE_WKT",
            "HATBOX_QUERY_WITH_MATRIX_WKB", "HATBOX_QUERY_WITH_MATRIX_WKT", "HATBOX_SPATIALIZE", "HATBOX_GET_DATASET_BOUNDS",
            "WKB_TO_STRING", };

        final String[] h2AliasMethod = { "net.sourceforge.hatbox.jts.Proc.buildIndex",
            "net.sourceforge.hatbox.jts.Proc.deSpatialize", "net.sourceforge.hatbox.jts.Proc.queryIntersectsWkb",
            "net.sourceforge.hatbox.jts.Proc.queryIntersectsWkt", "net.sourceforge.hatbox.jts.Proc.mbrIntersectsEnv",
            "net.sourceforge.hatbox.jts.Proc.queryWithPredicateWkb", "net.sourceforge.hatbox.jts.Proc.queryWithPredicateWkt",
            "net.sourceforge.hatbox.jts.Proc.queryWithMatrixWkb", "net.sourceforge.hatbox.jts.Proc.queryWithMatrixWkt",
            "net.sourceforge.hatbox.jts.Proc.spatialize", "net.sourceforge.hatbox.jts.Proc.getDatasetBounds",
            H2CacheImpl.class.getName() + ".wkbToString", };

        for (int i = 0; i < h2Alias.length; i++)
        {
            if (!existingAliases.contains(h2Alias[i]))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Creating alias: " + h2Alias[i]);
                }
                stmt.executeUpdate(new StringBuilder("create alias ").append(h2Alias[i]).append(" for \"")
                        .append(h2AliasMethod[i]).append('"').toString());
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Extend the superclass to spatialize the database and set the cache size
     * on the database.
     */
    @Override
    protected void initSchema(Connection conn, Statement stmt) throws CacheException
    {
        final Lock writeLock = getLock().writeLock();
        writeLock.lock();
        try
        {
            super.initSchema(conn, stmt);

            try
            {
                initAliases(stmt);
            }
            catch (SQLException | RuntimeException e)
            {
                throw new CacheException("Failed to spatialize database: " + e, e);
            }

            final String cacheSizeString = System.getProperty("opensphere.db.cacheSizePercentage");
            float cacheSize = DEFAULT_CACHE_SIZE_PERCENTAGE;
            try
            {
                if (cacheSizeString != null)
                {
                    cacheSize = Float.parseFloat(cacheSizeString);
                }
            }
            catch (final NumberFormatException e)
            {
                LOGGER.warn("Could not parse system property opensphere.db.cacheSizePercentage as a float: " + cacheSizeString);
            }
            final int kb = (int)(Runtime.getRuntime().maxMemory() * cacheSize / 100 / Constants.BYTES_PER_KILOBYTE);
            getCacheUtil().execute("set cache_size " + kb, stmt);

            // getCacheUtil().execute("set database_event_listener='" +
            // H2Listener.class.getCanonicalName() + "'", stmt);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    protected void scheduleDataTrimmer()
    {
        super.scheduleDataTrimmer();

        final DatabaseSizeDataTrimmer dataTrimmer = myDataTrimmer;
        if (dataTrimmer != null)
        {
            getCacheUtil().scheduleDataTrimmer(dataTrimmer, getExecutor());
        }
    }
}
