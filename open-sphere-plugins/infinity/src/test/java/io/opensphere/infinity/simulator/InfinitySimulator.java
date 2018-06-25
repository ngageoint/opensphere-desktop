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
import io.opensphere.mantle.infinity.InfinityUtilities;
import io.opensphere.server.util.JsonUtils;

/** Infinity servlet simulator. */
public class InfinitySimulator extends AbstractServer
{
    /** Flag indicating if response is numeric binning */
    private boolean isNumericBin = false;

    /** Bin width contained in numeric binning request*/
    private double myBinWidth = InfinityUtilities.DEFAULT_BIN_WIDTH;

    /** Bin offset contained in numeric binning request*/
    private double myBinOffset = InfinityUtilities.DEFAULT_BIN_OFFSET;

    /** Bin min_doc_count in numeric binning request*/
    private double myMinDocCount = 1;

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

        String aggsField = null;
        if (request.getAggs() != null && request.getAggs().getBins() != null)
        {
            if (request.getAggs().getBins().getTerms() != null)
            {
                isNumericBin = false;
                aggsField = request.getAggs().getBins().getTerms().getField();
                writeResponse(exchange, HttpURLConnection.HTTP_OK, getResponseBody(aggsField));
            }
            else if (request.getAggs().getBins().getHistogram() != null)
            {
                isNumericBin = true;
                myBinWidth = request.getAggs().getBins().getHistogram().getInterval();
                myBinOffset = request.getAggs().getBins().getHistogram().getOffset();
                myMinDocCount = request.getAggs().getBins().getHistogram().getMin_doc_count();
                aggsField = request.getAggs().getBins().getHistogram().getField();
                writeResponse(exchange, HttpURLConnection.HTTP_OK, getResponseBody(aggsField));
            }
        }

        exchange.getResponseHeaders().add("Content-Type", "application/json");
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
        if(isNumericBin)
        {
            mapper.writeValue(output, getNumericBinResponse(aggsField));
        }
        else
        {
            mapper.writeValue(output, getResponse(aggsField));
        }
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
            @SuppressWarnings("unchecked")
            Bucket<String>[] buckets = new Bucket[numberOfBins];
            int binCount = 1000;
            for (int i = 0; i < numberOfBins; i++)
            {
                buckets[i] = new Bucket<String>(bins[i], binCount);
                totalCount += binCount;
                binCount -= 100;
            }

            Aggregations aggregations = new Aggregations();
            aggregations.getBins().setBuckets(buckets);
            response.setAggregations(aggregations);
        }
        else
        {
            totalCount = (int)(Math.random() * 20000) + 1;
        }
        response.setHits(new Hits(totalCount));
        return response;
    }

    /**
     * Gets the response for numeric binning.
     *
     * @param aggsField the aggs field (bin field)
     * @return the response
     */
    private SearchResponse getNumericBinResponse(String aggsField)
    {
        SearchResponse response = new SearchResponse();
        int totalCount = 0;
        if (aggsField != null)
        {
            int maxBins = 10;
            int numberOfBins = (int)(Math.random() * maxBins) + 1;

            if(myMinDocCount == 0)
            {
                //Make room for empty bin
                numberOfBins++;
            }

            @SuppressWarnings("unchecked")
            Bucket<Double>[] buckets = new Bucket[numberOfBins];
            int binCount = 1000;
            double binIndex = myBinOffset;
            boolean addEmptyBin = (myMinDocCount == 0);
            int i = 0;
            while (i < numberOfBins)
            {
                buckets[i] = new Bucket<Double>(Double.valueOf(binIndex), binCount);
                binIndex += myBinWidth;
                totalCount += binCount;
                binCount -= 100;
                i++;

                if(addEmptyBin)
                {
                    buckets[i] = new Bucket<Double>(Double.valueOf(binIndex), 0);
                    binIndex += myBinWidth;
                    addEmptyBin = false;
                    i++;
                }
            }

            Aggregations aggregations = new Aggregations();
            aggregations.getBins().setBuckets(buckets);
            response.setAggregations(aggregations);
        }
        else
        {
            totalCount = (int)(Math.random() * 20000) + 1;
        }
        response.setHits(new Hits(totalCount));
        return response;
    }
}
