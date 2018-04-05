package io.opensphere.test.core.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * An abstract server simulator.
 */
public abstract class AbstractServer
{
    /**
     * Starts the HTTP server.
     *
     * @param contexts the context(s)
     */
    public void startServer(String... contexts)
    {
        startServer(8080, contexts);
    }

    /**
     * Starts the HTTP server.
     *
     * @param port the port
     * @param contexts the context(s)
     */
    public void startServer(int port, String... contexts)
    {
        try
        {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            for (String context : contexts)
            {
                httpServer.createContext(context, this::handle);
            }
            httpServer.setExecutor(null);
            httpServer.start();
            String addresses = Arrays.stream(contexts).map(c -> "http://localhost:" + port + c).collect(Collectors.joining(", "));
            System.out.println("Service running at " + addresses);
            System.out.println("Type [CTRL]+[C] to quit!");
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }

    /**
     * Handles an exchange.
     *
     * @param exchange the exchange
     * @throws IOException Bad.
     */
    protected abstract void handle(HttpExchange exchange) throws IOException;

    /**
     * Writes the response to the exchange.
     *
     * @param exchange the exchange
     * @param statusCode the status code
     * @param responseBody the response body
     * @throws IOException if something bad happened
     */
    protected void writeResponse(HttpExchange exchange, int statusCode, byte[] responseBody) throws IOException
    {
        try (OutputStream os = exchange.getResponseBody())
        {
            int responseSize = responseBody != null ? responseBody.length : 0;
            exchange.sendResponseHeaders(statusCode, responseSize);
            if (responseBody != null)
            {
                os.write(responseBody);
            }
        }
    }
}
