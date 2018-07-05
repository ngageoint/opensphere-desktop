package io.opensphere.infinity.simulator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.sun.net.httpserver.HttpExchange;

import io.opensphere.core.server.AbstractServer;
import io.opensphere.infinity.json.Aggregations;
import io.opensphere.infinity.json.Bucket;
import io.opensphere.infinity.json.Hits;
import io.opensphere.infinity.json.Script;
import io.opensphere.infinity.json.SearchRequest;
import io.opensphere.infinity.json.SearchResponse;
import io.opensphere.mantle.infinity.InfinityUtilities;
import io.opensphere.server.util.JsonUtils;

/** Infinity servlet simulator. */
public class InfinitySimulator extends AbstractServer
{
    /** Bin width contained in numeric binning request. */
    private double myBinWidth = InfinityUtilities.DEFAULT_BIN_WIDTH;

    /** Bin offset contained in numeric binning request. */
    private double myBinOffset = InfinityUtilities.DEFAULT_BIN_OFFSET;

    /** The date format received during date binning. */
    private String myDateFormat = InfinityUtilities.DEFAULT_DATE_BIN_FORMAT;

    /** The dayOfWeek flag is using script binning. */
    private Script myScript;

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
                aggsField = request.getAggs().getBins().getTerms().getField();
                myScript = request.getAggs().getBins().getTerms().getScript();
                writeResponse(exchange, HttpURLConnection.HTTP_OK, getResponseBody(aggsField, false, false));
            }
            else if (request.getAggs().getBins().getHistogram() != null)
            {
                myBinWidth = request.getAggs().getBins().getHistogram().getInterval();
                myBinOffset = request.getAggs().getBins().getHistogram().getOffset();
                aggsField = request.getAggs().getBins().getHistogram().getField();
                writeResponse(exchange, HttpURLConnection.HTTP_OK, getResponseBody(aggsField, true, false));
            }
            else if (request.getAggs().getBins().getDate_histogram() != null)
            {
                myDateFormat = request.getAggs().getBins().getDate_histogram().getFormat();
                aggsField = request.getAggs().getBins().getDate_histogram().getField();
                writeResponse(exchange, HttpURLConnection.HTTP_OK, getResponseBody(aggsField, false, true));
            }
        }

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        System.out.println("Response: " + exchange.getResponseCode());
    }

    /**
     * Gets the response body.
     *
     * @param aggsField the aggs field (bin field)
     * @param isNumericBin whether is a numeric bin
     * @param isDateBin whether is a date bin
     * @return the response body
     * @throws IOException if something bad happens
     */
    private byte[] getResponseBody(String aggsField, boolean isNumericBin, boolean isDateBin) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectMapper mapper = JsonUtils.createMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        if (isNumericBin)
        {
            mapper.writeValue(output, getNumericBinResponse(aggsField));
        }
        else if (isDateBin)
        {
            mapper.writeValue(output, getDateBinResponse(aggsField));
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
        if (aggsField != null || myScript != null)
        {
            String[] bins;

            if (myScript != null)
            {
                // Represents dayOfWeek or hourOfDay
                bins = new String[] { "1", "2", "3", "4", "5", "6", "7" };
            }
            else
            {
                bins = new String[] { "Ardbeg", "Bowmore", "Bruichladdich", "Bunnahabhain", "Caol Ila", "Kilchoman", "Lagavulin",
                    "Laphroaig" };
            }

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

            @SuppressWarnings("unchecked")
            Bucket<Double>[] buckets = new Bucket[numberOfBins];
            int binCount = 1000;
            double binIndex = myBinOffset;
            for (int i = 0; i < numberOfBins; i++)
            {
                buckets[i] = new Bucket<Double>(Double.valueOf(binIndex), binCount);
                binIndex += myBinWidth;
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
     * Gets the response for date binning.
     *
     * @param aggsField the aggs field (bin field)
     * @return the response
     */
    private SearchResponse getDateBinResponse(String aggsField)
    {
        SearchResponse response = new SearchResponse();
        int totalCount = 0;
        if (aggsField != null)
        {
            LocalDateTime[] bins = new LocalDateTime[] { LocalDateTime.of(1998, Month.JANUARY, 25, 21, 15),
                LocalDateTime.of(1999, Month.JANUARY, 31, 20, 45), LocalDateTime.of(2016, Month.FEBRUARY, 7, 19, 18),
                LocalDateTime.of(1865, Month.NOVEMBER, 11, 11, 00), LocalDateTime.of(1945, Month.SEPTEMBER, 11, 8, 12), };

            int numberOfBins = (int)(Math.random() * bins.length) + 1;

            @SuppressWarnings("unchecked")
            Bucket<Long>[] buckets = new Bucket[numberOfBins];
            int binCount = 1000;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(myDateFormat);
            for (int i = 0; i < numberOfBins; i++)
            {
                buckets[i] = new Bucket<Long>(Long.valueOf(bins[i].toEpochSecond(ZoneOffset.UTC)), binCount,
                        bins[i].format(formatter));
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
}
