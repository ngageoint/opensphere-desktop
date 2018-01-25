package com.bitsys.common.http.message;

import java.io.Closeable;

/**
 * This interface defines the contract for HTTP response messages.
 */
public interface HttpResponse extends HttpMessage, Closeable
{
   /**
    * Returns the HTTP status code.
    *
    * @return the HTTP status code.
    */
   int getStatusCode();

   /**
    * Returns <code>true</code> if the status code returned by
    * {@link #getStatusCode()} indicates an informational response (i.e. a
    * <code>1xx</code>-range code).
    *
    * @return <code>true</code> if the status code indicates an informational response.
    */
   boolean isInformationalStatusCode();

   /**
    * Returns <code>true</code> if the status code returned by
    * {@link #getStatusCode()} indicates a successful response (i.e. a
    * <code>2xx</code>-range code).
    *
    * @return <code>true</code> if the status code indicates a successful response.
    */
   boolean isSuccessfulStatusCode();

   /**
    * Returns <code>true</code> if the status code returned by
    * {@link #getStatusCode()} indicates a redirection response (i.e. a
    * <code>3xx</code>-range code).
    *
    * @return <code>true</code> if the status code indicates a redirection response.
    */
   boolean isRedirectionStatusCode();

   /**
    * Returns <code>true</code> if the status code returned by
    * {@link #getStatusCode()} indicates a client error response (i.e. a
    * <code>4xx</code>-range code).
    *
    * @return <code>true</code> if the status code indicates a client error response.
    */
   boolean isClientErrorStatusCode();

   /**
    * Returns <code>true</code> if the status code returned by
    * {@link #getStatusCode()} indicates a server error response (i.e. a
    * <code>5xx</code>-range code).
    *
    * @return <code>true</code> if the status code indicates a server error response.
    */
   boolean isServerErrorStatusCode();

   /**
    * Returns the HTTP status message.
    *
    * @return the HTTP status message.
    */
   String getStatusMessage();
}
