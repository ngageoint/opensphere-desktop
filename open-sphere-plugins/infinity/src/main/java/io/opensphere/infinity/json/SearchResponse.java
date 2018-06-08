package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/** Elasticsearch search response JSON bean. */
@JsonPropertyOrder({ "hits", "aggregations" })
public class SearchResponse
{
    /** The hits. */
    private Hits myHits;

    /** The aggregations. */
    private Aggregations myAggregations;

    /**
     * Gets the hits.
     *
     * @return the hits
     */
    public Hits getHits()
    {
        return myHits;
    }

    /**
     * Sets the hits.
     *
     * @param hits the hits
     */
    public void setHits(Hits hits)
    {
        myHits = hits;
    }

    /**
     * Gets the aggregations.
     *
     * @return the aggregations
     */
    public Aggregations getAggregations()
    {
        return myAggregations;
    }

    /**
     * Sets the aggregations.
     *
     * @param aggregations the aggregations
     */
    public void setAggregations(Aggregations aggregations)
    {
        myAggregations = aggregations;
    }
}
