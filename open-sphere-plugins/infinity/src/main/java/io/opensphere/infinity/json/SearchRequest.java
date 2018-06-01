package io.opensphere.infinity.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.ObjectMapper;

import io.opensphere.server.util.JsonUtils;

/** Elasticsearch request JSON bean. */
@JsonPropertyOrder({ "size", "timeout", "query", "aggs"})
public class SearchRequest
{
    private int mySize;

    private String myTimeout;

    private Query myQuery = new Query();

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

    public static void main(String[] args)
    {
        ObjectMapper mapper = JsonUtils.createMapper();
        SearchRequest request = new SearchRequest();
        request.setSize(0);
        request.setTimeout("30s");
        Object[] must = new Object[1];
        must[0] = new RangeHolder(123, 456);
        request.getQuery().getBool().setMust(must);
        request.setAggs(new Aggs("blah.keyword", 10000, 1000000000000000000L));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
            mapper.writeValue(System.out, request);
//            mapper.writeValue(os, request);
//            SearchRequest readValue = mapper.readValue(os.toByteArray(), SearchRequest.class);
//            System.out.println(((RangeHolder)readValue.getQuery().getBool().getMust()[0]).getRange().getTimefield().getGte());
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }
}
