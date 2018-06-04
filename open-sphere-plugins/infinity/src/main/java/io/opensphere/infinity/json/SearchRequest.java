package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/** Elasticsearch search request JSON bean. */
@JsonPropertyOrder({ "size", "timeout", "query", "aggs"})
public class SearchRequest
{
    /** The size. */
    private int mySize;

    /** The timeout. */
    private String myTimeout;

    /** The query. */
    private Query myQuery = new Query();

    /** The aggs. */
    private Aggs myAggs;

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize()
    {
        return mySize;
    }

    /**
     * Sets the size.
     *
     * @param size the size
     */
    public void setSize(int size)
    {
        mySize = size;
    }

    /**
     * Gets the timeout.
     *
     * @return the timeout
     */
    public String getTimeout()
    {
        return myTimeout;
    }

    /**
     * Sets the timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(String timeout)
    {
        myTimeout = timeout;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public Query getQuery()
    {
        return myQuery;
    }

    /**
     * Sets the query.
     *
     * @param query the query
     */
    public void setQuery(Query query)
    {
        myQuery = query;
    }

    /**
     * Gets the aggs.
     *
     * @return the aggs
     */
    public Aggs getAggs()
    {
        return myAggs;
    }

    /**
     * Sets the aggs.
     *
     * @param aggs the aggs
     */
    public void setAggs(Aggs aggs)
    {
        myAggs = aggs;
    }
}
