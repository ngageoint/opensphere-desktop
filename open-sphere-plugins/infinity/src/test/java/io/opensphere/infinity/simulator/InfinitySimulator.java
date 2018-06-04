package io.opensphere.infinity.simulator;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.sun.net.httpserver.HttpExchange;

import io.opensphere.core.server.AbstractServer;
import io.opensphere.infinity.json.Aggregations;
import io.opensphere.infinity.json.Bucket;
import io.opensphere.infinity.json.Hits;
import io.opensphere.infinity.json.SearchRequest;
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.server.util.JsonUtils;

/** Infinity servlet simulator. */
public class InfinitySimulator extends AbstractServer
{
    /**
     * The main.
     *
     * @param args program args.
     */
    public static void main(String[] args)
    {
        new InfinitySimulator().startServer("/ogc/infinityServlet");
    }

    @Override
    protected void handle(HttpExchange exchange) throws IOException
    {
        System.out.println("Request: " + exchange.getRequestURI());

        // Get the aggs field (bin field) if present
        SearchRequest request = JsonUtils.createMapper().readValue(exchange.getRequestBody(), SearchRequest.class);
        String aggsField = request.getAggs() != null ? request.getAggs().getBins().getTerms().getField() : null;

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        writeResponse(exchange, HttpURLConnection.HTTP_OK, getResponseBody(aggsField));

        System.out.println("Response: " + exchange.getResponseCode());
    }

    /**
     * Gets the response body.
     *
     * @param aggsField the aggs field (bin field)
     * @return the response body
     * @throws IOException if something bad happens
     */
    private byte[] getResponseBody(String aggsField) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectMapper mapper = JsonUtils.createMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.writeValue(output, getResponse(aggsField));
        return output.toByteArray();
    }

    /**
     * Gets the response.
     *
     * @param aggsField the aggs field (bin field)
     * @return the response
     */
    private SearchResponse getResponse(String aggsField)
    {
        SearchResponse response = new SearchResponse();
        int totalCount = 0;
        if (aggsField != null)
        {
            String[] bins = new String[] { "Ardbeg", "Bowmore", "Bruichladdich", "Bunnahabhain", "Caol Ila", "Kilchoman",
                "Lagavulin", "Laphroaig" };
            int numberOfBins = (int)(Math.random() * bins.length) + 1;
            Bucket[] buckets = new Bucket[numberOfBins];
            int binCount = 1000;
            for (int i = 0; i < numberOfBins; i++)
            {
                buckets[i] = new Bucket(bins[i], binCount);
                totalCount += binCount;
                binCount -= 100;
            }

            Aggregations aggregations = new Aggregations();
            aggregations.getBins().setBuckets(buckets);
            response.setAggregations(aggregations);
        }
        else
        {
            totalCount = (int)(Math.random() * 1000) + 1;
        }
        response.setHits(new Hits(totalCount));
        return response;
    }
}
