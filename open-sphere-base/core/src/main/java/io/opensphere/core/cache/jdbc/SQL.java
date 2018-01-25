package io.opensphere.core.cache.jdbc;

/**
 * SQL constants.
 */
public final class SQL
{
    /** Add SQL. */
    public static final String ADD = " ADD ";

    /** Add if not exists SQL. */
    public static final String ADD_IF_NOT_EXISTS = " ADD IF NOT EXISTS ";

    /** Alter table add SQL. */
    public static final String ALTER_TABLE = "ALTER TABLE ";

    /** And SQL. */
    public static final String AND = " AND ";

    /** As SQL. */
    public static final String AS = " AS ";

    /** Create index SQL. */
    public static final String CREATE_INDEX = "CREATE INDEX IF NOT EXISTS ";

    /** Create unique index SQL. */
    public static final String CREATE_INDEX_UNIQUE = "CREATE UNIQUE INDEX IF NOT EXISTS ";

    /** Create sequence SQL. */
    public static final String CREATE_SEQUENCE = "CREATE SEQUENCE IF NOT EXISTS ";

    /** Create table SQL. */
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";

    /** Create temporary table SQL. */
    public static final String CREATE_TEMP_TABLE = "CREATE LOCAL TEMPORARY TABLE ";

    /** Group id column identifier. */
    public static final String DATA_GROUP_GROUP_ID;

    /** Beginning of delete statements. */
    public static final String DELETE_FROM = "DELETE FROM ";

    /** Group id column identifier after a '.'. */
    public static final String DOT_GROUP_ID = "." + ColumnNames.GROUP_ID;

    /** Sequence column identifier after a '.'. */
    public static final String DOT_SEQUENCE;

    /** Drop all objects SQL. */
    public static final String DROP_ALL_OBJECTS = "DROP ALL OBJECTS";

    /** Drop table SQL. */
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    /** Drop trigger SQL. */
    public static final String DROP_TRIGGER = "DROP TRIGGER IF EXISTS ";

    /** Equals SQL. */
    public static final String EQUALS = " = ";

    /** Exists SQL. */
    public static final String EXISTS_SELECT = "EXISTS (SELECT ";

    /** Expression for expiration time that accounts for nulls. */
    public static final String EXPIRATION_TIME_QUERY = "IFNULL(" + ColumnNames.EXPIRATION_TIME + ", " + Long.MAX_VALUE + ')';

    /** For update SQL. */
    public static final String FOR_UPDATE = " FOR UPDATE";

    /** Foreign key SQL. */
    public static final String FOREIGN_KEY = "FOREIGN KEY (";

    /** From SQL. */
    public static final String FROM = " FROM ";

    /** The name of the group id sequence. */
    public static final String GROUP_ID_SEQUENCE = "GROUP_ID_SEQUENCE";

    /** In SQL. */
    public static final String IN = " IN ";

    /** Increment by SQL. */
    public static final String INCREMENT_BY = " INCREMENT BY ";

    /** Inner join SQL. */
    public static final String INNER_JOIN = " INNER JOIN ";

    /** Beginning of insert statements. */
    public static final String INSERT_INTO = "INSERT INTO ";

    /** Intersect SQL. */
    public static final String INTERSECT_SELECT = " INTERSECT SELECT ";

    /** IS NOT NULL SQL. */
    public static final String IS_NOT_NULL = " IS NOT NULL ";

    /** Is null SQL. */
    public static final String IS_NULL = " IS NULL ";

    /** Key SQL. */
    public static final String KEY = " KEY (\"";

    /** Left outer join SQL. */
    public static final String LEFT_JOIN = " LEFT OUTER JOIN ";

    /** Length SQL. */
    public static final String LENGTH = "LENGTH(\"";

    /** Like SQL. */
    public static final String LIKE = " LIKE ";

    /** Limit SQL. */
    public static final String LIMIT = " LIMIT ";

    /** Beginning of merge statements. */
    public static final String MERGE_INTO = "MERGE INTO ";

    /** Natural join SQL. */
    public static final String NATURAL_JOIN = " NATURAL JOIN ";

    /** Next value for SQL. */
    public static final String NEXT_VALUE_FOR = " NEXT VALUE FOR ";

    /** Not SQL. */
    public static final String NOT = "NOT ";

    /** Not critical SQL. */
    public static final String NOT_CRITICAL_QUERY;

    /** Not equals SQL. */
    public static final String NOT_EQUALS = " != ";

    /** Not persistent SQL. */
    public static final String NOT_PERSISTENT = " NOT PERSISTENT";

    /** Null SQL. */
    public static final String NULL = "NULL";

    /** Offset SQL. */
    public static final String OFFSET = " OFFSET ";

    /** On SQL. */
    public static final String ON = " ON ";

    /** On delete cascade SQL. */
    public static final String ON_DELETE_CASCADE = " ON DELETE CASCADE";

    /** Order by SQL. */
    public static final String ORDER_BY = " ORDER BY ";

    /** Primary key SQL. */
    public static final String PRIMARY_KEY = "PRIMARY KEY";

    /** References SQL. */
    public static final String REFERENCES = ") REFERENCES ";

    /** Right outer join SQL. */
    public static final String RIGHT_JOIN = " RIGHT OUTER JOIN ";

    /** Select SQL. */
    public static final String SELECT = "SELECT ";

    /** Select distinct SQL. */
    public static final Object SELECT_DISTINCT = "SELECT DISTINCT ";

    /** Set SQL. */
    public static final String SET = " SET ";

    /** Start with SQL. */
    public static final String START_WITH = " START WITH ";

    /** String constant used for constructing SQL queries. */
    public static final String TICK_COMMA_TICK = "','";

    /** Transactional SQL. */
    public static final String TRANSACTIONAL = " TRANSACTIONAL";

    /** Union SQL. */
    public static final String UNION = " UNION ";

    /** Update SQL. */
    public static final String UPDATE = "UPDATE ";

    /** Values SQL. */
    public static final String VALUES = " VALUES (";

    /** Where SQL. */
    public static final String WHERE = " WHERE ";

    static
    {
        DATA_GROUP_GROUP_ID = TableNames.DATA_GROUP + DOT_GROUP_ID;
        DOT_SEQUENCE = '.' + ColumnNames.SEQUENCE;
        NOT_CRITICAL_QUERY = ColumnNames.CRITICAL + NOT_EQUALS + "'TRUE'";
    }

    /** Disallow instantiation. */
    private SQL()
    {
    }
}
