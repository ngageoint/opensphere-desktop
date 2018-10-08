package io.opensphere.core.common.monitor;

import java.io.Serializable;
import java.sql.Statement;
import java.util.Date;

/**
 * Data Structure to hold useful information about a DB query
 *
 */
public class QueryTracker implements Serializable
{
    /**
     * Version of the Query Tracker object
     */
    private static final long serialVersionUID = 1L;

    private static final int MAX_STR_LENGTH = 30;

    /**
     * Unique ID of this tracker
     */
    private String id = null;

    /**
     * When the query started
     */
    private Date startTime = null;

    /**
     * HTTP query string
     */
    private String query = null;

    /**
     * SQL query string
     */
    private String sqlQuery = null;

    /**
     * Remote host that executed the query
     */
    private String remoteHost = null;

    /**
     * Statement object (must be a cancelable implementation)
     */
    transient private Statement statement = null;

    /**
     * Type of query
     */
    private String type = null;

    /**
     * Stack trace
     */
    private StackTraceElement[] stackTrace = null;

    /**
     * Username of user executing query
     */
    private String username = null;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    /**
     * Used for display purposes.
     *
     * @return shortened query with ellipses (...)
     */
    public String getShortQuery()
    {
        if (query == null)
        {
            return null;
        }
        return query.length() > MAX_STR_LENGTH ? (query.substring(0, (MAX_STR_LENGTH - 4)) + "...") : query;
    }

    public String getQuery()
    {
        return query;
    }

    /**
     * Returns a string representing how long query has been executing on the DB
     * in HH:MM:SS format
     *
     * @return length of time of execution
     */
    public String getExecutionTime()
    {
        int totalSeconds = (int)(System.currentTimeMillis() - startTime.getTime()) / 1000;
        int hours = totalSeconds / 3600;
        int remainder = totalSeconds % 3600;
        int minutes = remainder / 60;
        int seconds = remainder % 60;
        return (hours < 10 ? "0" : "") + hours + "H:" + (minutes < 10 ? "0" : "") + minutes + "M:" + (seconds < 10 ? "0" : "")
                + seconds + "S";
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getRemoteHost()
    {
        return remoteHost;
    }

    public void setRemoteHost(String originatingIP)
    {
        this.remoteHost = originatingIP;
    }

    public Statement getStatement()
    {
        return statement;
    }

    public void setStatement(Statement statement)
    {
        this.statement = statement;
    }

    /**
     * Used for display purposes for sql query
     *
     * @return shortened query with ellipses (...)
     */
    public String getShortSqlQuery()
    {
        return sqlQuery.length() > MAX_STR_LENGTH ? (sqlQuery.substring(0, (MAX_STR_LENGTH - 4)) + "...") : sqlQuery;
    }

    public String getSqlQuery()
    {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery)
    {
        this.sqlQuery = sqlQuery;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public StackTraceElement[] getStackTrace()
    {
        return stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackDump)
    {
        this.stackTrace = stackDump;
    }

    public String getShortStackTraceString()
    {
        String stackString = getStackTraceString();
        return stackString.length() > MAX_STR_LENGTH ? (stackString.substring(0, (MAX_STR_LENGTH - 4)) + "...") : stackString;
    }

    public String getStackTraceString()
    {
        StringBuilder stackString = new StringBuilder();
        if (stackTrace != null)
        {
            for (int i = 0; i < stackTrace.length; i++)
            {
                stackString.append("at ");
                stackString.append(stackTrace[i].getClassName());
                stackString.append(".");
                stackString.append(stackTrace[i].getMethodName());
                stackString.append("(");
                stackString.append(stackTrace[i].getFileName());
                stackString.append(":");
                stackString.append(stackTrace[i].getLineNumber());
                stackString.append(")");
                stackString.append("\n");
            }
        }
        return stackString.toString();
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

}
