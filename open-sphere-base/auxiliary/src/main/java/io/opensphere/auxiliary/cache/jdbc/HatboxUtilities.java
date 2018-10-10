package io.opensphere.auxiliary.cache.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import io.opensphere.core.cache.CacheException;
import io.opensphere.core.cache.jdbc.CacheUtilities;
import io.opensphere.core.cache.jdbc.StatementAppropriator;
import io.opensphere.core.util.lang.StringUtilities;
import net.sourceforge.hatbox.jts.Proc;

/**
 * Utilities for Hatbox.
 */
final class HatboxUtilities
{
    /** The schema name. */
    public static final String SCHEMA_NAME = "PUBLIC";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(HatboxUtilities.class);

    /**
     * Build the spatial index for a table.
     *
     * @param dbState The in-memory cache of the database state.
     * @param cacheUtilities The cache utilities.
     * @param conn The connection to use.
     * @param tableName The table name.
     * @param geometryColumnName The geometry column name.
     * @throws CacheException If there is a database error.
     */
    public static void buildSpatialIndex(H2DatabaseState dbState, final CacheUtilities cacheUtilities, Connection conn,
            String tableName, String geometryColumnName) throws CacheException
    {
        List<String> neededTriggers = Arrays.asList(tableName + "_INSTRG", tableName + "_UPDTRG", tableName + "_DELTRG");

        if (dbState.getCreatedTriggers().containsAll(neededTriggers))
        {
            return;
        }

        Collection<String> existingTriggers = new StatementAppropriator(conn)
                .appropriateStatement((unused, stmt) -> getTriggerNames(cacheUtilities, stmt));
        dbState.getCreatedTriggers().addAll(existingTriggers);

        if (!existingTriggers.containsAll(neededTriggers))
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Building index on table: " + tableName);
            }
            @SuppressWarnings("PMD.PrematureDeclaration")
            long t0 = System.nanoTime();
            try
            {
                Proc.buildIndex(conn, SCHEMA_NAME, tableName, 100, null);
                dbState.getCreatedTriggers().addAll(neededTriggers);
            }
            catch (SQLException e)
            {
                throw new CacheException("Failed to create spatial index: " + e, e);
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(StringUtilities.formatTimingMessage("Spatial indexing complete for table " + tableName + " in ",
                        System.nanoTime() - t0));
            }
        }
    }

    /**
     * Remove spatial index for a table.
     *
     * @param dbState The database state.
     * @param conn The connection to use.
     * @param schemaName The schema name.
     * @param tableName The table name.
     * @throws CacheException If there is a database error.
     */
    public static void despatialize(H2DatabaseState dbState, Connection conn, String schemaName, String tableName)
        throws CacheException
    {
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("De-spatializing table [" + tableName + "]");
            }
            long t0 = System.nanoTime();

            dbState.getCreatedTriggers()
                    .removeAll(Arrays.asList(tableName + "_INSTRG", tableName + "_UPDTRG", tableName + "_DELTRG"));

            Proc.deSpatialize(conn, schemaName, tableName);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                        StringUtilities.formatTimingMessage("Despatialized table " + tableName + " in ", System.nanoTime() - t0));
            }
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to remove spatial index: " + e, e);
        }
    }

    /**
     * Prepare a table to be used by the Hatbox aliases and indices.
     *
     * @param conn The connection to use.
     * @param schemaName The schema name.
     * @param tableName The table name.
     * @param geometryColumnName The geometry column name.
     * @throws CacheException If there is a database error.
     */
    public static void spatialize(Connection conn, String schemaName, String tableName, String geometryColumnName)
        throws CacheException
    {
        try
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Spatializing table [" + tableName + "]");
            }
            long t0 = System.nanoTime();

            // Hatbox does not appear to use the geometry type or the SRID.
            Proc.spatialize(conn, schemaName, tableName, geometryColumnName, null, "", "false", null);

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                        StringUtilities.formatTimingMessage("Spatialized table " + tableName + " in ", System.nanoTime() - t0));
            }
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to create spatial index: " + e, e);
        }
    }

    /**
     * Get the names of the triggers currently in the database.
     *
     * @param cacheUtilities The cache utilities.
     * @param stmt A statement to use.
     * @return The trigger names.
     * @throws CacheException If the database query failed.
     */
    static Collection<String> getTriggerNames(CacheUtilities cacheUtilities, Statement stmt) throws CacheException
    {
        Collection<String> existingTriggers = new ArrayList<>();
        ResultSet rs = cacheUtilities.executeQuery(stmt, "select trigger_name from information_schema.triggers");
        try
        {
            while (rs.next())
            {
                existingTriggers.add(rs.getString(1));
            }
        }
        catch (SQLException e)
        {
            throw new CacheException("Failed to read results: " + e, e);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace("Failed to close result set: " + e, e);
                }
            }
        }
        return existingTriggers;
    }

    /** Disallow instantiation. */
    private HatboxUtilities()
    {
    }
}
