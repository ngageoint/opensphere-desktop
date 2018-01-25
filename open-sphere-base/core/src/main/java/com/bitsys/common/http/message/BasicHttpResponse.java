package com.bitsys.common.http.message;

import java.io.IOException;
import java.util.Map;

import com.bitsys.common.http.entity.HttpEntity;

/**
 * This class is a basic implementation of {@link HttpResponse}.
 */
public class BasicHttpResponse extends AbstractHttpMessage implements HttpResponse
{
   /**
    * The HTTP status code.
    */
   private final int statusCode;

   /**
    * The status message.
    */
   private final String statusMessage;

   /**
    * The HTTP response body entity.
    */
   private final HttpEntity entity;

   /**
    * Constructs a <code>BasicHttpResponse</code>.
    *
    * @param statusCode
    *           the HTTP status code.
    * @param statusMessage
    *           the status message.
    */
   public BasicHttpResponse(final int statusCode, final String statusMessage)
   {
      this(statusCode, statusMessage, null);
   }

   /**
    * Constructs a <code>BasicHttpResponse</code>.
    *
    * @param statusCode
    *           the HTTP status code.
    * @param statusMessage
    *           the status message.
    * @param entity
    *           the response body entity.
    */
   public BasicHttpResponse(final int statusCode, final String statusMessage, final HttpEntity entity)
   {
      if (statusMessage == null)
      {
         throw new IllegalArgumentException("The status message is null");
      }
      this.statusCode = statusCode;
      this.statusMessage = statusMessage;
      this.entity = entity;
   }

   @Override
   public HttpEntity getEntity()
   {
      return entity;
   }

   @Override
   public boolean isInformationalStatusCode() {
      return getStatusCode() >= 100 && getStatusCode() < 200;
   }

   @Override
   public boolean isSuccessfulStatusCode() {
      return getStatusCode() >= 200 && getStatusCode() < 300;
   }

   @Override
   public boolean isRedirectionStatusCode() {
      return getStatusCode() >= 300 && getStatusCode() < 400;
   }

   @Override
   public boolean isClientErrorStatusCode() {
      return getStatusCode() >= 400 && getStatusCode() < 500;
   }

   @Override
   public boolean isServerErrorStatusCode() {
      return getStatusCode() >= 500 && getStatusCode() < 600;
   }

   @Override
   public int getStatusCode()
   {
      return statusCode;
   }

   @Override
   public String getStatusMessage()
   {
      return statusMessage;
   }

   /**
    * Closes the entity's input stream.
    */
   @Override
   public void close() throws IOException {
      if (getEntity() != null && getEntity().getContent() != null) {
         getEntity().getContent().close();
      }
   }

   @Override
   public String toString()
   {
      final StringBuilder builder = new StringBuilder();
      builder.append(getStatusCode());
      builder.append(' ');
      builder.append(getStatusMessage());
      for (final Map.Entry<String, String> entry : getHeaders().entries())
      {
         builder.append('\n');
         builder.append(entry.getKey());
         builder.append(": ");
         builder.append(entry.getValue());
      }
      if (getEntity() != null)
      {
         builder.append('\n');
         builder.append("entity=");
         builder.append(getEntity());
      }
      return builder.toString();
   }
}
